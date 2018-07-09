package com.rndtechnosoft.fynder.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.utility.image.zoomable.ZoomableDraweeViewSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ravi on 1/23/2017.
 */

public class FullChatImageActivity extends AppCompatActivity {
    private final String TAG = "FullChatImage";
    private final int OPEN_IMAGE = 532;
    private ShareActionProvider mShareActionProvider;
    private List<String> storedPath = new ArrayList<>();
    public static final String KEY_IMAGE_LOW_RES = FullChatImageActivity.class.getName() + ".KEY_IMAGE_LOW_RES";
    public static final String KEY_IMAGE_HIGH_RES = FullChatImageActivity.class.getName() + ".KEY_IMAGE_HIGH_RES";
    private Bitmap bitmap;
    private ImageRequest imageRequest;
    private MenuItem menuShare;
    private MenuItem menuShare2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "OnCreate");
        setContentView(R.layout.activity_full_chat_image);
        Intent intent = getIntent();
        String lowRes = intent.getStringExtra(KEY_IMAGE_LOW_RES);
        String highRes = intent.getStringExtra(KEY_IMAGE_HIGH_RES);
        Log.i(TAG, "Image low res: " + lowRes + " , High res: " + highRes);
        ZoomableDraweeViewSupport imageView = (ZoomableDraweeViewSupport) findViewById(R.id.iv_photo);
        imageRequest = ImageRequest.fromUri(highRes);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setLowResImageRequest(ImageRequest.fromUri(lowRes))
                .setImageRequest(imageRequest)
                .setOldController(imageView.getController())
                .build();

        imageView.setController(controller);


//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.i(TAG, "OnCreateOptionMenu");
        getMenuInflater().inflate(R.menu.menu_share_image, menu);
        menuShare = menu.findItem(R.id.action_share);
        menuShare2 = menu.findItem(R.id.action_share2);
        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuShare);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED)) {
                menuShare.setVisible(false);
                menuShare2.setVisible(true);
            }
        }


        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>>
                dataSource = imagePipeline.fetchDecodedImage(imageRequest, this);

        dataSource.subscribe(new BaseBitmapDataSubscriber() {
            @Override
            public void onNewResultImpl(@Nullable final Bitmap frescoBitmap) {
                // You can use the bitmap in only limited ways
                // No need to do any cleanup.

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if ((ContextCompat.checkSelfPermission(FullChatImageActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) ||
                            (ContextCompat.checkSelfPermission(FullChatImageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED)) {
                        bitmap = frescoBitmap;
                        return;
                    }
                }
                Log.i(TAG, "Download -> FINISH = bitmap is null: " + (frescoBitmap == null));
                FullChatImageActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        shareBitmap(frescoBitmap);
                    }
                });

            }

            @Override
            public void onFailureImpl(DataSource dataSource) {
                // No cleanup required here.
                Log.e(TAG, "Download -> Error get bitmap: " + dataSource.getFailureCause().getMessage());
            }
        }, CallerThreadExecutor.getInstance());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share2) {
            Log.i(TAG, "Menu share clicked..!!");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) ||
                        (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED)) {
                    requestPermissions(new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, OPEN_IMAGE);
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == OPEN_IMAGE) {
            if (grantResults.length < 1) {
                return;
            }
            boolean permission1 = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean permission2 = grantResults[1] == PackageManager.PERMISSION_GRANTED;
            boolean isPermissionGranted = permission1 && permission2;
            if (isPermissionGranted && bitmap != null) {
                shareBitmap(bitmap);
                menuShare.setVisible(true);
                menuShare2.setVisible(false);
            }
        }
    }

    private void shareBitmap(Bitmap bitmap) {
        String path = MediaStore.Images.Media.insertImage(this.getContentResolver(),
                bitmap, "Image Description", null);
        Uri bmpUri = Uri.parse(path);
        storedPath.add(path);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        shareIntent.setType("image/*");
        mShareActionProvider.setShareIntent(shareIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"On Destroy");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED)) {
                return;
            }
        }
        for (String path : storedPath) {
            File file = new File(path);
            Log.i(TAG,"File is exist: "+file.exists());
            boolean isDeleted = file.delete();
            Log.i(TAG,"Is deleted: "+isDeleted);
        }
    }
}
