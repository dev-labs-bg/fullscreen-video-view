package bg.devlabs.fullscreenvideoview;

/**
 * Created by Slavi Petrov on 20.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
interface IFullscreenVideoView {
    boolean isFullscreen();

    void toggleFullscreen();

    void setOrientation(int screenOrientation);
}
