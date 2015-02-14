package com.zendeka.glcamera;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * Created by Lawrence on 9/18/13.
 */
public class EGLContextFactory implements GLSurfaceView.EGLContextFactory {
    public static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

    private boolean mShared;
    private EGLContext mSharedContext;

    private int[] mAttributeList;

    @Override
    public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
        EGLContext sharedContext = EGL10.EGL_NO_CONTEXT;

        if (mShared && mSharedContext != null) {
            sharedContext = mSharedContext;
        }

        EGLContext context = egl.eglCreateContext(display, eglConfig, sharedContext, mAttributeList);

        if (mShared && mSharedContext == null) {
            mSharedContext = context;
        }

        return context;
    }

    @Override
    public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
        egl.eglDestroyContext(display, context);
    }

    public boolean getShared() {
        return mShared;
    }

    public void setShared(boolean shared) {
        mShared = shared;
    }

    public EGLContext getSharedContext() {
        return mSharedContext;
    }

    public int[] getAttributeList() {
        return mAttributeList;
    }

    public void setAttributeList(int[] attributeList) {
        mAttributeList = attributeList;
    }
}
