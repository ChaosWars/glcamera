package com.zendeka.glcamera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.util.Log;

/**
 * Created by Lawrence on 8/2/13.
 */
public class CameraAccess {
    private static final String TAG = "CameraAccess";

    public static int getBackFacingCamera (Context context) {
        if (!checkCameraAvailability(context)) {
            return -1;
        }

        int nrCameras = Camera.getNumberOfCameras();

        for (int cameraId = 0; cameraId < nrCameras; ++cameraId) {
            CameraInfo cameraInfo = new CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);

            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                return cameraId;
            }
        }

        return -1;
    }

    public static int getFrontFacingCamera (Context context) {
        if (!checkCameraAvailability(context)) {
            return -1;
        }

        int nrCameras = Camera.getNumberOfCameras();

        for (int cameraId = 0; cameraId < nrCameras; ++cameraId) {
            CameraInfo cameraInfo = new CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);

            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
                return cameraId;
            }
        }

        return -1;
    }

    public static Camera openCamera(Context context, int cameraId) throws IllegalArgumentException {
        if (cameraId < 0) {
            throw new IllegalArgumentException("Invalid camera id");
        }

        if (!checkCameraAvailability(context)) {
            return null;
        }

        Camera camera = null;

        if (cameraId > -1) {
            try {
                camera = Camera.open(cameraId);
            }
            catch (RuntimeException e) {
                Log.e(TAG, "Failed to open camera");
                e.printStackTrace();
            }
        }

        return camera;
    }

    public static Camera.Parameters configureCamera(Camera camera) {
        Camera.Parameters params = camera.getParameters();
        params.setPreviewFormat(ImageFormat.NV21);
        return params;
    }

    private static boolean checkCameraAvailability(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }
}
