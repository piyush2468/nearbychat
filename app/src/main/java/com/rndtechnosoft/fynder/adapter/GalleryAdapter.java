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
 * Created by Ravi on 11/16/2016.
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(UserImage item);
    }

    private final List<UserImage> items;
    private OnItemClickListener listener;
    private boolean previewVersion = false;

    public GalleryAdapter(List<UserImage> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public GalleryAdapter(List<UserImage> items){
        this.items = items;
        previewVersion = true;
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_gallery, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UserImage model = items.get(position);
        holder.image.setImageURI(model.getThumbPic());
        holder.model = model;
        holder.listener = listener;
        holder.textMainImage.setVisibility(model.isMainImage() ? View.VISIBLE : View.GONE);
        if(previewVersion){
            holder.deSelectedView.setVisibility(model.isSelected() ? View.GONE : View.VISIBLE);
        }else{
            holder.deSelectedView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private SimpleDraweeView image;
        private UserImage model;
        private TextView textMainImage;
        private OnItemClickListener listener;
        private View deSelectedView;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (SimpleDraweeView) itemView.findViewById(R.id.images);
            textMainImage = (TextView) itemView.findViewById(R.id.text_main_image);
            deSelectedView = itemView.findViewById(R.id.image_deselected);
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
