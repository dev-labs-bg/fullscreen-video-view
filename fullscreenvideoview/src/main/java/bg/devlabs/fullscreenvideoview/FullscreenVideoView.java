package bg.devlabs.fullscreenvideoview;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.io.File;
import java.io.IOException;

import bg.devlabs.fullscreenvideoview.orientation.LandscapeOrientation;
import bg.devlabs.fullscreenvideoview.orientation.OrientationDelegate;
import bg.devlabs.fullscreenvideoview.orientation.PortraitOrientation;

/**
 * Created by Slavi Petrov on 05.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
public class FullscreenVideoView extends FrameLayout implements IFullscreenVideoView,
        SurfaceHolder.Callback {
    // Views
    private VideoSurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private ProgressBar progressBar;
    VideoControllerView controller;
    // MediaPlayer
    private VideoMediaPlayer videoMediaPlayer;

    private boolean isAutoStartEnabled;
    private boolean isMediaPlayerPrepared;

    private String videoPath;
    private OrientationDelegate orientationDelegate;

    // Listeners
    private MediaPlayer.OnPreparedListener onPreparedListener;
    private View.OnTouchListener onTouchListener;

    public FullscreenVideoView(@NonNull final Context context) {
        super(context);
    }

    public FullscreenVideoView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public FullscreenVideoView(@NonNull final Context context, @Nullable final AttributeSet attrs,
                               final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(final AttributeSet attrs) {
        findChildViews();
        // Skip this init rows - needed when changing FullscreenVideoView properties in XML
        if (!isInEditMode()) {
            videoMediaPlayer = new VideoMediaPlayer(this);
            initOrientationHandlers();
        }

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        controller.init(this, videoMediaPlayer, attrs);
        setupProgressBarColor();
        initOnBackPressedListener();

        // Setup VideoView
        setupOnTouchListener();
        setupOnPreparedListener();
    }

    private void initOrientationHandlers() {
        if (!isInEditMode()) {
            orientationDelegate = new VideoOrientationDelegate();
            orientationDelegate.enable();
        }
    }

    private void findChildViews() {
        final LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(R.layout.video_player, this, true);
        surfaceView = findViewById(R.id.surface_view);
        progressBar = findViewById(R.id.progress_bar);
        controller = findViewById(R.id.video_controller);
    }

    public FullscreenVideoView videoFile(@NonNull final File videoFile) {
        videoPath = videoFile.getPath();
        setupMediaPlayer();
        return this;
    }

    public FullscreenVideoView videoPath(@NonNull final String videoPath) {
        this.videoPath = videoPath;
        setupMediaPlayer();
        return this;
    }

    private void initOnBackPressedListener() {
        setFocusableInTouchMode(true);
        requestFocus();
        setOnKeyListener(new VideoOnKeyListener());
    }

    private LayoutInflater getLayoutInflater() {
        Context context = getContext();
        return LayoutInflater.from(context);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            orientationDelegate.activateFullscreen();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            orientationDelegate.exitFullscreen();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        handleOnDetach();
        super.onDetachedFromWindow();
    }

    private void handleOnDetach() {
        Log.d(FullscreenVideoView.class.getSimpleName(), "onDetachedFromWindow: ");
        controller.onDestroy();
        controller = null;

        onTouchListener = null;
        // Disable and null the OrientationEventListener
        if (orientationDelegate != null) {
            orientationDelegate.disable();
            orientationDelegate = null;
        }

        if (videoMediaPlayer != null) {
            videoMediaPlayer.clearFullscreenVideoView();
            videoMediaPlayer.setOnPreparedListener(null);
            videoMediaPlayer.stop();
            videoMediaPlayer.release();
            videoMediaPlayer = null;
        }
        onPreparedListener = null;

        if (surfaceHolder != null) {
            surfaceHolder.removeCallback(this);
            surfaceHolder.getSurface().release();
            surfaceHolder = null;
        }

        if (surfaceView != null) {
            surfaceView.invalidate();
            surfaceView.destroyDrawingCache();
            surfaceView = null;
        }

        progressBar = null;
        setOnKeyListener(null);
        setOnTouchListener(null);
    }

    private void setupOnPreparedListener() {
        onPreparedListener = new VideoOnPreparedListener();
    }

    private void setupMediaPlayer() {
        try {
            showProgress();
            videoMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            videoMediaPlayer.setDataSource(videoPath);
            videoMediaPlayer.setOnPreparedListener(onPreparedListener);
            videoMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupOnTouchListener() {
        onTouchListener = new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.performClick();
                controller.show();
                return false;
            }
        };

        setOnTouchListener(onTouchListener);
    }

    private void setupProgressBarColor() {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        progressBar.animate().setDuration(shortAnimTime);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public FullscreenVideoView isAutoStartEnabled(boolean autoStartEnabled) {
        isAutoStartEnabled = autoStartEnabled;
        return this;
    }

    public FullscreenVideoView enterFullscreenDrawable(Drawable enterFullscreenDrawable) {
        this.controller.setEnterFullscreenDrawable(enterFullscreenDrawable);
        return this;
    }

    public FullscreenVideoView exitFullscreenDrawable(Drawable exitFullscreenDrawable) {
        this.controller.setExitFullscreenDrawable(exitFullscreenDrawable);
        return this;
    }

    public FullscreenVideoView progressBarColor(int progressBarColor) {
        this.controller.setProgressBarColor(ContextCompat.getColor(getContext(), progressBarColor));
        return this;
    }

    public FullscreenVideoView playIcon(Drawable playDrawable) {
        this.controller.setPlayDrawable(playDrawable);
        return this;
    }

    public FullscreenVideoView pauseIcon(Drawable pauseDrawable) {
        this.controller.setPauseDrawable(pauseDrawable);
        return this;
    }

    public FullscreenVideoView fastForwardSeconds(int seconds) {
        this.controller.setFastForwardSeconds(seconds);
        return this;
    }

    public FullscreenVideoView rewindSeconds(int seconds) {
        this.controller.setRewindSeconds(seconds);
        return this;
    }

    public FullscreenVideoView landscapeOrientation(LandscapeOrientation landscapeOrientation) {
        orientationDelegate.setLandscapeOrientation(landscapeOrientation);
        return this;
    }

    public FullscreenVideoView portraitOrientation(PortraitOrientation portraitOrientation) {
        orientationDelegate.setPortraitOrientation(portraitOrientation);
        return this;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (videoMediaPlayer != null) {
            videoMediaPlayer.setDisplay(surfaceView.getHolder());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Not used
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (videoMediaPlayer != null && isMediaPlayerPrepared) {
            videoMediaPlayer.pause();
        }
    }

    @Override
    public boolean isLandscape() {
        return orientationDelegate.isLandscape();
    }

    @Override
    public void toggleFullscreen() {
        orientationDelegate.toggleFullscreen();
    }

    private class VideoOnKeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
            return (event.getAction() == KeyEvent.ACTION_UP) &&
                    (keyCode == KeyEvent.KEYCODE_BACK) &&
                    orientationDelegate.shouldHandleOnBackPressed();
        }
    }

    private class VideoOnPreparedListener implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            Log.d(FullscreenVideoView.class.getSimpleName(), "onPrepared: ");
            if (!((Activity) getContext()).isDestroyed()) {
                hideProgress();
                // Get the dimensions of the video
                int videoWidth = videoMediaPlayer.getVideoWidth();
                int videoHeight = videoMediaPlayer.getVideoHeight();
                surfaceView.updateLayoutParams(videoWidth, videoHeight);
                // Start media player if auto start is enabled
                if (mediaPlayer != null && isAutoStartEnabled) {
                    isMediaPlayerPrepared = true;
                    mediaPlayer.start();
                }
            }
        }
    }

    private class VideoOrientationDelegate extends OrientationDelegate {
        public VideoOrientationDelegate() {
            super(getContext(), FullscreenVideoView.this);
        }

        @Override
        public void onOrientationChanged() {
            // Update the fullscreen button drawable
            controller.updateFullScreenDrawable();
        }
    }
}
