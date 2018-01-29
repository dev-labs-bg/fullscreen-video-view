package bg.devlabs.fullscreenvideoview;

import android.media.MediaPlayer;
import android.support.annotation.Nullable;

/**
 * Created by Slavi Petrov on 20.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
class VideoMediaPlayer extends MediaPlayer {
    @Nullable
    private FullscreenVideoView fullscreenVideoView;
    private boolean isAutoStartEnabled;
    private boolean canPause = true;
    private boolean canSeekBackward = true;
    private boolean canSeekForward = true;

    VideoMediaPlayer(@Nullable FullscreenVideoView fullscreenVideoView) {
        this.fullscreenVideoView = fullscreenVideoView;
    }

    static int getBufferPercentage() {
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
        if (fullscreenVideoView != null) {
            fullscreenVideoView.toggleFullscreen();
        }
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

    boolean isAutoStartEnabled() {
        return isAutoStartEnabled;
    }

    void enableAutoStart() {
        isAutoStartEnabled = true;
    }

    void setPauseEnabled(boolean canPause) {
        this.canPause = canPause;
    }

    void setCanSeekBackward(boolean canSeekBackward) {
        this.canSeekBackward = canSeekBackward;
    }

    void setCanSeekForward(boolean canSeekForward) {
        this.canSeekForward = canSeekForward;
    }
}
