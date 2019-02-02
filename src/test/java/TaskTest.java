import org.junit.Test;
import executor.Task;

import java.time.LocalDateTime;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class TaskTest {

    private static final LocalDateTime NOW = LocalDateTime.now();

    @Test
    public void isNeedExecuteTest() {
        assertTrue(new Task(NOW, ()-> null).isNeedExecute());
        assertTrue(new Task(NOW.minusSeconds(1), ()-> null).isNeedExecute());
    }

    @Test
    public void isNotNeedExecuteTest() {
        assertFalse(new Task(NOW.plusSeconds(1), ()-> null).isNeedExecute());
        assertFalse(new Task(NOW.plusMinutes(1), ()-> null).isNeedExecute());
    }

    @Test
    public void compareTaskTest() {
        Task task1 = new Task(NOW.plusSeconds(1), () -> null);
        Task task2 = new Task(NOW.plusSeconds(2), () -> null);
        assertThat(task1.compareTo(task2), is(-1));
    }

    @Test
    public void compareTaskTest2() {
        Task task1 = new Task(NOW.plusSeconds(1), () -> null);
        Task task2 = new Task(NOW.plusSeconds(2), () -> null);
        assertThat(task2.compareTo(task1), is(1));
    }

    @Test
    public void compareTaskTest3() throws InterruptedException {
        Task task1 = new Task(NOW, () -> null);
        Thread.sleep(1);
        Task task2 = new Task(NOW, () -> null);
        assertThat(task2.compareTo(task1), is(1));
    }
}