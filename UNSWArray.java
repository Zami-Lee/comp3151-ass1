import java.util.concurrent.locks.*;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.lang.Thread.*;

public class UNSWArray {
    // === Class Attributes ===

    // The array itself
    private int[] array;

    // ReentrantReadWriteLock to prevent multiple writes occuring at the same time while allowing multiple readers if no writers
    // Also supports a fairness parameter which we will make use of
    private final ReentrantReadWriteLock globalLock;
    private final Lock r;
    private final Lock w;

    // A queue to buffer insert operations - meaning in theory we can gather a number of insert operations in the queue and then apply them all at once
    private ArrayBlockingQueue<Integer> insertQueue;

    // ReentrantLock to allow only one process to drain from the insert queue at a time
    private final ReentrantLock insertQueueDrainLock;

    // Semaphore to block array from exceeding maximum size and block calls to insert until there is space
    private final Semaphore sizeCheck;

    // === Private Helper Functions ===

    // The calling function must have acquired the global write lock before using this function to ensure mutex
    // Provides the actual logic of inserting a value into the array
    // This is currently O(n log n) needs to be replaced with O(n) logic
    private void insertIntoArray(int val) {
        array[0] = val;
        Arrays.sort(array,0,array.length);
    }

    // The calling function must have acquired the global write lock before using this function to ensure mutex
    // Provides the actual logic of the cleanup operation and pushes -1 values as far to the end as possible up to "upTo"
    // This is currently O(n log n) needs to be updated with O(n) logic
    private void forceCleanup(int upTo) {
        if (upTo <= 0) {
            return; //don't do any thing
        }else{
            Arrays.sort(array,0,upTo + 1);
        }
    }

    // The calling function should IDEALLY (but not necessarily) have obtained the global read lock before calling this function
    // Finds the index of the value "x" in the array or returns -1 if not found
    // Comments within this function are sparse - it just works...
    private int findIndex(int x) {
        int low = 0;
        int high = array.length - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            int localVal = array[mid];

            if (localVal == x) {
                return mid;
            }else if (localVal == -1) {
                int lowMid = mid; int highMid = mid;

                while (array[lowMid] == -1 && lowMid > low) {
                    lowMid--;
                }

                while (array[highMid] == -1 && highMid < high) {
                    highMid++;
                }

                if (array[lowMid] == x) {
                    return lowMid;
                }else if (array[highMid] == x) {
                    return highMid;
                }else if (array[lowMid] == -1 && array[highMid] == -1) {
                    return -1;
                }else if (x < array[lowMid]) {
                    high = lowMid - 1;
                }else if (x > array[highMid]) {
                    if (array[highMid] == -1) {
                        high = mid - 1;
                    }else {
                        low = highMid + 1;
                    }
                }

            }else if (localVal > x) {
                high = mid - 1;
            }else { //localVal < x
                low = mid + 1;
            }
        }

        return -1;
    }

    // === Public Methods ===

    // Insert a value into the array "atomically"
    public int insert(int x) {
        // First add the item to the insert queue - we allow it to block if the queue is full!
        try {
            // wait until there is room in the array - the sizeCheck semaphore ensures this
            // deletion operations will release permits back to the semaphore and unblock this if required
            sizeCheck.acquire();
            // If put() is interrupted without succeeding we want to hand back a permit
            try {
                insertQueue.put(x);
            } catch (InterruptedException e) {
                // Error handling - hand back permit
                sizeCheck.release();
                return -1;
            }
        } catch (InterruptedException e) {
            return -1; // we were unable to put it in the queue
        }


        // Now attempt to acquire the insertQueueDrain lock so we can drain from the insertQueue and update the actual array
        boolean ourLock = insertQueueDrainLock.tryLock();

        if (ourLock == false) {
            return 0; // if we can't acquire the lock exit out of the function - another thread will be taking care of the inserts into the array
            // Returning 0 is a special value to indiciate it was added to the queue successfully but this thread won't be taking care of the inserts into the array
        }

        // If we get here we have obtained the insertQueueDrainLock - enclose remaining code in try / finally for safety
        try {

            // We also need to now acquire the globalLock (writeLock) so we can update the array and ensure no other writes are occurring or readers are reading
            w.lock();

            // Try / finally construct recommended to ensure prevention of deadlock
            try {
                // Drain the insert queue (up to a maximum of 100 integers at one time to prevent starvation of other potential reader threads) and insert into the array
                for (int i = 0; i < 100; i++) {
                    // Remove the end of the queue / if it is empty returns null
                    Object valToInsertO = insertQueue.poll();

                    // If the queue is empty we're done
                    if (valToInsertO == null) {
                        break;
                    }

                    // Cast-type to integer
                    int valToInsert = (int) valToInsertO;

                    // Now - actually insert the integer into the array
                    this.insertIntoArray(valToInsert);
                }

            } finally {
                // release the global write lock allowing readers to read again
                w.unlock();
            }


        } finally {
            // release the insert queue drain lock allowing for insertion operations to occur again
            insertQueueDrainLock.unlock();
        }

        return 1; // special value to indicate success and this thread took care of the inserts into the array
    }

    // Delete a value from the array "atomically"
    public void delete(int x) {
        // acquire global read lock as deletes can occur at the same time as reads and membership checks but not writes or cleanups
        r.lock();

        // Try / finally block for safety
        try {
            // Find the index where the value is currently stored
            int index = findIndex(x);
            // if it exists set the value to -1 to "delete" it
            if (index != -1) {
                array[index] = -1;
            }
            // IMPORTANT - we release a permit on the sizeCheck semaphore to unblock any waiting processes that might be waiting to insert a value into a full array
            sizeCheck.release();
        } finally {
            r.unlock();
        }
    }

    // Public facing cleanup function to remove -1 values
    // Wrapper around mutex forceCleanup function
    public boolean cleanup() {
        // First acquire global write lock
        w.lock();

        // Try / finally block for safety
        try {
            // Perform a cleanup which requires mutex
            this.forceCleanup(array.length - 1);
        } finally {
            w.unlock();
        }

        return true;
    }

    // Public facing set membership function
    // Effectively wraps findIndex and returns true if a positive value is returned and false if -1 is returned
    public boolean member(int x) {
        // First acquire global read lock
        r.lock();

        // Value to return
        boolean returnVal = false;

        // Try / finally block for safety
        try {
            // We attempt to find the index of the value in the array
            int index = findIndex(x);

            // If it's found we set the return value to true
            if (index != -1) {
                returnVal = true;
            }

        } finally {
            r.unlock();
        }

        return returnVal;
    }

    // === Constructors ===

    public UNSWArray(int size) {
        // Initialise the array and metadata
        this.array = new int[size];

        // Set all values to -1 by default
        for (int i = 0; i < size; i++) {
            this.array[i] = -1;
        }

        // Initialise the locks
        globalLock = new ReentrantReadWriteLock(true); //fairness enabled
        r = globalLock.readLock();
        w = globalLock.writeLock();

        insertQueueDrainLock = new ReentrantLock(true); //fairness enabled

        // Initailise the insert queue
        insertQueue = new ArrayBlockingQueue<>(100, true); //allow for a maximum of 100 insert operations to be queued at one time

        // Initialise the sizeCheck semaphore - ensuring that it is fair and provies "size" number of permits
        sizeCheck = new Semaphore(size, true);
    }

    // This is for testing purposes only at this stage
    public void printArray() {
        System.out.println(Arrays.toString(this.array));
    }

    // === Tests ===

    public static void Test1() {
        UNSWArray a1 = new UNSWArray(20);

        Thread insertThread1 = new Thread(() -> {
            for (int i = 0; i <= 19; i++) {
                System.out.println("Inserting: " + i);
                a1.insert(i);

                if (i % 2 == 0) {
                    try {
                        Thread.sleep(8);
                    } catch (InterruptedException e) {

                    }
                }
            }
        });

        Thread insertThread2 = new Thread(() -> {
            for (int i = 0; i <= 9; i++) {
                System.out.println("Deleting: " + i);
                a1.delete(i);
                if (i % 3 == 1) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {

                    }
                }
            }
        });

        insertThread1.start();
        insertThread2.start();

        try {
            insertThread1.join();
            insertThread2.join();
        } catch (InterruptedException e) {}


        a1.printArray();

        return;
    }

    // === Main Function ===

    public static void main(String[] args) {
        UNSWArray.Test1();
    }

}
