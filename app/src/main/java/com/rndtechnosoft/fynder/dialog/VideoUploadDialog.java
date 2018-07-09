package com.rndtechnosoft.fynder.dialog;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.activity.NearbyHomeActivity;
import com.rndtechnosoft.fynder.activity.RoomActivity;
import com.rndtechnosoft.fynder.activity.SingleChatActivity;
import com.rndtechnosoft.fynder.model.Chat;
import com.rndtechnosoft.fynder.utility.MyImageUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Map;

/**
 * Created by Ravi on 3/18/2017.
 */

public class VideoUploadDialog extends AlertDialog {
    private final String TAG = "VideoUpload";
    private VideoView myVideoView;
    private SeekBar videoSeekBar;
    private TextView videoTextProgress;
    private LinearLayout progressUploadView;
    private ImageButton playButton;
    private Button uploadButton;
    private Handler myHandler = new Handler();
    private AppCompatActivity activity;
    private Chat chat;

    public VideoUploadDialog(final AppCompatActivity activity, final String filePath, final DatabaseReference chatRef, final Chat chat) {
        super(activity);
        this.activity = activity;
        this.chat = chat;
        if (filePath == null || chat == null) {
            Log.e(TAG,"Error while opening!!");
            dismiss();
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(activity);
        View alertView = inflater.inflate(R.layout.dialog_video_upload, null);
        TextView textFilePath = (TextView) alertView.findViewById(R.id.text_file_path);
        TextView textFileSize = (TextView) alertView.findViewById(R.id.text_file_size);
        videoSeekBar = (SeekBar) alertView.findViewById(R.id.video_seek_bar);
        videoTextProgress = (TextView) alertView.findViewById(R.id.video_text_progress);
        progressUploadView = (LinearLayout) alertView.findViewById(R.id.progress_view_uploading);
        playButton = (ImageButton) alertView.findViewById(R.id.play_button);
        uploadButton = (Button) alertView.findViewById(R.id.button_upload);
        myVideoView = (VideoView) alertView.findViewById(R.id.vview);
        myVideoView.setZOrderOnTop(true);
//        ImageView imageView = (ImageView) alertView.findViewById(R.id.video_preview);

        prepareVideo(filePath);
        File file = new File(filePath);
        long fileSize = file.length() / 1024;
        textFilePath.setText(filePath);
        textFileSize.setText(size(fileSize));

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

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if(firebaseUser == null){
                    Toast.makeText(activity, activity.getString(R.string.error_uploading_video), Toast.LENGTH_SHORT).show();
                    return;
                }
                progressUploadView.setVisibility(View.VISIBLE);
                uploadButton.setText(activity.getString(R.string.uploading));
                uploadButton.setEnabled(false);
                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND);
                String uid = firebaseUser.getUid();
                Log.i(TAG,"Uid: "+uid);

                final String filename = uid + "/chat/video/" + System.currentTimeMillis();
                Bitmap thumbnail = MyImageUtil.scaleBitmap(bitmap, 150, 150);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] thumbData = baos.toByteArray();
                StorageReference imageRef = FirebaseStorage.getInstance().getReference()
                        .child(filename + ".jpg");
                UploadTask uploadTask = imageRef.putBytes(thumbData);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to upload the thumbnail video: " + e.getMessage());
                        errorUpload();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    @SuppressWarnings("VisibleForTests")
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        final Uri thumbUrl = taskSnapshot.getDownloadUrl();
                        Log.i(TAG, "Success to Upload thumbnail, url: " + thumbUrl);
                        String extension = filePath.substring(filePath.lastIndexOf("."));
                        StorageReference videoRef = FirebaseStorage.getInstance().getReference()
                                .child(filename + "." + extension);
                        UploadTask uploadTask = videoRef.putFile(Uri.fromFile(new File(filePath)));
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Failed to upload the video: " + e.getMessage());
                                errorUpload();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                if(progressUploadView != null) {
                                    progressUploadView.setVisibility(View.GONE);
                                }
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                Chat chatModel = VideoUploadDialog.this.chat;
                                if(downloadUrl == null || chatModel == null || chatRef == null || thumbUrl == null){
                                    errorUpload();
                                    return;
                                }
                                Log.i(TAG, "Success to upload the video, url: " + downloadUrl);
                                chatModel.setUrl(downloadUrl.toString());
                                chatModel.setUrlThumbnail(thumbUrl.toString());
                                DatabaseReference updateRef = chatRef.push();
                                Map<String, Object> postValues = chatModel.toMap();
                                updateRef.updateChildren(postValues);
                                if(activity instanceof SingleChatActivity) {
                                    ((SingleChatActivity) activity).setReplyMessageIdToNull();
                                }else if(activity instanceof RoomActivity) {
                                    ((RoomActivity) activity).setReplyMessageIdToNull();
                                }else if(activity instanceof NearbyHomeActivity) {
                                    ((NearbyHomeActivity) activity).setReplyMessageIdToNull();
                                }
                                dismiss();
                            }
                        });
                    }
                });



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


    private String size(long size) {
        String hrSize = "";
        double m = size / 1024.0;
        DecimalFormat dec = new DecimalFormat("0.00");

        if (m > 1) {
            hrSize = dec.format(m).concat(" MB");
        } else {
            hrSize = dec.format(size).concat(" KB");
        }
        return hrSize;
    }

    private void prepareVideo(String videoPath) {

//        myVideoView.setVideoURI(uriVideoSource);
        myVideoView.setVideoPath(videoPath);

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

                    long duration = myVideoView.getDuration(); //in millisecond
                    videoTextProgress.setVisibility(View.VISIBLE);
                    videoTextProgress.setText(getProgressText(0, duration));
                    if(chat != null){
                        chat.setDuration(milliSecondsToTimer(duration));
                    }
                }
            };

    private MediaPlayer.OnErrorListener myVideoViewErrorListener =
            new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {

                    uploadButton.setVisibility(View.GONE);
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

    private void errorUpload(){
        Toast.makeText(activity, activity.getString(R.string.error_uploading_video), Toast.LENGTH_SHORT).show();
        if(progressUploadView != null) {
            progressUploadView.setVisibility(View.GONE);
        }
        if(uploadButton != null){
            uploadButton.setText(activity.getString(R.string.upload));
            uploadButton.setEnabled(true);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        myHandler.removeCallbacks(mUpdateTimeTask);
        progressUploadView.setVisibility(View.GONE);
        Log.i(TAG, "on detached from window");
    }
}
