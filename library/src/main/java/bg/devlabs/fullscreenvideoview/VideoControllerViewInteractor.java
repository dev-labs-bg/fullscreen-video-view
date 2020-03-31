package bg.devlabs.fullscreenvideoview;

/**
 * Manages interactions between the VideoControllerView and other classes.
 */
interface VideoControllerViewInteractor {

    /**
     * Removes the controller from the screen.
     */
    void hide();

    /**
     * Shows the controller on screen. It will go away automatically after 'timeout' milliseconds
     * of inactivity.
     *
     * @param timeout The timeout in milliseconds. Use 0 to show the controller
     *                until hide() is called.
     */
    void show(int timeout);

    /**
     * Sets progress to the SeekBar.
     *
     * @return progress current position
     */
    int setProgress();

    /**
     * Checks if the SeekBar is currently being dragged.
     *
     * @return true if it's dragging, otherwise false
     */
    boolean isDragging();

    /**
     * Checks if the controller is showing.
     *
     * @return true if it's showing, otherwise false
     */
    boolean isShowing();

    /**
     * Checks if the player is playing.
     *
     * @return true if it's playing, otherwise false
     */
    boolean isPlaying();

    /**
     * Changes the isDragging value.
     *
     * @param isDragging indicates if the progress bar is being dragged or not.
     */
    void setIsDragging(boolean isDragging);

    /**
     * Updates the pause/play drawable of the video controller.
     */
    void updatePausePlay();

    /**
     * Refreshes the progress bar.
     */
    void refreshProgress();

    /**
     * Gets the video duration.
     *
     * @return the video duration
     */
    int getDuration();

    /**
     * Seeks to a preferred position.
     *
     * @param position the selected position
     */
    void seekTo(int position);

    /**
     * Changes the current time.
     *
     * @param position the selected position
     */
    void setCurrentTime(int position);

    /**
     * Updates the SeekBar progress.
     *
     * @param position the selected position
     */
    void updateSeekBarProgress(long position);

    /**
     * Hides the video thumbnail image.
     */
    void hideThumbnail();
}
