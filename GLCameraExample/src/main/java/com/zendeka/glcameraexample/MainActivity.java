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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainLayout = (FrameLayout) findViewById(R.id.mainLayout);
        mCameraView = (CameraView) findViewById(R.id.cameraView);

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
        mCameraView.releaseCamera();
        mCameraView.releaseCameraRendererResources();
        mMainLayout.setKeepScreenOn(false);
        mCameraView.getICameraRenderer().getOpenGLSurfaceView().onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMainLayout.setKeepScreenOn(true);
        mCameraView.getICameraRenderer().getOpenGLSurfaceView().onResume();
    }
}
