package com.zendeka.glcamera;

import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.zendeka.glesutils.utils.GLGetError;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Created by Lawrence on 8/2/13.
 */
public class CameraPreviewCallback implements Camera.PreviewCallback {
    private final GLSurfaceView mGLSurfaceView;

    private final String mTag;

    private int mWidth;
    private int mHeight;

    private int mYTexture;
    private int mUVTexture;

    private boolean mTexturesCreated;

    public CameraPreviewCallback(String tag, GLSurfaceView glSurfaceView) {
        mTag = tag;
        mGLSurfaceView = glSurfaceView;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mWidth == 0 || mHeight == 0 || mGLSurfaceView == null) {
            Log.w(mTag, "OnPreviewFrame: width or height == 0");
            return;
        }

        final int yDataLength = mWidth * mHeight;
        final int uvDataLength = data.length - yDataLength;
        final ByteBuffer yPixels = ByteBuffer.wrap(data, 0, yDataLength);
        final ByteBuffer uvPixels = ByteBuffer.wrap(data, yDataLength, uvDataLength);

        if (!mTexturesCreated) {
            Log.d(mTag, "Size: " + mWidth + "x" + mHeight);
            Log.d(mTag, "data size: " + data.length);
            Log.d(mTag, "Y data size: " + yDataLength);
            Log.d(mTag, "UV data size: " + uvDataLength);

            mGLSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    IntBuffer yTextureBuffer = IntBuffer.allocate(1);
                    GLES20.glGenTextures(1, yTextureBuffer); GLGetError.getOpenGLErrors(mTag);
                    mYTexture = yTextureBuffer.get(0);
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mYTexture); GLGetError.getOpenGLErrors(mTag);
                    GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, mWidth, mHeight, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, null); GLGetError.getOpenGLErrors(mTag);

                    IntBuffer uvTextureBuffer = IntBuffer.allocate(1);
                    GLES20.glGenTextures(1, uvTextureBuffer); GLGetError.getOpenGLErrors(mTag);
                    mUVTexture = uvTextureBuffer.get(0);
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mUVTexture); GLGetError.getOpenGLErrors(mTag);
                    GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA, mWidth, mHeight / 2, 0, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, null); GLGetError.getOpenGLErrors(mTag);
                }
            });

            mTexturesCreated = true;
        }

        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                //Upload Y data
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0); GLGetError.getOpenGLErrors(mTag);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mYTexture); GLGetError.getOpenGLErrors(mTag);
                GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mWidth, mHeight, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, yPixels); GLGetError.getOpenGLErrors(mTag);

                //Upload UV data
                GLES20.glActiveTexture(GLES20.GL_TEXTURE1); GLGetError.getOpenGLErrors(mTag);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mUVTexture); GLGetError.getOpenGLErrors(mTag);
                GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mWidth, mHeight / 2, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, uvPixels); GLGetError.getOpenGLErrors(mTag);
            }
        });
    }

    public GLSurfaceView getGLSurfaceView() {
        return mGLSurfaceView;
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

    public void releaseTextures() {
        int[] textures = {mYTexture, mUVTexture};
        IntBuffer textureBuffer = IntBuffer.wrap(textures);

        GLES20.glDeleteTextures(2, textureBuffer);

        mYTexture = 0;
        mUVTexture = 0;
        mTexturesCreated = false;
    }
}
