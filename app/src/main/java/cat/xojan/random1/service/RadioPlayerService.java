package cat.xojan.random1.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.IOException;

import cat.xojan.random1.R;
import cat.xojan.random1.commons.PlayerUtil;

public class RadioPlayerService extends Service {

    public static final String TAG = RadioPlayerService.class.getSimpleName();
    public static final String EXTRA_URL = "extra_url";

    private MediaPlayer mMediaPlayer;
    private Handler mHandler;
    private IBinder mBinder;
    private Listener mListener;

    public void registerClient(Listener listener) {
        mListener = listener;
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public void pause() {
        mMediaPlayer.pause();
    }

    public void start() {
        mMediaPlayer.start();
    }

    public void removeCallbacks() {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    public void seekTo(int currentPosition) {
        mMediaPlayer.seekTo(currentPosition);
    }

    public class RadioPlayerServiceBinder extends Binder {
        public RadioPlayerService getServiceInstance(){
            return RadioPlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        // The service is being created
        mHandler = new Handler();
        mBinder = new RadioPlayerServiceBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        String url = intent.getStringExtra(EXTRA_URL);
        if (mMediaPlayer == null) {
            startMediaPlayer(url);
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder ;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        stopMediaPlayer();
    }

    /**
     * Update timer on seek bar.
     */
    public void updateSeekBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private void startMediaPlayer(String url) {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            Log.d(TAG, "setDataSource: " + url);
            mMediaPlayer.setDataSource(url);
        } catch (IOException e) {
            Crashlytics.logException(e);
            return;
        }
        mMediaPlayer.prepareAsync();
        Log.d(TAG, "prepareAsync");
        mMediaPlayer.setOnPreparedListener(new MediaPlayerPreparedListener());
        mMediaPlayer.setOnBufferingUpdateListener(new BufferingUpdateListener());
        mMediaPlayer.setOnCompletionListener(new MediaPlayerCompletionListener());
    }

    private void stopMediaPlayer() {
        if (mMediaPlayer != null) {
            Log.d(TAG, "stopMediaPlayer");
            mMediaPlayer.stop();
            mHandler.removeCallbacks(mUpdateTimeTask);
            mMediaPlayer.release();
        }
    }

    /**
     * Background Runnable thread.
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mMediaPlayer.getDuration();
            int currentDuration = mMediaPlayer.getCurrentPosition();
            int progress = PlayerUtil.getProgressPercentage(currentDuration, totalDuration);
            mListener.progressUpdate(progress, currentDuration);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    private class MediaPlayerPreparedListener implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mp) {
            Log.d(TAG, "onPrepared");
            mListener.onPrepared(mMediaPlayer.getDuration());
            mMediaPlayer.start();
            updateSeekBar();
        }
    }

    private class BufferingUpdateListener implements MediaPlayer.OnBufferingUpdateListener {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            if (mMediaPlayer.isPlaying()) {
                mListener.updateBufferProgress(percent);;
            }
        }
    }

    private class MediaPlayerCompletionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mListener.updateButton(R.drawable.ic_play_arrow);
            mMediaPlayer.seekTo(0);
        }
    }

    public interface Listener {

        void onPrepared(int duration);

        void progressUpdate(int progress, int currentDuration);

        void updateBufferProgress(int percent);

        void updateButton(int ic_play_arrow);
    }
}