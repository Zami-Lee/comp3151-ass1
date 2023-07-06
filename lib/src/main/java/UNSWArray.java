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

    public void delete(int x) {
        // if array is empty, return empty array
        System.out.println("deleting " + x);
        if (array.length == 0) {
            System.out.println("nothing to delete");
        } else {
            // find index
            int index = binarySearch(x);
            // if x does not exist
            if (index < 0) {
                System.out.println("element does not exist");
                return;
            } else {
                // check if index is currently locked
                while (locks.containsKey(index)) {
                    // if yes, then try again
                    index = binarySearch(x);
                }
                // if not locked, create lock + insert + release lock
                try {
                    createLock(index);
                    array[index] = -1;
                    System.out.println("deleted " + x);
                } finally {
                    releaseLock(index);
                }
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
        }
    }

    // TODO: clean up negative values and do not insert duplicates
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

    // https://www.geeksforgeeks.org/binary-search/
    private int binarySearch(int x) {
        int low = 0;
        int high = array.length - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;

            // skip all negative values
            while (array[mid] == -1 && mid <= high) {
                mid++;
            }

            if (mid > high) {
                return -1;
            }

            if (array[mid] == x) {
                return mid;
            } else if (array[mid] < x) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }
}