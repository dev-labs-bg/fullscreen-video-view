package bg.devlabs.fullscreenvideoview;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
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

import bg.devlabs.fullscreenvideoview.util.DeviceUtils;
import bg.devlabs.fullscreenvideoview.util.UiUtils;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;

/**
 * Created by Slavi Petrov on 05.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
public class FullscreenVideoView extends FrameLayout implements IFullscreenVideoView,
        SurfaceHolder.Callback {
    private VideoSurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private ProgressBar progressBar;
    private VideoControllerView controller;
    private boolean isFullscreen, isAutoStartEnabled;
    private VideoMediaPlayer videoMediaPlayer = new VideoMediaPlayer(this);
    private String videoPath;
    private File videoFile;
    private ActionBar supportActionBar;
    private android.app.ActionBar actionBar;
    private int originalWidth, originalHeight;
    // Listeners
    private OrientationEventListener orientationEventListener;
    private MediaPlayer.OnPreparedListener onPreparedListener;
    private View.OnTouchListener onTouchListener;
    private boolean isMediaPlayerPrepared;

    public FullscreenVideoView(@NonNull Context context) {
        super(context);
        init();
    }

    public FullscreenVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FullscreenVideoView(@NonNull Context context, @Nullable AttributeSet attrs,
                               int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater layoutInflater = getLayoutInflater();
        View root = layoutInflater.inflate(R.layout.video_player, this, true);
        this.surfaceView = root.findViewById(R.id.surface_view);
        this.progressBar = root.findViewById(R.id.progress_bar);
    }

    public FullscreenVideoView init(@NonNull File videoFile) {
        this.videoFile = videoFile;
        init();
        return this;
    }

    public FullscreenVideoView init(@NonNull String videoPath) {
        this.videoPath = videoPath;
        setupView();
        return this;
    }

    private void setupView() {
        setupBar();

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        controller = new VideoControllerView(getContext(), getLayoutInflater());

        setupProgressBar();
        initOrientationListener();
        initOnBackPressedListener();
        // Setup VideoView
        setupOnTouchListener();
        setupOnPreparedListener();

        setupMediaPlayer();
    }

    private void initOnBackPressedListener() {
        setFocusableInTouchMode(true);
        requestFocus();
        setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return event.getAction() == KeyEvent.ACTION_UP &&
                        keyCode == KeyEvent.KEYCODE_BACK &&
                        shouldHandleOnBackPressed();
            }
        });
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
        super.onDetachedFromWindow();
        handleOnDetach();
    }

    public void handleOnDetach() {
        controller.onDestroy();
        onPreparedListener = null;
        onTouchListener = null;
        // Disable and null the OrientationEventListener
        if (orientationEventListener != null) {
            orientationEventListener.disable();
            orientationEventListener = null;
        }

        videoMediaPlayer.release();

        if (surfaceHolder != null) {
            surfaceHolder.removeCallback(this);
            surfaceHolder.getSurface().release();
        }
    }

    private void setupBar() {
        Context context = getContext();
        if (context instanceof AppCompatActivity) {
            this.supportActionBar = ((AppCompatActivity) context).getSupportActionBar();
        } else if (context instanceof Activity) {
            this.actionBar = ((Activity) context).getActionBar();
        }
    }

    private void setupOnPreparedListener() {
        onPreparedListener = new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                if (((Activity) getContext()).isDestroyed()) {
                    return;
                }
                hideProgress();
                // Get the dimensions of the video
                int videoWidth = videoMediaPlayer.getVideoWidth();
                int videoHeight = videoMediaPlayer.getVideoHeight();
                surfaceView.updateLayoutParams(videoWidth, videoHeight);
                // Setup controller
                controller.setMediaPlayer(videoMediaPlayer);
                controller.setAnchorView(FullscreenVideoView.this);
                // Start media player if auto start is enabled
                if (mediaPlayer != null && isAutoStartEnabled) {
                    isMediaPlayerPrepared = true;
                    mediaPlayer.start();
                }
            }
        };
    }

    private void setupMediaPlayer() {
        try {
            showProgress();
            videoMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            videoMediaPlayer.setDataSource(getVideoPath());
            videoMediaPlayer.setOnPreparedListener(onPreparedListener);
            videoMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getVideoPath() {
        if (videoPath != null) {
            return videoPath;
        } else if (videoFile != null) {
            return videoFile.getPath();
        } else {
            return null;
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

    private void setupProgressBar() {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        progressBar.animate().setDuration(shortAnimTime);
    }

    public void setOrientation(int orientation) {
        ((Activity) getContext()).setRequestedOrientation(orientation);
    }

    void hideProgress() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    void showProgress() {
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
        // Update isFullscreen flag
        if (!isFullscreen) {
            isFullscreen = true;
        }

        // Update the fullscreen button drawable
        controller.updateFullScreenDrawable();

        // Change the screen orientation to SENSOR_LANDSCAPE
        setOrientation(SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        UiUtils.hideOtherViews((ViewGroup) getParent());

        // Save the video player original width and height
        this.originalWidth = getWidth();
        this.originalHeight = getHeight();
        // TODO: Add check if the video should be landscape or portrait in isFullscreen
        Activity activity = (Activity) getContext();
        // Get the dimensions of the video
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
        // Update isFullscreen flag
        if (isFullscreen) {
            isFullscreen = false;
        }

        // Update the fullscreen button drawable
        controller.updateFullScreenDrawable();

        // Change the screen orientation to PORTRAIT
        // TODO: Calculating the size according to if the view is on the whole screen or not
        setOrientation(SCREEN_ORIENTATION_PORTRAIT);

        UiUtils.showOtherViews((ViewGroup) getParent());

        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = originalWidth;
        params.height = originalHeight;
        setLayoutParams(params);

        showActionBar();
        Activity activity = (Activity) getContext();
        toggleSystemUiVisibility(activity.getWindow());
    }

    private void showActionBar() {
        if (supportActionBar != null) {
            supportActionBar.show();
        }

        if (actionBar != null) {
            actionBar.show();
        }
    }

    private void hideActionBar() {
        if (supportActionBar != null) {
            supportActionBar.hide();
        }

        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private void initOrientationListener() {
        orientationEventListener = new OrientationEventHandler(getContext(), this);
        orientationEventListener.enable();
    }

    public boolean shouldHandleOnBackPressed() {
        if (isFullscreen) {
            // Locks the screen orientation to portrait
            setOrientation(SCREEN_ORIENTATION_PORTRAIT);
            controller.updateFullScreenDrawable();
            return true;
        }

        return false;
    }

    public FullscreenVideoView isAutoStartEnabled(boolean autoStartEnabled) {
        isAutoStartEnabled = autoStartEnabled;
        return this;
    }

    public FullscreenVideoView enterFullscreenDrawable(int enterFullscreenDrawable) {
        this.controller.setEnterFullscreenDrawable(enterFullscreenDrawable);
        return this;
    }

    public FullscreenVideoView exitFullscreenDrawable(int exitFullscreenDrawable) {
        this.controller.setExitFullscreenDrawable(exitFullscreenDrawable);
        return this;
    }

    public FullscreenVideoView progressBarColor(int progressBarColor) {
        this.controller.setProgressBarColor(ContextCompat.getColor(getContext(), progressBarColor));
        return this;
    }

    public FullscreenVideoView playIcon(int playDrawable) {
        this.controller.setPlayDrawable(playDrawable);
        return this;
    }

    public FullscreenVideoView pauseIcon(int pauseDrawable) {
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

    public void surfaceCreated(SurfaceHolder holder) {
        if (videoMediaPlayer != null) {
            videoMediaPlayer.setDisplay(surfaceView.getHolder());
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Not used
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (videoMediaPlayer != null && isMediaPlayerPrepared) {
            videoMediaPlayer.pause();
        }
    }

    @Override
    public boolean isFullscreen() {
        return isFullscreen;
    }

    @Override
    public void toggleFullscreen() {
        if (isFullscreen) {
            isFullscreen = false;
            setOrientation(SCREEN_ORIENTATION_PORTRAIT);
        } else {
            isFullscreen = true;
            setOrientation(SCREEN_ORIENTATION_LANDSCAPE);
        }
    }
}
