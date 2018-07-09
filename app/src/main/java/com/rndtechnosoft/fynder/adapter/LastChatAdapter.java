package com.rndtechnosoft.fynder.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.model.Chat;
import com.rndtechnosoft.fynder.model.LastChat;
import com.rndtechnosoft.fynder.utility.MyDateUtil;
import com.rndtechnosoft.fynder.utility.listener.OnTabUpdateListener;
import com.vanniktech.emoji.EmojiTextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Ravi on 12/7/2016.
 */

public class LastChatAdapter extends RecyclerView.Adapter<LastChatAdapter.ViewHolder> {
    private List<LastChat> lastChatList;
    private OnItemClickListener onItemClick;
    private OnTabUpdateListener onTabUpdateListener;
    private int countBadge = 0;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(LastChat item);
    }

    public LastChatAdapter(OnTabUpdateListener onTabUpdateListener,List<LastChat> lastChatList) {
        this.onTabUpdateListener = onTabUpdateListener;
        this.lastChatList = lastChatList;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClick) {
        this.onItemClick = onItemClick;
    }

    public void setOnTabUpdate(){
        this.countBadge++;
        if(onTabUpdateListener != null){
            onTabUpdateListener.onTabUpdate(3,this.countBadge);
        }
    }

    public void resetCountBadge(){
        countBadge = 0;
    }

    public void remove(int position){
        lastChatList.remove(position);
        notifyItemRemoved(position);
    }

    public void sort(){
        Collections.sort(lastChatList, new Comparator<LastChat>() {
            @Override
            public int compare(LastChat lc1, LastChat lc2) {
                return (lc2.getLastTimestamp() > lc1.getLastTimestamp()) ? 1 : -1;
            }
        });
        notifyDataSetChanged();
    }

    public Context getContext(){
        return context;
    }

    @Override
    public int getItemCount() {
        return lastChatList.size();
    }

    @Override
    public LastChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_chats, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(LastChatAdapter.ViewHolder holder, int position) {
        LastChat model = lastChatList.get(position);
        holder.model = model;
        holder.textDisplayName.setText(model.getDisplayName());
        holder.textLastMessage.setText(model.getLastMessage());
        holder.textDateTime.setVisibility(model.getLastTimestamp() == 0? View.GONE : View.VISIBLE);
        holder.textDateTime.setText(MyDateUtil.getLastChatTime((Context)onTabUpdateListener,model.getLastTimestamp()));
        holder.textCountBadge.setVisibility(model.getCountBadge() == 0 ? View.GONE : View.VISIBLE);
        holder.textCountBadge.setText(String.valueOf(model.getCountBadge()));
        holder.imageProfile.setImageURI(model.getImageUrl());
        holder.imageTick.setVisibility(model.isSender() ? View.VISIBLE : View.GONE);
        holder.imageTick.setImageResource(model.getLastStatus() == Chat.DELIVERED ?
                R.drawable.tick_single : R.drawable.tick_double);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout rootLayout;
        private SimpleDraweeView imageProfile;
        private TextView textDisplayName;
        private EmojiTextView textLastMessage;
        private TextView textDateTime;
        private TextView textCountBadge;
        private ImageView imageTick;
        private LastChat model;

        ViewHolder(View itemView) {
            super(itemView);
            rootLayout = (LinearLayout) itemView.findViewById(R.id.root_layout);
            imageProfile = (SimpleDraweeView) itemView.findViewById(R.id.image_profile);
            RoundingParams circle = RoundingParams.asCircle()
                    .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY);
            imageProfile.getHierarchy().setRoundingParams(circle);
            imageProfile.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
            imageProfile.getHierarchy().setPlaceholderImage(R.drawable.ic_profile_default);
            textDisplayName = (TextView) itemView.findViewById(R.id.text_display_name);
            textLastMessage = (EmojiTextView) itemView.findViewById(R.id.text_last_chat);
            textCountBadge = (TextView) itemView.findViewById(R.id.text_count_notification);
            textDateTime = (TextView) itemView.findViewById(R.id.text_date_last_msg);
            imageTick = (ImageView) itemView.findViewById(R.id.image_tick);
            rootLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClick != null) {
                        onItemClick.onItemClick(model);
                    }
                }
            });
        }
    }
}
