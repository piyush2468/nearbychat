package com.rndtechnosoft.fynder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.rndtechnosoft.fynder.FynderApplication;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.adapter.UsersAdapter;
import com.rndtechnosoft.fynder.model.Relationship;
import com.rndtechnosoft.fynder.model.User;
import com.rndtechnosoft.fynder.utility.Constants;
import com.rndtechnosoft.fynder.utility.MyDateUtil;

import java.util.ArrayList;
import java.util.List;

public class ContactActivity extends AppCompatActivity {
    private final String TAG = "ContactActivity";
    public static final String KEY_TITLE = ContactActivity.class.getName() + ".KEY_TITLE";
    public static final String KEY_TYPE = ContactActivity.class.getName() + ".KEY_TYPE";
    public static final int TYPE_PENDING_INVITE = 0;
    public static final int TYPE_RECEIVE_INVITE = 1;
    public static final int TYPE_FRIEND = 2;
    public static final int TYPE_DECLINE = 3;
    public static final int TYPE_BLOCKED = 4;

    private int type;
    private Query followerRef;
    private Query followingRef;
    private UsersAdapter userAdapter;
//    private TextView textStatus;
    private List<User> contactList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        Intent intent = getIntent();
        String title = intent.getStringExtra(KEY_TITLE);
        FynderApplication.getInstance().sendScreenviwedevent(Constants.CT_PRIVATEOTHER,title);
        type = intent.getIntExtra(KEY_TYPE, -1);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list_user);
//        textStatus = (TextView) findViewById(R.id.text_status);
        userAdapter = new UsersAdapter(ContactActivity.this,contactList,false,true,new UsersAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User item) {
                Intent intent = new Intent(ContactActivity.this, ProfileActivity.class);
                intent.putExtra(ProfileActivity.KEY_UID, item.getId());
                intent.putExtra(ProfileActivity.KEY_DISPLAY_NAME, item.getName());
                startActivity(intent);
            }
        });

        recyclerView.setLayoutManager(new GridLayoutManager(ContactActivity.this,2));
        recyclerView.setAdapter(userAdapter);

        setSupportActionBar(toolbar);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null){
            String uid = firebaseUser.getUid();
            Log.i(TAG,"This uid: "+uid);
            followerRef = FirebaseDatabase.getInstance().getReference("relationship")
                    .orderByChild("follower").equalTo(uid);
            followingRef = FirebaseDatabase.getInstance().getReference("relationship")
                    .orderByChild("following").equalTo(uid);
        }

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG,"On Start");
        if(type == -1){
            finish();
        }
        contactList.clear();
        userAdapter.notifyDataSetChanged();
        if(followerRef != null){
            followerRef.addChildEventListener(followerListener);
        }
        if(followingRef != null){
            followingRef.addChildEventListener(followingListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(followerRef != null){
            followerRef.removeEventListener(followerListener);
        }
        if(followingRef != null){
            followingRef.removeEventListener(followingListener);
        }
        for(User user:contactList){
            user.detachProfileRef();
        }
    }

    //Do not call onCreate on Parent Activity
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private ChildEventListener followingListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.i(TAG,"Following => On Added: "+dataSnapshot);
            Relationship model = dataSnapshot.getValue(Relationship.class);
            model.setId(dataSnapshot.getKey());
            if(type == TYPE_PENDING_INVITE && model.getStatus() == Relationship.PENDING){
                User user = new User();
                user.setId(model.getFollower());
                String desc = String.format(getString(R.string.time_pending_invite)
                        , MyDateUtil.getDateTime(ContactActivity.this, model.getTimestamp()));
                user.setDescription(desc);
                contactList.add(user);
                user.attachProfileRef(userAdapter);
            }else if(type == TYPE_BLOCKED && model.getStatus() == Relationship.BLOCKED){
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if(firebaseUser != null) {
                    String uid = firebaseUser.getUid();
                    if (uid.equals(model.getActionUid())) {
                        User user = new User();
                        user.setId(model.getFollower());
                        String desc = String.format(getString(R.string.time_blocked)
                                , MyDateUtil.getDateTime(ContactActivity.this, model.getTimestamp()));
                        user.setDescription(desc);
                        contactList.add(user);
                        user.attachProfileRef(userAdapter);
                    }
                }
            }else if(type == TYPE_DECLINE && model.getStatus() == Relationship.DECLINED){
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if(firebaseUser != null) {
                    String uid = firebaseUser.getUid();
                    if (uid.equals(model.getActionUid())) {
                        User user = new User();
                        user.setId(model.getFollower());
                        String desc = String.format(getString(R.string.time_declined)
                                , MyDateUtil.getDateTime(ContactActivity.this, model.getTimestamp()));
                        user.setDescription(desc);
                        contactList.add(user);
                        user.attachProfileRef(userAdapter);
                    }
                }
            }else if(type == TYPE_FRIEND && model.getStatus() == Relationship.ACCEPTED){
                User user = new User();
                user.setId(model.getFollower());
                String desc = String.format(getString(R.string.time_friend)
                        , MyDateUtil.getDateTime(ContactActivity.this, model.getTimestamp()));
                user.setDescription(desc);
                contactList.add(user);
                user.attachProfileRef(userAdapter);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.i(TAG,"Following => On Changed: "+dataSnapshot);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.i(TAG,"Following => On Removed: "+dataSnapshot);
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            Log.i(TAG,"Following => On Moved: "+dataSnapshot);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG,"Following -> On Canceled: "+databaseError.getMessage());
        }
    };

    private ChildEventListener followerListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.i(TAG,"Follower => On Added: "+dataSnapshot);
            Relationship model = dataSnapshot.getValue(Relationship.class);
            model.setId(dataSnapshot.getKey());
            if(type == TYPE_RECEIVE_INVITE && model.getStatus() == Relationship.PENDING){
                User user = new User();
                user.setId(model.getFollowing());
                String desc = String.format(getString(R.string.time_receive_invite)
                        , MyDateUtil.getDateTime(ContactActivity.this, model.getTimestamp()));
                user.setDescription(desc);
                contactList.add(user);
                user.attachProfileRef(userAdapter);
            }else if(type == TYPE_BLOCKED && model.getStatus() == Relationship.BLOCKED){
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if(firebaseUser != null) {
                    String uid = firebaseUser.getUid();
                    if (uid.equals(model.getActionUid())) {
                        User user = new User();
                        user.setId(model.getFollowing());
                        String desc = String.format(getString(R.string.time_blocked)
                                , MyDateUtil.getDateTime(ContactActivity.this, model.getTimestamp()));
                        user.setDescription(desc);
                        contactList.add(user);
                        user.attachProfileRef(userAdapter);
                    }
                }
            }else if(type == TYPE_DECLINE && model.getStatus() == Relationship.DECLINED){
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if(firebaseUser != null) {
                    String uid = firebaseUser.getUid();
                    if (uid.equals(model.getActionUid())) {
                        User user = new User();
                        user.setId(model.getFollowing());
                        String desc = String.format(getString(R.string.time_declined)
                                , MyDateUtil.getDateTime(ContactActivity.this, model.getTimestamp()));
                        user.setDescription(desc);
                        contactList.add(user);
                        user.attachProfileRef(userAdapter);
                    }
                }
            }else if(type == TYPE_FRIEND && model.getStatus() == Relationship.ACCEPTED){
                User user = new User();
                user.setId(model.getFollowing());
                String desc = String.format(getString(R.string.time_friend)
                        , MyDateUtil.getDateTime(ContactActivity.this, model.getTimestamp()));
                user.setDescription(desc);
                contactList.add(user);
                user.attachProfileRef(userAdapter);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.i(TAG,"Follower => On Changed: "+dataSnapshot);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.i(TAG,"Follower => On Removed: "+dataSnapshot);
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            Log.i(TAG,"Follower => On Moved: "+dataSnapshot);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG,"Follower -> On Canceled: "+databaseError.getMessage());
        }
    };

}
