package com.zendeka.glcamera;

import android.hardware.Camera;
import android.opengl.GLSurfaceView;

/**
 * Created by Lawrence on 9/16/13.
 */
public interface CameraPreviewCallback extends Camera.PreviewCallback {
    public void setEGLContextFactory(EGLContextFactory eglContextFactory);
    public EGLContextFactory getEGLContextFactory();
    public void setGLSurfaceView(GLSurfaceView glSurfaceView);
    public GLSurfaceView getGLSurfaceView();
    public int getWidth();
    public void setWidth(int width);
    public int getHeight();
    public void setHeight(int height);
    public void releaseTextures();
}
