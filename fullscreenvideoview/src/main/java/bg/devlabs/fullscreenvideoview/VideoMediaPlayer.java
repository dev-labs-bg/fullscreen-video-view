package bg.devlabs.fullscreenvideoview;

import android.media.MediaPlayer;

/**
 * Created by Slavi Petrov on 20.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
public class VideoMediaPlayer extends MediaPlayer {
    private FullscreenVideoView fullscreenVideoView;
    // TODO: Implement
    private boolean canPause = true;
    private boolean canSeekBackward = true;
    private boolean canSeekForward = true;

    VideoMediaPlayer(FullscreenVideoView fullscreenVideoView) {
        super();
        this.fullscreenVideoView = fullscreenVideoView;
    }

    public int getBufferPercentage() {
        return 0;
    }

    public boolean canPause() {
        return canPause;
    }

    public boolean canSeekBackward() {
        return canSeekBackward;
    }

    public boolean canSeekForward() {
        return canSeekForward;
    }

    public void toggleFullScreen() {
        fullscreenVideoView.toggleFullscreen();
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
}
