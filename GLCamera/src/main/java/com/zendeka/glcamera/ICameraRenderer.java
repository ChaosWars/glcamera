package com.zendeka.glcamera;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;
import android.view.ViewGroup;

import com.zendeka.glcamera.CameraRenderer.Size;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Lawrence on 8/2/13.
 */
public class ICameraRenderer extends ViewGroup implements Renderer {
    private static final String TAG = "ICameraRenderer";

    private GLSurfaceView mGLSurfaceView;
    private CameraRenderer mCameraRenderer;
    private CameraPreviewCallback mCameraPreviewCallback;

    private Size mCameraSize = new Size();
    private Size mScreenSize = new Size();

    public ICameraRenderer(Context context) {
        super(context);

        mGLSurfaceView = new GLSurfaceView(context);
        mGLSurfaceView.setZOrderMediaOverlay(true);

        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        mGLSurfaceView.setRenderer(this);

        addView(mGLSurfaceView);

        mCameraPreviewCallback = new CameraPreviewCallback();
        mCameraPreviewCallback.setGLSurfaceView(mGLSurfaceView);
    }

    public void setCameraSize(Size size) {
        mCameraSize = size;
        setupCameraRendererBuffers();
    }

    public GLSurfaceView getOpenGLSurfaceView() {
        return mGLSurfaceView;
    }

    public CameraRenderer getCameraRenderer() {
        return mCameraRenderer;
    }

    public CameraPreviewCallback getCameraPreviewCallback() {
        return mCameraPreviewCallback;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mGLSurfaceView.layout(l, t, r, b);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated");

        mCameraRenderer = new CameraRenderer(mGLSurfaceView.getContext());
        mCameraRenderer.setCameraPreviewCallback(mCameraPreviewCallback);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged");

        mScreenSize.width = width;
        mScreenSize.height = height;

        GLES20.glViewport(0, 0, mScreenSize.width, mScreenSize.height);

        mCameraPreviewCallback.setWidth(mScreenSize.width);
        mCameraPreviewCallback.setHeight(mScreenSize.height);

        setupCameraRendererBuffers();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mCameraRenderer.render();
    }

    private void setupCameraRendererBuffers() {
        if (mScreenSize.width > 0 && mScreenSize.height > 0
                && mCameraSize.width > 0 && mCameraSize.height > 0) {
            mCameraRenderer.createBuffers(mScreenSize, mCameraSize);
        }
    }
}
