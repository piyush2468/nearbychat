package com.rndtechnosoft.fynder.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.activity.ProfileActivity;
import com.rndtechnosoft.fynder.activity.RoomActivity;
import com.rndtechnosoft.fynder.activity.SingleChatActivity;
import com.rndtechnosoft.fynder.model.Item;
import com.rndtechnosoft.fynder.model.Relationship;
import com.rndtechnosoft.fynder.model.UserImage;
import com.rndtechnosoft.fynder.model.event.NotificationEvent;
import com.rndtechnosoft.fynder.utility.PushFCM;

import java.util.Map;

/**
 * Created by Ravi on 12/19/2016.
 */

public class ProfileDialog extends AlertDialog {
    private final String TAG = "ProfileDialog";
    private Activity activity;

    public ProfileDialog(final Activity activity, final UserImage item,final Item model
            ,final DatabaseReference blockRef, final String myImageUrl, final String myName) {
        super(activity);
        this.activity = activity;
        LayoutInflater inflater = LayoutInflater.from(activity);
        View alertView = inflater.inflate(R.layout.dialog_profile, null);
        TextView textDisplayName = (TextView) alertView.findViewById(R.id.text_display_name);
        TextView textDescription = (TextView) alertView.findViewById(R.id.text_description);
        SimpleDraweeView imageProfile = (SimpleDraweeView) alertView.findViewById(R.id.profile_image);
        RoundingParams circle = RoundingParams.asCircle()
                .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY);
        imageProfile.getHierarchy().setRoundingParams(circle);
        Button buttonClose = (Button) alertView.findViewById(R.id.close_button);
        ImageView buttonInvite = (ImageView) alertView.findViewById(R.id.button_invite);
        ImageView buttonHome = (ImageView) alertView.findViewById(R.id.button_home);
        ImageView buttonChat = (ImageView) alertView.findViewById(R.id.button_chat);
        ImageView buttonBlock = (ImageView) alertView.findViewById(R.id.button_block);
        View viewInvite = alertView.findViewById(R.id.button_invite_layout);
        View viewChat = alertView.findViewById(R.id.button_chat_layout);
        View viewBlock = alertView.findViewById(R.id.button_block_layout);
//        View viewHome = alertView.findViewById(R.id.button_home_layout);
        int status = model.getIndex();
        textDisplayName.setText(item.getName());
        String description = getTextStatus(status);
        String desc = !description.isEmpty() ? String.format("(%s)", description) : "";
        textDescription.setText(desc);
        if (item.getThumbPic() != null) {
            Uri lowResUri = Uri.parse(item.getThumbPic());
            Uri highResUri = Uri.parse(item.getOriginalPic());
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setLowResImageRequest(ImageRequest.fromUri(lowResUri))
                    .setImageRequest(ImageRequest.fromUri(highResUri))
                    .setOldController(imageProfile.getController())
                    .build();
            imageProfile.setController(controller);
//            imageProfile.setImageURI(item.getThumbPic());
        } else {
            Uri uriDefault = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                    .path(String.valueOf(R.drawable.ic_profile_default))
                    .build();
            imageProfile.setImageURI(uriDefault);
        }

        //Visibility of all buttons
        viewBlock.setVisibility(blockRef != null ? View.VISIBLE : View.GONE);
        if (status == RoomActivity.STATUS_MY_SELF) {
            viewInvite.setVisibility(View.GONE);
            viewChat.setVisibility(View.GONE);
            viewBlock.setVisibility(View.GONE);
        } else if (status == RoomActivity.STATUS_FRIEND) {
            viewInvite.setVisibility(View.GONE);
        } else if(status == RoomActivity.STATUS_READY_TO_INVITE){
            viewChat.setVisibility(View.GONE);
        } else {
//        if(status == RoomActivity.STATUS_PENDING_INVITE ||
//                status == RoomActivity.STATUS_RECEIVED_INVITATION ||
//                status == RoomActivity.STATUS_BLOCK ||
//                status == RoomActivity.STATUS_GOT_BLOCKED ||
//                status == RoomActivity.STATUS_DECLINE ||
//                status == RoomActivity.STATUS_GOT_DECLINED){
            viewInvite.setVisibility(View.GONE);
            viewChat.setVisibility(View.GONE);
        }
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, ProfileActivity.class);
                intent.putExtra(ProfileActivity.KEY_UID, item.getId());
                intent.putExtra(ProfileActivity.KEY_DISPLAY_NAME, item.getName());
                activity.startActivity(intent);
                dismiss();
            }
        });
        buttonChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, SingleChatActivity.class);
                intent.putExtra(SingleChatActivity.KEY_FRIEND_ID, item.getId());
                intent.putExtra(SingleChatActivity.KEY_TITLE, item.getName());
                intent.putExtra(SingleChatActivity.KEY_RELATIONSHIP_ID, model.getText());
                activity.startActivity(intent);
                dismiss();
            }
        });

        buttonInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(activity)
                        .setTitle(activity.getString(R.string.title_invitation))
                        .setMessage(activity.getString(R.string.message_sent_invitation))
                        .setPositiveButton(activity.getString(R.string.send),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                        if(firebaseUser == null){
                                            return;
                                        }
                                        String myUid = firebaseUser.getUid();
                                        DatabaseReference relationRef = FirebaseDatabase.getInstance().getReference("relationship");
                                        Relationship model = new Relationship();
                                        model.setFollowing(myUid);
                                        model.setFollower(item.getId());
                                        model.setStatus(Relationship.PENDING);
                                        model.setActionUid(model.getFollowing());
                                        Map<String, Object> postValues = model.toMap();
                                        DatabaseReference ref = relationRef.push();
                                        ref.updateChildren(postValues);
                                        PushFCM pushFCM = new PushFCM(activity, NotificationEvent.TYPE_PROFILE, myName);
                                        pushFCM.setId(myUid);
                                        pushFCM.setImageUrl(myImageUrl);
                                        pushFCM.setRecipients(item.getToken());
                                        pushFCM.setMessageId(ref.getKey());
                                        pushFCM.push(String.valueOf(Relationship.PENDING), null);
                                        dismiss();
                                    }
                                })
                        .setNegativeButton(activity.getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).create().show();
            }
        });
        buttonBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//                if(firebaseUser == null){
//                    return;
//                }
//                String myUid = firebaseUser.getUid();
//                if(!myUid.equals(item.getId())){
//                    return;
//                }
                if(blockRef == null){
                    return;
                }
                new AlertDialog.Builder(activity)
                        .setTitle(activity.getString(R.string.title_block_user_from_room))
                        .setMessage(activity.getString(R.string.msg_block_user_from_room))
                        .setPositiveButton(activity.getString(R.string.block), new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                blockRef.setValue(ServerValue.TIMESTAMP);
                                dismiss();
                            }
                        })
                        .setNegativeButton(activity.getString(R.string.cancel), new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create().show();
            }
        });

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setView(alertView);
    }

    private String getTextStatus(int statusFriend) {
        switch (statusFriend) {
            case RoomActivity.STATUS_BLOCK:
                return activity.getString(R.string.alert_block_by_me);
            case RoomActivity.STATUS_GOT_BLOCKED:
                return activity.getString(R.string.text_blocked);
            case RoomActivity.STATUS_DECLINE:
                return activity.getString(R.string.alert_declined_by_me);
            case RoomActivity.STATUS_GOT_DECLINED:
                return activity.getString(R.string.alert_declined_invitation);
            case RoomActivity.STATUS_READY_TO_INVITE:
                return activity.getString(R.string.ready_to_invite);
            case RoomActivity.STATUS_PENDING_INVITE:
                return activity.getString(R.string.alert_pending_invitation);
            case RoomActivity.STATUS_RECEIVED_INVITATION:
                return activity.getString(R.string.alert_waiting_invitation);
            case RoomActivity.STATUS_FRIEND:
                return activity.getString(R.string.text_my_friend);
            case RoomActivity.STATUS_MY_SELF:
                return activity.getString(R.string.text_its_me);
            default:
                return "";
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.i(TAG, "on detached from window");
    }
}
