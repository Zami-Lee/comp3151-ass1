import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestMember2 {
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

}
