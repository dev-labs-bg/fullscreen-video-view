package bg.devlabs.fullscreenvideoview;

/**
 * Serves as a communication layer between the video view and the media player.
 */
public interface VideoMediaPlayer {

    /**
     * Checks if the video is playing.
     *
     * @return true if it's playing, false otherwise
     */
    boolean isPlaying();

    /**
     * Seeks the media player progress to a given duration.
     *
     * @param position The duration used for the seek.
     */
    void seekTo(int position);

    /**
     * Seeks the media player progress by a given duration.
     *
     * @param duration The duration used for the seek. A positive integer when seeking forward and
     *                 a negative one when seeking backwards.
     */
//    void seekBy(int duration);

    /**
     * Checks if the pause of the video is enabled.
     *
     * @return true if it can be paused, false otherwise
     */
    boolean canPause();

    /**
     * Gets the current position of the video.
     */
    int getCurrentPosition();

    /**
     * Gets the video duration.
     */
    int getDuration();

    /**
     * Gets the video buffer percentage.
     */
    int getBufferPercentage();

    /**
     * Changes the playback speed of the media player.
     *
     * @param speed The speed value in float value. The value 1.00 is 1x speed,
     *              2.00 is 2x speed, etc.
     */
    void changePlaybackSpeed(float speed);

    /**
     * Called when a pause or resume event occurs.
     */
    void onPauseResume();
}
