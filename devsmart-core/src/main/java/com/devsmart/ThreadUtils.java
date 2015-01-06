package com.devsmart;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

public class ThreadUtils {

    private static final ThreadGroup CPUThreadGroup = new ThreadGroup("CPU Threads");
    private static ThreadFactory CPUThreadPoolFactory = new ThreadFactory() {

        private int mThreadNum = 0;

        @Override
        public Thread newThread(Runnable runnable) {
            Thread retval = new Thread(CPUThreadGroup, runnable, String.format("CPU Thread %d", mThreadNum++));
            return retval;
        }
    };

    private static final ThreadGroup IOThreadGroup = new ThreadGroup("IO Threads");
    private static ThreadFactory IOThreadPoolFactory = new ThreadFactory() {

        private int mIOThreadNum = 0;

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(IOThreadGroup, r, String.format("IO Thread %d", mIOThreadNum++));
            return thread;
        }
    };

    public static final ScheduledExecutorService CPUThreads = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), CPUThreadPoolFactory);
    public static final ExecutorService IOThreads = Executors.newCachedThreadPool(IOThreadPoolFactory);

}