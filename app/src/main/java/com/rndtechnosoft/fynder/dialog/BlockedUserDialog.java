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
import com.rndtechnosoft.fynder.adapter.ParticipantAdapter;
import com.rndtechnosoft.fynder.adapter.UsersAdapter;
import com.rndtechnosoft.fynder.model.User;
import com.rndtechnosoft.fynder.utility.MyDateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ravi on 12/23/2016.
 */

public class BlockedUserDialog extends AlertDialog {
    private final String TAG = "ProfileDialog";
    private Activity activity;
    private DatabaseReference blockedRef;
    private ListView listView;
    private TextView textTitle;
    private UsersAdapter usersAdapter;
    private List<User> userList = new ArrayList<>();

    public BlockedUserDialog(final Activity activity) {
        super(activity);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            dismiss();
            return;
        }
        String uid = firebaseUser.getUid();
        blockedRef = FirebaseDatabase.getInstance().getReference("rooms").child("public").child(uid).child("blocked_user");
        this.activity = activity;
        LayoutInflater inflater = LayoutInflater.from(activity);

        View alertView = inflater.inflate(R.layout.dialog_users, null);
        Button buttonClose = (Button) alertView.findViewById(R.id.close_button);
        textTitle = (TextView) alertView.findViewById(R.id.text_status_user);

        RecyclerView recyclerView = (RecyclerView) alertView.findViewById(R.id.list_user);
//        textStatus = (TextView) findViewById(R.id.text_status);
        usersAdapter = new UsersAdapter(activity,userList,false,false,new UsersAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final User item) {
                if (item != null && item.getId() != null) {
                    String displayName = item.getName() == null ? "" : item.getName();
                    String title = String.format(activity.getString(R.string.title_unblock), displayName);
                    new AlertDialog.Builder(activity)
                            .setTitle(title)
                            .setMessage(activity.getString(R.string.message_unblock))
                            .setPositiveButton(activity.getString(R.string.yes), new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    blockedRef.child(item.getId()).removeValue();
                                }
                            })
                            .setNegativeButton(activity.getString(R.string.no), new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).create().show();
                }
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(usersAdapter);

        String title = String.format(activity.getString(R.string.title_dialog_block_user), 0);
        textTitle.setText(title);
        blockedRef.addChildEventListener(blockedListener);

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setView(alertView);

    }

    private ChildEventListener blockedListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            listView.setVisibility(View.VISIBLE);
            User user = new User();
            user.setId(dataSnapshot.getKey());
            String desc = String.format(activity.getString(R.string.time_blocked)
                    , MyDateUtil.getDateTime(activity, dataSnapshot.getValue(Long.class)));
            user.setDescription(desc);
            userList.add(user);
            user.attachProfileRef(usersAdapter);
            String title = String.format(activity.getString(R.string.title_dialog_block_user), userList.size());
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
            String title = String.format(activity.getString(R.string.title_dialog_block_user), userList.size());
            textTitle.setText(title);
            listView.setVisibility(userList.size() == 0 ? View.GONE : View.VISIBLE);
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
        if (blockedRef != null) {
            blockedRef.removeEventListener(blockedListener);
        }
    }
}
