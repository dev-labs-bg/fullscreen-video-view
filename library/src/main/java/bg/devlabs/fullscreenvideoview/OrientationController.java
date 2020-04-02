package bg.devlabs.fullscreenvideoview;

/**
 * Controls the orientation change.
 */
public interface OrientationController {

    /**
     * Checks if the video view is in landscape.
     *
     * @return A boolean which indicates whether the view is landscape or portrait
     */
    boolean isLandscape();

    /**
     * Toggles the fullscreen mode.
     */
    void toggleFullscreen();
}
