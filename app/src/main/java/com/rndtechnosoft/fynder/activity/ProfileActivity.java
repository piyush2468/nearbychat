package com.rndtechnosoft.fynder.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rndtechnosoft.fynder.FynderApplication;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.adapter.GalleryAdapter;
import com.rndtechnosoft.fynder.model.Relationship;
import com.rndtechnosoft.fynder.model.User;
import com.rndtechnosoft.fynder.model.UserImage;
import com.rndtechnosoft.fynder.model.event.NotificationEvent;
import com.rndtechnosoft.fynder.utility.Constants;
import com.rndtechnosoft.fynder.utility.MyDateUtil;
import com.rndtechnosoft.fynder.utility.Network;
import com.rndtechnosoft.fynder.utility.PushFCM;
import com.rndtechnosoft.fynder.utility.Token;
import com.rndtechnosoft.fynder.utility.listener.AppBarStateChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private final String TAG = "ProfileActivity";
    public static final String KEY_UID = ProfileActivity.class.getName() + ".KEY_UID";
    public static final String KEY_DISPLAY_NAME = ProfileActivity.class.getName() + ".KEY_DISPLAY_NAME";
    private SimpleDraweeView imageHeader;
    private AppBarLayout appBarLayout;
    private View viewOverlay;
    private CardView layoutAttention;
    private CardView layoutImages;
    private CardView layoutLastOnline;
    private TextView textBirthday, textGender, textDisplayName, textAttention, textEmail, textLastOnline, textRelationship, textInteresrtedIn, textLookingFor;
    private LinearLayout layoutBirthday, layoutGender, layoutRelationShip, layoutInteresredIn, layoutLookingFor, layoutEmail;
    private MenuItem menuBlock;
    private RecyclerView galleryView;
    private GalleryAdapter adapter;
    private List<UserImage> galleryList = new ArrayList<>();
    private String uid;
    private String displayName;
    private String myUid;
    private String mainImage;
    private String mainImageUrl;
    private Relationship myRel;
    private int status = -1;
    private boolean isConnected = false;
    private boolean isFirstState = true;
    private FirebaseAuth mAuth;
    private FloatingActionButton fab;
    private FloatingActionButton fabDecline;

    private DatabaseReference profileRef;
    //    private DatabaseReference connectedRef;
    private DatabaseReference imagesRef;
    private DatabaseReference lastOnlineRef;
    private DatabaseReference myProfileRef;
    private Query followingRef;
    private Query followerRef;
    private Query tokenRef;
    private List<Relationship> relationships = new ArrayList<>();

    private PushFCM pushFCM;
    private DatabaseReference connectedRef;
    private MenuItem editProfile,logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        uid = intent.getStringExtra(KEY_UID);
        displayName = intent.getStringExtra(KEY_DISPLAY_NAME);

        if (uid == null || displayName == null) {
            finish();
            return;
        }

        if(uid.equalsIgnoreCase(myUid)){
            FynderApplication.getInstance().sendScreenviwedevent(Constants.CT_MYPROFILE,displayName);
        }else{
            FynderApplication.getInstance().sendScreenviwedevent(Constants.CT_USERPROFILE,displayName);
        }

        pushFCM = new PushFCM(this, NotificationEvent.TYPE_PROFILE);

        imageHeader = (SimpleDraweeView) findViewById(R.id.image_header);
        viewOverlay = findViewById(R.id.overlay_background);
        textLastOnline = (TextView) findViewById(R.id.text_last_login);
        textEmail = (TextView) findViewById(R.id.text_email);
        textAttention = (TextView) findViewById(R.id.text_attention);
        textDisplayName = (TextView) findViewById(R.id.text_display_name);
        textBirthday = (TextView) findViewById(R.id.text_birthday);
        textGender = (TextView) findViewById(R.id.text_gender);
        textRelationship = (TextView) findViewById(R.id.text_relationship);
        textLookingFor = (TextView) findViewById(R.id.text_looking_for);
        textInteresrtedIn = (TextView) findViewById(R.id.text_interestedin);

        layoutImages = (CardView) findViewById(R.id.layout_images);
        layoutAttention = (CardView) findViewById(R.id.layout_attention);
        layoutLastOnline = (CardView) findViewById(R.id.layout_last_online);
        layoutBirthday = (LinearLayout) findViewById(R.id.layout_birthday);
        layoutGender = (LinearLayout) findViewById(R.id.layout_gender);
        layoutRelationShip = (LinearLayout) findViewById(R.id.layout_relationStatus);
        layoutInteresredIn = (LinearLayout) findViewById(R.id.layout_interestedin);
        layoutLookingFor = (LinearLayout) findViewById(R.id.layout_looking_for);
        layoutEmail = (LinearLayout) findViewById(R.id.layout_email);

        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
//        appBarLayout.setExpanded(urlImage != null);
        appBarLayout.setExpanded(false);

        galleryView = (RecyclerView) findViewById(R.id.list_gallery);
        adapter = new GalleryAdapter(galleryList, new GalleryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(UserImage item) {
                if (uid == null || item.getId() == null) {
                    return;
                }
                Intent intent = new Intent(ProfileActivity.this, FullProfileImageActivity.class);
                intent.putExtra(FullProfileImageActivity.KEY_IMAGE, item.getId());
                intent.putExtra(FullProfileImageActivity.KEY_IMAGE_URL, item.getOriginalPic());
                intent.putExtra(FullProfileImageActivity.KEY_UID, uid);
                startActivity(intent);
            }
        });
        galleryView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        galleryView.setAdapter(adapter);

        profileRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        lastOnlineRef = profileRef.child("lastOnline");
        tokenRef = FirebaseDatabase.getInstance().getReference("token").orderByChild("userId").equalTo(uid);
        imagesRef = profileRef.child("images");
        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fabDecline = (FloatingActionButton) findViewById(R.id.fab_close);
        fabDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Network.isConnected(ProfileActivity.this)) {
                    Toast.makeText(ProfileActivity.this, getString(R.string.error_internet), Toast.LENGTH_SHORT).show();
                    return;
                }
                new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle(getString(R.string.title_decline))
                        .setMessage(getString(R.string.message_dialog_decline))
                        .setPositiveButton(getString(R.string.title_decline),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        DatabaseReference relationRef = FirebaseDatabase.getInstance().getReference("relationship");
                                        Relationship model = new Relationship();
                                        model.setFollower(myRel.getFollower());
                                        model.setFollowing(myRel.getFollowing());
                                        model.setStatus(Relationship.DECLINED);
                                        model.setActionUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        Map<String, Object> postValues = model.toMap();
                                        DatabaseReference ref = relationRef.child(myRel.getId());
                                        ref.updateChildren(postValues);
                                        pushFCM.setMessageId(myRel.getId());
                                        pushFCM.push(String.valueOf(Relationship.DECLINED), null);
                                    }
                                })
                        .setNegativeButton(getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).create().show();

            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Network.isConnected(ProfileActivity.this)) {
                    Toast.makeText(ProfileActivity.this, getString(R.string.error_internet), Toast.LENGTH_SHORT).show();
                    return;
                }
                final DatabaseReference relationRef = FirebaseDatabase.getInstance().getReference("relationship");
                String message = status == Relationship.PENDING ? getString(R.string.message_dialog_pending) :
                        getString(R.string.message_dialog_un_block);
                switch (status) {
                    case Relationship.ACCEPTED: //already being friend
                        //start activity to chat page
                        if (myRel == null || myRel.getId() == null) {
                            Log.e(TAG, "Relationship model is NULL");
                            return;
                        }
                        Intent intent = new Intent(ProfileActivity.this, SingleChatActivity.class);
                        intent.putExtra(SingleChatActivity.KEY_FRIEND_ID, uid);
                        intent.putExtra(SingleChatActivity.KEY_TITLE, displayName);
                        intent.putExtra(SingleChatActivity.KEY_RELATIONSHIP_ID, myRel.getId());
                        startActivity(intent);
                        break;
                    case Relationship.DECLINED:
                    case Relationship.BLOCKED:
                    case Relationship.PENDING: //Accept the invitation
                        new AlertDialog.Builder(ProfileActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (myRel == null || FirebaseAuth.getInstance().getCurrentUser() == null) {
                                            Toast.makeText(ProfileActivity.this, getString(R.string.general_error), Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        Relationship model = new Relationship();
                                        model.setFollower(myRel.getFollower());
                                        model.setFollowing(myRel.getFollowing());
                                        model.setStatus(Relationship.ACCEPTED);
                                        model.setActionUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        Map<String, Object> postValues = model.toMap();
                                        DatabaseReference ref = relationRef.child(myRel.getId());
                                        ref.updateChildren(postValues);
                                        pushFCM.setMessageId(myRel.getId());
                                        pushFCM.push(String.valueOf(Relationship.ACCEPTED), null);
                                    }
                                })
                                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).create().show();
                        break;
                    default: //send request add friend or send invitation
                        new AlertDialog.Builder(ProfileActivity.this)
                                .setTitle(getString(R.string.title_invitation))
                                .setMessage(getString(R.string.message_sent_invitation))
                                .setPositiveButton(getString(R.string.send),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Relationship model = new Relationship();
                                                model.setFollowing(myUid);
                                                model.setFollower(uid);
                                                model.setStatus(Relationship.PENDING);
                                                model.setActionUid(model.getFollowing());
                                                Map<String, Object> postValues = model.toMap();
                                                DatabaseReference ref = relationRef.push();
                                                ref.updateChildren(postValues);
                                                pushFCM.setMessageId(ref.getKey());
                                                pushFCM.push(String.valueOf(Relationship.PENDING), null);
                                            }
                                        })
                                .setNegativeButton(getString(R.string.cancel),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        }).create().show();


                }
            }
        });

        imageHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uid == null || mainImage == null || mainImageUrl == null) {
                    return;
                }
                Intent intent = new Intent(ProfileActivity.this, FullProfileImageActivity.class);
                intent.putExtra(FullProfileImageActivity.KEY_IMAGE, mainImage);
                intent.putExtra(FullProfileImageActivity.KEY_IMAGE_URL, mainImageUrl);
                intent.putExtra(FullProfileImageActivity.KEY_UID, uid);
                startActivity(intent);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(displayName);
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();
        galleryList.clear();
        adapter.notifyDataSetChanged();
        mAuth.addAuthStateListener(mAuthListener);
        appBarLayout.addOnOffsetChangedListener(appBarStateChangeListener);
        profileRef.addValueEventListener(profileListener);
        imagesRef.addChildEventListener(imageListener);
        tokenRef.addChildEventListener(tokenListener);
        connectedRef.addValueEventListener(onlineListener);
        lastOnlineRef.addValueEventListener(lastOnlineListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
        if (followerRef != null) {
            followerRef.removeEventListener(followerListener);
        }
        if (followingRef != null) {
            followingRef.removeEventListener(followingListener);
        }
        appBarLayout.removeOnOffsetChangedListener(appBarStateChangeListener);
        profileRef.removeEventListener(profileListener);
        imagesRef.removeEventListener(imageListener);
        tokenRef.removeEventListener(tokenListener);
        connectedRef.removeEventListener(onlineListener);
        lastOnlineRef.removeEventListener(lastOnlineListener);
        if (myProfileRef != null) {
            myProfileRef.removeEventListener(myProfileListener);
        }
        isFirstState = true;
    }

    private AppBarStateChangeListener appBarStateChangeListener = new AppBarStateChangeListener() {
        @Override
        public void onStateChanged(AppBarLayout appBarLayout, State state) {
//            Log.i(TAG,"State: "+state.name());
//            Log.i(TAG, "is Collapsed: "+(state == State.COLLAPSED ));
            imageHeader.setVisibility(state == State.COLLAPSED ? View.GONE : View.VISIBLE);
            viewOverlay.setVisibility(state == State.COLLAPSED ? View.GONE : View.VISIBLE);
        }
    };

    private ValueEventListener lastOnlineListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() == null) {
                layoutLastOnline.setVisibility(View.GONE);
                return;
            }
            if(uid.equalsIgnoreCase(myUid)){
                textLastOnline.setText("Online");
                layoutLastOnline.setVisibility(View.VISIBLE);
            }else{
                layoutLastOnline.setVisibility(dataSnapshot.getValue(Long.class) > 0 ? View.VISIBLE : View.GONE);
                textLastOnline.setText(MyDateUtil.getDateTime(ProfileActivity.this, dataSnapshot.getValue(Long.class)));
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
            if(model.getName()!=null)
            textDisplayName.setText(model.getName());

            if (model.getEmail() != null) {
                textEmail.setText(model.getEmail());
            }
            if (model.getBirthday() != null) {
                try {
                    String year = model.getBirthday().substring(0, 4);
                    Log.i(TAG, "year: " + year);
                    String month = model.getBirthday().substring(4, 6);
                    Log.i(TAG, "month: " + month);
                    String day = model.getBirthday().substring(6, 8);
                    int yearInt = Integer.parseInt(year);
                    Log.i(TAG, "day: " + day);
                    int monthInt = Integer.parseInt(month);
                    int dayInt = Integer.parseInt(day);
                    layoutBirthday.setVisibility(View.VISIBLE);
                    textBirthday.setText(displayDate(yearInt, monthInt, dayInt));
                } catch (Exception e) {
                    Log.e(TAG, "Error parse date: " + e.getMessage());
                }
            }
            if (model.getGender() != null) {
                layoutGender.setVisibility(View.VISIBLE);
                textGender.setText(model.getGender().equalsIgnoreCase("FEMALE") ? getString(R.string.female) :
                        getString(R.string.male));
            }
            if (model.getRelationshipStatus() != null) {
                layoutRelationShip.setVisibility(View.VISIBLE);
                textRelationship.setText(model.getRelationshipStatus());
            }
            if (model.getLookingFor() != null) {
                layoutLookingFor.setVisibility(View.VISIBLE);
                textLookingFor.setText(model.getLookingFor());
            }
            if (model.getInterestedIn() != null) {
                layoutInteresredIn.setVisibility(View.VISIBLE);
                textInteresrtedIn.setText(model.getInterestedIn());
            }
            if (model.getEmail() != null) {
                layoutEmail.setVisibility(View.GONE);
                textEmail.setText(model.getEmail());
            }
            for (DataSnapshot election : dataSnapshot.getChildren()) {
                if (election.getKey().equals("images")) {
                    String key = model.getImageUrl() == null ? "default" : model.getImageUrl();
                    DataSnapshot defaultImage = election.child(key);
                    if (defaultImage.exists()) {
                        mainImage = key;
                        appBarLayout.setExpanded(true);
                        UserImage userImage = defaultImage.getValue(UserImage.class);
                        mainImageUrl = userImage.getOriginalPic();
                        DraweeController controller = Fresco.newDraweeControllerBuilder()
                                .setLowResImageRequest(ImageRequest.fromUri(userImage.getThumbPic()))
                                .setImageRequest(ImageRequest.fromUri(userImage.getOriginalPic()))
                                .setOldController(imageHeader.getController())
                                .build();
                        imageHeader.setController(controller);
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
            if (user != null) {
                // User is signed in
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                myUid = user.getUid();
                pushFCM.setId(myUid);
                Log.i(TAG, "Friend uid: " + uid);
                fab.setVisibility(myUid.equals(uid) ? View.GONE : View.VISIBLE);
                myProfileRef = FirebaseDatabase.getInstance().getReference("users").child(myUid);
                followingRef = FirebaseDatabase.getInstance().getReference("relationship")
                        .orderByChild("following").equalTo(uid);
                followerRef = FirebaseDatabase.getInstance().getReference("relationship")
                        .orderByChild("follower").equalTo(uid);
                followingRef.addChildEventListener(followingListener);
                followerRef.addChildEventListener(followerListener);
                myProfileRef.addValueEventListener(myProfileListener);
            } else {
                Log.d(TAG, "onAuthStateChanged:signed_out");
                finish();
            }
        }
    };

    private ChildEventListener followingListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.i(TAG, "Following On Added-> " + dataSnapshot);
            Relationship model = dataSnapshot.getValue(Relationship.class);
            model.setId(dataSnapshot.getKey());
            relationships.add(model);
            followOnChangeState(true, model);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.w(TAG, "Following On Changed-> " + dataSnapshot);
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
            followOnChangeState(true, model);
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
            followOnChangeState(false, model);
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
            followOnChangeState(false, model);
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
    private ValueEventListener onlineListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            isConnected = dataSnapshot.getValue(Boolean.class);
            Log.i(TAG, "Is Connected: " + isConnected);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener imageListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            galleryView.setVisibility(View.VISIBLE);
            UserImage model = dataSnapshot.getValue(UserImage.class);
            model.setId(dataSnapshot.getKey());
            Log.i(TAG, "image id: " + model.getId());
            galleryList.add(0, model);
            adapter.notifyItemInserted(0);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            int position = 0;
            for (UserImage userImage : galleryList) {
                if (userImage.getId().equals(dataSnapshot.getKey())) {
                    galleryList.remove(position);
                    adapter.notifyItemRemoved(position);
                    break;
                }
                position++;
            }
            galleryView.setVisibility(galleryList.size() == 0 ? View.GONE : View.VISIBLE);
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void followOnChangeState(boolean isFollowing, Relationship model) {
        String compareId = isFollowing ? model.getFollower() : model.getFollowing();
        if (myUid != null && myUid.equals(compareId)) {
            Log.i(TAG, "status: " + model.getStatus() + " , key: " + model.getId());
            status = model.getStatus();
            myRel = model;
            switch (model.getStatus()) {
                case Relationship.ACCEPTED:
                    if (!isFirstState) {
                        Toast.makeText(this, getString(R.string.toast_now_friend), Toast.LENGTH_LONG).show();
                    }
                    layoutAttention.setVisibility(View.GONE);
                    fab.setVisibility(View.VISIBLE);
                    fab.setImageResource(R.drawable.ic_chat_white);
                    fabDecline.setVisibility(View.GONE);
                    menuBlock.setVisible(true);
                    break;
                case Relationship.PENDING:
                    boolean fromMe = myUid.equals(model.getFollowing());
                    Log.i(TAG, "Invitation from me: " + fromMe);
                    fabDecline.setVisibility(fromMe ? View.GONE : View.VISIBLE);
                    fab.setVisibility(fromMe ? View.GONE : View.VISIBLE);
                    fab.setImageResource(R.drawable.ic_done_white);
                    layoutAttention.setVisibility(View.VISIBLE);
                    String message = fromMe ? getString(R.string.alert_waiting_invitation) :
                            getString(R.string.alert_pending_invitation);
                    textAttention.setText(message);
                    textAttention.setTextColor(ContextCompat.getColor(ProfileActivity.this,
                            R.color.color_green_dark));
                    menuBlock.setVisible(false);
                    break;
                case Relationship.BLOCKED:
                    fabDecline.setVisibility(View.GONE);
                    fab.setImageResource(R.drawable.ic_done_white);
                    fab.setVisibility(myUid.equals(model.getActionUid()) ? View.VISIBLE : View.GONE);
                    layoutAttention.setVisibility(View.VISIBLE);
                    String messageBlocked = myUid.equals(model.getActionUid()) ? getString(R.string.alert_block_by_me) :
                            getString(R.string.alert_block_user);
                    textAttention.setText(messageBlocked);
                    textAttention.setTextColor(ContextCompat.getColor(ProfileActivity.this,
                            android.R.color.holo_red_dark));
                    menuBlock.setVisible(false);
                    break;
                case Relationship.DECLINED:
                    fabDecline.setVisibility(View.GONE);
                    fab.setImageResource(R.drawable.ic_done_white);
                    layoutAttention.setVisibility(View.VISIBLE);
                    fab.setVisibility(myUid.equals(model.getActionUid()) ? View.VISIBLE : View.GONE);
                    String messageDeclined = myUid.equals(model.getActionUid()) ? getString(R.string.alert_declined_by_me) :
                            getString(R.string.alert_declined_invitation);
                    textAttention.setText(messageDeclined);
                    textAttention.setTextColor(ContextCompat.getColor(ProfileActivity.this,
                            android.R.color.holo_red_dark));
                    menuBlock.setVisible(false);
                    break;
            }
        }
        isFirstState = false;
    }

    private ValueEventListener myProfileListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.i(TAG, "My Profile => " + dataSnapshot);
            User model = dataSnapshot.getValue(User.class);
            if (model == null) {
                return;
            }
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


//    private ValueEventListener tokenListener = new ValueEventListener() {
//        @Override
//        public void onDataChange(DataSnapshot dataSnapshot) {
//            String token = dataSnapshot.getValue(String.class);
//            pushFCM.setRecipients(token);
//            Log.i(TAG, "Get token: " + token);
//        }
//
//        @Override
//        public void onCancelled(DatabaseError databaseError) {
//
//        }
//    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        menuBlock = menu.findItem(R.id.action_block);
        editProfile = menu.findItem(R.id.action_edit);
        logout = menu.findItem(R.id.action_logout);
        if (uid.equalsIgnoreCase(myUid)) {
            editProfile.setVisible(true);
            logout.setVisible(true);
        } else {
            editProfile.setVisible(false);
            logout.setVisible(false);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_block:

                if (myRel == null || FirebaseAuth.getInstance().getCurrentUser() == null) {
                    Toast.makeText(ProfileActivity.this, getString(R.string.general_error), Toast.LENGTH_SHORT).show();
                    return true;
                }
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.title_block))
                        .setMessage(getString(R.string.message_block))
                        .setPositiveButton(getString(R.string.title_block),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        DatabaseReference relationRef = FirebaseDatabase.getInstance().getReference("relationship");
                                        Relationship model = new Relationship();
                                        model.setFollower(myRel.getFollower());
                                        model.setFollowing(myRel.getFollowing());
                                        model.setStatus(Relationship.BLOCKED);
                                        model.setActionUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        Map<String, Object> postValues = model.toMap();
                                        DatabaseReference ref = relationRef.child(myRel.getId());
                                        ref.updateChildren(postValues);
                                        pushFCM.setMessageId(myRel.getId());
                                        pushFCM.push(String.valueOf(Relationship.BLOCKED), null);
                                    }
                                })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create().show();

                break;

            case R.id.action_edit:
                Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class);
                startActivity(intent);
                break;

            case R.id.action_logout:
                FynderApplication.getInstance().sendActionevent(Constants.CT_LOGOUT);
                FynderApplication.getInstance().deleteAllSharePrefs(getApplicationContext());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private String displayDate(int year, int month, int day) {
        String month_array[] = getResources().getStringArray(R.array.month);
        return month_array[month] + " " + String.valueOf(day) + ", " + String.valueOf(year);
    }

    //Do not call onCreate on Parent Activity
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
