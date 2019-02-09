package ru.chernyshev.executor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Исполнитель задач по времени
 */
class TimeTaskExecutor implements IExecutor, Runnable {

    /**
     * Время ожидания потока при отсутсвтии задач в очереди
     */
    private static final int WAIT_TIMEOUT_MILLIS = 10_000;

    private final Logger logger = LogManager.getLogger(TimeTaskExecutor.class);

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
    @Override
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
    @Override
    public void turnOff() {
        isRunning.set(false);
    }

    /**
     * Добавить задачу в очередь на исполнение
     *
     * @param task задача
     */
    @Override
    public void add(ITask task) {
        logger.trace("Add new task to queue");
        queue.add(task);
        synchronized (this) {
            notify();
        }
    }

    /**
     * извлекает задачи из очереди в потоке, и исполняет их если это необходимо
     */
    @Override
    public void run() {
        while (isRunning.get()) {
            ITask task = pollTask();
            logger.trace("Task for time {} execute start", task.getRunningTime());
            progressCount.incrementAndGet();

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
                        logger.trace("Task for time {} execute complete", task.getRunningTime());
                        return obj;
                    });
        }
    }

    /**
     * @return количество выполняемых задач в текущий момент времени
     */
    @Override
    public int getProgressCount() {
        return progressCount.get();
    }

    /**
     * @return количество выполенных задач
     */
    @Override
    public int getCompleteSuccessCount() {
        return completeSuccessCount.get();
    }

    /**
     * @return количество завершенных с ошибкой
     */
    @Override
    public int getCompleteErrorCount() {
        return completeErrorCount.get();
    }

    /**
     * Извлечь и удалить первый элемент в очереди
     */
    private synchronized ITask pollTask() {
        while (true) {
            ITask task = queue.peek();

            if (task == null) {
                waitTask(WAIT_TIMEOUT_MILLIS);
                continue;
            }

            if (task.isNeedExecute()) {
                queue.remove(task);
                return task;
            } else {
                long millisToExecute = LocalDateTime.now().until(task.getRunningTime(), ChronoUnit.MILLIS);
                if (millisToExecute > 0) {
                    waitTask(millisToExecute);
                }
            }
        }
    }

    /**
     * Переводит поток в состояние ожидания
     *
     * @param waitTimeoutMillis время ожидания
     */
    private synchronized void waitTask(long waitTimeoutMillis) {
        logger.trace("Wait {} milliseconds", waitTimeoutMillis);
        try {
            wait(waitTimeoutMillis);
        } catch (InterruptedException e) {
            logger.warn(e);
        }
    }
}
