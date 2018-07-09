package com.rndtechnosoft.fynder.model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ravi on 2/15/2017.
 */

public class RoomInvitation {
    private String from;
    private String subject;
    private String displayName;
    private long timestamp;
    private DatabaseReference profileRef;
    private ValueEventListener profileListener;

    public RoomInvitation(){}

    public RoomInvitation(DataSnapshot dataSnapshot){
        setSubject(dataSnapshot.getKey());
        DataSnapshot dataValue = dataSnapshot.child(getSubject());
        for (DataSnapshot election : dataValue.getChildren()) {
            if (election.getKey().equals("from")) {
                setFrom(election.getValue(String.class));
            }else if (election.getKey().equals("timestamp")) {
                setTimestamp(election.getValue(Long.class));
            }
        }
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("timestamp", ServerValue.TIMESTAMP);
        result.put("from", from);

        return result;
    }

    public void attachProfile(){
        if(getFrom() == null){
            return;
        }
        profileRef = FirebaseDatabase.getInstance().getReference("users").child(getFrom()).child("name");
        profileListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setDisplayName(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        profileRef.addValueEventListener(profileListener);
    }

    public void detachProfile(){
        if(profileRef != null && profileListener != null){
            profileRef.removeEventListener(profileListener);
        }
    }
}
