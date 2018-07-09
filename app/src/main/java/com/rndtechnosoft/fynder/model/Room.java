package com.rndtechnosoft.fynder.model;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.rndtechnosoft.fynder.adapter.RoomAdapter;
import com.rndtechnosoft.fynder.fragment.RoomFragment;
import com.rndtechnosoft.fynder.model.event.OnChatRoomEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ravi on 11/20/2016.
 */

@IgnoreExtraProperties
public class Room {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private String inviterName;
    private int numMembers;
    private boolean myRoom;
    private Chat chat;


    private double latitude;
    private double longitude;
    private String city;
    private String country;
    private String state;

    private long timeNewMsg;
    private long timeJoined;
    private long timePosted;
    private Intent intent;
    private DatabaseReference profileRef;
    private DatabaseReference roomNameRef;
    private DatabaseReference roomDescRef;
    private DatabaseReference chatRef;
    private DatabaseReference myJoinedRef;
    private DatabaseReference invitedRef;
    private DatabaseReference profileInviterRef;
    private DatabaseReference roomImageRef;
    private ValueEventListener profileListener;
    private ValueEventListener roomNameListener;
    private ValueEventListener roomCountryListener;
    private ValueEventListener roomLatitudeListener;
    private ValueEventListener roomLogitudeListener;
    private ValueEventListener roomStateListener;
    private ValueEventListener roomCityListener;
    private ValueEventListener roomDescListener;
    private ChildEventListener chatListener;
    private ValueEventListener myJoinedListener;
    private ValueEventListener invitationListener;
    private ValueEventListener profileInviterListener;
    private ValueEventListener roomImageListener;
    private DatabaseReference roomLatitude;
    private DatabaseReference roomLogitude;
    private DatabaseReference roomCity;
    private DatabaseReference roomState;
    private DatabaseReference roomCountry;

    public Room() {
    }

    public Room(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getInviterName() {
        return inviterName;
    }

    public void setInviterName(String inviterName) {
        this.inviterName = inviterName;
    }

    public int getNumMembers() {
        return numMembers;
    }

    public void setNumMembers(int numMembers) {
        this.numMembers = numMembers;
    }

    public boolean isMyRoom() {
        return myRoom;
    }

    public void setMyRoom(boolean myRoom) {
        this.myRoom = myRoom;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }


    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public void attachProfileRef(final RoomAdapter adapter) {
        if (id == null) {
            return;
        }
        profileRef = FirebaseDatabase.getInstance().getReference("users").child(id);
        roomNameRef = FirebaseDatabase.getInstance().getReference("rooms").child("public").child(id).child("name");
        roomDescRef = FirebaseDatabase.getInstance().getReference("rooms").child("public").child(id).child("description");
        roomImageRef = FirebaseDatabase.getInstance().getReference("rooms").child("public").child(id).child("image");
        roomLatitude = FirebaseDatabase.getInstance().getReference("rooms").child("public").child(id).child("latitude");
        roomLogitude = FirebaseDatabase.getInstance().getReference("rooms").child("public").child(id).child("longitude");
        roomCity = FirebaseDatabase.getInstance().getReference("rooms").child("public").child(id).child("city");
        roomState = FirebaseDatabase.getInstance().getReference("rooms").child("public").child(id).child("address");
        roomCountry = FirebaseDatabase.getInstance().getReference("rooms").child("public").child(id).child("country");

        profileListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User model = dataSnapshot.getValue(User.class);
                if(model == null){
                    return;
                }
                setImageUrl(model.getImageUrl());
                for (DataSnapshot election : dataSnapshot.getChildren()) {
                    if (election.getKey().equals("images")) {
                        String key = model.getImageUrl() == null ? "default" : model.getImageUrl();
                        DataSnapshot defaultImage = election.child(key);
                        if (defaultImage.exists()) {
                            UserImage userImage = defaultImage.getValue(UserImage.class);
                            setImageUrl(userImage.getThumbPic());
                            break;
                        }
                    }
                }
                Log.i("RoomFragment", "Image url: " + getImageUrl());
                adapter.notifyDataSetChanged();
                adapter.sort();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        roomNameListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setName(dataSnapshot.getValue(String.class));
//                Log.i("MainActivity", "From room listener: " + getName());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        roomDescListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setDescription(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        roomCountryListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    setCountry(dataSnapshot.getValue(String.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        roomStateListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    setState(dataSnapshot.getValue(String.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        roomCityListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    setCity(dataSnapshot.getValue(String.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        roomLatitudeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    setLatitude(dataSnapshot.getValue(double.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        roomLogitudeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    setLongitude(dataSnapshot.getValue(double.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        roomImageListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null){
                    profileRef.addValueEventListener(profileListener);
                }else{
                    RoomImage roomImage = dataSnapshot.getValue(RoomImage.class);
                    setImageUrl(roomImage.getThumbPic());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        roomImageRef.addValueEventListener(roomImageListener);
        roomNameRef.addValueEventListener(roomNameListener);
        roomDescRef.addValueEventListener(roomDescListener);
        roomLatitude.addValueEventListener(roomLatitudeListener);
        roomLogitude.addValueEventListener(roomLogitudeListener);
        roomCity.addValueEventListener(roomCityListener);
        roomCountry.addValueEventListener(roomCountryListener);
        roomState.addValueEventListener(roomStateListener);
    }

    public void attachRoomRef() {
        if (id == null) {
            return;
        }
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }
        String myUid = firebaseUser.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rooms").child("public").child(id);
        chatRef = ref.child("message");
        myJoinedRef = ref.child("participant").child(myUid);

        chatListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                chat.setId(dataSnapshot.getKey());
                setChat(chat);
                timeNewMsg = chat.getTimestamp();
                final DatabaseReference senderRef = FirebaseDatabase.getInstance().getReference("users").child(getChat().getFrom());
                final ValueEventListener senderListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User model = dataSnapshot.getValue(User.class);
                        if(model == null){
                            return;
                        }
                        getChat().setDisplayName(model.getName());
//                        Log.i("MainActivity", "From sender listener, display name: " + getChat().getDisplayName());
                        if (getName() != null && getImageUrl() != null && timeJoined > 0 && timeJoined < timeNewMsg && timePosted < timeNewMsg) {
//                            Log.i("MainActivity", "Triggered by Sender name listener");
                            EventBus.getDefault().post(new OnChatRoomEvent(Room.this));
                            timePosted = timeNewMsg;
                        }
                        senderRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                final DatabaseReference profileImageRef = FirebaseDatabase.getInstance().getReference("users").child(id);
                final ValueEventListener profileImageListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User model = dataSnapshot.getValue(User.class);
                        setImageUrl(model.getImageUrl());
                        for (DataSnapshot election : dataSnapshot.getChildren()) {
                            if (election.getKey().equals("images")) {
                                String key = model.getImageUrl() == null ? "default" : model.getImageUrl();
                                DataSnapshot defaultImage = election.child(key);
                                if (defaultImage.exists()) {
                                    UserImage userImage = defaultImage.getValue(UserImage.class);
                                    setImageUrl(userImage.getThumbPic());
                                    break;
                                }
                            }
                        }
                        if (getName() != null && getChat() != null && getChat().getDisplayName() != null
                                && timeJoined > 0 && timeJoined < timeNewMsg && timePosted < timeNewMsg) {
                            Log.i("MainActivity", "Triggered by Profile listener");
                            EventBus.getDefault().post(new OnChatRoomEvent(Room.this));
                            timePosted = timeNewMsg;
                        }
                        Log.i("RoomFragment", "Image url: " + getImageUrl());
                        profileImageRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                final DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child("public").child(id).child("name");
                final ValueEventListener roomListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        setName(dataSnapshot.getValue(String.class));
                        if (getChat() != null && getImageUrl() != null && getChat().getDisplayName() != null
                                && timeJoined > 0 && timeJoined < timeNewMsg && timePosted < timeNewMsg) {
                            Log.i("MainActivity", "Triggered by Room listener");
                            EventBus.getDefault().post(new OnChatRoomEvent(Room.this));
                            timePosted = timeNewMsg;
                        }
                        roomRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                senderRef.addValueEventListener(senderListener);
                profileImageRef.addValueEventListener(profileImageListener);
                roomRef.addValueEventListener(roomListener);

//                Log.i("MainActivity", "From chat listener -> room name: " + getName() + " , display name: " + getChat().getDisplayName());
                if (getChat().getDisplayName() != null && getName() != null && getImageUrl() != null
                        && timeJoined > 0 && timeJoined < timeNewMsg && timePosted < timeNewMsg) {
//                    Log.i("MainActivity", "Triggered by Chat listener");
                    EventBus.getDefault().post(new OnChatRoomEvent(Room.this));
                    timePosted = timeNewMsg;
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

        myJoinedListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                timeJoined = dataSnapshot.getValue(Long.class);
                Log.i("MainActivity", "My Time joined: " + timeJoined);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        chatRef.addChildEventListener(chatListener);
        myJoinedRef.addValueEventListener(myJoinedListener);

    }

    public void attachRoomInvitation(final RoomFragment roomFragment) {
        if (id == null) {
            return;
        }
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }
        Log.i("RoomFragment", "Attach room invitation : " + id + " , my uid: " + firebaseUser.getUid());
        invitedRef = FirebaseDatabase.getInstance().getReference("rooms").child("public").child(id)
                .child("roomInvitation").child(firebaseUser.getUid());
        invitationListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("RoomFragment", "Invitation => data: " + dataSnapshot.getValue());
                if (dataSnapshot.getValue() == null) {
                    roomFragment.delRoomInvitation(Room.this);
                } else {
                    roomFragment.addRoomInvitation(Room.this);
                    for (DataSnapshot election : dataSnapshot.getChildren()) {
                        if (election.getKey().equals("from")) {
                            String fromId = election.getValue(String.class);
                            profileInviterRef = FirebaseDatabase.getInstance().getReference("rooms")
                                    .child("public").child(fromId).child("name");
                            profileInviterListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    setInviterName(dataSnapshot.getValue(String.class));
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            };
                            profileInviterRef.addValueEventListener(profileInviterListener);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        invitedRef.addValueEventListener(invitationListener);
    }

    public void detachProfileRef() {
        if (profileRef != null && profileListener != null) {
            profileRef.removeEventListener(profileListener);
        }
        if (roomNameRef != null && roomNameListener != null) {
            roomNameRef.removeEventListener(roomNameListener);
        }
        if (roomDescRef != null && roomDescListener != null) {
            roomDescRef.removeEventListener(roomDescListener);
        }
        if (roomLatitude != null && roomLatitudeListener != null) {
            roomLatitude.removeEventListener(roomLatitudeListener);
        }
        if (roomLogitude != null && roomLogitudeListener != null) {
            roomLogitude.removeEventListener(roomLogitudeListener);
        }
        if (roomCity != null && roomCityListener != null) {
            roomCity.removeEventListener(roomCityListener);
        }
        if (roomState != null && roomStateListener != null) {
            roomState.removeEventListener(roomStateListener);
        }
        if (roomCountry != null && roomCountryListener != null) {
            roomCountry.removeEventListener(roomCountryListener);
        }
        if (roomImageRef != null && roomImageListener != null) {
            roomImageRef.removeEventListener(roomImageListener);
        }
        if (invitedRef != null && invitationListener != null) {
            invitedRef.removeEventListener(invitationListener);
        }
        if (profileInviterRef != null && profileInviterListener != null) {
            profileInviterRef.removeEventListener(profileInviterListener);
        }
    }

    public void detachRoomRef() {
//        if (profileRef != null && profileListener != null) {
//            profileRef.removeEventListener(profileListener);
//        }
//        if (roomRef != null && roomNameListener != null) {
//            roomRef.removeEventListener(roomNameListener);
//        }
        if (chatRef != null && chatListener != null) {
            chatRef.removeEventListener(chatListener);
        }
        if (myJoinedRef != null) {
            Log.i("RoomFragment", "Joined ref is not NULL");
            myJoinedRef.removeValue();
            if (myJoinedListener != null) {
                myJoinedRef.removeEventListener(myJoinedListener);
            }
        }

    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("timestamp", ServerValue.TIMESTAMP);
        result.put("name", name);
        result.put("description", description);
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("city", city);
        result.put("address", state);
        result.put("country", country);
        return result;
    }
}
