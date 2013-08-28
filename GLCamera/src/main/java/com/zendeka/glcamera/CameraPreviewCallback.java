package com.zendeka.glcamera;

import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;

/**
 * Created by Lawrence on 8/2/13.
 */
public class CameraPreviewCallback implements Camera.PreviewCallback {
    private GLSurfaceView mGLSurfaceView;
    private int mWidth;
    private int mHeight;

    private int mYTexture;
    private int mUVTexture;

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mWidth == 0 || mHeight == 0 || mGLSurfaceView == null) {
            return;
        }

        final int yDataLength = mWidth * mHeight;
        final int uvDataLength = data.length - yDataLength;
        final ByteBuffer yPixels = ByteBuffer.wrap(data, 0, yDataLength);
        final ByteBuffer uvPixels = ByteBuffer.wrap(data, yDataLength, uvDataLength);

        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                //Upload Y data
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mYTexture);
                GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mWidth, mHeight, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, yPixels);

                //Upload UV data
                GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mUVTexture);
                GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mWidth / 2, mHeight / 2, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, uvPixels);
            }
        });
    }

    public GLSurfaceView getGLSurfaceView() {
        return mGLSurfaceView;
    }

    public void setGLSurfaceView(GLSurfaceView glSurfaceView) {
        mGLSurfaceView = glSurfaceView;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public int getYTexture() {
        return mYTexture;
    }

    public void setYTexture(int textureId) {
        mYTexture = textureId;
    }

    public int getUVTexture() {
        return mUVTexture;
    }

    public void setTextureUV(int textureId) {
        mUVTexture = textureId;
    }
}
