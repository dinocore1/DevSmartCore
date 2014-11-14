package com.devsmart.android.fragments;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class CameraPreviewFragment extends Fragment implements SurfaceHolder.Callback {

    private FrameLayout mFrame;
    private SurfaceView mSurfaceView;
    private float mZoom = 1.0f;
    private float mPanX = 0f;
    private float mPanY = 0f;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFrame = new FrameLayout(getActivity());
        mFrame.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mSurfaceView = new SurfaceView(getActivity());
        mSurfaceView.getHolder().addCallback(this);
        mFrame.addView(mSurfaceView);


        return mFrame;
    }

    public void setPanZoom(float zoom, float x, float y) {

    }

    @Override
    public void onResume() {



        super.onResume();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
