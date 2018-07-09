package com.rndtechnosoft.fynder.dialog;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rndtechnosoft.fynder.FynderApplication;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.model.RoomImage;
import com.rndtechnosoft.fynder.model.User;
import com.rndtechnosoft.fynder.model.UserImage;
import com.rndtechnosoft.fynder.utility.image.bottompicker.ImageBottomPicker;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created by Ravi on 2/19/2017.
 */

public class RoomSettingDialog extends AlertDialog {
    private final String TAG = "RoomDialog";
    private DatabaseReference roomCountry;
    private DatabaseReference roomState;
    private FragmentActivity activity;
//    private TextView textTitle;
//    private TextInputLayout inputLayoutName;
    private TextInputEditText inputName;
    private TextInputEditText inputDesc;
    private SimpleDraweeView imageProfile;
    private DatabaseReference profileRef;
    private DatabaseReference roomNameRef;
    private DatabaseReference roomDescRef;
    private DatabaseReference roomImageRef;
    private DatabaseReference roomLatitude;
    private DatabaseReference roomCity;
    private DatabaseReference roomLognitude;
    private StorageReference storageRef;
    private ProgressDialog myProgressDialog;
    private String oldFilename;

    public RoomSettingDialog(final FragmentActivity activity, String title) {
        super(activity);
        this.activity = activity;
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            dismiss();
            return;
        }
        final String uid = firebaseUser.getUid();
        profileRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child("public").child(uid);
        roomNameRef = roomRef.child("name");
        roomDescRef = roomRef.child("description");
        roomImageRef = roomRef.child("image");
        roomLatitude = roomRef.child("latitude");
        roomLognitude = roomRef.child("longitude");
        roomCity = roomRef.child("city");
        roomState = roomRef.child("address");
        roomCountry = roomRef.child("country");
        storageRef = FirebaseStorage.getInstance().getReference();
        LayoutInflater inflater = LayoutInflater.from(activity);

        View alertView = inflater.inflate(R.layout.dialog_room_setting, null);
        Button buttonClose = (Button) alertView.findViewById(R.id.button_close);
        myProgressDialog = new ProgressDialog(activity);
        myProgressDialog.setMessage(activity.getString(R.string.uploading_image));
        TextView textTitle = (TextView) alertView.findViewById(R.id.text_status_user);
//        inputLayoutName = (TextInputLayout) alertView.findViewById(R.id.input_layout_name);
        inputName = (TextInputEditText) alertView.findViewById(R.id.input_name);
        inputDesc = (TextInputEditText) alertView.findViewById(R.id.input_description);
        imageProfile = (SimpleDraweeView) alertView.findViewById(R.id.image_profile);
        RoundingParams circle = RoundingParams.asCircle()
                .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY);
        imageProfile.getHierarchy().setRoundingParams(circle);
        imageProfile.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageBottomSheet();
            }
        });
        alertView.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageBottomSheet();
            }
        });
        alertView.findViewById(R.id.button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputName.getText().length() == 0){
                    Toast.makeText(activity, activity.getString(R.string.error_empty_room_name), Toast.LENGTH_SHORT).show();
                    return;
                }
                roomNameRef.setValue(inputName.getText().toString());
                roomDescRef.setValue(inputDesc.getText().toString());
                roomLatitude.setValue(FynderApplication.getInstance().getLat());
                roomLognitude.setValue(FynderApplication.getInstance().getLng());
                roomState.setValue(FynderApplication.getInstance().getArea());
                roomCountry.setValue(FynderApplication.getInstance().getCountry());
                roomCity.setValue(FynderApplication.getInstance().getCity());
                dismiss();
            }
        });
        roomNameRef.addValueEventListener(roomNameListener);
        roomDescRef.addValueEventListener(roomDescListener);
        roomImageRef.addValueEventListener(roomImageListener);
        textTitle.setText(title);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setView(alertView);
    }


    private void openImageBottomSheet() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED)) {
                activity.requestPermissions(new String[]{android.Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 90);
                return;
            }
        }
        new ImageBottomPicker.Builder(activity)
                .setOnImageSelectedListener(new ImageBottomPicker.OnImageSelectedListener() {
                    @Override
                    public void onImageSelected(String path) {
                        Log.i(TAG,"On Image success: "+path);
                        doUploadImage(path);
                    }
                })
                .create().show(activity.getSupportFragmentManager());
    }

    private void doUploadImage(final String path){
        myProgressDialog.show();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }
        final String uid = firebaseUser.getUid();
        final String filename = String.valueOf(System.currentTimeMillis());
        StorageReference thumbImagesRef = storageRef.child(uid + "/room_image/" + filename + "_thumb.jpg");
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        Bitmap thumbnail = Bitmap.createScaledBitmap(bitmap, 90, 90, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] thumbData = baos.toByteArray();
        UploadTask uploadThumbnailTask = thumbImagesRef.putBytes(thumbData);
        OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener =
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @SuppressWarnings("VisibleForTests")
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        final Uri thumbUrl = taskSnapshot.getDownloadUrl();
                        Log.i(TAG, "Success to Upload thumbnail, url: " + thumbUrl);
                        StorageReference imagesRef = storageRef.child(uid + "/room_image/" + filename + ".jpg");
                        UploadTask uploadTask = imagesRef.putFile(Uri.fromFile(new File(path)));
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Error upload thumb image: " + e.getMessage());
                                myProgressDialog.dismiss();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                Log.i(TAG, "Success to Upload, url: " + downloadUrl);
                                //save new update
                                RoomImage roomImage = new RoomImage();
                                roomImage.setFilename(filename);
                                roomImage.setOriginalPic(downloadUrl.toString());
                                roomImage.setThumbPic(thumbUrl.toString());
                                roomImageRef.setValue(roomImage);

                                //delete old image
                                if(oldFilename != null){
                                    storageRef.child(uid + "/room_image/" + oldFilename+".jpg").delete();
                                    storageRef.child(uid + "/room_image/" + oldFilename+"_thumb.jpg").delete();
                                }

                                myProgressDialog.dismiss();
                            }
                        });
                    }
                };
        uploadThumbnailTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to upload thumb: " + e.getMessage());
                myProgressDialog.dismiss();
            }
        }).addOnSuccessListener(onSuccessListener);
    }

    private ValueEventListener roomNameListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.getValue() != null) {
                inputName.setText(dataSnapshot.getValue(String.class));
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ValueEventListener roomDescListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.getValue() != null) {
                inputDesc.setText(dataSnapshot.getValue(String.class));
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ValueEventListener roomImageListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.getValue() == null){
                profileRef.addValueEventListener(profileListener);
            }else{
                RoomImage roomImage = dataSnapshot.getValue(RoomImage.class);
                String lowResUrl = roomImage.getThumbPic();
                String highResUrl = roomImage.getOriginalPic();
                oldFilename = roomImage.getFilename();
                Log.i(TAG,"Room image available => "+oldFilename);
                if(lowResUrl != null && highResUrl != null) {
                    DraweeController controller = Fresco.newDraweeControllerBuilder()
                            .setLowResImageRequest(ImageRequest.fromUri(lowResUrl))
                            .setImageRequest(ImageRequest.fromUri(highResUrl))
                            .setOldController(imageProfile.getController())
                            .build();
                    imageProfile.setController(controller);
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ValueEventListener profileListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            User model = dataSnapshot.getValue(User.class);
            model.setImageUrl(model.getImageUrl());
            for (DataSnapshot election : dataSnapshot.getChildren()) {
                if (election.getKey().equals("images")) {
                    String key = model.getImageUrl() == null ? "default" : model.getImageUrl();
                    DataSnapshot defaultImage = election.child(key);
                    if (defaultImage.exists()) {
                        UserImage userImage = defaultImage.getValue(UserImage.class);
                        DraweeController controller = Fresco.newDraweeControllerBuilder()
                                .setLowResImageRequest(ImageRequest.fromUri(userImage.getThumbPic()))
                                .setImageRequest(ImageRequest.fromUri(userImage.getOriginalPic()))
                                .setOldController(imageProfile.getController())
                                .build();
                        imageProfile.setController(controller);
                        break;
                    }
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.i(TAG, "on detached from window");
        if(roomNameRef != null){
            roomNameRef.removeEventListener(roomNameListener);
        }
        if(roomDescRef != null){
            roomDescRef.removeEventListener(roomDescListener);
        }
        if(profileRef != null){
            profileRef.removeEventListener(profileListener);
        }
        if(roomImageRef != null){
            roomImageRef.removeEventListener(roomImageListener);
        }
    }
}
