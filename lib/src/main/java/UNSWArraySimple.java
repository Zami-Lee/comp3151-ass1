import java.util.concurrent.locks.*;
import java.util.Arrays;

// Note: this is a simplified test version which locks the entire array (very inefficient)

public class UNSWArraySimple {
    private int[] array;
    private ReadWriteLock lock;

    public UNSWArraySimple() {
        this.array = new int[0];
        lock = new ReentrantReadWriteLock();
    }

    public UNSWArraySimple(int... args) {
        array = args;
        lock = new ReentrantReadWriteLock();
    }

    public int[] getArray() {
        return array;
    }

    public void insert(int x) {
        try {
            lock.writeLock().lock();
            // if array is empty, initialise new array
            if (array.length == 0) {
                array = new int[] {x};
            } else {
                int index = Arrays.binarySearch(array, x);
                if (index < 0) {
                    index = -index - 1; // Arrays.binarySearch() returns negative value of where index should be
                }
                shiftArray(index, x);
                forceCleanup(index);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void delete(int x) {
        try {
            lock.writeLock().lock();
            // if array is empty, return - nothing to delete
            if (array.length == 0) {
                return;
            } else {
                int index = findIndex(x);
                if (index < 0) {
                    return;
                }
                array[index] = -1;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean member(int x) {
        try {
            lock.readLock().lock();
            // if array is empty, return - nothing to delete
            if (array.length == 0) {
                return false;
            } else {
                int index = findIndex(x);
                if (index < 0) {
                    return false;
                }
                return true;
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public void print_sorted() {
        // Try / finally block for safety
        String sortedArray = " ";

        try {
            lock.readLock().lock();

            for (int i = 0; i < array.length; i++) {
                if (array[i] != -1) {
                    sortedArray += array[i] + ", ";
                }
            }

        } finally {
            lock.readLock().unlock();
        }

        sortedArray = sortedArray.replaceAll(", $", "");
        System.out.print(sortedArray);
    }

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

    private void shiftArray(int index, int value) {
        // make copy of array +1 length
        array = Arrays.copyOf(array, array.length + 1);
        // shift everything past value down
        for (int i = array.length - 1; i > index; i--) {
            array[i] = array[i - 1];
        }
        array[index] = value;
    }

    private void forceCleanup(int upTo) {
        if (upTo <= 0) {
            return; // don't do anything
        } else {
            // read down from index upTo to index 0 and put all non -1 values immediately before index upTo
            int editIndex = upTo;

            for (int readIndex = upTo; readIndex > 0; readIndex--) {
                if (array[readIndex] != -1) {
                    array[editIndex] = array[readIndex];
                    editIndex--;
                }
            }
            if (editIndex <= 0) return;

            // populate beginning of array with all the -1 values
            else {
                for (int i = 0; i <= editIndex; i++) {
                    array[i] = -1;
                }
            }
        }
    }

}