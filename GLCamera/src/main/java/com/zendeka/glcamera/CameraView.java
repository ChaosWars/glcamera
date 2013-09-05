package com.zendeka.glcamera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by Lawrence on 8/28/13.
 */
public class CameraView implements SurfaceHolder.Callback {
    private static String TAG = "CameraView";

    private WeakReference<Context> mContext;

    private ICameraRenderer mCameraRenderer;
    private CameraPreviewCallback mCameraPreviewCallback;
    private CameraPreview mCameraPreview;

    private int mCameraId;
    private Camera mCamera;

    public CameraView(Context context) {
        mContext = new WeakReference<Context>(context);
        init(context);
    }

    public ICameraRenderer getCameraRenderer() {
        return mCameraRenderer;
    }

    public void setCamaraRenderer(ICameraRenderer cameraRenderer) {
        mCameraRenderer = cameraRenderer;
    }

    public CameraPreviewCallback getCameraPreviewCallback() {
        return mCameraPreviewCallback;
    }

    public void setCameraPreviewCallback(CameraPreviewCallback previewCallback) {
        mCameraPreviewCallback = previewCallback;
    }

    public CameraPreview getCameraPreview() {
        return mCameraPreview;
    }

    public Camera getCamera() {
        return mCamera;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");
        releaseCamera();

        mCamera = CameraAccess.openCamera(mContext.get(), mCameraId);

        if (mCamera == null) {
            return;
        }

        Camera.Parameters params = CameraAccess.configureCamera(mCamera);
        mCamera.setParameters(params);
        Camera.Size previewSize = params.getPreviewSize();

        final CameraRenderer.Size size = new CameraRenderer.Size();
        size.width = previewSize.width;
        size.height = previewSize.height;

        if (mCameraRenderer != null) {
            mCameraRenderer.setCameraSize(size);
        }

        if (mCameraPreviewCallback != null) {
            mCamera.setPreviewCallback(mCameraPreviewCallback);
        }

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
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void init(Context context) {
        mCameraPreview = new CameraPreview(context);
        SurfaceHolder cameraSurfaceHolder = mCameraPreview.getSurfaceHolder();

        if (cameraSurfaceHolder != null) {
            cameraSurfaceHolder.addCallback(this);
        }

        mCameraId = CameraAccess.getBackFacingCamera(context);
        Log.d(TAG, "init()");
    }
}
