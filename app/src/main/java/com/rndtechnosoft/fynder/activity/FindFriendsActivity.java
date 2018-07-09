package com.rndtechnosoft.fynder.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.adapter.ParticipantAdapter;
import com.rndtechnosoft.fynder.adapter.UsersAdapter;
import com.rndtechnosoft.fynder.model.Relationship;
import com.rndtechnosoft.fynder.model.User;
import com.rndtechnosoft.fynder.utility.MyDateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ravi on 3/8/2017.
 */

public class FindFriendsActivity extends AppCompatActivity {
    private final String TAG = "FindFriendActivity";
    private DatabaseReference usersRef;
    private Query followerRef;
    private Query followingRef;
    private UsersAdapter usersAdapter;
    private List<User> contactList = new ArrayList<>();
    private ProgressDialog myProgressDialog;
//    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        myProgressDialog = new ProgressDialog(this);
        myProgressDialog.setMessage(getString(R.string.finding_friends));
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list_user);
        usersAdapter = new UsersAdapter(FindFriendsActivity.this,contactList,false,false,new UsersAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User item) {
                Intent intent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                intent.putExtra(ProfileActivity.KEY_UID, item.getId());
                intent.putExtra(ProfileActivity.KEY_DISPLAY_NAME, item.getName());
                startActivity(intent);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(FindFriendsActivity.this));
        recyclerView.setAdapter(usersAdapter);
        setSupportActionBar(toolbar);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "On Start");
        contactList.clear();
        usersAdapter.notifyDataSetChanged();
        contactList.clear();

        usersRef.addChildEventListener(usersListener);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "We're done loading the initial " + dataSnapshot.getChildrenCount() + " items");
                myProgressDialog.dismiss();
                usersRef.removeEventListener(this);
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser == null) {
                    return;
                }
                String uid = firebaseUser.getUid();
                Log.i(TAG, "This uid: " + uid);
                followerRef = FirebaseDatabase.getInstance().getReference("relationship")
                        .orderByChild("follower").equalTo(uid);
                followingRef = FirebaseDatabase.getInstance().getReference("relationship")
                        .orderByChild("following").equalTo(uid);
                followerRef.removeEventListener(followerListener);
                followerRef.addChildEventListener(followerListener);
                followingRef.removeEventListener(followingListener);
                followingRef.addChildEventListener(followingListener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        myProgressDialog.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        usersRef.removeEventListener(usersListener);
        if (followerRef != null) {
            followerRef.removeEventListener(followerListener);
        }
        if (followingRef != null) {
            followingRef.removeEventListener(followingListener);
        }
        for (User user : contactList) {
            user.detachProfileRef();
        }
        myProgressDialog.dismiss();
    }

    //Do not call onCreate on Parent Activity
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void removeUser(String id) {
        for (User user : contactList) {
            if (user.getId().equals(id)) {
                user.detachProfileRef();
                contactList.remove(user);
                usersAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_find_friends, menu);
        MenuItem menuSearch = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) menuSearch.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i(TAG,"On Query Text Submit => "+query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i(TAG,"On Query Text Change => "+newText);
                usersAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }


//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if(id == android.R.id.home){
//            Log.i(TAG,"Home clicked..!!");
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private ChildEventListener followerListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Relationship model = dataSnapshot.getValue(Relationship.class);
            Log.i(TAG, "Follower => On Added: " + model.getFollowing()+" , count list: "+contactList.size());
            removeUser(model.getFollowing());
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener followingListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Relationship model = dataSnapshot.getValue(Relationship.class);
            Log.i(TAG, "Following => On Added: " + model.getFollower()+" , count list: "+contactList.size());
            removeUser(model.getFollower());
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

//    private ValueEventListener usersListener = new ValueEventListener() {
//        @Override
//        public void onDataChange(DataSnapshot dataSnapshot) {
//            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//            if (firebaseUser == null) {
//                return;
//            }
//            contactList.clear();
//            for (DataSnapshot election : dataSnapshot.getChildren()) {
//                User user = new User(election.getKey());
//                if(user.getId().equals(firebaseUser.getUid())){
//                    continue;
//                }
//                for (DataSnapshot election2 : election.getChildren()) {
//                    if (election2.getKey().equals("lastOnline")) {
//                        user.setDescription(String.format(getString(R.string.time_last_online),
//                                MyDateUtil.getDateTime(FindFriendsActivity.this, election2.getValue(Long.class))));
//                        break;
//                    }
//                }
//                contactList.add(user);
//                user.attachProfileRef(usersAdapter);
//            }
//            myProgressDialog.dismiss();
//
//            String uid = firebaseUser.getUid();
//            Log.i(TAG, "This uid: " + uid);
//            followerRef = FirebaseDatabase.getInstance().getReference("relationship")
//                    .orderByChild("follower").equalTo(uid);
//            followingRef = FirebaseDatabase.getInstance().getReference("relationship")
//                    .orderByChild("following").equalTo(uid);
//            followerRef.removeEventListener(followerListener);
//            followerRef.addChildEventListener(followerListener);
//            followingRef.removeEventListener(followingListener);
//            followingRef.addChildEventListener(followingListener);
//        }
//
//        @Override
//        public void onCancelled(DatabaseError databaseError) {
//
//        }
//    };

    private ChildEventListener usersListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                return;
            }
            User user = new User(dataSnapshot.getKey());
            if (user.getId().equals(firebaseUser.getUid())) {
                return;
            }
            if(contactList.size() > 3){
                myProgressDialog.dismiss();
            }
            Log.i(TAG, "On Added => " + user.getId() + " , children count: " + dataSnapshot.getChildrenCount());
            for (DataSnapshot election2 : dataSnapshot.getChildren()) {
                if (election2.getKey().equals("lastOnline")) {
                    user.setDescription(String.format(getString(R.string.time_last_online),
                            MyDateUtil.getDateTime(FindFriendsActivity.this, election2.getValue(Long.class))));
                    break;
                }
            }
            contactList.add(user);
            user.attachProfileRef(usersAdapter);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
}
