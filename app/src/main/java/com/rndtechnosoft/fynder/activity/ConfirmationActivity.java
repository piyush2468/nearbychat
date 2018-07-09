package com.rndtechnosoft.fynder.activity;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.UriUtil;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rndtechnosoft.fynder.FynderApplication;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.model.User;
import com.rndtechnosoft.fynder.model.UserImage;
import com.rndtechnosoft.fynder.utility.Constants;
import com.rndtechnosoft.fynder.utility.MyDateUtil;
import com.rndtechnosoft.fynder.utility.MyImageUtil;
import com.rndtechnosoft.fynder.utility.image.bottompicker.ImageBottomPicker;
import com.rndtechnosoft.fynder.utility.listener.OnSuccessUploadListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;

/**
 * Created by Ravi on 3/3/2017.
 */

public class ConfirmationActivity extends AppCompatActivity {
    private final String TAG = "Confirmation";
    private final int OPEN_IMAGE = 536;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private FirebaseAuth mAuth;
    private DatabaseReference profileRef;
    private DatabaseReference connectedRef;
    private DatabaseReference timeRef;
    private StorageReference storageRef;
    private SharedPreferences sharedpreferences;
    private SimpleDraweeView imageProfile;
    private ProgressBar progressBar;
    private AutoCompleteTextView inputName;
    private AppCompatCheckBox checkBoxTerm;
    private TextView genderMale;
    private TextView genderFemale;
    private TextInputEditText inputBirthday;
    private boolean needUpload = true;
    private String uploadPath;
    private String uploadUrl;
    private String gender;
    private String birthday;
    private int _year;
    private int _month;
    private int _day;
    private boolean isConnected;
    private long curTimeStamp;
    private ProgressDialog myProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);
        checkLocationPermission(this);
        FynderApplication.getInstance().sendScreenviwedevent(Constants.CT_LOGIN2,"");
//      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sharedpreferences = getSharedPreferences(getString(R.string.KEY_PREF_NAME), Context.MODE_PRIVATE);
        storageRef = FirebaseStorage.getInstance().getReference();
        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        myProgressDialog = new ProgressDialog(this);
        myProgressDialog.setMessage(getString(R.string.loading_message));
        myProgressDialog.setCancelable(false);
        inputName = (AutoCompleteTextView) findViewById(R.id.name);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imageProfile = (SimpleDraweeView) findViewById(R.id.image_profile);
        genderMale = (TextView) findViewById(R.id.gender_male);
        genderFemale = (TextView) findViewById(R.id.gender_female);
        inputBirthday = (TextInputEditText) findViewById(R.id.input_birthday);
        checkBoxTerm = (AppCompatCheckBox) findViewById(R.id.check_box_term_and_condition);
        TextView textTerm = (TextView) findViewById(R.id.text_term_and_condition);
        String udata = getString(R.string.term_and_condition_clickable);
        SpannableString content = new SpannableString(udata);
        content.setSpan(new UnderlineSpan(), 0, udata.length(), 0);
        textTerm.setText(content);
        mAuth = FirebaseAuth.getInstance();
        RoundingParams circle = RoundingParams.asCircle()
                .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY);
        imageProfile.getHierarchy().setRoundingParams(circle);
        imageProfile.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.FIT_XY);
        imageProfile.getHierarchy().setPlaceholderImage(R.drawable.ic_profile_default);
        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageBottomSheet();
            }
        });
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageBottomSheet();
            }
        });
        findViewById(R.id.next_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FynderApplication.getInstance().sendActionevent(Constants.CT_LOGIN_NEXT);
                attemptLogin();
            }
        });
        textTerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(ConfirmationActivity.this)
                        .setView(LayoutInflater.from(ConfirmationActivity.this).inflate(R.layout.term_and_conditions, null))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create().show();
            }
        });
        genderMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMaleGender();
            }
        });
        genderFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFemaleGender();
            }
        });
        inputBirthday.setKeyListener(null);
        inputBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDateChooser();
            }
        });
    }


    private void openDateChooser() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                R.style.style_date_picker_dialog
                , new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                _year = year;
                _month = monthOfYear;
                _day = dayOfMonth;
                inputBirthday.setText(displayDate(year, monthOfYear, dayOfMonth));
                String stringYear = String.valueOf(year);
                String stringMonth = monthOfYear < 10 ? "0" + String.valueOf(monthOfYear) : String.valueOf(monthOfYear);
                String stringDay = dayOfMonth < 10 ? "0" + String.valueOf(dayOfMonth) : String.valueOf(dayOfMonth);

                birthday = stringYear + stringMonth + stringDay;
                Log.i(TAG, "Selected date: " + birthday);
            }
        }, _year, _month, _day);
        datePickerDialog.setTitle("");
        datePickerDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        connectedRef.addValueEventListener(onlineListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        curTimeStamp = 0;
        mAuth.removeAuthStateListener(mAuthListener);
        connectedRef.removeEventListener(onlineListener);
        if (profileRef != null) {
            profileRef.removeEventListener(profileListener);
        }
        if (timeRef != null) {
            timeRef.removeEventListener(curTimeListener);
            timeRef.removeValue();
        }
    }

    private FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                Log.i(TAG, "Display name: " + user.getDisplayName());
                Log.i(TAG, "Picture: " + user.getPhotoUrl());
                Log.i(TAG, "Email: " + user.getEmail());
                Log.i(TAG, "Provider ID: " + user.getProviderId() + " , providers: " + user.getProviders());
                inputName.setText(user.getDisplayName());
                profileRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
                timeRef = FirebaseDatabase.getInstance().getReference("currentTime").child(user.getUid());
                profileRef.addValueEventListener(profileListener);
                timeRef.addValueEventListener(curTimeListener);
                timeRef.setValue(ServerValue.TIMESTAMP);
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out");
            }
        }
    };

    private ValueEventListener profileListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            User model = dataSnapshot.getValue(User.class);
            if (model != null) {
                Log.i(TAG, "Name: " + model.getName() + " , gender: " + model.getGender());
                inputName.setText(model.getName());
                if (uploadPath == null) {
                    for (DataSnapshot election : dataSnapshot.getChildren()) {
                        if (election.getKey().equals("images")) {
                            String key = model.getImageUrl() == null ? "default" : model.getImageUrl();
                            DataSnapshot defaultImage = election.child(key);
                            Log.i(TAG, "Main image is exist: " + defaultImage.exists());
                            if (defaultImage.exists()) {
                                UserImage userImage = defaultImage.getValue(UserImage.class);
                                DraweeController controller = Fresco.newDraweeControllerBuilder()
                                        .setLowResImageRequest(ImageRequest.fromUri(userImage.getThumbPic()))
                                        .setImageRequest(ImageRequest.fromUri(userImage.getOriginalPic()))
                                        .setControllerListener(controllerListener)
                                        .build();
                                imageProfile.setController(controller);
                                needUpload = false;
                                break;
                            }
                        }
                    }
                }
                if (model.getBirthday() != null) {
                    birthday = model.getBirthday();
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
                        inputBirthday.setText(displayDate(yearInt, monthInt, dayInt));
                        _year = yearInt;
                        _month = monthInt;
                        _day = dayInt;

                    } catch (Exception e) {
                        Log.e(TAG, "Error parse date: " + e.getMessage());
                    }
                }
                if (model.getGender() != null) {
                    if (model.getGender().equalsIgnoreCase(getResources().getString(R.string.male))) {
                        setMaleGender();
                    } else if (model.getGender().equalsIgnoreCase(getResources().getString(R.string.female))) {
                        setFemaleGender();
                    }
                }
            }
            if (needUpload && uploadPath == null) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String urlHighImage = "";
                String urlLowImage = "";
                String highSize = "500";
                String lowSize = "75";
//                    // find the Facebook profile and get the user's id
                for (UserInfo profile : user.getProviderData()) {
                    // check if the provider id matches "facebook.com"
                    if (profile.getProviderId().equals("facebook.com")) {
                        String facebookUserId = profile.getUid();
                        // construct the URL to the profile picture, with a custom height
                        // alternatively, use '?type=small|medium|large' instead of ?height=
                        String photoHighUrl = "https://graph.facebook.com/" + facebookUserId + "/picture?height=" + highSize;
                        String photoLowUrl = "https://graph.facebook.com/" + facebookUserId + "/picture?height=" + lowSize;
                        Log.i(TAG, "Facebook, larger picture: " + photoHighUrl);
                        urlHighImage = photoHighUrl;
                        urlLowImage = photoLowUrl;
                        break;
                    } else if (profile.getProviderId().equals("google.com")) {
                        String googleId = profile.getUid();
                        Log.i(TAG, "Google, uid: " + googleId + " , url profile: " + profile.getPhotoUrl());
                        String imageUrl = profile.getPhotoUrl() == null ? "" : profile.getPhotoUrl().toString();
                        imageUrl = !imageUrl.isEmpty() && !imageUrl.contains("AAAAAAAAAAA") ? imageUrl : "";
                        if (!imageUrl.isEmpty() && imageUrl.contains("s96")) {
                            urlHighImage = imageUrl.replace("s96", "s" + highSize);
                            urlLowImage = imageUrl.replace("s96", "s" + lowSize);
                        }
                        Log.i(TAG, "Image Url: " + imageUrl);
                        break;
                    }
                }

                if (!urlHighImage.isEmpty()) {
                    uploadUrl = urlHighImage;
                    Log.i(TAG, " Upload url: " + uploadUrl);
                    DraweeController controller = Fresco.newDraweeControllerBuilder()
                            .setLowResImageRequest(ImageRequest.fromUri(urlLowImage))
                            .setImageRequest(ImageRequest.fromUri(urlHighImage))
                            .setControllerListener(controllerListener)
                            .build();
                    imageProfile.setController(controller);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void attemptLogin() {
        inputName.setError(null);
        inputBirthday.setError(null);
        String name = inputName.getText().toString();
        if (needUpload && uploadUrl == null && uploadPath == null) {
            Toast.makeText(this, getString(R.string.image_profile_mandatory), Toast.LENGTH_SHORT).show();
            return;
        }
        if (name.trim().length() == 0) {
            inputName.setError(getString(R.string.error_field_required));
            inputName.requestFocus();
            return;
        }
        if (name.trim().length() <= 3) {
            inputName.setError(getString(R.string.error_name_too_low));
            inputName.requestFocus();
            return;
        }
        if (gender == null) {
            Toast.makeText(this, getString(R.string.error_no_gender), Toast.LENGTH_SHORT).show();
            return;
        }
        if (birthday == null) {
            inputBirthday.setError(getString(R.string.error_no_birthday_title));
            new AlertDialog.Builder(ConfirmationActivity.this)
                    .setTitle(getString(R.string.error_no_birthday_title))
                    .setMessage(getString(R.string.error_no_birthday_message))
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            openDateChooser();
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();
            return;
        }

        if (curTimeStamp == 0) {
            Toast.makeText(this, getString(R.string.error_internet), Toast.LENGTH_SHORT).show();
            return;
        }

        if (MyDateUtil.getDiffYears(curTimeStamp, birthday) < 13) {
            Toast.makeText(this, getString(R.string.error_under_age), Toast.LENGTH_SHORT).show();
            return;
        }

        if (MyDateUtil.getDiffYears(curTimeStamp, birthday) > 100) {
            Toast.makeText(this, getString(R.string.error_too_old_age), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!checkBoxTerm.isChecked()) {
            Toast.makeText(this, getString(R.string.error_unchecked_agreements), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isConnected) {
            Toast.makeText(this, getString(R.string.error_internet), Toast.LENGTH_SHORT).show();
            return;
        }



        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_SHORT).show();
            return;
        }
        myProgressDialog.show();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        mDatabase.child(user.getUid()).child("name").setValue(name);
        mDatabase.child(user.getUid()).child("email").setValue(user.getEmail());
        mDatabase.child(user.getUid()).child("gender").setValue(gender);
        mDatabase.child(user.getUid()).child("birthday").setValue(birthday);
        mDatabase.child(user.getUid()).child("latitue").setValue(FynderApplication.getInstance().getLat());
        mDatabase.child(user.getUid()).child("longitude").setValue(FynderApplication.getInstance().getLng());
        mDatabase.child(user.getUid()).child("location").setValue(FynderApplication.getInstance().getLat()+","+ FynderApplication.getInstance().getLng());
        mDatabase.child(user.getUid()).child("address").setValue(FynderApplication.getInstance().getArea());
        mDatabase.child(user.getUid()).child("city").setValue(FynderApplication.getInstance().getCity());
        mDatabase.child(user.getUid()).child("country").setValue(FynderApplication.getInstance().getCountry());
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        int age = birthday != null ?
                MyDateUtil.getDiffYears(System.currentTimeMillis(), birthday) : 0;
        FynderApplication.getInstance().createProfile(name,user.getUid(),user.getEmail(),gender,birthday,age,user.getPhotoUrl().toString());
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.updateProfile(profileUpdates);
        }
        if (uploadPath != null) {
            uploadImageFromPath();
        } else if (needUpload) {
            uploadImageFromUrl();
        } else {
            startMainPage();
        }
    }



    private void uploadImageFromPath() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }
//        if (uploadPath == null || uploadPath.isEmpty()) {
//            Toast.makeText(this, getString(R.string.error_upload_image), Toast.LENGTH_SHORT).show();
//            startMainPage();
//            return;
//        }
        String uid = firebaseUser.getUid();
        String tempKey = needUpload ? "default" : profileRef.child("images").push().getKey();
        StorageReference profileImagesRef = storageRef.child(uid + "/" + tempKey + "_thumb.jpg");
        Bitmap bitmap = BitmapFactory.decodeFile(uploadPath);
        Bitmap thumbnail = Bitmap.createScaledBitmap(bitmap, 90, 90, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] thumbData = baos.toByteArray();
        UploadTask uploadThumbnailTask = profileImagesRef.putBytes(thumbData);
        OnSuccessUploadListener onSuccessUpload = new OnSuccessUploadListener(tempKey) {
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
                UploadTask uploadTask = profileImagesRef.putFile(Uri.fromFile(new File(uploadPath)));
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
                        profileRef.child("imageUrl").setValue(key);
                        startMainPage();
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

    private void uploadImageFromUrl() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        if (uploadUrl == null || uploadUrl.isEmpty()) {
//            Toast.makeText(this, getString(R.string.error_upload_image), Toast.LENGTH_SHORT).show();
            startMainPage();
            return;
        }
        final String uid = user.getUid();
        Log.i(TAG, "Upload URL: " + uploadUrl);
        ImageRequest imageRequest = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(uploadUrl))
                .setProgressiveRenderingEnabled(true)
                .build();
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>>
                dataSource = imagePipeline.fetchDecodedImage(imageRequest, this);

        dataSource.subscribe(new BaseBitmapDataSubscriber() {
            @Override
            public void onNewResultImpl(@Nullable final Bitmap bitmap) {
                // You can use the bitmap in only limited ways
                // No need to do any cleanup.
                Log.i(TAG, "Download -> FINISH = bitmap is null: " + (bitmap == null));

                if (bitmap == null) {
                    myProgressDialog.dismiss();
                    return;
                }

                StorageReference profileImagesRef = storageRef.child(uid + "/default_thumb.jpg");
                Bitmap thumbnail = Bitmap.createScaledBitmap(bitmap, 90, 90, true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                UploadTask uploadThumbnailTask = profileImagesRef.putBytes(data);
                uploadThumbnailTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        myProgressDialog.dismiss();
                        Log.e(TAG, "Error upload thumbnail => " + exception.getMessage());
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    @SuppressWarnings("VisibleForTests")
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        final Uri thumbUrl = taskSnapshot.getDownloadUrl();
                        Log.i(TAG, "Success to Upload thumbnail, url: " + thumbUrl);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] data = baos.toByteArray();
                        StorageReference profileImagesRef = storageRef.child(uid + "/default.jpg");
                        UploadTask uploadThumbnailTask = profileImagesRef.putBytes(data);
                        uploadThumbnailTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                Log.e(TAG, "Error upload original => " + exception.getMessage());
                                myProgressDialog.dismiss();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                Log.i(TAG, "Success to Upload, url: " + downloadUrl);
                                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
                                UserImage userImage = new UserImage(thumbUrl.toString(), downloadUrl.toString());
                                Map<String, Object> postValues = userImage.toMap();
//                                mDatabase.child(uid).child("images").child("default").setValue(userImage);
                                mDatabase.child(uid).child("images").child("default").updateChildren(postValues);
                                startMainPage();
                            }
                        });
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

    private ValueEventListener curTimeListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() != null) {
                curTimeStamp = dataSnapshot.getValue(Long.class);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ControllerListener<ImageInfo> controllerListener = new ControllerListener<ImageInfo>() {
        @Override
        public void onSubmit(String id, Object callerContext) {

        }

        @Override
        public void onFinalImageSet(
                String id,
                @Nullable ImageInfo imageInfo,
                @Nullable Animatable anim) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                }
            });

        }

        @Override
        public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }

        @Override
        public void onIntermediateImageFailed(String id, Throwable throwable) {

        }

        @Override
        public void onFailure(String id, Throwable throwable) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }

        @Override
        public void onRelease(String id) {

        }
    };

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
                            uploadPath = path;
                            Uri uriDefault = new Uri.Builder()
                                    .scheme(UriUtil.LOCAL_FILE_SCHEME) // "file"
                                    .path(uploadPath)
                                    .build();
                            imageProfile.setImageURI(uriDefault);
                        }
                    }
                })
                .create().show(getSupportFragmentManager());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "On Create option menu");
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_done) {
            attemptLogin();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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
        } else if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // location-related task you need to do.
                if (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    FynderApplication.getInstance().getLocation();
                }

            } else {

                // permission denied, boo! Disable the
                // functionality that depends on this permission.

            }
            return;
        }

    }

    private void startMainPage() {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(getString(R.string.USER_LOGGED), true);
        editor.apply();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            DatabaseReference globalRoomRef = FirebaseDatabase.getInstance().getReference("rooms")
                    .child("global").child("participant").child(firebaseUser.getUid());
            globalRoomRef.setValue(firebaseUser.getUid());
            globalRoomRef.onDisconnect().removeValue();
            DatabaseReference lastOnlineRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid()).child("lastOnline");
            lastOnlineRef.setValue(ServerValue.TIMESTAMP);
            //register a token
            String token = sharedpreferences.getString(getString(R.string.USER_TOKEN), null);
            if (token != null) {
                DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("token")
                        .child(firebaseUser.getUid());
                tokenRef.setValue(token);
            }
        }
        ConfirmationActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myProgressDialog.dismiss();
                startActivity(new Intent(ConfirmationActivity.this, NearbyHomeActivity.class));
                finish();
            }
        });
    }

    private ValueEventListener onlineListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            isConnected = dataSnapshot.getValue(Boolean.class);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private String displayDate(int year, int month, int day) {
        String month_array[] = getResources().getStringArray(R.array.month);
        return month_array[month] + " " + String.valueOf(day) + ", " + String.valueOf(year);
    }

    private void setMaleGender() {
        gender = getResources().getString(R.string.male);
        genderMale.setTextColor(ContextCompat.getColor(this, R.color.color_white));
        genderMale.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_male_pressed));
        genderFemale.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        genderFemale.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_female));
    }

    private void setFemaleGender() {
        gender = getResources().getString(R.string.female);
        genderMale.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        genderMale.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_male));
        genderFemale.setTextColor(ContextCompat.getColor(this, R.color.color_white));
        genderFemale.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_female_pressed));
    }

    //Do not call onCreate on Parent Activity
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    public boolean checkLocationPermission(final Activity activity) {
        if (ContextCompat.checkSelfPermission(activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(activity,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

}
