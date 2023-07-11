// import org.junit.jupiter.api.Test;
// import static org.junit.jupiter.api.Assertions.assertEquals;

// import java.util.Arrays;

// public class TestMember {
//     @Test
//     public void testOnlyMember() throws InterruptedException {
//         UNSWArray array = new UNSWArray(1);

//         assertEquals(array.member(1), true);
//     }

//     @Test
//     public void testAddMember() throws InterruptedException {
//         UNSWArray array = new UNSWArray();

//         Thread thread1 = new Thread(() -> {
//             array.insert(1);
//         });

//         thread1.run();
//         thread1.start();

//         assertEquals(array.member(1), true);
//     }

//     @Test
//     public void testMemberDuringInsert() throws InterruptedException {
//         UNSWArray array = new UNSWArray();

//         Thread thread1 = new Thread(() -> {
//             array.insert(1);
//         });

//         Thread thread2 = new Thread(() -> {
//             assertEquals(array.member(1), true);
//         });

//         thread1.run();
//         thread2.run();
//         thread1.start();
//         thread2.start();
//     }

//     @Test
//     public void testMemberAfterInsert() throws InterruptedException {
//         UNSWArray array = new UNSWArray();

//         Thread thread1 = new Thread(() -> {
//             array.insert(1);
//             array.insert(3);
//             array.insert(6);
//         });

//         Thread thread2 = new Thread(() -> {
//             assertEquals(array.member(1), true);
//         });

//         thread1.run();
//         thread1.start();
//         thread1.interrupt();
//         thread2.run();
//         thread2.start();
//     }
// }
