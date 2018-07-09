package com.rndtechnosoft.fynder.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.utility.MyImageUtil;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Ravi on 2/11/2017.
 */

public class PreviewImageActivity extends AppCompatActivity {
    public static final String KEY_URI = PreviewImageActivity.class.getName() + ".KEY_URI";
    public static final String KEY_RESULT_PATH = PreviewImageActivity.class.getName() + ".KEY_RESULT_PATH";
    public static final String KEY_IS_FROM_CAMERA = PreviewImageActivity.class.getName() + ".KEY_IS_FROM_CAMERA";
    public static final String KEY_IS_SQUARE = PreviewImageActivity.class.getName() + ".KEY_IS_SQUARE";
    private final String TAG = "PreviewImage";
    private File desFileDir;
    private String resultPath;
    private String uriString;
    private boolean isCropped;
    private boolean isFromCamera;
    private boolean isSquare;
    private SimpleDraweeView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preview_image);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        desFileDir = new File(getCacheDir(), getString(R.string.app_name)
                + "_" + System.currentTimeMillis() + ".jpg");

        resultPath = desFileDir.getAbsolutePath();

        imageView = (SimpleDraweeView) findViewById(R.id.imageView);
        Intent intent = getIntent();
        uriString = intent.getStringExtra(KEY_URI);
        isFromCamera = intent.getBooleanExtra(KEY_IS_FROM_CAMERA, false);
        isSquare = intent.getBooleanExtra(KEY_IS_SQUARE, false);
        if (uriString == null) {
            Log.e(TAG, "Error => URI is empty");
            finish();
            return;
        }

        try {
            ExifInterface exif = new ExifInterface(Uri.parse(uriString).getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            Log.i(TAG, "Orientation: " + orientation);
        } catch (IOException e) {
            Log.e(TAG, "Error get orientation: " + e.getMessage());
        }
        View viewRetake = findViewById(R.id.retake_image);
        viewRetake.setVisibility(isFromCamera ? View.VISIBLE : View.GONE);
        viewRetake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                deleteCameraFile();
                startActivity(new Intent(PreviewImageActivity.this, CameraActivity.class));
            }
        });

        findViewById(R.id.cancel_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFromCamera) {
                    deleteCameraFile();
                }
                finish();
            }
        });

        findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncTask<Void, Void, Void>() {
                    private ProgressDialog progressDialog;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progressDialog = new ProgressDialog(PreviewImageActivity.this);
                        progressDialog.setMessage(getString(R.string.loading_message));
                        progressDialog.show();
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            Uri uri = isCropped ? Uri.fromFile(new File(resultPath)) : Uri.parse(uriString);
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(PreviewImageActivity.this.getContentResolver()
                                    , uri);
                            String stringPath = isCropped ? new File(resultPath).getPath() : Uri.parse(uriString).getPath();
                            ExifInterface exif = new ExifInterface(stringPath);
                            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                    ExifInterface.ORIENTATION_NORMAL);
                            Log.i(TAG, "Orientation: " + orientation);
                            Matrix matrix = new Matrix();
                            matrix.postRotate(exifToDegrees(orientation));
                            int size = getResources().getInteger(R.integer.camera_image_size);
                            Bitmap resizedBitmap = MyImageUtil.scaleBitmap(bitmap, size, size);
                            resizedBitmap = Bitmap.createBitmap(resizedBitmap, 0, 0
                                    , resizedBitmap.getWidth(), resizedBitmap.getHeight(), matrix, true);
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                            byte[] bitmapdata = bos.toByteArray();
                            //write the bytes in file
                            FileOutputStream fos = new FileOutputStream(desFileDir);
                            fos.write(bitmapdata);
                            fos.flush();
                            fos.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Error resizing original image size: " + e.getMessage());
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        progressDialog.dismiss();
                        Intent intent = new Intent();
                        intent.putExtra(KEY_RESULT_PATH, resultPath);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }.execute();

            }
        });

        findViewById(R.id.crop_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UCrop.Options options = new UCrop.Options();
                options.setToolbarColor(ContextCompat.getColor(PreviewImageActivity.this, android.R.color.black));
                options.setStatusBarColor(ContextCompat.getColor(PreviewImageActivity.this, android.R.color.black));
                Uri desUri = Uri.fromFile(desFileDir);
                Uri uri = Uri.parse(uriString);
                UCrop uCrop = UCrop.of(uri, desUri)
                        .withOptions(options);
                if (isSquare) {
                    uCrop.withAspectRatio(1, 1);
                }
                uCrop.start(PreviewImageActivity.this);
            }
        });
        showImagePreview();
    }

    @Override
    public void onBackPressed() {
        if (isFromCamera) {
            deleteCameraFile();
        }
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void deleteCameraFile() {
        File file = new File(uriString);
        boolean isDeleted = file.delete();
        Log.e(TAG, "File has deleted: " + isDeleted);
    }

    private void showImagePreview() {
        try {
            Uri uri = Uri.parse(uriString);
            imageView.setImageURI(uri);
        } catch (Exception e) {
            Log.e(TAG, "Error set image: " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            uriString = UCrop.getOutput(data).toString();
            showImagePreview();
            isCropped = true;
        }
    }

    private int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

}
