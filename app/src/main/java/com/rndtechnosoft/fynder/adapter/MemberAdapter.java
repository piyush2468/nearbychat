package com.rndtechnosoft.fynder.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.model.UserImage;

import java.util.List;

/**
 * Created by Ravi on 11/21/2016.
 */

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder>{

    public interface OnItemClickListener {
        void onItemClick(UserImage item);
    }

    private final List<UserImage> items;
    private final OnItemClickListener listener;

    public MemberAdapter(List<UserImage> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_member, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UserImage model = items.get(position);
        holder.image.setImageURI(model.getThumbPic());
        holder.model = model;
        holder.listener = listener;
        holder.textDisplayName.setText(model.getName());
        holder.textHostImage.setVisibility(model.isMainImage() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private SimpleDraweeView image;
        private UserImage model;
        private TextView textHostImage;
        private TextView textDisplayName;
        private OnItemClickListener listener;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (SimpleDraweeView) itemView.findViewById(R.id.images);
            textHostImage = (TextView) itemView.findViewById(R.id.text_host);
            textDisplayName = (TextView) itemView.findViewById(R.id.text_member_name);
            image.getHierarchy().setPlaceholderImage(R.drawable.ic_profile_default);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(model);
                }
            });
        }

    }
}
