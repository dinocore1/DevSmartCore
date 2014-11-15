package com.devsmart.android.demo;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.devsmart.android.fragments.CameraPreviewFragment;
import com.devsmart.android.fragments.ChooseFileDialogFragment;

import java.io.File;


public class DemoActivity extends FragmentActivity {

    private Button mChooseFile;
    private Button mCameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        mChooseFile = (Button) findViewById(R.id.choosefile);
        mChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChooseFileDialogFragment chooseFrag = ChooseFileDialogFragment.newInstance();
                chooseFrag.setCallback(new ChooseFileDialogFragment.ChooseFileCallback() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onFileSelected(File file) {
                        Toast.makeText(DemoActivity.this, file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    }
                });
                chooseFrag.show(getSupportFragmentManager(), "Choose");
            }
        });

        mCameraPreview = (Button) findViewById(R.id.camerapreview);
        mCameraPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CameraPreviewFragment frag = CameraPreviewFragment.createInstance(0);

                FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
                tr.replace(R.id.content, frag);
                tr.addToBackStack(null);
                tr.commit();
            }
        });
    }
}
