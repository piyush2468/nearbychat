package com.rndtechnosoft.fynder.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.rndtechnosoft.fynder.adapter.ParticipantAdapter;
import com.rndtechnosoft.fynder.adapter.UsersAdapter;
import com.rndtechnosoft.fynder.model.Chat;
import com.rndtechnosoft.fynder.model.Relationship;
import com.rndtechnosoft.fynder.model.User;
import com.rndtechnosoft.fynder.model.UserImage;
import com.rndtechnosoft.fynder.model.event.NotificationEvent;
import com.rndtechnosoft.fynder.utility.MyDateUtil;
import com.rndtechnosoft.fynder.utility.MyImageUtil;
import com.rndtechnosoft.fynder.utility.PushFCM;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SendImageActivity extends AppCompatActivity {
    private final String TAG = "SendImage";
    private boolean isGlobalRoomSelected = false;
//    private int countSelected = 0;
    private ImageView imageTickGlobalRoom;
    private FloatingActionButton fab;
    private Query followerRef;
    private Query followingRef;
    private DatabaseReference myProfileRef;
    private StorageReference storageRef;
    private TextView textFriendList;
    private UsersAdapter usersAdapter;
    private List<User> contactList = new ArrayList<>();
    private Uri imageUri;
    private UploadTask uploadTask;
    private ProgressDialog myProgressDialog;
    private PushFCM pushFCM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if(Intent.ACTION_SEND.equals(action) && type != null && type.startsWith("image/")){
            imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        }

        myProgressDialog = new ProgressDialog(this);
        myProgressDialog.setMessage(getString(R.string.uploading_image));
        myProgressDialog.setCancelable(false);
        imageTickGlobalRoom = (ImageView) findViewById(R.id.image_tick_global_room);
        textFriendList = (TextView) findViewById(R.id.text_friend_list);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list_user);
//        textStatus = (TextView) findViewById(R.id.text_status);
        usersAdapter = new UsersAdapter(SendImageActivity.this,contactList,false,false,new UsersAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User item) {
                item.setSelected(!item.isSelected());
                usersAdapter.notifyDataSetChanged();
                int addGlobalRoom = isGlobalRoomSelected ? 1 : 0;
                int countUserSelected = 0;
                for(User user:contactList){
                    if(user.isSelected()){
                        countUserSelected++;
                    }
                }
                countUserSelected += addGlobalRoom;
                fab.setVisibility(countUserSelected > 0 ? View.VISIBLE : View.GONE);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(SendImageActivity.this));
        recyclerView.setAdapter(usersAdapter);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        pushFCM = new PushFCM(this, NotificationEvent.TYPE_SINGLE_CHAT);

        if(firebaseUser != null){
            String uid = firebaseUser.getUid();
            pushFCM.setUid(uid);
            Log.i(TAG,"This uid: "+uid);
            followerRef = FirebaseDatabase.getInstance().getReference("relationship")
                    .orderByChild("follower").equalTo(uid);
            followingRef = FirebaseDatabase.getInstance().getReference("relationship")
                    .orderByChild("following").equalTo(uid);
            myProfileRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        }else{
            finish();
            Toast.makeText(this, getString(R.string.login_required), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
        }
        storageRef = FirebaseStorage.getInstance().getReference();
        fab = (FloatingActionButton) findViewById(R.id.fab_accept);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if(imageUri == null || firebaseUser == null) {
                    Snackbar.make(view, getString(R.string.error_send_image), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }
//                for(User user:contactList){
//                    Log.i(TAG,"User: "+user.getName()+" , token: "+user.getToken());
//                }
                myProgressDialog.show();
                String uid = firebaseUser.getUid();
                StorageReference imageRef = storageRef.child(uid+"/chat/images/thumbnail/"+System.currentTimeMillis()+".jpg");
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    Bitmap bitmapThumbnail = MyImageUtil.scaleBitmap(bitmap, 70, 70);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmapThumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();
                    uploadTask = imageRef.putBytes(data);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Failed to upload thumb: " + e.getMessage());
                        }
                    }).addOnSuccessListener(onSuccessUploadImage);
                    Log.i(TAG,"Bitmap is null: "+(bitmap == null));
                } catch (IOException e) {
                    Log.e(TAG,"Error bitmap: "+e.getMessage());
                    Snackbar.make(view, getString(R.string.error_send_image), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
//                    return;
                }

            }
        });

        findViewById(R.id.global_room_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isGlobalRoomSelected = !isGlobalRoomSelected;
                imageTickGlobalRoom.setVisibility(isGlobalRoomSelected ? View.VISIBLE : View.GONE);
                int addGlobalRoom = isGlobalRoomSelected ? 1 : 0;
                int countUserSelected = 0;
                for(User user:contactList){
                    if(user.isSelected()){
                        countUserSelected++;
                    }
                }
                countUserSelected += addGlobalRoom;
                Log.i(TAG, "Global room -> Count Selected: " + countUserSelected);
                fab.setVisibility(countUserSelected > 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG,"On Start");
        contactList.clear();
        usersAdapter.notifyDataSetChanged();
        if(followerRef != null){
            followerRef.addChildEventListener(followerListener);
        }
        if(followingRef != null){
            followingRef.addChildEventListener(followingListener);
        }
        if(myProfileRef != null){
            myProfileRef.addValueEventListener(myProfileListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(followerRef != null){
            followerRef.removeEventListener(followerListener);
        }
        if(followingRef != null){
            followingRef.removeEventListener(followingListener);
        }
        if(myProfileRef != null){
            myProfileRef.removeEventListener(myProfileListener);
        }
        if(uploadTask != null){
            uploadTask.removeOnSuccessListener(onSuccessUploadImage);
        }
        for(User user:contactList){
            user.detachProfileRef();
            user.detachTokenRef();
        }
        myProgressDialog.dismiss();
    }

    private ChildEventListener followerListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.i(TAG,"Follower => On Added: "+dataSnapshot);
            Relationship model = dataSnapshot.getValue(Relationship.class);
            model.setId(dataSnapshot.getKey());
            if(model.getStatus() == Relationship.ACCEPTED){
                textFriendList.setVisibility(View.VISIBLE);
                User user = new User();
                user.setId(model.getFollowing());
                user.setRelationshipId(model.getId());
                String desc = String.format(getString(R.string.time_friend)
                        , MyDateUtil.getDateTime(SendImageActivity.this, model.getTimestamp()));
                user.setDescription(desc);
                contactList.add(user);
                user.attachProfileRef(usersAdapter);
                user.attachTokenRef();
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
            Log.i(TAG,"Following => On Added: "+dataSnapshot);
            Relationship model = dataSnapshot.getValue(Relationship.class);
            model.setId(dataSnapshot.getKey());
            if(model.getStatus() == Relationship.ACCEPTED){
                textFriendList.setVisibility(View.VISIBLE);
                User user = new User();
                user.setId(model.getFollower());
                user.setRelationshipId(model.getId());
                String desc = String.format(getString(R.string.time_friend)
                        , MyDateUtil.getDateTime(SendImageActivity.this, model.getTimestamp()));
                user.setDescription(desc);
                contactList.add(user);
                user.attachProfileRef(usersAdapter);
                user.attachTokenRef();
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

    private OnSuccessListener<UploadTask.TaskSnapshot> onSuccessUploadImage = new OnSuccessListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            @SuppressWarnings("VisibleForTests")
            final Uri thumbUrl = taskSnapshot.getDownloadUrl();
            Log.i(TAG, "Success to Upload thumbnail, url: " + thumbUrl);
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                return;
            }
            final String uid = firebaseUser.getUid();
            StorageReference imageRef = storageRef.child(uid + "/chat/images/" + System.currentTimeMillis() + ".jpg");
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                Bitmap bitmapHigh = MyImageUtil.scaleBitmap(bitmap, 300, 300);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmapHigh.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                UploadTask uploadHighImage = imageRef.putBytes(data);
                uploadHighImage.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error upload High image: " + e.getMessage());
                        Toast.makeText(SendImageActivity.this, getString(R.string.error_send_image),Toast.LENGTH_SHORT).show();
                        myProgressDialog.dismiss();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests")
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Log.i(TAG, "Success to Upload, url: " + downloadUrl);
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        if(firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            if (isGlobalRoomSelected) {
                                Chat chat = new Chat();
                                chat.setType(Chat.TYPE_IMAGE);
                                chat.setFrom(uid);
                                chat.setUrlThumbnail(thumbUrl.toString());
                                chat.setUrl(downloadUrl.toString());
                                chat.setBody(getString(R.string.send_an_image));
                                DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("rooms").child("global").child("message");
                                DatabaseReference updateRef = chatRef.push();
                                Map<String, Object> postValues = chat.toMap();
                                updateRef.updateChildren(postValues);
                            }
                            for(User user: contactList){
                                if(user.isSelected()){
                                    Chat chat = new Chat();
                                    chat.setType(Chat.TYPE_IMAGE);
                                    chat.setFrom(uid);
                                    chat.setUrlThumbnail(thumbUrl.toString());
                                    chat.setUrl(downloadUrl.toString());
                                    chat.setBody(getString(R.string.send_an_image));
                                    chat.setTo(user.getId());
                                    chat.setStatus(Chat.DELIVERED);
                                    DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats").child(user.getRelationshipId());
                                    DatabaseReference updateRef = chatRef.push();
                                    String key = updateRef.getKey();
                                    Map<String, Object> postValues = chat.toMap();
                                    updateRef.updateChildren(postValues);
                                    pushFCM.setRecipients(user.getToken());
                                    pushFCM.setId(user.getRelationshipId());
                                    pushFCM.setMessageId(key);
                                    pushFCM.setTitle(user.getName());
                                    pushFCM.push(chat.getBody(),chatRef.child(key).child("posted"));
                                }
                            }
                            finish();
                            Intent intent = new Intent(SendImageActivity.this, NearbyHomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };

    private ValueEventListener myProfileListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            User model = dataSnapshot.getValue(User.class);
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
}
