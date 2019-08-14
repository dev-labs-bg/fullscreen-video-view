/*
 * Copyright (C) 2006 The Android Open Source Project
 * Modifications Copyright (C) 2017 Dev Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bg.devlabs.fullscreenvideoview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Locale;

import bg.devlabs.fullscreenvideoview.listener.mediacontroller.MediaControllerListener;
import bg.devlabs.fullscreenvideoview.playbackspeed.PlaybackSpeedPopupMenuListener;
import bg.devlabs.fullscreenvideoview.orientation.OrientationManager;
import bg.devlabs.fullscreenvideoview.playbackspeed.PlaybackSpeedOptions;

import static bg.devlabs.fullscreenvideoview.Constants.VIEW_TAG_CLICKED;

/**
 * A view containing controls for a MediaPlayer. Typically contains the
 * buttons like "Play/Pause", "Rewind", "Fast Forward" and a progress
 * slider. It takes care of synchronizing the controls with the state
 * of the MediaPlayer.
 * <p>
 * The way to use this class is to instantiate it programmatically.
 * The MediaController will create a default set of controls
 * and put them in a window floating above your application. Specifically,
 * the controls will float above the view specified with setAnchorView().
 * The window will disappear if left idle for three seconds and reappear
 * when the user touches the anchor view.
 * <p>
 * Functions like show() and hide() have no effect when MediaController
 * is created in an xml layout.
 * <p>
 * MediaController will hide and
 * show the buttons according to these rules:
 * <ul>
 * <li> The "previous" and "next" buttons are hidden until setPrevNextListeners()
 * has been called
 * <li> The "previous" and "next" buttons are visible but disabled if
 * setPrevNextListeners() was called with null listeners
 * <li> The "rewind" and "fast-forward" buttons are shown unless requested
 * otherwise by using the MediaController(Context, boolean) constructor
 * with the boolean set to false
 * </ul>
 */
@SuppressWarnings("unused")
class VideoControllerView extends FrameLayout {
    private static final String TAG = "VideoControllerView";
    private static final int DEFAULT_TIMEOUT = 3000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;

    @Nullable
    private VideoMediaPlayer videoMediaPlayer;
    private TextView endTime;
    private TextView currentTime;
    private boolean isDragging;
    @Nullable
    private Handler handler = new VideoControllerView.MessageHandler(this);
    private SeekBar progress;
    // There are two scenarios that can trigger the SeekBar listener to trigger:
    //
    // The first is the user using the TouchPad to adjust the position of the
    // SeekBar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "isDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    @Nullable
    private SeekBar.OnSeekBarChangeListener seekListener = new OnSeekChangeListener();

    @Nullable
    private MediaControllerListener mediaControllerListener;

    private ButtonManager buttonManager;
    private int progressBarColor = Color.WHITE;

    private int fastForwardDuration = Constants.FAST_FORWARD_DURATION;
    private int rewindDuration = Constants.REWIND_DURATION;

    public VideoControllerView(Context context) {
        super(context);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        layoutInflater.inflate(R.layout.video_controller, this, true);
        init();
    }

    public VideoControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        layoutInflater.inflate(R.layout.video_controller, this, true);
        init();
        setupXmlAttributes(attrs);
    }

    private void init() {
        if (!isInEditMode()) {
            setVisibility(INVISIBLE);
        }

        buttonManager = new ButtonManager(
                getContext(),
                (ImageButton) findViewById(R.id.start_pause_media_button),
                (ImageButton) findViewById(R.id.forward_media_button),
                (ImageButton) findViewById(R.id.rewind_media_button),
                (ImageButton) findViewById(R.id.fullscreen_media_button),
                (TextView) findViewById(R.id.playback_speed_button)
        );

        setupButtonListeners();

        progress = findViewById(R.id.progress_seek_bar);
        if (progress != null) {
            progress.getProgressDrawable().setColorFilter(progressBarColor, PorterDuff.Mode.SRC_IN);
            progress.getThumb().setColorFilter(progressBarColor, PorterDuff.Mode.SRC_IN);
            progress.setOnSeekBarChangeListener(seekListener);
            progress.setMax(1000);
        }

        endTime = findViewById(R.id.time);
        currentTime = findViewById(R.id.time_current);
    }

    private void setupButtonListeners() {
        buttonManager.setStartPauseButtonClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoMediaPlayer != null && mediaControllerListener != null) {
                    if (videoMediaPlayer.isPlaying()) {
                        mediaControllerListener.onPauseClicked();
                    } else {
                        mediaControllerListener.onPlayClicked();
                    }
                }

                doPauseResume();
                show(DEFAULT_TIMEOUT);
            }
        });

        buttonManager.setFullscreenButtonClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaControllerListener != null) {
                    mediaControllerListener.onFullscreenClicked();
                }

                view.setTag(VIEW_TAG_CLICKED);
                doToggleFullscreen();
                show(DEFAULT_TIMEOUT);
            }
        });

        buttonManager.setFfwdButtonOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoMediaPlayer == null) {
                    return;
                }

                if (mediaControllerListener != null) {
                    mediaControllerListener.onFastForwardClicked();
                }

                int pos = videoMediaPlayer.getCurrentPosition();
                pos += fastForwardDuration; // milliseconds
                videoMediaPlayer.seekTo(pos);
                setProgress();

                show(DEFAULT_TIMEOUT);
            }
        });

        buttonManager.setRewButtonOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoMediaPlayer == null) {
                    return;
                }

                if (mediaControllerListener != null) {
                    mediaControllerListener.onRewindClicked();
                }

                int pos = videoMediaPlayer.getCurrentPosition();
                pos -= rewindDuration; // milliseconds
                videoMediaPlayer.seekTo(pos);
                setProgress();

                show(DEFAULT_TIMEOUT);
            }
        });

        buttonManager.setPlaybackSpeedPopupMenuListener(
                new PlaybackSpeedPopupMenuListener() {
                    @Override
                    public void onSpeedSelected(float speed, String text) {
                        // Update the Playback Speed Drawable according to the clicked menu item
                        buttonManager.updatePlaybackSpeedText(text);
                        // Change the Playback Speed of the VideoMediaPlayer
                        if (videoMediaPlayer != null) {
                            videoMediaPlayer.changePlaybackSpeed(speed);
                        }
                        // Hide the VideoControllerView
                        hide();
                    }

                    @Override
                    public void onPopupMenuDismissed() {
                        show();
                    }

                    @Override
                    public void onPopupMenuShown() {
                        // Show the VideoControllerView and until hide is called
                        show(0);
                    }
                }
        );
    }

    private void setupXmlAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(
                attrs,
                R.styleable.VideoControllerView,
                0,
                0
        );
        buttonManager.setupDrawables(typedArray);
        setupProgressBar(typedArray);
        // Recycle the TypedArray
        typedArray.recycle();
    }

    private void setupProgressBar(TypedArray a) {
        int color = a.getColor(R.styleable.VideoControllerView_progress_color, 0);
        if (color != 0) {
            // Set the default color
            progressBarColor = color;
        }
        progress.getProgressDrawable().setColorFilter(progressBarColor, PorterDuff.Mode.SRC_IN);
        progress.getThumb().setColorFilter(progressBarColor, PorterDuff.Mode.SRC_IN);
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    public void show() {
        show(DEFAULT_TIMEOUT);
    }

    /**
     * Change the buttons visibility according to the flags in {@link #videoMediaPlayer}.
     */
    private void setupButtonsVisibility() {
        if (videoMediaPlayer == null) {
            return;
        }

        buttonManager.setupButtonsVisibility();
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     *
     * @param timeout The timeout in milliseconds. Use 0 to show
     *                the controller until hide() is called.
     */
    private void show(int timeout) {
        if (!isShowing()) {
            buttonManager.requestStartPauseButtonFocus();
            setProgress();
            setupButtonsVisibility();
            setVisibility(VISIBLE);
        }

        buttonManager.updatePausePlay();
        buttonManager.updateFullScreenDrawable();

        // Cause the progress bar to be updated even if it's showing.
        // This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        if (handler == null) {
            return;
        }

        handler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = handler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            handler.removeMessages(FADE_OUT);
            handler.sendMessageDelayed(msg, timeout);
        } else {
            handler.removeMessages(FADE_OUT);
        }
    }

    public void updateFullScreenDrawable() {
        buttonManager.updateFullScreenDrawable();
    }

    private boolean isShowing() {
        return getVisibility() == VISIBLE;
    }

    /**
     * Remove the controller from the screen.
     */
    private void hide() {
        try {
            setVisibility(INVISIBLE);
            if (handler != null) {
                handler.removeMessages(SHOW_PROGRESS);
            }
        } catch (IllegalArgumentException ignored) {
            Log.w("MediaController", "already removed");
        }
    }

    private static CharSequence stringForTime(int timeMs) {
        int totalSeconds = timeMs / Constants.ONE_SECOND_MILLISECONDS;
        int seconds = totalSeconds % Constants.ONE_MINUTE_SECONDS;
        int minutes = (totalSeconds / Constants.ONE_MINUTE_SECONDS) % Constants.ONE_MINUTE_SECONDS;
        int hours = totalSeconds / Constants.ONE_HOUR_SECONDS;
        return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
    }

    private int setProgress() {
        if (videoMediaPlayer == null || isDragging) {
            return 0;
        }

        int position = videoMediaPlayer.getCurrentPosition();
        int duration = videoMediaPlayer.getDuration();
        if (progress != null) {
            if (duration > 0) {
                // Use long to avoid overflow
                long pos = Constants.ONE_MILLISECOND * position / duration;
                progress.setProgress((int) pos);
            }

            int percent = videoMediaPlayer.getBufferPercentage();
            progress.setSecondaryProgress(percent * 10);
        }

        if (endTime != null) {
            endTime.setText(stringForTime(duration));
        }

        if (currentTime != null) {
            currentTime.setText(stringForTime(position));
        }

        return position;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performClick();
        show(DEFAULT_TIMEOUT);
        return true;
    }

    private void doPauseResume() {
        if (videoMediaPlayer == null) {
            return;
        }
        videoMediaPlayer.onPauseResume();
        buttonManager.updatePausePlay();
    }

    private void doToggleFullscreen() {
        if (videoMediaPlayer == null) {
            return;
        }

        videoMediaPlayer.toggleFullScreen();
    }

    @Override
    public void setEnabled(boolean enabled) {
        buttonManager.setButtonsEnabled(enabled);
        if (progress != null) {
            progress.setEnabled(enabled);
        }

        setupButtonsVisibility();
        super.setEnabled(enabled);
    }

    public void onDetach() {
        seekListener = null;
        handler = null;
        videoMediaPlayer = null;
        mediaControllerListener = null;
    }

    public void setEnterFullscreenDrawable(Drawable enterFullscreenDrawable) {
        buttonManager.setEnterFullscreenDrawable(enterFullscreenDrawable);
    }

    public void setExitFullscreenDrawable(Drawable exitFullscreenDrawable) {
        buttonManager.setExitFullscreenDrawable(exitFullscreenDrawable);
    }

    public void setProgressBarColor(int progressBarColor) {
        this.progressBarColor = ContextCompat.getColor(getContext(), progressBarColor);
    }

    public void setPlayDrawable(Drawable playDrawable) {
        buttonManager.setPlayDrawable(playDrawable);
    }

    public void setPauseDrawable(Drawable pauseDrawable) {
        buttonManager.setPauseDrawable(pauseDrawable);
    }

    public void setFastForwardDuration(int fastForwardDuration) {
        this.fastForwardDuration = fastForwardDuration * 1000;
    }

    public void setRewindDuration(int rewindDuration) {
        this.rewindDuration = rewindDuration * 1000;
    }

    public void setFastForwardDrawable(Drawable fastForwardDrawable) {
        buttonManager.setFastForwardDrawable(fastForwardDrawable);
    }

    public void setRewindDrawable(Drawable rewindDrawable) {
        buttonManager.setRewindDrawable(rewindDrawable);
    }

    public void setPlaybackSpeedOptions(PlaybackSpeedOptions playbackSpeedOptions) {
        buttonManager.setPlaybackSpeedOptions(playbackSpeedOptions);
    }

    public void setOnMediaControllerListener(MediaControllerListener mediaControllerListener) {
        this.mediaControllerListener = mediaControllerListener;
    }

    public void init(final OrientationManager orientationManager,
                     VideoMediaPlayer videoMediaPlayer,
                     AttributeSet attrs) {

        setupXmlAttributes(attrs);
        this.videoMediaPlayer = videoMediaPlayer;

        buttonManager.setOrientationHelper(orientationManager);
        buttonManager.setVideoMediaPlayer(videoMediaPlayer);
        buttonManager.updatePausePlay();
        buttonManager.updateFullScreenDrawable();
        buttonManager.updateFastForwardDrawable();
        buttonManager.updateRewindDrawable();

        getViewTreeObserver().addOnWindowFocusChangeListener(new ViewTreeObserver.OnWindowFocusChangeListener() {
            @Override
            public void onWindowFocusChanged(boolean hasFocus) {
                if (orientationManager.isLandscape()) {
                    ((Activity) getContext()).getWindow().getDecorView()
                            .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            );
                }
            }
        });
    }

    public void hideProgress() {
        currentTime.setVisibility(View.GONE);
        endTime.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
    }

    public void hideFullscreenButton() {
        buttonManager.hideFullscreenButton();
    }

    private static class MessageHandler extends Handler {
        private final WeakReference<VideoControllerView> view;

        MessageHandler(VideoControllerView view) {
            this.view = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            VideoControllerView view = this.view.get();
            if (view == null || view.videoMediaPlayer == null) {
                return;
            }

            if (msg.what == FADE_OUT) {
                view.hide();
            } else { // SHOW_PROGRESS
                int position = view.setProgress();
                if (!view.isDragging && view.isShowing() && view.videoMediaPlayer.isPlaying()) {
                    Message message = obtainMessage(SHOW_PROGRESS);
                    sendMessageDelayed(message, 1000 - (position % 1000));
                }
            }
        }
    }

    private class OnSeekChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            show(Constants.ONE_HOUR_MILLISECONDS);

            isDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            if (handler != null) {
                handler.removeMessages(SHOW_PROGRESS);
            }
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (videoMediaPlayer == null) {
                return;
            }

            if (!fromUser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = videoMediaPlayer.getDuration();
            long newPosition = (duration * progress) / Constants.ONE_MILLISECOND;
            videoMediaPlayer.seekTo((int) newPosition);
            if (currentTime != null) {
                currentTime.setText(stringForTime((int) newPosition));
            }

            if (mediaControllerListener != null) {
                mediaControllerListener.onSeekBarProgressChanged(newPosition);
            }

            videoMediaPlayer.hideThumbnail();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isDragging = false;
            setProgress();
            buttonManager.updatePausePlay();
            show(DEFAULT_TIMEOUT);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            if (handler != null) {
                handler.sendEmptyMessage(SHOW_PROGRESS);
            }
        }
    }
}