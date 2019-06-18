package bg.devlabs.fullscreenvideoview;

import android.annotation.TargetApi;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
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
    private boolean showPlaybackSpeedButton = false;

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

    public boolean showPlaybackSpeedButton() {
        return showPlaybackSpeedButton;
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
            if (fullscreenVideoView != null) {
                fullscreenVideoView.hideThumbnail();
            }
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

    public void disablePause() {
        this.canPause = false;
    }

    public void addSeekForwardButton() {
        this.showSeekForwardButton = true;
    }

    public void addSeekBackwardButton() {
        this.showSeekBackwardButton = true;
    }

    public void addPlaybackSpeedButton() {
        this.showPlaybackSpeedButton = true;
    }

    void setCanSeekBackward(boolean canSeekBackward) {
        this.showSeekBackwardButton = canSeekBackward;
    }

    void setCanSeekForward(boolean canSeekForward) {
        this.showSeekForwardButton = canSeekForward;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void changePlaybackSpeed(float speed) {
        PlaybackParams playbackParams = new PlaybackParams();
        playbackParams.setSpeed(speed);
        setPlaybackParams(playbackParams);
    }
}
