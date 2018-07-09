package com.rndtechnosoft.fynder.utility;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Created by Shubham on 3/29/2016.
 */
public class ThinTextView extends AppCompatTextView {
    public ThinTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ThinTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ThinTextView(Context context) {
        super(context);
        init();
    }

    public void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "Exo-Black.otf");
        setTypeface(tf, Typeface.NORMAL);
    }

}
