// For the purpose of testing the concurrency logic we'll define our array size to be 2
// This will allow for the sizeCheck Semaphore to block processes quite easily without having to do massive numbers of "insert" operations
#define ARRAYSIZE 2

// If either thread holds a global read lock their respective globalWRLock flag will be '1'
// Similarly, the flag for global write lock is '2'
// Note: Just like the Java implementation multiple threads can hold a read lock at the same time but only one thread can hold a write lock (no read locks at the same time either)
byte globalRWLock[2] = {0,0};

// These flags indicate what operation their respective thread is currently performing
// '0': non-critical
// '1': insert -> requesting insert by pushing to queue
// '2': cleanup
// '3': delete
// '4': member
// '5': insert -> performing actual array insert
byte flags[2] = {0,0};

// The following lock allows for inserts to perform semi-concurrently
// We allow in the Java implementation for inserts to add their request to a queue (blocking only if the array is full)
// The thread will then either attempt to acquire a lock exclusive to all other threads that might be inserting right now
// called insertQueueDrainLock (below). This gives it the right to actually edit the underlying array. Other threads will poll this
// lock and if they can't acquire it simply exit the insert on their end after adding to the queue and move on, knowing that another thread will actually
// take care of adding the value it wants to insert to the underlying array.
// '0': not holding lock
// '1': holding lock
byte insertQueueDrainLock[2] = {0,0};

// Semaphore - controls blocking of insert operations when the array is full
// Note that we obviously set the number of permits to the size of the array
int sizeCheck = ARRAYSIZE;

/*
    FUNCTION DEFINITIONS FOR NON CRITICAL SECTION AND OPERATIONS
*/

inline non_critical_section() {
    // Set operation flag to non-critical
    flags[_pid] = 0;
    // Remain in non critical section for a non deterministic amount of time
    do
        :: true -> skip;
        :: true -> break;
    od
}

inline perform_operation() {
    // Repeat for a non deterministic amount of time
    int n = 0;
    do
        :: true -> skip;
        :: true -> break;
    od
}

inline insert() {

    // "Acquire a permit" from the sizeCheck semaphore (the acquisition of the permit itself should be atomic)
    // The act of acquiring this permit means we have been given room in the array to insert
    byte havePermit = 0;
    do
        :: (havePermit == 0) -> d_step {
            if
                :: (sizeCheck > 0) -> sizeCheck = sizeCheck - 1; havePermit = 1;
                :: else -> skip;
            fi
        };
        :: (havePermit == 1) -> break;
    od

    // Set operation flag to requesting insert
    flags[_pid] = 1;

    // Determine if we are the thread that will be draining the insert queue to actually perform the insertions into the array
    // This represents acquiring an exclusive insertQueueDrainLock lock as in the Java implementation
    byte drainQueue = 0;
    d_step {
        if
            :: (insertQueueDrainLock[0] == 1 || insertQueueDrainLock[1] == 1) -> skip;
            :: else -> insertQueueDrainLock[_pid] = 1; drainQueue = 1;
        fi
    }

    // If we are the process that acquired the drain queue lock continue, otherwise we're done here
    if
        :: (drainQueue == 1) ->
            // Acquire the global write lock
            byte gWLock = 0;
            do
                :: (gWLock == 0) -> d_step {
                    if
                        :: (globalRWLock[0] == 0 && globalRWLock[1] == 0) -> globalRWLock[_pid] = 2; gWLock = 1;
                        :: else -> skip;
                    fi
                }
                :: else -> break;
            od

            // CRITICAL SECTION - INSERTING
            flags[_pid] = 5;
            // Stay inside this operation for a non-deterministic amount of time - not because insert is non deterministic but we want to allow time for SPIN to interleave
            perform_operation();

            int n = 1; // Don't know why but this is needed to prevent "jump into d_step sequence" error

            // Hand back the global write lock and drain queue lock
            d_step {
                // Atomically check that no other insert is waiting in the queue right before handing back the lock to prevent starvation of a queued insert
                insertQueueDrainLock[_pid] = 0;
                globalRWLock[_pid] = 0;
                // Set flags back to non critical as we're done
                flags[_pid] = 0;
            }


        :: else -> flags[_pid] = 0; skip; // Set flags back to non critical as we're done
    fi
}

inline delete() {

    // Acquire the global read lock
    byte gRLock = 0;
    do
        :: (gRLock == 0) -> d_step {
            if
                :: (globalRWLock[0] != 2 && globalRWLock[1] != 2) -> globalRWLock[_pid] = 1; gRLock = 1;
                :: else -> skip;
            fi
        }
        :: else -> break;
    od

    // Once we have obtained the global read lock perform the operations

    // CRITICAL SECTION - DELETING
    // Set operation flag to delete
    flags[_pid] = 3;

    // Perform the operation
    perform_operation();

    // "Release a permit" from the sizeCheck semaphore (the release of the permit itself should be atomic)
    // In Promela / SPIN we'll just check to make sure we don't exceed ARRAYSIZE so we don't accidentally give back too many permits
    byte havePermit = 0;
    do
        :: (havePermit == 0) -> d_step {
            if
                :: (sizeCheck > 0) -> sizeCheck = sizeCheck - 1; havePermit = 1;
                :: else -> skip;
            fi
        };
        :: (havePermit == 1) -> break;
    od

    int n = 1; // Don't know why but this is needed to prevent "jump into d_step sequence" error

    d_step {
        // Hand the read lock back now we're done
        globalRWLock[_pid] = 0;

        // Consider setting flags back to non critical when done
        flags[_pid] = 0;
    }
}

inline member() {

    // Acquire the global read lock
    byte gRLock = 0;
    do
        :: (gRLock == 0) -> d_step {
            if
                :: (globalRWLock[0] != 2 && globalRWLock[1] != 2) -> globalRWLock[_pid] = 1; gRLock = 1;
                :: else -> skip;
            fi
        }
        :: else -> break;
    od

    // Once we have obtained the global read lock perform the operations

    // CRITICAL SECTION - MEMBER CHECKING
    // Set operation flag to member check
    flags[_pid] = 4;

    // Perform the operation
    perform_operation();

    int n = 1;

    d_step {
        // Hand the read lock back now we're done
        globalRWLock[_pid] = 0;

        // Consider setting flags back to non critical when done
        flags[_pid] = 0;
    }
}

inline cleanup() {

    // Acquire the global write lock
    byte gWLock = 0;
    do
        :: (gWLock == 0) -> d_step {
            if
                :: (globalRWLock[0] == 0 && globalRWLock[1] == 0) -> globalRWLock[_pid] = 2; gWLock = 1;
                :: else -> skip;
            fi
        }
        :: else -> break;
    od

    // Once we have obtained the global write lock perform the operations

    // CRITICAL SECTION - CLEANUP
    // Set operation flag to cleanup
    flags[_pid] = 2;

    // Perform the operation
    perform_operation();

    int n = 1;

    d_step {
        // Hand the write lock back now we're done
        globalRWLock[_pid] = 0;

        // Consider setting flags back to non critical when done
        flags[_pid] = 0;
    }
}

// When we test this model in SPIN we assume weak fairness as in the Java implementation we set all locks and semaphore

active[2] proctype arrayThreads() {
    int i = 0;
    do
        :: insert();
        :: delete();
        :: member();
        :: cleanup();
        :: non_critical_section();
    od
}

// DEFINE LTL MUTEX PROPERTIES FOR TESTING BELOW
ltl boundedSize {[]!(sizeCheck < 0 || sizeCheck > ARRAYSIZE)}
ltl mutex {[]!((flags[0] == 2 && flags[1] > 1) || (flags[0] == 5 && flags[1] > 1))}


// These flags indicate what operation their respective thread is currently performing
// '0': non-critical
// '1': insert request -> requesting insert by pushing to queue
// '2': cleanup
// '3': delete
// '4': member
// '5': insert -> performing actual array insert
