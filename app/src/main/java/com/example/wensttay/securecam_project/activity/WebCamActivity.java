package com.example.wensttay.securecam_project.activity;


import java.io.File;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wensttay.securecam_project.RegisterCodeProvider;
import com.example.wensttay.securecam_project.WebCamSurfaceView;
import com.example.wensttay.securecam_project.R;

public class WebCamActivity extends AppCompatActivity {

    private Camera mCamera;
    private WebCamSurfaceView mPreview;
    private MediaRecorder mediaRecorder;
    private TextView codeTextView;
    private Context myContext;
    private LinearLayout cameraPreview;
    private String outputFileName;
    private boolean recording = false;
    private FloatingActionButton flashButton;
    private OnClickListener captrureListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            startOrStop();
        }
    };
    private OnClickListener flashListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(hasFlash()){
                Camera.Parameters p = mCamera.getParameters();
                String flashMode = p.getFlashMode();
                System.out.println("Tem Flash!: " + flashMode);

                if(flashMode.equals(Camera.Parameters.FLASH_MODE_OFF)){
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                }else{
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }
                mCamera.setParameters(p);
                mCamera.startPreview();
            }
        }
    };
//    private TextView switchCamera;
//    private boolean cameraFront = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web_cam);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTitle("Web Cam");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myContext = this;

//        button_capture

        initialize();
        String camCode = getIntent().getExtras().getString(RegisterCodeProvider.CAM_PREFERENCES_CODE_TAG);
        codeTextView.setText(camCode);
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


    @Override
    public void onResume() {
        super.onResume();

        if (!hasCamera(myContext)) {
            Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (mCamera == null) {
            // if the front facing camera does not exist
//            if (findFrontFacingCamera() < 0) {
//                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
//                switchCamera.setVisibility(View.GONE);
//            }
            mCamera = Camera.open(findBackFacingCamera());

            if(!hasFlash()){
                flashButton.setVisibility(View.INVISIBLE);
            }

            mCamera.setDisplayOrientation(90);
            mPreview.refreshCamera(mCamera);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // when on Pause, release camera in order to be used from other
        // applications
        releaseCamera();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                if (recording) stopAndSaveCapture();
                finish();
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    public void initialize() {
        cameraPreview = (LinearLayout) findViewById(R.id.camera_preview);

        mPreview = new WebCamSurfaceView(myContext, mCamera);
        cameraPreview.addView(mPreview);

        codeTextView = (TextView) findViewById(R.id.button_capture);
        codeTextView.setOnClickListener(captrureListener);


        flashButton = (FloatingActionButton) findViewById(R.id.flashImageIcon);
        flashButton.setOnClickListener(flashListener);

//        switchCamera = (Button) findViewById(R.id.button_ChangeCamera);
//        switchCamera.setOnClickListener(switchCameraListener);
    }

    private void startOrStop() {

        if (recording) {
            stopAndSaveCapture();
        } else {
            startCapture();
        }

    }

    private void stopAndSaveCapture() {
        // stop recording and release camera
        mediaRecorder.stop(); // stop the recording
        releaseMediaRecorder(); // release the MediaRecorder object
        mPreview.refreshCamera(mCamera);
        Toast.makeText(WebCamActivity.this, "Video captured!", Toast.LENGTH_LONG).show();
        recording = false;
    }

    private void startCapture() {

        if (!prepareMediaRecorder()) {
            Toast.makeText(WebCamActivity.this, "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
            finish();
        }

        // work on UiThread for better performance
        runOnUiThread(new Runnable() {
            public void run() {
                // If there are stories, add them to the table
                try {
                    mediaRecorder.start();

                    if (outputFileName != null) {
                        MediaScannerConnection.scanFile(getApplicationContext(), new String[]{outputFileName}, null, null);
                    }

                    Toast.makeText(WebCamActivity.this, "Started Capture!", Toast.LENGTH_LONG).show();
                } catch (final Exception ex) {
                    // Log.i("---","Exception in thread");
                }
            }
        });

        recording = true;
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset(); // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            mCamera.lock(); // lock camera for later use
        }
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
//                cameraFront = false;
                break;
            }
        }

        if (numberOfCameras > 0 && cameraId == -1) {
            cameraId = 1;
//            cameraFront = true;
        }

        return cameraId;
    }

    private boolean hasCamera(Context context) {
        // check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasFlash() {
        if (mCamera == null) {
            return false;
        }

        Camera.Parameters parameters = mCamera.getParameters();

        if (parameters.getFlashMode() == null) {
            return false;
        }

        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if (supportedFlashModes == null
                || supportedFlashModes.isEmpty()
                || supportedFlashModes.size() == 1
                && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF)) {
            return false;
        }

        return true;
    }

    private boolean prepareMediaRecorder() {

        mediaRecorder = new MediaRecorder();

        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);
//        mediaRecorder.setOrientationHint(90);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
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

        // USAR CAM CODE AQUI
        outputFileName = generateOutputFileName("CAM XSO299-09");
        mediaRecorder.setOutputFile(outputFileName);

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException | IOException e) {
            releaseMediaRecorder();
            return false;
        }

        return true;
    }

    private String generateOutputFileName(String camCode) {
        String pathAux = Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_DCIM
                + File.separator + getResources().getString(R.string.app_album_file);;

        File aux = new File(pathAux);
        if (!aux.exists()) {
            aux.mkdir();
        }
        pathAux += File.separator + camCode + "-" + System.currentTimeMillis() + ".mp4";
        return pathAux;
    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.release();
            mCamera = null;
        }
    }


//    private int findFrontFacingCamera() {
//        int cameraId = -1;
//        // Search for the front facing camera
//        int numberOfCameras = Camera.getNumberOfCameras();
//        for (int i = 0; i < numberOfCameras; i++) {
//            CameraInfo info = new CameraInfo();
//            Camera.getCameraInfo(i, info);
//            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
//                cameraId = i;
//                cameraFront = true;
//                break;
//            }
//        }
//        return cameraId;
//    }

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