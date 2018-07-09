package com.rndtechnosoft.fynder.utility;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.model.Chat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Ravi on 1/29/2017.
 */

public abstract class AudioPlayer {
    private final String TAG = "AudioPlayer";
    private Activity activity;
    private String url;
    private String filePath;
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private TextView textProgress;
    private ProgressBar progressBar;
    private ImageButton playButton;
    private Handler myHandler = new Handler();
    private boolean isPrepared = false;

    public AudioPlayer(Activity activity, String url, String filePath, String myFilePath, SeekBar seekBar,
                       TextView textProgress, ProgressBar progressBar, ImageButton playButton,final Chat chat) {
        mediaPlayer = new MediaPlayer();
        this.activity = activity;
        this.url = url;
        this.filePath = filePath;
        this.seekBar = seekBar;
        this.textProgress = textProgress;
        this.progressBar = progressBar;
        this.playButton = playButton;
        if (myFilePath != null && new File(myFilePath).exists()) {
            Log.i(TAG, "File path of mine (sender) is exist: " + myFilePath);
            setAudioPath(myFilePath);
        } else if (filePath != null && new File(filePath).exists()) {
            Log.i(TAG, "File path is exist: " + filePath);
            setAudioPath(filePath);
        }
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.i(TAG, "on Prepared..");
                isPrepared = true;
                long totalDur = mediaPlayer.getDuration();
                Log.i(TAG, "Total duration: " + totalDur);
                AudioPlayer.this.textProgress.setVisibility(View.VISIBLE);
                AudioPlayer.this.textProgress.setText(getProgressText(0, totalDur));
                chat.setDuration(milliSecondsToTimer(totalDur));
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.i(TAG, "Media player completed.");
                myHandler.removeCallbacks(mUpdateTimeTask);
                AudioPlayer.this.seekBar.setProgress(0);
                AudioPlayer.this.textProgress.setText(getProgressText(0, mediaPlayer.getDuration()));
                onCompleted();
                AudioPlayer.this.playButton.setImageResource(R.drawable.ic_play);
            }
        });
        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                myHandler.removeCallbacks(mUpdateTimeTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                myHandler.removeCallbacks(mUpdateTimeTask);
                int totalDuration = AudioPlayer.this.mediaPlayer.getDuration();
                int currentPosition = progressToTimer(seekBar.getProgress(), totalDuration);
                // forward or backward to certain seconds
                AudioPlayer.this.mediaPlayer.seekTo(currentPosition);
                myHandler.postDelayed(mUpdateTimeTask, 100);
            }
        });
    }

    private void setAudioPath(String audioPath) {
        try {
            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            Log.e(TAG, "Error set file path: " + e.getMessage());
        }
    }

    public void play() {
        if (isPrepared) {
            doPlay();
        } else {
            //download and play action.
            if (url == null) {
                Log.e(TAG, "Url download link is null");
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(new Callback() {
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "error downloading audio: " + e.getMessage());
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }

                public void onResponse(Call call, final Response response) {
                    try {
                        FileOutputStream fos = new FileOutputStream(filePath);
                        fos.write(response.body().bytes());
                        fos.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error after audio download: " + e.getMessage());
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            try {
                                if (!response.isSuccessful()) {
                                    throw new IOException("Failed to download file: " + response);
                                }

                                if (filePath != null && new File(filePath).exists()) {
                                    Log.i(TAG, "File path is exist: " + filePath);
                                    setAudioPath(filePath);
                                    doPlay();
                                }
                            } catch (IOException e) {
                                Log.e(TAG, "Error after audio download: " + e.getMessage());
                            }
                        }
                    });
                }
            });
        }
    }

    private void doPlay() {
        if (!mediaPlayer.isPlaying()) {
            Log.i(TAG, "Playing....!!");
            mediaPlayer.start();
            myHandler.postDelayed(mUpdateTimeTask, 100);
            playButton.setImageResource(R.drawable.ic_pause);
        } else {
            Log.i(TAG, "Stop..!!");
            mediaPlayer.pause();
            myHandler.removeCallbacks(mUpdateTimeTask);
            playButton.setImageResource(R.drawable.ic_play);
        }
    }

    private String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    /**
     * Function to get Progress percentage
     *
     * @param currentDuration
     * @param totalDuration
     */
    private int getProgressPercentage(int currentDuration, int totalDuration) {
        int currentSeconds = (currentDuration / 1000);
        int totalSeconds = (totalDuration / 1000);

        // calculating percentage
        Double percentage = (((double) currentSeconds) / totalSeconds) * 100;

        // return percentage
        return percentage.intValue();
    }

    /**
     * Function to change progress to timer
     *
     * @param progress      -
     * @param totalDuration returns current duration in milliseconds
     */
    private int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }

    private String getProgressText(long currentDur, long totalDur) {
        return milliSecondsToTimer(currentDur) + "/" + milliSecondsToTimer(totalDur);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            int startTime = mediaPlayer.getCurrentPosition();
            int totalTime = mediaPlayer.getDuration();
            textProgress.setText(getProgressText(startTime, totalTime));
            seekBar.setProgress(getProgressPercentage(startTime, totalTime));
            myHandler.postDelayed(this, 100);
        }
    };

    public abstract void onCompleted();
}
