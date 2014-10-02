package com.devsmart.android;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ThreadUtils {

    private static final ThreadGroup IOThreadGroup = new ThreadGroup("IO Threads");

    private static ThreadFactory IOThreadPoolFactory = new ThreadFactory() {

        private int mThreadNum = 0;

        @Override
        public Thread newThread(Runnable runnable) {
            Thread retval = new Thread(IOThreadGroup, runnable, String.format("IO Thread %d", mThreadNum++));
            return retval;
        }
    };

    public static ExecutorService IOThreadPool = Executors.newCachedThreadPool(IOThreadPoolFactory);
}
