package com.rndtechnosoft.fynder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.NotificationInfo;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.rndtechnosoft.fynder.activity.NearbyHomeActivity;
import com.rndtechnosoft.fynder.activity.ProfileActivity;
import com.rndtechnosoft.fynder.activity.RoomActivity;
import com.rndtechnosoft.fynder.activity.SingleChatActivity;
import com.rndtechnosoft.fynder.model.Relationship;
import com.rndtechnosoft.fynder.model.event.NotificationEvent;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Map;

/**
 * Created by Ravi on 12/24/2016.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private final String TAG = "FCM";
    private int type = 0;
    private String id;
    private String title;
    private String message;
    private String imageUrl;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> mapData = remoteMessage.getData();
        if (mapData == null) {
            Log.e(TAG, "Map data is NULL");
            return;
        }

        Bundle extras = new Bundle();
        for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
            extras.putString(entry.getKey(), entry.getValue());
        }

        NotificationInfo info = CleverTapAPI.getNotificationInfo(extras);

        if (info.fromCleverTap) {
            CleverTapAPI.createNotification(getApplicationContext(), extras);
            return;
        } else {
            // not from CleverTap handle yourself or pass to another provider
        }

        id = mapData.get("id");
        message = mapData.get("message");
        title = mapData.get("title");
        String fromId = mapData.get("uid");
        String messageId = mapData.get("message_id");
        String displayName = mapData.get("display_name");
        imageUrl = mapData.get("image_url");
        try {
            type = Integer.parseInt(mapData.get("type"));
        } catch (NumberFormatException e) {
            Log.e(TAG, "Type error => " + e.getMessage());
            return;
        }
        if (id == null) {
            Log.e(TAG, "ID is null");
            return;
        }

        Log.d(TAG, "FirebaseMessagingService => from: " + fromId + " , title: " + title + " , message: " + message + " , image url: " + imageUrl);
        SharedPreferences sharedpreferences = getSharedPreferences(getString(R.string.KEY_PREF_NAME), Context.MODE_PRIVATE);
        if (type == NotificationEvent.TYPE_SINGLE_CHAT) { //Single chat notification
            String keyLastMsg = String.format(getString(R.string.USER_SINGLE_CHAT_LAST_MESSAGE), fromId);
            String keyCountMsg = String.format(getString(R.string.USER_SINGLE_CHAT_COUNT), fromId);
            if(!SingleChatActivity.active) {
                String storedLastMsg = sharedpreferences.getString(keyLastMsg, "");
                int storedCountMsg = sharedpreferences.getInt(keyCountMsg, 0);
                int countMessage = storedCountMsg + 1;
                String countMsg = String.format(getString(R.string.new_messages), countMessage);
                String storeMessage = countMessage > 1 ? storedLastMsg + "\n" + message : message;
                String lastMessage = countMessage > 1 ? countMsg + "\n" + storeMessage : "";
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(keyLastMsg, storeMessage);
                editor.putInt(keyCountMsg, countMessage);
                editor.apply();

                final Intent intent = new Intent(this, SingleChatActivity.class);
                intent.putExtra(SingleChatActivity.KEY_FRIEND_ID, fromId);
                intent.putExtra(SingleChatActivity.KEY_TITLE, title);
                intent.putExtra(SingleChatActivity.KEY_RELATIONSHIP_ID, id);

                if (countMessage > 1) {
                    message = countMsg;
                }

                loadNotification(intent, 2, message, lastMessage);
                //get last message to local store
                if (messageId != null) {
                    final DatabaseReference singleChatRef = FirebaseDatabase.getInstance().getReference("chats")
                            .child(id).child(messageId);
                    singleChatRef.keepSynced(true);
                    singleChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.w(TAG, "Update message: " + dataSnapshot);
                            singleChatRef.removeEventListener(this);
                            singleChatRef.keepSynced(false);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }else{
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(keyLastMsg, "");
                editor.putInt(keyCountMsg, 0);
                editor.apply();
            }
        } else if (type == NotificationEvent.TYPE_ROOM_JOIN) {
            Intent intent = new Intent(this, RoomActivity.class);
            intent.putExtra(RoomActivity.KEY_ID, id);
            intent.putExtra(RoomActivity.KEY_ROOM_NAME, title);
            title = "Room: " + title;
            Log.i(TAG, "Room => Join state");
            String keyJoinMsg = String.format(getString(R.string.USER_ROOM_JOIN_MESSAGE), id);
            String keyJoinUserId = String.format(getString(R.string.USER_ROOM_JOIN_USER_ID), id);
            String keyJoinCount = String.format(getString(R.string.USER_ROOM_JOIN_COUNT), id);
            String storedJoinMsg = sharedpreferences.getString(keyJoinMsg, "");
            String storedJoinUserIds = sharedpreferences.getString(keyJoinUserId, null);
            int countJoin = sharedpreferences.getInt(keyJoinCount, 0);
//                int countJoin = storedJoinCount;

            if (storedJoinUserIds != null) {
                try {
                    boolean isUserExist = false;
                    JSONArray jsonUserId = new JSONArray(storedJoinUserIds);
                    for (int i = 0; i < jsonUserId.length(); i++) {
                        String userId = jsonUserId.getString(i);
                        Log.i(TAG, "Check user Id: " + userId);
                        if (userId.equals(fromId)) {
                            isUserExist = true;
                            break;
                        }
                    }
                    if (!isUserExist) {
                        jsonUserId.put(fromId);
                        storedJoinUserIds = jsonUserId.toString();
                        countJoin++;
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error Parsing JSON User id: " + e.getMessage());
                }
            } else {
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(fromId);
                storedJoinUserIds = jsonArray.toString();
            }

//                int countJoin = storedJoinCount + 1;
            String countJoinMsg = String.format(getString(R.string.new_participants_join), countJoin);
            String participant = String.format(getString(R.string.participant_join), displayName);
            String storeMessage = countJoin > 1 ? storedJoinMsg + "\n" + participant : participant;
            String bigMessage = countJoin > 1 ? countJoinMsg + "\n" + storeMessage : "";
            String bodyMessage = countJoin > 1 ? countJoinMsg : participant;
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(keyJoinMsg, storeMessage);
            editor.putString(keyJoinUserId, storedJoinUserIds);
            editor.putInt(keyJoinCount, countJoin);
            editor.apply();

            message = participant;
            loadNotification(intent, 1, bodyMessage, bigMessage);
        } else if (type == NotificationEvent.TYPE_ROOM_MESSAGE) {
            Intent intent = new Intent(this, RoomActivity.class);
            intent.putExtra(RoomActivity.KEY_ID, id);
            intent.putExtra(RoomActivity.KEY_ROOM_NAME, title);
            title = "Room: " + title;
            Log.i(TAG, "Room => Incoming Message");
            String keyLastMsg = String.format(getString(R.string.USER_ROOM_NEW_INCOMING_MESSAGE), id);
            String keyCountMsg = String.format(getString(R.string.USER_ROOM_NEW_INCOMING_COUNT), id);
            String storedLastMsg = sharedpreferences.getString(keyLastMsg, "");
            int storedCountMsg = sharedpreferences.getInt(keyCountMsg, 0);
            int countMessage = storedCountMsg + 1;
            String countMsg = String.format(getString(R.string.new_messages), countMessage);
            String name = displayName.length() <= 8 ? displayName : displayName.substring(0, 5) + "...";
            String body = name + " : " + message;
            String storeMessage = countMessage > 1 ? storedLastMsg + "\n" + body : body;
            String lastMessage = countMessage > 1 ? countMsg + "\n" + storeMessage : "";
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(keyLastMsg, storeMessage);
            editor.putInt(keyCountMsg, countMessage);
            editor.apply();

            message = countMessage > 1 ? countMsg : body;

            loadNotification(intent, 2, message, lastMessage);
            //get last message to local store
            if (messageId != null) {
                final DatabaseReference chatRoomRef = FirebaseDatabase.getInstance().getReference("rooms")
                        .child("public").child(id).child("message").child(messageId);
                chatRoomRef.keepSynced(true);
                chatRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.w(TAG, "Update message: " + dataSnapshot);
                        chatRoomRef.removeEventListener(this);
                        chatRoomRef.keepSynced(false);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        } else if (type == NotificationEvent.TYPE_ROOM_INVITE) {
            Intent intent = new Intent(this, RoomActivity.class);
            intent.putExtra(RoomActivity.KEY_ID, id);
            intent.putExtra(RoomActivity.KEY_ROOM_NAME, displayName);
            loadNotification(intent, 1, message, null);
            final DatabaseReference invitedRef = FirebaseDatabase.getInstance().getReference("rooms")
                    .child("public").child(id).child("roomInvitation");
            invitedRef.keepSynced(true);
            invitedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.w(TAG, "Update invitation data: " + dataSnapshot);
                    invitedRef.removeEventListener(this);
                    invitedRef.keepSynced(false);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else if (type == NotificationEvent.TYPE_PROFILE) { //
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra(ProfileActivity.KEY_UID, id);
            intent.putExtra(ProfileActivity.KEY_DISPLAY_NAME, title);
            loadNotification(intent, 2, getProfileMessage(message), null);

            if(messageId != null){
                final DatabaseReference invitedRef = FirebaseDatabase.getInstance().getReference("relationship")
                        .child(messageId);
                invitedRef.keepSynced(true);
                invitedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.w(TAG, "Update invitation data: " + dataSnapshot);
                        invitedRef.removeEventListener(this);
                        invitedRef.keepSynced(false);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
//        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
    }

    private String getProfileMessage(String statusString) {
        int status;
        try {
            status = Integer.parseInt(statusString);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parse num from invitation: " + e.getMessage());
            return "";
        }
        if (status == Relationship.PENDING) {
            return getString(R.string.new_invitation);
        } else if (status == Relationship.ACCEPTED) {
            return getString(R.string.invitation_accepted);
        } else if (status == Relationship.DECLINED) {
            return getString(R.string.invitation_declined);
        } else if (status == Relationship.BLOCKED) {
            return getString(R.string.invitation_blocked);
        } else {
            return "";
        }
    }

    private void loadNotification(final Intent intent, final int tabPosition, final String bodyMessage,
                                  final String bigMessage) {
        if (imageUrl != null) {
            ImageRequest imageRequest = ImageRequestBuilder
                    .newBuilderWithSource(Uri.parse(imageUrl))
                    .setProgressiveRenderingEnabled(true)
                    .build();
            ImagePipeline imagePipeline = Fresco.getImagePipeline();
            DataSource<CloseableReference<CloseableImage>>
                    dataSource = imagePipeline.fetchDecodedImage(imageRequest, this);
            dataSource.subscribe(new BaseBitmapDataSubscriber() {
                @Override
                public void onNewResultImpl(@Nullable final Bitmap bitmap) {
                    // You can use the bitmap in only limited ways
                    // No need to do any cleanup.
                    Log.i(TAG, "Download -> FINISH = bitmap is null: " + (bitmap == null));
                    doNotification(intent, tabPosition, bodyMessage, bigMessage, bitmap);
                }

                @Override
                public void onFailureImpl(DataSource dataSource) {
                    // No cleanup required here.
                    Log.e(TAG, "Download -> Error get bitmap: " + dataSource.getFailureCause().getMessage());
                    doNotification(intent, tabPosition, bodyMessage, bigMessage, null);
                }
            }, CallerThreadExecutor.getInstance());
        } else {
            doNotification(intent, tabPosition, bodyMessage, bigMessage, null);
        }
    }

    private void doNotification(Intent intent, int tabPosition, String contentMessage, String bigMessage, Bitmap icon) {
        Intent backIntent = new Intent(this, NearbyHomeActivity.class);
        backIntent.putExtra(NearbyHomeActivity.KEY_POSITION_TAB, tabPosition);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pIntent = PendingIntent.getActivities(this, 0,
                new Intent[]{backIntent, intent}, PendingIntent.FLAG_UPDATE_CURRENT);
        boolean isOnBackground = FynderApplication.started <= FynderApplication.stopped;
        Log.i(TAG, "Is On background: " + isOnBackground);
        if (isOnBackground) {
            NotificationCompat.Builder mNotification =
                    new NotificationCompat.Builder(this)
                            .setAutoCancel(true)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentTitle(title)
                            .setContentText(contentMessage)
                            .setContentIntent(pIntent);
            if (icon != null) {
                //resize bitmap
//            icon = Bitmap.createScaledBitmap(icon, 100, 100, false);
                //set bitmap
                mNotification.setLargeIcon(icon);
            }
            if (bigMessage != null && !bigMessage.isEmpty()) {
                mNotification.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(bigMessage));
            }
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification myNotification = mNotification.build();
            myNotification.defaults = Notification.DEFAULT_ALL;
            notificationManager.notify(id, type, myNotification);
        } else {
            String titleMessage = null;
            String bodyMessage = message;
            if (type == NotificationEvent.TYPE_SINGLE_CHAT) {
                titleMessage = getString(R.string.private_message);
            } else if (type == NotificationEvent.TYPE_PROFILE) {
                titleMessage = getString(R.string.friend_information);
                bodyMessage = getProfileMessage(message);
            } else if (type == NotificationEvent.TYPE_ROOM_JOIN ||
                    type == NotificationEvent.TYPE_ROOM_MESSAGE) {
                titleMessage = getString(R.string.chat_room);
            }
            NotificationEvent notificationEvent = new NotificationEvent(type, id, bodyMessage, intent);
            notificationEvent.setImageUrl(imageUrl);
            notificationEvent.setDisplayName(title);
            notificationEvent.setTitle(titleMessage);
            EventBus.getDefault().post(notificationEvent);
        }
    }
}
