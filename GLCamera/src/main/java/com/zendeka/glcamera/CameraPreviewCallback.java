package com.zendeka.glcamera;

import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;

/**
 * Created by Lawrence on 8/2/13.
 */
public class CameraPreviewCallback implements Camera.PreviewCallback {
    private final GLSurfaceView mGLSurfaceView;
    private final int mWidth;
    private final int mHeight;

    private int mYTexture;
    private int mUVTexture;

    public CameraPreviewCallback(GLSurfaceView surfaceView, int width, int height) {
        mGLSurfaceView = surfaceView;
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
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

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
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
