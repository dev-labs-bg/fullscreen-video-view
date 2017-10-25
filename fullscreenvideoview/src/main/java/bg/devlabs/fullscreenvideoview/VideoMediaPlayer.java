package bg.devlabs.fullscreenvideoview;

import android.media.MediaPlayer;

/**
 * Created by Slavi Petrov on 20.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
public class VideoMediaPlayer extends MediaPlayer {
    private FullscreenVideoView fullscreenVideoView;
    private boolean isAutoStartEnabled;
    // TODO: Implement
    private boolean canPause = true;
    private boolean canSeekBackward = true;
    private boolean canSeekForward = true;

    VideoMediaPlayer(FullscreenVideoView fullscreenVideoView) {
        super();
        this.fullscreenVideoView = fullscreenVideoView;
    }

    int getBufferPercentage() {
        return 0;
    }

    boolean canPause() {
        return canPause;
    }

    boolean canSeekBackward() {
        return canSeekBackward;
    }

    boolean canSeekForward() {
        return canSeekForward;
    }

    void toggleFullScreen() {
        fullscreenVideoView.toggleFullscreen();
    }

    void onPauseResume() {
        if (isPlaying()) {
            pause();
        } else {
            start();
        }
    }

    void onDetach() {
        fullscreenVideoView = null;
        setOnPreparedListener(null);
        stop();
        release();
    }

    void setAutoStartEnabled(boolean isAutoStartEnabled) {
        this.isAutoStartEnabled = isAutoStartEnabled;
    }

    boolean isAutoStartEnabled() {
        return isAutoStartEnabled;
    }
}
