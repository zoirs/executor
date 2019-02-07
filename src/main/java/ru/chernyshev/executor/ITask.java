package ru.chernyshev.executor;

/**
 * Обертка для задачи и времени в которое она должна быть выполненна
 */
interface ITask<T> extends Comparable<T> {

    /**
     * @return true если пришло время выполнить задачу
     */
    boolean isNeedExecute();

    /**
     * Выполнить задачу
     *
     * @return результат выполнения задачи
     * @throws Exception генерирует исполняемая задача
     */
    Object execute() throws Exception;
} 