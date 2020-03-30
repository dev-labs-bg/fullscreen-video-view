package bg.devlabs.fullscreenvideoview;

import android.widget.SeekBar;

import static bg.devlabs.fullscreenvideoview.Constants.DEFAULT_CONTROLLER_TIMEOUT;

/**
 * Captures progress bar on seek change event.
 */
public class OnSeekChangeListener implements SeekBar.OnSeekBarChangeListener {

    private VideoControllerViewInteractor interactor;

    OnSeekChangeListener(VideoControllerViewInteractor interactor) {
        this.interactor = interactor;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        interactor.show(Constants.ONE_HOUR_MILLISECONDS);
        interactor.setIsDragging(true);

        // By removing these pending progress messages we make sure
        // that a) we won't update the progress while the user adjusts
        // the seekbar and b) once the user is done dragging the thumb
        // we will post one of these messages to the queue again and
        // this ensures that there will be exactly one message queued up.
        interactor.hide();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser) {
            // We're not interested in programmatically generated changes to
            // the progress bar's position.
            return;
        }

        long duration = interactor.getDuration();
        long newPosition = (duration * progress) / Constants.ONE_MILLISECOND;

        interactor.seekTo((int) newPosition);
        interactor.setCurrentTime((int) newPosition);
        interactor.updateSeekBarProgress(newPosition);
        interactor.hideThumbnail();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        interactor.setIsDragging(false);
        interactor.setProgress();
        interactor.updatePausePlay();
        interactor.show(DEFAULT_CONTROLLER_TIMEOUT);

        // Ensure that progress is properly updated in the future,
        // the call to show() does not guarantee this because it is a
        // no-op if we are already showing.
        interactor.refreshProgress();
    }
}
