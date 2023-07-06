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
        System.out.println("inserting " + x);
        lock.writeLock().lock();
        try {
            // if array is empty, initialise new array
            if (array.length == 0) {
                array = new int[] {x};
                System.out.println("inserted " + x);
            } else {
                int index = Arrays.binarySearch(array, x);
                if (index < 0) {
                    index = -index - 1; // Arrays.binarySearch() returns negative value of where index should be
                }
                shiftArray(index, x);
                System.out.println("inserted " + x);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    // TODO: CLEAN UP NEGATIVE VALUES
    private void shiftArray(int index, int value) {
        // make copy of array +1 length
        array = Arrays.copyOf(array, array.length + 1);
        // shift everything past value down
        for (int i = array.length - 1; i > index; i--) {
            array[i] = array[i - 1];
        }
        array[index] = value;
    }
}