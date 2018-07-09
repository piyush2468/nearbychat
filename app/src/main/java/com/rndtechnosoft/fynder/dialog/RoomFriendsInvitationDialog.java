package com.rndtechnosoft.fynder.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.rndtechnosoft.fynder.activity.FindFriendsActivity;
import com.rndtechnosoft.fynder.activity.ProfileActivity;
import com.rndtechnosoft.fynder.adapter.ParticipantAdapter;
import com.rndtechnosoft.fynder.adapter.UsersAdapter;
import com.rndtechnosoft.fynder.model.Relationship;
import com.rndtechnosoft.fynder.model.RoomInvitation;
import com.rndtechnosoft.fynder.model.User;
import com.rndtechnosoft.fynder.model.UserImage;
import com.rndtechnosoft.fynder.model.event.NotificationEvent;
import com.rndtechnosoft.fynder.utility.MyDateUtil;
import com.rndtechnosoft.fynder.utility.PushFCM;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ravi on 2/15/2017.
 */

public class RoomFriendsInvitationDialog extends AlertDialog {
    private final String TAG = "RoomInvitation";
    private RecyclerView recyclerView;
    private Activity activity;
    private Query followingRef;
    private Query followerRef;
    private DatabaseReference invitedRef;
    private DatabaseReference subscribersRef;
    private DatabaseReference profileRef;
    private TextView textTitle;
    private UsersAdapter usersAdapter;
    private List<User> userList = new ArrayList<>();
    private List<RoomInvitation> invitedList = new ArrayList<>();
    private List<String> subscriberList = new ArrayList<>();
    private PushFCM pushFCM;


    public RoomFriendsInvitationDialog(final Activity activity, final String roomId, final String roomName) {
        super(activity);
        this.activity = activity;
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (roomId == null || firebaseUser == null) {
            dismiss();
            return;
        }
        final String uid = firebaseUser.getUid();
        pushFCM = new PushFCM(activity, NotificationEvent.TYPE_ROOM_INVITE);
        pushFCM.setTitle(activity.getString(R.string.room_invitation));
        pushFCM.setUid(uid);
        pushFCM.setId(roomId);
        pushFCM.setDisplayName(roomName);
        Log.i(TAG,"This uid: "+uid);
        profileRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        followerRef = FirebaseDatabase.getInstance().getReference("relationship")
                .orderByChild("follower").equalTo(uid);
        followingRef = FirebaseDatabase.getInstance().getReference("relationship")
                .orderByChild("following").equalTo(uid);
        invitedRef = FirebaseDatabase.getInstance().getReference("rooms").child("public")
                .child(roomId).child("roomInvitation");
        subscribersRef = FirebaseDatabase.getInstance().getReference("rooms").child("public")
                .child(roomId).child("subscribers");
        profileRef.addValueEventListener(profileListener);
        LayoutInflater inflater = LayoutInflater.from(activity);
        View alertView = inflater.inflate(R.layout.dialog_users, null);
        Button buttonClose = (Button) alertView.findViewById(R.id.close_button);
        textTitle = (TextView) alertView.findViewById(R.id.text_status_user);

        recyclerView = (RecyclerView) alertView.findViewById(R.id.list_user);
//        textStatus = (TextView) findViewById(R.id.text_status);
        usersAdapter = new UsersAdapter(activity,userList,false,false,new UsersAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final User item) {
                Log.i(TAG,"User token: "+item.getToken());
                String message = String.format(activity.getString(R.string.message_room_invitation), item.getName());
                new AlertDialog.Builder(activity)
                        .setMessage(message)
                        .setPositiveButton("Ok", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                RoomInvitation invited = null;
                                boolean isSubscribed = false;
                                for(RoomInvitation model: invitedList){
                                    if(model.getSubject().equals(item.getId())){
                                        invited = model;
                                        break;
                                    }
                                }
                                for(String userSubscribe: subscriberList){
                                    if(userSubscribe.equals(item.getId())){
                                        isSubscribed = true;
                                        break;
                                    }
                                }
                                if(invited != null){
                                    String message = invited.getDisplayName() == null ?
                                            activity.getString(R.string.alert_invited_no_name) :
                                            String.format(activity.getString(R.string.alert_invited_with_name), invited.getDisplayName());
                                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if(isSubscribed){
                                    Toast.makeText(activity, activity.getString(R.string.alert_subscribed), Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                RoomInvitation model = new RoomInvitation();
                                model.setFrom(uid);
                                DatabaseReference ref = invitedRef.child(item.getId());
                                ref.setValue(model.toMap());

                                //push notification
                                String message = String.format(activity.getString(R.string.room_invitation_msg),
                                        firebaseUser.getDisplayName(), roomName);
                                pushFCM.setRecipients(item.getToken());
                                pushFCM.push(message,null);
                            }
                        })
                        .setNegativeButton(activity.getString(R.string.cancel),
                                new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                })
                        .create().show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(usersAdapter);
        String title = String.format(activity.getString(R.string.title_dialog_friends), 0);
        textTitle.setText(title);
        invitedRef.addValueEventListener(invitedListener);
        subscribersRef.addValueEventListener(subscriberListener);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setView(alertView);
    }

    private ValueEventListener profileListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for (DataSnapshot election : dataSnapshot.getChildren()) {
                User model = dataSnapshot.getValue(User.class);
                if (election.getKey().equals("images")) {
                    String key = model.getImageUrl() == null ? "default" : model.getImageUrl();
                    DataSnapshot defaultImage = election.child(key);
                    if (defaultImage.exists()) {
                        UserImage userImage = defaultImage.getValue(UserImage.class);
                        pushFCM.setImageUrl(userImage.getThumbPic());
                        break;
                    }
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ValueEventListener invitedListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.i(TAG, "dataSnapshot value: " + dataSnapshot.getValue());
            invitedList.clear();
            if(dataSnapshot.getValue() != null){
                for (DataSnapshot election : dataSnapshot.getChildren()) {
                    RoomInvitation model = new RoomInvitation(election);
                    model.attachProfile();
                    invitedList.add(model);
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener followingListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.i(TAG, "Following => On Added: " + dataSnapshot);
            recyclerView.setVisibility(View.VISIBLE);
            Relationship model = dataSnapshot.getValue(Relationship.class);
            model.setId(dataSnapshot.getKey());
            if (model.getStatus() == Relationship.ACCEPTED) {
                User user = new User();
                user.setId(model.getFollower());
                user.setRelationshipId(model.getId());
                String desc = String.format(activity.getString(R.string.time_friend)
                        , MyDateUtil.getDateTime(activity, model.getTimestamp()));
                user.setDescription(desc);
                userList.add(user);
                user.attachProfileRef(usersAdapter);
                user.attachTokenRef();
                String title = String.format(activity.getString(R.string.title_dialog_friends), userList.size());
                textTitle.setText(title);
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

    private ChildEventListener followerListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.i(TAG, "Follower => On Added: " + dataSnapshot);
            recyclerView.setVisibility(View.VISIBLE);
            Relationship model = dataSnapshot.getValue(Relationship.class);
            model.setId(dataSnapshot.getKey());
            if (model.getStatus() == Relationship.ACCEPTED) {
                User user = new User();
                user.setId(model.getFollowing());
                user.setRelationshipId(model.getId());
                String desc = String.format(activity.getString(R.string.time_friend)
                        , MyDateUtil.getDateTime(activity, model.getTimestamp()));
                user.setDescription(desc);
                userList.add(user);
                user.attachProfileRef(usersAdapter);
                user.attachTokenRef();
                String title = String.format(activity.getString(R.string.title_dialog_friends), userList.size());
                textTitle.setText(title);
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

    private ValueEventListener subscriberListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            subscriberList.clear();
            if(dataSnapshot != null) {
                for (DataSnapshot election : dataSnapshot.getChildren()) {
                    subscriberList.add(election.getKey());
                }
            }
            userList.clear();
            usersAdapter.notifyDataSetChanged();
            followerRef.removeEventListener(followerListener);
            followingRef.removeEventListener(followingListener);
            followerRef.addChildEventListener(followerListener);
            followingRef.addChildEventListener(followingListener);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.i(TAG, "on detached from window");
        for(User model:userList){
            model.detachProfileRef();
            model.detachTokenRef();
        }
        for(RoomInvitation model:invitedList){
            model.detachProfile();
        }
        userList.clear();
        invitedList.clear();
        subscriberList.clear();
        if (usersAdapter != null) {
            usersAdapter.notifyDataSetChanged();
        }
        if (invitedRef != null) {
            invitedRef.removeEventListener(invitedListener);
        }
        if(subscribersRef != null){
            subscribersRef.removeEventListener(subscriberListener);
        }
        if (followingRef != null) {
            followingRef.removeEventListener(followingListener);
        }
        if (followerRef != null) {
            followerRef.removeEventListener(followerListener);
        }
        profileRef.removeEventListener(profileListener);
    }
}
