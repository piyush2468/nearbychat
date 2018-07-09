package com.rndtechnosoft.fynder.model.event;

import com.rndtechnosoft.fynder.model.Room;

/**
 * Created by Ravi on 11/27/2016.
 */

public class OnChatRoomEvent {
    public final Room room;

    public OnChatRoomEvent(Room room){
        this.room = room;
    }
}
