package com.zendeka.glcamera;

import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

/**
 * Created by Lawrence on 8/28/13.
 */
public class CameraPreview  {
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;

    private List<Camera.Size> mSupportedCameraSizes;

    public CameraPreview(Context context) {
        init(context);
    }

    public List<Camera.Size> getSupportedCameraSizes() {
        return mSupportedCameraSizes;
    }

    public SurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    public SurfaceHolder getSurfaceHolder() {
        return mSurfaceHolder;
    }

    private void init(Context context) {
        mSurfaceView = new SurfaceView(context);
        mSurfaceHolder = mSurfaceView.getHolder();

        if (mSurfaceHolder != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            }
        }
    }
}
