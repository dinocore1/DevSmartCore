package com.devsmart.android.views;


import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

public class CameraPreviewView extends ViewGroup {

    public SurfaceView mSurfaceView;

    private int mCameraWidth;
    private int mCameraHeight;

    private float mZoom = 1.0f;
    private float mPanX = 0f;
    private float mPanY = 0f;
    private RectF src = new RectF();
    private RectF dest = new RectF();
    private Matrix matrix = new Matrix();
    private boolean centerCrop = true;


    public CameraPreviewView(Context context) {
        super(context);
        init();
    }

    public CameraPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraPreviewView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setClipChildren(true);
        mSurfaceView = new SurfaceView(getContext());
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        addView(mSurfaceView);

    }

    public void setZoomPan(float zoom, float x, float y){
        mZoom = zoom;
        mPanX = x;
        mPanY = y;
        requestLayout();
    }

    public void setCameraPreviewSize(int width, int height) {
        mCameraWidth = width;
        mCameraHeight = height;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        dest.set(l, t, r, b);

        if(centerCrop){
            int newWidth, newHeight;
            if(getMeasuredWidth() / (float)getMeasuredHeight() > 1){
                newWidth = getMeasuredWidth();
                newHeight = (int) (newWidth * mCameraHeight / (float)mCameraWidth);
            } else {
                newHeight = getMeasuredHeight();
                newWidth = (int) (newHeight * mCameraWidth / (float)mCameraHeight);
            }

            float dx = (newWidth - dest.width()) / 2;
            float dy = (newHeight - dest.height()) / 2;

            src.set(l - dx, t - dy, r + dx, b + dy);
            matrix.setScale(mZoom, mZoom, src.centerX(), src.centerY());
            matrix.postTranslate(mPanX, mPanY);
            matrix.mapRect(src);

            mSurfaceView.layout((int)src.left, (int)src.top, (int)src.right, (int)src.bottom);

        } else {
            src.set(0, 0, mCameraWidth, mCameraHeight);

            matrix.setRectToRect(src, dest, Matrix.ScaleToFit.CENTER);
            matrix.preScale(mZoom, mZoom, mPanX, mPanY);
            matrix.mapRect(src);

            mSurfaceView.layout((int) src.left, (int) src.top, (int) src.right, (int) src.bottom);
        }

    }

}
