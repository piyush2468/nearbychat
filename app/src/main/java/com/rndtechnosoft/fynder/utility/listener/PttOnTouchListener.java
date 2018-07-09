package com.rndtechnosoft.fynder.utility.listener;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.dialog.PttDialog;

import java.io.File;

/**
 * Created by Ravi on 1/25/2017.
 */

public abstract class PttOnTouchListener implements View.OnTouchListener{
    private final String TAG = "AudioRecord";
    private final String EXT = ".3gp";
    private PttDialog pttDialog;
    private String audioFilename;
    private MediaRecorder recorder = new MediaRecorder();
    private Activity activity;
    private long startTimeRecording;
    private long endTimeRecording;
    private final long minimumDuration = 1500;

    public PttOnTouchListener(Activity activity){
        this.activity = activity;
    }

    private void startRecording() {
        pttDialog = new PttDialog(activity);
        pttDialog.show();
        audioFilename = activity.getCacheDir().getAbsolutePath()+"/"+System.currentTimeMillis()+EXT;
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile(audioFilename);
        recorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                Log.e(TAG,"Media Recorder error: "+what+" , extra: "+extra);
//                Toast.makeText(activity, String.format(activity.getString(R.string.audio_on_error),what,extra)
//                        , Toast.LENGTH_SHORT).show();
            }
        });
        recorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                Log.w(TAG,"Media Recorder info: "+what+" , extra: "+extra);
//                Toast.makeText(activity, String.format(activity.getString(R.string.audio_on_info),what,extra)
//                        , Toast.LENGTH_SHORT).show();
            }
        });
        try {
            recorder.prepare();
            recorder.start();
        } catch (Exception e) {
            Toast.makeText(activity, activity.getString(R.string.audio_error_start), Toast.LENGTH_SHORT).show();
        }
    }
    private void deletePttFile() {
        if (null != audioFilename) {
            File file = new File(audioFilename);
            file.delete();
        }
    }

    private void stopRecording() {
        boolean validRecording = (endTimeRecording - startTimeRecording) > minimumDuration;

//        PttDialog.dismissDialog();
        pttDialog.dismiss();
        if (null != recorder) {
            if (validRecording) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                }
            }
            try {
                recorder.stop();
            } catch (Exception e) {
                validRecording = false;
            } finally {
                recorder.reset();
                recorder.release();
            }
        }
        if (validRecording) {
            onCompleteRecorded(audioFilename);
        } else {
            Toast.makeText(activity, activity.getString(R.string.audio_recording), Toast.LENGTH_SHORT).show();
            deletePttFile();
        }

        audioFilename = null;
    }

    public abstract void onCompleteRecorded(String audioFilePath);

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED)) {
                activity.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                return false;
            }
        }
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            startTimeRecording = System.currentTimeMillis();
            startRecording();
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            endTimeRecording = System.currentTimeMillis();
            stopRecording();
        }
        return false;
    }
}
