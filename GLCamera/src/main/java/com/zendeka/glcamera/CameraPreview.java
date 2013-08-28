package com.zendeka.glcamera;

import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Lawrence on 8/28/13.
 */
public class CameraPreview extends ViewGroup {
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;

    private List<Camera.Size> mSupportedCameraSizes;

    public CameraPreview(Context context) {
        super(context);
        init(context);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public List<Camera.Size> getSupportedCameraSizes() {
        return mSupportedCameraSizes;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        mSurfaceView.layout(l, t, r, b);
//        mSurfaceView.layout(0, 0, 1, 1);
    }

    public SurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    public SurfaceHolder getSurfaceHolder() {
        return mSurfaceHolder;
    }

    private void init(Context context) {
        mSurfaceView = new SurfaceView(context);
        addView(mSurfaceView);

        mSurfaceHolder = mSurfaceView.getHolder();

        if (mSurfaceHolder != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            }
        }
    }
}
