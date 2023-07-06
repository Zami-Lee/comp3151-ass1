import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

public class TestDelete {
    @Test
    public void testDeleteEmpty() throws InterruptedException {
        UNSWArray array = new UNSWArray(1);

        Thread deleteThread1 = new Thread(() -> {
            array.delete(1);
        });

        deleteThread1.start();
        deleteThread1.join();

        int[] expected = {-1};
        assertEquals(Arrays.toString(expected), Arrays.toString(array.getArray()));
    }

    @Test
    public void testDeleteNeighbour() throws InterruptedException {
        UNSWArray array = new UNSWArray(1, 2);

        Thread deleteThread1 = new Thread(() -> {
            array.delete(2);
        });

        Thread deleteThread2 = new Thread(() -> {
            array.delete(1);
        });

        deleteThread1.start();
        deleteThread2.start();

        deleteThread1.join();
        deleteThread2.join();

        int[] expected = {-1, -1};
        assertEquals(Arrays.toString(expected), Arrays.toString(array.getArray()));
    }

    @Test
    public void testDeleteRandom() throws InterruptedException {
        UNSWArray array = new UNSWArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        Thread deleteThread1 = new Thread(() -> {
            array.delete(1);
        });

        Thread deleteThread2 = new Thread(() -> {
            array.delete(5);
        });

        Thread deleteThread3 = new Thread(() -> {
            array.delete(10);
        });

        deleteThread1.start();
        deleteThread2.start();
        deleteThread3.start();

        deleteThread1.join();
        deleteThread2.join();
        deleteThread3.join();

        int[] expected = {-1, 2, 3, 4, -1, 6, 7, 8, 9, -1};
        assertEquals(Arrays.toString(expected), Arrays.toString(array.getArray()));
    }

    @Test
    public void testDeleteSameElement() throws InterruptedException {
        UNSWArray array = new UNSWArray(1, 2, 3);

        Thread deleteThread1 = new Thread(() -> {
            array.delete(2);
        });

        Thread deleteThread2 = new Thread(() -> {
            array.delete(2);
        });

        deleteThread1.start();
        deleteThread2.start();

        deleteThread1.join();
        deleteThread2.join();

        int[] expected = {1, -1, 3};
        assertEquals(Arrays.toString(expected), Arrays.toString(array.getArray()));
    }

    @Test
    public void testDeleteAndInsertSamePosition() throws InterruptedException {
        UNSWArray array = new UNSWArray(1, 2, 4, 5);

        Thread deleteThread1 = new Thread(() -> {
            array.delete(2);
        });

        Thread insertThread1 = new Thread(() -> {
            array.insert(3);
        });

        deleteThread1.start();
        insertThread1.start();

        deleteThread1.join();
        insertThread1.join();

        int[] expected = {1, -1, 3, 4, 5};
        assertEquals(Arrays.toString(expected), Arrays.toString(array.getArray()));
    }

    @Test
    public void testDeleteAndInsertSamePosition2() throws InterruptedException {
        UNSWArray array = new UNSWArray(2, 4, 5);

        Thread deleteThread1 = new Thread(() -> {
            array.delete(2);
        });

        Thread insertThread1 = new Thread(() -> {
            array.insert(1);
        });

        deleteThread1.start();
        insertThread1.start();

        deleteThread1.join();
        insertThread1.join();

        int[] expected = {-1, 1, 4, 5};
        assertEquals(Arrays.toString(expected), Arrays.toString(array.getArray()));
    }

    @Test
    public void testDeleteAndInsertDiffPosition() throws InterruptedException {
        UNSWArray array = new UNSWArray(1, 2, 4, 5);

        Thread deleteThread1 = new Thread(() -> {
            array.delete(2);
            array.insert(8);
        });

        Thread insertThread1 = new Thread(() -> {
            array.insert(7);
            array.delete(4);
        });

        deleteThread1.start();
        insertThread1.start();

        deleteThread1.join();
        insertThread1.join();

        int[] expected = {1, -1, -1, 5, 7, 8};
        assertEquals(Arrays.toString(expected), Arrays.toString(array.getArray()));
    }

    @Test
    public void testDeleteMany() throws InterruptedException {
        UNSWArray array = new UNSWArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        Thread deleteThread1 = new Thread(() -> {
            array.delete(1);
            array.delete(3);
            array.delete(5);
            array.delete(7);
            array.delete(9);
        });

        Thread deleteThread2 = new Thread(() -> {
            array.delete(2);
            array.delete(4);
            array.delete(6);
            array.delete(8);
            array.delete(10);
        });

        deleteThread1.start();
        deleteThread2.start();

        deleteThread1.join();
        deleteThread2.join();

        int[] expected = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        assertEquals(Arrays.toString(expected), Arrays.toString(array.getArray()));
    }
}
