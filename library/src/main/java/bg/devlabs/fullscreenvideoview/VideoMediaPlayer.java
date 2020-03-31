package bg.devlabs.fullscreenvideoview;

import android.media.MediaPlayer;

import bg.devlabs.fullscreenvideoview.model.MediaPlayerError;

/**
 *
 */
public interface VideoMediaPlayer {

    boolean isPlaying();

    void seekBy(int duration);

    boolean canPause();

    int getCurrentPosition();

    int getDuration();

    int getBufferPercentage();

    void seekTo(int position);

    void onMediaPlayerPrepared(
            MediaPlayer mediaPlayer,
            int videoWidth,
            int videoHeight,
            boolean isAutoStartEnabled
    );

    void onMediaPlayerError(MediaPlayerError error);

    void onMediaPlayerCompletion();
}
