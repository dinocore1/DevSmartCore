package com.devsmart.swing;

import com.devsmart.TaskQueue;
import com.devsmart.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.concurrent.ExecutorService;

public abstract class BackgroundTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackgroundTask.class);

    public static void runBackgroundTask(BackgroundTask task, TaskQueue queue) {
        queue.execute(task);
    }

    public static void runBackgroundTask(BackgroundTask task, ExecutorService service){
        service.submit(task);
    }

    public static void runBackgroundTask(BackgroundTask task){
        ThreadUtils.IOThreads.submit(task);
    }

    public void onBefore() {}

    public abstract void onBackground();

    public void onAfter() {}

    private boolean mIsWaiting = true;
    private boolean mCanceled = false;

    private synchronized void stageFinished() {
        mIsWaiting = false;
        notifyAll();
    }


    private void doOnBefore() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    onBefore();
                } finally {
                    stageFinished();
                }
            }
        });
    }

    @Override
    public synchronized final void run() {
        try {
            if(!mCanceled) {
                mIsWaiting = true;
                doOnBefore();
                while(mIsWaiting){
                    wait();
                }
            }
            if(!mCanceled) {
                onBackground();
            }
        } catch(Throwable e){
            LOGGER.error("Unhandled exception while executing BackgroundTask", e);
            throw new RuntimeException(e);
        } finally {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (!isCanceled()) {
                        onAfter();
                    }
                }
            });
        }
    }

    public synchronized void cancel() {
        mCanceled = true;
    }

    public synchronized boolean isCanceled() {
        return mCanceled;
    }

}