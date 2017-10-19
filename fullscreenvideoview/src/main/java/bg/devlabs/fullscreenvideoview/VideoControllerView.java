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
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
    MediaPlayerControl mPlayer;
    TextView mEndTime, mCurrentTime;
    boolean mShowing;
    boolean mDragging;
    Handler mHandler = new MessageHandler(this);
    private LayoutInflater mLayoutInflater;
    private ViewGroup mAnchor;
    private View mRoot;
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
            if (mPlayer == null) {
                return;
            }

            if (!fromUser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = mPlayer.getDuration();
            long newPosition = (duration * progress) / 1000L;
            mPlayer.seekTo((int) newPosition);
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
    private OnClickListener mRewListener = new OnClickListener() {
        public void onClick(View v) {
            if (mPlayer == null) {
                return;
            }

            int pos = mPlayer.getCurrentPosition();
            pos -= rewindSeconds; // milliseconds
            mPlayer.seekTo(pos);
            setProgress();

            show(sDefaultTimeout);
        }
    };
    private OnClickListener mFfwdListener = new OnClickListener() {
        public void onClick(View v) {
            if (mPlayer == null) {
                return;
            }

            int pos = mPlayer.getCurrentPosition();
            pos += fastForwardSeconds; // milliseconds
            mPlayer.seekTo(pos);
            setProgress();

            show(sDefaultTimeout);
        }
    };
    private int exitFullscreenDrawable = R.drawable.ic_media_fullscreen_shrink;
    private int enterFullscreenDrawable = R.drawable.ic_media_fullscreen_stretch;
    private int progressBarColor = Color.WHITE;
    private int playDrawable = R.drawable.ic_media_play;
    private int pauseDrawable = R.drawable.ic_media_pause;
    private int fastForwardSeconds = 15000;
    private int rewindSeconds = 5000;
    private int mediaControllerGravity = Gravity.BOTTOM;
    private int mediaControllerLayout = R.layout.media_controller;

    public VideoControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = null;
        Log.i(TAG, TAG);
    }

    public VideoControllerView(Context context, LayoutInflater layoutInflater) {
        super(context);
        this.mLayoutInflater = layoutInflater;
        Log.i(TAG, TAG);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        if (mRoot != null)
            initControllerView(mRoot);
    }

    public void setMediaPlayer(MediaPlayerControl player) {
        mPlayer = player;
        updatePausePlay();
        updateFullScreenDrawable();
    }

    /**
     * Set the view that acts as the anchor for the control view.
     * This can for example be a VideoView, or your Activity's main view.
     *
     * @param view The view to which to anchor the controller when it is visible.
     */
    public void setAnchorView(ViewGroup view) {
        mAnchor = view;

        LayoutParams frameParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        removeAllViews();
        View v = makeControllerView(view);
        addView(v, frameParams);
    }

    /**
     * Create the view that holds the widgets that control playback.
     * Derived classes can override this to create their own.
     *
     * @return The controller view.
     */
    protected View makeControllerView(ViewGroup view) {
        mRoot = mLayoutInflater.inflate(mediaControllerLayout, (ViewGroup) view.getRootView(),
                false);
        initControllerView(mRoot);
        return mRoot;
    }

    private void initControllerView(View v) {
        mStartPauseButton = v.findViewById(R.id.start_pause_media_button);
        if (mStartPauseButton != null) {
            mStartPauseButton.requestFocus();
            mStartPauseButton.setOnClickListener(mPauseListener);
        }

        mFullscreenButton = v.findViewById(R.id.fullscreen_media_button);
        if (mFullscreenButton != null) {
            mFullscreenButton.requestFocus();
            mFullscreenButton.setOnClickListener(mFullscreenListener);
        }

        mFfwdButton = v.findViewById(R.id.forward_media_button);
        if (mFfwdButton != null) {
            mFfwdButton.setOnClickListener(mFfwdListener);
        }

        mRewButton = v.findViewById(R.id.rewind_media_button);
        if (mRewButton != null) {
            mRewButton.setOnClickListener(mRewListener);
        }

        mProgress = v.findViewById(R.id.progress_seek_bar);
        if (mProgress != null) {
            mProgress.getProgressDrawable().setColorFilter(progressBarColor, PorterDuff.Mode.SRC_IN);
            mProgress.getThumb().setColorFilter(progressBarColor, PorterDuff.Mode.SRC_IN);
            mProgress.setOnSeekBarChangeListener(mSeekListener);
            mProgress.setMax(1000);
        }

        mEndTime = v.findViewById(R.id.time);
        mCurrentTime = v.findViewById(R.id.time_current);
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
        if (mPlayer == null) {
            return;
        }

        try {
            if (mStartPauseButton != null && !mPlayer.canPause()) {
                mStartPauseButton.setEnabled(false);
            }
            if (mRewButton != null && !mPlayer.canSeekBackward()) {
                mRewButton.setEnabled(false);
            }
            if (mFfwdButton != null && !mPlayer.canSeekForward()) {
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
        if (!mShowing && mAnchor != null) {
            setProgress();
            if (mStartPauseButton != null) {
                mStartPauseButton.requestFocus();
            }
            disableUnsupportedButtons();

            LayoutParams tlp = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    mediaControllerGravity
            );

            mAnchor.addView(this, tlp);
            mShowing = true;
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

    public boolean isShowing() {
        return mShowing;
    }

    /**
     * Remove the controller from the screen.
     */
    public void hide() {
        if (mAnchor == null) {
            return;
        }

        try {
            mAnchor.removeView(this);
            mHandler.removeMessages(SHOW_PROGRESS);
        } catch (IllegalArgumentException ex) {
            Log.w("MediaController", "already removed");
        }
        mShowing = false;
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
        if (mPlayer == null || mDragging) {
            return 0;
        }

        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
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
        if (mPlayer == null) {
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
            if (uniqueDown && !mPlayer.isPlaying()) {
                mPlayer.start();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayer.isPlaying()) {
                mPlayer.pause();
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

    public void updatePausePlay() {
        if (mRoot == null || mStartPauseButton == null || mPlayer == null) {
            return;
        }

        if (mPlayer.isPlaying()) {
            mStartPauseButton.setImageResource(pauseDrawable);
        } else {
            mStartPauseButton.setImageResource(playDrawable);
        }
    }

    public void updateFullScreenDrawable() {
        if (mRoot == null || mFullscreenButton == null || mPlayer == null) {
            return;
        }

        if (mPlayer.isFullScreen()) {
            mFullscreenButton.setImageResource(exitFullscreenDrawable);
        } else {
            mFullscreenButton.setImageResource(enterFullscreenDrawable);
        }
    }

    void doPauseResume() {
        if (mPlayer == null) {
            return;
        }

        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }

        updatePausePlay();
    }

    void doToggleFullscreen() {
        if (mPlayer == null) {
            return;
        }

        mPlayer.toggleFullScreen();
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
        mAnchor = null;
        mHandler = null;
        mPlayer = null;
        mRoot = null;
        Log.d(TAG, "onDestroy: ");
    }

    public void setEnterFullscreenDrawable(@DrawableRes int enterFullscreenDrawable) {
        if (enterFullscreenDrawable == 0) {
            return;
        }
        this.enterFullscreenDrawable = enterFullscreenDrawable;
    }

    public void setExitFullscreenDrawable(@DrawableRes int exitFullscreenDrawable) {
        if (exitFullscreenDrawable == 0) {
            return;
        }
        this.exitFullscreenDrawable = exitFullscreenDrawable;
    }

    public void setProgressBarColor(int progressBarColor) {
        this.progressBarColor = progressBarColor;
    }

    public void setPlayDrawable(int playDrawable) {
        if (playDrawable == 0) {
            return;
        }
        this.playDrawable = playDrawable;
    }

    public void setPauseDrawable(int pauseDrawable) {
        if (pauseDrawable == 0) {
            return;
        }
        this.pauseDrawable = pauseDrawable;
    }

    public void setFastForwardSeconds(int fastForwardSeconds) {
        this.fastForwardSeconds = fastForwardSeconds * 1000;
    }

    public void setRewindSeconds(int rewindSeconds) {
        this.rewindSeconds = rewindSeconds * 1000;
    }

    public void setMediaControllerLayout(int mediaControllerLayout) {
        this.mediaControllerLayout = mediaControllerLayout;
    }

    public void setMediaControllerGravity(int mediaControllerGravity) {
        this.mediaControllerGravity = mediaControllerGravity;
    }

    private static class MessageHandler extends Handler {
        private final WeakReference<VideoControllerView> mView;

        MessageHandler(VideoControllerView view) {
            mView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            VideoControllerView view = mView.get();
            if (view == null || view.mPlayer == null) {
                return;
            }

            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    view.hide();
                    break;
                case SHOW_PROGRESS:
                    pos = view.setProgress();
                    if (!view.mDragging && view.mShowing && view.mPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    }
}