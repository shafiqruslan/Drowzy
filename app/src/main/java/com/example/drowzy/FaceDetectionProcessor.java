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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.IOException;
import java.util.List;


/**
 * Face Detector Demo.
 */
public class FaceDetectionProcessor extends VisionProcessorBase<List<FirebaseVisionFace>> {

    private static final String TAG = "FaceDetectionProcessor";
    private final FirebaseVisionFaceDetector detector;

    private boolean sleep;
    private Context context;
    private LivePreviewActivity livePreviewActivity;
    private long begin = 0;
    public static int flag = 0;

    public FaceDetectionProcessor(Context context, LivePreviewActivity livePreviewActivity) {
        this.context = context;
        this.livePreviewActivity = livePreviewActivity;

        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .build();

        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);

    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionFace>> detectInImage(FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionFace> faces,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay
    ) {

        graphicOverlay.clear();
        if (originalCameraImage != null) {
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
            graphicOverlay.add(imageGraphic);
        }
        for (int i = 0; i < faces.size(); ++i) {
            FirebaseVisionFace face = faces.get(i);

            int cameraFacing =
                    frameMetadata != null ? frameMetadata.getCameraFacing() :
                            Camera.CameraInfo.CAMERA_FACING_BACK;
            FaceGraphic faceGraphic = new FaceGraphic(graphicOverlay, face, cameraFacing);
            graphicOverlay.add(faceGraphic);
            eyeTracking(face);
        }
        graphicOverlay.postInvalidate();
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }

    /*
     * handleMessage() defines the operations to perform when
     * the Handler receives a new Message to process.
     */

    public void eyeTracking(FirebaseVisionFace face) {
        boolean right_eye_sleep = face.getRightEyeOpenProbability() < 0.2;
        boolean left_eye_sleep = face.getLeftEyeOpenProbability() < 0.2;

        if (right_eye_sleep && left_eye_sleep) {
            //if your begin variable is reset
            if (begin == 0) {
                begin = System.currentTimeMillis();
                sleep = true;
            }
            Log.d(TAG, "eyeTracking: " + sleep);
        } else {
            //reset your begin variable
            begin = 0;
            if(flag==1) {
                livePreviewActivity.cancelDialog();
                livePreviewActivity.stopPlaying();
            }
            sleep = false;
        }

        Log.d(TAG, "Eyes closed time: "+ "begin" + begin + "current" + System.currentTimeMillis());
        if(sleep && System.currentTimeMillis()-begin>500){
            Log.d(TAG, "Show alert");
//            alertBox();

            if (flag==0) {
                livePreviewActivity.showWarningDialog();
                begin = 0;
//                submit();
            }
            //for saving
//            flag = 0;
        }
    }

    private void submit(){
        livePreviewActivity.runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                livePreviewActivity.submitPost();
            }
        });
    }

//    private void playMedia(){
//        stopPlaying();
//        mediaPlayer = MediaPlayer.create(context, R.raw.alarm);
//        mediaPlayer.start(); // no need to call prepare(); create() does that for you
//    }
//
//    private void stopPlaying(){
//        if(mediaPlayer!=null) {
//            mediaPlayer.stop();
//            mediaPlayer.release();
//            mediaPlayer = null;
//        }
//    }
//
//    public void alertBox(){
////        handler.post(new Runnable() {
////            public void run() {
//        playMedia();
//                AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                builder.setMessage("Drowsy Detected. Do you want to navigate to the nearest places?")
//                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                // FIRE ZE MISSILES!
//                                flag=0;
//                                stopPlaying();
//                                Intent intent = new Intent(livePreviewActivity, LocationList.class);
////                                Uri gmmIntentUri = Uri.parse("google.navigation:q=Taronga+Zoo,+Sydney+Australia");
////                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
////                                mapIntent.setPackage("com.google.android.apps.maps");
//                                livePreviewActivity.startActivity(intent);
//                                livePreviewActivity.finish();
//                            }
//                        })
//                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                // User cancelled the dialog
//                                stopPlaying();
//                                flag=0;
//                            }
//                        });
//                alertDialog = builder.create();
//                // Create the AlertDialog object and return it
//                    alertDialog.show();
//                    Log.d(TAG, "alertBox: alertBox is popup");
//            }

//        });
//    }


}
