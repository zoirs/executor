package ru.chernyshev.executor;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Обертка для задачи и времени в которое она должна быть выполненна
 */
class ScheduledTask implements ITask<ScheduledTask> {

    /**
     * Генератор порядковых номеров задач
     * */
    private static final AtomicInteger ORDER_NUMBER_GENERATOR = new AtomicInteger(0);

    /**
     * Время в которое необходимо выполнить callable
     */
    private final LocalDateTime runningTime;

    /**
     * Поступившая задача
     */
    private final Callable callable;

    /**
     * Порядковый номер поступления задачи
     */
    private final int orderNumber;

    /**
     * Создать объект
     *
     * @param runningTime время выполнения
     * @param callable    задача для выполнения
     * @throws NullPointerException если runningTime или callable null
     */
    ScheduledTask(LocalDateTime runningTime, Callable callable) {
        Objects.requireNonNull(runningTime);
        Objects.requireNonNull(callable);
        this.runningTime = runningTime;
        this.callable = callable;
        this.orderNumber = ORDER_NUMBER_GENERATOR.incrementAndGet();
    }

    /**
     * Сравнивает текущий объект {@link ScheduledTask} с другим объектом {@link ScheduledTask}
     * Первоначально сравнивает по дате выполнения runningTime,
     * если даты равны, то сравнивает порядковый номер поступления задачи orderNumber
     *
     * @param o другой {@link ScheduledTask} для сравнения с текущим, не null
     * @return отрицательное если меньше, положительное если больше
     */
    @Override
    public int compareTo(ScheduledTask o) {
        return Comparator
                .comparing(ScheduledTask::getRunningTime, LocalDateTime::compareTo)
                .thenComparing(ScheduledTask::getOrderNumber)
                .compare(this, o);
    }


    /**
     * Выполнить задачу
     *
     * @return результат выполнения задачи
     */
    @Override
    public Object execute() throws Exception {
        return callable.call();
    }

    /**
     * @return true если пришло время выполнить задачу
     */
    @Override
    public boolean isNeedExecute() {
        return LocalDateTime.now().compareTo(getRunningTime()) >= 0;
    }

    /**
     * @return время в которое задача должна быть выполнена
     */
    private LocalDateTime getRunningTime() {
        return runningTime;
    }

    /**
     * @return порядковый номер прихода задачи
     */
    private int getOrderNumber() {
        return orderNumber;
    }
}
