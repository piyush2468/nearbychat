package com.rndtechnosoft.fynder.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rndtechnosoft.fynder.FynderApplication;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.activity.ContactActivity;
import com.rndtechnosoft.fynder.activity.NearbyHomeActivity;
import com.rndtechnosoft.fynder.activity.SingleChatActivity;
import com.rndtechnosoft.fynder.adapter.LastChatAdapter;
import com.rndtechnosoft.fynder.model.LastChat;
import com.rndtechnosoft.fynder.model.Relationship;
import com.rndtechnosoft.fynder.utility.Constants;
import com.rndtechnosoft.fynder.utility.listener.OnTabUpdateListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ravi on 11/29/2016.
 */

public class PrivateFragment extends Fragment {
    private final String TAG = "PrivateFragment";
    private TextView textAlertInvitation;
    private LinearLayout layoutAlertInvitation;
    private LinearLayout progressBarView;
    private CardView findFriendLayout;
    private Query followerRef;
    private Query followingRef;
    private int countInvitation = 0;
    private int countFriend = 0;
    private int countPending = 0;
    private int countDeclined = 0;
    private int countBlocked = 0;
    private MenuItem menuFriendList;
    private MenuItem menuInviteList;
    private MenuItem menuPendingList;
    private MenuItem menuDeclinedList;
    private MenuItem menuBlockedList;
    private List<LastChat> lastChatList = new ArrayList<>();
    private LastChatAdapter adapter;
    private OnTabUpdateListener listener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_private, container, false);
        textAlertInvitation = (TextView) rootView.findViewById(R.id.text_alert_invitation);
        layoutAlertInvitation = (LinearLayout) rootView.findViewById(R.id.alert_invitation);
        progressBarView = (LinearLayout) rootView.findViewById(R.id.progressBarView);
        findFriendLayout = (CardView) rootView.findViewById(R.id.no_friend_layout);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.list_chat_nearby);
        adapter = new LastChatAdapter(listener, lastChatList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null){
            String uid = firebaseUser.getUid();
            followerRef = FirebaseDatabase.getInstance().getReference("relationship")
                    .orderByChild("follower").equalTo(uid);
            followingRef = FirebaseDatabase.getInstance().getReference("relationship")
                    .orderByChild("following").equalTo(uid);
        }

        layoutAlertInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ContactActivity.class);
                intent.putExtra(ContactActivity.KEY_TITLE,getString(R.string.invitation_list));
                intent.putExtra(ContactActivity.KEY_TYPE, ContactActivity.TYPE_RECEIVE_INVITE);
                startActivity(intent);
                if(listener != null){
                    listener.onTabUpdate(2,countInvitation * -1);
                }
            }
        });

        adapter.setOnItemClickListener(new LastChatAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(LastChat item) {
                Log.i(TAG,"On Click chat -> display name: "+item.getDisplayName()+" , count badge: "+item.getCountBadge());
                Intent intent = new Intent(getActivity(), SingleChatActivity.class);
                intent.putExtra(SingleChatActivity.KEY_FRIEND_ID, item.getUid());
                intent.putExtra(SingleChatActivity.KEY_TITLE, item.getDisplayName());
                intent.putExtra(SingleChatActivity.KEY_RELATIONSHIP_ID, item.getRelationshipId());
                startActivity(intent);
                if(item.getCountBadge() > 0 && listener != null){
                    listener.onTabUpdate(2, item.getCountBadge() * -1);
                }
            }
        });

        rootView.findViewById(R.id.button_find_friend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NearbyHomeActivity.setTab(0);
//                startActivity(new Intent(getActivity(), FindFriendsActivity.class));
            }
        });
        callStart();
        return rootView;
    }

    private void callStart(){
        countBlocked =0;
        countDeclined =0;
        countInvitation=0;
        countPending=0;
        countFriend=0;
        FynderApplication.getInstance().sendScreenviwedevent(Constants.CT_PRIVATE,"");
        if(followerRef != null){
            followerRef.addChildEventListener(followerListener);
            followerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    progressBarView.setVisibility(View.GONE);
                    findFriendLayout.setVisibility(lastChatList.size() == 0 ? View.VISIBLE : View.GONE);
                    followerRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        if(followingRef != null){
            followingRef.addChildEventListener(followingListener);
            followingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    progressBarView.setVisibility(View.GONE);
                    findFriendLayout.setVisibility(lastChatList.size() == 0 ? View.VISIBLE : View.GONE);
                    followingRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        lastChatList.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.i(TAG, "On Create");

    }

    @Override
    public void onStart() {
        super.onStart();

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
        for(LastChat lastChat:lastChatList){
            lastChat.detachRef();
        }
        if(adapter != null){
            adapter.resetCountBadge();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            listener = (OnTabUpdateListener) context;
        }catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnTabUpdateListener");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i(TAG, "On Create option menu");

        GlobalFragment.completeRefresh(GlobalFragment.item);
        inflater.inflate(R.menu.menu_private, menu);
        menuFriendList = menu.findItem(R.id.menu_friend_list);
        menuInviteList = menu.findItem(R.id.menu_invitation_list);
        menuPendingList = menu.findItem(R.id.menu_pending);
        menuDeclinedList = menu.findItem(R.id.menu_decline);
        menuBlockedList = menu.findItem(R.id.menu_block);
        menuFriendList.setVisible(countFriend > 0);
        menuInviteList.setVisible(countInvitation > 0);
        menuPendingList.setVisible(countPending > 0);
        menuBlockedList.setVisible(countBlocked > 0);
        menuDeclinedList.setVisible(countDeclined > 0);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menu_pending){
            Intent intent = new Intent(getActivity(), ContactActivity.class);
            intent.putExtra(ContactActivity.KEY_TITLE,getString(R.string.pending_acceptance));
            intent.putExtra(ContactActivity.KEY_TYPE, ContactActivity.TYPE_PENDING_INVITE);
            startActivity(intent);
            return true;
        }else if(id == R.id.menu_block){
            Intent intent = new Intent(getActivity(), ContactActivity.class);
            intent.putExtra(ContactActivity.KEY_TITLE,getString(R.string.blocked));
            intent.putExtra(ContactActivity.KEY_TYPE, ContactActivity.TYPE_BLOCKED);
            startActivity(intent);
            return true;
        }else if(id == R.id.menu_decline){
            Intent intent = new Intent(getActivity(), ContactActivity.class);
            intent.putExtra(ContactActivity.KEY_TITLE,getString(R.string.declined));
            intent.putExtra(ContactActivity.KEY_TYPE, ContactActivity.TYPE_DECLINE);
            startActivity(intent);
            return true;
        }else if(id == R.id.menu_friend_list){
            Intent intent = new Intent(getActivity(), ContactActivity.class);
            intent.putExtra(ContactActivity.KEY_TITLE,getString(R.string.friend_list));
            intent.putExtra(ContactActivity.KEY_TYPE, ContactActivity.TYPE_FRIEND);
            startActivity(intent);
            return true;
        }else if(id == R.id.menu_invitation_list){
            Intent intent = new Intent(getActivity(), ContactActivity.class);
            intent.putExtra(ContactActivity.KEY_TITLE,getString(R.string.invitation_list));
            intent.putExtra(ContactActivity.KEY_TYPE, ContactActivity.TYPE_RECEIVE_INVITE);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ChildEventListener followerListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.i(TAG, "Follower On Added -> " + dataSnapshot);
            Relationship model = dataSnapshot.getValue(Relationship.class);
            model.setId(dataSnapshot.getKey());

            if(model.getStatus() == Relationship.PENDING){
                countInvitation++;
                if(menuInviteList != null) {
                    menuInviteList.setVisible(true);
                }
                if(listener != null){
                    listener.onTabUpdate(2,countInvitation);
                }
                String textInvite= String.format(getString(R.string.get_invitation),countInvitation);
                textAlertInvitation.setText(textInvite);
            }
            else if(model.getStatus() == Relationship.ACCEPTED){
                LastChat lastChat = new LastChat(model.getId(),model.getFollowing());
//                lastChat.setLastMessage(getString(R.string.start_conversation));
                lastChatList.add(lastChat);
                adapter.notifyItemChanged(lastChatList.size()-1);
                lastChat.attachRef(adapter,lastChatList.size()-1);
                countFriend++;
                if(menuFriendList != null){
                    menuFriendList.setVisible(true);
                }
            }else if(model.getStatus() == Relationship.BLOCKED){
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if(firebaseUser != null){
                    String uid = firebaseUser.getUid();
                    if(uid.equals(model.getActionUid())){
                        countBlocked++;
                        if(menuBlockedList != null){
                            menuBlockedList.setVisible(true);
                        }
                    }
                }
            }else if(model.getStatus() == Relationship.DECLINED){
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if(firebaseUser != null){
                    String uid = firebaseUser.getUid();
                    if(uid.equals(model.getActionUid())){
                        countDeclined++;
                        if(menuDeclinedList != null){
                            menuDeclinedList.setVisible(true);
                        }
                    }
                }
            }

            layoutAlertInvitation.setVisibility(countInvitation > 0?
                    View.VISIBLE : View.GONE);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.w(TAG, "Follower On changed-> " + dataSnapshot);
            Relationship model = dataSnapshot.getValue(Relationship.class);
            model.setId(dataSnapshot.getKey());
//            if(model.getStatus() == Relationship.ACCEPTED){

//            }
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
            Log.i(TAG, "Following On Added -> " + dataSnapshot);
            Relationship model = dataSnapshot.getValue(Relationship.class);
            model.setId(dataSnapshot.getKey());
            if(model.getStatus() == Relationship.ACCEPTED){
                LastChat lastChat = new LastChat(model.getId(),model.getFollower());
//                lastChat.setLastMessage(getString(R.string.start_conversation));
                lastChatList.add(lastChat);
                adapter.notifyItemChanged(lastChatList.size()-1);
                lastChat.attachRef(adapter,lastChatList.size()-1);
                countFriend++;
                if(menuFriendList != null){
                    menuFriendList.setVisible(true);
                }
            }else if(model.getStatus() == Relationship.PENDING){
                countPending++;
                if(menuPendingList != null){
                    menuPendingList.setVisible(true);
                }
            }else if(model.getStatus() == Relationship.BLOCKED){
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if(firebaseUser != null){
                    String uid = firebaseUser.getUid();
                    if(uid.equals(model.getActionUid())){
                        countBlocked++;
                        if(menuBlockedList != null){
                            menuBlockedList.setVisible(true);
                        }
                    }
                }
            }else if(model.getStatus() == Relationship.DECLINED){
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if(firebaseUser != null){
                    String uid = firebaseUser.getUid();
                    if(uid.equals(model.getActionUid())){
                        countDeclined++;
                        if(menuDeclinedList != null){
                            menuDeclinedList.setVisible(true);
                        }
                    }
                }
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.i(TAG, "Following On Added -> " + dataSnapshot);
            Relationship model = dataSnapshot.getValue(Relationship.class);
            model.setId(dataSnapshot.getKey());
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
