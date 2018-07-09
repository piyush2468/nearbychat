package com.rndtechnosoft.fynder.activity;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rndtechnosoft.fynder.FynderApplication;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.adapter.ChatAdapter;
import com.rndtechnosoft.fynder.adapter.MemberAdapter;
import com.rndtechnosoft.fynder.dialog.BlockedUserDialog;
import com.rndtechnosoft.fynder.dialog.ProfileDialog;
import com.rndtechnosoft.fynder.dialog.ReplyMessageInputView;
import com.rndtechnosoft.fynder.dialog.RoomDisplayDialog;
import com.rndtechnosoft.fynder.dialog.RoomFriendsInvitationDialog;
import com.rndtechnosoft.fynder.dialog.SubscribersDialog;
import com.rndtechnosoft.fynder.dialog.VideoUploadDialog;
import com.rndtechnosoft.fynder.fragment.RoomFragment;
import com.rndtechnosoft.fynder.model.Chat;
import com.rndtechnosoft.fynder.model.Item;
import com.rndtechnosoft.fynder.model.Relationship;
import com.rndtechnosoft.fynder.model.Room;
import com.rndtechnosoft.fynder.model.RoomImage;
import com.rndtechnosoft.fynder.model.User;
import com.rndtechnosoft.fynder.model.UserBlocked;
import com.rndtechnosoft.fynder.model.UserImage;
import com.rndtechnosoft.fynder.model.event.NotificationEvent;
import com.rndtechnosoft.fynder.utility.Ad_Helper;
import com.rndtechnosoft.fynder.utility.Constants;
import com.rndtechnosoft.fynder.utility.MyDateUtil;
import com.rndtechnosoft.fynder.model.event.OnChatRoomEvent;
import com.rndtechnosoft.fynder.model.event.OnEnterEvent;
import com.rndtechnosoft.fynder.model.event.OnLeaveEvent;
import com.rndtechnosoft.fynder.utility.MyImageUtil;
import com.rndtechnosoft.fynder.utility.PushFCM;
import com.rndtechnosoft.fynder.utility.Token;
import com.rndtechnosoft.fynder.utility.image.bottompicker.ImageBottomPicker;
import com.rndtechnosoft.fynder.utility.listener.OnSuccessUploadListener;
import com.rndtechnosoft.fynder.utility.listener.PttOnTouchListener;
import com.rndtechnosoft.fynder.utility.listener.RecyclerTouchListener;
import com.rndtechnosoft.fynder.utility.listener.ToolbarActionBarCallback;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoomActivity extends AppCompatActivity {
    public static final String KEY_ID = RoomActivity.class.getName() + ".KEY_ID";
    public static final String KEY_ROOM_NAME = RoomActivity.class.getName() + ".KEY_NAME";
    public static final String KEY_IS_JOINED = RoomActivity.class.getName() + ".KEY_IS_JOINED";
    private final String TAG = "RoomActivity";
    public static final int STATUS_MY_SELF = -1;
    public static final int STATUS_READY_TO_INVITE = 0;
    public static final int STATUS_PENDING_INVITE = 1;
    public static final int STATUS_RECEIVED_INVITATION = 2;
    public static final int STATUS_GOT_DECLINED = 3;
    public static final int STATUS_DECLINE = 4;
    public static final int STATUS_GOT_BLOCKED = 5;
    public static final int STATUS_BLOCK = 6;
    public static final int STATUS_FRIEND = 7;
    private final int STATUS_GENERAL_ERROR = 8;
    private final int OPEN_IMAGE = 536;

    private ImageButton sendButton;
    private ImageButton audioButton;
    private ImageButton smileyButton;
    private EmojiEditText inputTextChat;
    private EmojiPopup emojiPopup;
    private RecyclerView membersView;
    private RecyclerView chatsView;
    private LinearLayoutManager linearLayoutManager;
    private LinearLayout inputReplyLayout;
    private MemberAdapter memberAdapter;
    private ChatAdapter chatAdapter;
    private ImageView imageDown;
    private TextView textBadge;
    private MenuItem menuSubscriber;
    private CardView invitedLayout;
    private SimpleDraweeView imageLogo;
    private SimpleDraweeView invitedProfileImage;
    private TextView invitedProfileName;
    private TextView invitedTime;
    private List<UserImage> memberList = new ArrayList<>();
    private List<Chat> chatList = new ArrayList<>();
    private List<UserBlocked> blockedUsers = new ArrayList<>();
    private List<Query> tokenSubscribers = new ArrayList<>();
    private DatabaseReference participantRef;
    private DatabaseReference chatRef;
    private DatabaseReference blockedUserRef;
    private DatabaseReference connectedRef;
    private DatabaseReference myProfileRef;
    private Query tokenRef;
    private DatabaseReference subscriberRef;
    private DatabaseReference invitedRef;
    private DatabaseReference invitedProfileRef;
    private DatabaseReference roomImageRef;
    private DatabaseReference profileRef;
    private StorageReference storageRef;
    private Query followingRef;
    private Query followerRef;
    private List<Relationship> relationships = new ArrayList<>();
    private SharedPreferences sharedPreferences;

    private ActionMode mActionMode;
    private RecyclerTouchListener recyclerTouchListener;

    private String roomId;
    private boolean isConnected;
    private boolean isShown;
    private boolean isJoined;
    private boolean isBlocked;
    private long myTimestamp = 0;
    private boolean isLastItemHidden = false;
    private int countBadge = 0;
    private String myImageUrl;
    private String myName;
    private String roomName;
    private String replyMessageId;

    private Animation animShowNewMessage;
    private Animation animHideNewMessage;
    private RelativeLayout newMessageLayout;
    private SimpleDraweeView newMessageImage;
    private TextView roomNewMessage;
    private TextView subjectNewMessage;
    private TextView textNewMessage;
    private List<Room> queueMessage = new ArrayList<>();

    private UploadTask uploadTask;
    private OnSuccessListener<UploadTask.TaskSnapshot> onSuccessUpload;

    private PushFCM pushFCM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        roomId = intent.getStringExtra(KEY_ID);
        isJoined = intent.getBooleanExtra(KEY_IS_JOINED, false);
        if (roomId == null) {
            finish();
            return;
        }

        setContentView(R.layout.activity_room);

        Log.i(TAG, "On Create => room Id: " + roomId);
        sharedPreferences = getSharedPreferences(getString(R.string.KEY_PREF_NAME), Context.MODE_PRIVATE);
        roomName = intent.getStringExtra(KEY_ROOM_NAME);
        FynderApplication.getInstance().sendScreenviwedevent(Constants.CT_ROOMDETAIL,roomName);
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child("public").child(roomId);
        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        roomImageRef = roomRef.child("image");
        participantRef = roomRef.child("participant");
        chatRef = roomRef.child("message");
        subscriberRef = roomRef.child("subscribers");
        blockedUserRef = roomRef.child("blocked_user");
        storageRef = FirebaseStorage.getInstance().getReference();
        sendButton = (ImageButton) findViewById(R.id.button_send);
        audioButton = (ImageButton) findViewById(R.id.button_audio);
        smileyButton = (ImageButton) findViewById(R.id.button_smiley);
        inputTextChat = (EmojiEditText) findViewById(R.id.input_text_chat);
        invitedLayout = (CardView) findViewById(R.id.invited_layout);
        invitedProfileImage = (SimpleDraweeView) findViewById(R.id.profile_image_invited);
        RoundingParams circle = RoundingParams.asCircle()
                .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY);
        invitedProfileImage.getHierarchy().setRoundingParams(circle);
        invitedProfileImage.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
        invitedProfileName = (TextView) findViewById(R.id.text_invited_from_name);
        invitedTime = (TextView) findViewById(R.id.text_invited_time);
        inputReplyLayout = (LinearLayout) findViewById(R.id.input_reply_layout);
        ViewGroup rootView = (ViewGroup) findViewById(R.id.activityRoot);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        membersView = (RecyclerView) findViewById(R.id.list_members);
        chatsView = (RecyclerView) findViewById(R.id.list_chat);
        animShowNewMessage = AnimationUtils.loadAnimation(this, R.anim.slide_right);
        animHideNewMessage = AnimationUtils.loadAnimation(this, R.anim.slide_left);
        newMessageLayout = (RelativeLayout) findViewById(R.id.layout_new_message);
        newMessageImage = (SimpleDraweeView) findViewById(R.id.icon_new_message);
        newMessageImage.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
        RoundingParams rounded = RoundingParams.fromCornersRadius(10)
                .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY);
        newMessageImage.getHierarchy().setRoundingParams(rounded);
        roomNewMessage = (TextView) findViewById(R.id.text_room);
        subjectNewMessage = (TextView) findViewById(R.id.text_subject);
        textNewMessage = (TextView) findViewById(R.id.text_new_message);
        imageDown = (ImageView) findViewById(R.id.icon_double_down);
        textBadge = (TextView) findViewById(R.id.text_badge_count);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        chatsView.setLayoutManager(linearLayoutManager);
        pushFCM = new PushFCM(this);
        pushFCM.setId(roomId);
        pushFCM.setTitle(roomName);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            invitedRef = roomRef.child("roomInvitation").child(firebaseUser.getUid());
            pushFCM.setUid(firebaseUser.getUid());
            pushFCM.setDisplayName(firebaseUser.getDisplayName());
            myProfileRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());
            chatAdapter = new ChatAdapter(this, chatList, firebaseUser.getUid());
            chatsView.setAdapter(chatAdapter);
        }

        recyclerTouchListener = new RecyclerTouchListener(this, chatsView, new RecyclerTouchListener.RecyclerOnClickListener() {
            @Override
            public void onClick(View view, int position) {
                if(mActionMode == null){
                    Log.e(TAG,"Action mode is NULL");
                    return;
                }
                Log.i(TAG,"Count selected: "+chatAdapter.getSelectedCount());
                if(chatAdapter.getSelectedCount() > 0){
                    chatAdapter.toggleSelection(position);
                    mActionMode.setTitle(String.valueOf(chatAdapter.getSelectedCount()));
                }

                if(chatAdapter.getSelectedCount() == 0){
                    mActionMode.finish();
                }else{
                    Menu menu = mActionMode.getMenu();
                    menu.findItem(R.id.action_reply).setVisible(chatAdapter.getSelectedCount() == 1);
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                Log.i(TAG,"OnLongClick on message: "+chatList.get(position).getBody());
                mActionMode = startSupportActionMode(new ToolbarActionBarCallback(RoomActivity.this, chatAdapter) {
                    @Override
                    public void onReply(Chat chat) {
                        Log.i(TAG,"Reply => Message: "+chat.getBody());
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        inputTextChat.requestFocus();

                        replyMessageId = chat.getId();
                        inputReplyLayout.addView(new ReplyMessageInputView(RoomActivity.this, chat,
                                Color.parseColor(chatAdapter.getColor(chat.getFrom()))));
                    }
                });
                chatAdapter.toggleSelection(position);
                if(chatAdapter.getSelectedCount() > 0){
                    mActionMode.setTitle(String.valueOf(chatAdapter.getSelectedCount()));
                }
                if(chatAdapter.getSelectedCount() == 0){
                    mActionMode.finish();
                }else{
                    Menu menu = mActionMode.getMenu();
                    menu.findItem(R.id.action_reply).setVisible(chatAdapter.getSelectedCount() == 1);
                }
            }
        });

        memberAdapter = new MemberAdapter(memberList, new MemberAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(UserImage item) {
                Item model = statusFriend(item.getId());
                Log.i(TAG, "Uid: " + item.getId() + " , Display name: " + item.getName() + " , status friend: " + model.getIndex());
                if (model.getIndex() == STATUS_GENERAL_ERROR) {
                    return;
                }
                DatabaseReference blockRef = null;
                if (!isBlockedUser(item.getId()) && !roomId.equals(item.getId())) {
                    Log.i(TAG, "Member block is visible");
                    blockRef = blockedUserRef.child(item.getId());
                }
                new ProfileDialog(RoomActivity.this, item, model, blockRef, myImageUrl, myName).show();
            }
        });
        membersView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        membersView.setAdapter(memberAdapter);

        TextView mTitle = (TextView) findViewById(R.id.text_title_bar);
        imageLogo = (SimpleDraweeView) findViewById(R.id.image_logo);
        imageLogo.getHierarchy().setRoundingParams(circle);
        imageLogo.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
        imageLogo.setVisibility(View.GONE);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        if (roomName != null) {
//            getSupportActionBar().setTitle(roomName);
            mTitle.setText(roomName);
        }

        emojiPopup = EmojiPopup.Builder.fromRootView(rootView)
                .setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
                    @Override
                    public void onEmojiPopupShown() {
                        smileyButton.setImageResource(R.drawable.ic_keyboard);
                    }
                }).setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
                    @Override
                    public void onEmojiPopupDismiss() {
                        smileyButton.setImageResource(R.drawable.ic_smiley);
                    }
                }).setOnSoftKeyboardCloseListener(new OnSoftKeyboardCloseListener() {
                    @Override
                    public void onKeyboardClose() {
                        emojiPopup.dismiss();
                    }
                }).build(inputTextChat);

        smileyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emojiPopup.toggle();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                if (!isConnected) {
                    Toast.makeText(RoomActivity.this, getString(R.string.error_internet), Toast.LENGTH_SHORT).show();
                    return;
                }
                Ad_Helper.adCountGlobal++;

                if(Ad_Helper.wannaSendGlobal(Ad_Helper.adCountGlobal)){
                    SendAd();
                }
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null) {
                    DatabaseReference updateRef = chatRef.push();
                    Chat chat = new Chat();
                    chat.setBody(inputTextChat.getText().toString());
                    chat.setFrom(firebaseUser.getUid());
                    chat.setReplyMessage(replyMessageId);
                    Map<String, Object> postValues = chat.toMap();
                    updateRef.updateChildren(postValues);
                    String messageId = updateRef.getKey();
                    Log.i(TAG, "Message id: " + messageId);
                    pushFCM.setMessageId(messageId);
                    pushFCM.setType(NotificationEvent.TYPE_ROOM_MESSAGE);
                    DatabaseReference ref = updateRef.child("posted");
                    pushFCM.push(chat.getBody(), ref);
                    setReplyMessageIdToNull();
                    scrollToBottom();
                    isLastItemHidden = false;
                } else {
                    Toast.makeText(RoomActivity.this, getString(R.string.error_send_text_message), Toast.LENGTH_SHORT).show();
                }
                inputTextChat.setText("");
            }
        });

        audioButton.setOnTouchListener(new PttOnTouchListener(this) {
            @Override
            public void onCompleteRecorded(final String audioFilePath) {
                Log.i(TAG, "On Complete audio recorded: " + audioFilePath);
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser == null) {
                    return;
                }
                File audioFile = new File(audioFilePath);
                if (!audioFile.exists()) {
                    Log.e(TAG, "Audio file path does not exist");
                    return;
                }

                Ad_Helper.adCountGlobal++;

                if(Ad_Helper.wannaSendGlobal(Ad_Helper.adCountGlobal)){
                    SendAd();
                }

                String tempKey = String.valueOf(System.currentTimeMillis());
                String uid = firebaseUser.getUid();
                Chat chat = new Chat();
                chat.setType(Chat.TYPE_AUDIO);
                chat.setFrom(uid);
                chat.setId(tempKey);
                chat.setReplyMessage(replyMessageId);
                chatList.add(chat);
                chatAdapter.notifyItemChanged(chatList.size() - 1);
                scrollToBottom();
                StorageReference audioRef = storageRef.child(uid + "/chat/audio/" + System.currentTimeMillis() + ".3gp");
                uploadTask = audioRef.putFile(Uri.fromFile(audioFile));
                onSuccessUpload = new OnSuccessUploadListener(tempKey) {
                    @Override
                    public void onSuccessUpload(String key, UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests")
                        Uri audioUrl = taskSnapshot.getDownloadUrl();
                        Log.i(TAG, "Success to upload audio, Url " + audioUrl);
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (firebaseUser == null) {
                            return;
                        }
                        String uid = firebaseUser.getUid();
                        Chat chat = new Chat();
                        chat.setType(Chat.TYPE_AUDIO);
                        chat.setFrom(uid);
                        chat.setUrl(audioUrl.toString());
                        chat.setBody(getString(R.string.audio_send));
                        chat.setPath(audioFilePath);
                        chat.setReplyMessage(replyMessageId);
                        DatabaseReference updateRef = chatRef.push();
                        Map<String, Object> postValues = chat.toMap();
                        updateRef.updateChildren(postValues);
                        scrollToBottom();
                        String messageId = updateRef.getKey();
                        Log.i(TAG, "Message id: " + messageId);
                        pushFCM.setMessageId(messageId);
                        pushFCM.setType(NotificationEvent.TYPE_ROOM_MESSAGE);
                        DatabaseReference ref = updateRef.child("posted");
                        pushFCM.push(chat.getBody(), ref);
                        setReplyMessageIdToNull();
                        removeChat(key);
                    }
                };
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to upload the audio: " + e.getMessage());
                    }
                }).addOnSuccessListener(onSuccessUpload);
            }
        });

        final View activityRootView = findViewById(R.id.activityRoot);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                boolean isKeyboardOpen = heightDiff > dpToPx(RoomActivity.this, 200); // if more than 200 dp, it's probably a keyboard...
                boolean isChecked = sharedPreferences.getBoolean(getString(R.string.USER_ROOM_VIEW_ONLINE), true);
                // ... do something here
                membersView.setVisibility(isKeyboardOpen || !isChecked ? View.GONE : View.VISIBLE);
            }
        });

        newMessageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (queueMessage.size() == 0) {
                    return;
                }
                Room model = queueMessage.get(0);
                if (model == null || model.getIntent() == null) {
                    return;
                }
                startActivity(model.getIntent());
            }
        });

        imageDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollToBottom();
            }
        });
        findViewById(R.id.button_add_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageBottomSheet();
            }
        });

        findViewById(R.id.invited_not_now).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (invitedRef != null) {
                    invitedRef.removeValue();
                }
            }
        });

        findViewById(R.id.invited_subscribe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(RoomActivity.this).setTitle(getString(R.string.subscriber_title))
                        .setMessage(getString(R.string.subscriber_active))
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (firebaseUser == null) {
                                    String toastMsg = getString(R.string.subscriber_error_active);
                                    Toast.makeText(RoomActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                DatabaseReference ref = subscriberRef.child(firebaseUser.getUid());
                                ref.setValue(ServerValue.TIMESTAMP);
//                                if(menuSubscriber != null) {
//                                    menuSubscriber.setChecked(true);
//                                }
                                if (invitedRef != null) {
                                    invitedRef.removeValue();
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create().show();
            }
        });

        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        findViewById(R.id.profile_bar_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new RoomDisplayDialog(RoomActivity.this, roomId).show();
            }
        });
    }

    private void SendAd() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            DatabaseReference updateRef = chatRef.push();
            Chat chat = new Chat();
            chat.setFrom(firebaseUser.getUid());
            chat.setType(Chat.TYPE_AD);
            chat.setBody("  ");
            chat.setAdName(Ad_Helper.randomStr);
            chat.setReplyMessage(replyMessageId);
            Map<String, Object> postValues = chat.toMap();
            updateRef.updateChildren(postValues);
            String messageId = updateRef.getKey();
            Log.i(TAG, "Message id: " + messageId);
            pushFCM.setMessageId(messageId);
            pushFCM.setType(NotificationEvent.TYPE_ROOM_MESSAGE);
            DatabaseReference ref = updateRef.child("posted");
            pushFCM.push(chat.getBody(), ref);
            setReplyMessageIdToNull();
            scrollToBottom();
            isLastItemHidden = false;

        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        isShown = true;

        if(recyclerTouchListener != null) {
            chatsView.addOnItemTouchListener(recyclerTouchListener);
        }

        memberList.clear();
        memberAdapter.notifyDataSetChanged();
        chatList.clear();
        queueMessage.clear();
        pushFCM.clearRecipients();
        tokenSubscribers.clear();
        if (chatAdapter != null) {
            chatAdapter.notifyDataSetChanged();
        }
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        //Join room
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        relationships.clear();
        blockedUsers.clear();
        connectedRef.addValueEventListener(onlineListener);
        blockedUserRef.addChildEventListener(blockedUserListener);
        subscriberRef.addChildEventListener(subscriberListener);
        participantRef.addChildEventListener(participantListener);
        roomImageRef.addValueEventListener(roomImageListener);
        if (invitedRef != null) {
            invitedRef.addValueEventListener(invitedListener);
        }
        chatRef.addChildEventListener(chatListener);
        inputTextChat.addTextChangedListener(textWatcher);
        if (myProfileRef != null) {
            myProfileRef.addValueEventListener(myProfileListener);
        }
        //!myUid.equals(roomId)
        chatsView.addOnScrollListener(onScrollListener);
        if (firebaseUser != null) {
            followingRef = FirebaseDatabase.getInstance().getReference("relationship")
                    .orderByChild("following").equalTo(firebaseUser.getUid());
            followerRef = FirebaseDatabase.getInstance().getReference("relationship")
                    .orderByChild("follower").equalTo(firebaseUser.getUid());
            followingRef.addChildEventListener(followingListener);
            followerRef.addChildEventListener(followerListener);
            if (!roomId.equals(firebaseUser.getUid())) {
                tokenRef = FirebaseDatabase.getInstance().getReference("token").orderByChild("userId").equalTo(roomId);
                                tokenRef.addChildEventListener(tokenListener);
                if (!isJoined) {
                    Log.i(TAG, "Join to the room: " + roomId);
                    DatabaseReference joinRef = participantRef.child(firebaseUser.getUid());
                    joinRef.setValue(ServerValue.TIMESTAMP);
                    joinRef.onDisconnect().removeValue();

                    Intent intent = new Intent();
                    intent.putExtra(RoomFragment.JOINED_ROOM_ID, roomId);
                    setResult(RESULT_OK, intent);
                } else {
                    Log.i(TAG, "User has already joined on this room");
                }
            } else {
                Log.i(TAG, "This is owner user");
            }
        }
        //reset last stored data
        String keyJoinMsg = String.format(getString(R.string.USER_ROOM_JOIN_MESSAGE), roomId);
        String keyCountMsg = String.format(getString(R.string.USER_ROOM_JOIN_COUNT), roomId);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(keyJoinMsg, "");
        editor.putInt(keyCountMsg, 0);
        editor.apply();

        //push notification for join state
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isBlocked) {
                    return;
                }
                Log.i(TAG, "Perform push to join state, is joined: " + isJoined);
                if (!isJoined) {
                    pushFCM.setType(NotificationEvent.TYPE_ROOM_JOIN);
                    pushFCM.push(null, null);
                }
            }
        }, 3000);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
        notificationManager.cancel(NotificationEvent.TYPE_ROOM_JOIN);
        notificationManager.cancel(NotificationEvent.TYPE_ROOM_MESSAGE);
        notificationManager.cancel(NotificationEvent.TYPE_ROOM_INVITE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "On Pause");
        isJoined = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isShown = false;

        if(recyclerTouchListener != null) {
            chatsView.removeOnItemTouchListener(recyclerTouchListener);
        }

        connectedRef.removeEventListener(onlineListener);
        participantRef.removeEventListener(participantListener);
        chatRef.removeEventListener(chatListener);
        blockedUserRef.removeEventListener(blockedUserListener);
        subscriberRef.removeEventListener(subscriberListener);
        roomImageRef.removeEventListener(roomImageListener);
        if (invitedRef != null) {
            invitedRef.removeEventListener(invitedListener);
        }
        if (invitedProfileRef != null) {
            invitedProfileRef.removeEventListener(invitedProfileListener);
        }
        for (UserImage image : memberList) {
            image.detachProfileRef();
        }
        for (Chat chat : chatList) {
            chat.detachProfileRef();
        }
        if (tokenRef != null) {
            tokenRef.removeEventListener(tokenListener);
        }
        if (followerRef != null) {
            followerRef.removeEventListener(followerListener);
        }
        if (followingRef != null) {
            followingRef.removeEventListener(followingListener);
        }
        if(profileRef != null){
            profileRef.removeEventListener(profileListener);
        }
        if (uploadTask != null && onSuccessUpload != null) {
            uploadTask.removeOnSuccessListener(onSuccessUpload);
        }
        if (mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
            mRunnable = null;
        }
        for (Query ref : tokenSubscribers) {
            ref.removeEventListener(tokenListener);
        }
        inputTextChat.removeTextChangedListener(textWatcher);
        chatsView.removeOnScrollListener(onScrollListener);
        if (myProfileRef != null) {
            myProfileRef.removeEventListener(myProfileListener);
        }
//        isJoined = true;
    }

    public void setReplyMessageIdToNull(){
        replyMessageId = null;
        if(inputReplyLayout != null){
            inputReplyLayout.removeAllViews();
        }
    }

    //Do not call onCreate on NearbyHomeActivity
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (emojiPopup != null && emojiPopup.isShowing()) {
            emojiPopup.dismiss();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "On Create option menu");
        getMenuInflater().inflate(R.menu.menu_room_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null || roomId == null) {
            return super.onPrepareOptionsMenu(menu);
        }
        boolean isOwner = roomId.equals(firebaseUser.getUid());
        Log.i(TAG, "On Prepare option menu: roomId: " + roomId + ", is owner: " + isOwner);
        MenuItem menuBlockList = menu.findItem(R.id.action_list_blocked);
        MenuItem menuLeave = menu.findItem(R.id.action_leave);
        MenuItem menuViewOnline = menu.findItem(R.id.action_view_online);
        menuSubscriber = menu.findItem(R.id.action_subscribe);

        menuViewOnline.setChecked(sharedPreferences.getBoolean(getString(R.string.USER_ROOM_VIEW_ONLINE),true));
        menuBlockList.setVisible(isOwner);
        menuLeave.setVisible(!isOwner);
        menuSubscriber.setVisible(!isOwner);
        membersView.setVisibility(menuViewOnline.isChecked() ? View.VISIBLE : View.GONE);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_leave) {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                participantRef.child(firebaseUser.getUid()).removeValue();
                Intent intent = new Intent();
                intent.putExtra(RoomFragment.JOINED_ROOM_ID, roomId);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
            return true;
        } else if (id == R.id.action_view_online) {
            boolean isChecked = item.isChecked();
            item.setChecked(!isChecked);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.USER_ROOM_VIEW_ONLINE), item.isChecked());
            editor.apply();
            membersView.setVisibility(item.isChecked() ? View.VISIBLE : View.GONE);
            return true;
        } else if (id == R.id.action_list_blocked) {
            new BlockedUserDialog(this).show();
            return true;
        } else if (id == R.id.action_list_subscribers) {
            new SubscribersDialog(this, roomId).show();
            return true;
        } else if (id == R.id.action_room_invite) {
            new RoomFriendsInvitationDialog(this, roomId, roomName).show();
            return true;
        } else if (id == R.id.action_subscribe) {
            final boolean isChecked = item.isChecked();
            String title = isChecked ? getString(R.string.unsubscriber_title) : getString(R.string.subscriber_title);
            String message = isChecked ? getString(R.string.subscriber_inactive) : getString(R.string.subscriber_active);
            new AlertDialog.Builder(this).setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (firebaseUser == null) {
                                String toastMsg = isChecked ? getString(R.string.subscriber_error_inactive) :
                                        getString(R.string.subscriber_error_active);
                                Toast.makeText(RoomActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            DatabaseReference ref = subscriberRef.child(firebaseUser.getUid());
                            if (isChecked) {
                                ref.removeValue();
                                item.setChecked(false);
                            } else {
                                ref.setValue(ServerValue.TIMESTAMP);
                                item.setChecked(true);
                            }
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ChildEventListener participantListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            String memberId = dataSnapshot.getKey();
            Long timestamp = dataSnapshot.getValue(Long.class);
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                finish();
                return;
            }
            String myUid = firebaseUser.getUid();
            if (isBlockedUser(memberId)) {
                if (myUid.equals(memberId)) {
                    isBlocked = true;
                    participantRef.child(myUid).removeValue();
                    Toast.makeText(RoomActivity.this, getString(R.string.alert_for_blocked_user), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                    finish();
                }
                return;
            }
            Log.i(TAG, "On Added => Member Id: " + memberId + " , Timestamp: " + timestamp);
            UserImage userImage = new UserImage();
            userImage.setId(memberId);
            userImage.setMainImage(roomId.equals(memberId));
            memberList.add(userImage);
            memberAdapter.notifyItemInserted(memberList.size() - 1);
            userImage.attachProfileListener(memberAdapter, memberList.size() - 1);

            if (!myUid.equals(memberId) &&
                    myTimestamp < timestamp && myTimestamp > 0) {
                //add join state message
                Chat joinState = new Chat();
                joinState.setType(Chat.TYPE_JOIN);
                joinState.setFrom(memberId);
                chatList.add(joinState);
                if (chatAdapter != null) {
                    chatAdapter.notifyItemInserted(chatList.size() - 1);
                    joinState.attachProfileListener(chatAdapter, chatList.size() - 1);
                }
            } else {
                myTimestamp = timestamp;
            }

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            String memberId = dataSnapshot.getKey();
            Log.i(TAG, "On Changed => Member Id: " + memberId);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String memberId = dataSnapshot.getKey();
            boolean isBlocked = isBlockedUser(memberId);
            Log.i(TAG, "On Removed => Member Id: " + memberId + " , is blocked user: " + isBlocked);
            int position = 0;
            for (UserImage model : memberList) {
                if (model.getId().equals(memberId)) {
                    memberList.remove(position);
                    memberAdapter.notifyItemRemoved(position);
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (firebaseUser != null) {
                        if (!firebaseUser.getUid().equals(memberId)) {
                            //add left state message
                            Chat leftChat = new Chat();
                            leftChat.setType(Chat.TYPE_LEFT);
                            leftChat.setFrom(memberId);
                            leftChat.setBody(isBlocked ? getString(R.string.state_kick_out) :
                                    getString(R.string.state_left));
                            chatList.add(leftChat);
                            if (chatAdapter != null) {
                                chatAdapter.notifyItemInserted(chatList.size() - 1);
                                leftChat.attachProfileListener(chatAdapter, chatList.size() - 1);
                            }
                        }
                    }
                    break;
                }
                position++;
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener subscriberListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            String userId = dataSnapshot.getKey();
            Log.i(TAG, "Add -> Subscribers Id: " + userId);
            Query ref = FirebaseDatabase.getInstance().getReference("token").orderByChild("userId").equalTo(userId);
            tokenSubscribers.add(ref);
            ref.addChildEventListener(tokenListener);
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            try {
                if (firebaseUser != null && firebaseUser.getUid().equals(userId)) {
                    menuSubscriber.setChecked(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String userId = dataSnapshot.getKey();
            Log.i(TAG, "Remove -> Subscribers Id: " + userId);
            for (final Query ref : tokenSubscribers) {
                if (ref.equals(FirebaseDatabase.getInstance().getReference("token").orderByChild("userId").equalTo(userId))) {
                    Log.i(TAG, "perform remove.");
                    ref.removeEventListener(tokenListener);
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String token = dataSnapshot.getValue(String.class);
                            pushFCM.removeRecipients(token);
                            Log.i(TAG, "Remove token: " + token);
                            ref.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    tokenSubscribers.remove(ref);
                    break;
                }
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener chatListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Chat chat = dataSnapshot.getValue(Chat.class);
            chat.setId(dataSnapshot.getKey());
            chat.setReplyChat(findChatById(chat.getReplyMessage()));
            Log.i(TAG, "Message: " + chat.getBody() + " , display name: " + chat.getDisplayName());
            String thisDate = MyDateUtil.getDateMessage(RoomActivity.this, chat.getTimestamp());
            if (chatList.size() == 0 || (chatList.size() > 1 && chatList.get(chatList.size() - 1).getTimestamp() > 0 &&
                    !thisDate.equals(MyDateUtil.getDateMessage(RoomActivity.this, chatList.get(chatList.size() - 1).getTimestamp())))) {
                //this is for date message only
                Chat dateMessage = new Chat();
                dateMessage.setType(Chat.TYPE_DATE);
                dateMessage.setBody(thisDate);
                chatList.add(dateMessage);
            }
            if (chatList.size() > 1 && chatList.get(chatList.size() - 1).getTimestamp() > 0 && chat.getType() == Chat.TYPE_TEXT) {
                Chat prevModel = chatList.get(chatList.size() - 1);
//                long diff = chat.getTimestamp() - prevModel.getTimestamp();
//                if (prevModel.getFrom().equals(chat.getFrom())
//                        && MyDateUtil.isMergeRequired(chat.getTimestamp(), prevModel.getTimestamp())
//                        && prevModel.getType() == Chat.TYPE_TEXT
//                        && chat.getType() == Chat.TYPE_TEXT && !isLastItemHidden) {
////                    Log.i(TAG, "Diff time: " + diff + " , message: " + chat.getBody());
//                    String newBody = prevModel.getBody() + "\n" + chat.getBody();
//                    prevModel.setBody(newBody);
//                    prevModel.setTimestamp(chat.getTimestamp());
//                    if (chatAdapter != null) {
//                        chatAdapter.notifyItemChanged(chatList.size() - 1);
//                    }
//                    int viewPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
//                    if (viewPosition >= chatList.size() - 2) {
//                        Log.i(TAG, "scroll to bottom");
//                        scrollToBottom();
//                    }
//                } else {
                    addMessage(chat);
//                }
            } else {
                addMessage(chat);
            }
//            if (myTimestamp > 0 && myTimestamp < chat.getTimestamp()) {
            //scroll to Bottom
//                scrollToBottom();
//            }
            //check last position is NOT shown
            if (isLastItemHidden) {
                countBadge++;
                showDownNotification(countBadge);
                //BEGIN add unread message
                boolean isUnreadExist = false;
                for (int i = chatList.size() - 1; i >= 0; i--) {
                    Chat model = chatList.get(i);
                    if (model.getType() == Chat.TYPE_UNREAD_MESSAGE) {
                        isUnreadExist = true;
                        String message = String.format(getString(R.string.unread_message), countBadge);
                        model.setBody(message);
                        chatAdapter.notifyItemChanged(i);
                        break;
                    }
                }
                if (!isUnreadExist) {
                    Chat unRead = new Chat();
                    unRead.setType(Chat.TYPE_UNREAD_MESSAGE);
                    String message = countBadge == 1 ? getString(R.string.unread_message_only_one) :
                            String.format(getString(R.string.unread_message), countBadge);
                    unRead.setBody(message);
                    chatList.add(chatList.size() - 1, unRead);
                }
                Log.i(TAG, "Is unread message exist: " + isUnreadExist);
                //END add unread message
            } else {
                hideUnreadMessage();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener followingListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.i(TAG, "Following -> " + dataSnapshot);
            Relationship model = dataSnapshot.getValue(Relationship.class);
            model.setId(dataSnapshot.getKey());
            relationships.add(model);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.w(TAG, "Follower On changed-> " + dataSnapshot);
            Relationship model = dataSnapshot.getValue(Relationship.class);
            model.setId(dataSnapshot.getKey());
            for (Relationship relationship : relationships) {
                if (relationship.getId().equals(dataSnapshot.getKey())) {
                    relationship.setFollowing(model.getFollower());
                    relationship.setFollower(model.getFollower());
                    relationship.setActionUid(model.getActionUid());
                    relationship.setStatus(model.getStatus());
                    break;
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener followerListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.i(TAG, "Follower -> " + dataSnapshot);
            Relationship model = dataSnapshot.getValue(Relationship.class);
            model.setId(dataSnapshot.getKey());
            relationships.add(model);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.w(TAG, "Follower On changed-> " + dataSnapshot);
            Relationship model = dataSnapshot.getValue(Relationship.class);
            model.setId(dataSnapshot.getKey());
            for (Relationship relationship : relationships) {
                if (relationship.getId().equals(dataSnapshot.getKey())) {
                    relationship.setFollowing(model.getFollower());
                    relationship.setFollower(model.getFollower());
                    relationship.setActionUid(model.getActionUid());
                    relationship.setStatus(model.getStatus());
                    break;
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ValueEventListener invitedListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.i(TAG, "Invited: " + dataSnapshot.getValue());
            boolean isDataExist = dataSnapshot.getValue() != null;
            invitedLayout.setVisibility(isDataExist ? View.VISIBLE : View.GONE);
            if (isDataExist) {
                for (DataSnapshot election : dataSnapshot.getChildren()) {
                    if (election.getKey().equals("from")) {
                        invitedProfileRef = FirebaseDatabase.getInstance().getReference("users")
                                .child(election.getValue(String.class));
                        invitedProfileRef.addValueEventListener(invitedProfileListener);
                    } else if (election.getKey().equals("timestamp")) {
                        invitedTime.setText(MyDateUtil.getDateTime(RoomActivity.this, election.getValue(Long.class)));
                    }
                }

            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener blockedUserListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            UserBlocked userBlocked = new UserBlocked();
            userBlocked.setTimestamp(dataSnapshot.getValue(Long.class));
            userBlocked.setUid(dataSnapshot.getKey());
            blockedUsers.add(userBlocked);
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                String uid = firebaseUser.getUid();
                if (userBlocked.getUid().equals(uid)) {
                    long currentTime = System.currentTimeMillis();
                    long blockedTime = userBlocked.getTimestamp();
                    long diffTimestamp = currentTime - blockedTime;
                    Log.i(TAG, "Current time: " + currentTime + " , blocked time: " + blockedTime + " , difference time: " + diffTimestamp);
                    if (diffTimestamp < 5000) { //
                        participantRef.child(uid).removeValue();
                        Toast.makeText(RoomActivity.this, getString(R.string.alert_you_kicked), Toast.LENGTH_SHORT).show();
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String uid = dataSnapshot.getKey();
            int position = 0;
            for (UserBlocked model : blockedUsers) {
                if (model.getUid().equals(uid)) {
                    blockedUsers.remove(position);
                    break;
                }
                position++;
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ValueEventListener roomImageListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.getValue() != null){
                RoomImage roomImage = dataSnapshot.getValue(RoomImage.class);
                imageLogo.setVisibility(View.VISIBLE);
                imageLogo.setImageURI(roomImage.getThumbPic());
            }else{
                if(roomImageRef != null) {
                    profileRef = FirebaseDatabase.getInstance().getReference("users").child(roomId);
                    profileRef.addValueEventListener(profileListener);
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ValueEventListener profileListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            User model = dataSnapshot.getValue(User.class);
            if(model == null){
                return;
            }
            for (DataSnapshot election : dataSnapshot.getChildren()) {
                if (election.getKey().equals("images")) {
                    String key = model.getImageUrl() == null ? "default" : model.getImageUrl();
                    DataSnapshot defaultImage = election.child(key);
                    if (defaultImage.exists()) {
                        imageLogo.setVisibility(View.VISIBLE);
                        UserImage userImage = defaultImage.getValue(UserImage.class);
                        imageLogo.setImageURI(userImage.getThumbPic());
                        break;
                    }
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ValueEventListener invitedProfileListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            User model = dataSnapshot.getValue(User.class);
            invitedProfileName.setText(model.getName());
            for (DataSnapshot election : dataSnapshot.getChildren()) {
                if (election.getKey().equals("images")) {
                    String key = model.getImageUrl() == null ? "default" : model.getImageUrl();
                    DataSnapshot defaultImage = election.child(key);
                    if (defaultImage.exists()) {
                        UserImage userImage = defaultImage.getValue(UserImage.class);
                        DraweeController controller = Fresco.newDraweeControllerBuilder()
                                .setLowResImageRequest(ImageRequest.fromUri(userImage.getThumbPic()))
                                .setImageRequest(ImageRequest.fromUri(userImage.getOriginalPic()))
                                .setOldController(invitedProfileImage.getController())
                                .build();
                        invitedProfileImage.setController(controller);
                        break;
                    }
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ValueEventListener myProfileListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            User model = dataSnapshot.getValue(User.class);
            if(model == null){
                return;
            }
            myName = model.getName();
            pushFCM.setDisplayName(myName);
            for (DataSnapshot election : dataSnapshot.getChildren()) {
                if (election.getKey().equals("images")) {
                    String key = model.getImageUrl() == null ? "default" : model.getImageUrl();
                    DataSnapshot defaultImage = election.child(key);
                    if (defaultImage.exists()) {
                        UserImage userImage = defaultImage.getValue(UserImage.class);
                        myImageUrl = userImage.getThumbPic();
                        pushFCM.setImageUrl(myImageUrl);
                        break;
                    }
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener tokenListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                return;
            }
            Token token = dataSnapshot.getValue(Token.class);
            if (firebaseUser.getUid().equals(token.getUserId())) {
                Log.d(TAG, "It's me, then no need to add me on the list for push notification");
                return;
            }
            Log.i(TAG, "Get token: " + token.getToken());
            pushFCM.addRecipients(token.getToken());
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Token token = dataSnapshot.getValue(Token.class);
            if (token != null) {
                Log.i(TAG, "Get token: " + token.getToken());
                pushFCM.removeRecipients(token.getToken());
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    private boolean isBlockedUser(String uid) {
        if (uid == null || uid.isEmpty()) {
            return false;
        }
        boolean isBlock = false;
        for (UserBlocked userBlocked : blockedUsers) {
            if (uid.equals(userBlocked.getUid())) {
                isBlock = true;
                break;
            }
        }
        return isBlock;
    }

    private Item statusFriend(String friendId) { //icon = status
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            return new Item(null, STATUS_GENERAL_ERROR);
//            return STATUS_GENERAL_ERROR;
        }
        String myUid = firebaseUser.getUid();
        if (myUid.equals(friendId)) {
//            return STATUS_MY_SELF;
            return new Item(null, STATUS_MY_SELF);
        }

        int statusFriend = STATUS_READY_TO_INVITE;
        String relationshipId = null;
        for (Relationship model : relationships) {
            if (model.getFollowing().equals(friendId) || model.getFollower().equals(friendId)) {
                relationshipId = model.getId();
                switch (model.getStatus()) {
                    case Relationship.ACCEPTED:
                        statusFriend = STATUS_FRIEND;
                        break;
                    case Relationship.BLOCKED:
                        statusFriend = myUid.equals(model.getActionUid()) ? STATUS_BLOCK :
                                STATUS_GOT_BLOCKED;
                        break;
                    case Relationship.DECLINED:
                        statusFriend = myUid.equals(model.getActionUid()) ? STATUS_DECLINE :
                                STATUS_GOT_DECLINED;
                        break;
                    case Relationship.PENDING:
                        statusFriend = myUid.equals(model.getActionUid()) ? STATUS_PENDING_INVITE :
                                STATUS_RECEIVED_INVITATION;
                        break;
                }
                break;
            }
        }
        return new Item(relationshipId, statusFriend);
    }

    private void scrollToBottom() {
        Log.i(TAG, "Action scroll to bottom");
        chatsView.postDelayed(new Runnable() {
            @Override
            public void run() {
                chatsView.scrollToPosition(chatsView.getAdapter().getItemCount() - 1);
            }
        }, 100);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            int length = charSequence.length();
            sendButton.setVisibility(length > 0 ? View.VISIBLE : View.GONE);
            audioButton.setVisibility(length > 0 ? View.GONE : View.VISIBLE);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private ValueEventListener onlineListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            isConnected = dataSnapshot.getValue(Boolean.class);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void addMessage(Chat chat) {
        if (chatAdapter == null) {
            return;
        }
        chatList.add(chat);
        chatAdapter.notifyItemInserted(chatList.size() - 1);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && !firebaseUser.getUid().equals(chat.getFrom())) {
            Log.i(TAG, "Read Message: " + chat.getBody() + " , from: " + chat.getFrom());
            chat.attachProfileListener(chatAdapter, chatList.size() - 1);
        }
        int viewPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
        if (viewPosition >= chatList.size() - 3) {
            Log.i(TAG, "scroll to bottom");
            scrollToBottom();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEnterEvent(OnEnterEvent event) {
        Log.i(TAG, "On enter event, ID: " + event.userId + " , is joined: " + isJoined);
        //Join room
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && !isJoined) {
            Log.i(TAG, "Join to the room: " + roomId);
            DatabaseReference joinRef = participantRef.child(firebaseUser.getUid());
            joinRef.setValue(ServerValue.TIMESTAMP);
            joinRef.onDisconnect().removeValue();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLeaveEvent(OnLeaveEvent event) {
        Log.i(TAG, "On leave room event, ID: " + event.userId);
        DatabaseReference leaveRef = participantRef.child(event.userId);
        leaveRef.removeValue();
        isJoined = false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChatEvent(OnChatRoomEvent event) {
        if (!isShown || roomId == null) {
            return;
        }
        boolean isSameRoom = roomId.equals(event.room.getId());
        if (isSameRoom) {
            return;
        }
        Log.i(TAG, "Chat listener => message: " + event.room.getChat().getBody() + " , room name: " + event.room.getName());
        Room room = event.room;
        Intent intent = new Intent(RoomActivity.this, RoomActivity.class);
        intent.putExtra(RoomActivity.KEY_ID, room.getId());
        intent.putExtra(RoomActivity.KEY_ROOM_NAME, room.getName());
        room.setIntent(intent);
        queueMessage.add(room);
        showNewMessage();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNotificationEvent(NotificationEvent event) {
        Log.i(TAG, "On notification event => message: " + event.message);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
        notificationManager.cancel(event.tag, event.id);
        Chat chat = new Chat();
        chat.setDisplayName(event.getDisplayName());
        chat.setBody(event.message);
        Room room = new Room();
        room.setImageUrl(event.getImageUrl());
        room.setName(event.getTitle());
        room.setChat(chat);
        room.setIntent(event.intent);
        queueMessage.add(room);
        showNewMessage();
    }

    private float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    private void showNewMessage() {
        newMessageLayout.setVisibility(View.VISIBLE);
        animShowNewMessage.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                newMessageLayout.setVisibility(View.VISIBLE);
                hideNewMessage();
                animHideNewMessage = AnimationUtils.loadAnimation(RoomActivity.this, R.anim.slide_left);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        Log.i(TAG, "is Animated has end: " + animShowNewMessage.hasEnded() + ", size queue message: " + queueMessage.size());
        if (!animShowNewMessage.hasEnded() && queueMessage.size() > 0) {
            Room model = queueMessage.get(0);
            Log.i(TAG, "Display name: " + model.getChat().getDisplayName());
            newMessageImage.setVisibility(model.getImageUrl() == null || model.getImageUrl().isEmpty() ? View.GONE : View.VISIBLE);
            newMessageImage.setImageURI(model.getImageUrl());
            if (model.getName() == null) {
                roomNewMessage.setVisibility(View.GONE);
            } else {
                roomNewMessage.setVisibility(View.VISIBLE);
                roomNewMessage.setText(model.getName());
            }
            textNewMessage.setText(model.getChat().getBody());
            subjectNewMessage.setText(model.getChat().getDisplayName());
            newMessageLayout.startAnimation(animShowNewMessage);
        } else {
            Log.e(TAG, "Animation does not started, maybe under queue");
        }
    }

    private void hideNewMessage() {
        newMessageLayout.setVisibility(View.VISIBLE);
        Log.i(TAG, "Hide new message");
        animHideNewMessage.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.i(TAG, "Hide animation start");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.i(TAG, "Hide animation end");
                newMessageLayout.setVisibility(View.GONE);
                animShowNewMessage = AnimationUtils.loadAnimation(RoomActivity.this, R.anim.slide_right);
                if (!queueMessage.isEmpty()) {
                    Log.i(TAG, "data queue is NOT empty");
                    queueMessage.remove(0);
                    if (queueMessage.size() > 0) {
                        Log.i(TAG, "try to show another animation");
                        showNewMessage();
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        newMessageLayout.setAnimation(animHideNewMessage);
    }

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
//            scrollY += dy;
            int viewPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
            if (viewPosition < 0) {
                return;
            }
            isLastItemHidden = viewPosition < chatList.size() - 2;
//            Log.i(TAG, "On Scrolled -> is last scrolled hidden: " + isLastItemHidden +
//                    " , view position: " + viewPosition);
            if (!isLastItemHidden) {
                countBadge = 0;
                showDownNotification(0); //hide double down icon
            }
        }
    };

    private void hideUnreadMessage() {
        Log.i(TAG, "Count items: " + chatList.size());
        for (int i = chatList.size() - 1; i >= 0; i--) {
            Chat model = chatList.get(i);
            if (model.getType() == Chat.TYPE_UNREAD_MESSAGE) {
                Log.i(TAG, "Hide unread position: " + i + " , text: " + model.getBody());
                chatList.remove(i);
                chatAdapter.notifyItemRemoved(i);
                break;
            }
        }
    }

    private void showDownNotification(int countBadge) {
        imageDown.setVisibility(countBadge == 0 ? View.GONE : View.VISIBLE);
        textBadge.setVisibility(countBadge == 0 ? View.GONE : View.VISIBLE);
        textBadge.setText(String.valueOf(countBadge));
    }

    private void openImageBottomSheet() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, OPEN_IMAGE);
                return;
            }
        }
        new ImageBottomPicker.Builder(this)
                .setVideoSelection()
                .setOnImageSelectedListener(new ImageBottomPicker.OnImageSelectedListener() {
                    @Override
                    public void onImageSelected(String path) {
                        Log.i(TAG, "On image Completed: " + path);


                        Ad_Helper.adCountGlobal++;

                        if(Ad_Helper.wannaSendGlobal(Ad_Helper.adCountGlobal)){
                            SendAd();
                        }


                        if(MyImageUtil.isImageFile(new File(path))) {
                            doUploadImage(path);
                        }else{
                            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (firebaseUser == null) {
                                return;
                            }
                            String uid = firebaseUser.getUid();
                            Chat chat = new Chat();
                            chat.setType(Chat.TYPE_VIDEO);
                            chat.setFrom(uid);
                            chat.setPath(path);
                            chat.setBody(getString(R.string.send_a_video));
                            chat.setReplyMessage(replyMessageId);
                            new VideoUploadDialog(RoomActivity.this, path, chatRef, chat).show();
                        }
                    }
                })
                .create().show(getSupportFragmentManager());
    }

    private Chat findChatById(String id) {
        if(id == null){
            return null;
        }
        for (Chat model : chatList) {
            if(model.getId() == null){
                continue;
            }
            if (model.getId().equals(id)) {
                Log.i(TAG,"Find chat id: "+model.getId()+" , body: "+model.getBody());
                return model;
            }
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
        if (requestCode == OPEN_IMAGE) {
            if (grantResults.length < 1) {
                return;
            }
            boolean permission1 = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean permission2 = grantResults[1] == PackageManager.PERMISSION_GRANTED;
            boolean permission3 = grantResults[2] == PackageManager.PERMISSION_GRANTED;
            boolean isPermissionGranted = permission1 && permission2 && permission3;
            if (isPermissionGranted) {
                openImageBottomSheet();
            }
        }
    }

    private void removeChat(String idChat) {
        for (int i = chatList.size() - 1; i >= 0; i--) {
            Chat model = chatList.get(i);
            if (model.getId().equals(idChat)) {
                chatList.remove(i);
                chatAdapter.notifyItemRemoved(i);
                break;
            }
        }
    }

    private void doUploadImage(final String path) {

        Ad_Helper.adCountGlobal++;

        if(Ad_Helper.wannaSendGlobal(Ad_Helper.adCountGlobal)){
            SendAd();
        }

        String tempKey = String.valueOf(System.currentTimeMillis());
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }
        String uid = firebaseUser.getUid();
        Chat chat = new Chat();
        chat.setType(Chat.TYPE_IMAGE);
        chat.setPath(path);
        chat.setFrom(uid);
        chat.setId(tempKey);
        chat.setTimestamp(System.currentTimeMillis() / 1000);
        chat.setReplyMessage(replyMessageId);
        mRunnable = new AddChatRunnable(chat);
        mHandler.postDelayed(mRunnable, 500);
        inputReplyLayout.removeAllViews();
        StorageReference imageRef = storageRef.child(uid + "/chat/images/thumbnail/" + System.currentTimeMillis() + ".jpg");
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        Bitmap thumbnail = Bitmap.createScaledBitmap(bitmap, 70, 70, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] thumbData = baos.toByteArray();
        uploadTask = imageRef.putBytes(thumbData);
        onSuccessUpload = new OnSuccessUploadListener(tempKey) {
            @Override
            public void onSuccessUpload(final String key, UploadTask.TaskSnapshot taskSnapshot) {
                @SuppressWarnings("VisibleForTests")
                final Uri thumbUrl = taskSnapshot.getDownloadUrl();
                Log.i(TAG, "Success to Upload thumbnail, url: " + thumbUrl);
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser == null) {
                    return;
                }
                final String uid = firebaseUser.getUid();
                StorageReference imageRef = storageRef.child(uid + "/chat/images/" + System.currentTimeMillis() + ".jpg");
                UploadTask uploadTask = imageRef.putFile(Uri.fromFile(new File(path)));
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error upload thumb image: " + e.getMessage());
                        removeChat(key);
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests")
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Log.i(TAG, "Success to Upload, url: " + downloadUrl);
                        Chat chat = new Chat();
                        chat.setType(Chat.TYPE_IMAGE);
                        chat.setFrom(uid);
                        chat.setPath(path);
                        chat.setUrlThumbnail(thumbUrl.toString());
                        chat.setUrl(downloadUrl.toString());
                        chat.setBody(getString(R.string.send_an_image));
                        chat.setReplyMessage(replyMessageId);
                        DatabaseReference updateRef = chatRef.push();
                        Map<String, Object> postValues = chat.toMap();
                        updateRef.updateChildren(postValues);
                        removeChat(key);
                        String messageId = updateRef.getKey();
                        Log.i(TAG, "Message id: " + messageId);
                        pushFCM.setMessageId(messageId);
                        pushFCM.setType(NotificationEvent.TYPE_ROOM_MESSAGE);
                        DatabaseReference ref = updateRef.child("posted");
                        pushFCM.push(chat.getBody(), ref);
                        setReplyMessageIdToNull();
                        scrollToBottom();
                    }
                });
            }
        };
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to upload thumb: " + e.getMessage());
            }
        }).addOnSuccessListener(onSuccessUpload);
    }


    private final Handler mHandler = new Handler();
    private Runnable mRunnable;

    private class AddChatRunnable implements Runnable {
        private Chat chat;

        public AddChatRunnable(Chat chat) {
            this.chat = chat;
        }

        @Override
        public void run() {
            if (chat == null) {
                return;
            }
            chatList.add(chat);
            chatAdapter.notifyItemChanged(chatList.size() - 1);
            scrollToBottom();
        }
    }
}
