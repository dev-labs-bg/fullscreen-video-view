package bg.devlabs.fullscreenvideoview;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.io.File;
import java.io.IOException;

import bg.devlabs.fullscreenvideoview.orientation.LandscapeOrientation;
import bg.devlabs.fullscreenvideoview.orientation.OrientationEventHandler;
import bg.devlabs.fullscreenvideoview.orientation.PortraitOrientation;
import bg.devlabs.fullscreenvideoview.util.DeviceUtils;
import bg.devlabs.fullscreenvideoview.util.UiUtils;

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

    private boolean isLandscape;
    private boolean isAutoStartEnabled;
    private boolean isMediaPlayerPrepared;
    private int originalWidth;
    private int originalHeight;
    private String videoPath;

    // Listeners
    private OrientationEventListener orientationEventListener;
    private MediaPlayer.OnPreparedListener onPreparedListener;
    private View.OnTouchListener onTouchListener;
    // Orientation
    private LandscapeOrientation landscapeOrientation = LandscapeOrientation.SENSOR;
    private PortraitOrientation portraitOrientation = PortraitOrientation.PORTRAIT;

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
            initOrientationListener();
        }

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        controller.init(videoMediaPlayer, attrs);
        setupProgressBarColor();
        initOnBackPressedListener();

        // Setup VideoView
        setupOnTouchListener();
        setupOnPreparedListener();
    }

    private void initOrientationListener() {
        if (!isInEditMode()) {
            orientationEventListener = new OrientationEventHandler(getContext(), this);
            orientationEventListener.enable();
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
            activateFullscreen();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            exitFullscreen();
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
        if (orientationEventListener != null) {
            orientationEventListener.disable();
            orientationEventListener = null;
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

    public void setOrientation(int orientation) {
        ((Activity) getContext()).setRequestedOrientation(orientation);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void toggleSystemUiVisibility(Window activityWindow) {
        int newUiOptions = activityWindow.getDecorView().getSystemUiVisibility();
        newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        activityWindow.getDecorView().setSystemUiVisibility(newUiOptions);
    }

    private void activateFullscreen() {
        // Update isLandscape flag
        if (!isLandscape) {
            isLandscape = true;
        }

        // Update the fullscreen button drawable
        controller.updateFullScreenDrawable();

        // Change the screen orientation to SENSOR_LANDSCAPE
        Activity activity = ((Activity) getContext());
        setOrientation(landscapeOrientation.getValue());

        UiUtils.hideOtherViews((ViewGroup) getParent());

        // Save the video player original width and height
        originalWidth = getWidth();
        originalHeight = getHeight();
        // TODO: Add check if the video should be landscape or portrait in isLandscape
        updateLayoutParams(activity);

        // Hiding the supportToolbar
        hideActionBar();

        // Hide status bar
        toggleSystemUiVisibility(activity.getWindow());
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void updateLayoutParams(Activity activity) {
        ViewGroup.LayoutParams params = getLayoutParams();
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        WindowManager windowManager = activity.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        boolean hasSoftKeys = DeviceUtils.hasSoftKeys(display);
        boolean isSystemBarOnBottom = DeviceUtils.isSystemBarOnBottom(activity);
        int navBarHeight = DeviceUtils.getNavigationBarHeight(resources);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        if (hasSoftKeys) {
            if (isSystemBarOnBottom) {
                height += navBarHeight;
            } else {
                width += navBarHeight;
            }
        }

        params.width = width;
        params.height = height;

        setLayoutParams(params);
    }

    private void exitFullscreen() {
        // Update isLandscape flag
        if (isLandscape) {
            isLandscape = false;
        }

        // Update the fullscreen button drawable
        controller.updateFullScreenDrawable();

        // Change the screen orientation to PORTRAIT
        Activity activity = (Activity) getContext();
        // TODO: Calculating the size according to if the view is on the whole screen or not
        setOrientation(portraitOrientation.getValue());

        UiUtils.showOtherViews((ViewGroup) getParent());

        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = originalWidth;
        params.height = originalHeight;
        setLayoutParams(params);

        showActionBar();
        toggleSystemUiVisibility(activity.getWindow());
    }

    private void showActionBar() {
        ActionBar supportActionBar = ((AppCompatActivity) getContext()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.show();
        }

        android.app.ActionBar actionBar = ((Activity) getContext()).getActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    }

    private void hideActionBar() {
        ActionBar supportActionBar = ((AppCompatActivity) getContext()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }

        android.app.ActionBar actionBar = ((Activity) getContext()).getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    boolean shouldHandleOnBackPressed() {
        if (isLandscape) {
            // Locks the screen orientation to portrait
            setOrientation(portraitOrientation.getValue());
            controller.updateFullScreenDrawable();
            return true;
        }

        return false;
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
        this.landscapeOrientation = landscapeOrientation;
        return this;
    }

    public FullscreenVideoView portraitOrientation(PortraitOrientation portraitOrientation) {
        this.portraitOrientation = portraitOrientation;
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
        return isLandscape;
    }

    @Override
    public void toggleFullscreen() {
        int newOrientation = landscapeOrientation.getValue();
        if (isLandscape) {
            newOrientation = portraitOrientation.getValue();
        }

        isLandscape = !isLandscape;
        setOrientation(newOrientation);
    }

    private class VideoOnKeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
            return (event.getAction() == KeyEvent.ACTION_UP) &&
                    (keyCode == KeyEvent.KEYCODE_BACK) &&
                    shouldHandleOnBackPressed();
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
}
