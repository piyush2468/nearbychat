package com.rndtechnosoft.fynder.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.rndtechnosoft.fynder.R;

/**
 * Created by Ravi on 1/25/2017.
 */

public class PttDialog extends Dialog{
    private ImageView image2;
    private ImageView image3;
    private ImageView image4;

    private int imageIndex = 0;
    private Handler handler = new Handler();
    private long delay = 250;
    private Runnable imageChanger = new Runnable() {
        @Override
        public void run() {
            int mod = imageIndex++ % 4;
            image2.setVisibility(mod > 0 ? View.VISIBLE : View.GONE);
            image3.setVisibility(mod > 1 ? View.VISIBLE : View.GONE);
            image4.setVisibility(mod > 2 ? View.VISIBLE : View.GONE);
            handler.postDelayed(imageChanger, delay);
        }
    };

    public PttDialog(Context context) {
        super(context);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        View view = getLayoutInflater().inflate(R.layout.ptt_popup, null);
        setContentView(view);

        image2 = (ImageView) view.findViewById(R.id.image_ptt_2);
        image3 = (ImageView) view.findViewById(R.id.image_ptt_3);
        image4 = (ImageView) view.findViewById(R.id.image_ptt_4);

        setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                handler.removeCallbacks(imageChanger);
                handler.postDelayed(imageChanger, delay);
            }
        });

        setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                handler.removeCallbacks(imageChanger);
            }
        });

        setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                handler.removeCallbacks(imageChanger);
            }
        });
    }

}
