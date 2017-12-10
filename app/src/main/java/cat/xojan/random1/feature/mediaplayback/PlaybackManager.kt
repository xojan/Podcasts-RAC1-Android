package cat.xojan.random1.feature.mediaplayback

import android.content.Context
import android.os.Bundle
import android.os.ResultReceiver
import android.os.SystemClock
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log

class PlaybackManager(appContext: Context, val queueManager: QueueManager,
                      private val listener: PlaybackStateListener): PlayerListener {

    private val TAG = PlaybackManager::class.simpleName
    val player = Player(appContext, this)

    fun handlePlayRequest(mediaId: String?) {
        Log.d(TAG, "handlePlayRequest: mediaId= " + mediaId)
        val currentMedia = queueManager.getMediaItem(mediaId)
        listener.onPlaybackStart()
        player.play(currentMedia)
    }

    val mediaSessionCallback = object : MediaSessionCompat.Callback() {

        override fun onPlay() {
            super.onPlay()
            Log.d(TAG, "onPlay: ")
        }

        override fun onPause() {
            super.onPause()
            Log.d(TAG, "onPause: ")
            if( player.isPlaying()) {
                player.pause()
            }
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            Log.d(TAG, "onPlayFromMediaId: " + mediaId)
            queueManager.setQueue(mediaId)
            handlePlayRequest(mediaId)
        }

        override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
            super.onCommand(command, extras, cb)
            /*if( COMMAND_EXAMPLE.equalsIgnoreCase(command) ) {
                //Custom command here
            }*/
            Log.d(TAG, "onCommand " + command)
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            Log.d(TAG, "onSeekTo " + pos.toString())
        }
    }

    /*private fun successfullyRetrievedAudioFocus(): Boolean {
        val audioManager = mediaPlaybackService.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val result = audioManager.requestAudioFocus(mediaPlaybackService,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)

        return result == AudioManager.AUDIOFOCUS_GAIN
    }

    private fun setMediaPlaybackState(state: Int) {
        val playbackstateBuilder = PlaybackStateCompat.Builder()
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PAUSE)
        } else {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PLAY)
        }
        playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
        mediaSession.setPlaybackState(playbackstateBuilder.build())
    }*/

    override fun onCompletion() {
    }

    override fun onPlaybackStatusChanged(state: Int) {
        val position: Long = player.getCurrentPosition().toLong()
        val stateBuilder = PlaybackStateCompat.Builder().setActions(getAvailableActions())
        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime())
        listener.updatePlaybackState(stateBuilder.build())

        if (state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_PAUSED) {
            listener.onNotificationRequired()
        }
    }

    override fun onError(error: String) {
    }

    private fun getAvailableActions(): Long {
        var actions = PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT

        actions = if (player.isPlaying()) {
            actions or PlaybackStateCompat.ACTION_PAUSE
        } else {
            actions or PlaybackStateCompat.ACTION_PLAY
        }
        return actions
    }
}