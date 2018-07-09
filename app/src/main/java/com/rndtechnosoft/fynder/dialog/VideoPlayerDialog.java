package com.rndtechnosoft.fynder.dialog;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.rndtechnosoft.fynder.R;

import java.io.File;

/**
 * Created by Ravi on 3/20/2017.
 */

public class VideoPlayerDialog extends AlertDialog {
    private final String TAG = "VideoPlayer";
    private VideoView myVideoView;
    private SeekBar videoSeekBar;
    private TextView videoTextProgress;
    private ImageButton playButton;
    private Activity activity;
    private String url;
    private String filePath;
    private ProgressBar progressBar;
    private Handler myHandler = new Handler();

    public VideoPlayerDialog(final Activity activity, String filePath, String url){
        super(activity);
        this.activity = activity;
        this.filePath = filePath;
        this.url = url;
        if(url == null){
            Toast.makeText(activity, activity.getString(R.string.error_while_opening_video), Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(activity);
        View alertView = inflater.inflate(R.layout.dialog_video_player, null);
        videoSeekBar = (SeekBar) alertView.findViewById(R.id.video_seek_bar);
        videoTextProgress = (TextView) alertView.findViewById(R.id.video_text_progress);
        progressBar = (ProgressBar) alertView.findViewById(R.id.progress_bar);
        playButton = (ImageButton) alertView.findViewById(R.id.play_button);
        myVideoView = (VideoView) alertView.findViewById(R.id.video_view);
        myVideoView.setZOrderOnTop(true);

        prepareVideo();
        videoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                int totalDuration = myVideoView.getDuration();
                int currentPosition = progressToTimer(seekBar.getProgress(), totalDuration);
                // forward or backward to certain seconds
                myVideoView.seekTo(currentPosition);
                myHandler.postDelayed(mUpdateTimeTask, 100);
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!myVideoView.isPlaying()) {
                    Log.i(TAG, "Playing....!!");
                    myVideoView.start();
                    myHandler.postDelayed(mUpdateTimeTask, 100);
                    playButton.setImageResource(R.drawable.ic_pause);
                } else {
                    Log.i(TAG, "Stop..!!");
                    myVideoView.pause();
                    myHandler.removeCallbacks(mUpdateTimeTask);
                    playButton.setImageResource(R.drawable.ic_play);
                }
            }
        });

        alertView.findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setView(alertView);

    }

    private void prepareVideo() {

//        myVideoView.setVideoURI(uriVideoSource);
        if(filePath!= null && new File(filePath).exists()) {
            myVideoView.setVideoPath(filePath);
            Log.i(TAG,"Play from local device: "+filePath);
        }else{
            Log.i(TAG,"Play from url: "+url);
            myVideoView.setVideoURI(Uri.parse(url));
        }

        myVideoView.setOnCompletionListener(myVideoViewCompletionListener);
        myVideoView.setOnPreparedListener(MyVideoViewPreparedListener);
        myVideoView.setOnErrorListener(myVideoViewErrorListener);

        myVideoView.requestFocus();
        myVideoView.start();
        myHandler.postDelayed(mUpdateTimeTask, 100);
        playButton.setImageResource(R.drawable.ic_pause);
    }

    private MediaPlayer.OnCompletionListener myVideoViewCompletionListener =
            new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer arg0) {
                    Log.i(TAG,"On completed");
                    myHandler.removeCallbacks(mUpdateTimeTask);
                    videoSeekBar.setProgress(0);
                    videoTextProgress.setText(getProgressText(0, myVideoView.getDuration()));
                    playButton.setImageResource(R.drawable.ic_play);
                }
            };

    private MediaPlayer.OnPreparedListener MyVideoViewPreparedListener =
            new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.i(TAG,"On prepared");
                    progressBar.setVisibility(View.GONE);
                    long duration = myVideoView.getDuration(); //in millisecond
                    videoTextProgress.setVisibility(View.VISIBLE);
                    videoTextProgress.setText(getProgressText(0, duration));
                }
            };

    private MediaPlayer.OnErrorListener myVideoViewErrorListener =
            new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e(TAG,"On Error");

                    String errWhat = "";
                    switch (what) {
                        case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                            errWhat = "MEDIA_ERROR_UNKNOWN";
                            break;
                        case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                            errWhat = "MEDIA_ERROR_SERVER_DIED";
                            break;
                        default:
                            errWhat = "unknown what";
                    }

                    String errExtra = "";
                    switch (extra) {
                        case MediaPlayer.MEDIA_ERROR_IO:
                            errExtra = "MEDIA_ERROR_IO";
                            break;
                        case MediaPlayer.MEDIA_ERROR_MALFORMED:
                            errExtra = "MEDIA_ERROR_MALFORMED";
                            break;
                        case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                            errExtra = "MEDIA_ERROR_UNSUPPORTED";
                            break;
                        case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                            errExtra = "MEDIA_ERROR_TIMED_OUT";
                            break;
                        default:
                            errExtra = "...others";

                    }

                    Toast.makeText(activity,
                            "Error!!!\n" +
                                    "what: " + errWhat + "\n" +
                                    "extra: " + errExtra,
                            Toast.LENGTH_LONG).show();
                    dismiss();
                    return true;
                }
            };

    private String getProgressText(long currentDur, long totalDur) {
        return milliSecondsToTimer(currentDur) + "/" + milliSecondsToTimer(totalDur);
    }

    private int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
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

    private int getProgressPercentage(int currentDuration, int totalDuration) {
        int currentSeconds = (currentDuration / 1000);
        int totalSeconds = (totalDuration / 1000);

        // calculating percentage
        Double percentage = (((double) currentSeconds) / totalSeconds) * 100;

        // return percentage
        return percentage.intValue();
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            int startTime = myVideoView.getCurrentPosition();
            int totalTime = myVideoView.getDuration();
            videoTextProgress.setText(getProgressText(startTime, totalTime));
            videoSeekBar.setProgress(getProgressPercentage(startTime, totalTime));
            myHandler.postDelayed(this, 100);
        }
    };



    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        myHandler.removeCallbacks(mUpdateTimeTask);
        Log.i(TAG, "on detached from window");
    }
}
