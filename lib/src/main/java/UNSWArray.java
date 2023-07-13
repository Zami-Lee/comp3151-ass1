import java.util.concurrent.locks.*;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

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

    private void swapElement(int x, int y) {
        int temp = array[x];
        array[x] = array[y];
        array[y] = temp;
    }

    // The calling function must have acquired the global write lock before using this function to ensure mutex
    // Provides the actual logic of inserting a value into the array
    // Insert at beginning and keep swapping until order is correct - O(n) since the array is always sorted before insertion
    private void insertIntoArray(int val) {
        array[0] = val;

        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i+1]) {
                swapElement(i, i+1);
            }
        }
    }

    // The calling function must have acquired the global write lock before using this function to ensure mutex
    // Provides the actual logic of the cleanup operation and pushes -1 values as far to the end as possible up to "upTo"
    // For all -1 values, swap until they reach the end - O(n) since the array is always sorted before
    private void forceCleanup(int upTo) {
        if (upTo <= 0) {
            return; //don't do any thing
        } else {
            for (int i = 0; i < upTo; i++) {
                if (array[i+1] == -1) {
                    swapElement(i, i+1);
                }
            }
        }
    }

    // The calling function should IDEALLY (but not necessarily) have obtained the global read lock before calling this function
    // Finds the index of the value "x" in the array or returns -1 if not found
    // Comments within this function are sparse - it just works...
    private int findIndex(int x) {
        int low = 0;
        int high = array.length - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;

            // skip all negative values
            while (array[mid] == -1 && mid < high) {
                mid++;
            }

            if (mid > high) {
                return -1;
            }

            // return index of element
            if (array[mid] == x) {
                return mid;

            } else if (array[mid] < x) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        // if element does not exist
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

                    // do not insert duplicate
                    if (findIndex(valToInsert) != -1) {
                        return 1;
                    }

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

    // Public getter for testing
    public int[] getArray() {
        return array;
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
}

// import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.locks.*;
// import java.util.Arrays;
// import java.util.Map;

// public class UNSWArray {
//     private int[] array;
//     private Map<Integer, ReadWriteLock> locks;

//     public UNSWArray() {
//         this.array = new int[0];
//         locks = new ConcurrentHashMap<>();
//     }

//     public UNSWArray(int... args) {
//         array = args;
//         locks = new ConcurrentHashMap<>();
//     }

//     public int[] getArray() {
//         return array;
//     }

//     public void insert(int x) {
//         // if array is empty, initialise new array
//         System.out.println("inserting " + x);
//         if (array.length == 0) {
//             array = new int[] {x};
//             System.out.println("inserted " + x);
//         } else {
//             int index = Arrays.binarySearch(array, x);
//             if (index < 0) {
//                 index = -index - 1; // Arrays.binarySearch() returns negative value of where index should be
//             }

//             // check if index is currently locked
//             while (locks.containsKey(index)) {
//                 // if yes, then try again
//                 index = Arrays.binarySearch(array, x);
//                 if (index < 0) {
//                     index = -index - 1; // Arrays.binarySearch() returns negative value of where index should be
//                 }
//             }
//             // if not locked, create lock + insert + release lock
//             try {
//                 createLock(index);
//                 insertIntoArray(index, x);
//             } finally {
//                 releaseLock(index);
//             }
//         }
//     }

//     public void delete(int x) {
//         // if array is empty, return empty array
//         System.out.println("deleting " + x);
//         if (array.length == 0) {
//             System.out.println("nothing to delete");
//         } else {
//             // find index
//             int index = binarySearch(x);
//             // if x does not exist
//             if (index < 0) {
//                 System.out.println("element does not exist");
//                 return;
//             } else {
//                 // check if index is currently locked
//                 while (locks.containsKey(index)) {
//                     // if yes, then try again
//                     index = binarySearch(x);
//                 }
//                 // if not locked, create lock + insert + release lock
//                 try {
//                     createLock(index);
//                     array[index] = -1;
//                     System.out.println("deleted " + x);
//                 } finally {
//                     releaseLock(index);
//                 }
//             }
//         }
//     }

//     public boolean member(int x) {
//         return false;
//     }


//     private void createLock(int index) {
//         ReadWriteLock newLock = new ReentrantReadWriteLock();
//         locks.put(index, newLock);
//     }

//     private void releaseLock(int index) {
//         ReadWriteLock lock = locks.get(index);
//         locks.remove(index, lock);
//         if (lock != null && ((ReentrantReadWriteLock) lock).isWriteLockedByCurrentThread()) {
//             lock.writeLock().unlock();
//         }
//     }

//     // TODO: clean up negative values and do not insert duplicates
//     private void insertIntoArray(int index, int x) {
//         // make copy of array +1 length
//         // TODO: CHECK ATOMIC
//         array = Arrays.copyOf(array, array.length + 1);
//         // shift everything past x down
//         for (int i = array.length - 1; i > index; i--) {
//             array[i] = array[i - 1];
//         }
//         array[index] = x;
//         System.out.println("inserted " + x);
//     }

//     // https://www.geeksforgeeks.org/binary-search/
//     private int binarySearch(int x) {
//         int low = 0;
//         int high = array.length - 1;

//         while (low <= high) {
//             int mid = low + (high - low) / 2;

//             // skip all negative values
//             while (array[mid] == -1 && mid <= high) {
//                 mid++;
//             }

//             if (mid > high) {
//                 return -1;
//             }

//             if (array[mid] == x) {
//                 return mid;
//             } else if (array[mid] < x) {
//                 low = mid + 1;
//             } else {
//                 high = mid - 1;
//             }
//         }
//         return -1;
//     }
// }
