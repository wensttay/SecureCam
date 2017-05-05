package com.example.wensttay.securecam_project;


import java.io.IOException;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WebCamActivity extends AppCompatActivity {
    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mediaRecorder;
    private TextView capture, switchCamera;
    private Context myContext;
    private LinearLayout cameraPreview;
    private String outputFileName;

    boolean recording = false;
    private boolean cameraFront = false;

    private OnClickListener captrureListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            startOrStop();
        }
    };

    private void startOrStop(){
        if (recording) {
            stopAndSaveCapture();
        } else {
            startCapture();
        }
    }

    private void stopAndSaveCapture(){
        // stop recording and release camera
        mediaRecorder.stop(); // stop the recording
        releaseMediaRecorder(); // release the MediaRecorder object
        Toast.makeText(WebCamActivity.this, "Video captured!", Toast.LENGTH_LONG).show();
        recording = false;
    }

    private void startCapture(){

            if (!prepareMediaRecorder()) {
                Toast.makeText(WebCamActivity.this, "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
                finish();
            }
            // work on UiThread for better performance
            runOnUiThread(new Runnable() {
                public void run() {
                    // If there are stories, add them to the table
                    try {
                        Toast.makeText(WebCamActivity.this, "Started Capture!", Toast.LENGTH_LONG).show();
                        mediaRecorder.start();
                        if (outputFileName != null) {
                            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{outputFileName}, null, null);
                        }
                    } catch (final Exception ex) {
                        // Log.i("---","Exception in thread");
                    }
                }
            });

            recording = true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                if(recording) stopAndSaveCapture();
                finish();
        }
        return (super.onOptionsItemSelected(menuItem));
    }


//    private OrientationEventListener mOrientationEventListener;
//    private int mDeviceOrientation = 0;
//    private OnClickListener switchCameraListener = new OnClickListener() {
//    @Override
//    public void onClick(View v) {
//        // get the number of cameras
//        if (!recording) {
//            int camerasNumber = Camera.getNumberOfCameras();
//            if (camerasNumber > 1) {
//                // release the old camera instance
//                // switch camera, from the front and the back and vice versa
//                releaseCamera();
//                chooseCamera();
//            } else {
//                Toast toast = Toast.makeText(myContext, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
//                toast.show();
//            }
//        }
//    }
//};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_video_capture_example);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTitle("Web Cam");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        myContext = this;
        initialize();

//        mOrientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
//            @Override
//            public void onOrientationChanged(int orientation) {
//                mDeviceOrientation = orientation;
//            }
//        };
//
//        if(mOrientationEventListener.canDetectOrientation()) {
//            mOrientationEventListener.enable();
//        }
    }

    public void initialize() {
        cameraPreview = (LinearLayout) findViewById(R.id.camera_preview);

        mPreview = new CameraPreview(myContext, mCamera);
        cameraPreview.addView(mPreview);

        capture = (TextView) findViewById(R.id.button_capture);
        capture.setOnClickListener(captrureListener);

//        switchCamera = (Button) findViewById(R.id.button_ChangeCamera);
//        switchCamera.setOnClickListener(switchCameraListener);
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        // Search for the back facing camera
        // get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        // for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }

    public void onResume() {
        super.onResume();
        if (!hasCamera(myContext)) {
            Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (mCamera == null) {
            // if the front facing camera does not exist
            if (findFrontFacingCamera() < 0) {
                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
                switchCamera.setVisibility(View.GONE);
            }
            mCamera = Camera.open(findBackFacingCamera());
            mCamera.setDisplayOrientation(90);
            mPreview.refreshCamera(mCamera);
        }
    }

//    public void onResume(int degrees) {
//        super.onResume();
//        if (!hasCamera(myContext)) {
//            Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
//            toast.show();
//            finish();
//        }
//        if (mCamera == null) {
//            // if the front facing camera does not exist
//            if (findFrontFacingCamera() < 0) {
//                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
//                switchCamera.setVisibility(View.GONE);
//            }
//            mCamera = Camera.open(findBackFacingCamera());
//            mCamera.setDisplayOrientation(degrees);
//            mPreview.refreshCamera(mCamera);
//        }
//    }

//    public void chooseCamera() {
//        // if the camera preview is the front
//        if (cameraFront) {
//            int cameraId = findBackFacingCamera();
//            if (cameraId >= 0) {
//                // open the backFacingCamera
//                // set a picture callback
//                // refresh the preview
//
//                mCamera = Camera.open(cameraId);
//                // mPicture = getPictureCallback();
//                mPreview.refreshCamera(mCamera);
//            }
//        } else {
//            int cameraId = findFrontFacingCamera();
//            if (cameraId >= 0) {
//                // open the backFacingCamera
//                // set a picture callback
//                // refresh the preview
//
//                mCamera = Camera.open(cameraId);
//                // mPicture = getPictureCallback();
//                mPreview.refreshCamera(mCamera);
//            }
//        }
//    }

    @Override
    protected void onPause() {
        super.onPause();
        // when on Pause, release camera in order to be used from other
        // applications
        releaseCamera();
    }

    private boolean hasCamera(Context context) {
        // check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset(); // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            mCamera.lock(); // lock camera for later use
        }
    }

    private boolean prepareMediaRecorder() {

        mediaRecorder = new MediaRecorder();

        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);
//        mediaRecorder.setOrientationHint(90);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
        outputFileName = Environment.getExternalStorageDirectory()
                + "/" + Environment.DIRECTORY_DCIM
                + "/SecureCAM/CAM "+System.currentTimeMillis()+".mp4";
        mediaRecorder.setOutputFile(outputFileName);
        mediaRecorder.setMaxDuration(60000); // Set max duration 60 sec.
//        mediaRecorder.setMaxFileSize(200000000); // Set max file size 50M
        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    Toast.makeText(WebCamActivity.this, "Time limit exceeded (1m)!", Toast.LENGTH_LONG).show();
                    startOrStop();
                }
            }
        });

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            return false;
        }
        return true;

    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//
//        // Checks the orientation of the screen
//        if(!recording){
//
//            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                releaseCamera();
//                if (mDeviceOrientation > 200){
//                    onResume(0);
//                }else{
//                    onResume(180);
//                }
//            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//                releaseCamera();
//                onResume(90);
//            }
//
//            System.out.println("OTHER: " + mDeviceOrientation);
//        }
//    }

}