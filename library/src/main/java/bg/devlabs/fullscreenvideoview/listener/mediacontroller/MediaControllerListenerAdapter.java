package bg.devlabs.fullscreenvideoview.listener.mediacontroller;

/**
 * Created by Slavi Petrov on 14.08.2019
 * Dev Labs
 * slavi@devlabs.bg
 *
 * This adapter class provides empty implementations of the methods
 * from [{@link MediaControllerListener}]. Any custom listener that cares only about a subset of
 * the methods of this listener can simply subclass this adapter class instead of implementing
 * the interface directly.
 */
@SuppressWarnings("unused")
public class MediaControllerListenerAdapter implements MediaControllerListener {
    @Override
    public void onPlayClicked() {
    }

    @Override
    public void onPauseClicked() {
    }

    @Override
    public void onRewindClicked() {
    }

    @Override
    public void onFastForwardClicked() {
    }

    @Override
    public void onFullscreenClicked() {
    }

    @Override
    public void onSeekBarProgressChanged(long progressMs) {
    }
}
