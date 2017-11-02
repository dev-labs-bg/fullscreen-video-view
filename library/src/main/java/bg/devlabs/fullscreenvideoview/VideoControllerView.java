package bg.devlabs.fullscreenvideoview;
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Locale;

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

    VideoMediaPlayer videoMediaPlayer;
    private TextView endTime;
    private TextView currentTime;
    boolean isDragging;
    private Handler handler = new VideoControllerView.MessageHandler(this);
    private SeekBar progress;
    private ImageButton startPauseButton;
    private ImageButton ffwdButton;
    private ImageButton rewButton;
    private ImageButton fullscreenButton;
    private View.OnClickListener pauseListener = new PauseOnClickListener();
    private View.OnClickListener fullscreenListener = new FullscreenOnClickListener();
    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the position of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "isDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private SeekBar.OnSeekBarChangeListener seekListener = new OnSeekChangeListener();
    private View.OnClickListener rewListener = new RewindOnClickListener();
    private View.OnClickListener ffwdListener = new FfwdOnClickListener();
    // Drawables for the buttons of the controller
    private Drawable exitFullscreenDrawable = ContextCompat.getDrawable(getContext(),
            R.drawable.ic_fullscreen_exit_white_48dp);
    private Drawable enterFullscreenDrawable = ContextCompat.getDrawable(getContext(),
            R.drawable.ic_fullscreen_white_48dp);
    private Drawable playDrawable = ContextCompat.getDrawable(getContext(),
            R.drawable.ic_play_arrow_white_48dp);
    private Drawable pauseDrawable = ContextCompat.getDrawable(getContext(),
            R.drawable.ic_pause_white_48dp);
    private Drawable fastForwardDrawable = ContextCompat.getDrawable(getContext(),
            R.drawable.ic_fast_forward_white_48dp);
    private Drawable rewindDrawable = ContextCompat.getDrawable(getContext(),
            R.drawable.ic_fast_rewind_white_48dp);
    private int progressBarColor = Color.WHITE;

    private int fastForwardDuration = Constants.FAST_FORWARD_DURATION;
    private int rewindDuration = Constants.REWIND_DURATION;
    // VideoView interface which is used to communicate with the VideoView
    private FullscreenVideoView fullscreenVideoView;

    public VideoControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        layoutInflater.inflate(R.layout.video_controller, this, true);
        initControllerView();
        setupXmlAttributes(attrs);
    }

    public VideoControllerView(Context context) {
        super(context);
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        layoutInflater.inflate(R.layout.video_controller, this, true);
        initControllerView();
    }

    private void setupXmlAttributes(AttributeSet attrs) {
        TypedArray typedArr = getContext().obtainStyledAttributes(attrs,
                R.styleable.FullscreenVideoView, 0, 0);
        setupPlayPauseButton(typedArr);
        setupFullscreenButton(typedArr);
        setupFastForwardButton(typedArr);
        setupRewindButton(typedArr);
        setupProgressBar(typedArr);
        // Recycle the TypedArray
        typedArr.recycle();
    }

    private void setupProgressBar(TypedArray a) {
        int color = a.getColor(R.styleable.FullscreenVideoView_progress_color, 0);
        if (color != 0) {
            // Set the default color
            progressBarColor = color;
        }
        progress.getProgressDrawable().setColorFilter(progressBarColor, PorterDuff.Mode.SRC_IN);
        progress.getThumb().setColorFilter(progressBarColor, PorterDuff.Mode.SRC_IN);
    }

    private void setupRewindButton(TypedArray a) {
        Drawable drawable = a.getDrawable(R.styleable.FullscreenVideoView_rew_drawable);
        if (drawable != null) {
            rewindDrawable = drawable;
        }
        rewButton.setImageDrawable(rewindDrawable);
    }

    private void setupFastForwardButton(TypedArray a) {
        Drawable drawable = a.getDrawable(R.styleable.FullscreenVideoView_ffwd_drawable);
        if (drawable != null) {
            fastForwardDrawable = drawable;
        }
        ffwdButton.setImageDrawable(fastForwardDrawable);
    }

    private void setupFullscreenButton(TypedArray a) {
        Drawable enterDrawable = a.getDrawable(
                R.styleable.FullscreenVideoView_enter_fullscreen_drawable);
        if (enterDrawable != null) {
            enterFullscreenDrawable = enterDrawable;
        }
        fullscreenButton.setImageDrawable(enterFullscreenDrawable);

        Drawable exitDrawable = a.getDrawable(
                R.styleable.FullscreenVideoView_exit_fullscreen_drawable);
        if (exitDrawable != null) {
            exitFullscreenDrawable = exitDrawable;
        }
    }

    private void setupPlayPauseButton(TypedArray a) {
        Drawable drawable = a.getDrawable(R.styleable.FullscreenVideoView_play_drawable);
        if (drawable != null) {
            playDrawable = drawable;
        }
        startPauseButton.setImageDrawable(playDrawable);

        Drawable drawable1 = a.getDrawable(R.styleable.FullscreenVideoView_pause_drawable);
        if (drawable1 != null) {
            pauseDrawable = drawable1;
        }
    }

    private void initControllerView() {
        if (!isInEditMode()) {
            setVisibility(INVISIBLE);
        }

        startPauseButton = findViewById(R.id.start_pause_media_button);
        if (startPauseButton != null) {
            startPauseButton.requestFocus();
            startPauseButton.setOnClickListener(pauseListener);
        }

        fullscreenButton = findViewById(R.id.fullscreen_media_button);
        if (fullscreenButton != null) {
            fullscreenButton.requestFocus();
            fullscreenButton.setOnClickListener(fullscreenListener);
        }

        ffwdButton = findViewById(R.id.forward_media_button);
        if (ffwdButton != null) {
            ffwdButton.setOnClickListener(ffwdListener);
        }

        rewButton = findViewById(R.id.rewind_media_button);
        if (rewButton != null) {
            rewButton.setOnClickListener(rewListener);
        }

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

    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    public void show() {
        show(DEFAULT_TIMEOUT);
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control interface to be a MediaPlayerControlExt
     */
    @SuppressWarnings("FeatureEnvy")
    private void disableUnsupportedButtons() {
        if (videoMediaPlayer == null) {
            return;
        }

        try {
            if (startPauseButton != null && !videoMediaPlayer.canPause()) {
                startPauseButton.setEnabled(false);
            }
            if (rewButton != null && !videoMediaPlayer.canSeekBackward()) {
                rewButton.setEnabled(false);
                rewButton.setVisibility(INVISIBLE);
            }
            if (ffwdButton != null && !videoMediaPlayer.canSeekForward()) {
                ffwdButton.setEnabled(false);
                ffwdButton.setVisibility(INVISIBLE);
            }
        } catch (IncompatibleClassChangeError ex) {
            // We were given an old version of the interface, that doesn't have
            // the setCanPause/canSeekXYZ methods. This is OK, it just means we
            // assume the media can be paused and seeked, and so we don't disable
            // the buttons.
            ex.printStackTrace();
        }
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
            setProgress();
            if (startPauseButton != null) {
                startPauseButton.requestFocus();
            }
            disableUnsupportedButtons();
            setVisibility(VISIBLE);
        }

        updatePausePlay();
        updateFullScreenDrawable();

        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        handler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = handler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            handler.removeMessages(FADE_OUT);
            handler.sendMessageDelayed(msg, timeout);
        }
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
            handler.removeMessages(SHOW_PROGRESS);
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
            int percent = VideoMediaPlayer.getBufferPercentage();
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

    private void updateRewindDrawable() {
        if (rewButton == null || videoMediaPlayer == null) {
            return;
        }

        rewButton.setImageDrawable(rewindDrawable);
    }

    private void updateFastForwardDrawable() {
        if (ffwdButton == null || videoMediaPlayer == null) {
            return;
        }

        ffwdButton.setImageDrawable(fastForwardDrawable);
    }

    private void updatePausePlay() {
        if (startPauseButton == null || videoMediaPlayer == null) {
            return;
        }

        if (videoMediaPlayer.isPlaying()) {
            startPauseButton.setImageDrawable(pauseDrawable);
        } else {
            startPauseButton.setImageDrawable(playDrawable);
        }
    }

    public void updateFullScreenDrawable() {
        if (fullscreenButton == null || videoMediaPlayer == null) {
            return;
        }

        if (fullscreenVideoView.isLandscape()) {
            fullscreenButton.setImageDrawable(exitFullscreenDrawable);
        } else {
            fullscreenButton.setImageDrawable(enterFullscreenDrawable);
        }
    }

    private void doPauseResume() {
        if (videoMediaPlayer == null) {
            return;
        }
        videoMediaPlayer.onPauseResume();
        updatePausePlay();
    }

    private void doToggleFullscreen() {
        if (videoMediaPlayer == null) {
            return;
        }

        videoMediaPlayer.toggleFullScreen();
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (startPauseButton != null) {
            startPauseButton.setEnabled(enabled);
        }
        if (ffwdButton != null) {
            ffwdButton.setEnabled(enabled);
        }
        if (rewButton != null) {
            rewButton.setEnabled(enabled);
        }
        if (progress != null) {
            progress.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    public void onDetach() {
        ffwdListener = null;
        fullscreenListener = null;
        pauseListener = null;
        rewListener = null;
        seekListener = null;
        handler = null;
        videoMediaPlayer = null;
        Log.d(TAG, "onDetach: ");
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

    public void setProgressBarColor(int progressBarColor) {
        this.progressBarColor = ContextCompat.getColor(getContext(), progressBarColor);
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

    public void setFastForwardDuration(int fastForwardDuration) {
        this.fastForwardDuration = fastForwardDuration * 1000;
    }

    public void setRewindDuration(int rewindDuration) {
        this.rewindDuration = rewindDuration * 1000;
    }

    public void setFastForwardDrawable(Drawable fastForwardDrawable) {
        this.fastForwardDrawable = fastForwardDrawable;
    }

    public void setRewindDrawable(Drawable rewindDrawable) {
        this.rewindDrawable = rewindDrawable;
    }

    public void init(FullscreenVideoView fullscreenVideoView, VideoMediaPlayer videoMediaPlayer,
                     AttributeSet attrs) {
        setupXmlAttributes(attrs);
        this.videoMediaPlayer = videoMediaPlayer;
        this.fullscreenVideoView = fullscreenVideoView;
        setMediaIcons();
    }

    private void setMediaIcons() {
        updatePausePlay();
        updateFullScreenDrawable();
        updateFastForwardDrawable();
        updateRewindDrawable();
    }

    private static class MessageHandler extends Handler {
        private final WeakReference<VideoControllerView> mView;

        MessageHandler(VideoControllerView view) {
            mView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            VideoControllerView view = mView.get();
            if (view == null || view.videoMediaPlayer == null) {
                return;
            }

            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    view.hide();
                    break;
                case SHOW_PROGRESS:
                    pos = view.setProgress();
                    if (!view.isDragging && view.isShowing() && view.videoMediaPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    }

    private class PauseOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            doPauseResume();
            show(DEFAULT_TIMEOUT);
        }
    }

    private class FullscreenOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            doToggleFullscreen();
            show(DEFAULT_TIMEOUT);
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
            handler.removeMessages(SHOW_PROGRESS);
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
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isDragging = false;
            setProgress();
            updatePausePlay();
            show(DEFAULT_TIMEOUT);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            handler.sendEmptyMessage(SHOW_PROGRESS);
        }
    }

    private class RewindOnClickListener implements View.OnClickListener {
        public void onClick(View v) {
            if (videoMediaPlayer == null) {
                return;
            }

            int pos = videoMediaPlayer.getCurrentPosition();
            pos -= rewindDuration; // milliseconds
            videoMediaPlayer.seekTo(pos);
            setProgress();

            show(DEFAULT_TIMEOUT);
        }
    }

    private class FfwdOnClickListener implements View.OnClickListener {
        public void onClick(View v) {
            if (videoMediaPlayer == null) {
                return;
            }

            int pos = videoMediaPlayer.getCurrentPosition();
            pos += fastForwardDuration; // milliseconds
            videoMediaPlayer.seekTo(pos);
            setProgress();

            show(DEFAULT_TIMEOUT);
        }
    }
}