package bg.devlabs.fullscreenvideoview;

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

    void changePlaybackSpeed(float speed);

    void onPauseResume();
}
