package com.rndtechnosoft.fynder.model;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rndtechnosoft.fynder.adapter.ParticipantAdapter;
import com.rndtechnosoft.fynder.adapter.UsersAdapter;
import com.rndtechnosoft.fynder.utility.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ravi on 11/5/2016.
 */

@IgnoreExtraProperties
public class User {
    private String id;
    private String relationshipId;
    private String name;
    private String gender;
    private String email;
    private String imageUrl;
    private String birthday;
    private String description;
    private List<String> token = new ArrayList<>();
    private String bio;
    private String relationshipStatus;
    private double latitude;
    private double longitude;
    private String city;
    private String country;
    private String state;



    private String lookingFor;
    private String interestedIn;

    private boolean selected;
    private DatabaseReference profileRef;
    private Query tokenRef;
    private ValueEventListener profileListener;
    private ChildEventListener tokenListener;

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public User() {
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

    public User(String id){
        this.id = id;
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRelationshipId() {
        return relationshipId;
    }

    public void setRelationshipId(String relationshipId) {
        this.relationshipId = relationshipId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }


    public List<String> getToken() {
        return token;
    }public void setToken(List<String> token) {
        this.token = token;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getRelationshipStatus() {
        return relationshipStatus;
    }

    public void setRelationshipStatus(String relationshipStatus) {
        this.relationshipStatus = relationshipStatus;
    }

    public String getLookingFor() {
        return lookingFor;
    }

    public void setLookingFor(String lookingFor) {
        this.lookingFor = lookingFor;
    }

    public String getInterestedIn() {
        return interestedIn;
    }

    public void setInterestedIn(String interestedIn) {
        this.interestedIn = interestedIn;
    }

    public void attachTokenRef(){
        if(id != null){
            getToken().clear();
//            tokenRef = FirebaseDatabase.getInstance().getReference("token").child(id);
            tokenRef = FirebaseDatabase.getInstance().getReference("token").orderByChild("userId").equalTo(id);
            tokenListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Token token = dataSnapshot.getValue(Token.class);
                    if (token != null) {
                        getToken().add(token.getToken());
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Token token = dataSnapshot.getValue(Token.class);
                    if (token != null) {
                        getToken().remove(token.getToken());
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            tokenRef.addChildEventListener(tokenListener);
        }
    }

    public void attachProfileRef(final UsersAdapter adapter) {
        if (id != null) {
            profileRef = FirebaseDatabase.getInstance().getReference("users").child(id);
            profileListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User model = dataSnapshot.getValue(User.class);
                    if(model == null){
                        return;
                    }
                    setName(model.getName());
                    setBirthday(model.getBirthday());
                    setGender(model.getGender());
                    setLatitude(model.getLatitude());
                    setLongitude(model.getLongitude());
                    setCity(model.getCity());
                    setCountry(model.getCountry());
                    setState(model.getState());
                    setImageUrl(model.getImageUrl());
                    for (DataSnapshot election : dataSnapshot.getChildren()) {
                        if (election.getKey().equals("images")) {
                            String key = model.getImageUrl() == null ? "default" : model.getImageUrl();
                            DataSnapshot defaultImage = election.child(key);
                            if (defaultImage.exists()) {
                                UserImage userImage = defaultImage.getValue(UserImage.class);
                                setImageUrl(userImage.getOriginalPic());
                                break;
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    adapter.sort();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            profileRef.addValueEventListener(profileListener);
        }
    }

    public void detachProfileRef() {
        if (profileRef != null && profileListener != null) {
            Log.w("NearbyHomeActivity.log", "on Detact listener..");
            profileRef.removeEventListener(profileListener);
        }
    }

    public void detachTokenRef(){
        if(tokenRef != null && tokenListener != null){
            tokenRef.removeEventListener(tokenListener);
        }
    }
}
