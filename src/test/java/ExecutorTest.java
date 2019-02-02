import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import executor.Executor;
import executor.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ExecutorTest {

    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final int WAIT_TIME = 100;

    private Executor executor;

    @Before
    public void before() {
        executor = new Executor();
    }

    @After
    public void after(){
        executor.turnOff();
    }

    @Test
    public void executeNow() throws InterruptedException {
        CountDownLatch lock = new CountDownLatch(1);

        executor.add(NOW, createCallable(lock));
        lock.await(WAIT_TIME, TimeUnit.MILLISECONDS);
        assertCompletedCount(1, 0);
    }

    @Test
    public void noExecuteFutureTaskTest() throws InterruptedException {
        CountDownLatch lock = new CountDownLatch(1);

        executor.add(NOW.plusSeconds(1), createCallable(lock));
        lock.await(WAIT_TIME, TimeUnit.MILLISECONDS);

        assertCompletedCount(0, 0);
    }

    @Test
    public void completeWithErrorTest() throws InterruptedException {
        CountDownLatch lock = new CountDownLatch(1);

        executor.add(NOW, () -> {
            throw new RuntimeException();
        });

        lock.await(WAIT_TIME, TimeUnit.MILLISECONDS);

        assertCompletedCount(0, 1);
    }

    @Test
    public void executeSomeTasksTest() throws InterruptedException {
        CountDownLatch lock = new CountDownLatch(3);

        executor.add(NOW, createCallable(lock));
        executor.add(NOW.minusMinutes(1), createCallable(lock));
        executor.add(NOW.minusSeconds(1), createCallable(lock));

        lock.await(WAIT_TIME, TimeUnit.MILLISECONDS);

        assertThat(lock.getCount(), is(0L));
        assertCompletedCount(3, 0);
    }

    @Test
    public void executePastAndNoExecuteFutureTest() throws InterruptedException {
        CountDownLatch lock = new CountDownLatch(3);

        executor.add(NOW, createCallable(lock));
        executor.add(NOW.plusSeconds(1), createCallable(lock));
        executor.add(NOW.minusSeconds(1), createCallable(lock));

        lock.await(WAIT_TIME, TimeUnit.MILLISECONDS);

        assertThat(lock.getCount(), is(1L));
        assertCompletedCount(2, 0);
    }

    @Test
    public void sortTest1() throws InterruptedException {
        Exchanger<String> exchanger = new Exchanger<>();

        List<Task> tasks = new ArrayList<>();

        tasks.add(new Task(NOW.minusSeconds(3), () -> exchanger.exchange("first")));
        tasks.add(new Task(NOW.minusSeconds(2), () -> exchanger.exchange("second")));

        executor.addAll(tasks);

        String first = exchanger.exchange(null);

        assertThat(first, is("first"));
    }

    @Test
    public void sortTest2() throws InterruptedException {
        Exchanger<String> exchanger = new Exchanger<>();

        List<Task> tasks = new ArrayList<>();

        tasks.add(new Task(NOW.minusSeconds(2), () -> exchanger.exchange("second")));
        tasks.add(new Task(NOW.minusSeconds(3), () -> exchanger.exchange("first")));

        executor.addAll(tasks);

        String first = exchanger.exchange(null);

        assertThat(first, is("first"));
    }

    @Test
    public void sortOneTimeTest() throws InterruptedException {
        Exchanger<String> exchanger = new Exchanger<>();

        List<Task> tasks = new ArrayList<>();

        tasks.add(new Task(NOW.minusSeconds(3), () -> exchanger.exchange("first")));
        tasks.add(new Task(NOW.minusSeconds(3), () -> exchanger.exchange("second")));

        executor.addAll(tasks);

        String first = exchanger.exchange(null);

        assertThat(first, is("first"));
    }

    private void assertCompletedCount(int successCount, int errorCount) {
        assertThat(executor.getProgress().size(), is(0));
        assertThat(executor.getCompleteSuccess().size(), is(successCount));
        assertThat(executor.getCompleteError().size(), is(errorCount));
    }

    private Callable createCallable(CountDownLatch lock) {
        return () -> {
            lock.countDown();
            return null;
        };
    }
}
