package ru.chernyshev.executor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Исполнитель задач по времени
 */
class TimeTaskExecutor implements IExecutor, Runnable {

    /**
     * Приоритетная очередь задач
     */
    private final BlockingQueue<ITask> queue = new PriorityBlockingQueue<>();

    /**
     * Количество выполняемых задач в текущий момент времени
     */
    private final AtomicInteger progressCount = new AtomicInteger(0);

    /**
     * Количество выполенных задач
     */
    private final AtomicInteger completeSuccessCount = new AtomicInteger(0);

    /**
     * Количество задач завершенных с ошибкой
     */
    private final AtomicInteger completeErrorCount = new AtomicInteger(0);

    /**
     * Состояние исполнителя задач
     */
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    /**
     * Запусить исполнитель задач в отдельном потоке
     */
    public void turnOn() {
        boolean canStarted = isRunning.compareAndSet(false, true);
        if (!canStarted) {
            throw new RuntimeException("Already started");
        }
        new Thread(this).start();
    }

    /**
     * Остановить исполнитель задач
     */
    public void turnOff() {
        isRunning.set(false);
    }

    /**
     * Добавить задачу в очередь на исполнение
     *
     * @param task задача
     */
    public void add(ITask task) {
        queue.add(task);
    }

    /**
     * извлекает задачи из очереди в потоке, и исполняет их если это необходимо
     */
    @Override
    public void run() {

        while (isRunning.get()) {
            final ITask task = queue.peek();
            if (task != null && task.isNeedExecute()) {
                progressCount.incrementAndGet();
                queue.remove(task);

                CompletableFuture
                        .supplyAsync(() -> {
                            try {
                                return task.execute();
                            } catch (Exception e) {
                                throw new RuntimeException("Execute exception", e);
                            }
                        })
                        .handle((obj, err) -> {
                            if (err == null) {
                                completeSuccessCount.incrementAndGet();
                            } else {
                                completeErrorCount.incrementAndGet();
                            }
                            progressCount.decrementAndGet();
                            return obj;
                        });
            }
        }
    }

    /**
     * @return количество выполняемых задач в текущий момент времени
     */
    public int getProgressCount() {
        return progressCount.get();
    }

    /**
     * @return количество выполенных задач
     */
    public int getCompleteSuccessCount() {
        return completeSuccessCount.get();
    }

    /**
     * @return количество завершенных с ошибкой
     */
    public int getCompleteErrorCount() {
        return completeErrorCount.get();
    }
}
