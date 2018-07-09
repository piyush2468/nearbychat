package com.rndtechnosoft.fynder.utility.image.bottompicker;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.activity.CameraActivity;
import com.rndtechnosoft.fynder.activity.PreviewImageActivity;
import com.rndtechnosoft.fynder.utility.image.bottompicker.adapter.ImageGalleryAdapter;


public class ImageBottomPicker extends BottomSheetDialogFragment {

    private static final String TAG = "ImageBottomPicker";
    private static final int REQ_CODE = 124;
//    private static final int REQ_CODE_GALLERY = 2;
    ImageGalleryAdapter imageGalleryAdapter;
    Builder builder;
    TextView tv_title;
    private RecyclerView rc_gallery;
    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {


        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {

            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismissAllowingStateLoss();
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }
    };
//    private Uri cameraImageUri;

    public void show(FragmentManager fragmentManager) {

        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(this, getTag());
        ft.commitAllowingStateLoss();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onViewCreated(View contentView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(contentView, savedInstanceState);


    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.bottompicker_content_view, null);
        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams layoutParams =
                (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
            if(builder  !=null)
            if (builder.peekHeight > 0) {
                // ((BottomSheetBehavior) behavior).setPeekHeight(1500);
                ((BottomSheetBehavior) behavior).setPeekHeight(builder.peekHeight);
            }

        }

        rc_gallery = (RecyclerView) contentView.findViewById(R.id.rc_gallery);
        setRecyclerView();

        tv_title = (TextView) contentView.findViewById(R.id.tv_title);
        setTitle();
    }


    private void setRecyclerView() {

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 4);
        rc_gallery.setLayoutManager(gridLayoutManager);


        rc_gallery.addItemDecoration(new GridSpacingItemDecoration(gridLayoutManager.getSpanCount(), builder.spacing, false));

        imageGalleryAdapter = new ImageGalleryAdapter(
                getActivity()
                , builder);
        rc_gallery.setAdapter(imageGalleryAdapter);
        imageGalleryAdapter.setOnItemClickListener(new ImageGalleryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                ImageGalleryAdapter.PickerTile pickerTile = imageGalleryAdapter.getItem(position);

                switch (pickerTile.getTileType()) {
                    case ImageGalleryAdapter.PickerTile.CAMERA:
//                        startCameraIntent();
                        startActivityForResult(new Intent(getActivity(), CameraActivity.class), REQ_CODE);
                        break;
//                    case ImageGalleryAdapter.PickerTile.GALLERY:
//                        startGalleryIntent();
//                        break;
                    case ImageGalleryAdapter.PickerTile.IMAGE:
//                        complete(pickerTile.getImageUri());
                        Uri uri = pickerTile.getImageUri();
                        if(uri != null) {
                            if (pickerTile.isImage()) {
                                Intent intent = new Intent(getActivity(), PreviewImageActivity.class);
                                intent.putExtra(PreviewImageActivity.KEY_URI, uri.toString());
                                intent.putExtra(PreviewImageActivity.KEY_IS_SQUARE, builder.isSquare);
                                startActivityForResult(intent, REQ_CODE);
                            } else {
                                builder.onImageSelectedListener.onImageSelected(uri.getPath());
                                dismissAllowingStateLoss();
                            }
                        }
                        break;

                    default:
                        errorMessage();
                }

            }
        });
    }

    private void setTitle() {

        if (!builder.showTitle) {
            tv_title.setVisibility(View.GONE);
            return;
        }

        if (!TextUtils.isEmpty(builder.title)) {
            tv_title.setText(builder.title);
        }

        if (builder.titleBackgroundResId > 0) {
            tv_title.setBackgroundResource(builder.titleBackgroundResId);
        }

    }


//    private void complete(Uri uri) {
        //uri = Uri.parse(uri.toString());
//        builder.onImageSelectedListener.onImageSelected(uri);

//        dismissAllowingStateLoss();
//    }

//    private void startCameraIntent() {

//        new SandriosCamera(getActivity(), REQ_CODE_CAMERA)
//                .setShowPicker(false)
////                .setVideoFileSize(15) //File Size in MB: Default is no limit
//                .setMediaAction(CameraConfiguration.MEDIA_ACTION_PHOTO) // default is CameraConfiguration.MEDIA_ACTION_BOTH
//                .enableImageCropping(true) // Default is false.
//                .launchCamera();
//    }

//    private void startGalleryIntent() {
//        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        if (galleryIntent.resolveActivity(getActivity().getPackageManager()) == null) {
//            errorMessage("This Application do not have Gallery Application");
//            return;
//        }
//
//        startActivityForResult(galleryIntent, REQ_CODE_GALLERY);
//
//    }


    private void errorMessage() {
        errorMessage(null);
    }

    private void errorMessage(String message) {
        String errorMessage = message == null ? "Something wrong." : message;

        if (builder.onErrorListener == null) {
            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
        } else {
            builder.onErrorListener.onError(errorMessage);
        }
    }


    public interface OnImageSelectedListener {
        void onImageSelected(String path);
    }

    public interface OnErrorListener {
        void onError(String message);
    }

    public interface ImageProvider {
        void onProvideImage(ImageView imageView, Uri imageUri);
    }

    public static class Builder {

        public Context context;
        public int maxCount = 1000;
        public Drawable cameraTileDrawable;
        public Drawable galleryTileDrawable;

        public int spacing = 1;
        public OnImageSelectedListener onImageSelectedListener;
        public OnErrorListener onErrorListener;
        public ImageProvider imageProvider;
        public boolean showCamera = true;
        public boolean showGallery = false;
        public int peekHeight = -1;
        public int cameraTileBackgroundResId = R.color.tedbottompicker_camera;
        public int galleryTileBackgroundResId = R.color.tedbottompicker_gallery;

        public String title;
        public boolean showTitle = true;
        public boolean imageOnly = true;
        private boolean isSquare = false;
        public int titleBackgroundResId;

        public Builder(@NonNull Context context) {

            this.context = context;

            setCameraTile(R.drawable.ic_camera_chat_bottompicker);
            setGalleryTile(R.drawable.ic_gallery);
            setSpacingResId(R.dimen.tedbottompicker_grid_layout_margin);
        }

        public Builder setMaxCount(int maxCount) {
            this.maxCount = maxCount;
            return this;
        }

        public Builder setVideoSelection(){
            this.imageOnly = false;
            return this;
        }

        public Builder setSquare(boolean isSquare){
            this.isSquare = isSquare;
            return this;
        }

        public Builder setOnImageSelectedListener(OnImageSelectedListener onImageSelectedListener) {
            this.onImageSelectedListener = onImageSelectedListener;
            return this;
        }

        public Builder setOnErrorListener(OnErrorListener onErrorListener) {
            this.onErrorListener = onErrorListener;
            return this;
        }

        public Builder showCameraTile(boolean showCamera) {
            this.showCamera = showCamera;
            return this;
        }

        public Builder setCameraTile(@DrawableRes int cameraTileResId) {
            setCameraTile(ContextCompat.getDrawable(context, cameraTileResId));
            return this;
        }

        public Builder setCameraTile(Drawable cameraTileDrawable) {
            this.cameraTileDrawable = cameraTileDrawable;
            return this;
        }

        public Builder showGalleryTile(boolean showGallery) {
            this.showGallery = showGallery;
            return this;
        }

        public Builder setGalleryTile(@DrawableRes int galleryTileResId) {
            setGalleryTile(ContextCompat.getDrawable(context, galleryTileResId));
            return this;
        }

        public Builder setGalleryTile(Drawable galleryTileDrawable) {
            this.galleryTileDrawable = galleryTileDrawable;
            return this;
        }

        public Builder setSpacing(int spacing) {
            this.spacing = spacing;
            return this;
        }

        public Builder setSpacingResId(@DimenRes int dimenResId) {
            this.spacing = context.getResources().getDimensionPixelSize(dimenResId);
            return this;
        }

        public Builder setPeekHeight(int peekHeight) {
            this.peekHeight = peekHeight;
            return this;
        }

        public Builder setPeekHeightResId(@DimenRes int dimenResId) {
            this.peekHeight = context.getResources().getDimensionPixelSize(dimenResId);
            return this;
        }

        public Builder setCameraTileBackgroundResId(@ColorRes int colorResId) {
            this.cameraTileBackgroundResId = colorResId;
            return this;
        }

        public Builder setGalleryTileBackgroundResId(@ColorRes int colorResId) {
            this.galleryTileBackgroundResId = colorResId;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setTitle(@StringRes int stringResId) {
            this.title = context.getResources().getString(stringResId);
            return this;
        }

        public Builder showTitle(boolean showTitle) {
            this.showTitle = showTitle;
            return this;
        }

        public Builder setTitleBackgroundResId(@ColorRes int colorResId) {
            this.titleBackgroundResId = colorResId;
            return this;
        }

        public Builder setImageProvider(ImageProvider imageProvider) {
            this.imageProvider = imageProvider;
            return this;
        }


        public ImageBottomPicker create() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                throw new RuntimeException("Missing required WRITE_EXTERNAL_STORAGE permission. Did you remember to request it first?");
            }

            if (onImageSelectedListener == null) {
                throw new RuntimeException("You have to setOnImageSelectedListener() for receive selected Uri");
            }

            ImageBottomPicker customBottomSheetDialogFragment = new ImageBottomPicker();

            customBottomSheetDialogFragment.builder = this;
            return customBottomSheetDialogFragment;
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG,"On Activity Result => request code: "+requestCode+" , result code: "+resultCode);
        if (resultCode == Activity.RESULT_OK) {
            if(requestCode == REQ_CODE && data != null){
                String path = data.getStringExtra(PreviewImageActivity.KEY_RESULT_PATH);
                builder.onImageSelectedListener.onImageSelected(path);
                dismissAllowingStateLoss();
            }
            super.onActivityResult(requestCode, resultCode, data);

//            Uri selectedImageUri = null;
//            int type = 0;
//            if (requestCode == REQ_CODE_GALLERY && data != null) {
//                selectedImageUri = data.getData();
//                type = ImageGalleryAdapter.PickerTile.GALLERY;
//                if (selectedImageUri == null) {
//                    errorMessage();
//                }
//            } else if (requestCode == REQ_CODE_CAMERA && data != null) {
//                // Do something with imagePath
//                selectedImageUri = data.getData();
//                type = ImageGalleryAdapter.PickerTile.CAMERA;
//            }
//
//            if (selectedImageUri != null) {
//                complete(selectedImageUri);
//            } else {
//                errorMessage();
//            }
        }

    }
}
