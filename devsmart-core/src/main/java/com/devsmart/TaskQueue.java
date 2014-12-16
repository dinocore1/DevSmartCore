package com.devsmart;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * TaskQueue are useful when you need to run a number of tasks (Runnable) sequentially. TaskQueue
 * tasks run on the given ExecutorService, but are guaranteed to run in series, even if the ExecutorService
 * is allowed to run with multiple threads. This is commonly used in conjunction with {@link ThreadUtils#CPUThreads}.
 */
public class TaskQueue {

    Logger logger = LoggerFactory.getLogger(TaskQueue.class);

    class Task implements Runnable {

        private final Runnable run;
        public Task(Runnable runnable) {
            this.run = runnable;
        }

        @Override
        public void run() {

            try {
                run.run();
            } catch (Throwable e) {
                logger.error("", e);
            }

            synchronized (TaskQueue.this){
                if(!mRunQueue.isEmpty()){
                    Task nextTask = mRunQueue.removeFirst();
                    mCurrentTask = mService.submit(nextTask);
                } else {
                    mCurrentTask = null;
                }
            }
        }
    }

    private final ExecutorService mService;
    private LinkedList<Task> mRunQueue = new LinkedList<Task>();
    private Future<?> mCurrentTask;

    public TaskQueue(ExecutorService service) {
        mService = service;
    }

    public synchronized void execute(Runnable runnable) {
        Task task = new Task(runnable);
        if(mCurrentTask == null) {
            mCurrentTask = mService.submit(task);
        } else {
            mRunQueue.addLast(task);
        }
    }

}