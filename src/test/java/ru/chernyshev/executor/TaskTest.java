package ru.chernyshev.executor;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 *  Тесты приоритетов задач
 */
public class TaskTest {

    private static final LocalDateTime NOW = LocalDateTime.now();

    /**
     * Задача с текущем временем выполнения может быть выполнена
     * Задача с прошедшим временем выполнения может быть выполнена
     */
    @Test
    public void isNeedExecuteTest() {
        assertTrue(new ScheduledTask(NOW, ()-> null).isNeedExecute());
        assertTrue(new ScheduledTask(NOW.minusSeconds(1), ()-> null).isNeedExecute());
    }

    /**
     * Задача с временем выполнения в будущем не может быть выполнена
     */
    @Test
    public void isNotNeedExecuteTest() {
        assertFalse(new ScheduledTask(NOW.plusSeconds(1), ()-> null).isNeedExecute());
        assertFalse(new ScheduledTask(NOW.plusMinutes(1), ()-> null).isNeedExecute());
    }

    /**
     * Задача более раннем временем выполнения должна быть в очереди выше
     */
    @Test
    public void compareTaskTest() {
        ScheduledTask task1 = new ScheduledTask(NOW.plusSeconds(1), () -> null);
        ScheduledTask task2 = new ScheduledTask(NOW.plusSeconds(2), () -> null);
        assertThat(task1.compareTo(task2), is(-1));
    }

    /**
     * Задача более поздним временем выполнения должна быть в очереди ниже
     */
    @Test
    public void compareTaskTest2() {
        ScheduledTask task1 = new ScheduledTask(NOW.plusSeconds(1), () -> null);
        ScheduledTask task2 = new ScheduledTask(NOW.plusSeconds(2), () -> null);
        assertThat(task2.compareTo(task1), is(1));
    }

    /**
     * Задачи с одинаковым временем выполнения должны выполнится по времени добавления
     */
    @Test
    public void compareTaskTest3() {
        ScheduledTask task1 = new ScheduledTask(NOW, () -> null);
        ScheduledTask task2 = new ScheduledTask(NOW, () -> null);
        assertThat(task2.compareTo(task1), is(1));
    }
}