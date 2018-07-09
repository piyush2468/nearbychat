package com.rndtechnosoft.fynder.adapter;

import android.app.Activity;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rndtechnosoft.fynder.FynderApplication;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.model.User;
import com.rndtechnosoft.fynder.utility.MyDateUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Ravi on 11/11/2016.
 */

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MyViewHolder> implements Filterable {
    private final boolean showme;
    private final Activity activity;
    private final OnItemClickListener listener;
    private final boolean isShowgGrid;
    private List<User> userList;
    private List<User> filteredData;

    private String myUid = "";
    private OnItemClickListener onItemClick;
    private ItemFilter mFilter = new ItemFilter();


    public interface OnItemClickListener {
        void onItemClick(User item);
    }

    public UsersAdapter(Activity activity, List<User> userList, boolean showme,boolean isShowgGrid, OnItemClickListener listener) {
        this.userList = userList;
        this.filteredData = userList;
        this.showme = showme;
        this.isShowgGrid = isShowgGrid;
        this.listener = listener;
        this.activity = activity;
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            myUid = firebaseUser.getUid();
        }
    }


    public void sort() {
        Collections.sort(userList, new Comparator<User>() {
            @Override
            public int compare(User o, User o2) {
                float[] result1 = new float[3];
                Location.distanceBetween(FynderApplication.getInstance().getLat(), FynderApplication.getInstance().getLng(), o.getLatitude(), o.getLongitude(), result1);
                Float distance1 = result1[0];

                float[] result2 = new float[3];
                Location.distanceBetween(FynderApplication.getInstance().getLat(), FynderApplication.getInstance().getLng(), o2.getLatitude(), o2.getLongitude(), result2);
                Float distance2 = result2[0];
                return distance1.compareTo(distance2);
            }
        });
        notifyDataSetChanged();
    }

    @Override
    public UsersAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if(isShowgGrid){
             v = LayoutInflater.from(activity).inflate(R.layout.list_user_new, parent, false);
        }else{
             v = LayoutInflater.from(activity).inflate(R.layout.list_contact, parent, false);
        }
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder mViewHolder, int position) {
        User model = filteredData.get(position);

        mViewHolder.model = model;
        String displayName = "";

        try {
            if (model.getId() != null) {
                displayName = model.getId().equals(myUid) ? activity.getString(R.string.you) :
                        model.getName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        mViewHolder.textDisplayName.setText(displayName);

        float distance = getDistancBetweenTwoPoints(FynderApplication.getInstance().getLat()
                , FynderApplication.getInstance().getLng()
                , model.getLatitude()
                , model.getLongitude());
        DecimalFormat precision = new DecimalFormat("0.00");
//        int[] array = {activity.getResources().getColor(R.color.colorAccent),
//                activity.getResources().getColor(R.color.color_left_chat_background),
//                activity.getResources().getColor(R.color.color_orange)};
//        int randomStr = array[new Random().nextInt(array.length)];

//        mViewHolder.textDistance.setTextColor(randomStr);
        mViewHolder.textDistance.setVisibility(model.getId().equals(myUid) ? View.GONE : View.VISIBLE);
        if (distance < 100) {
            mViewHolder.textDistance.setText("" + precision.format(distance) + " m");
        } else {
            mViewHolder.textDistance.setText("" + precision.format(distance / 1000) + " km");
        }
        if (model.getDescription() == null) {
            mViewHolder.textDescription.setVisibility(View.GONE);
        } else {
            mViewHolder.textDescription.setVisibility(View.VISIBLE);
            mViewHolder.textDescription.setText(model.getDescription());
        }
        mViewHolder.textAge.setVisibility(model.getBirthday() == null ? View.GONE : View.VISIBLE);
        mViewHolder.textSex.setVisibility(model.getGender() == null ? View.GONE : View.VISIBLE);
        mViewHolder.textLocation.setVisibility(model.getCity() == null ? View.GONE : View.VISIBLE);
        int age = model.getBirthday() != null ?
                MyDateUtil.getDiffYears(System.currentTimeMillis(), model.getBirthday()) : 0;
        String stringAge = age == 0 ? "" : String.valueOf(age);
        mViewHolder.textAge.setText(stringAge);
        mViewHolder.textSex.setText(model.getGender());
        mViewHolder.textLocation.setText(model.getCity());
//        try {
//            if(model.getGender().equalsIgnoreCase("FEMALE")) {
//                mViewHolder.textAge.setTextColor(activity.getResources().getColor(R.color.colorAccent));
//            }
//            else {
//                mViewHolder.textAge.setTextColor(activity.getResources().getColor(R.color.color_blue));
//            }
//        } catch (Resources.NotFoundException e) {
//
//        }

        mViewHolder.imageSelected.setVisibility(model.isSelected() ? View.VISIBLE : View.GONE);
        if (model.getGender() == null) {
            mViewHolder.imageGender.setVisibility(View.GONE);
            mViewHolder.imageGender.setImageBitmap(null);
        } else {
            mViewHolder.imageGender.setVisibility(View.VISIBLE);
            mViewHolder.imageGender.setImageResource(model.getGender().equalsIgnoreCase("FEMALE") ?
                    R.drawable.icon_female : R.drawable.icon_male);
        }
        mViewHolder.imageGender.setVisibility(model.isSelected() ? View.GONE : View.VISIBLE);
        String mediaId = model.getImageUrl();
        String imageUrl = (null == mediaId || mediaId.isEmpty()) ? "" : mediaId;
      if(!isShowgGrid){
          RoundingParams circle = RoundingParams.asCircle()
                  .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY);
          mViewHolder.imageProfile.getHierarchy().setRoundingParams(circle);
      }
        mViewHolder.imageProfile.setImageURI(imageUrl);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return filteredData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        View rootLayout;
        SimpleDraweeView imageProfile;
        TextView textDisplayName;
        TextView textDistance;
        TextView textDescription;
        ImageView imageSelected;
        ImageView imageGender;
        TextView textAge, textSex, textLocation;
        User model;

        public MyViewHolder(View item) {
            super(item);
            rootLayout = item.findViewById(R.id.root_layout);
            imageProfile = (SimpleDraweeView) item.findViewById(R.id.image_profile);
            imageProfile.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
            imageProfile.getHierarchy().setPlaceholderImage(R.drawable.ic_profile_default);
            textDisplayName = (TextView) item.findViewById(R.id.text_display_name);
            textDistance = (TextView) item.findViewById(R.id.text_distance);
            textDescription = (TextView) item.findViewById(R.id.text_description);
            imageSelected = (ImageView) item.findViewById(R.id.image_tick);
            imageGender = (ImageView) item.findViewById(R.id.image_gender);
            textAge = (TextView) item.findViewById(R.id.user_age);
            textSex = (TextView) item.findViewById(R.id.user_sex);
            textLocation = (TextView) item.findViewById(R.id.user_location);
            rootLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (model == null) {
                        return;
                    }
                    if (listener != null) {
                        listener.onItemClick(model);
                    }

                }
            });
        }
    }

    public Filter getFilter() {
        return mFilter;
    }

    private float getDistancBetweenTwoPoints(double lat1, double lon1, double lat2, double lon2) {

        float[] distance = new float[2];

        Location.distanceBetween(lat1, lon1,
                lat2, lon2, distance);

        return distance[0];
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<User> list = userList;

            int count = list.size();
            final ArrayList<User> nlist = new ArrayList<>(count);

            User filterableString;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i);
                String name = filterableString.getName();
                if (name != null && name.toLowerCase().contains(filterString)) {
                    nlist.add(filterableString);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<User>) results.values;
            notifyDataSetChanged();
        }

    }


}

