package com.rndtechnosoft.fynder.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.activity.FindFriendsActivity;
import com.rndtechnosoft.fynder.activity.ProfileActivity;
import com.rndtechnosoft.fynder.adapter.ItemAdapter;
import com.rndtechnosoft.fynder.adapter.ParticipantAdapter;
import com.rndtechnosoft.fynder.adapter.UsersAdapter;
import com.rndtechnosoft.fynder.model.Item;
import com.rndtechnosoft.fynder.model.User;
import com.rndtechnosoft.fynder.utility.MyDateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ravi on 2/14/2017.
 */

public class SubscribersDialog extends AlertDialog {
    private final String TAG = "SubscriberDialog";
    private RecyclerView recyclerView;
    private Activity activity;
    private DatabaseReference subscribersRef;
    private TextView textTitle;
    private UsersAdapter usersAdapter;
    private List<User> userList = new ArrayList<>();

    public SubscribersDialog(Activity activity, final String roomId) {
        super(activity);
        this.activity = activity;
        if (roomId == null) {
            dismiss();
            return;
        }
        subscribersRef = FirebaseDatabase.getInstance().getReference("rooms").child("public")
                .child(roomId).child("subscribers");
        LayoutInflater inflater = LayoutInflater.from(activity);

        View alertView = inflater.inflate(R.layout.dialog_users, null);
        Button buttonClose = (Button) alertView.findViewById(R.id.close_button);
        textTitle = (TextView) alertView.findViewById(R.id.text_status_user);

        recyclerView = (RecyclerView) alertView.findViewById(R.id.list_user);
//        textStatus = (TextView) findViewById(R.id.text_status);
        usersAdapter = new UsersAdapter(activity,userList,false,false,new UsersAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final User item) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser == null) {
                    return;
                }
                String uid = firebaseUser.getUid();
                boolean isOwner = uid.equals(roomId);
                final Activity activity = SubscribersDialog.this.activity;
                if (!isOwner) {
                    Intent intent = new Intent(activity, ProfileActivity.class);
                    intent.putExtra(ProfileActivity.KEY_UID, item.getId());
                    intent.putExtra(ProfileActivity.KEY_DISPLAY_NAME, item.getName());
                    activity.startActivity(intent);
                } else {
                    final Item[] items = {
                            new Item(activity.getString(R.string.option_view_user), android.R.drawable.ic_menu_view),
                            new Item(activity.getString(R.string.option_un_subscribe), android.R.drawable.ic_menu_close_clear_cancel),
                    };
                    ItemAdapter adapter = new ItemAdapter(activity, items);
                    new AlertDialog.Builder(activity)
                            .setTitle(activity.getString(R.string.subscriber_option))
                            .setAdapter(adapter, new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (i == 0) { //view user
                                        Intent intent = new Intent(activity, ProfileActivity.class);
                                        intent.putExtra(ProfileActivity.KEY_UID, item.getId());
                                        intent.putExtra(ProfileActivity.KEY_DISPLAY_NAME, item.getName());
                                        activity.startActivity(intent);
                                    } else { //unsubscribe
                                        new AlertDialog.Builder(activity)
                                                .setTitle(activity.getString(R.string.unsubscriber_title))
                                                .setMessage(activity.getString(R.string.unsubscriber_message))
                                                .setPositiveButton(activity.getString(R.string.unsubscribe),
                                                        new OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                if(subscribersRef == null){
                                                                    return;
                                                                }
                                                                DatabaseReference ref = subscribersRef.child(item.getId());
                                                                ref.removeValue();
                                                            }
                                                        })
                                                .setNegativeButton(activity.getString(R.string.cancel),
                                                        new OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                dialogInterface.dismiss();
                                                            }
                                                        }).create().show();
                                    }
                                }
                            }).create().show();
                }
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(usersAdapter);
        String title = String.format(activity.getString(R.string.title_dialog_subscriber_user), 0);
        textTitle.setText(title);
        subscribersRef.addChildEventListener(subscriberListener);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setView(alertView);
    }

    private ChildEventListener subscriberListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            recyclerView.setVisibility(View.VISIBLE);
            User user = new User();
            user.setId(dataSnapshot.getKey());
            String desc = String.format(activity.getString(R.string.time_subscriber)
                    , MyDateUtil.getDateTime(activity, dataSnapshot.getValue(Long.class)));
            user.setDescription(desc);
            userList.add(user);
            user.attachProfileRef(usersAdapter);
            String title = String.format(activity.getString(R.string.title_dialog_subscriber_user), userList.size());
            textTitle.setText(title);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            int position = 0;
            for (User model : userList) {
                if (model.getId().equals(dataSnapshot.getKey())) {
                    userList.remove(position);
                    usersAdapter.notifyDataSetChanged();
                    break;
                }
                position++;
            }
            String title = String.format(activity.getString(R.string.title_dialog_subscriber_user), userList.size());
            textTitle.setText(title);
            recyclerView.setVisibility(userList.size() == 0 ? View.GONE : View.VISIBLE);
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.i(TAG, "on detached from window");
        for(User model:userList){
            model.detachProfileRef();
        }
        userList.clear();
        if (usersAdapter != null) {
            usersAdapter.notifyDataSetChanged();
        }
        if (subscribersRef != null) {
            subscribersRef.removeEventListener(subscriberListener);
        }
    }
}
