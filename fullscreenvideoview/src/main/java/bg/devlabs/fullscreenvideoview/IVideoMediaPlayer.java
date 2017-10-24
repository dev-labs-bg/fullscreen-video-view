package bg.devlabs.fullscreenvideoview;

/**
 * Created by Slavi Petrov on 18.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
public interface IVideoMediaPlayer {
    void start();

    void pause();

    int getDuration();

    int getCurrentPosition();

    void seekTo(int pos);

    boolean isPlaying();

    int getBufferPercentage();

    boolean canPause();

    boolean canSeekBackward();

    boolean canSeekForward();

    boolean isFullScreen();

    void toggleFullScreen();
}
