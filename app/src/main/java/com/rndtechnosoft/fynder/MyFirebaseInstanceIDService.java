package com.rndtechnosoft.fynder;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.rndtechnosoft.fynder.utility.Token;

/**
 * Created by Ravi on 12/24/2016.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
//    private final String TAG = "FCM";
//    private SharedPreferences sharedpreferences;

    @Override
    public void onTokenRefresh() {
        final String TAG = "FCM";
        SharedPreferences sharedpreferences = getSharedPreferences(getString(R.string.KEY_PREF_NAME), Context.MODE_PRIVATE);
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "FirebaseInstanceIDService => Refreshed token: " + refreshedToken);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(getString(R.string.USER_TOKEN),refreshedToken);
        editor.apply();



        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {
            Log.i(TAG,"Updated");
            String deviceId = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
            Token token = new Token(firebaseUser.getUid(), refreshedToken);
            DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("token").child(deviceId);
            tokenRef.setValue(token);
        }else{
            Log.i(TAG,"User is not login");
        }
    }
}
