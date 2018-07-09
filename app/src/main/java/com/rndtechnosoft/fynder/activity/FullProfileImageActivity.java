package com.rndtechnosoft.fynder.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.adapter.GalleryAdapter;
import com.rndtechnosoft.fynder.model.UserImage;
import com.rndtechnosoft.fynder.utility.image.zoomable.ZoomableDraweeViewSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullProfileImageActivity extends AppCompatActivity {
    private final String TAG = "FullImage";
    private final int OPEN_IMAGE = 532;
    public static final String KEY_IMAGE = FullProfileImageActivity.class.getName() + ".KEY_IMAGE";
    public static final String KEY_UID = FullProfileImageActivity.class.getName() + ".KEY_UID";
    public static final String KEY_IMAGE_URL = FullProfileImageActivity.class.getName() + ".KEY_IMAGE_URL";

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
//    private static final boolean AUTO_HIDE = true;

    private static final int AUTO_HIDE_DELAY_MILLIS = 30000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    //        private View mContentView;
    private ViewPager pager;
    private FullScreenImageAdapter adapter;
    private List<UserImage> userImageList = new ArrayList<>();
    private List<String> storedPath = new ArrayList<>();
    private DatabaseReference imageRef;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            pager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
//    private View mControlsView;
    private RecyclerView recyclerView;
    private GalleryAdapter galleryAdapter;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
//            mControlsView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    };
//    private boolean mVisible;
    private String keyImageSelected;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private Bitmap bitmap;
    private ImageRequest imageRequest;
    private MenuItem menuShare;
    private MenuItem menuShare2;
    private ShareActionProvider mShareActionProvider;

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
//    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View view, MotionEvent motionEvent) {
//            if (AUTO_HIDE) {
//                delayedHide(AUTO_HIDE_DELAY_MILLIS);
//            }
//            return false;
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"On Create");
        Intent intent = getIntent();
        keyImageSelected = intent.getStringExtra(KEY_IMAGE);
        String imageUrl = intent.getStringExtra(KEY_IMAGE_URL);
        String uid = intent.getStringExtra(KEY_UID);

        if (keyImageSelected == null || uid == null) {
            finish();
            return;
        }
        imageRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("images");

        setContentView(R.layout.activity_full_profile_image);

        imageRequest = ImageRequest.fromUri(imageUrl);
//        mVisible = true;
//        mControlsView = findViewById(R.id.fullscreen_content_controls);
//        mContentView = findViewById(R.id.fullscreen_content);
        recyclerView = (RecyclerView) findViewById(R.id.list_preview_gallery);
        galleryAdapter = new GalleryAdapter(userImageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(galleryAdapter);
        galleryAdapter.setOnItemClickListener(new GalleryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(UserImage item) {
                Log.i(TAG,"Gallery on click id: "+item.getId());
                int position = 0;
                for(UserImage userImage: userImageList){
                    if(userImage.getId().equals(item.getId())) {
                        pager.setCurrentItem(position);
                        break;
                    }
                    position++;
                }
            }
        });
        pager = (ViewPager) findViewById(R.id.pager);
        adapter = new FullScreenImageAdapter(userImageList);
        pager.setAdapter(adapter);

        // Set up the user interaction to manually show or hide the system UI.
//        ((View) pager).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.i(TAG, "Pager on clicked");
//                toggle();
//            }
//        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
//        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

//    private void toggle() {
//        if (mVisible) {
//            hide();
//        } else {
//            show();
//        }
//    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
//        mControlsView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
//        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
//        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        pager.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
//        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
        delayedHide(AUTO_HIDE_DELAY_MILLIS);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    //Do not call onCreate on Parent Activity
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (keyImageSelected == null || imageRef == null) {
            finish();
            return;
        }
        pager.addOnPageChangeListener(pageChangeListener);
        imageRef.addValueEventListener(imageListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        pager.removeOnPageChangeListener(pageChangeListener);
        if (imageRef != null) {
            imageRef.removeEventListener(imageListener);
        }
    }

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            int i =0;
            for(UserImage userImage:userImageList){
                userImage.setSelected(position == i);
                i++;
            }
            galleryAdapter.notifyDataSetChanged();
            delayedHide(AUTO_HIDE_DELAY_MILLIS);
        }

        @Override
        public void onPageSelected(int position) {
            UserImage userImage = userImageList.get(position);
            Log.d(TAG, "Page selected: " + position + " => " + userImage.getThumbPic());

            ImageRequest imageRequest = ImageRequest.fromUri(userImage.getOriginalPic());
            ImagePipeline imagePipeline = Fresco.getImagePipeline();
            DataSource<CloseableReference<CloseableImage>>
                    dataSource = imagePipeline.fetchDecodedImage(imageRequest, this);

            if(menuShare != null && menuShare2 != null) {
                menuShare.setVisible(false);
                menuShare2.setVisible(true);
            }

            dataSource.subscribe(new BaseBitmapDataSubscriber() {
                @Override
                public void onNewResultImpl(@Nullable final  Bitmap frescoBitmap) {
                    // You can use the bitmap in only limited ways
                    // No need to do any cleanup.

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if ((ContextCompat.checkSelfPermission(FullProfileImageActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) ||
                                (ContextCompat.checkSelfPermission(FullProfileImageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED)) {
                            bitmap = frescoBitmap;
                            return;
                        }
                    }

                    Log.i(TAG, "Download -> FINISH = bitmap is null: " + (frescoBitmap == null));
                    FullProfileImageActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            shareBitmap(frescoBitmap);
                            if(menuShare != null) {
                                menuShare.setVisible(true);
                            }
                            if(menuShare2 != null) {
                                menuShare2.setVisible(false);
                            }
                        }
                    });

                }

                @Override
                public void onFailureImpl(DataSource dataSource) {
                    // No cleanup required here.
                    Log.e(TAG, "Download -> Error get bitmap: " + dataSource.getFailureCause().getMessage());
                }
            }, CallerThreadExecutor.getInstance());
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private class FullScreenImageAdapter extends PagerAdapter {
        private List<UserImage> userImageList;

        public FullScreenImageAdapter(List<UserImage> userImageList) {
            this.userImageList = userImageList;
        }

        @Override
        public int getCount() {
            return userImageList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final ViewHolder holder;
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewLayout = inflater.inflate(R.layout.view_pager_full_image, container, false);
            holder.imageView = (ZoomableDraweeViewSupport) viewLayout.findViewById(R.id.iv_photo);
            UserImage userImage = userImageList.get(position);
            Log.i(TAG,"Display pager => "+userImage.getOriginalPic());

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setLowResImageRequest(ImageRequest.fromUri(userImage.getThumbPic()))
                    .setImageRequest(ImageRequest.fromUri(userImage.getOriginalPic()))
                    .setOldController(holder.imageView.getController())
                    .build();
            holder.imageView.setController(controller);
            holder.imageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                        Log.i(TAG, "Imageview On touch : " + motionEvent);
//                        toggle();
                        show();
                    }
                    return false;
                }
            });
            container.addView(viewLayout);
            return viewLayout;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    static class ViewHolder {
        ZoomableDraweeViewSupport imageView;
    }

    private ValueEventListener imageListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            userImageList.clear();
            for (DataSnapshot election : dataSnapshot.getChildren()) {
//                Log.i(TAG,"Election -> key: "+election.getKey()+" , value: "+election.getValue());
                UserImage model = election.getValue(UserImage.class);
                model.setId(election.getKey());
                userImageList.add(model);
                Log.i(TAG, "Added value: " + model.getThumbPic());
            }
            adapter.notifyDataSetChanged();
            galleryAdapter.notifyDataSetChanged();
            int position = 0;
            for (UserImage image : userImageList) {
                if (image.getId().equals(keyImageSelected)) {
                    pager.setCurrentItem(position);
                    image.setSelected(true);
                    galleryAdapter.notifyItemChanged(position);
                    recyclerView.scrollToPosition(position);
                    break;
                }

                position++;
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

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

        if(imageRequest == null){
            return true;
        }

        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>>
                dataSource = imagePipeline.fetchDecodedImage(imageRequest, this);

        dataSource.subscribe(new BaseBitmapDataSubscriber() {
            @Override
            public void onNewResultImpl(@Nullable final  Bitmap frescoBitmap) {
                // You can use the bitmap in only limited ways
                // No need to do any cleanup.

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if ((ContextCompat.checkSelfPermission(FullProfileImageActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) ||
                            (ContextCompat.checkSelfPermission(FullProfileImageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED)) {
                        bitmap = frescoBitmap;
                        return;
                    }
                }
                Log.i(TAG, "Download -> FINISH = bitmap is null: " + (frescoBitmap == null));
                FullProfileImageActivity.this.runOnUiThread(new Runnable() {
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
        if(id == R.id.action_share2){
            Log.i(TAG,"Menu share clicked..!!");
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
        if(mShareActionProvider == null){
            return;
        }
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
            file.delete();
        }
    }
}
