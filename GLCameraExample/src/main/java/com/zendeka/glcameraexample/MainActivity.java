package com.zendeka.glcameraexample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.FrameLayout;

import com.zendeka.glcamera.CameraView;
import com.zendeka.glcamera.EGLContextFactory;
import com.zendeka.glcamera.NV21CameraPreviewCallback;

import javax.microedition.khronos.egl.EGL10;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private FrameLayout mMainLayout;
    private CameraView mCameraView;
    private GLRenderer mGLRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainLayout = (FrameLayout) findViewById(R.id.mainLayout);

        int[] attributeList = {EGLContextFactory.EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};

        EGLContextFactory eglContextFactory = new EGLContextFactory();
        eglContextFactory.setAttributeList(attributeList);
        eglContextFactory.setShared(true);

        mGLRenderer = new GLRenderer(this, eglContextFactory);

        NV21CameraPreviewCallback cameraPreviewCallback = new NV21CameraPreviewCallback("NV21CameraPreviewCallback");
//        YV12CameraPreviewCallback cameraPreviewCallback = new YV12CameraPreviewCallback("YV12CameraPreviewCallback");

        cameraPreviewCallback.setEGLContextFactory(eglContextFactory);
        cameraPreviewCallback.setGLSurfaceView(mGLRenderer.getOpenGLSurfaceView());

        mGLRenderer.setCameraPreviewCallback(cameraPreviewCallback);

        mCameraView = new CameraView(this);
        mCameraView.setCamaraRenderer(mGLRenderer);
        mCameraView.setCameraPreviewCallback(cameraPreviewCallback);

        addContentView(mCameraView.getCameraPreview().getSurfaceView(), new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        addContentView(mGLRenderer.getOpenGLSurfaceView(),
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));

        Log.d(TAG, "onCreate");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMainLayout.setKeepScreenOn(false);
        mGLRenderer.getOpenGLSurfaceView().onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMainLayout.setKeepScreenOn(true);
        mGLRenderer.getOpenGLSurfaceView().onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGLRenderer.releaseCameraRendererResources();
    }
}
