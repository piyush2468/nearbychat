package com.rndtechnosoft.fynder.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.model.RoomImage;
import com.rndtechnosoft.fynder.model.User;
import com.rndtechnosoft.fynder.model.UserImage;

/**
 * Created by Ravi on 3/16/2017.
 */

public class RoomDisplayDialog extends AlertDialog {
    private final String TAG = "RoomDialog";
    private String roomId;
    private TextView textRoomName;
    private TextView textRoomDescription;
    private SimpleDraweeView imageRoom;
    private DatabaseReference profileRef;
    private DatabaseReference roomNameRef;
    private DatabaseReference roomDescRef;
    private DatabaseReference roomImageRef;

    public RoomDisplayDialog(Context context, String roomId){
        super(context);
//        this.context = context;
        if(roomId == null){
            dismiss();
            return;
        }
        this.roomId = roomId;
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child("public").child(roomId);
        roomNameRef = roomRef.child("name");
        roomDescRef = roomRef.child("description");
        roomImageRef = roomRef.child("image");
        LayoutInflater inflater = LayoutInflater.from(context);

        View alertView = inflater.inflate(R.layout.dialog_room_display, null);
        textRoomName = (TextView) alertView.findViewById(R.id.text_room_name);
        textRoomDescription = (TextView) alertView.findViewById(R.id.text_room_description);
        imageRoom = (SimpleDraweeView) alertView.findViewById(R.id.image_room);
        RoundingParams rounded = RoundingParams.fromCornersRadius(10)
                .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY);
        imageRoom.getHierarchy().setRoundingParams(rounded);
        imageRoom.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.FIT_XY);

        roomNameRef.addValueEventListener(roomNameListener);
        roomDescRef.addValueEventListener(roomDescListener);
        roomImageRef.addValueEventListener(roomImageListener);

        alertView.findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setView(alertView);
    }

    private ValueEventListener roomNameListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.getValue() != null) {
                textRoomName.setText(dataSnapshot.getValue(String.class));
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ValueEventListener roomDescListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.getValue() != null) {
                textRoomDescription.setText(dataSnapshot.getValue(String.class));
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
                        UserImage userImage = defaultImage.getValue(UserImage.class);
                        String lowResUrl = userImage.getThumbPic();
                        String highResUrl = userImage.getOriginalPic();
                        if(lowResUrl != null && highResUrl != null) {
                            DraweeController controller = Fresco.newDraweeControllerBuilder()
                                    .setLowResImageRequest(ImageRequest.fromUri(lowResUrl))
                                    .setImageRequest(ImageRequest.fromUri(highResUrl))
                                    .setOldController(imageRoom.getController())
                                    .build();
                            imageRoom.setController(controller);
                        }
                        break;
                    }
                }
            }
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
                String lowResUrl = roomImage.getThumbPic();
                String highResUrl = roomImage.getOriginalPic();
                if(lowResUrl != null && highResUrl != null) {
                    DraweeController controller = Fresco.newDraweeControllerBuilder()
                            .setLowResImageRequest(ImageRequest.fromUri(lowResUrl))
                            .setImageRequest(ImageRequest.fromUri(highResUrl))
                            .setOldController(imageRoom.getController())
                            .build();
                    imageRoom.setController(controller);
                }
                if(profileRef != null){
                    profileRef.removeEventListener(profileListener);
                }
            }else{
                profileRef = FirebaseDatabase.getInstance().getReference("users").child(roomId);
                profileRef.addValueEventListener(profileListener);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.i(TAG, "on detached from window");
        if(roomNameRef != null){
            roomNameRef.removeEventListener(roomNameListener);
        }
        if(roomDescRef != null){
            roomDescRef.removeEventListener(roomDescListener);
        }
        if(roomImageRef != null){
            roomImageRef.removeEventListener(roomImageListener);
        }
        if(profileRef != null){
            profileRef.removeEventListener(profileListener);
        }
    }

}
