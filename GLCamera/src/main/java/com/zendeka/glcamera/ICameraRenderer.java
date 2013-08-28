package com.zendeka.glcamera;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Lawrence on 8/2/13.
 */
public class ICameraRenderer implements Renderer {
    private GLSurfaceView mGLSurfaceView;
    private CameraRenderer mCameraRenderer;
    private CameraPreviewCallback mCameraPreviewCallback;

    public void setOpenGLSurfaceView(GLSurfaceView glSurfaceView) {
        mGLSurfaceView = glSurfaceView;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mCameraRenderer = new CameraRenderer(mGLSurfaceView.getContext());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        mCameraPreviewCallback = new CameraPreviewCallback(mGLSurfaceView, width, height);
        mCameraRenderer.setCameraPreviewCallback(mCameraPreviewCallback);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mCameraRenderer.render();
    }
}
