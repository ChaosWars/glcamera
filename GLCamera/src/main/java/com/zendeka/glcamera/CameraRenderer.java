package com.zendeka.glcamera;

import android.content.Context;

/**
 * Created by Lawrence on 8/2/13.
 */
public interface CameraRenderer<CameraPreviewCallbackType> {

    public static class Size {
        public int width;
        public int height;
    }

    public interface OnRenderCallback {
        public void preRender();
        public void postRender();
    }

    public void setOnRenderCallback(OnRenderCallback onRenderCallback);
    public void setCameraPreviewCallback(CameraPreviewCallbackType cameraPreviewCallback);
    public void render();
    public void createBuffers(Size screenSize, Size cameraSize);
    public boolean isBuffersCreated();
    public void releaseBuffers();
    public void releaseShaderProgram();
    public void createShaderProgram(Context context);
    public boolean isShaderProgramCreated();
}
