package com.rndtechnosoft.fynder.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.utility.MyImageUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Ravi on 12/29/2016.
 */

public class CameraActivity extends AppCompatActivity {
    private final String TAG = "CameraActivity";
    private ImageView imageFlash;
    private ImageView imageSwitchCamera;
    private Camera mCamera;
    SurfaceHolder surfaceHolder;
    SurfaceHolder.Callback surfaceCallback;
    private boolean flashed = false;
    private boolean frontCamera = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "On Create");
        setContentView(R.layout.activity_camera);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        View viewFlash = findViewById(R.id.view_flash);
        View viewSwitchImage = findViewById(R.id.view_switch_camera);
        imageFlash = (ImageView) findViewById(R.id.button_flash);
        imageSwitchCamera = (ImageView) findViewById(R.id.button_switch_camera);

        if (surfaceHolder == null) {
            surfaceHolder = surfaceView.getHolder();
//            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        surfaceCallback = my_callback();

        boolean isFlashAvailable = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
//        frontCamera = getFrontCameraId() != -1;
        viewFlash.setVisibility(isFlashAvailable ? View.VISIBLE : View.GONE);
        viewSwitchImage.setVisibility(getFrontCameraId() != -1 ? View.VISIBLE : View.GONE);
        int resSwitchCam = frontCamera ? R.drawable.ic_camera_rear : R.drawable.ic_camera_front;
        imageSwitchCamera.setImageResource(resSwitchCam);
        findViewById(R.id.view_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        Log.i(TAG, "On Picture Taken..");
                        try {
                            Bitmap bm = BitmapFactory.decodeByteArray(data, 0, (data != null) ? data.length : 0);
                            int size = getResources().getInteger(R.integer.camera_image_size);
                            bm = MyImageUtil.scaleBitmap(bm, size, size);
                            Matrix mtx = new Matrix();
                            if (frontCamera) {
                                Log.i(TAG, "front camera");
                                mtx.postRotate(-90);

                            } else {
                                Log.i(TAG, "back camera");
                                mtx.postRotate(90);
                            }
                            bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), mtx, true);
                            File fileOutput = new File(getCacheDir(), getString(R.string.app_name) + ".jpg");
                            FileOutputStream fos = new FileOutputStream(fileOutput);

                            bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//                            fos.write(data);

                            Intent returnIntent = new Intent();
                            returnIntent.setData(Uri.fromFile(fileOutput));
                            setResult(RESULT_OK, returnIntent);
                            fos.flush();
                            fos.close();
                            Intent intent = new Intent(CameraActivity.this, PreviewImageActivity.class);
                            intent.putExtra(PreviewImageActivity.KEY_URI, Uri.fromFile(fileOutput).toString());
                            intent.putExtra(PreviewImageActivity.KEY_IS_FROM_CAMERA, true);
                            startActivityForResult(intent, 100);
                        } catch (FileNotFoundException e) {
                            Log.e(TAG, "File not found: " + e.getMessage());
                            setResult(RESULT_CANCELED);
                        } catch (IOException e) {
                            Log.e(TAG, "IO error: " + e.getMessage());
                            setResult(RESULT_CANCELED);
                        }
//                        finish();
                    }
                });
            }
        });
        viewFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (frontCamera) {
                    Toast.makeText(CameraActivity.this, getString(R.string.alert_error_flash), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    Toast.makeText(CameraActivity.this, getString(R.string.alert_error_flash_not_found), Toast.LENGTH_SHORT).show();
                    return;
                }
                flashed = !flashed;
                int resId = flashed ? R.drawable.ic_flash_off : R.drawable.ic_flash_on;
                imageFlash.setImageResource(resId);
                Camera.Parameters p = mCamera.getParameters();
                p.setFlashMode(flashed ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(p);
            }
        });
        viewSwitchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getFrontCameraId() == -1) {
                    Toast.makeText(CameraActivity.this, getString(R.string.alert_front_camera_undetected), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (flashed) {
                    Toast.makeText(CameraActivity.this, getString(R.string.alert_error_flash), Toast.LENGTH_SHORT).show();
                }
                flashed = false;
                frontCamera = !frontCamera;
                imageFlash.setImageResource(R.drawable.ic_flash_on);
//                Camera.Parameters p = mCamera.getParameters();
//                p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
//                mCamera.setParameters(p);
                mCamera.release();
                try {
                    int cameraId;
                    if (frontCamera) {
                        cameraId = getFrontCameraId();
                    } else {
                        cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                    }
//                        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                    mCamera = Camera.open(cameraId);
                    mCamera.setPreviewDisplay(surfaceHolder);
//                    if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
//                        mCamera.setDisplayOrientation(90);
//                    } else {
//                        mCamera.setDisplayOrientation(0);
//                    }
//                    setCameraDisplayOrientation(cameraId, mCamera);
                    mCamera.setDisplayOrientation(getDegreesOrientation(cameraId));
                    mCamera.startPreview();
                    int resId = frontCamera ? R.drawable.ic_camera_rear : R.drawable.ic_camera_front;
                    imageSwitchCamera.setImageResource(resId);
                } catch (IOException exception) {
                    Log.e(TAG, "Error switch camera: " + exception.getMessage());
                    mCamera.release();
                    mCamera = null;
                }

            }
        });
    }

    SurfaceHolder.Callback my_callback() {
        SurfaceHolder.Callback ob1 = new SurfaceHolder.Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.i(TAG, "Surface destroyed");
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                int cameraId = getFrontCameraId();
                try {
//                    if (cameraId != -1) {
//                        mCamera = Camera.open(cameraId);
//                    } else {
                    mCamera = Camera.open();
//                    }
//                    setCameraDisplayOrientation(cameraId, mCamera);
                    Camera.Parameters params = mCamera.getParameters();
                    params.setPictureFormat(ImageFormat.JPEG);
                    params.setJpegQuality(100);
                    List<Camera.Size> sizes = params.getSupportedPictureSizes();
                    Camera.Size size = sizes.get(0);
                    for (int i = 0; i < sizes.size(); i++) {
                        if (sizes.get(i).width > size.width)
                            size = sizes.get(i);
                    }
                    params.setPictureSize(size.width, size.height);
                    mCamera.setParameters(params);
                    int orientation = getDegreesOrientation(cameraId);
                    Log.i(TAG, "Orientation: " + orientation);
                    mCamera.setDisplayOrientation(orientation);
                    mCamera.setPreviewDisplay(holder);
                } catch (IOException exception) {
                    mCamera.release();
                    mCamera = null;
                } catch (Exception e) {
                    Toast.makeText(CameraActivity.this, getString(R.string.error_camera), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                       int height) {
                // make any resize, rotate or reformatting changes here
//                if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
//                    mCamera.setDisplayOrientation(90);
//                } else {
//                    mCamera.setDisplayOrientation(0);
//                }
                try {
                    mCamera.startPreview();
                } catch (Exception e) {
                    Toast.makeText(CameraActivity.this, getString(R.string.error_camera), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        };
        return ob1;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private int getFrontCameraId() {
        if (Build.VERSION.SDK_INT < 22) {
            Camera.CameraInfo ci = new Camera.CameraInfo();
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, ci);
                if (ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) return i;
            }
        } else {
            try {
                CameraManager cManager = (CameraManager) getApplicationContext()
                        .getSystemService(Context.CAMERA_SERVICE);
                for (int j = 0; j < cManager.getCameraIdList().length; j++) {
                    String[] cameraId = cManager.getCameraIdList();
                    CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId[j]);
                    int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (cOrientation == CameraCharacteristics.LENS_FACING_FRONT)
                        return j;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return -1; // No front-facing camera found
    }

//    Bitmap rotateImage(Bitmap source, float angle) {
//        Matrix matrix = new Matrix();
//        matrix.postRotate(angle);
//        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
//                matrix, true);
//    }

    private int getDegreesOrientation(int cameraId) {
        try {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, info);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
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
                result = (360 - result) % 360;  // compensate the mirror
            } else {  // back-facing
                result = (info.orientation - degrees + 360) % 360;
            }
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error get orientation: " + e.getMessage());
            return 90;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "On Start");
        surfaceHolder.addCallback(surfaceCallback);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "On Stop");
        surfaceHolder.removeCallback(surfaceCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "On Activity Result => request code: " + requestCode + " , result code: " + resultCode);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            final String path = data.getStringExtra(PreviewImageActivity.KEY_RESULT_PATH);
            Intent intent = new Intent();
            intent.putExtra(PreviewImageActivity.KEY_RESULT_PATH, path);
            setResult(RESULT_OK, intent);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    MediaScannerConnection.scanFile(CameraActivity.this,
                            new String[]{path}, new String[]{"image/jpeg"}, null);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    finish();
                }
            }.execute();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}
