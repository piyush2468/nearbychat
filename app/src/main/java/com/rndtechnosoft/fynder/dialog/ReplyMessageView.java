package com.rndtechnosoft.fynder.dialog;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.model.Chat;
import com.rndtechnosoft.fynder.model.User;
import com.rndtechnosoft.fynder.model.UserImage;

/**
 * Created by Ravi on 3/25/2017.
 */

public class ReplyMessageView extends CardView {
    //    private final String TAG = "ReplyMessage";
    private TextView textDisplayName;
    private SimpleDraweeView imageProfile;

    public ReplyMessageView(Context context, Chat chat, int resColor,boolean itsme) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View replyView;
        if(itsme) {
            replyView = inflater.inflate(R.layout.reply_message_right, null);
        }else {
            replyView = inflater.inflate(R.layout.reply_message_left, null);
        }
        String displayName = chat.getDisplayName();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && firebaseUser.getUid().equals(chat.getFrom())) {
            displayName = context.getString(R.string.you);
            resColor = ContextCompat.getColor(context, R.color.color_right_chat_background);
        }
        imageProfile = (SimpleDraweeView) replyView.findViewById(R.id.left_profile_pic);
        SimpleDraweeView rightImage = (SimpleDraweeView) replyView.findViewById(R.id.right_image);
        RoundingParams circle = RoundingParams.asCircle()
                .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY)
                .setBorder(resColor, 2);
        RoundingParams rounded = RoundingParams.fromCornersRadius(5)
                .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY);
        imageProfile.getHierarchy().setRoundingParams(circle);
        imageProfile.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
        imageProfile.setImageURI(chat.getProfilePic());
        rightImage.getHierarchy().setRoundingParams(rounded);
        rightImage.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
        textDisplayName = (TextView) replyView.findViewById(R.id.text_display_name);
        TextView textMessage = (TextView) replyView.findViewById(R.id.text_message);
        ImageView iconMessage = (ImageView) replyView.findViewById(R.id.reply_icon_message);
        imageProfile.setVisibility(chat.getProfilePic() == null ? GONE : VISIBLE);

        textDisplayName.setTextColor(resColor);
        textDisplayName.setText(displayName);
        textMessage.setText(chat.getBody());


        if (chat.getType() == Chat.TYPE_TEXT) {
            iconMessage.setVisibility(GONE);
            rightImage.setVisibility(GONE);
        } else if (chat.getType() == Chat.TYPE_IMAGE) {
            iconMessage.setImageResource(R.drawable.ic_camera_chat);
            textMessage.setText(context.getString(R.string.photo));
            rightImage.setVisibility(VISIBLE);
            rightImage.setImageURI(chat.getUrlThumbnail());
        } else if (chat.getType() == Chat.TYPE_AUDIO) {
            iconMessage.setImageResource(R.drawable.ic_mic);
            rightImage.setVisibility(GONE);
            String message = context.getString(R.string.reply_audio);
            if (chat.getDuration() != null) {
                message = String.format(context.getString(R.string.reply_audio_with_duration), chat.getDuration());
            }
            textMessage.setText(message);
        } else if (chat.getType() == Chat.TYPE_VIDEO) {
            iconMessage.setImageResource(android.R.drawable.presence_video_online);
            rightImage.setVisibility(VISIBLE);
            rightImage.setImageURI(chat.getUrlThumbnail());
            String message = context.getString(R.string.reply_video);
            if (chat.getDuration() != null) {
                message = String.format(context.getString(R.string.reply_video_with_duration), chat.getDuration());
            }
            textMessage.setText(message);
        }
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            getBackground().setAlpha(0);
        } else {
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        }
        if(chat.getFrom() != null) {
            final DatabaseReference profileRef = FirebaseDatabase.getInstance().getReference("users").child(chat.getFrom());
            profileRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User model = dataSnapshot.getValue(User.class);
                    if(model == null){
                        return;
                    }
                    textDisplayName.setText(model.getName());
                    for (DataSnapshot election : dataSnapshot.getChildren()) {
                        if (election.getKey().equals("images")) {
                            String key = model.getImageUrl() == null ? "default" : model.getImageUrl();
                            DataSnapshot defaultImage = election.child(key);
                            if (defaultImage.exists()) {
                                UserImage userImage = defaultImage.getValue(UserImage.class);
                                imageProfile.setVisibility(VISIBLE);
                                imageProfile.setImageURI(userImage.getThumbPic());
                                break;
                            }
                        }
                    }
                    profileRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        this.addView(replyView);
    }
}
