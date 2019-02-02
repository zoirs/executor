package executor;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.*;

public class Executor implements Runnable {

    private final BlockingQueue<Task> set = new PriorityBlockingQueue<>();

    private final Deque<Task> progress = new ConcurrentLinkedDeque<>();
    private final Deque<Task> completeSuccess = new ConcurrentLinkedDeque<>();
    private final Deque<Task> completeError = new ConcurrentLinkedDeque<>();

    private boolean isRunning;

    public Executor() {
        this.isRunning = true;
        new Thread(this).start();
    }

    public void add(LocalDateTime time, Callable callable) {
        set.add(new Task(time, callable));
    }

    public void addAll(Collection<? extends Task> tasks) {
        set.addAll(tasks);
    }

    @Override
    public void run() {

        while (isRunning) {
            final Task task = set.peek();
            if (task != null && task.isNeedExecute()) {
                progress.add(task);
                set.remove(task);

                CompletableFuture
                        .supplyAsync(() -> {
                            try {
                                return task.getCallable().call();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .handle((obj, err) -> {
                            progress.remove(task);
                            if (err == null) {
                                completeSuccess.add(task);
                            } else {
                                completeError.add(task);
                            }
                            return obj;
                        });
            }
        }
    }

    public void turnOff() {
        isRunning = false;
    }

    public Deque<Task> getProgress() {
        return progress;
    }

    public Deque<Task> getCompleteSuccess() {
        return completeSuccess;
    }

    public Deque<Task> getCompleteError() {
        return completeError;
    }
}
