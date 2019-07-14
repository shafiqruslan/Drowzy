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
import android.annotation.TargetApi;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.annotation.KeepName;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import java.time.LocalDate; // Import the LocalDateTime class
import java.time.LocalTime; // Import the DateTimeFormatter class
import java.util.Timer;
import java.util.TimerTask;

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
    private AlertDialog alertDialog;
    private AlertDialog alertDialog2;
    private MediaPlayer mediaPlayer;
    private MediaPlayer mediaPlayer2;
//    private static final int PERMISSION_REQUESTS = 1;

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]

    //Timer
    private static final long START_TIME_IN_MILLIS = 60000;
    private TextView mTextViewCountDown;

    private CountDownTimer mCountDownTimer;

//    private boolean mTimerRunning;

    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_live_preview);
        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]
        preview = (CameraSourcePreview) findViewById(R.id.firePreview);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = (GraphicOverlay) findViewById(R.id.fireFaceOverlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        Button buttonClose = (Button) findViewById(R.id.button_close);
        buttonClose.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                closeCustomDialog();
            }
        });

        Button buttonHistory = (Button) findViewById(R.id.button_history);
        buttonHistory.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(LivePreviewActivity.this, HistoryList.class);

                startActivity(intent);
                finish();
            }
        });
        ToggleButton facingSwitch = (ToggleButton) findViewById(R.id.facingSwitch);
        facingSwitch.setOnCheckedChangeListener(this);
        // Hide the toggle button if there is only 1 camera
        if (Camera.getNumberOfCameras() == 1) {
            facingSwitch.setVisibility(View.GONE);
        }

        getCameraPermission();

        mTextViewCountDown = findViewById(R.id.text_view_countdown);

//        updateCountDownText();
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

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void submitPost() {

        final LocalDate date = LocalDate.now();
        DateTimeFormatter myFormatDate = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        final String formattedDate = date.format(myFormatDate);
        System.out.println("datez" +formattedDate);

        final LocalTime time = LocalTime.now();
        DateTimeFormatter myFormatTime = DateTimeFormatter.ofPattern("HH:mm:ss");
        final String formattedTime = time.format(myFormatTime);

        Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show();

        // [START single_value_read]
        final String userId = getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        UserContent user = dataSnapshot.getValue(UserContent.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(LivePreviewActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new post
                            writeNewPost(userId, user.username, formattedTime,formattedDate);
                        }

                        // Finish this Activity, back to the stream
//                        finish();
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());

                    }
                });
        // [END single_value_read]
    }

    // [START write_fan_out]
    private void writeNewPost(String userId, String username, String time, String date) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("sleeps").push().getKey();
        SleepContent sleep = new SleepContent(userId, username,time,date);

        Map<String, Object> sleepValues = sleep.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/sleeps/" + key, sleepValues);
        childUpdates.put("/user-sleeps/" + userId + "/" + key, sleepValues);
        childUpdates.put("/user-date-sleeps/" + userId + "/" + date + "/" + key , sleepValues);


        mDatabase.updateChildren(childUpdates);
    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public void determineDrowsy(){
//
//        //get user id
//        String userId = getUid();
//
//        //get date
//        LocalDate date = LocalDate.now();
//        DateTimeFormatter myFormatDate = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//        String formattedDate = date.format(myFormatDate);
//
//        //get time
//        LocalTime time = LocalTime.now();
//        LocalTime minus_30 = time.minus(Duration.ofSeconds(30));
//        DateTimeFormatter myFormatTime = DateTimeFormatter.ofPattern("HH:mm:ss");
//        final String formattedTime = time.format(myFormatTime);
//        final String minusformattedTime = minus_30.format(myFormatTime);
//
//        Query query = mDatabase.child("user-date-sleeps").child(userId).child(formattedDate).orderByChild("time").startAt(minusformattedTime).endAt(formattedTime);
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                int count=0;
//                if (dataSnapshot.exists()) {
//                    // dataSnapshot is the "issue" node with all children with id 0
//                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//                        // do something with the individual "issues"
////                     String time = postSnapshot.child("time").getValue().toString();
//                     count++;
//                    }
//                }
//                if(count > 5){
//                    showErrorDialog();
//                }
//                else{
//                    count=0;
//                }
//                Log.d(TAG, "onDataChange: count" + count);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }

    public void closeCustomDialog() {

        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_close, viewGroup, false);

        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog

                    }
                });

        //finally creating the alert dialog and displaying it
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 10;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);
    }

    public void showErrorDialog() {
        FaceDetectionProcessor.flag = 1;
        resetTimer();
        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_error, viewGroup, false);

        playMedia();
        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // FIRE ZE MISSILES!
                                FaceDetectionProcessor.flag=0;
                                stopPlaying();
                                Intent intent = new Intent(LivePreviewActivity.this, LocationList.class);
//                                Uri gmmIntentUri = Uri.parse("google.navigation:q=Taronga+Zoo,+Sydney+Australia");
//                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
//                                mapIntent.setPackage("com.google.android.apps.maps");
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                stopPlaying();
                                FaceDetectionProcessor.flag=0;
                                startTimer();
                            }
                        });

        //finally creating the alert dialog and displaying it
        alertDialog2 = builder.create();
        alertDialog2.show();
        Button btnPositive = alertDialog2.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = alertDialog2.getButton(AlertDialog.BUTTON_NEGATIVE);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 10;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void showWarningDialog() {
        if(alertDialog==null){
            //before inflating the custom alert dialog layout, we will get the current activity viewgroup
            ViewGroup viewGroup = findViewById(android.R.id.content);

            //then we will inflate the custom alert dialog xml that we created
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_warning, viewGroup, false);

            //Now we need an AlertDialog.Builder object
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
            playAlert();
            //setting the view of the builder to our custom view that we already inflated
            builder.setView(dialogView);

            //finally creating the alert dialog and displaying it
            alertDialog = builder.create();
            alertDialog.show();
        }
        FaceDetectionProcessor.flag2=1;
    }

    public void cancelDialog(){
        if(alertDialog!=null) {
            alertDialog.dismiss();
            alertDialog=null;
            FaceDetectionProcessor.flag2 = 0;
        }
    }

    private void playMedia(){
        stopPlaying();
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
        mediaPlayer.start(); // no need to call prepare(); create() does that for you
    }

    private void playAlert(){
        stopAlert();
        mediaPlayer2 = MediaPlayer.create(this, R.raw.alert);
        mediaPlayer2.start(); // no need to call prepare(); create() does that for you
    }

    public void stopPlaying(){
        if(mediaPlayer!=null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void stopAlert(){
        if(mediaPlayer2!=null) {
            mediaPlayer2.stop();
            mediaPlayer2.release();
            mediaPlayer2 = null;
        }
    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public void checkDrowsy(){
//        if(System.currentTimeMillis()-FaceDetectionProcessor.begin2>30000) {
//            determineDrowsy();
//            FaceDetectionProcessor.begin2=0;
//        }
//
//    }



//Timer

    public void startTimer() {
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
//                mTimerRunning = false;
                determineDrowsy();
                resetTimer();
                startTimer();
                FaceDetectionProcessor.count=0;
            }
        }.start();

//        mTimerRunning = true;
    }

//    private void pauseTimer() {
//        mCountDownTimer.cancel();
//        mTimerRunning = false;
//    }

    private void resetTimer() {
        mCountDownTimer.cancel();
//        mTimerRunning = false;
        mTimeLeftInMillis = START_TIME_IN_MILLIS;
        updateCountDownText();
    }

    private void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        mTextViewCountDown.setText(timeLeftFormatted);
    }

    public void submit(){
        runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                submitPost();
            }
        });
    }

    public void determineDrowsy(){
        if(FaceDetectionProcessor.count>48){
            showErrorDialog();
            cancelDialog();
            submit();
        }
    }

}