package com.rndtechnosoft.fynder.activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.rndtechnosoft.fynder.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Ravi on 3/4/2017.
 */

public class SplashActivity extends AppCompatActivity {


    private String TAG;
    private SharedPreferences sharedpreferences;
    private boolean isMainStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getHash();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        sharedpreferences = getSharedPreferences(getString(R.string.KEY_PREF_NAME), Context.MODE_PRIVATE);
        boolean isLogged = sharedpreferences.getBoolean(getString(R.string.USER_LOGGED), false);
        if (isLogged) {

            if (isMainStarted) {
                return;
            }
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                DatabaseReference globalRoomRef = FirebaseDatabase.getInstance().getReference("rooms")
                        .child("global").child("participant").child(firebaseUser.getUid());
                globalRoomRef.setValue(firebaseUser.getUid());
                globalRoomRef.onDisconnect().removeValue();
                DatabaseReference lastOnlineRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid()).child("lastOnline");
                lastOnlineRef.setValue(ServerValue.TIMESTAMP);
            }
            SplashActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashActivity.this, NearbyHomeActivity.class));
                    finish();
                    isMainStarted = true;
                }
            });
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }


    }


    public void getHash() {

        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.rndtechnosoft.fynder",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                try {
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

}
