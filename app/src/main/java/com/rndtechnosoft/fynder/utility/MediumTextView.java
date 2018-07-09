package com.rndtechnosoft.fynder.utility;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Created by Shubham on 3/29/2016.
 */
public class MediumTextView extends AppCompatTextView {
    public MediumTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MediumTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MediumTextView(Context context) {
        super(context);
        init();
    }

    public void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "Ubuntu-R.ttf.otf");
        setTypeface(tf, Typeface.NORMAL);
    }

}
