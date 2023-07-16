import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class TestComparison {

    @Test
    public void testSimple() throws InterruptedException{
        UNSWArraySimple a1 = new UNSWArraySimple();

        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                a1.insert(i);
            }
            System.out.println(Arrays.toString(a1.getArray()));
            for (int i = 0; i < 10000; i++) {
                a1.member(i);
            }
            for (int i = 0; i < 10000; i++) {
                a1.delete(i);
            }
            System.out.println(Arrays.toString(a1.getArray()));

        });

        thread1.start();

        try {
            thread1.join();
        } catch (InterruptedException e) {

        }

    }

    @Test
    public void testInsertSimple() throws InterruptedException{
        UNSWArraySimple a1 = new UNSWArraySimple();

        long startTime = System.currentTimeMillis();

        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 1000;  i++) {
                a1.insert(i);
            }
            for (int i = 0; i < 1000;  i++) {
                a1.delete(i);
            }
        });

        thread1.start();

        try {
            thread1.join();
        } catch (InterruptedException e) {

        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("Elapsed time: " + elapsedTime + " milliseconds");
    }

    @Test
    public void testInsertSimple2Threads() throws InterruptedException{
        UNSWArraySimple a1 = new UNSWArraySimple();

        long startTime = System.currentTimeMillis();

        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 1000;  i += 2) {
                a1.insert(i);
                System.out.println("thread 1");
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 1; i < 1000;  i += 2) {
                a1.insert(i);
                System.out.println("thread 2");
            }
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {

        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("Elapsed time: " + elapsedTime + " milliseconds");
    }

    @Test
    public void testInsert() throws InterruptedException{
        UNSWArray a1 = new UNSWArray(1000);

        long startTime = System.currentTimeMillis();

        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 1000; i += 2) {
                a1.insert(i);
                System.out.println("thread 1");
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 1; i < 1000; i += 2) {
                a1.insert(i);
            }
        });

        Thread thread3 = new Thread(() -> {
            try {
                // allow all inserts first
                Thread.sleep(1);
            } catch (InterruptedException e) {

            }
            for (int i = 1; i < 1000; i += 2) {
                a1.delete(i);
                System.out.println("thread 2");
            }
        });

        Thread thread4 = new Thread(() -> {
            try {
                // allow all inserts first
                Thread.sleep(1);
            } catch (InterruptedException e) {

            }
            for (int i = 0; i < 1000; i += 2) {
                a1.delete(i);
            }
        });

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        try {
            thread1.join();
            thread2.join();
            thread3.join();
            thread4.join();

        } catch (InterruptedException e) {

        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("Elapsed time 2: " + elapsedTime + " milliseconds");
    }

}
