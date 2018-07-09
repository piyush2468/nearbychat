package com.rndtechnosoft.fynder.model;

/**
 * Created by Ravi on 12/21/2016.
 */

public class UserBlocked {
    private long timestamp;
    private String uid;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
