package com.rndtechnosoft.fynder.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.NativeExpressAdView;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.activity.FullChatImageActivity;
import com.rndtechnosoft.fynder.activity.ProfileActivity;
import com.rndtechnosoft.fynder.dialog.ReplyMessageView;
import com.rndtechnosoft.fynder.dialog.VideoPlayerDialog;
import com.rndtechnosoft.fynder.model.Chat;
import com.rndtechnosoft.fynder.utility.Ad_Helper;
import com.rndtechnosoft.fynder.utility.AudioPlayer;
import com.rndtechnosoft.fynder.utility.MyDateUtil;
import com.rndtechnosoft.fynder.utility.MyImageUtil;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.emoji.EmojiCategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ravi on 11/11/2016.
 */

public class ChatAdapter extends RecyclerView.Adapter {
    private final String TAG = "ChatAdapter";
    private final String emo_regex = "([\\u20a0-\\u32ff\\ud83c\\udc00-\\ud83d\\udeff\\udbb9\\udce5-\\udbb9\\udcee])";
    private final int VIEW_RIGHT_TEXT = 0;
    private final int VIEW_LEFT_TEXT = 1;
    private final int VIEW_RIGHT_IMAGE = 2;
    private final int VIEW_LEFT_IMAGE = 3;
    private final int VIEW_RIGHT_AUDIO = 4;
    private final int VIEW_LEFT_AUDIO = 5;
    private final int VIEW_RIGHT_VIDEO = 6;
    private final int VIEW_LEFT_VIDEO = 7;
    private final int VIEW_NATIVE_SMALL_AD = 8;
    private final int VIEW_NATIVE_MEDIUM_AD = 9;
    private final int VIEW_NATIVE_BIG_AD = 10;
    private final int VIEW_BANNER_AD = 11;
    private final int VIEW_MEDIUM_BANNER_AD = 12;
    private final int VIEW_DATE = 45;
    private final int VIEW_STATE_JOIN = 46;
    private final int VIEW_STATE_LEFT = 47;
    private final int VIEW_UNREAD_MESSAGE = 48;
    private String uid;
    private Activity activity;
    private List<Chat> list;
    private List<String> colorList;
    private Map<String, String> mapColor = new HashMap<>();
    private SparseBooleanArray mSelectedItemsIds;
    private boolean isPlaying = false;

    public ChatAdapter(Activity activity, List<Chat> list, String uid) {
        this.activity = activity;
        this.list = list;
        this.uid = uid;
        mSelectedItemsIds = new SparseBooleanArray();
    }

    private List<String> getColorList() {
        if (null == colorList || colorList.isEmpty()) {
            colorList = new ArrayList<>();
            colorList.add("#3749b9");
            colorList.add("#a30362");
            colorList.add("#bd502c");
            colorList.add("#8b5e3c");
            colorList.add("#6fa132");
            colorList.add("#3e7b7d");
            colorList.add("#5652a3");
            colorList.add("#059f55");
            colorList.add("#9e695b");
            colorList.add("#028497");
            colorList.add("#c47e16");
            colorList.add("#9b5a85");
            colorList.add("#d6562b");
            colorList.add("#ec008c");
            colorList.add("#7e3f20");
            colorList.add("#a3509f");
            colorList.add("#872237");
            colorList.add("#69856e");
            colorList.add("#c0923f");
            colorList.add("#ed1c24");
            colorList.add("#c69f0c");
            colorList.add("#72733e");
            colorList.add("#3a4a9f");
            colorList.add("#673c51");
            colorList.add("#006838");
            colorList.add("#a0a96a");
            colorList.add("#02ba02");
            colorList.add("#cc0099");
            colorList.add("#660000");
            colorList.add("#cc0033");
            colorList.add("#003399");
            colorList.add("#b53333");
            colorList.add("#3300ff");
            colorList.add("#999900");
            colorList.add("#bb7f65");
            colorList.add("#41aa8d");
            colorList.add("#5c6d2d");
            colorList.add("#000066");
            colorList.add("#17479e");
            colorList.add("#8e6b6f");
            colorList.add("#db3e6b");
            colorList.add("#9b8579");
            colorList.add("#662d91");
            colorList.add("#b25528");
            colorList.add("#be1e2d");
        }
        return colorList;
    }

    public String getColor(String userId) {
        String color = mapColor.get(userId);
        if (null == color) {
            if (getColorList().isEmpty()) {
                color = "#000000";
            } else {
                color = getColorList().remove(0);
                mapColor.put(userId, color);
            }
        }
        return color;
    }

    public List<Chat> getChatList() {
        return list;
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        Chat model = list.get(position);
        int type = model.getType();
        if (type == Chat.TYPE_DATE) {
            return VIEW_DATE;
        } else if (type == Chat.TYPE_JOIN) {
            return VIEW_STATE_JOIN;
        } else if (type == Chat.TYPE_LEFT) {
            return VIEW_STATE_LEFT;
        } else if (type == Chat.TYPE_UNREAD_MESSAGE) {
            return VIEW_UNREAD_MESSAGE;
        } else if (type == Chat.TYPE_AD) {
            if (model.getAdName().equalsIgnoreCase(Ad_Helper.AD_NATIVE_SMALL)) {
                return VIEW_NATIVE_SMALL_AD;
            } else if (model.getAdName().equalsIgnoreCase(Ad_Helper.AD_NATIVE_MEDIUM)) {
                return VIEW_NATIVE_MEDIUM_AD;
            } else if (model.getAdName().equalsIgnoreCase(Ad_Helper.AD_NATIVE_BIG)) {
                return VIEW_NATIVE_BIG_AD;
            } else if (model.getAdName().equalsIgnoreCase(Ad_Helper.AD_BANNER)) {
                return VIEW_BANNER_AD;
            } else if (model.getAdName().equalsIgnoreCase(Ad_Helper.AD_BANNER_MEDIUM)) {
                return VIEW_MEDIUM_BANNER_AD;
            } else {
                return VIEW_MEDIUM_BANNER_AD;
            }
        } else {
            if (list.get(position).getFrom().equals(uid)) {
                if (type == Chat.TYPE_TEXT) {
                    return VIEW_RIGHT_TEXT;
                } else if (type == Chat.TYPE_IMAGE) {
                    return VIEW_RIGHT_IMAGE;
                } else if (type == Chat.TYPE_AUDIO) {
                    return VIEW_RIGHT_AUDIO;
                } else {
                    return VIEW_RIGHT_VIDEO;
                }
            } else {
                if (type == Chat.TYPE_TEXT) {
                    return VIEW_LEFT_TEXT;
                } else if (type == Chat.TYPE_IMAGE) {
                    return VIEW_LEFT_IMAGE;
                } else if (type == Chat.TYPE_AUDIO) {
                    return VIEW_LEFT_AUDIO;
                } else {
                    return VIEW_LEFT_VIDEO;
                }
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_LEFT_TEXT) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_chat_text_left_side_new, viewGroup, false);
            vh = new ViewLeftTextHolder(v);
        } else if (viewType == VIEW_DATE) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_date_message, viewGroup, false);
            vh = new ViewDateHolder(v);
        } else if (viewType == VIEW_STATE_JOIN) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_state_joined, viewGroup, false);
            vh = new ViewJoinHolder(v);
        } else if (viewType == VIEW_STATE_LEFT) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_state_left, viewGroup, false);
            vh = new ViewLeftHolder(v);
        } else if (viewType == VIEW_UNREAD_MESSAGE) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_unread_message, viewGroup, false);
            vh = new ViewUnreadHolder(v);
        } else if (viewType == VIEW_RIGHT_IMAGE) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_chat_image_right_side, viewGroup, false);
            vh = new ViewRightImageHolder(v);
        } else if (viewType == VIEW_LEFT_IMAGE) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_chat_image_left_side, viewGroup, false);
            vh = new ViewLeftImageHolder(v);
        } else if (viewType == VIEW_RIGHT_AUDIO) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_chat_audio_right_side, viewGroup, false);
            vh = new ViewRightAudioHolder(v);
        } else if (viewType == VIEW_LEFT_AUDIO) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_chat_audio_left_side, viewGroup, false);
            vh = new ViewLeftAudioHolder(v);
        } else if (viewType == VIEW_RIGHT_VIDEO) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_chat_video_right_side, viewGroup, false);
            vh = new ViewRightVideoHolder(v);
        } else if (viewType == VIEW_LEFT_VIDEO) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_chat_video_left_side, viewGroup, false);
            vh = new ViewLeftVideoHolder(v);
        } else if (viewType == VIEW_NATIVE_SMALL_AD) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_chat_nativesmall_ad, viewGroup, false);
            vh = new ViewNativeSmallAdHolder(v);
        } else if (viewType == VIEW_NATIVE_MEDIUM_AD) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_chat_nativemedium_ad, viewGroup, false);
            vh = new ViewNativeMediumAdHolder(v);
        } else if (viewType == VIEW_NATIVE_BIG_AD) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_chat_nativebig_ad, viewGroup, false);
            vh = new ViewNativeBigAdHolder(v);
        } else if (viewType == VIEW_BANNER_AD) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_chat_banner_ad, viewGroup, false);
            vh = new ViewBannerAdHolder(v);
        } else if (viewType == VIEW_MEDIUM_BANNER_AD) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_chat_banner_medium_ad, viewGroup, false);
            vh = new ViewBannerMediumAdHolder(v);
        } /*else if (viewType == VIEW_INTERSTITIAL_AD) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_chat_banner_medium_ad, viewGroup, false);
            vh = new ViewInterstitialAdHolder(v);
        } */ else {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_chat_text_right_side_new, viewGroup, false);
            vh = new ViewRightTextHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        Chat model = list.get(position);
//        Log.i("GlobalRoom","Adapter -> text: "+model.getBody());

        /** Change background color of the selected items in list view  **/
        viewHolder.itemView
                .setBackgroundColor(mSelectedItemsIds.get(position) ? 0x9934B5E4
                        : Color.TRANSPARENT);
        if (viewHolder instanceof ViewLeftTextHolder) {
            int resColor = Color.parseColor(getColor(model.getFrom()));
            ((ViewLeftTextHolder) viewHolder).leftDisplayName.setVisibility(model.getTo() == null ? View.VISIBLE : View.GONE);
            ((ViewLeftTextHolder) viewHolder).leftDisplayName.setText(model.getDisplayName());
            ((ViewLeftTextHolder) viewHolder).leftDisplayName.setTextColor(resColor);
            ((ViewLeftTextHolder) viewHolder).leftTime.setText(MyDateUtil.getTime(model.getTimestamp()));
            ((ViewLeftTextHolder) viewHolder).leftProfileImage.setImageURI(model.getProfilePic());
//            ViewGroup.LayoutParams params = ((ViewLeftTextHolder) viewHolder).linear_u.getLayoutParams();
//            params.width = ((ViewLeftTextHolder) viewHolder).leftDisplayName.getWidth();
//            ((ViewLeftTextHolder) viewHolder).linear_u.setLayoutParams(params);
            ((ViewLeftTextHolder) viewHolder).leftProfileImage.setVisibility(model.getProfilePic() == null ?
                    View.GONE : View.VISIBLE);
            ((ViewLeftTextHolder) viewHolder).model = model;
            //check emoji
            boolean isOneEmoji = false;
            for (String word : model.getBody().split(" ")) {
                int length = model.getBody().length();
                Log.i(TAG, "Word: " + word + " , length: " + length);
                if (word.matches(emo_regex) && length == 2) {
                    isOneEmoji = true;
                }
            }
            int normalSize = (int) activity.getResources().getDimension(R.dimen.emoji_size);
            int largerSize = (int) activity.getResources().getDimension(R.dimen.emoji_size_larger);
            Log.i(TAG, "Normal size: " + normalSize + " , larger size: " + largerSize);
            ((ViewLeftTextHolder) viewHolder).leftText.setEmojiSize(isOneEmoji ? largerSize : normalSize);
//            ((ViewLeftTextHolder) viewHolder).leftText.setText(model.getBody());
            ((ViewLeftTextHolder) viewHolder).leftText.setText(model.getBody());

            //reply layout
            Chat replyChat = model.getReplyChat();
            if (replyChat == null) {
                ((ViewLeftTextHolder) viewHolder).replyLayout.removeAllViews();
                ((ViewLeftTextHolder) viewHolder).replyLayout.setVisibility(View.GONE);
            } else {

                ((ViewLeftTextHolder) viewHolder).replyLayout.setVisibility(View.VISIBLE);
                if (((ViewLeftTextHolder) viewHolder).replyLayout.getChildCount() == 0) {
                    int resReplyColor = Color.parseColor(getColor(replyChat.getFrom()));
                    ((ViewLeftTextHolder) viewHolder).replyLayout.addView(
                            new ReplyMessageView(activity, replyChat, resReplyColor, false)
                    );
                }
            }
        } else if (viewHolder instanceof ViewLeftAudioHolder) {
            int resColor = Color.parseColor(getColor(model.getFrom()));
            ((ViewLeftAudioHolder) viewHolder).leftDisplayName.setVisibility(model.getTo() == null ? View.VISIBLE : View.GONE);
            ((ViewLeftAudioHolder) viewHolder).leftDisplayName.setText(model.getDisplayName());
            ((ViewLeftAudioHolder) viewHolder).leftDisplayName.setTextColor(resColor);
            ((ViewLeftAudioHolder) viewHolder).leftTime.setText(MyDateUtil.getTime(model.getTimestamp()));
            ((ViewLeftAudioHolder) viewHolder).leftProfileImage.setImageURI(model.getProfilePic());
            ((ViewLeftAudioHolder) viewHolder).leftProfileImage.setVisibility(model.getProfilePic() == null ?
                    View.GONE : View.VISIBLE);
            ((ViewLeftAudioHolder) viewHolder).model = model;
            ((ViewLeftAudioHolder) viewHolder).leftTextProgress.setVisibility(View.GONE);
            ((ViewLeftAudioHolder) viewHolder).audioPlayer = new AudioPlayer(
                    activity,
                    model.getUrl(),
                    activity.getCacheDir().getAbsolutePath() + "/" + model.getId() + ".3gp",
                    null,
                    ((ViewLeftAudioHolder) viewHolder).leftSeekBar,
                    ((ViewLeftAudioHolder) viewHolder).leftTextProgress,
                    ((ViewLeftAudioHolder) viewHolder).leftProgressBar,
                    ((ViewLeftAudioHolder) viewHolder).leftPlayButton,
                    model) {
                @Override
                public void onCompleted() {
                    Log.i(TAG, "OnComplete play");
                    isPlaying = false;
                }
            };
            //reply layout
            Chat replyChat = model.getReplyChat();
            if (replyChat == null) {
                ((ViewLeftAudioHolder) viewHolder).replyLayout.removeAllViews();
                ((ViewLeftAudioHolder) viewHolder).replyLayout.setVisibility(View.GONE);
            } else {
                ((ViewLeftAudioHolder) viewHolder).replyLayout.setVisibility(View.VISIBLE);
                if (((ViewLeftAudioHolder) viewHolder).replyLayout.getChildCount() == 0) {
                    int resReplyColor = Color.parseColor(getColor(replyChat.getFrom()));
                    ((ViewLeftAudioHolder) viewHolder).replyLayout.addView(
                            new ReplyMessageView(activity, replyChat, resReplyColor, false)
                    );
                }
            }
        } else if (viewHolder instanceof ViewLeftVideoHolder) {
            int resColor = Color.parseColor(getColor(model.getFrom()));
            RoundingParams rounded = RoundingParams.fromCornersRadii(0, 40, 40, 40)
                    .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY)
                    .setBorder(resColor, 0);
            ((ViewLeftVideoHolder) viewHolder).leftDisplayName.setVisibility(model.getTo() == null ? View.VISIBLE : View.GONE);
            ((ViewLeftVideoHolder) viewHolder).leftDisplayName.setText(model.getDisplayName());
            ((ViewLeftVideoHolder) viewHolder).leftDisplayName.setTextColor(resColor);
            ((ViewLeftVideoHolder) viewHolder).leftTime.setText(MyDateUtil.getTime(model.getTimestamp()));
            ((ViewLeftVideoHolder) viewHolder).leftProfileImage.setImageURI(model.getProfilePic());
            ((ViewLeftVideoHolder) viewHolder).leftProfileImage.setVisibility(model.getProfilePic() == null ?
                    View.GONE : View.VISIBLE);
            ((ViewLeftVideoHolder) viewHolder).model = model;
            ((ViewLeftVideoHolder) viewHolder).leftVideo.getHierarchy().setRoundingParams(rounded);
            ((ViewLeftVideoHolder) viewHolder).leftVideo.setImageURI(model.getUrlThumbnail());
            ((ViewLeftVideoHolder) viewHolder).leftDurationLayout.setVisibility(model.getDuration() == null ? View.GONE : View.VISIBLE);
            ((ViewLeftVideoHolder) viewHolder).leftDuration.setText(model.getDuration() != null ? model.getDuration() : "");
            //reply layout
            Chat replyChat = model.getReplyChat();
            if (replyChat == null) {
                ((ViewLeftVideoHolder) viewHolder).replyLayout.removeAllViews();
                ((ViewLeftVideoHolder) viewHolder).replyLayout.setVisibility(View.GONE);
            } else {
                ((ViewLeftVideoHolder) viewHolder).replyLayout.setVisibility(View.VISIBLE);
                if (((ViewLeftVideoHolder) viewHolder).replyLayout.getChildCount() == 0) {
                    int resReplyColor = Color.parseColor(getColor(replyChat.getFrom()));
                    ((ViewLeftVideoHolder) viewHolder).replyLayout.addView(
                            new ReplyMessageView(activity, replyChat, resReplyColor, false)
                    );
                }
            }
        } else if (viewHolder instanceof ViewLeftImageHolder) {
            int resColor = Color.parseColor(getColor(model.getFrom()));
            RoundingParams rounded = RoundingParams.fromCornersRadii(0, 40, 40, 40)
                    .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY)
                    .setBorder(resColor, 0);
            ((ViewLeftImageHolder) viewHolder).leftDisplayName.setVisibility(model.getTo() == null ? View.VISIBLE : View.GONE);
            ((ViewLeftImageHolder) viewHolder).leftDisplayName.setText(model.getDisplayName());
            ((ViewLeftImageHolder) viewHolder).leftDisplayName.setTextColor(resColor);
            ((ViewLeftImageHolder) viewHolder).leftImageLowRes.getHierarchy().setRoundingParams(rounded);
            ((ViewLeftImageHolder) viewHolder).leftImageHighRes.getHierarchy().setRoundingParams(rounded);

            ((ViewLeftImageHolder) viewHolder).leftTime.setText(MyDateUtil.getTime(model.getTimestamp()));
            ((ViewLeftImageHolder) viewHolder).leftProfileImage.setImageURI(model.getProfilePic());
            ((ViewLeftImageHolder) viewHolder).leftProfileImage.setVisibility(model.getProfilePic() == null ?
                    View.GONE : View.VISIBLE);
            ((ViewLeftImageHolder) viewHolder).model = model;
            boolean isCached = MyImageUtil.isDownloaded(Uri.parse(model.getUrl()));
//            String imageUrl = isCached ? model.getUrl() : model.getUrlThumbnail();
            ((ViewLeftImageHolder) viewHolder).leftImageLowRes.setVisibility(isCached ? View.GONE : View.VISIBLE);
            ((ViewLeftImageHolder) viewHolder).leftImageHighRes.setVisibility(isCached ? View.VISIBLE : View.GONE);
            if (isCached) {
                ((ViewLeftImageHolder) viewHolder).leftImageHighRes.setImageURI(model.getUrl());
            } else {
                ((ViewLeftImageHolder) viewHolder).leftImageLowRes.setImageURI(model.getUrlThumbnail());
            }

            //reply layout
            Chat replyChat = model.getReplyChat();
            if (replyChat == null) {
                ((ViewLeftImageHolder) viewHolder).replyLayout.removeAllViews();
                ((ViewLeftImageHolder) viewHolder).replyLayout.setVisibility(View.GONE);
            } else {
                ((ViewLeftImageHolder) viewHolder).replyLayout.setVisibility(View.VISIBLE);
                if (((ViewLeftImageHolder) viewHolder).replyLayout.getChildCount() == 0) {
                    int resReplyColor = Color.parseColor(getColor(replyChat.getFrom()));
                    ((ViewLeftImageHolder) viewHolder).replyLayout.addView(
                            new ReplyMessageView(activity, replyChat, resReplyColor, false)
                    );
                }
            }

        } else if (viewHolder instanceof ViewDateHolder) {
            ((ViewDateHolder) viewHolder).textDate.setText(model.getBody());
        } else if (viewHolder instanceof ViewUnreadHolder) {
            ((ViewUnreadHolder) viewHolder).textUnreadMessage.setText(model.getBody());
        } else if (viewHolder instanceof ViewNativeSmallAdHolder) {
            AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
            adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
            ((ViewNativeSmallAdHolder) viewHolder).nativeExpressAdView.loadAd(adRequestBuilder.build());
        } else if (viewHolder instanceof ViewNativeMediumAdHolder) {
            AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
            adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
            ((ViewNativeMediumAdHolder) viewHolder).nativeExpressAdView.loadAd(adRequestBuilder.build());
        } else if (viewHolder instanceof ViewNativeBigAdHolder) {
            AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
            adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
            ((ViewNativeBigAdHolder) viewHolder).nativeExpressAdView.loadAd(adRequestBuilder.build());
        } else if (viewHolder instanceof ViewBannerAdHolder) {
            AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
            adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
            ((ViewBannerAdHolder) viewHolder).adView.loadAd(adRequestBuilder.build());

        } else if (viewHolder instanceof ViewBannerMediumAdHolder) {
            AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
            adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
            ((ViewBannerMediumAdHolder) viewHolder).adView.loadAd(adRequestBuilder.build());
        } else if (viewHolder instanceof ViewJoinHolder) {
            String displayName = model.getDisplayName();
            if (displayName == null) {
                ((ViewJoinHolder) viewHolder).rootLayout.setVisibility(View.GONE);
            } else {
                ((ViewJoinHolder) viewHolder).rootLayout.setVisibility(View.VISIBLE);
                ((ViewJoinHolder) viewHolder).textDisplayName.setText(displayName + " " + activity.getResources().getString(R.string.state_joined));
            }
        } else if (viewHolder instanceof ViewLeftHolder) {
            String displayName = model.getDisplayName();
            if (displayName == null) {
                ((ViewLeftHolder) viewHolder).rootLayout.setVisibility(View.GONE);
            } else {
                ((ViewLeftHolder) viewHolder).rootLayout.setVisibility(View.VISIBLE);
                ((ViewLeftHolder) viewHolder).textDisplayName.setText(displayName + " " + model.getBody());
//                ((ViewLeftHolder) viewHolder).textDescription.setText(model.getBody());
            }
        } else if (viewHolder instanceof ViewRightAudioHolder) {
            ((ViewRightAudioHolder) viewHolder).rightTime.setText(MyDateUtil.getTime(model.getTimestamp()));
            ((ViewRightAudioHolder) viewHolder).model = model;

            ((ViewRightAudioHolder) viewHolder).rightProfileImage.setImageURI(model.getProfilePic());
            ((ViewRightAudioHolder) viewHolder).rightProfileImage.setVisibility(model.getProfilePic() == null ?
                    View.GONE : View.VISIBLE);
            if (model.getUrl() == null) {
                ((ViewRightAudioHolder) viewHolder).rightProgressLayout.setVisibility(View.VISIBLE);
                ((ViewRightAudioHolder) viewHolder).rightAudioLayout.setVisibility(View.GONE);
            } else {
                ((ViewRightAudioHolder) viewHolder).audioPlayer = new AudioPlayer(
                        activity,
                        model.getUrl(),
                        activity.getCacheDir().getAbsolutePath() + "/" + model.getId() + ".3gp",
                        model.getPath(),
                        ((ViewRightAudioHolder) viewHolder).rightSeekBar,
                        ((ViewRightAudioHolder) viewHolder).rightTextProgress,
                        ((ViewRightAudioHolder) viewHolder).rightProgressBar,
                        ((ViewRightAudioHolder) viewHolder).rightPlayButton,
                        model) {
                    @Override
                    public void onCompleted() {
                        Log.i(TAG, "OnComplete play");
                        isPlaying = false;
                    }
                };
                ((ViewRightAudioHolder) viewHolder).rightProgressLayout.setVisibility(View.GONE);
                ((ViewRightAudioHolder) viewHolder).rightAudioLayout.setVisibility(View.VISIBLE);
                if (model.getTo() == null) { //chat room mode
                    ((ViewRightAudioHolder) viewHolder).rightImageTick.setVisibility(View.GONE);
                } else {
                    if (model.getStatus() == Chat.DELIVERED) {
                        try {
                            ((ViewRightAudioHolder) viewHolder).rightImageTick.setVisibility(View.VISIBLE);
                            ((ViewRightAudioHolder) viewHolder).rightImageTick.setImageResource(R.drawable.tick_single);
                        } catch (OutOfMemoryError e) {
                            ((ViewRightAudioHolder) viewHolder).rightImageTick.setVisibility(View.GONE);
                        }
                    } else if (model.getStatus() == Chat.READ) {
                        try {
                            ((ViewRightAudioHolder) viewHolder).rightImageTick.setVisibility(View.VISIBLE);
                            ((ViewRightAudioHolder) viewHolder).rightImageTick.setImageResource(R.drawable.tick_double);
                        } catch (OutOfMemoryError e) {
                            ((ViewRightAudioHolder) viewHolder).rightImageTick.setVisibility(View.GONE);
                        }
                    } else {
                        ((ViewRightAudioHolder) viewHolder).rightImageTick.setVisibility(View.GONE);
                    }
                }
            }
            //reply layout
            Chat replyChat = model.getReplyChat();
            if (replyChat == null) {
                ((ViewRightAudioHolder) viewHolder).replyLayout.removeAllViews();
                ((ViewRightAudioHolder) viewHolder).replyLayout.setVisibility(View.GONE);
            } else {
                ((ViewRightAudioHolder) viewHolder).replyLayout.setVisibility(View.VISIBLE);
                if (((ViewRightAudioHolder) viewHolder).replyLayout.getChildCount() == 0) {
                    int resReplyColor = Color.parseColor(getColor(replyChat.getFrom()));
                    ((ViewRightAudioHolder) viewHolder).replyLayout.addView(
                            new ReplyMessageView(activity, replyChat, resReplyColor, true)
                    );
                }
            }
        } else if (viewHolder instanceof ViewRightVideoHolder) {
            Log.i(TAG, "Right video => thumbnail: " + model.getUrlThumbnail());
            ((ViewRightVideoHolder) viewHolder).rightTime.setText(MyDateUtil.getTime(model.getTimestamp()));
            if (model.getTo() == null) { //chat room mode
                ((ViewRightVideoHolder) viewHolder).rightImageTick.setVisibility(View.GONE);
            } else {
                if (model.getStatus() == Chat.DELIVERED) {
                    try {
                        ((ViewRightVideoHolder) viewHolder).rightImageTick.setVisibility(View.VISIBLE);
                        ((ViewRightVideoHolder) viewHolder).rightImageTick.setImageResource(R.drawable.tick_single);
                    } catch (OutOfMemoryError e) {
                        ((ViewRightVideoHolder) viewHolder).rightImageTick.setVisibility(View.GONE);
                    }
                } else if (model.getStatus() == Chat.READ) {
                    try {
                        ((ViewRightVideoHolder) viewHolder).rightImageTick.setVisibility(View.VISIBLE);
                        ((ViewRightVideoHolder) viewHolder).rightImageTick.setImageResource(R.drawable.tick_double);
                    } catch (OutOfMemoryError e) {
                        ((ViewRightVideoHolder) viewHolder).rightImageTick.setVisibility(View.GONE);
                    }
                } else {
                    ((ViewRightVideoHolder) viewHolder).rightImageTick.setVisibility(View.GONE);
                }
            }
            ((ViewRightVideoHolder) viewHolder).model = model;
            ((ViewRightVideoHolder) viewHolder).rightVideo.setImageURI(model.getUrlThumbnail());
            ((ViewRightVideoHolder) viewHolder).rightDurationLayout.setVisibility(model.getDuration() == null ? View.GONE : View.VISIBLE);
            ((ViewRightVideoHolder) viewHolder).rightDuration.setText(model.getDuration() != null ? model.getDuration() : "");
            //reply layout
            Chat replyChat = model.getReplyChat();
            if (replyChat == null) {
                ((ViewRightVideoHolder) viewHolder).replyLayout.removeAllViews();
                ((ViewRightVideoHolder) viewHolder).replyLayout.setVisibility(View.GONE);
            } else {
                ((ViewRightVideoHolder) viewHolder).replyLayout.setVisibility(View.VISIBLE);
                if (((ViewRightVideoHolder) viewHolder).replyLayout.getChildCount() == 0) {
                    int resReplyColor = Color.parseColor(getColor(replyChat.getFrom()));
                    ((ViewRightVideoHolder) viewHolder).replyLayout.addView(
                            new ReplyMessageView(activity, replyChat, resReplyColor, true)
                    );
                }
            }
        } else if (viewHolder instanceof ViewRightImageHolder) {
            ((ViewRightImageHolder) viewHolder).rightTime.setText(MyDateUtil.getTime(model.getTimestamp()));
            if (model.getTo() == null) { //chat room mode
                ((ViewRightImageHolder) viewHolder).rightImageTick.setVisibility(View.GONE);
            } else {
                if (model.getStatus() == Chat.DELIVERED) {
                    try {
                        ((ViewRightImageHolder) viewHolder).rightImageTick.setVisibility(View.VISIBLE);
                        ((ViewRightImageHolder) viewHolder).rightImageTick.setImageResource(R.drawable.tick_single);
                    } catch (OutOfMemoryError e) {
                        ((ViewRightImageHolder) viewHolder).rightImageTick.setVisibility(View.GONE);
                    }
                } else if (model.getStatus() == Chat.READ) {
                    try {
                        ((ViewRightImageHolder) viewHolder).rightImageTick.setVisibility(View.VISIBLE);
                        ((ViewRightImageHolder) viewHolder).rightImageTick.setImageResource(R.drawable.tick_double);
                    } catch (OutOfMemoryError e) {
                        ((ViewRightImageHolder) viewHolder).rightImageTick.setVisibility(View.GONE);
                    }
                } else {
                    ((ViewRightImageHolder) viewHolder).rightImageTick.setVisibility(View.GONE);
                }
            }
            ((ViewRightImageHolder) viewHolder).model = model;
            ((ViewRightImageHolder) viewHolder).rightProgressBar.setVisibility(model.getUrl() == null ? View.VISIBLE : View.GONE);
            String lowRes = model.getUrlThumbnail();
            String highRes = model.getUrl();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setLowResImageRequest(ImageRequest.fromUri(lowRes))
                    .setImageRequest(ImageRequest.fromUri(highRes))
//                    .setOldController(((ViewRightImageHolder) viewHolder).rightImage.getController())
                    .build();
            ((ViewRightImageHolder) viewHolder).rightImage.setController(controller);
            //reply layout
            Chat replyChat = model.getReplyChat();
            if (replyChat == null) {
                ((ViewRightImageHolder) viewHolder).replyLayout.removeAllViews();
                ((ViewRightImageHolder) viewHolder).replyLayout.setVisibility(View.GONE);
            } else {
                ((ViewRightImageHolder) viewHolder).replyLayout.setVisibility(View.VISIBLE);
                if (((ViewRightImageHolder) viewHolder).replyLayout.getChildCount() == 0) {
                    int resReplyColor = Color.parseColor(getColor(replyChat.getFrom()));
                    ((ViewRightImageHolder) viewHolder).replyLayout.addView(
                            new ReplyMessageView(activity, replyChat, resReplyColor, true)
                    );
                }
            }
//            boolean isCached = MyImageUtil.isDownloaded(Uri.parse(model.getUrl()));
//            if (isCached) {
//                ((ViewRightImageHolder) viewHolder).rightImageHighRes.setVisibility(View.VISIBLE);
//                ((ViewRightImageHolder) viewHolder).rightImageLowRes.setVisibility(View.GONE);
//                ((ViewRightImageHolder) viewHolder).rightImageHighRes.setImageURI(model.getUrl());
//            } else {
//                ((ViewRightImageHolder) viewHolder).rightImageHighRes.setVisibility(View.GONE);
//                ((ViewRightImageHolder) viewHolder).rightImageLowRes.setVisibility(View.VISIBLE);
//                ((ViewRightImageHolder) viewHolder).rightImageLowRes.setImageURI(model.getUrlThumbnail());
//            }
//            }
        } else {
            ((ViewRightTextHolder) viewHolder).rightTime.setText(MyDateUtil.getTime(model.getTimestamp()));
            if (model.getTo() == null) { //chat room mode
                ((ViewRightTextHolder) viewHolder).rightImageTick.setVisibility(View.GONE);
            } else {
                if (model.getStatus() == Chat.DELIVERED) {
                    try {
                        ((ViewRightTextHolder) viewHolder).rightImageTick.setVisibility(View.VISIBLE);
                        ((ViewRightTextHolder) viewHolder).rightImageTick.setImageResource(R.drawable.tick_single);
                    } catch (OutOfMemoryError e) {
                        ((ViewRightTextHolder) viewHolder).rightImageTick.setVisibility(View.GONE);
                    }
                } else if (model.getStatus() == Chat.READ) {
                    try {
                        ((ViewRightTextHolder) viewHolder).rightImageTick.setVisibility(View.VISIBLE);
                        ((ViewRightTextHolder) viewHolder).rightImageTick.setImageResource(R.drawable.tick_double);
                    } catch (OutOfMemoryError e) {
                        ((ViewRightTextHolder) viewHolder).rightImageTick.setVisibility(View.GONE);
                    }
                } else {
                    ((ViewRightTextHolder) viewHolder).rightImageTick.setVisibility(View.GONE);
                }
            }
            //check emoji
            boolean isOneEmoji = false;
            for (String word : model.getBody().split(" ")) {
                if (word.matches(emo_regex) && model.getBody().length() == 2) {
                    Log.i(TAG, "Word: " + word);
                    isOneEmoji = true;
                }
            }
            int normalSize = (int) activity.getResources().getDimension(R.dimen.emoji_size);
            int largerSize = (int) activity.getResources().getDimension(R.dimen.emoji_size_larger);
            Log.i(TAG, "Normal size: " + normalSize + " , larger size: " + largerSize);
            ((ViewRightTextHolder) viewHolder).rightText.setEmojiSize(isOneEmoji ? largerSize : normalSize);
            ((ViewRightTextHolder) viewHolder).rightText.setText(model.getBody());

            //reply layout
            Chat replyChat = model.getReplyChat();

            if (replyChat == null) {
                ((ViewRightTextHolder) viewHolder).replyLayout.removeAllViews();
                ((ViewRightTextHolder) viewHolder).replyLayout.setVisibility(View.GONE);
            } else {

                ((ViewRightTextHolder) viewHolder).replyLayout.setVisibility(View.VISIBLE);
                if (((ViewRightTextHolder) viewHolder).replyLayout.getChildCount() == 0) {
                    int resReplyColor = Color.parseColor(getColor(replyChat.getFrom()));
                    ((ViewRightTextHolder) viewHolder).replyLayout.addView(
                            new ReplyMessageView(activity, replyChat, resReplyColor, true)
                    );
                }
            }
        }
    }

    /***
     * Methods required for do selections, remove selections, etc.
     */

//Toggle selection methods
    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }


    //Remove selected selections
    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }


    //Put or delete selected position into SparseBooleanArray
    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);

        notifyDataSetChanged();
    }

    //Get total selected count
    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    //Return all selected ids
    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

    private class ViewRightTextHolder extends RecyclerView.ViewHolder {
        LinearLayout replyLayout;
        ImageView rightImageTick;
        EmojiTextView rightText;
        TextView rightTime;

        ViewRightTextHolder(View itemView) {
            super(itemView);
            replyLayout = (LinearLayout) itemView.findViewById(R.id.right_reply_layout);
            rightImageTick = (ImageView) itemView.findViewById(R.id.image_tick_status);
            rightText = (EmojiTextView) itemView.findViewById(R.id.right_text);
            rightTime = (TextView) itemView.findViewById(R.id.right_text_time);
        }
    }

    private class ViewRightAudioHolder extends RecyclerView.ViewHolder {
        LinearLayout replyLayout;
        LinearLayout rightProgressLayout;
        LinearLayout rightAudioLayout;
        ImageButton rightPlayButton;
        SeekBar rightSeekBar;
        TextView rightTextProgress;
        ProgressBar rightProgressBar;
        ImageView rightImageTick;
        TextView rightTime;
        Chat model;
        AudioPlayer audioPlayer;
        SimpleDraweeView rightProfileImage;

        ViewRightAudioHolder(View itemView) {
            super(itemView);
            rightProfileImage = (SimpleDraweeView) itemView.findViewById(R.id.right_profile_pic);
            RoundingParams circle = RoundingParams.asCircle()
                    .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY);
            rightProfileImage.getHierarchy().setRoundingParams(circle);
            rightProfileImage.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
            rightProfileImage.getHierarchy().setPlaceholderImage(R.drawable.ic_profile_default);
            replyLayout = (LinearLayout) itemView.findViewById(R.id.reply_layout);
            rightProgressLayout = (LinearLayout) itemView.findViewById(R.id.right_progress_layout);
            rightAudioLayout = (LinearLayout) itemView.findViewById(R.id.right_audio_layout);
            rightImageTick = (ImageView) itemView.findViewById(R.id.image_tick_status);
            rightTime = (TextView) itemView.findViewById(R.id.right_text_time);
            rightPlayButton = (ImageButton) itemView.findViewById(R.id.right_play_button);
            rightSeekBar = (SeekBar) itemView.findViewById(R.id.right_seek_bar);
            rightTextProgress = (TextView) itemView.findViewById(R.id.right_text_progress);
            rightProgressBar = (ProgressBar) itemView.findViewById(R.id.right_progress_bar);
            rightProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (model == null) {
                        return;
                    }
                    Intent intent = new Intent(activity, ProfileActivity.class);
                    intent.putExtra(ProfileActivity.KEY_UID, model.getTo());
                    intent.putExtra(ProfileActivity.KEY_DISPLAY_NAME, model.getDisplayName());
                    activity.startActivity(intent);
                }
            });

            rightPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(TAG, "Play button clicked");
                    if (audioPlayer == null) {
                        return;
                    }
                    if (isPlaying) {
                        Log.e(TAG, "Media has played already..!!");
                        Toast.makeText(activity, activity.getString(R.string.audio_required_wait), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    isPlaying = true;
                    audioPlayer.play();
                }
            });
        }
    }

    private class ViewRightVideoHolder extends RecyclerView.ViewHolder {
        LinearLayout replyLayout;
        SimpleDraweeView rightVideo;
        CardView rightDurationLayout;
        RelativeLayout rightButtonPlay;
        ProgressBar rightProgressBar;
        ImageView rightImageTick;
        TextView rightTime;
        TextView rightDuration;
        Chat model;

        ViewRightVideoHolder(View itemView) {
            super(itemView);
            replyLayout = (LinearLayout) itemView.findViewById(R.id.reply_layout);
            rightVideo = (SimpleDraweeView) itemView.findViewById(R.id.right_video);
            rightDurationLayout = (CardView) itemView.findViewById(R.id.right_duration_layout);
            rightVideo.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
            RoundingParams rounded = RoundingParams.fromCornersRadii(40, 40, 0, 40)
                    .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY)
                    .setBorder(ContextCompat.getColor(activity, R.color.color_right_chat_background), 0);
            rightVideo.getHierarchy().setRoundingParams(rounded);
            rightButtonPlay = (RelativeLayout) itemView.findViewById(R.id.right_button_play);
            rightImageTick = (ImageView) itemView.findViewById(R.id.image_tick_status);
            rightTime = (TextView) itemView.findViewById(R.id.right_text_time);
            rightDuration = (TextView) itemView.findViewById(R.id.right_text_duration);
            rightProgressBar = (ProgressBar) itemView.findViewById(R.id.right_progress_bar_image);
            rightButtonPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (model == null || model.getUrl() == null) {
                        return;
                    }
                    new VideoPlayerDialog(activity, model.getPath(), model.getUrl()).show();
                }
            });
        }
    }

    private class ViewRightImageHolder extends RecyclerView.ViewHolder {
        LinearLayout replyLayout;
        SimpleDraweeView rightImage;
        ProgressBar rightProgressBar;
        ImageView rightImageTick;
        TextView rightTime;
        Chat model;

        ViewRightImageHolder(View itemView) {
            super(itemView);
            replyLayout = (LinearLayout) itemView.findViewById(R.id.reply_layout);
            rightImage = (SimpleDraweeView) itemView.findViewById(R.id.right_image);
            RoundingParams rounded = RoundingParams.fromCornersRadii(40, 40, 0, 40)
                    .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY)
                    .setBorder(ContextCompat.getColor(activity, R.color.color_right_chat_background), 0);
            rightImage.getHierarchy().setRoundingParams(rounded);
            rightImage.getHierarchy().setPlaceholderImage(R.color.black_translucent);
            rightImageTick = (ImageView) itemView.findViewById(R.id.image_tick_status);
            rightTime = (TextView) itemView.findViewById(R.id.right_text_time);
            rightProgressBar = (ProgressBar) itemView.findViewById(R.id.right_progress_bar_image);
            View.OnClickListener onRightImageClick = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (model == null) {
                        return;
                    }
                    Intent intent = new Intent(activity, FullChatImageActivity.class);
                    intent.putExtra(FullChatImageActivity.KEY_IMAGE_LOW_RES, model.getUrlThumbnail());
                    intent.putExtra(FullChatImageActivity.KEY_IMAGE_HIGH_RES, model.getUrl());
                    activity.startActivity(intent);
                }
            };
            rightImage.setOnClickListener(onRightImageClick);
        }
    }

    private class ViewLeftAudioHolder extends RecyclerView.ViewHolder {
        LinearLayout replyLayout;
        SimpleDraweeView leftProfileImage;
        ImageButton leftPlayButton;
        SeekBar leftSeekBar;
        TextView leftTextProgress;
        TextView leftDisplayName;
        TextView leftTime;
        ProgressBar leftProgressBar;
        Chat model;
        AudioPlayer audioPlayer;

        ViewLeftAudioHolder(View itemView) {
            super(itemView);
            replyLayout = (LinearLayout) itemView.findViewById(R.id.reply_layout);
            leftProfileImage = (SimpleDraweeView) itemView.findViewById(R.id.left_profile_pic);
            RoundingParams circle = RoundingParams.asCircle()
                    .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY);
            leftProfileImage.getHierarchy().setRoundingParams(circle);
            leftProfileImage.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
            leftProfileImage.getHierarchy().setPlaceholderImage(R.drawable.ic_profile_default);
            leftDisplayName = (TextView) itemView.findViewById(R.id.left_display_name);
            leftTime = (TextView) itemView.findViewById(R.id.left_text_time);
            leftPlayButton = (ImageButton) itemView.findViewById(R.id.left_play_button);
            leftSeekBar = (SeekBar) itemView.findViewById(R.id.left_seek_bar);
            leftTextProgress = (TextView) itemView.findViewById(R.id.left_text_progress);
            leftProgressBar = (ProgressBar) itemView.findViewById(R.id.left_progress_bar);
            leftProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (model == null) {
                        return;
                    }
                    Intent intent = new Intent(activity, ProfileActivity.class);
                    intent.putExtra(ProfileActivity.KEY_UID, model.getFrom());
                    intent.putExtra(ProfileActivity.KEY_DISPLAY_NAME, model.getDisplayName());
                    activity.startActivity(intent);
                }
            });
            leftDisplayName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (model == null) {
                        return;
                    }
                    Intent intent = new Intent(activity, ProfileActivity.class);
                    intent.putExtra(ProfileActivity.KEY_UID, model.getFrom());
                    intent.putExtra(ProfileActivity.KEY_DISPLAY_NAME, model.getDisplayName());
                    activity.startActivity(intent);
                }
            });
            leftPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(TAG, "Play button clicked");
                    if (audioPlayer == null) {
                        return;
                    }
                    if (isPlaying) {
                        Log.e(TAG, "Media has played already..!!");
                        Toast.makeText(activity, activity.getString(R.string.audio_required_wait), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    isPlaying = true;
                    audioPlayer.play();
                }
            });
        }
    }

    private class ViewLeftVideoHolder extends RecyclerView.ViewHolder {
        LinearLayout replyLayout;
        SimpleDraweeView leftProfileImage;
        SimpleDraweeView leftVideo;
        CardView leftDurationLayout;
        RelativeLayout leftButtonPlay;
        TextView leftDisplayName;
        TextView leftTime;
        TextView leftDuration;
        Chat model;

        ViewLeftVideoHolder(View itemView) {
            super(itemView);
            replyLayout = (LinearLayout) itemView.findViewById(R.id.reply_layout);
            leftVideo = (SimpleDraweeView) itemView.findViewById(R.id.left_video);
            leftVideo.getHierarchy().setPlaceholderImage(R.color.black_translucent);
            leftDurationLayout = (CardView) itemView.findViewById(R.id.left_duration_layout);
            leftButtonPlay = (RelativeLayout) itemView.findViewById(R.id.left_button_play);
            leftProfileImage = (SimpleDraweeView) itemView.findViewById(R.id.left_profile_pic);
            RoundingParams circle = RoundingParams.asCircle()
                    .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY);
            leftProfileImage.getHierarchy().setRoundingParams(circle);
            leftProfileImage.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
            leftProfileImage.getHierarchy().setPlaceholderImage(R.drawable.ic_profile_default);
            leftDisplayName = (TextView) itemView.findViewById(R.id.left_display_name);
            leftTime = (TextView) itemView.findViewById(R.id.left_text_time);
            leftDuration = (TextView) itemView.findViewById(R.id.left_text_duration);
            leftProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (model == null) {
                        return;
                    }
                    Intent intent = new Intent(activity, ProfileActivity.class);
                    intent.putExtra(ProfileActivity.KEY_UID, model.getFrom());
                    intent.putExtra(ProfileActivity.KEY_DISPLAY_NAME, model.getDisplayName());
                    activity.startActivity(intent);
                }
            });
            leftDisplayName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (model == null) {
                        return;
                    }
                    Intent intent = new Intent(activity, ProfileActivity.class);
                    intent.putExtra(ProfileActivity.KEY_UID, model.getFrom());
                    intent.putExtra(ProfileActivity.KEY_DISPLAY_NAME, model.getDisplayName());
                    activity.startActivity(intent);
                }
            });
            leftButtonPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (model == null || model.getUrl() == null) {
                        return;
                    }
                    new VideoPlayerDialog(activity, model.getPath(), model.getUrl()).show();
                }
            });
        }
    }

    private class ViewLeftImageHolder extends RecyclerView.ViewHolder {
        LinearLayout replyLayout;
        SimpleDraweeView leftProfileImage;
        SimpleDraweeView leftImageLowRes;
        SimpleDraweeView leftImageHighRes;
        TextView leftDisplayName;
        TextView leftTime;
        ProgressBar leftProgressBar;
        Chat model;

        ViewLeftImageHolder(View itemView) {
            super(itemView);
            replyLayout = (LinearLayout) itemView.findViewById(R.id.reply_layout);
            leftProfileImage = (SimpleDraweeView) itemView.findViewById(R.id.left_profile_pic);
            RoundingParams circle = RoundingParams.asCircle()
                    .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY);
            leftImageLowRes = (SimpleDraweeView) itemView.findViewById(R.id.left_image_low_res);
            leftImageHighRes = (SimpleDraweeView) itemView.findViewById(R.id.left_image_high_res);
            leftImageLowRes.getHierarchy().setPlaceholderImage(R.color.black_translucent);
            leftImageHighRes.getHierarchy().setPlaceholderImage(R.color.black_translucent);
            leftProgressBar = (ProgressBar) itemView.findViewById(R.id.left_progress_bar);
            leftProfileImage.getHierarchy().setRoundingParams(circle);
            leftProfileImage.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
            leftProfileImage.getHierarchy().setPlaceholderImage(R.drawable.ic_profile_default);
            leftDisplayName = (TextView) itemView.findViewById(R.id.left_display_name);
            leftTime = (TextView) itemView.findViewById(R.id.left_text_time);
            leftProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (model == null) {
                        return;
                    }
                    Intent intent = new Intent(activity, ProfileActivity.class);
                    intent.putExtra(ProfileActivity.KEY_UID, model.getFrom());
                    intent.putExtra(ProfileActivity.KEY_DISPLAY_NAME, model.getDisplayName());
                    activity.startActivity(intent);
                }
            });
            leftDisplayName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (model == null) {
                        return;
                    }
                    Intent intent = new Intent(activity, ProfileActivity.class);
                    intent.putExtra(ProfileActivity.KEY_UID, model.getFrom());
                    intent.putExtra(ProfileActivity.KEY_DISPLAY_NAME, model.getDisplayName());
                    activity.startActivity(intent);
                }
            });

            View.OnClickListener leftImageOnClick = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (model == null) {
                        return;
                    }
                    if (leftImageLowRes.getVisibility() != View.VISIBLE) {
                        Intent intent = new Intent(activity, FullChatImageActivity.class);
                        intent.putExtra(FullChatImageActivity.KEY_IMAGE_LOW_RES, model.getUrlThumbnail());
                        intent.putExtra(FullChatImageActivity.KEY_IMAGE_HIGH_RES, model.getUrl());
                        activity.startActivity(intent);
                    } else {
                        leftProgressBar.setVisibility(View.VISIBLE);
                        leftImageHighRes.setVisibility(View.VISIBLE);
                        String url = model.getUrl();
                        Uri uri = Uri.parse(url);
                        ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
                            @Override
                            public void onFinalImageSet(
                                    String id,
                                    @Nullable ImageInfo imageInfo,
                                    @Nullable Animatable anim) {
                                if (imageInfo == null) {
                                    return;
                                }


                                leftProgressBar.setVisibility(View.GONE);
                                leftImageLowRes.setVisibility(View.GONE);
//                            Timber.tag(TAG).i("Image Received...!!!");
//                            notifyDataSetChanged();
                            }

                            @Override
                            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
//                                        FLog.d("Intermediate image received");
//                            Timber.tag(TAG).d("Intermediate image received");
                                leftProgressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onFailure(String id, Throwable throwable) {
//                                        FLog.e(getClass(), throwable, "Error loading %s", id)
//                            Timber.tag(TAG).e("Error Loading: " + throwable.getMessage());
                                leftProgressBar.setVisibility(View.GONE);
                                Toast.makeText(activity, activity.getString(R.string.error_downloading_image), Toast.LENGTH_SHORT).show();
                            }
                        };
                        DraweeController controller = Fresco.newDraweeControllerBuilder()
                                .setControllerListener(controllerListener)
                                .setUri(uri).build();
                        leftImageHighRes.setController(controller);
                    }
                }
            };
            leftImageLowRes.setOnClickListener(leftImageOnClick);
            leftImageHighRes.setOnClickListener(leftImageOnClick);
        }
    }

    private class ViewLeftTextHolder extends RecyclerView.ViewHolder {
        LinearLayout replyLayout;
        LinearLayout linear_u;
        SimpleDraweeView leftProfileImage;
        TextView leftDisplayName;
        EmojiTextView leftText;
        TextView leftTime;
        Chat model;

        ViewLeftTextHolder(View itemView) {
            super(itemView);
            replyLayout = (LinearLayout) itemView.findViewById(R.id.left_reply_layout);
            leftProfileImage = (SimpleDraweeView) itemView.findViewById(R.id.left_profile_pic);
            RoundingParams circle = RoundingParams.asCircle()
                    .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY);
            leftProfileImage.getHierarchy().setRoundingParams(circle);
            leftProfileImage.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
            leftProfileImage.getHierarchy().setPlaceholderImage(R.drawable.ic_profile_default);
            leftDisplayName = (TextView) itemView.findViewById(R.id.left_display_name);
            leftText = (EmojiTextView) itemView.findViewById(R.id.left_text);
            leftTime = (TextView) itemView.findViewById(R.id.left_text_time);
            leftProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (model == null) {
                        return;
                    }
                    Intent intent = new Intent(activity, ProfileActivity.class);
                    intent.putExtra(ProfileActivity.KEY_UID, model.getFrom());
                    intent.putExtra(ProfileActivity.KEY_DISPLAY_NAME, model.getDisplayName());
                    activity.startActivity(intent);
                }
            });
            leftDisplayName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (model == null) {
                        return;
                    }
                    Intent intent = new Intent(activity, ProfileActivity.class);
                    intent.putExtra(ProfileActivity.KEY_UID, model.getFrom());
                    intent.putExtra(ProfileActivity.KEY_DISPLAY_NAME, model.getDisplayName());
                    activity.startActivity(intent);
                }
            });
        }
    }

    private class ViewDateHolder extends RecyclerView.ViewHolder {
        TextView textDate;

        ViewDateHolder(View itemView) {
            super(itemView);
            textDate = (TextView) itemView.findViewById(R.id.text_date_message);
        }
    }

    private class ViewUnreadHolder extends RecyclerView.ViewHolder {
        TextView textUnreadMessage;

        ViewUnreadHolder(View itemView) {
            super(itemView);
            textUnreadMessage = (TextView) itemView.findViewById(R.id.text_unread_message);
        }
    }

    private class ViewNativeSmallAdHolder extends RecyclerView.ViewHolder {
        NativeExpressAdView nativeExpressAdView;

        ViewNativeSmallAdHolder(View itemView) {
            super(itemView);
            nativeExpressAdView = (NativeExpressAdView) itemView.findViewById(R.id.NativeExpressAdView);
        }
    }

    private class ViewNativeMediumAdHolder extends RecyclerView.ViewHolder {
        NativeExpressAdView nativeExpressAdView;

        ViewNativeMediumAdHolder(View itemView) {
            super(itemView);
            nativeExpressAdView = (NativeExpressAdView) itemView.findViewById(R.id.NativeExpressAdView);
        }

    }

    private class ViewNativeBigAdHolder extends RecyclerView.ViewHolder {
        NativeExpressAdView nativeExpressAdView;

        ViewNativeBigAdHolder(View itemView) {
            super(itemView);
            nativeExpressAdView = (NativeExpressAdView) itemView.findViewById(R.id.NativeExpressAdView);
        }

    }

    private class ViewBannerAdHolder extends RecyclerView.ViewHolder {
        AdView adView;

        ViewBannerAdHolder(View itemView) {
            super(itemView);
            adView = (AdView) itemView.findViewById(R.id.adView);
        }

    }

    private class ViewBannerMediumAdHolder extends RecyclerView.ViewHolder {
        AdView adView;

        ViewBannerMediumAdHolder(View itemView) {
            super(itemView);
            adView = (AdView) itemView.findViewById(R.id.adView);

        }

    }

    private class ViewInterstitialAdHolder extends RecyclerView.ViewHolder {

        ViewInterstitialAdHolder(View itemView) {
            super(itemView);
        }

    }

    private class ViewJoinHolder extends RecyclerView.ViewHolder {
        LinearLayout rootLayout;
        TextView textDisplayName;

        ViewJoinHolder(View itemView) {
            super(itemView);
            rootLayout = (LinearLayout) itemView.findViewById(R.id.root_layout);
            textDisplayName = (TextView) itemView.findViewById(R.id.text_joined_name);
        }
    }

    private class ViewLeftHolder extends RecyclerView.ViewHolder {
        LinearLayout rootLayout;
        TextView textDisplayName;

        ViewLeftHolder(View itemView) {
            super(itemView);
            rootLayout = (LinearLayout) itemView.findViewById(R.id.root_layout);
            textDisplayName = (TextView) itemView.findViewById(R.id.text_left_name);
        }
    }

}
