package executor;

import com.sun.istack.internal.NotNull;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.Callable;

public class Task implements Comparable<Task> {

    private final LocalDateTime runningTime;
    private final Callable callable;

    private final Date dateCreated;

    public Task(LocalDateTime runningTime, Callable callable) {
        this.runningTime = runningTime;
        this.callable = callable;
        this.dateCreated = new Date();
    }

    @Override
    public int compareTo(@NotNull Task task) {
        return Comparator
                .comparing(Task::getRunningTime, LocalDateTime::compareTo)
                .thenComparing(Task::getDateCreated, Date::compareTo)
                .compare(this, task);
    }

    public boolean isNeedExecute() {
        return LocalDateTime.now().compareTo(getRunningTime()) >= 0;
    }

    Callable getCallable() {
        return callable;
    }

    private LocalDateTime getRunningTime() {
        return runningTime;
    }

    private Date getDateCreated() {
        return dateCreated;
    }
}
