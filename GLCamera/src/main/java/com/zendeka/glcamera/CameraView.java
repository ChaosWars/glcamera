package com.zendeka.glcamera;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.IOException;

/**
 * Created by Lawrence on 8/28/13.
 */
public class CameraView extends FrameLayout implements SurfaceHolder.Callback {
    private static String TAG = "CameraView";

    private ICameraRenderer mICameraRenderer;
    private CameraPreview mCameraPreview;

    private int mCameraId;
    private Camera mCamera;

    public CameraView(Context context) {
        super(context);
        init(context);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public ICameraRenderer getICameraRenderer() {
        return mICameraRenderer;
    }

    public CameraPreview getCameraPreview() {
        return mCameraPreview;
    }

    public Camera getCamera() {
        return mCamera;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mICameraRenderer.layout(l, t, r, b);
//        mCameraPreview.layout(l, t, r, b);
        mCameraPreview.layout(0, 0, 1, 1);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");
        releaseCamera();

        mCamera = CameraAccess.openCamera(getContext(), mCameraId);
        CameraPreviewCallback cameraPreviewCallback = mICameraRenderer.getCameraPreviewCallback();


        if (mCamera == null || cameraPreviewCallback == null) {
            return;
        }

        CameraAccess.configureCamera(mCamera);

        Camera.Parameters params = mCamera.getParameters();
        Camera.Size previewSize = params.getPreviewSize();

        final CameraRenderer.Size size = new CameraRenderer.Size();
        size.width = previewSize.width;
        size.height = previewSize.height;

        mICameraRenderer.getOpenGLSurfaceView().queueEvent(new Runnable() {
            @Override
            public void run() {
                mICameraRenderer.setCameraSize(size);
            }
        });

        mCamera.setPreviewCallback(cameraPreviewCallback);

        try {
            mCamera.setPreviewDisplay(mCameraPreview.getSurfaceHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Starting camera preview");
        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        releaseCamera();

        mICameraRenderer.getOpenGLSurfaceView().queueEvent(new Runnable() {
            @Override
            public void run() {
                releaseCameraRendererResources();
            }
        });
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void releaseCameraRendererResources() {
        if (mICameraRenderer != null && mICameraRenderer.getCameraRenderer() != null) {
            CameraRenderer cameraRenderer = mICameraRenderer.getCameraRenderer();

            cameraRenderer.releaseBuffers();
            cameraRenderer.releaseShaderProgram();
        }
    }

    private void init(Context context) {
        mCameraPreview = new CameraPreview(context);
        addView(mCameraPreview, new FrameLayout.LayoutParams(1, 1));
//        addView(mCameraPreview, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
//                FrameLayout.LayoutParams.MATCH_PARENT));

        SurfaceHolder cameraSurfaceHolder = mCameraPreview.getSurfaceHolder();

        if (cameraSurfaceHolder != null) {
            cameraSurfaceHolder.addCallback(this);
        }

        mICameraRenderer = new ICameraRenderer(context);
        addView(mICameraRenderer,
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));

        mCameraId = CameraAccess.getBackFacingCamera(context);
        Log.d(TAG, "init()");
    }
}
