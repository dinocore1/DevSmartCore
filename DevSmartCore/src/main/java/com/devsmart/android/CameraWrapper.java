package com.devsmart.android;


import android.hardware.Camera;

import com.devsmart.android.utils.StateMachine;

import java.util.HashMap;

public class CameraWrapper implements StateMachine.StateChangeListener<CameraWrapper.State,CameraWrapper.Input> {

    private static CameraWrapper[] wrappers = new CameraWrapper[Camera.getNumberOfCameras()];
    public static CameraWrapper getCameara(int camId) {
        if(wrappers[camId] == null){
            wrappers[camId] = new CameraWrapper(camId);
        }

        return wrappers[camId];
    }

    public static enum State {
        Closed,
        Idle,
        Preview,
        Capture
    }

    public static enum Input {
        Open,
        Release,
        Configure,
        StartPreview,
        StopPreview,
        TakePicure,
        PictureReady,
    }

    public final int cameraId;
    private StateMachine<State, Input> cameraState;
    private Camera mCamera;

    private CameraWrapper(int cameraId){
        this.cameraId = cameraId;
        cameraState = new StateMachine<State, Input>(State.Closed);

        cameraState.configure(State.Closed, Input.Open, State.Idle);
        cameraState.configure(State.Idle, Input.Release, State.Closed);
        cameraState.configure(State.Idle, Input.Configure, State.Idle);
        cameraState.configure(State.Idle, Input.StartPreview, State.Preview);
        cameraState.configure(State.Preview, Input.StopPreview, State.Idle);

        cameraState.addListener(this);
    }

    @Override
    public void onStateChanged(StateMachine<State, Input> stateMachine, State lastState, State newState, Input input, Object data) {

        switch (input){
            case Open:
                mCamera = Camera.open();
                break;

            case Release:
                mCamera.release();
                mCamera = null;
                break;

            case StartPreview:
                mCamera.startPreview();
                break;

            case StopPreview:
                mCamera.stopPreview();
                break;
        }

    }

    public void open() {
        cameraState.input(Input.Open, null);
    }

    public void release() {
        cameraState.input(Input.Release, null);
    }

    public void startPreview() {
        cameraState.input(Input.StartPreview, null);
    }
    public void stopPreview() {
        cameraState.input(Input.StopPreview, null);
    }


    public Camera configure() {
        if(cameraState.getState() == State.Idle) {
            cameraState.input(Input.Configure, null);
            return mCamera;
        } else {
            return null;
        }
    }
}
