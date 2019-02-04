package ru.chernyshev.executor;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Обертка для задачи и времени в которое она должна быть выполненна
 */
class ScheduledTask implements Comparable<ScheduledTask>, ITask {

    /**
     * Время в которое необходимо выполнить callable
     */
    private final LocalDateTime runningTime;

    /**
     * Поступившая задача
     */
    private final Callable callable;

    /**
     * Дата поступления задачи
     */
    private final Date dateCreated;

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
        this.dateCreated = new Date();
    }

    /**
     * Сравнивает текущий объект {@link ScheduledTask} с другим объектом {@link ScheduledTask}
     * Первоначально сравнивает по дате выполнения runningTime,
     * если даты равны, то сравнивает дату поступления задачи dateCreated
     *
     * @param task другой {@link ScheduledTask} для сравнения с текущим, не null
     * @return отрицательное если меньше, положительное если больше
     */
    @Override
    public int compareTo(ScheduledTask task) {
        return Comparator
                .comparing(ScheduledTask::getRunningTime, LocalDateTime::compareTo)
                .thenComparing(ScheduledTask::getDateCreated, Date::compareTo)
                .compare(this, task);
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
     * @return время в которое система получила информацию о задаче
     */
    private Date getDateCreated() {
        return dateCreated;
    }
}
