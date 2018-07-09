package com.rndtechnosoft.fynder.model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.rndtechnosoft.fynder.adapter.MemberAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ravi on 11/6/2016.
 */

@IgnoreExtraProperties
public class UserImage {
    private long timestamp;
    private String id;
    private String name;
    private String thumbPic;
    private String originalPic;
    private String token;
    private DatabaseReference profileRef;
    private DatabaseReference tokenRef;
    private ValueEventListener profileListener;
    private ValueEventListener tokenListener;
    private boolean mainImage;
    private boolean selected;

    // Default constructor required for calls to
    // DataSnapshot.getValue(UserImage.class)
    public UserImage() {
    }

    public UserImage(String thumbPic, String originalPic) {
        this.thumbPic = thumbPic;
        this.originalPic = originalPic;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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

    public String getThumbPic() {
        return thumbPic;
    }

    public void setThumbPic(String thumbPic) {
        this.thumbPic = thumbPic;
    }

    public String getOriginalPic() {
        return originalPic;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setOriginalPic(String originalPic) {
        this.originalPic = originalPic;
    }

    public boolean isMainImage() {
        return mainImage;
    }

    public void setMainImage(boolean mainImage) {
        this.mainImage = mainImage;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void attachProfileListener(final MemberAdapter adapter, final int position) {
        if (id == null) {
            return;
        }
        profileRef = FirebaseDatabase.getInstance().getReference("users").child(id);
        tokenRef = FirebaseDatabase.getInstance().getReference("token").child(id);
        profileListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User model = dataSnapshot.getValue(User.class);
                if(model == null){
                    return;
                }
                setName(model.getName());
                for (DataSnapshot election : dataSnapshot.getChildren()) {
                    if (election.getKey().equals("images")) {
                        String key = model.getImageUrl() == null ? "default" : model.getImageUrl();
                        DataSnapshot defaultImage = election.child(key);
                        if (defaultImage.exists()) {
                            UserImage userImage = defaultImage.getValue(UserImage.class);
                            setThumbPic(userImage.getThumbPic());
                            setOriginalPic(userImage.getOriginalPic());
                            break;
                        }
                    }
                }
                adapter.notifyItemChanged(position);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        tokenListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setToken(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        profileRef.addValueEventListener(profileListener);
        tokenRef.addValueEventListener(tokenListener);
    }

    public void detachProfileRef() {
        if (profileRef != null && profileListener != null) {
//            Log.w("GlobalRoom","on Detact listener..");
            profileRef.removeEventListener(profileListener);
        }
        if(tokenRef != null && tokenListener != null){
            tokenRef.removeEventListener(tokenListener);
        }
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("timestamp", ServerValue.TIMESTAMP);
        result.put("thumbPic", thumbPic);
        result.put("originalPic", originalPic);
        return result;
    }
}
