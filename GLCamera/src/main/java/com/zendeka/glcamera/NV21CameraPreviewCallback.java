package com.zendeka.glcamera;

import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.zendeka.glesutils.utils.GLGetError;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_LUMINANCE;
import static android.opengl.GLES20.GL_LUMINANCE_ALPHA;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE1;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glTexImage2D;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLES20.glTexSubImage2D;

/**
 * Created by Lawrence on 8/2/13.
 */
public class NV21CameraPreviewCallback implements CameraPreviewCallback {
    private final String mTag;

    private EGLContextFactory mEglContextFactory;
    private GLSurfaceView mGLSurfaceView;

    private int mWidth;
    private int mHeight;

    private int mYTexture;
    private int mUVTexture;

    private boolean mTexturesCreated;

    public NV21CameraPreviewCallback(String tag) {
        mTag = tag;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mWidth == 0 || mHeight == 0) {
            Log.w(mTag, "OnPreviewFrame: width or height == 0");
            return;
        }

        final int yDataLength = mWidth * mHeight;
        final int uvDataLength = data.length - yDataLength;

        final ByteBuffer yPixels = ByteBuffer.allocateDirect(yDataLength).order(ByteOrder.nativeOrder());
        yPixels.put(data, 0, yDataLength).position(0);

        final ByteBuffer uvPixels = ByteBuffer.allocateDirect(uvDataLength).order(ByteOrder.nativeOrder());
        uvPixels.put(data, yDataLength, uvDataLength).position(0);

        if (!mTexturesCreated) {
            Log.d(mTag, "Size: " + mWidth + "x" + mHeight);
            Log.d(mTag, "Total data size: " + data.length);
            Log.d(mTag, "Y data size: " + yDataLength);
            Log.d(mTag, "UV data size: " + uvDataLength);

            mGLSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    //Set up Y texture
                    glActiveTexture(GL_TEXTURE0); GLGetError.getOpenGLErrors(mTag);

                    IntBuffer yTextureBuffer = IntBuffer.allocate(1);

                    glGenTextures(1, yTextureBuffer); GLGetError.getOpenGLErrors(mTag);

                    mYTexture = yTextureBuffer.get(0);

                    glBindTexture(GL_TEXTURE_2D, mYTexture); GLGetError.getOpenGLErrors(mTag);

                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST); GLGetError.getOpenGLErrors(mTag);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST); GLGetError.getOpenGLErrors(mTag);

                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE); GLGetError.getOpenGLErrors(mTag);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE); GLGetError.getOpenGLErrors(mTag);

                    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, mWidth, mHeight, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, null); GLGetError.getOpenGLErrors(mTag);

                    glBindTexture(GL_TEXTURE_2D, 0); GLGetError.getOpenGLErrors(mTag);

                    //Set up UV texture
                    glActiveTexture(GL_TEXTURE1); GLGetError.getOpenGLErrors(mTag);

                    IntBuffer uvTextureBuffer = IntBuffer.allocate(1);

                    glGenTextures(1, uvTextureBuffer); GLGetError.getOpenGLErrors(mTag);

                    mUVTexture = uvTextureBuffer.get(0);

                    glBindTexture(GL_TEXTURE_2D, mUVTexture); GLGetError.getOpenGLErrors(mTag);

                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST); GLGetError.getOpenGLErrors(mTag);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST); GLGetError.getOpenGLErrors(mTag);

                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE); GLGetError.getOpenGLErrors(mTag);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE); GLGetError.getOpenGLErrors(mTag);

                    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE_ALPHA, mWidth / 2, mHeight / 2, 0, GL_LUMINANCE_ALPHA, GL_UNSIGNED_BYTE, null); GLGetError.getOpenGLErrors(mTag);

                    //Reset texture bindings
                    glBindTexture(GL_TEXTURE_2D, 0); GLGetError.getOpenGLErrors(mTag);

                    glActiveTexture(GL_TEXTURE0); GLGetError.getOpenGLErrors(mTag);
                }
            });

            mTexturesCreated = true;
        }

        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                //Upload Y data
                glActiveTexture(GL_TEXTURE0); GLGetError.getOpenGLErrors(mTag);
                glBindTexture(GL_TEXTURE_2D, mYTexture); GLGetError.getOpenGLErrors(mTag);
                glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, mWidth, mHeight, GL_LUMINANCE, GL_UNSIGNED_BYTE, yPixels); GLGetError.getOpenGLErrors(mTag);

                //Upload UV data
                glActiveTexture(GL_TEXTURE1); GLGetError.getOpenGLErrors(mTag);
                glBindTexture(GL_TEXTURE_2D, mUVTexture); GLGetError.getOpenGLErrors(mTag);
                glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, mWidth / 2, mHeight / 2, GL_LUMINANCE_ALPHA, GL_UNSIGNED_BYTE, uvPixels); GLGetError.getOpenGLErrors(mTag);

                glActiveTexture(GL_TEXTURE0); GLGetError.getOpenGLErrors(mTag);
            }
        });
    }

    public void setEGLContextFactory(EGLContextFactory eglContextFactory) {
        mEglContextFactory = eglContextFactory;
    }

    public EGLContextFactory getEGLContextFactory() {
        return mEglContextFactory;
    }

    public void setGLSurfaceView(GLSurfaceView glSurfaceView) {
        mGLSurfaceView = glSurfaceView;
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

    public int getUVTexture() {
        return mUVTexture;
    }

    public void releaseTextures() {
        int[] textures = {mYTexture, mUVTexture};
        glDeleteTextures(2, textures, 0);

        mYTexture = 0;
        mUVTexture = 0;
        mTexturesCreated = false;
    }
}
