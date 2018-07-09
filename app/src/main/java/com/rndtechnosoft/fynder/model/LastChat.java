package com.rndtechnosoft.fynder.model;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.adapter.LastChatAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ravi on 12/6/2016.
 */

public class LastChat {
    private String relationshipId;
    private String uid;
    private String displayName;
    private String imageUrl;
    private String lastMessage;
    private long lastTimestamp;
    private int countBadge;
    private int lastStatus;
    private boolean sender;
    private DatabaseReference profileRef;
    private DatabaseReference chatRef;
    private ValueEventListener profileListener;
    private ValueEventListener chatListener;

    public LastChat(String relationshipId, String uid) {
        this.relationshipId = relationshipId;
        this.uid = uid;
    }

    public String getRelationshipId() {
        return relationshipId;
    }

    public String getUid() {
        return uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }

    public void setLastTimestamp(long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    public int getCountBadge() {
        return countBadge;
    }

    public void setCountBadge(int countBadge) {
        this.countBadge = countBadge;
    }

    public boolean isSender() {
        return sender;
    }

    public void setSender(boolean sender) {
        this.sender = sender;
    }

    public int getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(int lastStatus) {
        this.lastStatus = lastStatus;
    }

    public void attachRef(final LastChatAdapter adapter, final int position) {
        if (relationshipId == null || uid == null) {
            return;
        }
        //profile ref for display name and image profile update
        profileRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        profileListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User model = dataSnapshot.getValue(User.class);
                if(model == null){
                    return;
                }
                setDisplayName(model.getName());
                setImageUrl(model.getImageUrl());
                Log.i("PrivateFragment", "Display name: " + getDisplayName());
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
//                adapter.notifyItemChanged(position);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        chatRef = FirebaseDatabase.getInstance().getReference("chats").child(relationshipId);
        chatListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                Log.i("LastChat","Count: "+dataSnapshot.getChildrenCount());
                if(dataSnapshot.getChildrenCount() == 0){
                    setLastMessage(adapter.getContext().getString(R.string.start_conversation));
                    adapter.notifyItemChanged(position);
                    return;
                }
                List<Chat> list = new ArrayList<>();
                for(DataSnapshot election: dataSnapshot.getChildren()){
                    Chat chat = election.getValue(Chat.class);
                    chat.setId(dataSnapshot.getKey());
                    list.add(chat);
                }
                Chat lastChat = list.get(list.size()-1);
                setLastMessage(lastChat.getBody());
                setLastTimestamp(lastChat.getTimestamp());
                setSender(uid.equals(lastChat.getTo()));
                setLastStatus(lastChat.getStatus());
                if(uid.equals(lastChat.getFrom()) && lastChat.getStatus() == Chat.DELIVERED){ //left side and unread
//                    Log.i("LastChat", "Body: " + chat.getBody());
//                    countUnread++;
                    int countUnread = getCountBadge() + 1;
                    setCountBadge(countUnread);
                    adapter.setOnTabUpdate();
                }else{
                    setCountBadge(0);
                    adapter.setOnTabUpdate();
                }
                if(getLastMessage() == null){
                    adapter.remove(position);
                }else {
                    adapter.notifyDataSetChanged();
                    adapter.sort();
                }
//                Log.i("LastChat","Last message: "+lastChat.getBody());
//                chatRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
//        chatListener = new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                Chat chat = dataSnapshot.getValue(Chat.class);
//                chat.setId(dataSnapshot.getKey());
//                setLastMessage(chat.getBody());
//                setLastTimestamp(chat.getTimestamp());
//                setSender(uid.equals(chat.getTo()));
//                setLastStatus(chat.getStatus());
//                if(uid.equals(chat.getFrom()) && chat.getStatus() == Chat.DELIVERED){ //left side and unread
////                    Log.i("LastChat", "Body: " + chat.getBody());
////                    countUnread++;
//                    int countUnread = getCountBadge() + 1;
//                    setCountBadge(countUnread);
//                    adapter.setOnTabUpdate();
//                }
//                if(getLastMessage() == null){
//                    adapter.remove(position);
//                }else {
//                    adapter.notifyDataSetChanged();
//                    adapter.sort();
//                }
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        };
        profileRef.addValueEventListener(profileListener);
        chatRef.addValueEventListener(chatListener);
    }

    public void detachRef() {
        if (profileRef != null && profileListener != null) {
            profileRef.removeEventListener(profileListener);
        }
        if (chatRef != null && chatListener != null) {
            chatRef.removeEventListener(chatListener);
        }
    }
}
