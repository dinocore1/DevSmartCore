package com.devsmart.android.demo;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.devsmart.android.fragments.CameraPreviewFragment;

public class CameraTestActivity extends FragmentActivity {

    private CameraPreviewFragment mCameraFragemnt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);


        mCameraFragemnt = CameraPreviewFragment.createInstance(0, 1.5f, 0, 0);

        FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
        tr.add(R.id.camera, mCameraFragemnt);
        tr.commit();

    }
}
