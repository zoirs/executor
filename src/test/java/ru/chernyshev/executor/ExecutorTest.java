package ru.chernyshev.executor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Тесты исполнителя задач
 */
public class ExecutorTest {

    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final int WAIT_TIME = 100;

    private IExecutor executor;

    @Before
    public void before() {
        executor = new TimeTaskExecutor();
        executor.turnOn();
    }

    @After
    public void after() {
        executor.turnOff();
    }

    /**
     * Задача с текущим временем исполнения выполнится сразу
     */
    @Test
    public void executeNow() throws InterruptedException {
        CountDownLatch lock = new CountDownLatch(1);

        executor.add(new ScheduledTask(NOW, createCallable(lock)));
        lock.await(WAIT_TIME, TimeUnit.MILLISECONDS);
        assertCompletedCount(1, 0);
    }

    /**
     * Задача, которая должна выполнится в будущем не выполнятся
     */
    @Test
    public void noExecuteFutureScheduledTaskTest() throws InterruptedException {
        CountDownLatch lock = new CountDownLatch(1);

        executor.add(new ScheduledTask(NOW.plusSeconds(1), createCallable(lock)));
        lock.await(WAIT_TIME, TimeUnit.MILLISECONDS);

        assertCompletedCount(0, 0);
    }

    /**
     * Задача, завершенная с ошибкой есть в финальном отчете в завершенных с ошибкой
     */
    @Test
    public void completeWithErrorTest() throws InterruptedException {
        CountDownLatch lock = new CountDownLatch(1);

        executor.add(new ScheduledTask(NOW, () -> {
            throw new RuntimeException();
        }));

        lock.await(WAIT_TIME, TimeUnit.MILLISECONDS);

        assertCompletedCount(0, 1);
    }

    /**
     * Выполнение нескольких задач проходит успешно
     */
    @Test
    public void executeSomeScheduledTasksTest() throws InterruptedException {
        CountDownLatch lock = new CountDownLatch(3);

        executor.add(new ScheduledTask(NOW, createCallable(lock)));
        executor.add(new ScheduledTask(NOW.minusMinutes(1), createCallable(lock)));
        executor.add(new ScheduledTask(NOW.minusSeconds(1), createCallable(lock)));

        lock.await(WAIT_TIME, TimeUnit.MILLISECONDS);

        assertThat(lock.getCount(), is(0L));
        assertCompletedCount(3, 0);
    }


    /**
     * Задачи, которые должны выполнится сейчас - выполняются
     * Задачи, которые должны выполниться в будущем - не выполняются
     */
    @Test
    public void executePastAndNoExecuteFutureTest() throws InterruptedException {
        CountDownLatch lock = new CountDownLatch(3);

        executor.add(new ScheduledTask(NOW, createCallable(lock)));
        executor.add(new ScheduledTask(NOW.plusSeconds(1), createCallable(lock)));
        executor.add(new ScheduledTask(NOW.minusSeconds(1), createCallable(lock)));

        lock.await(WAIT_TIME, TimeUnit.MILLISECONDS);

        assertThat(lock.getCount(), is(1L));
        assertCompletedCount(2, 0);
    }

    private void assertCompletedCount(int successCount, int errorCount) {
        waitCompleteTasks();
        assertThat(executor.getCompleteSuccessCount(), is(successCount));
        assertThat(executor.getCompleteErrorCount(), is(errorCount));
    }

    private void waitCompleteTasks() {
        while (executor.getProgressCount() > 0) {
            Thread.yield();
        }
    }

    private Callable createCallable(CountDownLatch lock) {
        return () -> {
            lock.countDown();
            return null;
        };
    }
}
