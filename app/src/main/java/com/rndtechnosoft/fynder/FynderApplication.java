package com.rndtechnosoft.fynder;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.clevertap.android.sdk.ActivityLifecycleCallback;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.exceptions.CleverTapMetaDataNotFoundException;
import com.clevertap.android.sdk.exceptions.CleverTapPermissionsNotSatisfied;
import com.crashlytics.android.Crashlytics;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.rndtechnosoft.fynder.model.event.OnEnterEvent;
import com.rndtechnosoft.fynder.model.event.OnLeaveEvent;
import com.rndtechnosoft.fynder.utility.Ad_Helper;
import com.rndtechnosoft.fynder.utility.Constants;
import com.rndtechnosoft.fynder.utility.GPSTracker;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;


/**
 * Created by Ravi on 11/5/2016.
 */

public class FynderApplication extends Application implements Application.ActivityLifecycleCallbacks {
    private final String TAG = "FynderApplication";

    public static int started;
    public static int stopped;
    private static FynderApplication Instance;
    public static volatile Handler applicationHandler = null;
    private GPSTracker gps;
    private Double lat = 0.000000, lng = 0.000000;
    private String area = "", country = "", city = "";
    private SharedPreferences sharedpreferences;
    private CleverTapAPI cleverTap;

    @Override
    public void onCreate() {
        ActivityLifecycleCallback.register(this);
        super.onCreate();
        CleverTapAPI.setDebugLevel(1);
        Instance=this;
        Log.i(TAG, "On Create");
        applicationHandler = new Handler(getInstance().getMainLooper());
        registerActivityLifecycleCallbacks(this);
        Fabric.with(this, new Crashlytics());
        AppEventsLogger.activateApp(this);
        Fresco.initialize(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        EmojiManager.install(new IosEmojiProvider());
        getLocation();
        initCleverTap();
        Ad_Helper.startGame();
    }

    public void initCleverTap()
    {
        try {
            cleverTap = CleverTapAPI.getInstance(getApplicationContext());
        } catch (CleverTapMetaDataNotFoundException e) {
            // thrown if you haven't specified your CleverTap Account ID or Token in your AndroidManifest.xml
        } catch (CleverTapPermissionsNotSatisfied e) {
            // thrown if you havenâ€™t requested the required permissions in your AndroidManifest.xml
        }
    }

    public CleverTapAPI getCleverTap() {
        return cleverTap;
    }

    public void setCleverTap(CleverTapAPI cleverTap) {
        this.cleverTap = cleverTap;
    }

    public void sendEvent(String key) {
        FynderApplication.getInstance().getCleverTap().event.push(key);
    }

    public void sendEvent(String key, HashMap<String, Object> data) {
        this.cleverTap.event.push(key, data);
    }

    public void sendProfile(HashMap<String, Object> profileUpdate) {
        cleverTap.profile.push(profileUpdate);
    }

    public void sendScreenviwedevent(String screenname, String title){
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.CT_EVEVT_SCREEN_NAME, screenname);
        map.put(Constants.CT_EVEVT_SCREEN_TITLE, title);
        sendEvent(Constants.CT_EVEVT_SCREEN_VIEWED, map);
    }

    public void sendActionevent(String actionName){
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.CT_ACTION_NAME, actionName);
        sendEvent(Constants.CT_ACTIONS, map);
    }

    public void createProfile(String name, String uid, String email, String gender, String birthday, int age,String photo) {
        HashMap<String, Object> profileUpdate = new HashMap<String, Object>();
        profileUpdate.put("Name", name);                  // String
        profileUpdate.put("Identity", uid);                    // String or number
        profileUpdate.put("Email",email);               // Email address of the user
        if(gender.equalsIgnoreCase("male")) {
            profileUpdate.put("Gender","M");                           // Can be either M or F
        }
        else {
            profileUpdate.put("Gender", "F");                           // Can be either M or F
        }
        profileUpdate.put("Married", "N");                          // Can be either Y or N
        profileUpdate.put("DOB", birthday);                       // Date of Birth. Set the Date object to the appropriate value first
        profileUpdate.put("Age", age);                               // Not required if DOB is set
        profileUpdate.put("Photo", photo);    // URL to the Image
// optional fields. controls whether the user will be sent email, push etc.
        profileUpdate.put("MSG-email", true);                      // Disable email notifications
        profileUpdate.put("MSG-push", true);                        // Enable push notifications
        profileUpdate.put("MSG-sms", true);                        // Disable SMS notifications

        //profileUpdate.put("Employed", "Y");                         // Can be either Y or N
        //profileUpdate.put("Education", "Graduate");                 // Can be either Graduate, College or School
//        profileUpdate.put("Phone", phone);                     // Phone (without the country code)
        sendProfile(profileUpdate);
    }

    public static FynderApplication getInstance()
    {
        return Instance;
    }

    public boolean isConnected() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnectedOrConnecting()) {

            return true;
        }

        return false;
    }

    public void getLocation() {

        if (FynderApplication.getInstance().isConnected()) {

            gps = new GPSTracker(this);

            if (gps.canGetLocation()) {

                final double latitude = gps.getLatitude();
                final double longitude = gps.getLongitude();

                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(getApplicationContext(), Locale.ENGLISH);

                try {

                    addresses = geocoder.getFromLocation(latitude, longitude, 1);

                    if (addresses.size() > 0) {

                        FynderApplication.getInstance().setLat(latitude);
                        FynderApplication.getInstance().setLng(longitude);

                        FynderApplication.getInstance().setArea(getCompleteAddressString(latitude,longitude));
                        FynderApplication.getInstance().setCity(addresses.get(0).getLocality());
                        FynderApplication.getInstance().setCountry(addresses.get(0).getCountryName());

//                        Toast.makeText(this, addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName(), Toast.LENGTH_LONG).show();
                    }

                } catch (IOException e) {

                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }


    public void deleteAllSharePrefs(Context applicationContext){
        sharedpreferences = applicationContext.getSharedPreferences(getString(R.string.KEY_PREF_NAME), Context.MODE_PRIVATE);
        sharedpreferences.edit().clear().commit();

        if(FirebaseAuth.getInstance()!=null)
            FirebaseAuth.getInstance().signOut();
        if( LoginManager.getInstance()!=null)
            LoginManager.getInstance().logOut();
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex()-1; i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(", ");
                }
                strAdd = strReturnedAddress.toString();
                Log.e(" Current loction", "" + strReturnedAddress.toString());
            } else {
                Log.e("Current loction", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strAdd;
    }


    public void setLat(Double lat) {

        if (this.lat == null) {

            this.lat = 0.000000;
        }

        this.lat = lat;
    }

    public Double getLat() {

        if (this.lat == null) {

            this.lat = 0.000000;
        }

        return this.lat;
    }

    public void setLng(Double lng) {

        if (this.lng == null) {

            this.lng = 0.000000;
        }

        this.lng = lng;
    }

    public Double getLng() {

        return this.lng;
    }

    public void setCountry(String country) {

        this.country = country;
    }

    public String getCountry() {

        if (this.country == null) {

            this.setCountry("");
        }

        return this.country;
    }

    public void setCity(String city) {

        this.city = city;
    }

    public String getCity() {

        if (this.city == null) {

            this.setCity("");
        }

        return this.city;
    }

    public void setArea(String area) {

        this.area = area;
    }

    public String getArea() {

        if (this.area == null) {

            this.setArea("");
        }

        return this.area;
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.i(TAG, "On Terminate");
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        ++started;
        Log.i(TAG,"On Activity started: "+started);
        if (started == 1) {
            Log.w(TAG, "Online started.... start: "+started+" , stop: "+stopped);
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            sharedpreferences = getSharedPreferences(getString(R.string.KEY_PREF_NAME), Context.MODE_PRIVATE);
            if (firebaseUser != null && sharedpreferences.getBoolean(getString(R.string.USER_LOGGED), false)) {
                connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
                connectedRef.addValueEventListener(onlineListener);
            }
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        ++stopped;
        boolean isApplicationOnBackground = started <= stopped;
        Log.w(TAG, "is Application On Background: " + isApplicationOnBackground+" , start: "+started+" , stop: "+stopped);
        if (isApplicationOnBackground) {
            started=0;
            stopped=0;
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            sharedpreferences = getSharedPreferences(getString(R.string.KEY_PREF_NAME), Context.MODE_PRIVATE);
            if (firebaseUser != null && sharedpreferences.getBoolean(getString(R.string.USER_LOGGED), false)) {
                Log.i(TAG,"Delete online value");
                EventBus.getDefault().post(new OnLeaveEvent(firebaseUser.getUid()));
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("rooms")
                        .child("global").child("participant").child(firebaseUser.getUid());
                userRef.removeValue();
                if(connectedRef != null && onlineListener != null){
                    connectedRef.removeEventListener(onlineListener);
                }
                DatabaseReference lastOnlineRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid()).child("lastOnline");
                lastOnlineRef.setValue(ServerValue.TIMESTAMP);
            }
        }
    }




    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    private DatabaseReference connectedRef;
    private ValueEventListener onlineListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            boolean connected = snapshot.getValue(Boolean.class);
            if (connected) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null) {
                    EventBus.getDefault().post(new OnEnterEvent(firebaseUser.getUid()));
                    DatabaseReference globalRoomRef = FirebaseDatabase.getInstance().getReference("rooms")
                            .child("global").child("participant").child(firebaseUser.getUid());
                    globalRoomRef.setValue(firebaseUser.getUid());
                    globalRoomRef.onDisconnect().removeValue();
                    DatabaseReference lastOnlineRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid()).child("lastOnline");
                    lastOnlineRef.setValue(ServerValue.TIMESTAMP);
                }
                Log.i(TAG, "connected");
            } else {
                Log.i(TAG, "not connected");
            }
        }

        @Override
        public void onCancelled(DatabaseError error) {
            Log.e(TAG, "Listener was cancelled");
        }
    };

}
