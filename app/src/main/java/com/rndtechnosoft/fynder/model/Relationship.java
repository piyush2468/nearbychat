package com.rndtechnosoft.fynder.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ravi on 11/14/2016.
 */

@IgnoreExtraProperties
public class Relationship {
    public static final int PENDING = 0;
    public static final int ACCEPTED = 1;
    public static final int DECLINED = 2;
    public static final int BLOCKED = 3;
    private long timestamp;
    private String id;
    private String following;
    private String follower;
    private int status = -1; //pending / accepted / declined / blocked
    private String actionUid;

    public Relationship(){}

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

    public String getFollowing() {
        return following;
    }

    public void setFollowing(String following) {
        this.following = following;
    }

    public String getFollower() {
        return follower;
    }

    public void setFollower(String follower) {
        this.follower = follower;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getActionUid() {
        return actionUid;
    }

    public void setActionUid(String actionUid) {
        this.actionUid = actionUid;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("timestamp", ServerValue.TIMESTAMP);
        result.put("following", following);
        result.put("follower", follower);
        result.put("actionUid", actionUid);
        result.put("status", status);
        return result;
    }
}
