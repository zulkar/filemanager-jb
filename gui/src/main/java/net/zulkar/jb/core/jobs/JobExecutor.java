package net.zulkar.jb.core.jobs;

import java.util.concurrent.Future;

/**
 * this class is primarly to simplify unit-tests
 */
public class JobExecutor {
    public <T> Future<T> execute(CancellableBackgroundJob<T> job) {
        return job.execute();
    }
}
