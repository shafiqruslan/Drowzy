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
import android.media.FaceDetector;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.face.Face;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


/**
 * Face Detector Demo.
 */
public class FaceDetectionProcessor extends VisionProcessorBase<List<FirebaseVisionFace>> {

    private static final String TAG = "FaceDetectionProcessor";
    private final FirebaseVisionFaceDetector detector;

    private Context context;
    public LivePreviewActivity livePreviewActivity;
    private boolean sleep;
    private long begin = 0;
    private int flag = 0;
    private AlertDialog alertDialog;
    private MediaPlayer mediaPlayer;

    //Database
    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]


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

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]

    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
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
            if(flag==0){
                eyeTracking(face);
            }
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

//    public void eyeTracking(FirebaseVisionFace face){
//        boolean right_eye = face.getRightEyeOpenProbability() < 0.5;
//        boolean left_eye =  face.getLeftEyeOpenProbability() < 0.5;
//        Handler mHandler = new Handler();
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        };
//
//        if (right_eye && left_eye) {
//            begin = System.currentTimeMillis();
//            Log.d(TAG, "eyeTracking: " + sleep);
//            mHandler.postDelayed(runnable, 3000);
//            sleep = true;
//        }
//        else {
//            sleep = false;
//        }
//
//        if(sleep){
//            stop = System.currentTimeMillis();
//            Log.d(TAG, "eyeTracking: " + begin + "stop" + stop);
//            if(begin - stop >= 3000) {
//                alertBox();
//            }
//        }
//   }

    public void eyeTracking(FirebaseVisionFace face) {
        boolean right_eye = face.getRightEyeOpenProbability() < 0.2;
        boolean left_eye = face.getLeftEyeOpenProbability() < 0.2;

        if (right_eye && left_eye) {
            //if your begin variable is reset
            if (begin == 0) {
                begin = System.currentTimeMillis();
                sleep = true;
            }
            Log.d(TAG, "eyeTracking: " + sleep);
        } else {
            //reset your begin variable
            begin = 0;
            sleep = false;
        }

//        public static long startTimer(){
//            if (begin==0) {
//                long begin = System.currentTimeMillis();
//                return begin;
//            }
//        }

//        public static long pauseTimer(){
//            long pause = System.currentTimeMillis();
//            long duration = begin - pause;
//            return duration;
//        }

        Log.d(TAG, "Eyes closed time: "+ "begin" + begin + "current" + System.currentTimeMillis());
        if(sleep && System.currentTimeMillis()-begin>500){
            Log.d(TAG, "Show alert");
            alertBox();

            begin = 0;
            flag=1;
        }
    }

//    private void saveData(){
//        // [START single_value_read]
//        final String userId = getUid();
//        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
//                new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        // Get user value
//                        User user = dataSnapshot.getValue(User.class);
//
//                        // [START_EXCLUDE]
//                        if (user == null) {
//                            // User is null, error out
//                            Log.e(TAG, "User " + userId + " is unexpectedly null");
//                            Toast.makeText(NewPostActivity.this,
//                                    "Error: could not fetch user.",
//                                    Toast.LENGTH_SHORT).show();
//                        } else {
//                            // Write new post
//                            writeNewPost(userId, user.username, title, body);
//                        }
//
//                        // Finish this Activity, back to the stream
//                        setEditingEnabled(true);
//                        finish();
//                        // [END_EXCLUDE]
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
//                        // [START_EXCLUDE]
//                        setEditingEnabled(true);
//                        // [END_EXCLUDE]
//                    }
//                });
//        // [END single_value_read]
//    }

    private void playMedia(){
        stopPlaying();
        mediaPlayer = MediaPlayer.create(context, R.raw.alarm);
        mediaPlayer.start(); // no need to call prepare(); create() does that for you
    }

    private void stopPlaying(){
        if(mediaPlayer!=null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void alertBox(){
//        handler.post(new Runnable() {
//            public void run() {
        playMedia();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Drowsy Detected. Do you want to navigate to the nearest places?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // FIRE ZE MISSILES!
                                flag=0;
                                stopPlaying();
                                Intent intent = new Intent(livePreviewActivity, LocationList.class);
//                                Uri gmmIntentUri = Uri.parse("google.navigation:q=Taronga+Zoo,+Sydney+Australia");
//                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
//                                mapIntent.setPackage("com.google.android.apps.maps");
                                livePreviewActivity.startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                stopPlaying();
                                flag=0;
                            }
                        });
                alertDialog = builder.create();
                // Create the AlertDialog object and return it
                    alertDialog.show();
                    Log.d(TAG, "alertBox: alertBox is popup");
            }
//        });
//    }


}
