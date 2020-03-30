package bg.devlabs.fullscreenvideoview;

/**
 * Manages interactions between the VideoControllerView and other classes.
 */
interface VideoControllerViewInteractor {

    /**
     * Remove the controller from the screen.
     */
    void hide();

    /**
     * Show the controller on screen. It will go away automatically after 'timeout' milliseconds
     * of inactivity.
     *
     * @param timeout The timeout in milliseconds. Use 0 to show the controller
     *                until hide() is called.
     */
    void show(int timeout);

    /**
     * Set progress to the SeekBar.
     *
     * @return progress current position
     */
    int setProgress();

    /**
     * Check if the SeekBar is currently being dragged.
     *
     * @return true if it's dragging, otherwise false
     */
    boolean isDragging();

    /**
     * Check if the controller is showing.
     *
     * @return true if it's showing, otherwise false
     */
    boolean isShowing();

    /**
     * Check if the player is playing.
     *
     * @return true if it's playing, otherwise false
     */
    boolean isPlaying();

    /**
     * A setter for the isDragging value.
     *
     * @param isDragging indicates if the progress bar is being dragged or not.
     */
    void setIsDragging(boolean isDragging);

    /**
     * Update the pause/play drawable of the video controller.
     */
    void updatePausePlay();

    /**
     * Refresh the progress bar.
     */
    void refreshProgress();

    /**
     * Get the video duration.
     *
     * @return the video duration
     */
    int getDuration();

    /**
     * Seek to a preferred position.
     *
     * @param position the selected position
     */
    void seekTo(int position);

    /**
     * Update the current time.
     *
     * @param position the selected position
     */
    void setCurrentTime(int position);

    /**
     * Update SeekBar progress.
     *
     * @param position the selected position
     */
    void updateSeekBarProgress(long position);

    /**
     * Hide the video thumbnail image.
     */
    void hideThumbnail();
}
