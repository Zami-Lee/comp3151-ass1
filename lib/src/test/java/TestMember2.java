import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestMember2 {
    @Test
    public void testBasicNotMember() throws InterruptedException {
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
            assertTrue(!a1.member(2));
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {

        }
    }

    @Test
    public void testNotMember() throws InterruptedException {
        UNSWArray a1 = new UNSWArray(20);

        Thread thread1 = new Thread(() -> {
            for (int i = 1; i < 20; i += 2) {
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
            System.out.println("Asserting 0 not a member ");
            assertTrue(!a1.member(0));
        });

        Thread thread3 = new Thread(() -> {
            try {
                // allow all inserts first
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
            System.out.println("Asserting 16 not a member ");
            assertTrue(!a1.member(16));
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
    }

    @Test
    public void testBasicMemberMultithread() throws InterruptedException {
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
            assertTrue(a1.member(0));
        });

        Thread thread3 = new Thread(() -> {
            try {
                // allow all inserts first
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
            assertTrue(a1.member(1));
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
    }

    @Test public void testWriteFirstThenMember() throws InterruptedException {
        UNSWArray a1 = new UNSWArray(20);

        // write thread
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 20; i += 1) {
                System.out.println("Inserting: " + i);
                a1.insert(i);
            }
        });

        // read thread starts after
        Thread thread2 = new Thread(() -> {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {

            }

            for (int i = 0; i < 20; i += 1) {
                System.out.println("Member check: " + i);
                assertTrue(a1.member(i));
            }
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {

        }

    }

}
