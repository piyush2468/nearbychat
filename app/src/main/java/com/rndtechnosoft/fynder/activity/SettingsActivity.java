package com.rndtechnosoft.fynder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.adapter.SpinnerAdapter;
import com.rndtechnosoft.fynder.dialog.BlockedUserDialog;
import com.rndtechnosoft.fynder.dialog.RoomSettingDialog;
import com.rndtechnosoft.fynder.model.Selector;
import com.rndtechnosoft.fynder.model.User;
import com.rndtechnosoft.fynder.model.UserImage;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    private final String TAG = "SettingsActivity.log";
    private View layoutProfile;
    private SimpleDraweeView imageProfile;
    private TextView textDisplayName;
    private TextView textEmail;
    private Spinner spinnerFirstOpen;
    private SpinnerAdapter firstOpenAdapter;
    private List<Selector> selectorList = new ArrayList<>();
    private DatabaseReference profileRef;
    private DatabaseReference settingRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        layoutProfile = findViewById(R.id.layout_setting_profile);
        imageProfile = (SimpleDraweeView) findViewById(R.id.image_profile);
        RoundingParams circle = RoundingParams.asCircle()
                .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY);
        imageProfile.getHierarchy().setRoundingParams(circle);
        imageProfile.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
        imageProfile.getHierarchy().setPlaceholderImage(R.drawable.ic_profile_default);
        textDisplayName = (TextView) findViewById(R.id.text_display_name);
        textEmail = (TextView) findViewById(R.id.text_email);
        spinnerFirstOpen = (Spinner) findViewById(R.id.spinner_first_open);
        firstOpenAdapter = new SpinnerAdapter(this, selectorList);
        String[] itemFirstOpens = getResources().getStringArray(R.array.first_open_chooser);
        for (int i = 0; i < itemFirstOpens.length; i++) {
            Selector selector = new Selector();
            selector.setId(i);
            selector.setName(itemFirstOpens[i]);
            selectorList.add(selector);
        }
        spinnerFirstOpen.setAdapter(firstOpenAdapter);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            profileRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());
            settingRef = FirebaseDatabase.getInstance().getReference("settings").child(firebaseUser.getUid());
        } else {
            finish();
        }

        layoutProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingsActivity.this.startActivity(new Intent(SettingsActivity.this, ProfileEditActivity.class));
            }
        });

        findViewById(R.id.view_blocked_users).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new BlockedUserDialog(SettingsActivity.this).show();
            }
        });
        findViewById(R.id.view_room_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new RoomSettingDialog(SettingsActivity.this, getString(R.string.room_setting)).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (profileRef != null) {
            profileRef.addValueEventListener(profileListener);
        }
        if (settingRef != null) {
            settingRef.addValueEventListener(settingListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (profileRef != null) {
            profileRef.removeEventListener(profileListener);
        }
        if (settingRef != null) {
            settingRef.removeEventListener(settingListener);
        }
    }

    private ValueEventListener settingListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for (DataSnapshot election : dataSnapshot.getChildren()) {
                if (election.getKey().equals("firstOpen")) {
                    int position = election.getValue(Integer.class);
                    Log.i(TAG, "First open from listener: " + position);
                    spinnerFirstOpen.setSelection(position);

                }
            }
            spinnerFirstOpen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    Log.i(TAG,"On Selected => setting open tab: "+position);
                    for (Selector selector : selectorList) {
                        selector.setSelected(false);
                    }
                    Selector model = selectorList.get(position);
                    model.setSelected(true);
                    DatabaseReference ref = settingRef.child("firstOpen");
                    ref.setValue(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ValueEventListener profileListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            User model = dataSnapshot.getValue(User.class);
            Log.i(TAG, "Name: " + model.getName());
            textDisplayName.setText(model.getName());
            textEmail.setText(model.getEmail());
            layoutProfile.setVisibility(View.VISIBLE);
            for (DataSnapshot election : dataSnapshot.getChildren()) {
                if (election.getKey().equals("images")) {
                    String key = model.getImageUrl() == null ? "default" : model.getImageUrl();
                    DataSnapshot defaultImage = election.child(key);
                    if (defaultImage.exists()) {
                        UserImage userImage = defaultImage.getValue(UserImage.class);
                        imageProfile.setImageURI(userImage.getThumbPic());
                        break;
                    }
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    //Do not call onCreate on MainActivity
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
