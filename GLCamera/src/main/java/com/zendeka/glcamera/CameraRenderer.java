package com.zendeka.glcamera;

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
    public boolean getBuffersCreated();
    public void releaseBuffers();
    public void releaseShaderProgram();

}
