import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.*;
import java.util.Arrays;
import java.util.Map;

public class UNSWArray {
    private int[] array;
    private Map<Integer, ReadWriteLock> locks;

    public UNSWArray() {
        this.array = new int[0];
        locks = new ConcurrentHashMap<>();
    }

    public UNSWArray(int... args) {
        array = args;
        locks = new ConcurrentHashMap<>();
    }

    public int[] getArray() {
        return array;
    }

    public void insert(int x) {
        // if array is empty, initialise new array
        System.out.println("inserting " + x);
        if (array.length == 0) {
            array = new int[] {x};
            System.out.println("inserted " + x);
        } else {
            int index = Arrays.binarySearch(array, x);
            if (index < 0) {
                index = -index - 1; // Arrays.binarySearch() returns negative value of where index should be
            }

            // check if index is currently locked
            while (locks.containsKey(index)) {
                // if yes, then try again
                index = Arrays.binarySearch(array, x);
                if (index < 0) {
                    index = -index - 1; // Arrays.binarySearch() returns negative value of where index should be
                }
            }
            // if not locked, create lock + insert + release lock
            try {
                createLock(index);
                insertIntoArray(index, x);
            } finally {
                releaseLock(index);
            }
        }
    }

    private void createLock(int index) {
        ReadWriteLock newLock = new ReentrantReadWriteLock();
        locks.put(index, newLock);
    }

    private void releaseLock(int index) {
        ReadWriteLock lock = locks.get(index);
        locks.remove(index, lock);
        if (lock != null && ((ReentrantReadWriteLock) lock).isWriteLockedByCurrentThread()) {
            lock.writeLock().unlock();
            // locks.remove(index, lock);
        }
    }

    // TODO: CLEAN UP NEGATIVE VALUES
    private void insertIntoArray(int index, int x) {
        // make copy of array +1 length
        array = Arrays.copyOf(array, array.length + 1);
        // shift everything past value down
        for (int i = array.length - 1; i > index; i--) {
            array[i] = array[i - 1];
        }
        array[index] = x;
        System.out.println("inserted " + x);
    }
}