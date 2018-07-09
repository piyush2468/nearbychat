package com.rndtechnosoft.fynder.dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.activity.FindFriendsActivity;
import com.rndtechnosoft.fynder.activity.ProfileActivity;
import com.rndtechnosoft.fynder.activity.RoomActivity;
import com.rndtechnosoft.fynder.adapter.ParticipantAdapter;
import com.rndtechnosoft.fynder.adapter.UsersAdapter;
import com.rndtechnosoft.fynder.fragment.RoomFragment;
import com.rndtechnosoft.fynder.model.Room;
import com.rndtechnosoft.fynder.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ravi on 2/19/2017.
 */

public class RoomInvitationsDialog extends AlertDialog {
    private final String TAG = "RoomInvitation";
    private Activity activity;
    private TextView textTitle;
    private UsersAdapter usersAdapter;
    private List<Room> roomList;
    private List<User> userList = new ArrayList<>();

    public RoomInvitationsDialog(final Activity activity, List<Room> roomList, final List<Room> joinedRoomList){
        super(activity);
        this.activity = activity;
        this.roomList = roomList;
        if (roomList == null || joinedRoomList == null) {
            dismiss();
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(activity);
        View alertView = inflater.inflate(R.layout.dialog_users, null);
        Button buttonClose = (Button) alertView.findViewById(R.id.close_button);
        textTitle = (TextView) alertView.findViewById(R.id.text_status_user);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list_user);
        usersAdapter = new UsersAdapter(activity,userList,false,false,new UsersAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User item) {
                boolean isJoined = false;
                for (Room checkJoined : joinedRoomList) {
                    if (checkJoined.getId().equals(item.getId())) {
                        isJoined = true;
                        break;
                    }
                }
                Intent intent = new Intent(activity, RoomActivity.class);
                intent.putExtra(RoomActivity.KEY_ID, item.getId());
                intent.putExtra(RoomActivity.KEY_ROOM_NAME, item.getName());
                intent.putExtra(RoomActivity.KEY_IS_JOINED, isJoined);
                activity.startActivityForResult(intent, RoomFragment.FLAG_JOIN_ROOM_REQUEST);
                dismiss();

            }
        });

        if(activity!=null && recyclerView!=null){
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            recyclerView.setAdapter(usersAdapter);
        }

        String title = String.format(activity.getString(R.string.title_dialog_room_invitations), 0);
        textTitle.setText(title);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        init();
        setView(alertView);
    }

    private void init() {
        for(Room model:roomList){
            User user = new User();
            user.setId(model.getId());
            user.setName(model.getName());
            user.setImageUrl(model.getImageUrl());
            if(model.getInviterName() != null){
                String desc = String.format(activity.getString(R.string.invited_by),model.getInviterName());
                user.setDescription(desc);
            }
            userList.add(user);
            String title = String.format(activity.getString(R.string.title_dialog_room_invitations), userList.size());
            textTitle.setText(title);
        }
    }
}
