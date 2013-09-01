package com.zendeka.glcameraexample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.FrameLayout;

import com.zendeka.glcamera.CameraView;

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

        mCameraView = new CameraView(this);

        mGLRenderer = new GLRenderer(this);
        mCameraView.setCamaraRenderer(mGLRenderer);

        mCameraView.setCameraPreviewCallback(mGLRenderer.getCameraPreviewCallback());

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
