package com.rndtechnosoft.fynder.adapter;

import android.app.Activity;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rndtechnosoft.fynder.FynderApplication;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.model.Room;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Created by Ravi on 11/20/2016.
 */

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {
    private String myUid;

    public interface OnItemClickListener {
        void onItemClick(Room item);
    }
    private Activity activity;
    private List<Room> roomList;
    private final OnItemClickListener listener;

    public RoomAdapter(Activity activity, List<Room> roomList, OnItemClickListener listener) {
        this.roomList = roomList;
        this.activity=activity;
        this.listener = listener;

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null){
            myUid = firebaseUser.getUid();
        }

    }

    public void sort() {

        Collections.sort(roomList, new Comparator<Room>() {
            @Override
            public int compare(Room o, Room o2) {
                float[] result1 = new float[3];
                android.location.Location.distanceBetween(FynderApplication.getInstance().getLat(), FynderApplication.getInstance().getLng(), o.getLatitude(), o.getLongitude(), result1);
                Float distance1 = result1[0];

                float[] result2 = new float[3];
                android.location.Location.distanceBetween(FynderApplication.getInstance().getLat(), FynderApplication.getInstance().getLng(), o2.getLatitude(), o2.getLongitude(), result2);
                Float distance2 = result2[0];
                return distance1.compareTo(distance2);
            }
        });
        notifyDataSetChanged();

//        Collections.sort(//isMyRoomExist ? roomList.subList(1, roomList.size()-1) :
//                roomList
//                , new Comparator<Room>() {
//                    @Override
//                    public int compare(Room room1, Room room2) {
//                        return (room2.getNumMembers() > room1.getNumMembers()) ? 1 : -1;
//                    }
//                });
//        notifyDataSetChanged();

        //Set my room in to the top of list
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            int position = 0;
            boolean isMyRoomExist = false;
            for (Room room : roomList) {
                if (room.getId().equals(uid)) {
                    isMyRoomExist = true;
                    break;
                }
                position++;
            }
            Log.i("RoomFragment", "Adapter -> swap Position: " + position+" , is my room exist: "+isMyRoomExist);
            if (position > 0 && isMyRoomExist) { //need to swap
                Collections.swap(roomList, position, 0);
                notifyDataSetChanged();
            }
        }
    }


    private float getDistancBetweenTwoPoints(double lat1,double lon1,double lat2,double lon2) {

        float[] distance = new float[2];

        Location.distanceBetween( lat1, lon1,
                lat2, lon2, distance);

        return distance[0];
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    @Override
    public RoomAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(activity).inflate(R.layout.list_room, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RoomAdapter.ViewHolder holder, int position) {
        Room model = roomList.get(position);
        holder.textTitle.setText(model.getName());
        holder.textDescription.setText(model.getDescription());
        holder.textCity.setText(model.getCity()+", "+model.getCountry());
        holder.imageProfile.setImageURI(model.getImageUrl());
        holder.textNumOccupant.setText(String.valueOf(model.getNumMembers()));
        holder.textFlagMyRoom.setVisibility(model.isMyRoom() ? View.VISIBLE : View.GONE);
        holder.model = model;


        float distance =getDistancBetweenTwoPoints(FynderApplication.getInstance().getLat()
                , FynderApplication.getInstance().getLng()
                ,model.getLatitude()
                ,model.getLongitude());
        DecimalFormat precision = new DecimalFormat("0.00");
        int[] array = {activity.getResources().getColor(R.color.colorAccent),
                activity.getResources().getColor(R.color.color_left_chat_background),
                activity.getResources().getColor(R.color.color_orange)};
        int randomStr = array[new Random().nextInt(array.length)];

        holder.textDistance.setTextColor(randomStr);
//        holder.textDistance.setVisibility(model.getId().equals(myUid) ? View.GONE : View.VISIBLE);
        if(distance<100){
            holder.textDistance.setText(""+precision.format(distance)+" m");
        }else{
            holder.textDistance.setText(""+precision.format(distance/1000)+" km");
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout rootLayout;
        private SimpleDraweeView imageProfile;
        private TextView textNumOccupant;
        private TextView textTitle;
        private TextView textDescription;
        private TextView textFlagMyRoom;
        private Room model;
        public TextView textDistance;
        public TextView textCity;

        public ViewHolder(View itemView) {
            super(itemView);
            rootLayout = (LinearLayout) itemView.findViewById(R.id.root_layout);
            imageProfile = (SimpleDraweeView) itemView.findViewById(R.id.image_profile);
            RoundingParams rounded = RoundingParams.fromCornersRadius(10)
                    .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY);
            imageProfile.getHierarchy().setRoundingParams(rounded);
            imageProfile.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
            textNumOccupant = (TextView) itemView.findViewById(R.id.textLargeNumber);
            textTitle = (TextView) itemView.findViewById(R.id.text_title);
            textDistance = (TextView) itemView.findViewById(R.id.text_distance);
            textDescription = (TextView) itemView.findViewById(R.id.text_description);
            textCity = (TextView) itemView.findViewById(R.id.text_location);
            textFlagMyRoom = (TextView) itemView.findViewById(R.id.my_room_text);
            rootLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onItemClick(model);
                    }
                }
            });
            
            
        }
    }
}
