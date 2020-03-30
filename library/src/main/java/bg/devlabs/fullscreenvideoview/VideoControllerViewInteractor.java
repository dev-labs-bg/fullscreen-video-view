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
}
