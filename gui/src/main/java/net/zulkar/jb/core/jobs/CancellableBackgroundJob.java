package net.zulkar.jb.core.jobs;

import net.zulkar.jb.core.UiContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public abstract class CancellableBackgroundJob<T> {

    private static final Logger log = LogManager.getLogger(CancellableBackgroundJob.class);

    protected final UiContext context;
    private final SwingWorker<T, Void> worker;
    private final boolean needLock;
    protected volatile boolean userCancelled;

    public CancellableBackgroundJob(UiContext context, boolean needLock) {

        this.context = context;
        this.needLock = needLock;
        worker = new SpringWorkerExecutor();
    }


    Future<T> execute() {
        if (needLock) {
            this.context.lockActions(this);
        }
        worker.execute();
        return worker;
    }

    public void cancelJob() {
        userCancelled = true;
        worker.cancel(true);
    }

    protected abstract T doJob() throws Exception;

    protected abstract void succeedEDT(T result) throws Exception;

    protected abstract void failedEDT(Exception e) throws Exception;

    private class SpringWorkerExecutor extends SwingWorker<T, Void> {
        @Override
        protected T doInBackground() throws Exception {
            return CancellableBackgroundJob.this.doJob();
        }

        @Override
        protected void done() {
            if (needLock) {
                context.unlockActions(CancellableBackgroundJob.this);
            }

            try {
                processResult();
            } catch (Exception e) {
                log.error("Error processing result", e);
                context.getMainFrame().setStatus("error: %s", e.getMessage());
            }

        }

        private void processResult() throws Exception {
            try {

                if (!userCancelled) {
                    succeedEDT(get());
                }
            } catch (InterruptedException | ExecutionException e) {
                failedEDT(e);
            }
        }
    }
}
