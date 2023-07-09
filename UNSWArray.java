import java.util.concurrent.locks.*;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
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

    // ReentrantLock to allow only one process to drain from the insert queue at a time
    private final ReentrantLock insertQueueLock;

    // A queue to buffer insert operations
    private ArrayBlockingQueue<Integer> insertQueue;

    // A variable to keep track of how much of the array we have currently used up
    private int currentMaxIndex;

    // A variable to keep track of the current size of the array
    // When we exceed space in the array we will make a new copy and double the size
    private int currentArraySize;

    // === Private Helper Functions ===

    

    // === Public Methods ===

    // Insert a value into the array "atomically"
    public int insert(int x) {
        // First add the item to the insert queue - we allow it to block if the queue is full!
        try {
            insertQueue.put(x);
        } catch (InterruptedException e) {
            return -1; // we were unable to put it in the queue
        }
        

        // Now attempt to acquire the insertQueue lock so we can drain from the insertQueue and update the actual array
        boolean ourLock = insertQueueLock.tryLock();

        if (ourLock == false) {
            return 0; // if we can't acquire the lock exit out of the function - another thread will be taking care of the inserts into the array
            // Returning 0 is a special value to indiciate it was added to the queue successfully but this thread won't be taking care of the inserts into the array
        }

        // If we get here we have obtained the insertQueueLock - enclose remaining code in try / finally for safety
        try {

            // We also need to now acquire the globalLock (writeLock) so we can update the array and ensure no other writes are occurring or readers are reading
            w.lock();

            // Try / finally construct recommended to ensure prevention of deadlock
            try {
                // Drain the insert queue (up to a maximum of 100 integers to prevent starvation of other potential reader threads) and insert into the array
                for (int i = 0; i < 100; i++) {
                    // Remove the end of the queue / if it is empty returns null
                    Object valToInsertO = insertQueue.poll();

                    // If the queue is empty we're done
                    if (valToInsertO == null) {
                        break;
                    }

                    int valToInsert = (int) valToInsertO;

                    // Otherwise insert it into the array -- WILL NEED TO REWRITE THIS TO ENSURE SORTED ARRAY
                    if (currentMaxIndex < currentArraySize) {
                        array[currentMaxIndex] = valToInsert;
                        currentMaxIndex++;
                    }
                }

            } finally {
                // release the global write lock allowing readers to read again
                w.unlock();
            }

            
        } finally {
            // release the insert queue lock allowing for insertion operations to occur again
            insertQueueLock.unlock();
        }

        return 1; // special value to indicate success and this thread took care of the inserts into the array
    }

    // Delete a value from the array "atomically" -- TODO STILL
    public boolean delete(int x) {
        return true;
    }

    // === Constructors ===

    public UNSWArray(int size) {
        // Initialise the array and metadata
        this.array = new int[size];
        this.currentMaxIndex = 0;
        this.currentArraySize = size;

        // Initialise the locks
        globalLock = new ReentrantReadWriteLock(true); //fairness enabled
        r = globalLock.readLock();
        w = globalLock.writeLock();

        insertQueueLock = new ReentrantLock(true); //fairness enabled

        // Initailise the insert queue
        insertQueue = new ArrayBlockingQueue<>(100, true); //allow for a maximum of 100 insert operations to be queued at one time
    }
    
    // This is for testing purposes only at this stage
    public void printArray() {
        System.out.println(Arrays.toString(this.array));
    }

    // === Tests ===

    public static void Test1() {
        UNSWArray a1 = new UNSWArray(100);

        Thread insertThread1 = new Thread(() -> {
            for (int i = 0; i <= 98; i = i + 2) {
                a1.insert(i);

                if (i % 10 == 0) {
                    try {
                        Thread.sleep(8);
                    } catch (InterruptedException e) {

                    }
                }
            }
        });

        Thread insertThread2 = new Thread(() -> {
            for (int i = 1; i <= 99; i = i + 2) {
                a1.insert(i);
                if (i % 5 == 1) {
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