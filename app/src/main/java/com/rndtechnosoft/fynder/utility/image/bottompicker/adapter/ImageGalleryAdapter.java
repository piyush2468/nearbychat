package com.rndtechnosoft.fynder.utility.image.bottompicker.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.utility.MyImageUtil;
import com.rndtechnosoft.fynder.utility.image.bottompicker.ImageBottomPicker;
import com.rndtechnosoft.fynder.utility.image.bottompicker.view.SquareFrameLayout;
import com.rndtechnosoft.fynder.utility.image.bottompicker.view.SquareImageView;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * Created by TedPark on 2016. 8. 30..
 */
public class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.GalleryViewHolder> {
    private ArrayList<PickerTile> pickerTiles;
    private Context context;
    private ImageBottomPicker.Builder builder;
    private OnItemClickListener onItemClickListener;

    public ImageGalleryAdapter(Context context, ImageBottomPicker.Builder builder) {

        this.context = context;
        this.builder = builder;

        pickerTiles = new ArrayList<PickerTile>();

        if (builder.showCamera) {
            pickerTiles.add(new PickerTile(PickerTile.CAMERA));
        }

        if (builder.showGallery) {
            pickerTiles.add(new PickerTile(PickerTile.GALLERY));
        }



//        Cursor imageCursor = null;
        Cursor cursor = null;
        try {
            // Get relevant columns for use later.
            String[] projection = {
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.DATE_ADDED,
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.TITLE
            };

// Return only video and image metadata.
            String selection;
            if(builder.imageOnly){
                selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
            }else {
                selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                        + " OR "
                        + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
            }


            Uri queryUri = MediaStore.Files.getContentUri("external");

            CursorLoader cursorLoader = new CursorLoader(
                    context,
                    queryUri,
                    projection,
                    selection,
                    null, // Selection args (none).
                    MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
            );

            cursor = cursorLoader.loadInBackground();
//            final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.ImageColumns.ORIENTATION};
//            final String orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC";
//
//
    //            imageCursor = context.getApplicationContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);

            if (cursor != null) {

                int count = 0;
                String cachePath = "cache/"+context.getString(R.string.app_name) + ".jpg";
//                Log.i("Media_store", "Cache path: "+cachePath);
                while (cursor.moveToNext() && count < builder.maxCount) {
                    String imageLocation = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    if(imageLocation.contains(cachePath)){
                        continue;
                    }

                    File imageFile = new File(imageLocation);
                    if(!imageFile.exists()){
                        Log.i("Media_store", "File doesn't exist: "+imageLocation);
                        continue;
                    }

//                    String extension = MimeTypeMap.getFileExtensionFromUrl(imageLocation);
//                    Log.i("Media_store", "image location: "+imageLocation+" , mime type: "+MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension));
                    pickerTiles.add(new PickerTile(Uri.fromFile(imageFile), MyImageUtil.isImageFile(imageFile)));
                    count++;

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }


    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.bottompicker_grid_item, null);
        final GalleryViewHolder holder = new GalleryViewHolder(view);


        return holder;
    }

    @Override
    public void onBindViewHolder(final GalleryViewHolder holder, final int position) {

        PickerTile pickerTile = getItem(position);


        if (pickerTile.isCameraTile()) {
            holder.iv_thumbnail.setBackgroundResource(builder.cameraTileBackgroundResId);
            holder.iv_thumbnail.setImageDrawable(builder.cameraTileDrawable);
            holder.videoLayout.setVisibility(View.GONE);
        } else if (pickerTile.isGalleryTile()) {
            holder.iv_thumbnail.setBackgroundResource(builder.galleryTileBackgroundResId);
            holder.iv_thumbnail.setImageDrawable(builder.galleryTileDrawable);
            holder.videoLayout.setVisibility(View.GONE);
        } else {
            Uri uri = pickerTile.getImageUri();
            if (builder.imageProvider == null) {
                holder.iv_thumbnail.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
                holder.iv_thumbnail.getHierarchy().setPlaceholderImage(R.drawable.ic_gallery);
                holder.iv_thumbnail.setImageURI(uri);
//                Glide.with(context)
//                        .load(uri)
//                        .thumbnail(0.1f)
//                        .dontAnimate()
//                        .centerCrop()
//                        .placeholder(R.drawable.ic_gallery)
//                        .error(R.drawable.ic_gallery)
//                        .into(holder.iv_thumbnail);
            } else {
                builder.imageProvider.onProvideImage(holder.iv_thumbnail, uri);
            }
            holder.videoLayout.setVisibility(pickerTile.isImage? View.GONE : View.VISIBLE);

        }


        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(holder.itemView, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return pickerTiles.size();
    }

    public PickerTile getItem(int position) {
        return pickerTiles.get(position);
    }

    public void setOnItemClickListener(
            OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }


    public static class PickerTile {

        public static final int IMAGE = 1;
        public static final int CAMERA = 2;
        public static final int GALLERY = 3;
        protected final Uri imageUri;
        protected final
        @TileType
        int tileType;
        boolean isImage;

        PickerTile(@SpecialTileType int tileType) {
            this(null, tileType, true);
        }

        PickerTile(@NonNull Uri imageUri, boolean isImage) {
            this(imageUri, IMAGE, isImage);
        }

        protected PickerTile(@Nullable Uri imageUri, @TileType int tileType, boolean isImage) {
            this.imageUri = imageUri;
            this.tileType = tileType;
            this.isImage = isImage;
        }

        @Nullable
        public Uri getImageUri() {
            return imageUri;
        }

        @TileType
        public int getTileType() {
            return tileType;
        }

        public boolean isImageTile() {
            return tileType == IMAGE;
        }

        public boolean isCameraTile() {
            return tileType == CAMERA;
        }

        public boolean isGalleryTile() {
            return tileType == GALLERY;
        }

        public boolean isImage(){ return isImage;}
        @Override
        public String toString() {
            if (isImageTile()) {
                return "ImageTile: " + imageUri;
            } else if (isCameraTile()) {
                return "CameraTile";
            } else if (isGalleryTile()) {
                return "PickerTile";
            } else {
                return "Invalid item";
            }
        }

        @IntDef({IMAGE, CAMERA, GALLERY})
        @Retention(RetentionPolicy.SOURCE)
        public @interface TileType {
        }

        @IntDef({CAMERA, GALLERY})
        @Retention(RetentionPolicy.SOURCE)
        public @interface SpecialTileType {
        }
    }

    class GalleryViewHolder extends RecyclerView.ViewHolder {

        SquareFrameLayout root;
        RelativeLayout videoLayout;
        SimpleDraweeView iv_thumbnail;

        public GalleryViewHolder(View view) {
            super(view);
            root = (SquareFrameLayout) view.findViewById(R.id.root);
            iv_thumbnail = (SimpleDraweeView) view.findViewById(R.id.iv_thumbnail);
            videoLayout = (RelativeLayout) view.findViewById(R.id.video_layout);

        }

    }

}
