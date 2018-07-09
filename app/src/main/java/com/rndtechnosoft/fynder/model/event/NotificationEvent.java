package com.rndtechnosoft.fynder.model.event;

import android.content.Intent;

/**
 * Created by Ravi on 12/27/2016.
 */

public class NotificationEvent {
    public final static int TYPE_SINGLE_CHAT = 1;
    public final static int TYPE_PROFILE = 2;
    public final static int TYPE_ROOM_JOIN = 3;
    public final static int TYPE_ROOM_MESSAGE = 4;
    public final static int TYPE_ROOM_INVITE = 5;
    public final String message;
    public final Intent intent;
    public final int id;
    public final String tag;
    private String imageUrl;
    private String displayName;
    private String title;

    public NotificationEvent(int id,String tag,String message, Intent intent){
        this.id = id;
        this.tag = tag;
        this.message = message;
        this.intent = intent;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
