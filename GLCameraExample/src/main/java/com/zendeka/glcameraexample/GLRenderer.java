package com.zendeka.glcameraexample;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import com.zendeka.glcamera.CameraPreviewCallback;
import com.zendeka.glcamera.CameraRenderer;
import com.zendeka.glcamera.CameraRenderer.Size;
import com.zendeka.glcamera.ICameraRenderer;
import com.zendeka.glesutils.utils.GLGetError;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

/**
 * Created by Lawrence on 8/2/13.
 */
public class GLRenderer implements Renderer, ICameraRenderer {
    private static final String TAG = "GLRenderer";

    private GLSurfaceView mGLSurfaceView;
    private com.zendeka.glcamera.CameraRenderer mCameraRenderer;
    private CameraPreviewCallback mCameraPreviewCallback;

    private Size mCameraSize = new Size();
    private Size mScreenSize = new Size();

    public GLRenderer(Context context) {
        mGLSurfaceView = new GLSurfaceView(context);
        mGLSurfaceView.setZOrderMediaOverlay(true);

        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        mGLSurfaceView.setRenderer(this);

        mCameraPreviewCallback = new CameraPreviewCallback("CameraPreviewCallback", mGLSurfaceView);
    }

    public GLSurfaceView getOpenGLSurfaceView() {
        return mGLSurfaceView;
    }

    public com.zendeka.glcamera.CameraRenderer getCameraRenderer() {
        return mCameraRenderer;
    }

    public CameraPreviewCallback getCameraPreviewCallback() {
        return mCameraPreviewCallback;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated");

        mCameraRenderer = new CameraRenderer(mGLSurfaceView.getContext(), "CameraRenderer");
        mCameraRenderer.setCameraPreviewCallback(mCameraPreviewCallback);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged");

        mScreenSize.width = width;
        mScreenSize.height = height;

        glViewport(0, 0, mScreenSize.width, mScreenSize.height); GLGetError.getOpenGLErrors(TAG);

        if (mScreenSize.width > 0 && mScreenSize.height > 0
                && mCameraSize.width > 0 && mCameraSize.height > 0
                && !mCameraRenderer.getBuffersCreated()) {
            mCameraRenderer.createBuffers(mScreenSize, mCameraSize);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f); GLGetError.getOpenGLErrors(TAG);
        glClear(GL_COLOR_BUFFER_BIT); GLGetError.getOpenGLErrors(TAG);

        mCameraRenderer.render();
    }

    @Override
    public void setCameraSize(Size size) {
        mCameraSize = size;
        mCameraPreviewCallback.setWidth(mCameraSize.width);
        mCameraPreviewCallback.setHeight(mCameraSize.height);

        if (mScreenSize.width > 0 && mScreenSize.height > 0
                && mCameraSize.width > 0 && mCameraSize.height > 0
                && mCameraRenderer != null && !mCameraRenderer.getBuffersCreated()) {
            mGLSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    if (mCameraRenderer != null) {
                        mCameraRenderer.createBuffers(mScreenSize, mCameraSize);
                    }
                }
            });
        }
    }

    public void releaseCameraRendererResources() {
        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mCameraRenderer != null) {
                    mCameraRenderer.releaseBuffers();
                    mCameraRenderer.releaseShaderProgram();
                }

                if (mCameraPreviewCallback != null) {
                    mCameraPreviewCallback.releaseTextures();
                }
            }
        });
    }
}
