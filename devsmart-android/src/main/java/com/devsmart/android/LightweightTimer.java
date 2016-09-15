package com.devsmart.android;

import android.os.Handler;
import android.os.Looper;
import com.google.common.base.Preconditions;

import java.lang.ref.WeakReference;

public class LightweightTimer {

    private Handler mHandler;
    private WeakReference<Runnable> mOnTickCallback = null;
    private long mInterval;
    private boolean mRunning = false;

    public LightweightTimer(Runnable r, long millisec) {
        this(r, millisec, new Handler(Looper.getMainLooper()));
    }

    public LightweightTimer(Runnable r, long millisec, Handler handler) {
        mHandler = handler;
        setOnTick(r);
        setInterval(millisec);
    }

    public void setOnTick(Runnable r) {
        mOnTickCallback = new WeakReference<Runnable>(r);
    }

    public void setInterval(long millisec) {
        Preconditions.checkArgument(millisec > 0);
        mInterval = millisec;
    }

    public synchronized void start() {
        if(!mRunning) {
            mHandler.postDelayed(mOnTick, mInterval);
            mRunning = true;
        }
    }

    public synchronized void stop() {
        if(mRunning) {
            mHandler.removeCallbacks(mOnTick);
            mRunning = false;
        }
    }

    public synchronized boolean isRunning() {
        return mRunning;
    }

    private final Runnable mOnTick = new Runnable() {
        @Override
        public void run() {
            Runnable callback;
            synchronized (LightweightTimer.this) {
                if (!mRunning || mOnTickCallback == null || (callback = mOnTickCallback.get()) == null) {
                    stop();
                    return;
                }
            }

            callback.run();

            synchronized (LightweightTimer.this) {
                if(mRunning) {
                    mHandler.postDelayed(mOnTick, mInterval);
                }
            }

        }
    };
}