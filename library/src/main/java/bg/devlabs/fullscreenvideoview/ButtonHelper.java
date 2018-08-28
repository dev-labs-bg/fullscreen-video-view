package bg.devlabs.fullscreenvideoview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.widget.ImageButton;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import bg.devlabs.fullscreenvideoview.orientation.OrientationHelper;

/**
 * Created by Slavi Petrov on 04.06.2018
 * Dev Labs
 * slavi@devlabs.bg
 */
public class ButtonHelper {
    // Drawables for the buttons of the controller
    private Drawable exitFullscreenDrawable;
    private Drawable enterFullscreenDrawable;
    private Drawable playDrawable;
    private Drawable pauseDrawable;
    private Drawable fastForwardDrawable;
    private Drawable rewindDrawable;
    // Buttons
    private WeakReference<ImageButton> startPauseButton;
    private WeakReference<ImageButton> ffwdButton;
    private WeakReference<ImageButton> rewButton;
    private WeakReference<ImageButton> fullscreenButton;
    private WeakReference<TextView> playbackSpeedButton;

    private WeakReference<OrientationHelper> orientationHelper;
    private WeakReference<VideoMediaPlayer> videoMediaPlayer;

    ButtonHelper(Context context, ImageButton startPauseButton, ImageButton ffwdButton,
                 ImageButton rewButton, ImageButton fullscreenButton, TextView playbackSpeedButton) {
        this.exitFullscreenDrawable = ContextCompat.getDrawable(context,
                R.drawable.ic_fullscreen_exit_white_48dp);
        this.enterFullscreenDrawable = ContextCompat.getDrawable(context,
                R.drawable.ic_fullscreen_white_48dp);
        this.playDrawable = ContextCompat.getDrawable(context,
                R.drawable.ic_play_arrow_white_48dp);
        this.pauseDrawable = ContextCompat.getDrawable(context,
                R.drawable.ic_pause_white_48dp);
        this.fastForwardDrawable = ContextCompat.getDrawable(context,
                R.drawable.ic_fast_forward_white_48dp);
        this.rewindDrawable = ContextCompat.getDrawable(context,
                R.drawable.ic_fast_rewind_white_48dp);

        this.startPauseButton = new WeakReference<>(startPauseButton);
        this.ffwdButton = new WeakReference<>(ffwdButton);
        this.rewButton = new WeakReference<>(rewButton);
        this.fullscreenButton = new WeakReference<>(fullscreenButton);
        this.playbackSpeedButton = new WeakReference<>(playbackSpeedButton);
    }

    public void setupDrawables(TypedArray typedArray) {
        setupPlayPauseButton(typedArray);
        setupFullscreenButton(typedArray);
        setupRewindButton(typedArray);
        setupFastForwardButton(typedArray);
    }

    private void setupFastForwardButton(TypedArray a) {
        Drawable drawable = a.getDrawable(R.styleable.VideoControllerView_ffwd_drawable);
        if (drawable != null) {
            fastForwardDrawable = drawable;
        }
        ffwdButton.get().setImageDrawable(fastForwardDrawable);
    }

    private void setupRewindButton(TypedArray a) {
        Drawable drawable = a.getDrawable(R.styleable.VideoControllerView_rew_drawable);
        if (drawable != null) {
            rewindDrawable = drawable;
        }
        rewButton.get().setImageDrawable(rewindDrawable);
    }

    private void setupFullscreenButton(TypedArray a) {
        Drawable enterDrawable = a.getDrawable(
                R.styleable.VideoControllerView_enter_fullscreen_drawable);
        if (enterDrawable != null) {
            enterFullscreenDrawable = enterDrawable;
        }
        fullscreenButton.get().setImageDrawable(enterFullscreenDrawable);

        Drawable exitDrawable = a.getDrawable(
                R.styleable.VideoControllerView_exit_fullscreen_drawable);
        if (exitDrawable != null) {
            setExitFullscreenDrawable(exitDrawable);
        }
    }

    private void setupPlayPauseButton(TypedArray a) {
        Drawable drawable = a.getDrawable(R.styleable.VideoControllerView_play_drawable);
        if (drawable != null) {
            playDrawable = drawable;
        }
        startPauseButton.get().setImageDrawable(playDrawable);

        Drawable drawable1 = a.getDrawable(R.styleable.VideoControllerView_pause_drawable);
        if (drawable1 != null) {
            pauseDrawable = drawable1;
        }
    }

    public void setEnterFullscreenDrawable(Drawable enterFullscreenDrawable) {
        if (enterFullscreenDrawable != null) {
            this.enterFullscreenDrawable = enterFullscreenDrawable;
        }
    }

    public void setExitFullscreenDrawable(Drawable exitFullscreenDrawable) {
        if (exitFullscreenDrawable != null) {
            this.exitFullscreenDrawable = exitFullscreenDrawable;
        }
    }

    public void setPlayDrawable(Drawable playDrawable) {
        if (playDrawable != null) {
            this.playDrawable = playDrawable;
        }
    }

    public void setPauseDrawable(Drawable pauseDrawable) {
        if (pauseDrawable != null) {
            this.pauseDrawable = pauseDrawable;
        }
    }

    public void setFastForwardDrawable(Drawable fastForwardDrawable) {
        this.fastForwardDrawable = fastForwardDrawable;
    }

    public void setRewindDrawable(Drawable rewindDrawable) {
        this.rewindDrawable = rewindDrawable;
    }

    public void updateRewindDrawable() {
        if (rewButton.get() == null || videoMediaPlayer.get() == null) {
            return;
        }

        rewButton.get().setImageDrawable(rewindDrawable);
    }

    public void updateFastForwardDrawable() {
        if (ffwdButton.get() == null || videoMediaPlayer.get() == null) {
            return;
        }

        ffwdButton.get().setImageDrawable(fastForwardDrawable);
    }

    public void updatePausePlay() {
        if (startPauseButton.get() == null || videoMediaPlayer.get() == null) {
            return;
        }

        if (videoMediaPlayer.get().isPlaying()) {
            startPauseButton.get().setImageDrawable(pauseDrawable);
        } else {
            startPauseButton.get().setImageDrawable(playDrawable);
        }
    }

    public void updateFullScreenDrawable() {
        if (fullscreenButton.get() == null || orientationHelper.get() == null) {
            return;
        }

        if (orientationHelper.get().isLandscape()) {
            fullscreenButton.get().setImageDrawable(exitFullscreenDrawable);
        } else {
            fullscreenButton.get().setImageDrawable(enterFullscreenDrawable);
        }
    }

    public void setOrientationHelper(OrientationHelper orientationHelper) {
        this.orientationHelper = new WeakReference<>(orientationHelper);
    }

    public void setVideoMediaPlayer(VideoMediaPlayer videoMediaPlayer) {
        this.videoMediaPlayer = new WeakReference<>(videoMediaPlayer);
    }

    public void updatePlaybackSpeedText(String text) {
        playbackSpeedButton.get().setText(text);
    }
}
