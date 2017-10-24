package bg.devlabs.fullscreenvideoview;

import android.media.MediaPlayer;

/**
 * Created by Slavi Petrov on 20.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
public class VideoMediaPlayer extends MediaPlayer implements IVideoMediaPlayer {
    private IFullscreenVideoView fullscreenVideoView;
    // TODO: Implement
    private boolean canPause = true;
    private boolean canSeekBackward = true;
    private boolean canSeekForward = true;

    VideoMediaPlayer(IFullscreenVideoView fullscreenVideoView) {
        super();
        this.fullscreenVideoView = fullscreenVideoView;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return canPause;
    }

    @Override
    public boolean canSeekBackward() {
        return canSeekBackward;
    }

    @Override
    public boolean canSeekForward() {
        return canSeekForward;
    }

    @Override
    public boolean isFullScreen() {
        return fullscreenVideoView.isFullscreen();
    }

    @Override
    public void toggleFullScreen() {
        fullscreenVideoView.toggleFullscreen();
    }

    public void clearFullscreenVideoView() {
        fullscreenVideoView = null;
    }
}
