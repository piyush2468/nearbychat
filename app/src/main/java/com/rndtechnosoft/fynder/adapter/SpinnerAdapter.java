package com.rndtechnosoft.fynder.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.model.Selector;

import java.util.List;

/**
 * Created by Ravi on 3/14/2017.
 */

public class SpinnerAdapter extends ArrayAdapter<Selector> {

    public SpinnerAdapter(Context context, List<Selector> selectorArrayList) {
        super(context, android.R.layout.simple_spinner_item, selectorArrayList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Selector model = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_layout, parent, false);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.text1);
        textView.setText(model.getName());
//        convertView.setBackgroundColor(ContextCompat.getColor(getContext(), model.isSelected() ? R.color.colorPrimary : android.R.color.white));
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        Selector model = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_layout, parent, false);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.text1);
        textView.setText(model.getName());
        convertView.setBackgroundColor(ContextCompat.getColor(getContext(), model.isSelected() ? R.color.colorAccent : android.R.color.white));
        return convertView;
    }
}
