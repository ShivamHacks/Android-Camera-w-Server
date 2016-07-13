package com.example.shivamagrawal.cameratests;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;

import android.hardware.Camera;

import android.os.Bundle;
import android.util.*;
import android.view.*;
import android.widget.*;

import java.util.*;
import java.io.*;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;

import android.content.res.Configuration;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;

import android.os.*;
import android.content.Context;


import javax.net.ssl.HttpsURLConnection;

import java.net.URL;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request;
import com.android.volley.AuthFailureError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("deprecation")
public class CameraActivity extends Activity implements SurfaceHolder.Callback {

    private Camera mCamera;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    private Button finishCamera;
    private Button captureImage;
    private Button switchCameras;

    private int currrentCamID = Camera.CameraInfo.CAMERA_FACING_BACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceview);

        /*surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                return false;
            }
        });*/

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(CameraActivity.this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // Buttons

        finishCamera = (Button) findViewById(R.id.finish_camera);
        finishCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCamera();
            }
        });

        captureImage = (Button) findViewById(R.id.capture_image);
        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capture();
            }
        });

        switchCameras = (Button) findViewById(R.id.switch_cameras);
        switchCameras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCameras();
            }
        });
    }

    private void openCamera() {
        try {
            mCamera = Camera.open(currrentCamID);

            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
            mCamera.setDisplayOrientation(90);

            Camera.Parameters parameters = mCamera.getParameters();

            int width = surfaceView.getWidth();
            int height = surfaceView.getHeight();
            Camera.Size size = getOptimalPreviewSize(mCamera.getParameters().getSupportedPreviewSizes(), width, height);
            parameters.setPictureSize(size.width, size.height);

            if (parameters.getFocusMode().equals("auto")) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                mCamera.setParameters(parameters);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void finishCamera() {
        mCamera.stopPreview();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        finish();
    }

    private void switchCameras() {
        mCamera.stopPreview();
        mCamera.release();
        if (currrentCamID == Camera.CameraInfo.CAMERA_FACING_BACK) {
            currrentCamID = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            currrentCamID = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        openCamera();
    }


    private void capture() {
        mCamera.takePicture(null, null, null, new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.d("PIC", "CAPTURED");

                if (data != null) {
                    int screenWidth = getResources().getDisplayMetrics().widthPixels;
                    int screenHeight = getResources().getDisplayMetrics().heightPixels;
                    Bitmap bm = BitmapFactory.decodeByteArray(data, 0, (data != null) ? data.length : 0);
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        Bitmap scaled = Bitmap.createScaledBitmap(bm, screenHeight, screenWidth, true);
                        int w = scaled.getWidth();
                        int h = scaled.getHeight();
                        Matrix mtx = new Matrix();
                        mtx.postRotate(90);
                        bm = Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);
                    } else { // LANDSCAPE MODE
                        bm = Bitmap.createScaledBitmap(bm, screenWidth, screenHeight, true);
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 100, baos); // quality doesn't matter b/c PNG is lossless
                    byte[] imageBytes = baos.toByteArray();
                    String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                    //Log.d("IMG", encodedImage);
                    //reqGet(encodedImage);
                    //sendJSON(encodedImage);
                    sendImage(encodedImage);

                    MediaStore.Images.Media.insertImage(getContentResolver(), bm, "LOL", "LOLOL"); // store in image gallery
                }

                mCamera.startPreview();
            }
        });
    }

    private void reqGet(final String image) {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest sr = new StringRequest(Request.Method.POST,
                "http://10.0.0.11:3000/api/upload",
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("INTERNET", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("INTERNET", error.getMessage());
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("image", image);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(sr);
    }

    private void sendImage(String image) {
        RequestQueue queue = Volley.newRequestQueue(this);
        Map<String, String> params = new HashMap<String, String>();
        params.put("image", image);
        queue.add(Server.POST(params));
    }





















    // TODO: better buttons, image rotation etc.

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        openCamera();
    }

    // HELPER METHODS

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void setCameraDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(currrentCamID, info);
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(result);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("Surface Created", "");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("Surface Destroyed", "");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
    }
}

/*
StackOverflow answers:
- http://stackoverflow.com/questions/9540760/can-the-front-camera-use-autofocus
- http://stackoverflow.com/questions/16765527/android-switch-camera-when-button-clicked
- http://stackoverflow.com/questions/16297606/android-take-camera-picture-without-save-delete-confirmation
- http://stackoverflow.com/questions/19577299/android-camera-preview-stretched
- http://stackoverflow.com/questions/12796579/how-to-send-image-bitmap-to-server-in-android-with-multipart-form-data-json
 */