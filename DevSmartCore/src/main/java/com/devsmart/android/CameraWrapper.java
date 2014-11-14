package com.devsmart.android;


import android.hardware.Camera;

import com.devsmart.android.utils.StateMachine;

public class CameraWrapper {

    public static enum State {
        Idle,
        Preview,
        Capture
    }

    public static enum Input {
        Open,
        StartPreview,
        StopPreview,
        TakePicure,
        PictureReady,
        Release
    }

    public final int cameraId;
    StateMachine<State, Input> cameraState;
    private Camera mCamera;

    public CameraWrapper(int cameraId){
        this.cameraId = cameraId;
        mCamera = Camera.open(cameraId);
        cameraState = new StateMachine<State, Input>(State.Idle);
    }

    public void close() {
        mCamera.release();
        cameraState.input(Input.Release, null);
        mCamera = null;
    }
}
