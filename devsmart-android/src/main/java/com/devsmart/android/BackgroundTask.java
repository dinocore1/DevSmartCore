package com.devsmart.android;

import android.os.Handler;
import android.os.Looper;
import com.devsmart.TaskQueue;
import com.devsmart.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * A runnable task that can be executed in a thread pool with optional override methods
 * that will always run on the main thread before and after {@link #onBackground()}
 *
 * <p>This class is useful when writing asynchronous tasks that also need to interact with
 * the UI. For example, when performing long running I/O operations, it is often good
 * practice to show some sort of loading message to the user. {@code BackgroundTask}'s
 * {@link #onBefore()} and {@link #onAfter()} methods are always executed on the main thread
 * which makes it easy to write code to interface with UI while waiting for the background
 * task to complete.
 * </p>
 *
 * <pre>
 * {@code
 * BackgroundTask.runBackgroundTask(new BackgroundTask(){
 *     void onBefore(){
 *         // this code is executed on the Main thread
 *         // write code to show loading message here
 *     }
 *
 *     void onBackground() {
 *         // this code is executed in the thread pool
 *         //perform long running I/O here
 *     }
 *
 *    void onAfter() {
 *         // this code is executed on the Main thread
 *         // write code to hide loading message here
 *      }
 * });
 * }
 * </pre>
 */
public abstract class BackgroundTask implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(BackgroundTask.class);

	public static void runBackgroundTask(BackgroundTask task, TaskQueue queue) {
		task.mMainThreadHandler = new Handler(Looper.getMainLooper());
		queue.execute(task);
	}

	/**
	 * Submits a Runnable task for execution on {@code service} and returns
	 * a Future representing that task. The {@code task}'s {@link BackgroundTask#onBefore()}
	 * method will always be executed on the main thread before
	 * @param task
	 * @param service
	 * @return
	 * @see ExecutorService#submit(Runnable)
	 */
    public static Future<?> runBackgroundTask(BackgroundTask task, ExecutorService service){
		task.mMainThreadHandler = new Handler(Looper.getMainLooper());
		return service.submit(task);
	}

	/**
	 *
	 * @param task
	 * @return
	 * @see BackgroundTask#runBackgroundTask(BackgroundTask, ExecutorService)
	 */
	public static Future<?> runBackgroundTask(BackgroundTask task){
		return runBackgroundTask(task, ThreadUtils.IOThreads);
	}

	private Handler mMainThreadHandler;

	public void onBefore() {}

	public abstract void onBackground();

	public void onAfter() {}

	private boolean mIsWaiting = true;
    private boolean mCanceled = false;

	private synchronized void waitForStage() throws InterruptedException {
		while(mIsWaiting){
			wait();
		}
	}

	private synchronized void stageFinished() {
		mIsWaiting = false;
		notifyAll();
	}


	private void doOnBefore() {
		mMainThreadHandler.post(new Runnable(){

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
	public final void run() {

		try {
            if(!mCanceled) {
			    doOnBefore();
            }
            if(!mCanceled) {
			    waitForStage();
            }
            if(!mCanceled) {
			    onBackground();
            }
		} catch(Throwable e){
			LOGGER.error("Unhandled exception while executing BackgroundTask", e);
			throw new RuntimeException(e);
		} finally {
			mMainThreadHandler.post(new Runnable(){
				@Override
				public void run() {
					if(!mCanceled){
						onAfter();
					}
				}
			});
		}
	}

    public void cancel() {
        mCanceled = true;
    }

}