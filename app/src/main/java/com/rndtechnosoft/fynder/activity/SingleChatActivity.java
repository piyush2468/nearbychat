package com.rndtechnosoft.fynder.activity;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.adapter.ChatAdapter;
import com.rndtechnosoft.fynder.dialog.ReplyMessageInputView;
import com.rndtechnosoft.fynder.dialog.VideoUploadDialog;
import com.rndtechnosoft.fynder.model.Chat;
import com.rndtechnosoft.fynder.model.Relationship;
import com.rndtechnosoft.fynder.model.Room;
import com.rndtechnosoft.fynder.model.User;
import com.rndtechnosoft.fynder.model.UserImage;
import com.rndtechnosoft.fynder.model.event.NotificationEvent;
import com.rndtechnosoft.fynder.utility.Ad_Helper;
import com.rndtechnosoft.fynder.utility.MyDateUtil;
import com.rndtechnosoft.fynder.utility.MyImageUtil;
import com.rndtechnosoft.fynder.utility.PushFCM;
import com.rndtechnosoft.fynder.model.event.OnChatRoomEvent;
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

public class SingleChatActivity extends AppCompatActivity {
    private final String TAG = "SingleChat";
    public static final String KEY_TITLE = SingleChatActivity.class.getName() + ".KEY_TITLE";
    public static final String KEY_FRIEND_ID = SingleChatActivity.class.getName() + ".KEY_FRIEND_ID";
    public static final String KEY_RELATIONSHIP_ID = SingleChatActivity.class.getName() + ".KEY_RELATIONSHIP_ID";
    public static boolean active;
    private final int OPEN_IMAGE = 536;

    private FirebaseAuth mAuth;
    private DatabaseReference chatRef;
    private DatabaseReference connectedRef;
    private DatabaseReference friendOnlineRef;
    private Query tokenRef;
    private DatabaseReference friendRef;
    private DatabaseReference meRef;
    private DatabaseReference myProfileRef;
    private StorageReference storageRef;
    private Query friendRelRef;
    private SimpleDraweeView imageLogo;
    private ImageButton sendButton;
    private ImageButton audioButton;
    private ImageButton smileyButton;
    private EmojiEditText inputTextChat;
    private EmojiPopup emojiPopup;
    private RecyclerView chatsView;
    private ChatAdapter chatAdapter;
    private List<Chat> chatList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private LinearLayout inputReplyLayout;
    private boolean isConnected;
    private boolean isLastItemHidden = false;
    private boolean isFirstUnread = true;
    private boolean isBlocked = false;
    private boolean isFriendOnline = false;
    private int countBadge = 0;
    private int countLastMsg = 0;
    private int countMsg;
    private long curUnreadTime;
    private String title;

    private Animation animShowNewMessage;
    private Animation animHideNewMessage;
    private RelativeLayout newMessageLayout;
    private SimpleDraweeView newMessageImage;
    private TextView roomNewMessage;
    private TextView subjectNewMessage;
    private TextView textNewMessage;
    private ImageView imageDown;
    private TextView textBadge;
    private List<Room> queueMessage = new ArrayList<>();
    private SharedPreferences sharedpreferences;

    private String friendId;
    private String relationshipId;
    private String replyMessageId;

    private UploadTask uploadTask;
    private OnSuccessListener<UploadTask.TaskSnapshot> onSuccessUpload;

    private ActionMode mActionMode;
    private RecyclerTouchListener recyclerTouchListener;

    private PushFCM pushFCM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        friendId = intent.getStringExtra(KEY_FRIEND_ID);
        sharedpreferences = getSharedPreferences(getString(R.string.KEY_PREF_NAME), Context.MODE_PRIVATE);
        relationshipId = intent.getStringExtra(KEY_RELATIONSHIP_ID);
        title = intent.getStringExtra(KEY_TITLE);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (friendId == null || relationshipId == null || title == null || firebaseUser == null) {
//            Log.i(TAG,"Friend id: "+)
            finish();
            return;
        }
        String uid = firebaseUser.getUid();
        if (uid.equals(friendId)) {
            finish();
            return;
        }

        pushFCM = new PushFCM(this, NotificationEvent.TYPE_SINGLE_CHAT);
        pushFCM.setUid(uid);
        pushFCM.setId(relationshipId);
        pushFCM.setTitle(firebaseUser.getDisplayName());
        setContentView(R.layout.activity_single_chat_new);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) findViewById(R.id.text_title_bar);
        imageLogo = (SimpleDraweeView) findViewById(R.id.image_logo);
        RoundingParams circle = RoundingParams.asCircle()
                .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY);
        imageLogo.getHierarchy().setRoundingParams(circle);
        imageLogo.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
        imageLogo.setVisibility(View.GONE);
        ImageView backButton = (ImageView) findViewById(R.id.back_button);
        setSupportActionBar(toolbar);
        mTitle.setText(title);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setTitle(title);
        }

        Log.i(TAG, "On Create -> Friend Id: " + friendId);
        mAuth = FirebaseAuth.getInstance();
        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        chatRef = FirebaseDatabase.getInstance().getReference("chats").child(relationshipId);
        friendRef = FirebaseDatabase.getInstance().getReference("users").child(friendId);
        friendRelRef = FirebaseDatabase.getInstance().getReference("relationship")
                .orderByChild("actionUid").equalTo(friendId);
        storageRef = FirebaseStorage.getInstance().getReference();
        friendOnlineRef = FirebaseDatabase.getInstance().getReference("singleChatOnline").child(friendId);
        meRef = FirebaseDatabase.getInstance().getReference("singleChatOnline").child(uid);
        myProfileRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
//        tokenRef = FirebaseDatabase.getInstance().getReference("token").child(friendId);
        tokenRef = FirebaseDatabase.getInstance().getReference("token").orderByChild("userId").equalTo(friendId);
        sendButton = (ImageButton) findViewById(R.id.button_send);
        audioButton = (ImageButton) findViewById(R.id.button_audio);
        smileyButton = (ImageButton) findViewById(R.id.button_smiley);
        inputTextChat = (EmojiEditText) findViewById(R.id.input_text_chat);
        inputReplyLayout = (LinearLayout) findViewById(R.id.input_reply_layout);
        ViewGroup rootView = (ViewGroup) findViewById(R.id.main_content);
        chatsView = (RecyclerView) findViewById(R.id.list_chat);
        animShowNewMessage = AnimationUtils.loadAnimation(this, R.anim.slide_right);
        animHideNewMessage = AnimationUtils.loadAnimation(this, R.anim.slide_left);
        newMessageLayout = (RelativeLayout) findViewById(R.id.layout_new_message);
        newMessageImage = (SimpleDraweeView) findViewById(R.id.icon_new_message);
        imageDown = (ImageView) findViewById(R.id.icon_double_down);
        textBadge = (TextView) findViewById(R.id.text_badge_count);
        newMessageImage.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
        RoundingParams rounded = RoundingParams.fromCornersRadius(10)
                .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY);
        newMessageImage.getHierarchy().setRoundingParams(rounded);
        roomNewMessage = (TextView) findViewById(R.id.text_room);
        subjectNewMessage = (TextView) findViewById(R.id.text_subject);
        textNewMessage = (TextView) findViewById(R.id.text_new_message);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        chatsView.setLayoutManager(linearLayoutManager);
        chatAdapter = new ChatAdapter(this, chatList, firebaseUser.getUid());
        chatsView.setAdapter(chatAdapter);

        recyclerTouchListener = new RecyclerTouchListener(this, chatsView, new RecyclerTouchListener.RecyclerOnClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (mActionMode == null) {
                    Log.e(TAG, "Action mode is NULL");
                    return;
                }
                Log.i(TAG, "Count selected: " + chatAdapter.getSelectedCount());
                if (chatAdapter.getSelectedCount() > 0) {
                    chatAdapter.toggleSelection(position);
                    mActionMode.setTitle(String.valueOf(chatAdapter.getSelectedCount()));
                }

                if (chatAdapter.getSelectedCount() == 0) {
                    mActionMode.finish();
                } else {
                    Menu menu = mActionMode.getMenu();
                    menu.findItem(R.id.action_reply).setVisible(chatAdapter.getSelectedCount() == 1);
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                Log.i(TAG, "OnLongClick on message: " + chatList.get(position).getBody());
                mActionMode = startSupportActionMode(new ToolbarActionBarCallback(SingleChatActivity.this, chatAdapter) {
                    @Override
                    public void onReply(Chat chat) {
                        Log.i(TAG, "Reply => Message: " + chat.getBody());
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        inputTextChat.requestFocus();

                        replyMessageId = chat.getId();
                        inputReplyLayout.addView(new ReplyMessageInputView(SingleChatActivity.this, chat,
                                ContextCompat.getColor(SingleChatActivity.this, R.color.color_button_hover)));
                    }
                });
                chatAdapter.toggleSelection(position);
                if (chatAdapter.getSelectedCount() > 0) {
                    mActionMode.setTitle(String.valueOf(chatAdapter.getSelectedCount()));
                }
                if (chatAdapter.getSelectedCount() == 0) {
                    mActionMode.finish();
                } else {
                    Menu menu = mActionMode.getMenu();
                    menu.findItem(R.id.action_reply).setVisible(chatAdapter.getSelectedCount() == 1);
                }
            }
        });

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
                Ad_Helper.adCount++;
                if (!isConnected) {
                    Toast.makeText(SingleChatActivity.this, getString(R.string.error_internet), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (friendId == null) {
                    Toast.makeText(SingleChatActivity.this, getString(R.string.general_error), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isBlocked) {
                    showToastBlocked();
                    return;
                }
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if(Ad_Helper.wannaSendPrivate(Ad_Helper.adCount)){
                    SendAd();
                }
                if (firebaseUser != null) {
                    DatabaseReference updateRef = chatRef.push();
                    Chat chat = new Chat();
                    chat.setBody(inputTextChat.getText().toString());
                    chat.setFrom(firebaseUser.getUid());
                    chat.setTo(friendId);
                    chat.setStatus(Chat.DELIVERED);
                    chat.setReplyMessage(replyMessageId);
                    Map<String, Object> postValues = chat.toMap();
                    updateRef.updateChildren(postValues);
                    setReplyMessageIdToNull();
                    scrollToBottom();
                    isLastItemHidden = false;
                    Log.d(TAG, "onClick: "+Ad_Helper.adCount);
                } else {
                    Toast.makeText(SingleChatActivity.this, getString(R.string.error_send_text_message), Toast.LENGTH_SHORT).show();
                }
                inputTextChat.setText("");
            }
        });

        audioButton.setOnTouchListener(new PttOnTouchListener(this) {
            @Override
            public void onCompleteRecorded(final String audioFilePath) {
                Log.i(TAG, "On Complete audio recorded: " + audioFilePath);

                Ad_Helper.adCount++;

                if(Ad_Helper.wannaSendPrivate(Ad_Helper.adCount)){
                    SendAd();
                }

                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser == null) {
                    return;
                }
                File audioFile = new File(audioFilePath);
                if (!audioFile.exists()) {
                    Log.e(TAG, "Audio file path does not exist");
                    return;
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
                        chat.setTo(friendId);
                        chat.setReplyMessage(replyMessageId);
                        chat.setStatus(Chat.DELIVERED);
                        DatabaseReference updateRef = chatRef.push();
                        Map<String, Object> postValues = chat.toMap();
                        updateRef.updateChildren(postValues);
                        setReplyMessageIdToNull();
                        scrollToBottom();
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

        findViewById(R.id.button_add_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                openImageBottomSheet();
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
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                // Check if no view has focus:
//                View view = this.getCurrentFocus();
//                if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//                }
            }
        });
        findViewById(R.id.profile_bar_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SingleChatActivity.this, ProfileActivity.class);
                intent.putExtra(ProfileActivity.KEY_UID, friendId);
                intent.putExtra(ProfileActivity.KEY_DISPLAY_NAME, title);
                startActivity(intent);
            }
        });
    }



    private void SendAd() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            DatabaseReference updateRef = chatRef.push();
            Chat chat = new Chat();
            String tempKey = String.valueOf(System.currentTimeMillis());
            chat.setType(Chat.TYPE_AD);
            chat.setBody("  ");
            chat.setAdName(Ad_Helper.randomStr);
            chat.setFrom(firebaseUser.getUid());
            chat.setTo(friendId);
            chat.setId(tempKey);
            chatList.add(chat);
            chatAdapter.notifyItemChanged(chatList.size() - 1);
            scrollToBottom();
            chat.setStatus(Chat.DELIVERED);
            Map<String, Object> postValues = chat.toMap();
            updateRef.updateChildren(postValues);
            setReplyMessageIdToNull();
            isLastItemHidden = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        isFirstUnread = true;
        active = true;
        countMsg = 0;
        EventBus.getDefault().register(this);

        if (recyclerTouchListener != null) {
            chatsView.addOnItemTouchListener(recyclerTouchListener);
        }

        ///hide notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
        notificationManager.cancel(relationshipId, NotificationEvent.TYPE_SINGLE_CHAT);

        chatList.clear();
        queueMessage.clear();
        chatAdapter.notifyDataSetChanged();
//        EventBus.getDefault().register(this);
        mAuth.addAuthStateListener(mAuthListener);
        connectedRef.addValueEventListener(onlineListener);
        chatRef.addChildEventListener(chatListener);
        inputTextChat.addTextChangedListener(textWatcher);
        chatsView.addOnScrollListener(onScrollListener);
        friendRelRef.addChildEventListener(friendRelRefListener);
        friendOnlineRef.addValueEventListener(friendOnlineListener);
        friendRef.addValueEventListener(friendListener);
//        tokenRef.addValueEventListener(tokenListener);
        tokenRef.addChildEventListener(tokenListener);
        myProfileRef.addValueEventListener(myProfileListener);
        //read count SP
        String keyCount = String.format(getString(R.string.COUNT_LAST_CHAT), relationshipId);
        countLastMsg = sharedpreferences.getInt(keyCount, 0);
        Log.i(TAG, "On Start:  count last message: " + countLastMsg);

    }

    public void setReplyMessageIdToNull(){
        replyMessageId = null;
        if(inputReplyLayout != null){
            inputReplyLayout.removeAllViews();
        }
    }




    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG,"On Stop => reply message Id: "+replyMessageId);
        active = false;
        chatRef.removeEventListener(chatListener);
//        for (Chat chat : chatList) {
//            chat.detachProfileRef();
//        }
        if (recyclerTouchListener != null) {
            chatsView.removeOnItemTouchListener(recyclerTouchListener);
        }

        //reset last message
        String keyLastMsg = String.format(getString(R.string.USER_SINGLE_CHAT_LAST_MESSAGE), relationshipId);
        String keyCountMsg = String.format(getString(R.string.USER_SINGLE_CHAT_COUNT), relationshipId);

        EventBus.getDefault().unregister(this);
        mAuth.removeAuthStateListener(mAuthListener);
        inputTextChat.removeTextChangedListener(textWatcher);
        connectedRef.removeEventListener(onlineListener);
        chatsView.removeOnScrollListener(onScrollListener);
        friendRelRef.removeEventListener(friendRelRefListener);
        friendOnlineRef.removeEventListener(friendOnlineListener);
        friendRef.removeEventListener(friendListener);
        tokenRef.removeEventListener(tokenListener);
        myProfileRef.removeEventListener(myProfileListener);
        meRef.removeValue();
        hideUnreadMessage();
        if (uploadTask != null && onSuccessUpload != null) {
            uploadTask.removeOnSuccessListener(onSuccessUpload);
        }
        if (mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
            mRunnable = null;
        }
//        EventBus.getDefault().unregister(this);
        //write count SP
        String keyCount = String.format(getString(R.string.COUNT_LAST_CHAT), relationshipId);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt(keyCount, countMsg);
        editor.putString(keyLastMsg, "");
        editor.putInt(keyCountMsg, 0);
        editor.apply();

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
            if (isConnected) {
                meRef.setValue(friendId);
                meRef.onDisconnect().removeValue();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener chatListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if (friendId == null) {
                return;
            }
            countMsg++;
            Chat chat = dataSnapshot.getValue(Chat.class);
            chat.setId(dataSnapshot.getKey());
            chat.setReplyChat(findChatById(chat.getReplyMessage()));
//            Log.i(TAG, "Message: " + chat.getBody() + " , display name: " + chat.getDisplayName());
            String thisDate = MyDateUtil.getDateMessage(SingleChatActivity.this, chat.getTimestamp());
            if (chatList.size() == 0 || (chatList.size() > 1 && chatList.get(chatList.size() - 1).getTimestamp() > 0 &&
                    !thisDate.equals(MyDateUtil.getDateMessage(SingleChatActivity.this, chatList.get(chatList.size() - 1).getTimestamp())))) {
                //this is for date message only
                Chat dateMessage = new Chat();
                dateMessage.setType(Chat.TYPE_DATE);
                dateMessage.setBody(thisDate);
                chatList.add(dateMessage);
            }
//            if (chatList.size() > 1 && chatList.get(chatList.size() - 1).getTimestamp() > 0 && chat.getType() == Chat.TYPE_TEXT
//                    && chatList.get(chatList.size() - 1).getReplyMessage() == null && chat.getReplyMessage() == null) {
//                Chat prevModel = chatList.get(chatList.size() - 1);
//                long diff = chat.getTimestamp() - prevModel.getTimestamp();
//                if (prevModel.getFrom().equals(chat.getFrom())
//                        && MyDateUtil.isMergeRequired(chat.getTimestamp(), prevModel.getTimestamp())
//                        && prevModel.getType() == Chat.TYPE_TEXT
//                        && chat.getType() == Chat.TYPE_TEXT && !isLastItemHidden) {
//                    if (friendId.equals(chat.getTo()) && prevModel.getStatus() != chat.getStatus()) { //check right side
//                        addMessage(chat);
//                    } else {
//                        Log.i(TAG, "Diff time: " + diff + " , message: " + chat.getBody());
//                        String newBody = prevModel.getBody() + "\n" + chat.getBody();
//                        prevModel.setBody(newBody);
//                        prevModel.setTimestamp(chat.getTimestamp());
//                        if (chatAdapter != null) {
//                            chatAdapter.notifyItemChanged(chatList.size() - 1);
//                        }
//                        int viewPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
//                        if (viewPosition >= chatList.size() - 2) {
//                            Log.i(TAG, "scroll to bottom");
//                            scrollToBottom();
//                        }
//                    }
//                } else {
//                    addMessage(chat);
//                }
//            } else {
                addMessage(chat);
//            }
            if (friendId.equals(chat.getFrom())) { //as receiver (left side)
//                Log.i(TAG, "Left side -> msg: " + chat.getBody());
                if (chat.getStatus() != Chat.READ) {
//                    Log.i(TAG, "Left side -> set flag read for msg: " + chat.getBody());
                    DatabaseReference updateRef = chatRef.child(chat.getId()).child("status");
                    updateRef.setValue(Chat.READ);
                }
                chat.setDisplayName(title);
            }
            //show unread message
            Log.i(TAG, "Is first unread: " + isFirstUnread + " , Count items: " + countMsg + " , last message: " + countLastMsg+" , is last item hidden: "+isLastItemHidden);

            if (isLastItemHidden) {
                countBadge++;
//                if (isLastItemHidden) {
                showDownNotification(countBadge);
//                }
                showUnread(countBadge);
            } else if (isFirstUnread && countMsg > countLastMsg) {
//                boolean isNewMsg = true;
//                boolean isUnreadExist = false;
//                for (int i = chatList.size() - 1; i >= 0; i--) {
//                    Chat model = chatList.get(i);
//                    if (model.getType() == Chat.TYPE_UNREAD_MESSAGE) {
//                        isUnreadExist = true;
//                        break;
//                    }
//                }
//                if(isUnreadExist){
//                    Chat prevModel = chatList.get(chatList.size() - 2);
//                    boolean typeMessage = prevModel.getType() == Chat.TYPE_TEXT ||
//                            prevModel.getType() == Chat.TYPE_IMAGE ||
//                            prevModel.getType() == Chat.TYPE_AUDIO;
//                    Log.i(TAG,"Prev msg: "+prevModel.getBody()+", status : "+prevModel.getStatus()+" , is type message: "+typeMessage);
//                    isNewMsg = typeMessage && prevModel.getStatus() == Chat.DELIVERED;
//                }
//                Log.i(TAG,"Is new message for unread: "+isNewMsg+" , is unread exist: "+isUnreadExist);
                long diff = System.currentTimeMillis() - curUnreadTime;
                Log.i(TAG, "Diff time: " + diff);
                if (chat.getStatus() == Chat.DELIVERED &&
                        friendId.equals(chat.getFrom()) && (diff < 5000 || curUnreadTime == 0)) {// && isNewMsg) {
                    int countUnread = countMsg - countLastMsg;
                    showUnread(countUnread);
                    curUnreadTime = System.currentTimeMillis();
                } else {
                    isFirstUnread = false;
                    hideUnreadMessage();
                }
            } else {
                hideUnreadMessage();
            }
            //Push Notification
            if (chat.getStatus() != Chat.READ && !isFriendOnline) {
                boolean isPushed = checkPosted(dataSnapshot);
                boolean isRightSide = !friendId.equals(chat.getFrom());
                Log.i("PushFCM", "Push notification is begin: is Pushed -> " + isPushed + " , is right side: " + isRightSide);
                if (!isPushed && isRightSide) {
                    Log.i("PushFCM", "Executed");
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("chats")
                            .child(relationshipId).child(dataSnapshot.getKey()).child("posted");
                    pushFCM.setMessageId(chat.getId());
                    pushFCM.push(chat.getBody(), ref);
                }
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                return;
            }
            Chat chat = dataSnapshot.getValue(Chat.class);
            chat.setId(dataSnapshot.getKey());
            Log.i(TAG, "On Changed -> Message: " + chat.getBody() + " , display name: " + chat.getDisplayName());
            if (firebaseUser.getUid().equals(chat.getFrom())) { //right side
                Log.i(TAG, "update to read..!! status: " + chat.getStatus());
                for (int i = chatList.size() - 1; i >= 0; i--) {
                    Chat model = chatList.get(i);
                    if (model.getId() != null && model.getId().equals(chat.getId())) {
                        model.setStatus(chat.getStatus());
                        model.setPushed(checkPosted(dataSnapshot));
                        chatAdapter.notifyItemChanged(i);
                        Log.i(TAG, "UPDATED -> msg: " + model.getBody() + " , posted: " + model.isPushed());
                        break;
                    }
                }
            }
//            else{
//                Log.i(TAG,"Left side: "+chat.getStatus());
//            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            int position = 0;
            for (Chat model : chatList) {
                if (model.getId() != null && model.getId().equals(dataSnapshot.getKey())) {
                    if (position > 0 && chatList.get(position - 1).getId() == null) {
                        chatList.remove(position - 1);
                        chatAdapter.notifyItemRemoved(position - 1);
                    }
                    chatList.remove(position);
                    chatAdapter.notifyItemRemoved(position);
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

    private ValueEventListener friendListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (getSupportActionBar() == null) {
                Log.e(TAG, "Action bar is NULL");
                return;
            }
//            Log.i(TAG, "Friend -> " + dataSnapshot);
            User model = dataSnapshot.getValue(User.class);
            getSupportActionBar().setTitle(model.getName());
            for (DataSnapshot election : dataSnapshot.getChildren()) {
                if (election.getKey().equals("images")) {
                    String key = model.getImageUrl() == null ? "default" : model.getImageUrl();
                    DataSnapshot defaultImage = election.child(key);
                    if (defaultImage.exists()) {
                        UserImage userImage = defaultImage.getValue(UserImage.class);
                        String imageUrl = userImage.getThumbPic();
                        imageLogo.setVisibility(View.VISIBLE);
                        imageLogo.setImageURI(imageUrl);
                        break;
                    }
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                finish();
                startActivity(new Intent(SingleChatActivity.this, LoginActivity.class));
            }
        }
    };

    private void addMessage(Chat chat) {
        if (chatAdapter == null) {
            return;
        }
        chatList.add(chat);
        chatAdapter.notifyItemInserted(chatList.size() - 1);
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        final int position = chatList.size() - 1;
//        if (firebaseUser != null && !firebaseUser.getUid().equals(chat.getFrom())) {
//            Log.i(TAG, "Read Message: " + chat.getBody() + " , position: " + position);
//            Log.i(TAG, "Add msg -> Last Position : " + chatList.get(position).getBody());
//            chat.attachProfileListener(chatAdapter, position);
//        }
        int viewPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
        if (viewPosition >= chatList.size() - 3) {
            Log.i(TAG, "scroll to bottom");
            scrollToBottom();
        }
    }

    private void scrollToBottom() {
        if (chatAdapter == null || chatsView == null) {
            return;
        }
        chatsView.postDelayed(new Runnable() {
            @Override
            public void run() {
                chatsView.scrollToPosition(chatsView.getAdapter().getItemCount() - 1);
            }
        }, 100);
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
        if (countMsg <= countLastMsg) {
            return;
        }
//        Log.w(TAG, "Hide unread message");
        for (int i = chatList.size() - 1; i >= 0; i--) {
            Chat model = chatList.get(i);
            if (model.getType() == Chat.TYPE_UNREAD_MESSAGE) {
//                Log.i(TAG, "Hide unread position: " + i + " , text: " + model.getBody());
                chatList.remove(i);
                chatAdapter.notifyItemRemoved(i);
                break;
            }
        }
    }

    private boolean checkPosted(DataSnapshot dataSnapshot) {
        boolean isExist = false;
        for (DataSnapshot election : dataSnapshot.getChildren()) {
            if (election.getKey().equals("posted")) {
                isExist = election.getValue(Boolean.class);
                break;
            }
        }
        return isExist;
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

    private void showDownNotification(int countBadge) {
        imageDown.setVisibility(countBadge == 0 ? View.GONE : View.VISIBLE);
        textBadge.setVisibility(countBadge == 0 ? View.GONE : View.VISIBLE);
        textBadge.setText(String.valueOf(countBadge));
    }

    private void showUnread(int countBadge) {
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
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
            Log.i(TAG, "Begin add unread message");
            final int position = chatList.size() - 1;
            final Chat unRead = new Chat();
            unRead.setType(Chat.TYPE_UNREAD_MESSAGE);
            String message = countBadge == 1 ? getString(R.string.unread_message_only_one) :
                    String.format(getString(R.string.unread_message), countBadge);
            unRead.setBody(message);
            chatList.add(position, unRead);
            chatAdapter.notifyItemInserted(position);

        }

        Log.i(TAG, "Is unread message exist: " + isUnreadExist);
//            }
//        }, 500);
    }

    private ValueEventListener friendOnlineListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                return;
            }
            String uid = firebaseUser.getUid();
            isFriendOnline = dataSnapshot.getValue() != null && dataSnapshot.getValue(String.class).equals(uid);
            Log.i(TAG, "Is Friend online listener: " + dataSnapshot + " , is online: " + isFriendOnline);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener tokenListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Token token = dataSnapshot.getValue(Token.class);
            if (token != null) {
                Log.i(TAG, "Get token: " + token.getToken());
                pushFCM.addRecipients(token.getToken());
            }
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

    private ValueEventListener myProfileListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            User model = dataSnapshot.getValue(User.class);
            pushFCM.setTitle(model.getName());
            for (DataSnapshot election : dataSnapshot.getChildren()) {
                if (election.getKey().equals("images")) {
                    String key = model.getImageUrl() == null ? "default" : model.getImageUrl();
                    DataSnapshot defaultImage = election.child(key);
                    if (defaultImage.exists()) {
                        UserImage userImage = defaultImage.getValue(UserImage.class);
                        pushFCM.setImageUrl(userImage.getThumbPic());
                        break;
                    }
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener friendRelRefListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//            Log.i(TAG, "Friend relationship -> " + dataSnapshot);
            Relationship model = dataSnapshot.getValue(Relationship.class);
            model.setId(dataSnapshot.getKey());
            handleBlockedUser(model);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Relationship model = dataSnapshot.getValue(Relationship.class);
            model.setId(dataSnapshot.getKey());
            handleBlockedUser(model);
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

    private void handleBlockedUser(Relationship model) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }
        boolean isContainMe = model.getFollower().equals(firebaseUser.getUid()) ||
                model.getFollowing().equals(firebaseUser.getUid());
        boolean isUserBlocked = model.getStatus() != Relationship.ACCEPTED;
        isBlocked = isContainMe && isUserBlocked;
        if (isBlocked) {
            showToastBlocked();
        }
    }

    private void showToastBlocked() {
        Toast.makeText(SingleChatActivity.this, getString(R.string.alert_block_user), Toast.LENGTH_SHORT).show();
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
                animHideNewMessage = AnimationUtils.loadAnimation(SingleChatActivity.this, R.anim.slide_left);
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
                animShowNewMessage = AnimationUtils.loadAnimation(SingleChatActivity.this, R.anim.slide_right);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChatEvent(OnChatRoomEvent event) {
        Log.i(TAG, "Chat listener => message: " + event.room.getChat().getBody() + " , room name: " + event.room.getName());
        Room room = event.room;
        Intent intent = new Intent(SingleChatActivity.this, RoomActivity.class);
        intent.putExtra(RoomActivity.KEY_ID, room.getId());
        intent.putExtra(RoomActivity.KEY_ROOM_NAME, room.getName());
        intent.putExtra(RoomActivity.KEY_IS_JOINED, true);
        room.setIntent(intent);
        queueMessage.add(room);
        showNewMessage();
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
                        Log.i(TAG, "On image completed: " + path);
                        Ad_Helper.adCount++;

                        if(Ad_Helper.wannaSendPrivate(Ad_Helper.adCount)){
                            SendAd();
                        }

                        if (MyImageUtil.isImageFile(new File(path))) {
                            doUploadImage(path);
                        } else {
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
                            chat.setTo(friendId);
                            chat.setStatus(Chat.DELIVERED);
                            chat.setReplyMessage(replyMessageId);
                            new VideoUploadDialog(SingleChatActivity.this, path, chatRef, chat).show();
                        }
                    }
                })
                .create().show(getSupportFragmentManager());
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
        Ad_Helper.adCount++;

        if(Ad_Helper.wannaSendPrivate(Ad_Helper.adCount)){
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
            @SuppressWarnings("VisibleForTests")
            public void onSuccessUpload(final String key, UploadTask.TaskSnapshot taskSnapshot) {
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
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Log.i(TAG, "Success to Upload, url: " + downloadUrl);
                        Chat chat = new Chat();
                        chat.setType(Chat.TYPE_IMAGE);
                        chat.setFrom(uid);
                        chat.setPath(path);
                        chat.setUrlThumbnail(thumbUrl.toString());
                        chat.setUrl(downloadUrl.toString());
                        chat.setBody(getString(R.string.send_an_image));
                        chat.setTo(friendId);
                        chat.setStatus(Chat.DELIVERED);
                        chat.setReplyMessage(replyMessageId);
                        DatabaseReference updateRef = chatRef.push();
                        Map<String, Object> postValues = chat.toMap();
                        updateRef.updateChildren(postValues);
                        setReplyMessageIdToNull();
                        removeChat(key);
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

}
