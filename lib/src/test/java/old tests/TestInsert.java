// import org.junit.jupiter.api.Test;
// import static org.junit.jupiter.api.Assertions.assertEquals;

// import java.util.Arrays;

// public class TestInsert {
//     @Test
//     public void testEmptyInsert() throws InterruptedException {
//         UNSWArray array = new UNSWArray();

//         Thread insertThread1 = new Thread(() -> {
//             array.insert(1);
//         });

//         Thread insertThread2 = new Thread(() -> {
//             array.insert(2);
//         });

//         insertThread1.start();
//         insertThread2.start();

//         insertThread1.join();
//         insertThread2.join();

//         int[] expected = {1, 2};
//         assertEquals(Arrays.toString(expected), Arrays.toString(array.getArray()));
//     }

//     @Test
//     public void testEmptyInsert2() throws InterruptedException {
//         UNSWArray array = new UNSWArray();

//         Thread insertThread1 = new Thread(() -> {
//             array.insert(1);
//             array.insert(3);
//         });

//         Thread insertThread2 = new Thread(() -> {
//             array.insert(2);
//             array.insert(4);
//         });

//         insertThread1.start();
//         insertThread2.start();

//         insertThread1.join();
//         insertThread2.join();

//         int[] expected = {1, 2, 3, 4};
//         assertEquals(Arrays.toString(expected), Arrays.toString(array.getArray()));
//     }

//     @Test
//     public void testInsertNeighbour() throws InterruptedException {
//         // testing insertion at same index
//         UNSWArray array = new UNSWArray(1, 2, 5);

//         Thread insertThread1 = new Thread(() -> {
//             array.insert(3);
//         });

//         Thread insertThread2 = new Thread(() -> {
//             array.insert(4);
//         });

//         insertThread1.start();
//         insertThread2.start();

//         insertThread1.join();
//         insertThread2.join();

//         int[] expected = {1, 2, 3, 4, 5};
//         assertEquals(Arrays.toString(expected), Arrays.toString(array.getArray()));
//     }

//     @Test
//     public void testInsertBeginning() throws InterruptedException {
//         // testing insertion at same index
//         UNSWArray array = new UNSWArray(3, 5);

//         Thread insertThread1 = new Thread(() -> {
//             array.insert(1);
//         });

//         Thread insertThread2 = new Thread(() -> {
//             array.insert(2);
//         });

//         insertThread1.start();
//         insertThread2.start();

//         insertThread1.join();
//         insertThread2.join();

//         int[] expected = {1, 2, 3, 5};
//         assertEquals(Arrays.toString(expected), Arrays.toString(array.getArray()));
//     }

//     @Test
//     public void testInsertRandom() throws InterruptedException {
//         UNSWArray array = new UNSWArray(1, 3, 5);

//         Thread insertThread1 = new Thread(() -> {
//             array.insert(2);
//             array.insert(12);
//         });

//         Thread insertThread2 = new Thread(() -> {
//             array.insert(7);
//         });

//         insertThread1.start();
//         insertThread2.start();

//         insertThread1.join();
//         insertThread2.join();

//         int[] expected = {1, 2, 3, 5, 7, 12};
//         assertEquals(Arrays.toString(expected), Arrays.toString(array.getArray()));
//     }

//     @Test
//     public void testInsertMany() throws InterruptedException {
//         UNSWArray array = new UNSWArray();

//         Thread insertThread1 = new Thread(() -> {
//             array.insert(1);
//             array.insert(3);
//             array.insert(5);
//             array.insert(6);
//             array.insert(8);
//             array.insert(10);
//         });

//         Thread insertThread2 = new Thread(() -> {
//             array.insert(2);
//             array.insert(4);
//             array.insert(7);
//             array.insert(9);
//         });

//         insertThread1.start();
//         insertThread2.start();

//         insertThread1.join();
//         insertThread2.join();

//         int[] expected = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
//         assertEquals(Arrays.toString(expected), Arrays.toString(array.getArray()));
//     }

// }
