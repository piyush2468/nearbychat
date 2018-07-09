package com.rndtechnosoft.fynder.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.rndtechnosoft.fynder.R;

/**
 * Created by Ravi on 3/6/2017.
 */

public class Login2Activity extends AppCompatActivity {
    private final String TAG = "Login2Activity";
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private ProgressDialog myProgressDialog;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedpreferences = getSharedPreferences(getString(R.string.KEY_PREF_NAME), Context.MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        myProgressDialog = new ProgressDialog(this);
        myProgressDialog.setMessage(getString(R.string.loading_message));
        findViewById(R.id.email_sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    private void attemptLogin() {
//        if (mAuthTask != null) {
//            return;
//        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
//            showProgress(true);
//            mAuthTask = new UserLoginTask(email, password);
//            mAuthTask.execute((Void) null);
            myProgressDialog.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "signInWithEmail:failed", task.getException());
                                Toast.makeText(Login2Activity.this, R.string.login_failed,
                                        Toast.LENGTH_SHORT).show();
                                myProgressDialog.dismiss();
                            } else {
                                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (firebaseUser != null) {
                                    DatabaseReference globalRoomRef = FirebaseDatabase.getInstance().getReference("rooms")
                                            .child("global").child("participant").child(firebaseUser.getUid());
                                    globalRoomRef.setValue(firebaseUser.getUid());
                                    globalRoomRef.onDisconnect().removeValue();
                                    DatabaseReference lastOnlineRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid()).child("lastOnline");
                                    lastOnlineRef.setValue(ServerValue.TIMESTAMP);
                                    //register a token
//                                    String token = sharedpreferences.getString(getString(R.string.USER_TOKEN), null);
//                                    if (token != null) {
//                                        DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("token")
//                                                .child(firebaseUser.getUid());
//                                        tokenRef.setValue(token);
//                                    }
                                }
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putBoolean(getString(R.string.USER_LOGGED), true);
                                editor.apply();
                                Intent intent = new Intent(Login2Activity.this, NearbyHomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                            }

                            // ...
                        }
                    });
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    public void registerClicked(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    //    private FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
//        @Override
//        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//            FirebaseUser user = firebaseAuth.getCurrentUser();
//            if (user != null) {
//                // User is signed in
//                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
//                Log.i(TAG, "Display name: " + user.getDisplayName());
//                Log.i(TAG, "Picture: " + user.getPhotoUrl());
//                Log.i(TAG, "Email: " + user.getEmail());
//                Log.i(TAG, "Provider ID: " + user.getProviderId() + " , providers: " + user.getProviders());
//
//            }
//        }
//    };
//Do not call onCreate on Parent Activity
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
