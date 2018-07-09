package com.rndtechnosoft.fynder.utility;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.rndtechnosoft.fynder.BuildConfig;
import com.rndtechnosoft.fynder.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Ravi on 12/25/2016.
 */

public class PushFCM {
    private final String TAG = "PushFCM";
    private final String URL = "https://fcm.googleapis.com/fcm/send";
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient mClient = new OkHttpClient();
    private List<String> receipts = new ArrayList<>();
    private String title;
    private String serverKey;
    private String imageUrl;
    private String uid;
    private String id;
    private String messageId;
    private String displayName;
    private int type;

    public PushFCM(Activity activity) {
        serverKey = BuildConfig.FCM_SERVER_KEY;
    }

    public PushFCM(Activity activity, int type){
        this(activity);
        this.type = type;
    }

    public PushFCM(Activity activity, int type, String title) {
        this(activity);
        this.title = title;
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void addRecipients(String receipt){
        receipts.add(receipt);
    }

    public void removeRecipients(String receipt){
        receipts.remove(receipt);
    }

    public void clearRecipients(){
        receipts.clear();
    }

    public void setRecipients(String receipt){
        receipts.clear();
        receipts.add(receipt);
    }

    public void setRecipients(List<String> receipts){
                this.receipts.clear();
                this.receipts.addAll(receipts);
           }


    public void setImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }

    public void setId(String id){
        this.id = id;
    }

    public void setUid(String uid){
        this.uid = uid;
    }

    public void setType(int type){
        this.type = type;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setDisplayName(String displayName){
        this.displayName = displayName;
    }

    public void push(final String message, final DatabaseReference ref) {
        if (serverKey == null) {
            Log.e(TAG, "Server key is NULL");
            return;
        }
        if (title == null) {
            Log.e(TAG, "Title is NULL");
            return;
        }
        if (receipts.size() == 0) {
            Log.e(TAG, "Recipients is empty");
            return;
        }
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    JSONObject root = new JSONObject();
                    JSONObject data = new JSONObject();
                    data.put("type", type);
                    data.put("message", message);
                    data.put("title", title);
                    data.put("image_url", imageUrl);
                    data.put("uid",uid);
                    data.put("id", id);
                    data.put("message_id", messageId);
                    data.put("display_name", displayName);
                    root.put("data", data);
                    root.put("registration_ids", new JSONArray(receipts));
                    RequestBody body = RequestBody.create(JSON, root.toString());
                    Request request = new Request.Builder()
                            .url(URL)
                            .post(body)
                            .addHeader("Authorization", "key=" + serverKey)
                            .build();
                    Response response = mClient.newCall(request).execute();
                    String result = response.body().string();
                    Log.d(TAG, "Result: " + result);
                    return result;
                } catch (Exception ex) {
                    Log.e(TAG,"Exception -> "+ex.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject resultJson = new JSONObject(result);
                    int success, failure;
                    success = resultJson.getInt("success");
                    failure = resultJson.getInt("failure");
                    Log.i(TAG, "Message Success: " + success + " , Message Failed: " + failure);
                    if(ref != null){
                        ref.setValue(true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG, "Message Failed, Unknown error occurred.");
                }
            }
        }.execute();
    }

}
