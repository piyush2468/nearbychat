package com.rndtechnosoft.fynder.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.rndtechnosoft.fynder.model.Item;

/**
 * Created by Ravi on 11/16/2016.
 */

public class ItemAdapter extends ArrayAdapter<Item> {

    private Context context;
    private Item[] items;

    public ItemAdapter(Context context, Item[] items) {
        super(context, android.R.layout.select_dialog_item,android.R.id.text1,items);
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Use super class to create the View
        View v = super.getView(position, convertView, parent);
        TextView tv = (TextView)v.findViewById(android.R.id.text1);

        //Put the image on the TextView
        tv.setCompoundDrawablesWithIntrinsicBounds(items[position].index, 0, 0, 0);

        //Add margin between image and text (support various screen densities)
        int dp5 = (int) (5 * context.getResources().getDisplayMetrics().density + 0.5f);
        tv.setCompoundDrawablePadding(dp5);

        tv.setText(items[position].text);

        return v;
    }
}
