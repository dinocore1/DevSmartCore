package com.devsmart.android.fragments;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;

import com.devsmart.android.CameraWrapper;
import com.devsmart.android.views.CameraPreviewView;

import java.io.IOException;

public class CameraPreviewFragment extends Fragment implements SurfaceHolder.Callback {

    private static final String ARG_CAMID = "camid";

    private CameraWrapper mCamera;
    private Camera.Size mPreviewSize;

    private CameraPreviewView mFrame;
    private float mZoom = 1.0f;
    private float mPanX = 0f;
    private float mPanY = 0f;


    public static CameraPreviewFragment createInstance(int cameraId) {
        Bundle args = new Bundle();
        args.putInt(ARG_CAMID, cameraId);

        CameraPreviewFragment retval = new CameraPreviewFragment();
        retval.setArguments(args);
        return retval;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        int camId = args.getInt(ARG_CAMID);
        mCamera = CameraWrapper.getCameara(camId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFrame = new CameraPreviewView(getActivity());
        mFrame.setZoomPan(1.6f, 0, 0);

        mFrame.mSurfaceView.getHolder().addCallback(this);
        return mFrame;
    }

    public void setPanZoom(float zoom, float x, float y) {

    }

    @Override
    public void onResume() {

        mCamera.open();

        Camera.Parameters params = mCamera.configure().getParameters();
        mPreviewSize = params.getPreviewSize();

        mFrame.setCameraPreviewSize(mPreviewSize.width, mPreviewSize.height);


        super.onResume();
    }

    @Override
    public void onPause() {
        mCamera.release();
        super.onPause();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        try {
            mCamera.configure().setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            Log.e("", "", e);
        }

        mCamera.startPreview();


    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCamera.stopPreview();
        try {
            mCamera.configure().setPreviewDisplay(null);
        } catch (IOException e) {
            Log.e("", "", e);
        }
    }
}
