/*
 * Copyright 2017 Dev Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bg.devlabs.fullscreenvideoview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import bg.devlabs.fullscreenvideoview.playbackspeed.PlaybackSpeedPopupMenuListener;
import bg.devlabs.fullscreenvideoview.orientation.OrientationManager;
import bg.devlabs.fullscreenvideoview.playbackspeed.PlaybackSpeedOptions;

import static android.view.View.INVISIBLE;

/**
 * Created by Slavi Petrov on 04.06.2018
 * Dev Labs
 * slavi@devlabs.bg
 */
class ButtonManager {
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
    // Other
    private WeakReference<OrientationManager> orientationHelper;
    private WeakReference<VideoMediaPlayer> videoMediaPlayer;
    private PlaybackSpeedManager playbackSpeedManager;

    ButtonManager(Context context,
                  ImageButton startPauseButton,
                  ImageButton ffwdButton,
                  ImageButton rewButton,
                  ImageButton fullscreenButton,
                  TextView playbackSpeedButton) {

        this.exitFullscreenDrawable = ContextCompat.getDrawable(
                context,
                R.drawable.ic_fullscreen_exit_white_48dp
        );
        this.enterFullscreenDrawable = ContextCompat.getDrawable(
                context,
                R.drawable.ic_fullscreen_white_48dp
        );
        this.playDrawable = ContextCompat.getDrawable(
                context,
                R.drawable.ic_play_arrow_white_48dp
        );
        this.pauseDrawable = ContextCompat.getDrawable(
                context,
                R.drawable.ic_pause_white_48dp
        );
        this.fastForwardDrawable = ContextCompat.getDrawable(
                context,
                R.drawable.ic_fast_forward_white_48dp
        );
        this.rewindDrawable = ContextCompat.getDrawable(
                context,
                R.drawable.ic_fast_rewind_white_48dp
        );

        this.startPauseButton = new WeakReference<>(startPauseButton);
        this.ffwdButton = new WeakReference<>(ffwdButton);
        this.rewButton = new WeakReference<>(rewButton);
        this.fullscreenButton = new WeakReference<>(fullscreenButton);

        playbackSpeedManager = new PlaybackSpeedManager(context, playbackSpeedButton);
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
                R.styleable.VideoControllerView_enter_fullscreen_drawable
        );

        if (enterDrawable != null) {
            enterFullscreenDrawable = enterDrawable;
        }

        fullscreenButton.get().setImageDrawable(enterFullscreenDrawable);

        Drawable exitDrawable = a.getDrawable(
                R.styleable.VideoControllerView_exit_fullscreen_drawable
        );

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

    public void setOrientationHelper(OrientationManager orientationManager) {
        this.orientationHelper = new WeakReference<>(orientationManager);
    }

    public void setVideoMediaPlayer(VideoMediaPlayer videoMediaPlayer) {
        this.videoMediaPlayer = new WeakReference<>(videoMediaPlayer);
    }

    public void updatePlaybackSpeedText(String text) {
        playbackSpeedManager.setPlaybackSpeedText(text);
    }

    public void setFullscreenButtonClickListener(View.OnClickListener onClickListener) {
        fullscreenButton.get().requestFocus();
        fullscreenButton.get().setOnClickListener(onClickListener);
    }

    public void setStartPauseButtonClickListener(View.OnClickListener onClickListener) {
        startPauseButton.get().requestFocus();
        startPauseButton.get().setOnClickListener(onClickListener);
    }

    public void hideFullscreenButton() {
        fullscreenButton.get().setVisibility(View.GONE);
    }

    public void setupButtonsVisibility() {
        if (startPauseButton != null && !videoMediaPlayer.get().canPause()) {
            startPauseButton.get().setEnabled(false);
            startPauseButton.get().setVisibility(INVISIBLE);
        }

        if (rewButton != null && !videoMediaPlayer.get().showSeekBackwardButton()) {
            rewButton.get().setEnabled(false);
            rewButton.get().setVisibility(INVISIBLE);
        }

        if (ffwdButton != null && !videoMediaPlayer.get().showSeekForwardButton()) {
            ffwdButton.get().setEnabled(false);
            ffwdButton.get().setVisibility(INVISIBLE);
        }

        playbackSpeedManager.hidePlaybackButton(videoMediaPlayer.get().showPlaybackSpeedButton());
    }

    public void requestStartPauseButtonFocus() {
        if (startPauseButton != null) {
            startPauseButton.get().requestFocus();
        }
    }

    public void setButtonsEnabled(boolean isEnabled) {
        if (startPauseButton != null) {
            startPauseButton.get().setEnabled(isEnabled);
        }

        if (ffwdButton != null) {
            ffwdButton.get().setEnabled(isEnabled);
        }

        if (rewButton != null) {
            rewButton.get().setEnabled(isEnabled);
        }

        playbackSpeedManager.setPlaybackSpeedButtonEnabled(isEnabled);
    }

    public void setFfwdButtonOnClickListener(View.OnClickListener onClickListener) {
        ffwdButton.get().setOnClickListener(onClickListener);
    }

    public void setRewButtonOnClickListener(View.OnClickListener onClickListener) {
        rewButton.get().setOnClickListener(onClickListener);
    }

    public void setPlaybackSpeedPopupMenuListener(PlaybackSpeedPopupMenuListener listener) {
        playbackSpeedManager.setPlaybackSpeedButtonOnClickListener(listener);
    }

    public void setPlaybackSpeedOptions(PlaybackSpeedOptions playbackSpeedOptions) {
        playbackSpeedManager.setPlaybackSpeedOptions(playbackSpeedOptions);
    }
}
