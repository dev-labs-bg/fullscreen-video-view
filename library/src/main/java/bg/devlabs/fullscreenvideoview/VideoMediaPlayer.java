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
    private boolean showSeekBackwardButton = false;
    private boolean showSeekForwardButton = false;

    VideoMediaPlayer(@Nullable FullscreenVideoView fullscreenVideoView) {
        this.fullscreenVideoView = fullscreenVideoView;
    }

    public int getBufferPercentage() {
        return 0;
    }

    public boolean canPause() {
        return canPause;
    }

    public boolean showSeekForwardButton() {
        return showSeekForwardButton;
    }

    public boolean showSeekBackwardButton() {
        return showSeekBackwardButton;
    }

    public void toggleFullScreen() {
        if (fullscreenVideoView != null) {
            fullscreenVideoView.toggleFullscreen();
        }
    }

    public void onPauseResume() {
        if (isPlaying()) {
            pause();
        } else {
            start();
        }
    }

    public void onDetach() {
        fullscreenVideoView = null;
        setOnPreparedListener(null);
        stop();
        release();
    }

    public boolean isAutoStartEnabled() {
        return isAutoStartEnabled;
    }

    public void enableAutoStart() {
        isAutoStartEnabled = true;
    }

    public void setPauseEnabled(boolean canPause) {
        this.canPause = canPause;
    }

    public void addSeekForwardButton() {
        this.showSeekForwardButton = true;
    }

    public void addSeekBackwardButton() {
        this.showSeekBackwardButton = true;
    }
    
    @Deprecated
    void setCanSeekBackward(boolean canSeekBackward) {
        this.showSeekBackwardButton = canSeekBackward;
    }

    @Deprecated
    void setCanSeekForward(boolean canSeekForward) {
        this.showSeekForwardButton = canSeekForward;
    }
}
