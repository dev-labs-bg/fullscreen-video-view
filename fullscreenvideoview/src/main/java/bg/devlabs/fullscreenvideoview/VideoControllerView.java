package bg.devlabs.fullscreenvideoview;/*
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
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
class VideoControllerView extends FrameLayout {
    private static final String TAG = "VideoControllerView";
    private static final int sDefaultTimeout = 3000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private IVideoMediaPlayer videoMediaPlayer;
    private TextView mEndTime, mCurrentTime;
    boolean mDragging;
    Handler mHandler = new MessageHandler(this);
    private SeekBar mProgress;
    private ImageButton mStartPauseButton;
    private ImageButton mFfwdButton;
    private ImageButton mRewButton;
    private ImageButton mFullscreenButton;
    private OnClickListener mPauseListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            doPauseResume();
            show(sDefaultTimeout);
        }
    };
    private OnClickListener mFullscreenListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            doToggleFullscreen();
            show(sDefaultTimeout);
        }
    };
    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the position of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    @Nullable
    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
            if (videoMediaPlayer == null) {
                return;
            }

            if (!fromUser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = videoMediaPlayer.getDuration();
            long newPosition = (duration * progress) / 1000L;
            videoMediaPlayer.seekTo((int) newPosition);
            if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime((int) newPosition));
        }

        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            updatePausePlay();
            show(sDefaultTimeout);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };
    @Nullable
    private OnClickListener mRewListener = new OnClickListener() {
        public void onClick(View v) {
            if (videoMediaPlayer == null) {
                return;
            }

            int pos = videoMediaPlayer.getCurrentPosition();
            pos -= rewindSeconds; // milliseconds
            videoMediaPlayer.seekTo(pos);
            setProgress();

            show(sDefaultTimeout);
        }
    };
    @Nullable
    private OnClickListener mFfwdListener = new OnClickListener() {
        public void onClick(View v) {
            if (videoMediaPlayer == null) {
                return;
            }

            int pos = videoMediaPlayer.getCurrentPosition();
            pos += fastForwardSeconds; // milliseconds
            videoMediaPlayer.seekTo(pos);
            setProgress();

            show(sDefaultTimeout);
        }
    };
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

    private int fastForwardSeconds = 15000;
    private int rewindSeconds = 5000;
    private IFullscreenVideoView fullscreenVideoView;

    public VideoControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        layoutInflater.inflate(R.layout.video_controller, this, true);
        initControllerView();
        setupXmlAttributes(attrs);
    }

    public VideoControllerView(Context context) {
        super(context);
        final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        layoutInflater.inflate(R.layout.video_controller, this, true);
        initControllerView();
    }

    public void setupXmlAttributes(AttributeSet attrs) {
        TypedArray typedArr = getContext().obtainStyledAttributes(attrs,
                R.styleable.VideoControllerView, 0, 0);
        setupPlayPauseButton(typedArr);
        setupFullscreenButton(typedArr);
        setupFastForwardButton(typedArr);
        setupRewindButton(typedArr);
        setupProgressBar(typedArr);
        // Recycle the TypedArray
        typedArr.recycle();
    }

    void setupProgressBar(TypedArray a) {
        // TODO: Add different setters for the background and the thumb of the progress bar
        int progressBarColor = a.getColor(R.styleable.VideoControllerView_progress_color, 0);
        if (progressBarColor != 0) {
            mProgress.getProgressDrawable().setColorFilter(progressBarColor, PorterDuff.Mode.SRC_IN);
            mProgress.getThumb().setColorFilter(progressBarColor, PorterDuff.Mode.SRC_IN);
            setProgressBarColor(progressBarColor);
        } else {
            // Set the default color
            mProgress.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            mProgress.getThumb().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        }
    }

    void setupRewindButton(TypedArray a) {
        Drawable rewindDrawable = a.getDrawable(R.styleable.VideoControllerView_rew_drawable);
        if (rewindDrawable != null) {
            mRewButton.setImageDrawable(rewindDrawable);
            setRewindDrawable(rewindDrawable);
        } else {
            mRewButton.setImageResource(R.drawable.ic_fast_rewind_white_48dp);
        }
    }

    void setupFastForwardButton(TypedArray a) {
        Drawable ffwdDrawable = a.getDrawable(R.styleable.VideoControllerView_ffwd_drawable);
        if (ffwdDrawable != null) {
            mFfwdButton.setImageDrawable(ffwdDrawable);
            setFastForwardDrawable(ffwdDrawable);
        } else {
            mFfwdButton.setImageResource(R.drawable.ic_fast_forward_white_48dp);
        }
    }

    void setupFullscreenButton(TypedArray a) {
        Drawable enterFullscreenDrawable = a.getDrawable(
                R.styleable.VideoControllerView_enter_fullscreen_drawable);
        if (enterFullscreenDrawable != null) {
            mFullscreenButton.setImageDrawable(enterFullscreenDrawable);
            setEnterFullscreenDrawable(enterFullscreenDrawable);
        } else {
            // Set the default drawable
            mFullscreenButton.setImageResource(R.drawable.ic_fullscreen_white_48dp);
        }

        Drawable exitFullscreenDrawable = a.getDrawable(
                R.styleable.VideoControllerView_exit_fullscreen_drawable);
        // The exitFullscreenDrawable is not null, therefore pass it to the controller,
        // else there is a default value for it in the controller
        if (exitFullscreenDrawable != null) {
            setExitFullscreenDrawable(exitFullscreenDrawable);
        }
    }

    void setupPlayPauseButton(TypedArray a) {
        Drawable playDrawable = a.getDrawable(R.styleable.VideoControllerView_play_drawable);
        if (playDrawable != null) {
            mStartPauseButton.setImageDrawable(playDrawable);
            setPlayDrawable(playDrawable);
        } else {
            // Set the default drawable
            mStartPauseButton.setImageResource(R.drawable.ic_play_arrow_white_48dp);
        }

        Drawable pauseDrawable = a.getDrawable(R.styleable.VideoControllerView_pause_drawable);
        // The pauseDrawable is not null, therefore pass it to the controller, else there is a
        // default value for it in the controller
        if (pauseDrawable != null) {
            setPauseDrawable(pauseDrawable);
        }
    }

    public void setMediaPlayer(IVideoMediaPlayer player) {
        videoMediaPlayer = player;
        // TODO: Check

    }

    public void initControllerView() {
        if (!isInEditMode()) {
            setVisibility(INVISIBLE);
        }

        mStartPauseButton = findViewById(R.id.start_pause_media_button);
        if (mStartPauseButton != null) {
            mStartPauseButton.requestFocus();
            mStartPauseButton.setOnClickListener(mPauseListener);
        }

        mFullscreenButton = findViewById(R.id.fullscreen_media_button);
        if (mFullscreenButton != null) {
            mFullscreenButton.requestFocus();
            mFullscreenButton.setOnClickListener(mFullscreenListener);
        }

        mFfwdButton = findViewById(R.id.forward_media_button);
        if (mFfwdButton != null) {
            mFfwdButton.setOnClickListener(mFfwdListener);
        }

        mRewButton = findViewById(R.id.rewind_media_button);
        if (mRewButton != null) {
            mRewButton.setOnClickListener(mRewListener);
        }

        mProgress = findViewById(R.id.progress_seek_bar);
        if (mProgress != null) {
            mProgress.getProgressDrawable().setColorFilter(progressBarColor, PorterDuff.Mode.SRC_IN);
            mProgress.getThumb().setColorFilter(progressBarColor, PorterDuff.Mode.SRC_IN);
            mProgress.setOnSeekBarChangeListener(mSeekListener);
            mProgress.setMax(1000);
        }

        mEndTime = findViewById(R.id.time);
        mCurrentTime = findViewById(R.id.time_current);
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    public void show() {
        show(sDefaultTimeout);
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control interface to be a MediaPlayerControlExt
     */
    private void disableUnsupportedButtons() {
        if (videoMediaPlayer == null) {
            return;
        }

        try {
            if (mStartPauseButton != null && !videoMediaPlayer.canPause()) {
                mStartPauseButton.setEnabled(false);
            }
            if (mRewButton != null && !videoMediaPlayer.canSeekBackward()) {
                mRewButton.setEnabled(false);
            }
            if (mFfwdButton != null && !videoMediaPlayer.canSeekForward()) {
                mFfwdButton.setEnabled(false);
            }
        } catch (IncompatibleClassChangeError ex) {
            // We were given an old version of the interface, that doesn't have
            // the canPause/canSeekXYZ methods. This is OK, it just means we
            // assume the media can be paused and seeked, and so we don't disable
            // the buttons.
        }
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     *
     * @param timeout The timeout in milliseconds. Use 0 to show
     *                the controller until hide() is called.
     */
    public void show(int timeout) {
        if (!isShowing()) {
            setProgress();
            if (mStartPauseButton != null) {
                mStartPauseButton.requestFocus();
            }
            disableUnsupportedButtons();
            setVisibility(VISIBLE);
        }

        updatePausePlay();
        updateFullScreenDrawable();

        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
    }

    private boolean isShowing() {
        return getVisibility() == VISIBLE;
    }

    /**
     * Remove the controller from the screen.
     */
    public void hide() {
        try {
            setVisibility(INVISIBLE);
            mHandler.removeMessages(SHOW_PROGRESS);
        } catch (IllegalArgumentException ex) {
            Log.w("MediaController", "already removed");
        }
    }

    String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        }
    }

    int setProgress() {
        if (videoMediaPlayer == null || mDragging) {
            return 0;
        }

        int position = videoMediaPlayer.getCurrentPosition();
        int duration = videoMediaPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = videoMediaPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));

        return position;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performClick();
        show(sDefaultTimeout);
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (videoMediaPlayer == null) {
            return true;
        }

        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show(sDefaultTimeout);
                if (mStartPauseButton != null) {
                    mStartPauseButton.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !videoMediaPlayer.isPlaying()) {
                videoMediaPlayer.start();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && videoMediaPlayer.isPlaying()) {
                videoMediaPlayer.pause();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show(sDefaultTimeout);
        return super.dispatchKeyEvent(event);
    }

    private void updateRewindDrawable() {
        if (mRewButton == null || videoMediaPlayer == null) {
            return;
        }

        mRewButton.setImageDrawable(rewindDrawable);
    }

    private void updateFastForwardDrawable() {
        if (mFfwdButton == null || videoMediaPlayer == null) {
            return;
        }

        mFfwdButton.setImageDrawable(fastForwardDrawable);
    }

    public void updatePausePlay() {
        if (mStartPauseButton == null || videoMediaPlayer == null) {
            return;
        }

        if (videoMediaPlayer.isPlaying()) {
            mStartPauseButton.setImageDrawable(pauseDrawable);
        } else {
            mStartPauseButton.setImageDrawable(playDrawable);
        }
    }

    public void updateFullScreenDrawable() {
        if (mFullscreenButton == null || videoMediaPlayer == null) {
            return;
        }

        if (fullscreenVideoView.isLandscape()) {
            mFullscreenButton.setImageDrawable(exitFullscreenDrawable);
        } else {
            mFullscreenButton.setImageDrawable(enterFullscreenDrawable);
        }
    }

    void doPauseResume() {
        if (videoMediaPlayer == null) {
            return;
        }

        if (videoMediaPlayer.isPlaying()) {
            videoMediaPlayer.pause();
        } else {
            videoMediaPlayer.start();
        }

        updatePausePlay();
    }

    void doToggleFullscreen() {
        if (videoMediaPlayer == null) {
            return;
        }

        videoMediaPlayer.toggleFullScreen();
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mStartPauseButton != null) {
            mStartPauseButton.setEnabled(enabled);
        }
        if (mFfwdButton != null) {
            mFfwdButton.setEnabled(enabled);
        }
        if (mRewButton != null) {
            mRewButton.setEnabled(enabled);
        }
        if (mProgress != null) {
            mProgress.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    public void onDestroy() {
        mFfwdListener = null;
        mFullscreenListener = null;
        mPauseListener = null;
        mRewListener = null;
        mSeekListener = null;
        mHandler = null;
        videoMediaPlayer = null;
        Log.d(TAG, "onDestroy: ");
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
        this.progressBarColor = progressBarColor;
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

    public void setFastForwardSeconds(int fastForwardSeconds) {
        this.fastForwardSeconds = fastForwardSeconds * 1000;
    }

    public void setRewindSeconds(int rewindSeconds) {
        this.rewindSeconds = rewindSeconds * 1000;
    }

    public void setFastForwardDrawable(Drawable fastForwardDrawable) {
        this.fastForwardDrawable = fastForwardDrawable;
    }

    public void setRewindDrawable(Drawable rewindDrawable) {
        this.rewindDrawable = rewindDrawable;
    }

    public void init(IFullscreenVideoView fullscreenVideoView, IVideoMediaPlayer videoMediaPlayer,
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
                    if (!view.mDragging && view.isShowing() && view.videoMediaPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }

        @Override
        public String toString() {
            return "MessageHandler{" +
                    "mView=" + mView +
                    '}';
        }
    }
}