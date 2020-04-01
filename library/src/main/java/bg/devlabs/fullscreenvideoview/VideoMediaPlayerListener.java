package bg.devlabs.fullscreenvideoview;

import android.media.MediaPlayer;

import bg.devlabs.fullscreenvideoview.model.MediaPlayerError;

/**
 *
 */
public interface VideoMediaPlayerListener {

    /**
     *
     * @param mediaPlayer
     * @param videoWidth
     * @param videoHeight
     * @param isAutoStartEnabled
     */
    void onMediaPlayerPrepared(
            MediaPlayer mediaPlayer,
            int videoWidth,
            int videoHeight,
            boolean isAutoStartEnabled
    );

    /**
     *
     * @param error
     */
    void onMediaPlayerError(MediaPlayerError error);

    /**
     *
     */
    void onMediaPlayerCompletion();
}
