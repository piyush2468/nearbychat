package com.rndtechnosoft.fynder.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rndtechnosoft.fynder.FynderApplication;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.activity.ProfileActivity;
import com.rndtechnosoft.fynder.adapter.UsersAdapter;
import com.rndtechnosoft.fynder.model.Relationship;
import com.rndtechnosoft.fynder.model.User;
import com.rndtechnosoft.fynder.utility.Constants;
import com.rndtechnosoft.fynder.utility.MyDateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ravi on 3/8/2017.
 */

public class NearbyFriendsFragment extends Fragment {
    private final String TAG = "FindFriendActivity";
    private DatabaseReference usersRef;
    private Query followerRef;
    private Query followingRef;
    private UsersAdapter userAdapter;
    private List<User> contactList = new ArrayList<>();
    private ProgressDialog myProgressDialog;
//    private SearchView searchView;
private CardView findFriendLayout;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_nearby, container, false);
        FynderApplication.getInstance().sendScreenviwedevent(Constants.CT_NEARBY,"");
//      textStatus = (TextView) findViewById(R.id.text_status);
        myProgressDialog = new ProgressDialog(getActivity());
        myProgressDialog.setMessage(getString(R.string.finding_friends));
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.list_user);

        userAdapter = new UsersAdapter(getActivity(),contactList,true,true,new UsersAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User item) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra(ProfileActivity.KEY_UID, item.getId());
                intent.putExtra(ProfileActivity.KEY_DISPLAY_NAME, item.getName());
                startActivity(intent);
            }
        });

        findFriendLayout = (CardView) rootView.findViewById(R.id.no_friend_layout);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
        recyclerView.setAdapter(userAdapter);
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        rootView.findViewById(R.id.button_find_friend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "\n" +
                            "Let me recommend you this application for Best Chat App, you can find nearby people as well as nearby groups \n" +
                            "\n https://play.google.com/store/apps/details?id=" + getActivity().getPackageName());
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                } catch(Exception e) {
                    //e.toString();
                }
            }
        });
        callStart();
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private void callStart(){
        contactList.clear();
        userAdapter.notifyDataSetChanged();
        contactList.clear();

        usersRef.addChildEventListener(usersListener);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "We're done loading the initial " + dataSnapshot.getChildrenCount() + " items");
                myProgressDialog.dismiss();
                findFriendLayout.setVisibility(contactList.size() == 0 ? View.VISIBLE : View.GONE);
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
    public void onStart() {
        super.onStart();
        Log.i(TAG, "On Start");

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

    private void removeUser(String id) {
        for (User user : contactList) {
            if (user.getId().equals(id)) {
                user.detachProfileRef();
                contactList.remove(user);
                userAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        GlobalFragment.completeRefresh(GlobalFragment.item);
        inflater.inflate(R.menu.menu_find_friends, menu);
        MenuItem menuSearch = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) menuSearch.getActionView();
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FynderApplication.getInstance().sendActionevent(Constants.CT_NEARBY_SEARCH);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i(TAG,"On Query Text Submit => "+query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i(TAG,"On Query Text Change => "+newText);
                userAdapter.getFilter().filter(newText);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
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

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                return;
            }
            User user = new User(dataSnapshot.getKey());
            if (user.getId().equals(firebaseUser.getUid())) {
                return;
            }

            Relationship model = dataSnapshot.getValue(Relationship.class);
            Log.i(TAG, "Follower => On Added: " + model.getFollowing()+" , count list: "+contactList.size());
//            removeUser(model.getFollowing());
            removeUser(firebaseUser.getUid());
            if(model.getStatus() == Relationship.ACCEPTED){
                user.setId(model.getFollower());
                String desc = String.format(getString(R.string.time_friend)
                        , MyDateUtil.getDateTime(getActivity(), model.getTimestamp()));
                user.setDescription(desc);
                contactList.add(user);
                user.attachProfileRef(userAdapter);
            }
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

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                return;
            }
            User user = new User(dataSnapshot.getKey());
            if (user.getId().equals(firebaseUser.getUid())) {
                return;
            }

            Relationship model = dataSnapshot.getValue(Relationship.class);
            Log.i(TAG, "Following => On Added: " + model.getFollower()+" , count list: "+contactList.size());
//            removeUser(model.getFollower());
//            if(contactList.contains(firebaseUser.getUid()))
            removeUser(firebaseUser.getUid());
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
//                user.attachProfileRef(userAdapter);
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
                            MyDateUtil.getDateTime(getActivity(), election2.getValue(Long.class))));
                    break;
                }
            }
            contactList.add(user);
            user.attachProfileRef(userAdapter);
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
