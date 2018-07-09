package com.rndtechnosoft.fynder.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rndtechnosoft.fynder.BuildConfig;
import com.rndtechnosoft.fynder.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Ravi on 3/8/2017.
 */

public class AboutActivity extends AppCompatActivity {
    private final String TAG = "About";
    private String TYPE_SHARE;
    private String TYPE_CONTACT_US;
    private String TYPE_RATING;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        TYPE_SHARE = getString(R.string.about_share);
        TYPE_CONTACT_US = getString(R.string.about_contact_us);
        TYPE_RATING = getString(R.string.about_rating);
        TextView textVersion = (TextView) findViewById(R.id.text_version);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view_about);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        String version = String.format(getString(R.string.about_version), BuildConfig.VERSION_NAME);
        textVersion.setText(version);
        //populate list data
        String [] lists= new String[]{TYPE_RATING, TYPE_SHARE, TYPE_CONTACT_US};
        List<String> aboutList = new ArrayList<>(Arrays.asList(lists));
        AboutAdapter adapter = new AboutAdapter(aboutList);
        recyclerView.setAdapter(adapter);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class AboutAdapter extends RecyclerView.Adapter<AboutAdapter.ViewHolder>{
        private List<String> list;

        public AboutAdapter(List<String> list){
            this.list = list;
        }

        @Override
        public AboutAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_about, viewGroup, false);
            ViewHolder viewholder = new ViewHolder(v);
            return viewholder;
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        @Override
        public void onBindViewHolder(AboutAdapter.ViewHolder viewHolder, int position){
            String textString = list.get(position);
            viewHolder.textView.setText(textString);
            viewHolder.textString = textString;
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            public RelativeLayout rootLayout;
            public TextView textView;
            public String textString;

            public ViewHolder(View itemView){
                super(itemView);
                rootLayout = (RelativeLayout) itemView.findViewById(R.id.root_layout);
                textView = (TextView) itemView.findViewById(R.id.text_list_about);
                rootLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(textString.equalsIgnoreCase(TYPE_CONTACT_US)){
                            Log.i(TAG,"Contact us type");
                            String[] to = getResources().getStringArray(R.array.about_email_to);
                            String title = getString(R.string.about_email_title);
                            String subject = getString(R.string.about_email_subject);
                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                            emailIntent.setType("message/rfc822");  //set the email recipient
                            emailIntent.setData(Uri.parse("mailto:"));
                            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                            startActivity(Intent.createChooser(emailIntent, title));
                        }else if(textString.equalsIgnoreCase(TYPE_SHARE)){
                            Log.i(TAG,"Share type");
                            String message = getString(R.string.about_share_message);
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, message);
                            sendIntent.setType("text/plain");
                            startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.about_share_title)));
                        }else if(textString.equalsIgnoreCase(TYPE_RATING)){
                            String appId = BuildConfig.APPLICATION_ID;
                            Log.i(TAG,"Url: "+appId);
                            Uri uri = Uri.parse("market://details?id=" + appId);
                            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                            // To count with Play market backstack, After pressing back button,
                            // to taken back to our application, we need to add following flags to intent.
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                            }else{
                                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                            }
                            try {
                                startActivity(goToMarket);
                            } catch (ActivityNotFoundException e) {
                                startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("http://play.google.com/store/apps/details?id=" + appId)));
                            }
                        }else{
                            Log.e(TAG,"Wrong type: "+textString);
                        }
                    }
                });
            }
        }
    }
    //Do not call onCreate on MainActivity
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
