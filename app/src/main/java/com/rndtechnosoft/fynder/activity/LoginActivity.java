package com.rndtechnosoft.fynder.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.rndtechnosoft.fynder.FynderApplication;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.dialog.LoginBottomSheetDialog;
import com.rndtechnosoft.fynder.onboarding.PaperOnboardingEngine;
import com.rndtechnosoft.fynder.onboarding.PaperOnboardingPage;
import com.rndtechnosoft.fynder.onboarding.listeners.PaperOnboardingOnChangeListener;
import com.rndtechnosoft.fynder.onboarding.listeners.PaperOnboardingOnRightOutListener;
import com.rndtechnosoft.fynder.utility.Constants;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {//implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
//    private static final int REQUEST_READ_CONTACTS = 0;
    private static final int RC_SIGN_IN = 9001;
    private final String TAG = "LoginActivity";
    private ProgressDialog myProgressDialog;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;
    //Facebook
    private CallbackManager callbackManager;
    private SharedPreferences sharedpreferences;
    private boolean isMainStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding_main_layout);

        FynderApplication.getInstance().sendScreenviwedevent(Constants.CT_LOGIN,"");
        sharedpreferences = getSharedPreferences(getString(R.string.KEY_PREF_NAME), Context.MODE_PRIVATE);
        callbackManager = CallbackManager.Factory.create();
        setupOnBoarding();
        findViewById(R.id.other_options).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LoginBottomSheetDialog().show(getSupportFragmentManager());
            }
        });

        myProgressDialog = new ProgressDialog(this);
        myProgressDialog.setMessage(getString(R.string.loading_message));
        myProgressDialog.setCancelable(false);

        mAuth = FirebaseAuth.getInstance();

//        boolean isLogged = sharedpreferences.getBoolean(getString(R.string.USER_LOGGED), false);
//        if (isLogged) {
//            startMainPage();
//            return;
//        }


        mAuthListener = new FirebaseAuth.AuthStateListener() {
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

                    boolean isLogged = sharedpreferences.getBoolean(getString(R.string.USER_LOGGED), false);
                    if (isLogged) {
                        startMainPage();
                        return;
                    }

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                    ref.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putBoolean(getString(R.string.USER_LOGGED), true);
                                editor.apply();
                                startMainPage();
                                // use "username" already exists
                                // Let the user know he needs to pick another username.
                            } else {
                                // User does not exist. NOW call createUserWithEmailAndPassword
                                // Your previous code here.
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putBoolean(getString(R.string.USER_LOGGED), true);
                                editor.apply();
                                LoginActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        hideProgress();
                                        startActivity(new Intent(LoginActivity.this, ConfirmationActivity.class));
                                        finish();
                                    }
                                });

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });



                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, onConnectionFailedListener)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //Facebook
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                hideProgress();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                hideProgress();
            }
        });

    }

    private void setupOnBoarding() {
        PaperOnboardingEngine engine = new PaperOnboardingEngine(findViewById(R.id.onboardingRootView), getDataForOnboarding(), getApplicationContext());

        engine.setOnChangeListener(new PaperOnboardingOnChangeListener() {
            @Override
            public void onPageChanged(int oldElementIndex, int newElementIndex) {
//                Toast.makeText(getApplicationContext(), "Swiped from " + oldElementIndex + " to " + newElementIndex, Toast.LENGTH_SHORT).show();
            }
        });

        engine.setOnRightOutListener(new PaperOnboardingOnRightOutListener() {
            @Override
            public void onRightOut() {
                // Probably here will be your exit action

//                Toast.makeText(getApplicationContext(), "Swiped out right", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // Just example data for Onboarding
    private ArrayList<PaperOnboardingPage> getDataForOnboarding() {
        // prepare data
        PaperOnboardingPage scr1 = new PaperOnboardingPage("Fynd", "Discover singles in your area now.",
                Color.parseColor("#678FB4"), R.drawable.ic_ob_discover, R.drawable.onboarding_pager_circle_icon);
        PaperOnboardingPage scr2 = new PaperOnboardingPage("Chat", "Setup real dates & Talk to singles around the world.",
                Color.parseColor("#65B0B4"), R.drawable.ic_ob_chat, R.drawable.onboarding_pager_circle_icon);
        PaperOnboardingPage scr3 = new PaperOnboardingPage("Meet", "Meet people looking for somthing real.",
                Color.parseColor("#9B90BC"), R.drawable.ic_ob_meet, R.drawable.onboarding_pager_circle_icon);

        ArrayList<PaperOnboardingPage> elements = new ArrayList<>();
        elements.add(scr1);
        elements.add(scr2);
        elements.add(scr3);
        return elements;
    }



    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        myProgressDialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgress();
    }

    private void showProgress(String message) {
        if (myProgressDialog == null) {
            return;
        }
        myProgressDialog.setMessage(message);
        myProgressDialog.show();
    }

    private void hideProgress() {
        if (myProgressDialog == null) {
            return;
        }
        myProgressDialog.dismiss();
    }


    public void facebookClicked(View view) {
        FynderApplication.getInstance().sendActionevent(Constants.CT_FACEBOOK);
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email", "user_friends"));
        showProgress(getString(R.string.login_progress_facebook));
    }

    public void googlePlusClicked(View view) {
        FynderApplication.getInstance().sendActionevent(Constants.CT_GOOGLE);
        if (!mGoogleApiClient.isConnecting()) {
            showProgress(getString(R.string.login_progress_google));
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    }

    private GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener =
            new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    Log.e(TAG, "onConnectionFailed:" + connectionResult);
                    Toast.makeText(LoginActivity.this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
                }
            };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
//                Toast.makeText(this, getString(R.string.login_failed_google), Toast.LENGTH_SHORT).show();
                hideProgress();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, getString(R.string.login_failed_google),
                                    Toast.LENGTH_SHORT).show();
                            hideProgress();
                        }
//                        hideProgress();

                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, getString(R.string.login_failed_facebook),
                                    Toast.LENGTH_SHORT).show();
                        }

//                        hideProgress();

                        // ...
                    }
                });
    }


    private void startMainPage() {
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
            //register a token
//            String token = sharedpreferences.getString(getString(R.string.USER_TOKEN), null);
//            if (token != null) {
//                DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("token")
//                        .child(firebaseUser.getUid());
//                tokenRef.setValue(token);
//            }
        }
        LoginActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideProgress();
                startActivity(new Intent(LoginActivity.this, NearbyHomeActivity.class));
                finish();
                isMainStarted = true;
            }
        });
    }
}

