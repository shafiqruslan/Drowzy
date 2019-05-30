// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.example.drowzy;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.annotation.KeepName;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Demo app showing the various features of ML Kit for Firebase. This class is used to
 * set up continuous frame processing on frames from a camera source. */
@KeepName
public final class LivePreviewActivity extends AppCompatActivity
        implements OnRequestPermissionsResultCallback,
        CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "LivePreviewActivity";

    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    public static final int PERMISSIONS_REQUEST_CAMERA = 9000;
//    private static final int PERMISSION_REQUESTS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_live_preview);

        preview = (CameraSourcePreview) findViewById(R.id.firePreview);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = (GraphicOverlay) findViewById(R.id.fireFaceOverlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        ToggleButton facingSwitch = (ToggleButton) findViewById(R.id.facingSwitch);
        facingSwitch.setOnCheckedChangeListener(this);
        // Hide the toggle button if there is only 1 camera
        if (Camera.getNumberOfCameras() == 1) {
            facingSwitch.setVisibility(View.GONE);
        }

//        if (allPermissionsGranted()) {
//            if(isServicesOK()) {
//                if (isMapsEnabled()) {
//                    createCameraSource();
//                    startLocationService();
//                    Log.d(TAG, "onCreate: Camera Source");
//                }
//            }
//        } else {
//            getRuntimePermissions();
//        }

        getCameraPermission();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "Set facing");
        if (cameraSource != null) {
            if (isChecked) {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
            } else {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
            }
        }
        preview.stop();
        startCameraSource();
    }

    private void createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }
        Log.i(TAG, "Using Face Detector Processor");
        cameraSource.setMachineLearningFrameProcessor(new FaceDetectionProcessor(this, this));
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }


//    private String[] getRequiredPermissions() {
//        try {
//            PackageInfo info =
//                    this.getPackageManager()
//                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
//            String[] ps = info.requestedPermissions;
//            if (ps != null && ps.length > 0) {
//                return ps;
//            } else {
//                return new String[0];
//            }
//        } catch (Exception e) {
//            return new String[0];
//        }
//    }
//
//    private boolean allPermissionsGranted() {
//        for (String permission : getRequiredPermissions()) {
//            if (!isPermissionGranted(this, permission)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private void getRuntimePermissions() {
//        List<String> allNeededPermissions = new ArrayList<>();
//        for (String permission : getRequiredPermissions()) {
//            if (!isPermissionGranted(this, permission)) {
//                allNeededPermissions.add(permission);
//            }
//        }
//
//        if (!allNeededPermissions.isEmpty()) {
//            ActivityCompat.requestPermissions(
//                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(
//            int requestCode, String[] permissions, int[] grantResults) {
//        Log.i(TAG, "Permission granted!");
//        if (allPermissionsGranted()) {
//            if(isServicesOK()) {
//                if (isMapsEnabled()) {
//                    createCameraSource();
//                }
//            }
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }
//
//    private static boolean isPermissionGranted(Context context, String permission) {
//        if (ContextCompat.checkSelfPermission(context, permission)
//                == PackageManager.PERMISSION_GRANTED) {
//            Log.i(TAG, "Permission granted: " + permission);
//            return true;
//        }
//        Log.i(TAG, "Permission NOT granted: " + permission);
//        return false;
//    }

    private void getCameraPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_CAMERA);
        } else {
            // Permission has already been granted
            createCameraSource();
        }
    }

        @Override
        public void onRequestPermissionsResult(int requestCode,
                                               String[] permissions, int[] grantResults) {
            switch (requestCode) {
                case PERMISSIONS_REQUEST_CAMERA: {
                    Log.d(TAG, "onRequestPermissionsResult: true");
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // permission was granted, yay! Do the
                        // contacts-related task you need to do.
                        createCameraSource();
                    } else {
                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                        getCameraPermission();
                    }
                }

                // other 'case' lines to check for other
                // permissions this app might request.
            }
    }
}