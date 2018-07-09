package com.rndtechnosoft.fynder.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.model.User;
import com.rndtechnosoft.fynder.model.UserImage;
import com.rndtechnosoft.fynder.utility.MyDateUtil;
import com.rndtechnosoft.fynder.utility.MyImageUtil;
import com.rndtechnosoft.fynder.utility.image.bottompicker.ImageBottomPicker;
import com.rndtechnosoft.fynder.utility.listener.OnSuccessUploadListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;

/**
 * Created by Ravi on 11/6/2016.
 */

public class RegisterActivity extends AppCompatActivity {

    private final String TAG = "RegisterActivity";
    private final int OPEN_IMAGE = 536;

    private AutoCompleteTextView inputEmail;
    private AutoCompleteTextView inputName;
    private EditText inputPassword;
    private SimpleDraweeView imageProfile;
    private ProgressDialog myProgressDialog;
    private TextInputEditText inputBirthday;
    private TextView genderMale;
    private TextView genderFemale;
    private AppCompatCheckBox checkBoxTerm;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    private String uploadPath;
    private String gender;
    private String birthday;
    private int _year;
    private int _month;
    private int _day;
    private long curTimeStamp = System.currentTimeMillis();
    private SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Log.i(TAG,"OnCreate => timestamp: "+curTimeStamp);
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        sharedpreferences = getSharedPreferences(getString(R.string.KEY_PREF_NAME), Context.MODE_PRIVATE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        inputName = (AutoCompleteTextView) findViewById(R.id.name);
        inputEmail = (AutoCompleteTextView) findViewById(R.id.email);
        imageProfile = (SimpleDraweeView) findViewById(R.id.image_profile);
        inputPassword = (EditText) findViewById(R.id.password);
        genderMale = (TextView) findViewById(R.id.gender_male);
        genderFemale = (TextView) findViewById(R.id.gender_female);
        inputBirthday = (TextInputEditText) findViewById(R.id.input_birthday);
        checkBoxTerm = (AppCompatCheckBox) findViewById(R.id.check_box_term_and_condition);
        TextView textTerm = (TextView) findViewById(R.id.text_term_and_condition);
        String udata = getString(R.string.term_and_condition_clickable);
        SpannableString content = new SpannableString(udata);
        content.setSpan(new UnderlineSpan(), 0, udata.length(), 0);
        textTerm.setText(content);
        imageProfile = (SimpleDraweeView) findViewById(R.id.image_profile);
        inputPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });
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
        myProgressDialog = new ProgressDialog(this);
        myProgressDialog.setMessage(getString(R.string.loading_message));
        myProgressDialog.setCancelable(false);
        Button buttonRegister = (Button) findViewById(R.id.register_button);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"Button clicked..!!");
                attemptRegister();
            }
        });
        textTerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(RegisterActivity.this)
                        .setView(LayoutInflater.from(RegisterActivity.this).inflate(R.layout.term_and_conditions, null))
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

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        myProgressDialog.dismiss();
    }


    private void attemptRegister(){
        if(uploadPath == null) {
            Toast.makeText(this, getString(R.string.image_profile_mandatory), Toast.LENGTH_SHORT).show();
            return;
        }
        inputName.setError(null);
        inputPassword.setError(null);
        inputEmail.setError(null);

        String displayName = inputName.getText().toString();
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if(TextUtils.isEmpty(displayName)){
            inputName.setError(getString(R.string.error_field_required));
            focusView = inputName;
            cancel = true;
        }else if (TextUtils.isEmpty(email)) {
            inputEmail.setError(getString(R.string.error_field_required));
            focusView = inputEmail;
            cancel = true;
        }else if(!email.contains("@")){
            inputEmail.setError(getString(R.string.error_invalid_email));
            focusView = inputEmail;
            cancel = true;
        }else if(TextUtils.isEmpty(password)){
            inputPassword.setError(getString(R.string.error_field_required));
            focusView = inputPassword;
            cancel = true;
        }else if(password.length() < 4){
            inputPassword.setError(getString(R.string.error_invalid_password));
            focusView = inputPassword;
            cancel = true;
        }

        if(cancel){
            focusView.requestFocus();
        }else{
            if (gender == null) {
                Toast.makeText(this, getString(R.string.error_no_gender), Toast.LENGTH_SHORT).show();
                return;
            }
            if (birthday == null) {
                inputBirthday.setError(getString(R.string.error_no_birthday_title));
                new AlertDialog.Builder(this)
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

            if (MyDateUtil.getDiffYears(curTimeStamp, birthday) < 13) {
                Toast.makeText(this, getString(R.string.error_under_age),Toast.LENGTH_SHORT).show();
                return;
            }

            if(!checkBoxTerm.isChecked()){
                Toast.makeText(this, getString(R.string.error_unchecked_agreements),Toast.LENGTH_SHORT).show();
                return;
            }

            myProgressDialog.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, R.string.login_error_verification,
                                        Toast.LENGTH_SHORT).show();
                                RegisterActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        myProgressDialog.dismiss();
                                    }
                                });
                                return;
                            }
                            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                // User is signed in
                                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                                Log.i(TAG,"User name: "+user.getDisplayName());

                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(inputName.getText().toString())
                                        .build();

                                FirebaseUser userProfile = FirebaseAuth.getInstance().getCurrentUser();
                                if(userProfile != null) {
                                    userProfile.updateProfile(profileUpdates)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "User profile updated.");
                                                        Log.i(TAG, "User name: " + user.getDisplayName());
                                                        // pushing user to 'users' node using the userId
                                                        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
                                                        final DatabaseReference databaseReference = mDatabase.child(user.getUid());
                                                        // creating user object
                                                        User userModel = new User(inputName.getText().toString(), user.getEmail());

//                                            databaseReference.addValueEventListener(valueEventListener);
                                                        databaseReference.setValue(userModel);
                                                        if(uploadPath != null) {
                                                            String tempKey = "default";
                                                            final String uid = user.getUid();
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
                                                                    StorageReference profileImagesRef = storageRef.child(uid + "/" + key + ".jpg");
                                                                    UploadTask uploadTask = profileImagesRef.putFile(Uri.fromFile(new File(uploadPath)));
                                                                    uploadTask.addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Log.e(TAG, "Error upload thumb image: " + e.getMessage());
                                                                            myProgressDialog.dismiss();
                                                                        }
                                                                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                        @SuppressWarnings("VisibleForTests")
                                                                        @Override
                                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                            final Uri thumbUrl = taskSnapshot.getDownloadUrl();
                                                                            Log.i(TAG, "Success to Upload thumbnail, url: " + thumbUrl);
                                                                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                                                            Log.i(TAG, "Success to Upload, url: " + downloadUrl);
                                                                            UserImage userImage = new UserImage(thumbUrl.toString(), downloadUrl.toString());
                                                                            Map<String, Object> postValues = userImage.toMap();
//                            profileRef.child("images").child(tempKey).setValue(userImage);
                                                                            databaseReference.child("images").child(key).updateChildren(postValues);
                                                                            databaseReference.child("imageUrl").setValue(key);
                                                                            nextPage();
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
                                                        }else{
                                                            nextPage();
                                                        }
                                                    }
                                                }
                                            });
                                }
                            } else {
                                // User is signed out
                                Log.d(TAG, "onAuthStateChanged:signed_out");
                            }


                            // ...
                        }
                    });


        }
    }

//    private ValueEventListener valueEventListener = new ValueEventListener() {
//        @Override
//        public void onDataChange(DataSnapshot dataSnapshot) {
//            User user = dataSnapshot.getValue(User.class);
//
//            Log.d(TAG, "Database -> Display name: " + user.getName() + ", Email: " + user.getEmail());
//
//
//
//
//        }
//
//        @Override
//        public void onCancelled(DatabaseError databaseError) {
//            Log.e(TAG, "Failed to read value.", databaseError.toException());
//        }
//    };
//
    private void nextPage(){
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
//            String token = sharedpreferences.getString(getString(R.string.USER_TOKEN), null);
//            if (token != null) {
//                DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("token")
//                        .child(firebaseUser.getUid());
//                tokenRef.setValue(token);
//            }
        }
        RegisterActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myProgressDialog.dismiss();
                Intent intent = new Intent(RegisterActivity.this, NearbyHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
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
                        if(MyImageUtil.isImageFile(new File(path))) {
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
            attemptRegister();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String displayDate(int year, int month, int day) {
        String month_array[] = getResources().getStringArray(R.array.month);
        return month_array[month] + " " + String.valueOf(day) + ", " + String.valueOf(year);
    }

    private void setMaleGender() {
        gender = "MALE";
        genderMale.setTextColor(ContextCompat.getColor(this, R.color.color_white));
        genderMale.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_male_pressed));
        genderFemale.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        genderFemale.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_female));
    }

    private void setFemaleGender() {
        gender = "FEMALE";
        genderMale.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        genderMale.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_male));
        genderFemale.setTextColor(ContextCompat.getColor(this, R.color.color_white));
        genderFemale.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_female_pressed));
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

    //Do not call onCreate on Parent Activity
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
