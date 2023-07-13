import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

public class TestDelete2 {

    @Test
    public void testDeleteOneElement() throws InterruptedException {
        UNSWArray a1 = new UNSWArray(1);

        Thread thread1 = new Thread(() -> {
            a1.insert(0);
        });

        Thread thread2 = new Thread(() -> {
            try {
                // allow all inserts first
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
            a1.delete(0);
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {

        }

        int[] expected = {-1};
        assertEquals(Arrays.toString(expected), Arrays.toString(a1.getArray()));
    }

    @Test
    public void testDeleteDuplicateElementMultithread() throws InterruptedException {
        UNSWArray a1 = new UNSWArray(5);

        Thread thread1 = new Thread(() -> {
            a1.insert(0);
            a1.insert(1);
        });

        Thread thread2 = new Thread(() -> {
            try {
                // allow all inserts first
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
            a1.delete(0);
        });

        Thread thread3 = new Thread(() -> {
            try {
                // allow all inserts first
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
            a1.delete(0);
        });

        thread1.start();
        thread2.start();
        thread3.start();

        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {

        }

        int[] expected = {-1, -1, -1, -1, 1};
        assertEquals(Arrays.toString(expected), Arrays.toString(a1.getArray()));
    }

    @Test
    public void testDeleteInsertSameElement() throws InterruptedException {
        UNSWArray a1 = new UNSWArray(2);

        Thread thread1 = new Thread(() -> {
            a1.insert(0);
            a1.insert(1);
        });

        Thread thread2 = new Thread(() -> {
            a1.delete(0);
            a1.delete(1);
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        int[] expected = {-1, -1};
        int[] expected2 = {0, 1};
        int[] expected3 = {0, -1};
        int[] expected4 = {-1, 1};

        int[] actual = a1.getArray();

        assertTrue(Arrays.equals(expected, actual) ||
           Arrays.equals(expected2, actual) ||
           Arrays.equals(expected3, actual) ||
           Arrays.equals(expected4, actual));
    }

    @Test
    public void testDeleteAll() throws InterruptedException {
        UNSWArray a1 = new UNSWArray(20);

        // intial operation to create new array
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 20; i++) {
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

        Thread thread2 = new Thread(() -> {
            try {
                // allow all inserts first
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
            // now delete all odd numbers
            for (int i = 1; i < 20; i += 2) {
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

        Thread thread3 = new Thread(() -> {
            try {
                // allow all inserts first
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
            // now delete all even numbers
            for (int i = 0; i < 20; i += 2) {
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

        thread1.start();
        thread2.start();
        thread3.start();

        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {

        }

        int[] expected = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        assertEquals(Arrays.toString(expected), Arrays.toString(a1.getArray()));
    }

    @Test
    public void testInsertQueueFullThenDelete() throws InterruptedException {
        UNSWArray a1 = new UNSWArray(200);

        // fill queue
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 120; i++) {
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

        // delete to clear
        Thread thread2 = new Thread(() -> {
            try {
                // allow all inserts to queue and buffer
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }

            for (int i = 0; i < 25; i++) {
                System.out.println("Deleting: " + i);
                a1.delete(i);

                if (i % 2 == 0) {
                    try {
                        Thread.sleep(8);
                    } catch (InterruptedException e) {

                    }
                }
            }
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {

        }

        // all values up to 25 are deleted, then 25-119 all in array
        int[] expected = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119};
        assertEquals(Arrays.toString(expected), Arrays.toString(a1.getArray()));
    }

}
