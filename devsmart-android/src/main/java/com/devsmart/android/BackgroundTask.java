package com.devsmart.android;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.devsmart.ThreadUtils;

public abstract class BackgroundTask implements Runnable {


    public static Future<?> runBackgroundTask(BackgroundTask task, ExecutorService service){
		task.mMainThreadHandler = new Handler(Looper.getMainLooper());
		return service.submit(task);
	}

	public static Future<?> runBackgroundTask(BackgroundTask task){
		task.mMainThreadHandler = new Handler(Looper.getMainLooper());
		return ThreadUtils.IOThreads.submit(task);
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
			Log.e("", "BackgroundTask interrupted", e);
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