package com.rndtechnosoft.fynder.activity;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rndtechnosoft.fynder.FynderApplication;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.adapter.UsersAdapter;
import com.rndtechnosoft.fynder.dialog.VideoUploadDialog;
import com.rndtechnosoft.fynder.fragment.GlobalFragment;
import com.rndtechnosoft.fynder.fragment.NearbyFriendsFragment;
import com.rndtechnosoft.fynder.fragment.PrivateFragment;
import com.rndtechnosoft.fynder.fragment.RoomFragment;
import com.rndtechnosoft.fynder.model.Chat;
import com.rndtechnosoft.fynder.model.Room;
import com.rndtechnosoft.fynder.model.User;
import com.rndtechnosoft.fynder.model.UserImage;
import com.rndtechnosoft.fynder.model.event.NotificationEvent;
import com.rndtechnosoft.fynder.model.event.OnChatRoomEvent;
import com.rndtechnosoft.fynder.utility.Ad_Helper;
import com.rndtechnosoft.fynder.utility.Constants;
import com.rndtechnosoft.fynder.utility.CustomTypefaceSpan;
import com.rndtechnosoft.fynder.utility.MyImageUtil;
import com.rndtechnosoft.fynder.utility.Token;
import com.rndtechnosoft.fynder.utility.image.bottompicker.ImageBottomPicker;
import com.rndtechnosoft.fynder.utility.listener.OnSuccessUploadListener;
import com.rndtechnosoft.fynder.utility.listener.OnTabUpdateListener;
import com.rndtechnosoft.fynder.utility.listener.PttOnTouchListener;
import com.rndtechnosoft.fynder.utility.listener.SendGlobalChatListener;
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

public class NearbyHomeActivity extends AppCompatActivity implements OnTabUpdateListener
        , NavigationView.OnNavigationItemSelectedListener, ResultCallback<LocationSettingsResult> {
    public static final String KEY_POSITION_TAB = NearbyHomeActivity.class.getName() + ".POSITION_TAB";
    private static final int REQUEST_CHECK_SETTINGS = 11;
    private final String TAG = "NearbyHomeActivity.log";
    private final int OPEN_IMAGE = 536;
    private final String[] TITLE = {"NEAR BY", "GLOBAL", "ROOM", "PRIVATE"};
    private int[] arrayCountBadge = new int[TITLE.length];
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private static ViewPager mViewPager;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private TextView textNavName;
    private TextView textNavEmail;
    private SimpleDraweeView imageProfile;
    private LinearLayout layoutInputGlobalChat;
    private LinearLayout inputReplyLayout;
    private ImageButton sendButton;
    private ImageButton audioButton;
    private ImageButton smileyButton;
    private EmojiEditText inputTextChat;
    private EmojiPopup emojiPopup;
    //    private ListView mDrawerList;
    private LinearLayout mDrawer;
    private ImageView imageDown;
    private TextView textBadge;
    private DatabaseReference onlineParticipant;
    private DatabaseReference connectedRef;
    private DatabaseReference checkBannedRef;
    private DatabaseReference settingRef;
    private DatabaseReference myProfileRef;
    private StorageReference storageRef;
    private UsersAdapter adapterDrawer;
    //    private ProgressDialog myProgressDialog;
    private List<User> userList = new ArrayList<>();
    private boolean isConnected;
    //    private boolean isShown;
    private SendGlobalChatListener listener;

    private Animation animShowNewMessage;
    private Animation animHideNewMessage;
    private RelativeLayout newMessageLayout;
    private SimpleDraweeView newMessageImage;
    private TextView roomNewMessage;
    private TextView subjectNewMessage;
    private TextView textNewMessage;
    private List<Room> queueMessage = new ArrayList<>();
    private SharedPreferences sharedpreferences;
    private String replyMessageId;
    //    private int typeImage;
    private UploadTask uploadTask;
    private OnSuccessListener<UploadTask.TaskSnapshot> onSuccessUpload;
    private FirebaseUser firebaseUser;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static DatabaseReference profileRef;

    private void setListener(SendGlobalChatListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "On Create");
        Intent intent = getIntent();
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        sharedpreferences = getSharedPreferences(getString(R.string.KEY_PREF_NAME), Context.MODE_PRIVATE);
        Ad_Helper.ShowBigAd(NearbyHomeActivity.this);
        AdView mAdView = (AdView) findViewById(R.id.adView);
        Ad_Helper.loadbanner(mAdView);
//        myProgressDialog = new ProgressDialog(this);
//        myProgressDialog.setMessage(getString(R.string.uploading_image));
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);

        mDrawer = (LinearLayout) findViewById(R.id.right_drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_right_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        textNavName = (TextView) header.findViewById(R.id.nav_display_name);
        textNavEmail = (TextView) header.findViewById(R.id.nav_email);
        imageProfile = (SimpleDraweeView) header.findViewById(R.id.nav_image_profile);
        RoundingParams circle = RoundingParams.asCircle()
                .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY);
        imageProfile.getHierarchy().setRoundingParams(circle);
        imageProfile.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
        imageProfile.getHierarchy().setPlaceholderImage(R.drawable.ic_profile_default);

        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser == null) {
                    return;
                }
                FynderApplication.getInstance().sendActionevent(Constants.CT_NAV_HEADER);
                Intent intent = new Intent(NearbyHomeActivity.this, ProfileActivity.class);
                intent.putExtra(ProfileActivity.KEY_UID, firebaseUser.getUid());
                intent.putExtra(ProfileActivity.KEY_DISPLAY_NAME, firebaseUser.getDisplayName());
                startActivity(intent);
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list_user);
//        textStatus = (TextView) findViewById(R.id.text_status);
        adapterDrawer = new UsersAdapter(NearbyHomeActivity.this, userList, false, false, new UsersAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User item) {
                FynderApplication.getInstance().sendActionevent(Constants.CT_NAV_HEADER);
                Intent intent = new Intent(NearbyHomeActivity.this, ProfileActivity.class);
                intent.putExtra(ProfileActivity.KEY_UID, item.getId());
                intent.putExtra(ProfileActivity.KEY_DISPLAY_NAME, item.getName());
                startActivity(intent);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(NearbyHomeActivity.this));
        recyclerView.setAdapter(adapterDrawer);


        Menu m = navigationView.getMenu();
        for (int i = 0; i < m.size(); i++) {
            MenuItem mi = m.getItem(i);
            applyFontToMenuItem(mi);
        }

        displayLocationSettingsRequest(this);
        checkLocationPermission(this);
        sendButton = (ImageButton) findViewById(R.id.button_send);
        audioButton = (ImageButton) findViewById(R.id.button_audio);
        smileyButton = (ImageButton) findViewById(R.id.button_smiley);
        inputTextChat = (EmojiEditText) findViewById(R.id.input_text_chat);
        ViewGroup rootView = (ViewGroup) findViewById(R.id.main_content);
        layoutInputGlobalChat = (LinearLayout) findViewById(R.id.layout_input_text);
        inputReplyLayout = (LinearLayout) findViewById(R.id.input_reply_layout);
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

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        for (int i = 0; i < TITLE.length; i++) {
            customTabView(i, 0);
        }
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        recyclerView.setOnClickListener(new DrawerItemClickListener());

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    checkBannedRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("name");
                    checkBannedRef.addValueEventListener(checkBannedListener);
//                    String adminId = getString(R.string.administrator_id);
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    finish();
                    startActivity(new Intent(NearbyHomeActivity.this, LoginActivity.class));
                }
            }
        };

        onlineParticipant = FirebaseDatabase.getInstance().getReference("rooms").child("global").child("participant");
        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        storageRef = FirebaseStorage.getInstance().getReference();

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

        inputTextChat.setOnClickListener(new View.OnClickListener() {
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
                    Toast.makeText(NearbyHomeActivity.this, getString(R.string.error_internet), Toast.LENGTH_SHORT).show();
                    return;
                }

                Ad_Helper.adCountGlobal++;

                if (Ad_Helper.wannaSendGlobal(Ad_Helper.adCountGlobal)) {
                    SendAd();
                }

                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null) {
                    DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("rooms").child("global").child("message");
                    DatabaseReference updateRef = chatRef.push();
                    Chat chat = new Chat();
                    chat.setBody(inputTextChat.getText().toString());
                    chat.setFrom(firebaseUser.getUid());
                    chat.setReplyMessage(replyMessageId);
                    Map<String, Object> postValues = chat.toMap();
                    updateRef.updateChildren(postValues);
                    if (listener != null) {
                        listener.scrollToBottom();
                    }
                    setReplyMessageIdToNull();
                } else {
                    Toast.makeText(NearbyHomeActivity.this, getString(R.string.error_send_text_message), Toast.LENGTH_SHORT).show();
                }
                inputTextChat.setText("");

            }
        });
        audioButton.setOnTouchListener(new PttOnTouchListener(this) {
            @Override
            public void onCompleteRecorded(final String audioFilePath) {
                Ad_Helper.adCountGlobal++;

                if (Ad_Helper.wannaSendGlobal(Ad_Helper.adCountGlobal)) {
                    SendAd();
                }

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
                String tempKey = String.valueOf(System.currentTimeMillis());
                String uid = firebaseUser.getUid();
                Chat chat = new Chat();
                chat.setType(Chat.TYPE_AUDIO);
                chat.setFrom(uid);
                chat.setId(tempKey);
                chat.setReplyMessage(replyMessageId);
                if (listener != null) {
                    listener.addChat(chat);
                }
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
                        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("rooms").child("global").child("message");
                        DatabaseReference updateRef = chatRef.push();
                        Map<String, Object> postValues = chat.toMap();
                        updateRef.updateChildren(postValues);
                        if (listener != null) {
                            listener.scrollToBottom();
                            listener.removeChat(key);
                        }
                        setReplyMessageIdToNull();
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
                if (listener != null) {
                    listener.scrollToBottom();
                }
            }
        });
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FynderApplication.getInstance().createProfile(firebaseUser.getDisplayName(),
                firebaseUser.getUid(), firebaseUser.getEmail(), "", "", 0, firebaseUser.getPhotoUrl().toString());
        if (firebaseUser != null) {
            settingRef = FirebaseDatabase.getInstance().getReference("settings").child(firebaseUser.getUid());
            myProfileRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            setLocationData(getApplicationContext());
        }
    }

    private void SendAd() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("rooms").child("global").child("message");
            DatabaseReference updateRef = chatRef.push();
            Chat chat = new Chat();
            chat.setType(Chat.TYPE_AD);
            chat.setBody(" ");
            chat.setAdName(Ad_Helper.randomStr);
            chat.setFrom(firebaseUser.getUid());
            chat.setReplyMessage(replyMessageId);
            Map<String, Object> postValues = chat.toMap();
            updateRef.updateChildren(postValues);
            if (listener != null) {
                listener.scrollToBottom();
            }
            setReplyMessageIdToNull();
        }
    }

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = Typeface.createFromAsset(getAssets(), "Ubuntu-L.ttf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    public static void setLocationData(Context context) {
        if (context != null) {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                profileRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());
            }
            profileRef.child("latitude").setValue(FynderApplication.getInstance().getLat());
            profileRef.child("longitude").setValue(FynderApplication.getInstance().getLng());
            profileRef.child("location").setValue(FynderApplication.getInstance().getLat() + "," + FynderApplication.getInstance().getLng());
            profileRef.child("address").setValue(FynderApplication.getInstance().getArea());
            profileRef.child("city").setValue(FynderApplication.getInstance().getCity());
            profileRef.child("country").setValue(FynderApplication.getInstance().getCountry());
        }
    }


    @Override
    public void onTabUpdate(int position, int countBadge) {
        Log.i(TAG, "On Tab update: " + position + " , Count Badge: " + countBadge);
        arrayCountBadge[position] = countBadge;
        customTabView(position, countBadge);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.drawer_myprofile:
                FynderApplication.getInstance().sendActionevent(Constants.CT_NAV_MYPROFILE);
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser == null) {
                    break;
                }

                Intent intent1 = new Intent(NearbyHomeActivity.this, ProfileActivity.class);
                intent1.putExtra(ProfileActivity.KEY_UID, firebaseUser.getUid());
                intent1.putExtra(ProfileActivity.KEY_DISPLAY_NAME, firebaseUser.getDisplayName());
                startActivity(intent1);
                break;
            case R.id.drawer_friends:
                FynderApplication.getInstance().sendActionevent(Constants.CT_NAV_SETTING);
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.drawer_rate:
                FynderApplication.getInstance().sendActionevent(Constants.CT_NAV_RATEUS);
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                }
                break;
            case R.id.drawer_more:
                FynderApplication.getInstance().sendActionevent(Constants.CT_NAV_MOREAPPS);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.more_app))));
                break;
            case R.id.drawer_share:
                FynderApplication.getInstance().sendActionevent(Constants.CT_NAV_SHARE);
                try {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "\n" +
                            "Let me recommend you this application for Best Chat App, you can find nearby people as well as nearby groups \n" +
                            "\n https://play.google.com/store/apps/details?id=" + getPackageName());
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                } catch (Exception e) {
                    //e.toString();
                }
                break;
            case R.id.drawer_contact:
                FynderApplication.getInstance().sendActionevent(Constants.CT_NAV_CONTACT);
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setType("message/rfc822");
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getResources().getString(R.string.app_email)});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Fynder Feedback");
                intent.putExtra(Intent.EXTRA_TEXT, "Hey Team,\n\n Your Name: \n\nNumber: \n\nPurpose: \n\nMessage: \n\n");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                break;
            case R.id.drawer_setting:
                FynderApplication.getInstance().sendActionevent(Constants.CT_NAV_ABOUT);
                Intent i = new Intent(getBaseContext(), AboutActivity.class);
                startActivity(i);
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {

    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().

                            checkLocationPermission(NearbyHomeActivity.this);
                        } catch (Exception e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Log.i(TAG, "Create fragment position: " + position);
            switch (position) {
                case 0:
                    return new NearbyFriendsFragment();
                case 1:
                    GlobalFragment globalFragment = new GlobalFragment();
                    setListener(globalFragment);
                    return globalFragment;
                case 2:
                    return new RoomFragment();
                default:
                    return new PrivateFragment();
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Log.i(TAG, "Get page title: " + position);
            return TITLE[position];
//            switch (position) {
//                case 0:
//                    return "GLOBAL";
//                case 1:
//                    return "ROOM";
//                case 2:
//                    return "PRIVATE";
//            }
//            return null;
        }
    }

//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            // Check for the integer request code originally supplied to startResolutionForResult().
//            case REQUEST_CHECK_SETTINGS:
//                switch (resultCode) {
//                    case Activity.RESULT_OK:
//                        Log.i(TAG, "User agreed to make required location settings changes.");
//                        //Request location updates:
//                        checkLocationPermission(this);
//                        break;
//                    case Activity.RESULT_CANCELED:
//                        Log.i(TAG, "User chose not to make required location settings changes.");
//                        break;
//                }
//                break;
//        }
//    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "On Start");
//        isShown = true;
        EventBus.getDefault().register(this);
        userList.clear();
        queueMessage.clear();
        mAuth.addAuthStateListener(mAuthListener);
        onlineParticipant.addChildEventListener(childEventListener);
        mViewPager.addOnPageChangeListener(onPageChangeListener);
        inputTextChat.addTextChangedListener(textWatcher);
        connectedRef.addValueEventListener(onlineListener);
        drawer.addDrawerListener(toggle);
        if (settingRef != null) {
            settingRef.addValueEventListener(settingListener);
        }
        if (myProfileRef != null) {
            myProfileRef.addValueEventListener(myProfileListener);
        }

        String tokenId = sharedpreferences.getString(getString(R.string.USER_TOKEN), "");
        //register a token
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            Log.i(TAG, "Token: " + tokenId);
            Log.i(TAG, "User ID: " + userId);
            if (!tokenId.isEmpty()) {
                String deviceId = Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                Token token = new Token(userId, tokenId);
                DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("token").child(deviceId);
                tokenRef.setValue(token);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isOpened = false;
    }

    public static void setTab(int position) {
        mViewPager.setCurrentItem(position);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "On Stop");
//        isShown = false;
        drawer.removeDrawerListener(toggle);
        EventBus.getDefault().unregister(this);
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        if (onlineParticipant != null && childEventListener != null) {
            onlineParticipant.removeEventListener(childEventListener);
        }
        if (settingRef != null && settingListener != null) {
            settingRef.removeEventListener(settingListener);
        }
        if (myProfileRef != null) {
            myProfileRef.removeEventListener(myProfileListener);
        }
        for (User user : userList) {
            user.detachProfileRef();
        }
        if (uploadTask != null && onSuccessUpload != null) {
            uploadTask.removeOnSuccessListener(onSuccessUpload);
        }

        mViewPager.removeOnPageChangeListener(onPageChangeListener);
        inputTextChat.removeTextChangedListener(textWatcher);
        connectedRef.removeEventListener(onlineListener);
        try {
            checkBannedRef.removeEventListener(checkBannedListener);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt(getString(R.string.USER_LAST_OPEN_TAB), mViewPager.getCurrentItem());
        editor.apply();
    }

    private ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.i(TAG, "onChildAdded uid: " + dataSnapshot.getKey());
            User userProfile = new User();
            userProfile.setId(dataSnapshot.getKey());
            userList.add(userProfile);
            userProfile.attachProfileRef(adapterDrawer);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.i(TAG, "onChildChanged uid: " + dataSnapshot.getKey());
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.i(TAG, "onChildRemoved uid: " + dataSnapshot.getKey());
            for (User user : userList) {
                if (user.getId().equals(dataSnapshot.getKey())) {
                    user.detachProfileRef();
                    userList.remove(user);
                    adapterDrawer.notifyDataSetChanged();
                    break;
                }
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG, "On Cancel: " + databaseError.getMessage());
        }
    };

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        } else if (drawer.isDrawerOpen(GravityCompat.END)) {  /*Closes the Appropriate Drawer*/
            drawer.closeDrawer(GravityCompat.END);
            return;
        } else {
            super.onBackPressed();
        }
        if (emojiPopup != null && emojiPopup.isShowing()) {
            emojiPopup.dismiss();
        } else {
            super.onBackPressed();
        }
    }


    private class DrawerItemClickListener implements RecyclerView.OnClickListener {
        @Override
        public void onClick(View v) {
            drawer.closeDrawer(mDrawer);
        }
    }

    private ViewPager.OnPageChangeListener onPageChangeListener =
            new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    Log.i(TAG, "on page selected: " + position);
                    layoutInputGlobalChat.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
                    for (int i = 0; i < TITLE.length; i++) {
                        customTabView(i, arrayCountBadge[i]);
                    }

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            };

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

    public DrawerLayout getDrawer() {
        return drawer;
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChatEvent(OnChatRoomEvent event) {
        Log.i(TAG, "Chat listener => message: " + event.room.getChat().getBody() + " , room name: " + event.room.getName());
        Room room = event.room;
        Intent intent = new Intent(NearbyHomeActivity.this, RoomActivity.class);
        intent.putExtra(RoomActivity.KEY_ID, room.getId());
        intent.putExtra(RoomActivity.KEY_ROOM_NAME, room.getName());
        intent.putExtra(RoomActivity.KEY_IS_JOINED, true);
        room.setIntent(intent);
        queueMessage.add(room);
        showNewMessage();
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
                animHideNewMessage = AnimationUtils.loadAnimation(NearbyHomeActivity.this, R.anim.slide_left);
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
                animShowNewMessage = AnimationUtils.loadAnimation(NearbyHomeActivity.this, R.anim.slide_right);
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

    public EmojiEditText getInputTextChat() {
        return inputTextChat;
    }

    public LinearLayout getInputReplyLayout() {
        return inputReplyLayout;
    }

    public void setReplyMessageId(String id) {
        replyMessageId = id;
    }

    public void showDownNotification(int countBadge) {
        imageDown.setVisibility(countBadge == 0 ? View.GONE : View.VISIBLE);
        textBadge.setVisibility(countBadge == 0 ? View.GONE : View.VISIBLE);
        textBadge.setText(String.valueOf(countBadge));
    }

    private void customTabView(int position, int countBadge) {
        View view = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        TextView textTitle = (TextView) view.findViewById(R.id.tab_title);
        TextView textBadge = (TextView) view.findViewById(R.id.tab_badge);
        textTitle.setTextColor(mViewPager.getCurrentItem() == position ?
                ContextCompat.getColor(this, R.color.color_white) :
                ContextCompat.getColor(this, R.color.color_white));
        Log.i(TAG, "custom tab view: " + position + " | " + countBadge);
        textBadge.setVisibility(countBadge < 1 || mViewPager.getCurrentItem() == position ? View.GONE : View.VISIBLE);
//        textBadge.setText(String.valueOf(countBadge));
        textTitle.setText(TITLE[position]);
        tabLayout.getTabAt(position).setCustomView(null);
        tabLayout.getTabAt(position).setCustomView(view);
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
        } else if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            boolean permission1 = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean permission2 = grantResults[1] == PackageManager.PERMISSION_GRANTED;
            boolean isPermissionGranted = permission1 && permission2;
            if (isPermissionGranted) {
                //Request location updates:
                FynderApplication.getInstance().getLocation();
                setLocationData(getApplicationContext());
            } else {
                checkLocationPermission(this);
            }
            return;
        }
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
        ImageBottomPicker imageBottomPicker = new ImageBottomPicker.Builder(this)
                .setVideoSelection()
                .setOnImageSelectedListener(new ImageBottomPicker.OnImageSelectedListener() {
                    @Override
                    public void onImageSelected(String path) {
                        Log.i(TAG, "On image Selected: " + path);
                        if (path != null) {
                            Ad_Helper.adCountGlobal++;

                            if (Ad_Helper.wannaSendGlobal(Ad_Helper.adCountGlobal)) {
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
                                chat.setReplyMessage(replyMessageId);
                                DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("rooms").child("global").child("message");
                                new VideoUploadDialog(NearbyHomeActivity.this, path, chatRef, chat).show();
                            }
                        }
                    }
                })
                .create();
        imageBottomPicker.show(getSupportFragmentManager());
    }

    private void doUploadImage(final String path) {
        Ad_Helper.adCountGlobal++;

        if (Ad_Helper.wannaSendGlobal(Ad_Helper.adCountGlobal)) {
            SendAd();
        }
        String tempKey = String.valueOf(System.currentTimeMillis());
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }
        String uid = firebaseUser.getUid();
        final Chat chat = new Chat();
        chat.setType(Chat.TYPE_IMAGE);
        chat.setPath(path);
        chat.setFrom(uid);
        chat.setId(tempKey);
        chat.setTimestamp(System.currentTimeMillis() / 1000);
        chat.setReplyMessage(replyMessageId);
        if (listener != null) {
            listener.addChat(chat);
        }
        StorageReference imageRef = storageRef.child(uid + "/chat/images/thumbnail/" + System.currentTimeMillis() + ".jpg");
        final Bitmap bitmap = BitmapFactory.decodeFile(path);
        Bitmap thumbnail = MyImageUtil.scaleBitmap(bitmap, 70, 70);
        inputReplyLayout.removeAllViews();

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
//                Bitmap picture = MyImageUtil.scaleBitmap(bitmap, 450, 450, null);
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                picture.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                byte[] highResData = baos.toByteArray();
                if (listener != null) {
                    chat.setUrlThumbnail(thumbUrl.toString());
                    listener.updateChat(chat);
                }
//                UploadTask uploadTask = imageRef.putBytes(highResData);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error upload thumb image: " + e.getMessage());
                        if (listener != null) {
                            listener.removeChat(key);
                        }
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
                        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("rooms")
                                .child("global").child("message");//.child(key);
//                        chatRef.setValue(chat);
                        DatabaseReference updateRef = chatRef.push();
                        Map<String, Object> postValues = chat.toMap();
                        updateRef.updateChildren(postValues);
                        if (listener != null) {
                            listener.scrollToBottom();
                            listener.removeChat(key);
                        }
                        setReplyMessageIdToNull();
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

    private ValueEventListener checkBannedListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() == null) {
                FirebaseAuth.getInstance().signOut();
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.clear();
                editor.apply();
                finish();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private boolean isOpened;
    private ValueEventListener settingListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.i(TAG, "setting listener: " + dataSnapshot);

            if (isOpened) {
                return;
            }
            isOpened = true;

            if (dataSnapshot.getValue() == null) {
                mViewPager.setCurrentItem(sharedpreferences.getInt(getString(R.string.USER_LAST_OPEN_TAB), 0));
                return;
            }
            for (DataSnapshot election : dataSnapshot.getChildren()) {
                if (election.getKey().equals("firstOpen")) {
                    int requiredPos = NearbyHomeActivity.this.getIntent().getIntExtra(KEY_POSITION_TAB, -1);
                    if (requiredPos == -1) {
                        int key = election.getValue(Integer.class);
                        if (key > 0) {
                            mViewPager.setCurrentItem(key - 1);
                        } else {
                            mViewPager.setCurrentItem(sharedpreferences.getInt(getString(R.string.USER_LAST_OPEN_TAB), 0));
                        }
                    } else {
                        mViewPager.setCurrentItem(requiredPos);
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
            textNavName.setText(model.getName());
            textNavEmail.setText(model.getEmail());
            for (DataSnapshot election : dataSnapshot.getChildren()) {
                if (election.getKey().equals("images")) {
                    String key = model.getImageUrl() == null ? "default" : model.getImageUrl();
                    DataSnapshot defaultImage = election.child(key);
                    if (defaultImage.exists()) {
                        UserImage userImage = defaultImage.getValue(UserImage.class);
                        imageProfile.setImageURI(userImage.getThumbPic());
                        break;
                    }
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    public void setReplyMessageIdToNull() {
        replyMessageId = null;
        if (inputReplyLayout != null) {
            inputReplyLayout.removeAllViews();
        }
    }

    public void checkLocationPermission(final Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);

                return;
            } else {
                FynderApplication.getInstance().getLocation();
                setLocationData(getApplicationContext());
            }

        }
    }
}
