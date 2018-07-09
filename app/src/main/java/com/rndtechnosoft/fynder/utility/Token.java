package com.rndtechnosoft.fynder.utility;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 *  on 5/7/2017.
 */

@IgnoreExtraProperties
public class Token {
    private String userId;
    private String deviceId;
    private String token;

    public Token(){}

    public Token(String userId, String token){
        this.userId = userId;
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
