import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class TestCleanup2 {
    @Test
    public void testCleanupAll() throws InterruptedException {
        UNSWArray a1 = new UNSWArray(10);

        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                System.out.println("Inserting: " + i);
                a1.insert(i);
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                // allow all inserts first
                Thread.sleep(10);
            } catch (InterruptedException e) {

            }
            for (int i = 0; i < 10; i += 2) {
                System.out.println("Deleting: " + i);
                a1.delete(i);
            }
        });

        Thread thread3 = new Thread(() -> {
            try {
                // allow all inserts and deletes first
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
            for (int i = 1; i < 10; i += 2) {
                assertTrue(a1.member(i));
            }
            a1.cleanup();
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

        int[] expected = {-1, -1, -1, -1, -1, 1, 3, 5, 7, 9};
        assertEquals(Arrays.toString(expected), Arrays.toString(a1.getArray()));
    }

}
