package com.rndtechnosoft.fynder.utility.listener;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.UploadTask;

/**
 * Created by Ravi on 1/29/2017.
 */

public abstract class OnSuccessUploadListener implements OnSuccessListener<UploadTask.TaskSnapshot> {
    private String key;

    public OnSuccessUploadListener(String key){
        this.key = key;
    }

    public abstract void onSuccessUpload(String key, UploadTask.TaskSnapshot taskSnapshot);

    @Override
    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
        onSuccessUpload(key, taskSnapshot);
    }
}
