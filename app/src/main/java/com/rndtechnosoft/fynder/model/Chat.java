package com.rndtechnosoft.fynder.model;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.rndtechnosoft.fynder.adapter.ChatAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ravi on 11/11/2016.
 */
@IgnoreExtraProperties
public class Chat {
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_AUDIO = 2;
    public static final int TYPE_VIDEO = 3;
    public static final int TYPE_DATE = 4;
    public static final int TYPE_JOIN = 5;
    public static final int TYPE_LEFT = 6;
    public static final int TYPE_UNREAD_MESSAGE = 7;
    public static final int TYPE_AD = 8;
    public static final int DELIVERED = 1;
    public static final int READ = 2;
    private String id;
    private long timestamp;
    private String from;
    private String to;
    private int type = 0;
    private String body;
    private String url;
    private String urlThumbnail;
    private String path;
    private String duration;
    private String replyMessage;


    private String adName;
    private int status;
    private String displayName;
    private String profilePic;
    private String profilePicLarger;
    private Chat replyChat;
    private boolean pushed;
    private DatabaseReference profileRef;
    private ValueEventListener profileListener;

    public Chat() {
    }

    public String getId() {
        return id;
    }

    public String getAdName() {
        return adName;
    }

    public void setAdName(String adName) {
        this.adName = adName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlThumbnail() {
        return urlThumbnail;
    }

    public void setUrlThumbnail(String urlThumbnail) {
        this.urlThumbnail = urlThumbnail;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getProfilePicLarger() {
        return profilePicLarger;
    }

    public Chat getReplyChat() {
        return replyChat;
    }

    public void setReplyChat(Chat replyChat) {
        this.replyChat = replyChat;
    }

    public void setProfilePicLarger(String profilePicLarger) {
        this.profilePicLarger = profilePicLarger;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isPushed() {
        return pushed;
    }

    public void setPushed(boolean pushed) {
        this.pushed = pushed;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getReplyMessage() {
        return replyMessage;
    }

    public void setReplyMessage(String replyMessage) {
        this.replyMessage = replyMessage;
    }

    public void attachProfileListener(final ChatAdapter adapter, final int position) {
        if (getFrom() != null) {
            profileRef = FirebaseDatabase.getInstance().getReference("users").child(getFrom());
            profileListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User model = dataSnapshot.getValue(User.class);
                    if(model == null){
                        return;
                    }
                    setDisplayName(model.getName());
                    for (DataSnapshot election : dataSnapshot.getChildren()) {
                        if (election.getKey().equals("images")) {
                            String key = model.getImageUrl() == null ? "default" : model.getImageUrl();
                            DataSnapshot defaultImage = election.child(key);
                            if (defaultImage.exists()) {
                                UserImage userImage = defaultImage.getValue(UserImage.class);
                                setProfilePic(userImage.getThumbPic());
                                setProfilePicLarger(userImage.getOriginalPic());
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
            profileRef.addValueEventListener(profileListener);
        }
    }

    public void detachProfileRef() {
        if (profileRef != null && profileListener != null) {
            profileRef.removeEventListener(profileListener);
        }
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("timestamp", ServerValue.TIMESTAMP);
        result.put("from", from);
        result.put("to", to);
        result.put("body", body);
        result.put("type", type);
        result.put("adName", adName);
        result.put("url", url);
        result.put("urlThumbnail", urlThumbnail);
        result.put("path",path);
        result.put("duration",duration);
        result.put("replyMessage",replyMessage);
        if(to != null){
            result.put("status", status);
        }

        return result;
    }

}
