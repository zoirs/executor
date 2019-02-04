package ru.chernyshev.executor;

/**
 * Исполнитель задач по времени
 */
interface IExecutor {

    /**
     * Запусить исполнитель задач в отдельном потоке
     */
    void turnOn();

    /**
     * Остановить исполнитель задач
     */
    void turnOff();

    /**
     * Добавить задачу в очередь на выполнение
     *
     * @param task задача
     */
    void add(ITask task);

    /**
     * @return количество выполняемых задач в текущий момент времени
     */
    int getProgressCount();

    /**
     * @return количество выполенных задач
     */
    int getCompleteSuccessCount();

    /**
     * @return количество завершенных с ошибкой
     */
    int getCompleteErrorCount();
}
