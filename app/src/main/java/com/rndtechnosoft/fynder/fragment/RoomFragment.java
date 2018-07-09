package com.rndtechnosoft.fynder.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.rndtechnosoft.fynder.FynderApplication;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.activity.NearbyHomeActivity;
import com.rndtechnosoft.fynder.activity.RoomActivity;
import com.rndtechnosoft.fynder.adapter.RoomAdapter;
import com.rndtechnosoft.fynder.dialog.RoomInvitationsDialog;
import com.rndtechnosoft.fynder.dialog.RoomSettingDialog;
import com.rndtechnosoft.fynder.model.Room;
import com.rndtechnosoft.fynder.model.event.OnEnterEvent;
import com.rndtechnosoft.fynder.model.event.OnLeaveEvent;
import com.rndtechnosoft.fynder.utility.Ad_Helper;
import com.rndtechnosoft.fynder.utility.Constants;
import com.rndtechnosoft.fynder.utility.listener.OnTabUpdateListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ravi on 11/7/2016.
 */

public class RoomFragment extends Fragment {
    private final String TAG = "RoomFragment";
    public static final int FLAG_JOIN_ROOM_REQUEST = 523;
    public static final String JOINED_ROOM_ID = RoomFragment.class.getName() + ".JOINED_ROOM_ID";
    private DatabaseReference mDatabase;
    private DatabaseReference mGlobalDatabase;

    //    private RecyclerView recyclerView;
    private RoomAdapter adapter;
    private List<Room> roomList = new ArrayList<>();
    private List<Room> joinedRoomList = new ArrayList<>();
    private List<Room> invitedRoomList = new ArrayList<>();
    private MenuItem menuAddRoom,menuEditRoom;
    private boolean isMyRoomCreated = false;
    private int countInvite;
    private TextView textAlertInvitation;
    private LinearLayout layoutAlertInvitation;
    private LinearLayout progressBarView;
    private OnTabUpdateListener listener;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.i(TAG, "On Create");
        mDatabase = FirebaseDatabase.getInstance().getReference("rooms").child("public");
        mGlobalDatabase = FirebaseDatabase.getInstance().getReference("rooms").child("global");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "OnCreateView");
        View rootView = inflater.inflate(R.layout.fragment_room, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.list_chat_room);
        textAlertInvitation = (TextView) rootView.findViewById(R.id.text_alert_invitation);
        layoutAlertInvitation = (LinearLayout) rootView.findViewById(R.id.alert_invitation);
        progressBarView = (LinearLayout) rootView.findViewById(R.id.progressBarView);
        adapter = new RoomAdapter(getActivity(),roomList, new RoomAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Room item) {
                boolean isJoined = false;
                for (Room checkJoined : joinedRoomList) {
                    if (checkJoined.getId().equals(item.getId())) {
                        isJoined = true;
                        break;
                    }
                }
                Intent intent = new Intent(getActivity(), RoomActivity.class);
                intent.putExtra(RoomActivity.KEY_ID, item.getId());
                intent.putExtra(RoomActivity.KEY_ROOM_NAME, item.getName());
                intent.putExtra(RoomActivity.KEY_IS_JOINED, isJoined);
                startActivityForResult(intent, FLAG_JOIN_ROOM_REQUEST);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        layoutAlertInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Room model : invitedRoomList) {
                    Log.i(TAG, "Room name: " + model.getName());
                }
                new RoomInvitationsDialog(getActivity(), invitedRoomList, joinedRoomList).show();
            }
        });
        callStart();
        return rootView;
    }

    @Override
    public void onStart() {
        Log.i(TAG, "On Start");
        super.onStart();

    }

    private void callStart(){
        roomList.clear();
        invitedRoomList.clear();
        FynderApplication.getInstance().sendScreenviwedevent(Constants.CT_ROOMS,"");
        layoutAlertInvitation.setVisibility(View.GONE);
        countInvite = 0;
        adapter.notifyDataSetChanged();
        if (childEventListener != null && mDatabase != null) {
            mDatabase.addChildEventListener(childEventListener);
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    progressBarView.setVisibility(View.GONE);
                    mDatabase.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

//            mGlobalDatabase.addChildEventListener(globalChildEventListener);
//            mGlobalDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    if(dataSnapshot.getValue() != null) {
//                        Log.d("GlobalGroupSnap", dataSnapshot.getValue().toString());
//                        Room room = new Room();
//                        room.setName("Global");
//                        room.setDescription("Hello");
//                        room.setId(dataSnapshot.getKey());
//
//                        int countMember = 0;
//                        for (DataSnapshot election : dataSnapshot.getChildren()) {
//                            if (election.getKey().equals("participant")) {
//                                countMember = (int) election.getChildrenCount();
//                                break;
//                            }
//                        }
//
//                        room.setNumMembers(countMember);
//
//                        roomList.add(0, room);
//                        adapter.notifyItemInserted(0);
//                        adapter.notifyDataSetChanged();
//                    }
//                    progressBarView.setVisibility(View.GONE);
//                    mGlobalDatabase.removeEventListener(this);
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });



        }
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "On Stop");
        if (childEventListener != null && mDatabase != null) {
            mDatabase.removeEventListener(childEventListener);
        }
        for (Room room : roomList) {
            room.detachProfileRef();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
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
        inflater.inflate(R.menu.menu_room_fragment, menu);
        menuAddRoom = menu.findItem(R.id.action_add_room);
        menuEditRoom = menu.findItem(R.id.action_edit_room);
        if(isMyRoomCreated){
            menuEditRoom.setVisible(true);
            menuAddRoom.setVisible(false);
        }else{
            menuEditRoom.setVisible(false);
            menuAddRoom.setVisible(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_room) {
            FynderApplication.getInstance().sendActionevent(Constants.CT_CREATE_ROOM);
            new RoomSettingDialog(getActivity(),getString(R.string.title_create_room)).show();
            Ad_Helper.showInterstitial();
            return true;
        }else if(id == R.id.action_edit_room){
            FynderApplication.getInstance().sendActionevent(Constants.CT_EDIT_ROOM);
            new RoomSettingDialog(getActivity(),getString(R.string.room_setting)).show();
            Ad_Helper.showInterstitial();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addRoomInvitation(Room room) {
        layoutAlertInvitation.setVisibility(View.VISIBLE);
        invitedRoomList.add(room);
        countInvite++;
        if(listener != null){
            listener.onTabUpdate(1,countInvite);
        }
        String textInvite = String.format(getString(R.string.get_invitation), countInvite);
        textAlertInvitation.setText(textInvite);
    }

    public void delRoomInvitation(Room room) {
        boolean isExist = false;
        for(Room model:invitedRoomList){
            if(model.getId().equals(room.getId())){
                isExist = true;
                invitedRoomList.remove(model);
                break;
            }
        }
        if(!isExist){
            return;
        }
        countInvite--;
        layoutAlertInvitation.setVisibility(countInvite > 0 ? View.VISIBLE : View.GONE);
        String textInvite = String.format(getString(R.string.get_invitation), countInvite);
        textAlertInvitation.setText(textInvite);
    }

    private ValueEventListener globalValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
            String key = dataSnapshot.getKey();
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            Log.d(TAG, "onChildAdded -> key: " + key);
            if (key == null || firebaseUser == null) {
                return;
            }

            Room room = dataSnapshot.getValue(Room.class);
            room.setId(key);
            if (room.getName() == null) {
                return;
            }
            int countMember = 0;
            for (DataSnapshot election : dataSnapshot.getChildren()) {
                if (election.getKey().equals("participant")) {
                    countMember = (int) election.getChildrenCount();
                    break;
                }
            }
            room.setNumMembers(countMember);
            Log.i(TAG, "Room name: " + room.getName() + " , Count participant: " + countMember);
            if (key.equals(firebaseUser.getUid())) { //my room
                isMyRoomCreated = true;
                Log.i(TAG, "Key is equal => this is my room");
                if (menuAddRoom != null) {
                    menuAddRoom.setVisible(false);
                }
                //join to my room
                DatabaseReference joinRef = mDatabase.child(firebaseUser.getUid())
                        .child("participant").child(firebaseUser.getUid());
                joinRef.setValue(ServerValue.TIMESTAMP);
                joinRef.onDisconnect().removeValue();
                room.setMyRoom(true);
                roomList.add(0,room);
                adapter.notifyItemInserted(0);
                room.attachProfileRef(adapter);
                ///reordering index
//                for (int i = 1; i < roomList.size(); i++) {
//                    Room moveRoom = roomList.get(i);
//                    moveRoom.setPosition(i);
//                    adapter.notifyItemChanged(i);
//                }
                //add my room to joined list
                Room myRoom = new Room(firebaseUser.getUid());
                myRoom.attachRoomRef();
                joinedRoomList.add(myRoom);
            } else {
                roomList.add(room);
                adapter.notifyItemInserted(roomList.size() - 1);
                room.attachProfileRef(adapter);
                room.attachRoomInvitation(RoomFragment.this);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildChanged: " + dataSnapshot.getKey());
            String key = dataSnapshot.getKey();
            if (key == null) {
                return;
            }
            int countMember = 0;
            for (DataSnapshot election : dataSnapshot.getChildren()) {
                if (election.getKey().equals("participant")) {
                    countMember = (int) election.getChildrenCount();
                    break;
                }
            }
            int position = 0;
            boolean isExist = false;
            for (Room room : roomList) {
                if (room.getId().equals(key)) {
                    room.setNumMembers(countMember);
                    adapter.notifyItemChanged(position);
                    adapter.sort();
                    isExist = true;
                    break;
                }
                position++;
            }
            if (!isExist) {
                Room model = dataSnapshot.getValue(Room.class);
                if (model.getName() != null) {
                    Log.i(TAG, "Room name: " + model.getName());
                    model.setId(key);
                    model.setNumMembers(1);
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (firebaseUser != null && key.equals(firebaseUser.getUid())) {
                        roomList.add(0, model);
                        adapter.notifyItemInserted(0);
                    } else {
                        roomList.add(model);
                        adapter.notifyItemInserted(roomList.size() - 1);
                        adapter.sort();
                    }
                    model.attachProfileRef(adapter);
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onChildRemoved: " + dataSnapshot.getKey());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildMoved: " + dataSnapshot.getKey());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG, "onCancelled", databaseError.toException());
//            Toast.makeText(getActivity(), getString(R.string.error_load_category),
//                    Toast.LENGTH_SHORT).show();
        }

    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLeaveEvent(OnLeaveEvent event) {
        Log.i(TAG, "On leave event, ID: " + event.userId);
        //leave my room
        for (Room leaveRoom : joinedRoomList) {
            Log.i(TAG, "Leave room name: " + leaveRoom.getName() + " , room id: " + leaveRoom.getId());
            leaveRoom.detachRoomRef();
        }
        joinedRoomList.clear();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEnterEvent(OnEnterEvent event) {
        Log.i(TAG, "On enter event, ID: " + event.userId + " , is my room created: " + isMyRoomCreated);
        if (!isMyRoomCreated) {
            DatabaseReference joinRef = mDatabase.child(event.userId)
                    .child("participant").child(event.userId);
            joinRef.setValue(ServerValue.TIMESTAMP);
            joinRef.onDisconnect().removeValue();
            Room myRoom = new Room(event.userId);
            myRoom.attachRoomRef();
            joinedRoomList.add(myRoom);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "On Activity result");
        if (requestCode == FLAG_JOIN_ROOM_REQUEST) {
            if (data != null) {
                String joinedRoom = data.getStringExtra(JOINED_ROOM_ID);
                if (joinedRoom != null) {
                    boolean needToAdd = true;
                    Log.i(TAG, "Joined room: " + joinedRoom);
                    //check is already added
                    int position = 0;
                    for (Room cekRoom : joinedRoomList) {
                        if (cekRoom.getId().equals(joinedRoom)) {
                            needToAdd = false;
                            if (resultCode == Activity.RESULT_CANCELED) {
                                cekRoom.detachRoomRef();
                                joinedRoomList.remove(position);
                            }
                            break;
                        }
                        position++;
                    }
                    Log.w(TAG, "Is need to add ? " + needToAdd);
                    if (needToAdd && resultCode == Activity.RESULT_OK) {
                        Room room = new Room(joinedRoom);
                        room.attachRoomRef();
                        joinedRoomList.add(room);
                    }
                }
            } else {
                Log.e(TAG, "NOT added to joined room list");
            }
        }
    }

}
