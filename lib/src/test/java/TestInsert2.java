import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

public class TestInsert2 {

    @Test
    public void testInsertDifferentIndex() throws InterruptedException{
        UNSWArray a1 = new UNSWArray(5);

        Thread thread1 = new Thread(() -> {
            a1.insert(0);
        });

        Thread thread2 = new Thread(() -> {
            a1.insert(4);
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {

        }

        int[] expected = {-1, -1, -1, 0, 4};
        assertEquals(Arrays.toString(expected), Arrays.toString(a1.getArray()));
        a1.print_sorted();
    }

    @Test
    public void testInsertDuplicateElement() throws InterruptedException{
        UNSWArray a1 = new UNSWArray(3);

        Thread thread1 = new Thread(() -> {
            a1.insert(1);
        });

        Thread thread2 = new Thread(() -> {
            a1.insert(1);
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {

        }

        int[] expected = {-1, -1, 1};
        assertEquals(Arrays.toString(expected), Arrays.toString(a1.getArray()));
        a1.print_sorted();
    }

    @Test
    public void testInsertAtSameIndex() throws InterruptedException {
        UNSWArray a1 = new UNSWArray(5);

        Thread thread1 = new Thread(() -> {
            a1.insert(0);
            a1.insert(1);
        });

        Thread thread2 = new Thread(() -> {
            a1.insert(2);
            a1.insert(4);
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {

        }

        int[] expected = {-1, 0, 1, 2, 4};
        assertEquals(Arrays.toString(expected), Arrays.toString(a1.getArray()));
        a1.print_sorted();
    }

    @Test
    public void testInsertOddEven() throws InterruptedException {
        UNSWArray a1 = new UNSWArray(20);

        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 20; i += 2) {
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
            for (int i = 1; i < 20; i += 2) {
                System.out.println("Inserting: " + i);
                a1.insert(i);
                if (i % 3 == 1) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {

                    }
                }
                a1.print_sorted();
            }
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {

        }

        int[] expected = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19};
        assertEquals(Arrays.toString(expected), Arrays.toString(a1.getArray()));
        a1.print_sorted();
    }

    @Test
    public void TestThreeThreads() throws InterruptedException {
        UNSWArray a1 = new UNSWArray(40);

        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 40; i += 3) {
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
            for (int i = 1; i < 40; i += 3) {
                System.out.println("Inserting: " + i);
                a1.insert(i);
                if (i % 3 == 1) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {

                    }
                }
            }
        });

        Thread thread3 = new Thread(() -> {
            for (int i = 2; i < 40; i += 3) {
                System.out.println("Inserting: " + i);
                a1.insert(i);
                if (i % 4 == 1) {
                    try {
                        Thread.sleep(12);
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

        int[] expected = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39};
        assertEquals(Arrays.toString(expected), Arrays.toString(a1.getArray()));
        a1.print_sorted();
    }

}
