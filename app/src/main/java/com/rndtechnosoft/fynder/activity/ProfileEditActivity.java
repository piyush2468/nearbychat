package com.rndtechnosoft.fynder.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;

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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.adapter.GalleryAdapter;
import com.rndtechnosoft.fynder.adapter.ItemAdapter;
import com.rndtechnosoft.fynder.dialog.GenderSelectDialog;
import com.rndtechnosoft.fynder.dialog.RelationshipStatusSelectDialog;
import com.rndtechnosoft.fynder.dialog.InterestedInSelectDialog;
import com.rndtechnosoft.fynder.dialog.YouLookingSelectDialog;
import com.rndtechnosoft.fynder.model.Item;
import com.rndtechnosoft.fynder.model.User;
import com.rndtechnosoft.fynder.model.UserImage;
import com.rndtechnosoft.fynder.utility.MyImageUtil;
import com.rndtechnosoft.fynder.utility.image.bottompicker.ImageBottomPicker;
import com.rndtechnosoft.fynder.utility.listener.OnSuccessUploadListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class ProfileEditActivity extends AppCompatActivity implements View.OnClickListener,View.OnFocusChangeListener,GenderSelectDialog.AlertPositiveListener, RelationshipStatusSelectDialog.AlertPositiveListener, YouLookingSelectDialog.AlertPositiveListener, InterestedInSelectDialog.AlertPositiveListener {
    private final String TAG = "ProfileEditActivity.log";
    private final int OPEN_IMAGE = 536;
    private String mainImage;
    private String mainImageUrl;
    private boolean isDefault = false;
    private boolean setMainImage = false;
    private SimpleDraweeView imageProfile;
    private DatabaseReference profileRef;
    private DatabaseReference imagesRef;
    private StorageReference storageRef;
    private UploadTask uploadThumbnailTask;
    private ProgressDialog myProgressDialog;
    private GalleryAdapter adapter;
    private LinearLayout layoutGallery;
    RecyclerView galleryView;
    private List<UserImage> galleryList = new ArrayList<>();
    //birthday
    private int _year;
    private int _month;
    private int _day;

    private int  year, month, day;
    private int gender,relationshipStatus, youLooking, youLike, allowShowMyBirthday;


    private TextInputEditText etDisplayName, etStatus, etBirthday, etRelationStatus,etYouLooking, etInterestedIn,etGender;
    private TextInputLayout layoutDisplayName, layoutetStatus, layoutBirthday, layoutRelationStatus, layoutYouLooking, layoutYouLike,layoutGender;
    private ImageButton button_add_gallery;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        initView();
        setDP();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        galleryView.setLayoutManager(layoutManager);
        galleryView.setAdapter(adapter);


        setName();
        setStatus();

        storageRef = FirebaseStorage.getInstance().getReference();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            profileRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());
            imagesRef = profileRef.child("images");
        } else {
            finish();
        }
    }

    private void setDP() {
        adapter = new GalleryAdapter(galleryList, new GalleryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final UserImage item) {
                Log.i(TAG, "Id: " + item.getId() + " | Image Url: " + item.getThumbPic());
                final Item[] items = {
                        new Item(getString(R.string.option_view), android.R.drawable.ic_menu_view),
                        new Item(getString(R.string.option_set_main), android.R.drawable.ic_menu_more),
                        new Item(getString(R.string.option_remove), android.R.drawable.ic_menu_close_clear_cancel),
                };
                ItemAdapter adapter = new ItemAdapter(ProfileEditActivity.this, items);
                new AlertDialog.Builder(ProfileEditActivity.this)
                        .setTitle(getString(R.string.title_select_image))
                        .setAdapter(adapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    //view image
                                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                    if (firebaseUser == null) {
                                        return;
                                    }
                                    String uid = firebaseUser.getUid();
                                    Intent intent = new Intent(ProfileEditActivity.this, FullProfileImageActivity.class);
                                    intent.putExtra(FullProfileImageActivity.KEY_IMAGE, item.getId());
                                    intent.putExtra(FullProfileImageActivity.KEY_IMAGE, item.getOriginalPic());
                                    intent.putExtra(FullProfileImageActivity.KEY_UID, uid);
                                    startActivity(intent);
                                } else if (i == 1) {
                                    //set main image
                                    profileRef.child("imageUrl").setValue(item.getId());
                                } else if (i == 2) {
                                    new AlertDialog.Builder(ProfileEditActivity.this)
                                            .setTitle(getString(R.string.title_delete_image))
                                            .setMessage(getString(R.string.message_dialog_image))
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                                    if (firebaseUser == null) {
                                                        return;
                                                    }
                                                    String uid = firebaseUser.getUid();
                                                    profileRef.child("images").child(item.getId()).removeValue();
                                                    storageRef.child(uid + "/" + item.getId() + "_thumb.jpg").delete();
                                                    storageRef.child(uid + "/" + item.getId() + ".jpg").delete();
                                                }
                                            })
                                            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                }
                                            }).show();
                                }
                            }
                        }).create().show();
            }
        });
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        myProgressDialog = new ProgressDialog(this);
        myProgressDialog.setMessage(getString(R.string.uploading_image));
        layoutGallery = (LinearLayout) findViewById(R.id.layout_gallery);


        button_add_gallery = (ImageButton) findViewById(R.id.button_add_gallery);
        etDisplayName = (TextInputEditText) findViewById(R.id.etFullname);
        etStatus = (TextInputEditText) findViewById(R.id.etStatus);
        etBirthday = (TextInputEditText) findViewById(R.id.etbirthday);
        etRelationStatus = (TextInputEditText) findViewById(R.id.etrelationStatus);
        etYouLooking = (TextInputEditText) findViewById(R.id.etYouLooking);
        etInterestedIn = (TextInputEditText) findViewById(R.id.etYouLike);
        etGender = (TextInputEditText) findViewById(R.id.etGender);

        layoutDisplayName = (TextInputLayout) findViewById(R.id.input_layout_fullname);
        layoutetStatus = (TextInputLayout) findViewById(R.id.input_layout_status);
        layoutBirthday = (TextInputLayout) findViewById(R.id.input_layout_birthday);
        layoutRelationStatus = (TextInputLayout) findViewById(R.id.input_layout_relationStatus);
        layoutYouLooking = (TextInputLayout) findViewById(R.id.input_layout_YouLooking);
        layoutYouLike = (TextInputLayout) findViewById(R.id.input_layout_YouLike);
        layoutGender= (TextInputLayout) findViewById(R.id.input_layout_Gender);


        imageProfile = (SimpleDraweeView) findViewById(R.id.image_profile);

        RoundingParams circle = RoundingParams.asCircle()
                .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY);
        imageProfile.getHierarchy().setRoundingParams(circle);
        imageProfile.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
        imageProfile.getHierarchy().setPlaceholderImage(R.drawable.ic_profile_default);
        galleryView = (RecyclerView) findViewById(R.id.list_gallery);

        etGender.setOnFocusChangeListener(this);
        etRelationStatus.setOnFocusChangeListener(this);
        etYouLooking.setOnFocusChangeListener(this);
        etInterestedIn.setOnFocusChangeListener(this);
        etBirthday.setOnFocusChangeListener(this);
        button_add_gallery.setOnClickListener(this);
        fab.setOnClickListener(this);
        imageProfile.setOnClickListener(this);

        etGender.setInputType(InputType.TYPE_NULL);
        etRelationStatus.setInputType(InputType.TYPE_NULL);
        etYouLooking.setInputType(InputType.TYPE_NULL);
        etInterestedIn.setInputType(InputType.TYPE_NULL);
        etBirthday.setInputType(InputType.TYPE_NULL);

    }

    private void setStatus() {
        etStatus.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    private Timer timer = new Timer();
                    private final long DELAY = 3000; // milliseconds

                    @Override
                    public void afterTextChanged(final Editable s) {
                        timer.cancel();
                        timer = new Timer();
                        timer.schedule(
                                new TimerTask() {
                                    @Override
                                    public void run() {
                                        profileRef.child("bio").setValue(etStatus.getText().toString());
//                                        etStatus.setSelection(etStatus.getText().length());
                                    }
                                },
                                DELAY
                        );
                    }
                }
        );

    }




    private void setName() {
        etDisplayName.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    private Timer timer = new Timer();
                    private final long DELAY = 3000; // milliseconds

                    @Override
                    public void afterTextChanged(final Editable s) {
                        timer.cancel();
                        timer = new Timer();
                        timer.schedule(
                                new TimerTask() {
                                    @Override
                                    public void run() {

                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(etDisplayName.getText().toString())
                                                .build();
                                        FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates);
                                        profileRef.child("name").setValue(etDisplayName.getText().toString());
//                                        etDisplayName.setSelection(etDisplayName.getText().length());
                                    }
                                },
                                DELAY
                        );
                    }
                }
        );

    }

    @Override
    protected void onStart() {
        super.onStart();
        galleryList.clear();
        adapter.notifyDataSetChanged();
        if (profileRef != null) {
            profileRef.addValueEventListener(profileListener);
            imagesRef.addChildEventListener(imagesListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (profileRef != null) {
            profileRef.removeEventListener(profileListener);
            imagesRef.removeEventListener(imagesListener);
        }
        if (uploadThumbnailTask != null && onSuccessUpload != null) {
            uploadThumbnailTask.removeOnSuccessListener(onSuccessUpload);
        }
    }

    private ValueEventListener profileListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            User model = dataSnapshot.getValue(User.class);
            if (model == null) {
                return;
            }
            Log.i(TAG, "Name: " + model.getName());
            etDisplayName.setText(model.getName());
            etStatus.setText(model.getBio());
            etBirthday.setText(model.getGender());
            etRelationStatus.setText(model.getRelationshipStatus());
            etYouLooking.setText(model.getLookingFor());
            etInterestedIn.setText(model.getInterestedIn());

            boolean isImageAvailable = false;
            for (DataSnapshot election : dataSnapshot.getChildren()) {
                if (election.getKey().equals("images")) {
                    Log.i(TAG, "count images: " + election.getChildrenCount());
                    isImageAvailable = election.getChildrenCount() > 0;
                    String key = model.getImageUrl() == null ? "default" : model.getImageUrl();
                    mainImage = key;
                    DataSnapshot defaultImage = election.child(key);
                    Log.i(TAG, "Main image is exist: " + defaultImage.exists());
                    if (defaultImage.exists()) {
                        UserImage userImage = defaultImage.getValue(UserImage.class);
                        DraweeController controller = Fresco.newDraweeControllerBuilder()
                                .setLowResImageRequest(ImageRequest.fromUri(userImage.getThumbPic()))
                                .setImageRequest(ImageRequest.fromUri(userImage.getOriginalPic()))
                                .setOldController(imageProfile.getController())
                                .build();
                        imageProfile.setController(controller);
                        mainImageUrl = userImage.getOriginalPic();
                        //set main image to adapter
                        for (UserImage images : galleryList) {
                            images.setMainImage(mainImage.equals(images.getId()));
                        }
                        adapter.notifyDataSetChanged();
                        break;
                    } else {
                        profileRef.child("imageUrl").setValue(null);
                    }
                }
            }
            if (model.getBirthday() != null) {
                try {
                    String year = model.getBirthday().substring(0, 4);
                    Log.i(TAG, "year: " + year);
                    String month = model.getBirthday().substring(4, 6);
                    Log.i(TAG, "month: " + month);
                    String day = model.getBirthday().substring(6, 8);
                    int yearInt = Integer.parseInt(year);
                    Log.i(TAG, "day: " + day);
                    int monthInt = Integer.parseInt(month);
                    int dayInt = Integer.parseInt(day);
                    etBirthday.setText(displayDate(yearInt, monthInt, dayInt));
                    _year = yearInt;
                    _month = monthInt;
                    _day = dayInt;

                } catch (Exception e) {
                    Log.e(TAG, "Error parse date: " + e.getMessage());
                }
            }
            if (model.getGender() != null) {
                etGender.setText(model.getGender());
            }
            isDefault = !isImageAvailable;
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
        if (requestCode == OPEN_IMAGE) {
            if (grantResults.length < 1) {
                return;
            }
            boolean permission1 = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean permission2 = grantResults[1] == PackageManager.PERMISSION_GRANTED;
            boolean permission3 = grantResults[2] == PackageManager.PERMISSION_GRANTED;
            boolean isPermissionGranted = permission1 && permission2 && permission3;
            if (isPermissionGranted) {
                openImageBottomSheet();
            }
        }
    }


    private OnSuccessListener<UploadTask.TaskSnapshot> onSuccessUpload;

    private ChildEventListener imagesListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            layoutGallery.setVisibility(View.VISIBLE);
            UserImage model = dataSnapshot.getValue(UserImage.class);
            model.setId(dataSnapshot.getKey());
            String imageMain = mainImage == null ? "default" : mainImage;
            Log.i(TAG, "image id: " + model.getId() + " , main image: " + imageMain);
            model.setMainImage(imageMain.equals(model.getId()));
            galleryList.add(0, model);
            adapter.notifyItemInserted(0);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.i(TAG, "On Child Changed");
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.i(TAG, "REMOVED -> image id: " + dataSnapshot.getKey());
            int position = 0;
            for (UserImage userImage : galleryList) {
                if (userImage.getId().equals(dataSnapshot.getKey())) {
                    galleryList.remove(position);
                    adapter.notifyItemRemoved(position);
                    break;
                }
                position++;
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            Log.i(TAG, "On Child Moved");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    private String displayDate(int year, int month, int day) {
        String month_array[] = getResources().getStringArray(R.array.month);
        return month_array[month] + " " + String.valueOf(day) + ", " + String.valueOf(year);
    }

    //Do not call onCreate on Parent Activity
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void doUploadImage(final String path) {
        myProgressDialog.show();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }
        String uid = firebaseUser.getUid();
        String tempKey = isDefault ? "default" : profileRef.child("images").push().getKey();
        StorageReference profileImagesRef = storageRef.child(uid + "/" + tempKey + "_thumb.jpg");
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        Bitmap thumbnail = Bitmap.createScaledBitmap(bitmap, 90, 90, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] thumbData = baos.toByteArray();
        uploadThumbnailTask = profileImagesRef.putBytes(thumbData);
        onSuccessUpload = new OnSuccessUploadListener(tempKey) {
            @Override
            public void onSuccessUpload(final String key, UploadTask.TaskSnapshot taskSnapshot) {
                @SuppressWarnings("VisibleForTests")
                final Uri thumbUrl = taskSnapshot.getDownloadUrl();
                Log.i(TAG, "Success to Upload thumbnail, url: " + thumbUrl);
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser == null) {
                    return;
                }
                String uid = firebaseUser.getUid();
                StorageReference profileImagesRef = storageRef.child(uid + "/" + key + ".jpg");
                UploadTask uploadTask = profileImagesRef.putFile(Uri.fromFile(new File(path)));
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error upload thumb image: " + e.getMessage());
                        myProgressDialog.dismiss();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests")
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Log.i(TAG, "Success to Upload, url: " + downloadUrl);
                        UserImage userImage = new UserImage(thumbUrl.toString(), downloadUrl.toString());
                        Map<String, Object> postValues = userImage.toMap();
//                            profileRef.child("images").child(tempKey).setValue(userImage);
                        profileRef.child("images").child(key).updateChildren(postValues);
                        if (setMainImage) {
                            profileRef.child("imageUrl").setValue(key);
                        }
                        setMainImage = false;
                        myProgressDialog.dismiss();
                    }
                });
            }
        };
        uploadThumbnailTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to upload thumb: " + e.getMessage());
            }
        }).addOnSuccessListener(onSuccessUpload);
    }

    private void openImageBottomSheet() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, OPEN_IMAGE);
                return;
            }
        }
        new ImageBottomPicker.Builder(this)
                .setSquare(true)
                .setOnImageSelectedListener(new ImageBottomPicker.OnImageSelectedListener() {
                    @Override
                    public void onImageSelected(String path) {
                        Log.i(TAG, "On Image success: " + path);
                        if (MyImageUtil.isImageFile(new File(path))) {
                            doUploadImage(path);
                        }
                    }
                })
                .create().show(getSupportFragmentManager());
    }



    public void selectYouLooking(int position) {

        android.app.FragmentManager fm = getFragmentManager();

        YouLookingSelectDialog alert = new YouLookingSelectDialog();

        Bundle b  = new Bundle();
        b.putInt("position", position);

        alert.setArguments(b);
        alert.show(fm, "alert_dialog_select_you_looking");
    }



    public void selectRelationshipStatus(int position) {

        android.app.FragmentManager fm = getFragmentManager();

        RelationshipStatusSelectDialog alert = new RelationshipStatusSelectDialog();

        Bundle b  = new Bundle();
        b.putInt("position", position);

        alert.setArguments(b);
        alert.show(fm, "alert_dialog_select_relationship_status");
    }

    public void getRelationshipStatus(int mRelationship) {

        relationshipStatus = mRelationship;

        switch (mRelationship) {

            case 0: {

                etRelationStatus.setText(getString(R.string.relationship_status_0));
                if (profileRef != null) {
                    profileRef.child("relationshipStatus").setValue(getString(R.string.relationship_status_0));
                }
                break;
            }

            case 1: {

                etRelationStatus.setText(getString(R.string.relationship_status_1));
                if (profileRef != null) {
                    profileRef.child("relationshipStatus").setValue(getString(R.string.relationship_status_1));
                }
                break;
            }

            case 2: {

                etRelationStatus.setText(getString(R.string.relationship_status_2));
                if (profileRef != null) {
                    profileRef.child("relationshipStatus").setValue(getString(R.string.relationship_status_2));
                }
                break;
            }

            case 3: {

                etRelationStatus.setText(getString(R.string.relationship_status_3));
                if (profileRef != null) {
                    profileRef.child("relationshipStatus").setValue(getString(R.string.relationship_status_3));
                }
                break;
            }

            case 4: {

                etRelationStatus.setText(getString(R.string.relationship_status_4));
                if (profileRef != null) {
                    profileRef.child("relationshipStatus").setValue(getString(R.string.relationship_status_4));
                }
                break;
            }

            case 5: {

                etRelationStatus.setText(getString(R.string.relationship_status_5));
                if (profileRef != null) {
                    profileRef.child("relationshipStatus").setValue(getString(R.string.relationship_status_5));
                }
                break;
            }

            case 6: {

                etRelationStatus.setText(getString(R.string.relationship_status_6));
                if (profileRef != null) {
                    profileRef.child("relationshipStatus").setValue(getString(R.string.relationship_status_6));
                }
                break;
            }

            case 7: {

                etRelationStatus.setText(getString(R.string.relationship_status_7));
                if (profileRef != null) {
                    profileRef.child("relationshipStatus").setValue(getString(R.string.relationship_status_7));
                }
                break;
            }

            default: {

                break;
            }
        }
    }


    public void getYouLooking(int mLooking) {

        youLooking = mLooking;

        switch (mLooking) {

            case 0: {

                etYouLooking.setText(getString(R.string.you_looking_0));
                if (profileRef != null) {
                    profileRef.child("lookingFor").setValue(getString(R.string.you_looking_0));
                }
                break;
            }

            case 1: {

                etYouLooking.setText(getString(R.string.you_looking_1));
                if (profileRef != null) {
                    profileRef.child("lookingFor").setValue(getString(R.string.you_looking_1));
                }
                break;
            }

            case 2: {

                etYouLooking.setText(getString(R.string.you_looking_2));
                if (profileRef != null) {
                    profileRef.child("lookingFor").setValue(getString(R.string.you_looking_2));
                }
                break;
            }

            case 3: {

                etYouLooking.setText(getString(R.string.you_looking_3));
                if (profileRef != null) {
                    profileRef.child("lookingFor").setValue(getString(R.string.you_looking_3));
                }
                break;
            }

            default: {

                break;
            }
        }
    }

    public void selectYouLike(int position) {

        android.app.FragmentManager fm = getFragmentManager();

        InterestedInSelectDialog alert = new InterestedInSelectDialog();

        Bundle b  = new Bundle();
        b.putInt("position", position);

        alert.setArguments(b);
        alert.show(fm, "alert_dialog_select_you_like");
    }

    public void getYouLike(int mLike) {

        youLike = mLike;

        switch (mLike) {

            case 0: {

                etInterestedIn.setText(getString(R.string.you_like_0));
                if (profileRef != null) {
                    profileRef.child("interestedIn").setValue(getString(R.string.you_like_0));
                }
                break;
            }

            case 1: {

                etInterestedIn.setText(getString(R.string.male));
                if (profileRef != null) {
                    profileRef.child("interestedIn").setValue(getString(R.string.male));
                }
                break;
            }

            case 2: {

                etInterestedIn.setText(getString(R.string.female));
                if (profileRef != null) {
                    profileRef.child("interestedIn").setValue(getString(R.string.female));
                }
                break;
            }

            default: {

                break;
            }
        }
    }


    public void selectGender(int position) {

        android.app.FragmentManager fm =getFragmentManager();

        GenderSelectDialog alert = new GenderSelectDialog();

        Bundle b  = new Bundle();
        b.putInt("position", position);

        alert.setArguments(b);
        alert.show(fm, "alert_dialog_select_gender");
    }

    public void setGender(int mSex) {

        gender = mSex;

        if (mSex == 0) {

            if (profileRef != null) {
                profileRef.child("gender").setValue(getResources().getString(R.string.male));
            }
            etGender.setText(getString(R.string.male));

        } else {
            if (profileRef != null) {
                profileRef.child("gender").setValue(getResources().getString(R.string.female));
            }
            etGender.setText(getString(R.string.female));
        }
    }


    @Override
    public void onGenderSelect(int position) {
            setGender(position);
    }

    @Override
    public void onYouLikeSelect(int position) {
        getYouLike(position);
    }

    @Override
    public void onYouLookingSelect(int position) {
        getYouLooking(position);
    }

    @Override
    public void onRelationshipStatusSelect(int position) {
        getRelationshipStatus(position);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.etbirthday:
                if(hasFocus){
                    DatePickerDialog datePickerDialog = new DatePickerDialog(ProfileEditActivity.this,
                            R.style.style_date_picker_dialog
                            , new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                            _year = year;
                            _month = monthOfYear;
                            _day = dayOfMonth;
                            etBirthday.setText(displayDate(year, monthOfYear, dayOfMonth));
                            String stringYear = String.valueOf(year);
                            String stringMonth = monthOfYear < 10 ? "0" + String.valueOf(monthOfYear) : String.valueOf(monthOfYear);
                            String stringDay = dayOfMonth < 10 ? "0" + String.valueOf(dayOfMonth) : String.valueOf(dayOfMonth);

                            String birthday = stringYear + stringMonth + stringDay;
                            profileRef.child("birthday").setValue(birthday);
                        }
                    }, _year, _month, _day);
                    datePickerDialog.setTitle("");
                    datePickerDialog.show();
                }

                break;
            case R.id.etrelationStatus:
                if (hasFocus) {
                    selectRelationshipStatus(relationshipStatus);
                }
                break;
            case R.id.etGender:
                if (hasFocus) {
                    if(etGender.getText().toString().equalsIgnoreCase("MALE"))
                         selectGender(0);
                    else
                        selectGender(1);
                }
                break;
            case R.id.etYouLooking:
                if (hasFocus) {
                    selectYouLooking(youLooking);
                }
                break;
            case R.id.etYouLike:
                if (hasFocus) {
                    selectYouLike(youLike);
                }
                break;

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_add_gallery:
                openImageBottomSheet();
                break;
            case R.id.fab:
                openImageBottomSheet();
                break;

            case R.id.image_profile:
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser == null) {
                    return;
                }
                String uid = firebaseUser.getUid();
                Intent intent = new Intent(ProfileEditActivity.this, FullProfileImageActivity.class);
                intent.putExtra(FullProfileImageActivity.KEY_IMAGE, mainImage);
                intent.putExtra(FullProfileImageActivity.KEY_IMAGE_URL, mainImageUrl);
                intent.putExtra(FullProfileImageActivity.KEY_UID, uid);
                startActivity(intent);
                break;
        }
    }
}
