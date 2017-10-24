package bg.devlabs.fullscreenvideoview;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;

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
//todo move design stuff to the xml definition of the layout
public class FullscreenVideoView extends FrameLayout implements IFullscreenVideoView,
        SurfaceHolder.Callback {
    private VideoSurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private ProgressBar progressBar;
    private VideoControllerView controller;
    private boolean isFullscreen, isAutoStartEnabled, isMediaPlayerPrepared;
    private VideoMediaPlayer videoMediaPlayer;
    private String videoPath;
    private File videoFile;
    private ActionBar supportActionBar;
    private android.app.ActionBar actionBar;
    private int originalWidth, originalHeight;
    // Listeners
    private OrientationEventListener orientationEventListener;
    private MediaPlayer.OnPreparedListener onPreparedListener;
    private View.OnTouchListener onTouchListener;
    private View controllerRootView;
    private LandscapeOrientation landscapeOrientation = LandscapeOrientation.SENSOR;
    private PortraitOrientation portraitOrientation = PortraitOrientation.PORTRAIT;

    public FullscreenVideoView(@NonNull Context context) {
        super(context);
    }

    public FullscreenVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public FullscreenVideoView(@NonNull Context context, @Nullable AttributeSet attrs,
                               int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        findChildViews();
        setupBar();
        // Skip this init rows - needed when changing FullscreenVideoView properties in XML
        if (!isInEditMode()) {
            videoMediaPlayer = new VideoMediaPlayer(this);
            initOrientationListener();
        }

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        setupController();
        setupProgressBarColor();
        initOnBackPressedListener();

        // Setup VideoView
        setupOnTouchListener();
        setupOnPreparedListener();

        setupXmlViews(attrs);
    }

    private void setupController() {
        controllerRootView = findViewById(R.id.media_controller);
        controller = new VideoControllerView(getContext());
        controller.setRootView(controllerRootView);

        if (!isInEditMode()) {
            controller.setAnchorView(FullscreenVideoView.this);
        }
        controller.setMediaPlayer(videoMediaPlayer);
    }

    private void setupXmlViews(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.FullscreenVideoView, 0, 0);
        setupPlayPauseButton(a);
        setupFullscreenButton(a);
        setupFastForwardButton(a);
        setupRewindButton(a);
        setupProgressBar(a);
        // Recycle the TypedArray
        a.recycle();
    }

    private void setupProgressBar(TypedArray a) {
        SeekBar progressBar = controllerRootView.findViewById(R.id.progress_seek_bar);

        // TODO: Add different setters for the background and the thumb of the progress bar
        int progressBarColor = a.getColor(R.styleable.FullscreenVideoView_progress_color, 0);
        if (progressBarColor != 0) {
            progressBar.getProgressDrawable().setColorFilter(progressBarColor, PorterDuff.Mode.SRC_IN);
            progressBar.getThumb().setColorFilter(progressBarColor, PorterDuff.Mode.SRC_IN);
            controller.setProgressBarColor(progressBarColor);
        } else {
            // Set the default color
            progressBar.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            progressBar.getThumb().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        }

    }

    private void setupRewindButton(TypedArray a) {
        ImageButton rewindButton = controllerRootView.findViewById(R.id.rewind_media_button);

        Drawable rewindDrawable = a.getDrawable(R.styleable.FullscreenVideoView_rew_drawable);
        if (rewindDrawable != null) {
            rewindButton.setImageDrawable(rewindDrawable);
            controller.setRewindDrawable(rewindDrawable);
        } else {
            rewindButton.setImageResource(R.drawable.ic_fast_rewind_white_48dp);
        }
    }

    private void setupFastForwardButton(TypedArray a) {
        ImageButton ffwdButton = controllerRootView.findViewById(R.id.forward_media_button);

        Drawable ffwdDrawable = a.getDrawable(R.styleable.FullscreenVideoView_ffwd_drawable);
        if (ffwdDrawable != null) {
            ffwdButton.setImageDrawable(ffwdDrawable);
            controller.setFastForwardDrawable(ffwdDrawable);
        } else {
            ffwdButton.setImageResource(R.drawable.ic_fast_forward_white_48dp);
        }
    }

    private void setupFullscreenButton(TypedArray a) {
        ImageButton fullscreenButton = controllerRootView.findViewById(R.id.fullscreen_media_button);

        Drawable enterFullscreenDrawable = a.getDrawable(
                R.styleable.FullscreenVideoView_enter_fullscreen_drawable);
        if (enterFullscreenDrawable != null) {
            fullscreenButton.setImageDrawable(enterFullscreenDrawable);
            controller.setEnterFullscreenDrawable(enterFullscreenDrawable);
        } else {
            // Set the default drawable
            fullscreenButton.setImageResource(R.drawable.ic_fullscreen_white_48dp);
        }

        Drawable exitFullscreenDrawable = a.getDrawable(
                R.styleable.FullscreenVideoView_exit_fullscreen_drawable);
        // The exitFullscreenDrawable is not null, therefore pass it to the controller,
        // else there is a default value for it in the controller
        if (exitFullscreenDrawable != null) {
            controller.setExitFullscreenDrawable(exitFullscreenDrawable);
        }
    }

    private void setupPlayPauseButton(TypedArray a) {
        ImageButton playPauseButton = controllerRootView.findViewById(R.id.start_pause_media_button);

        Drawable playDrawable = a.getDrawable(R.styleable.FullscreenVideoView_play_drawable);
        if (playDrawable != null) {
            playPauseButton.setImageDrawable(playDrawable);
            controller.setPlayDrawable(playDrawable);
        } else {
            // Set the default drawable
            playPauseButton.setImageResource(R.drawable.ic_play_arrow_white_48dp);
        }

        Drawable pauseDrawable = a.getDrawable(R.styleable.FullscreenVideoView_pause_drawable);
        // The pauseDrawable is not null, therefore pass it to the controller, else there is a
        // default value for it in the controller
        if (pauseDrawable != null) {
            controller.setPauseDrawable(pauseDrawable);
        }
    }

    private void initOrientationListener() {
        if (!isInEditMode()) {
            orientationEventListener = new OrientationEventHandler(getContext(), this);
            orientationEventListener.enable();
        }
    }

    private void findChildViews() {
        LayoutInflater layoutInflater = getLayoutInflater();
        View root = layoutInflater.inflate(R.layout.video_player, this, true);
        this.surfaceView = root.findViewById(R.id.surface_view);
        this.progressBar = root.findViewById(R.id.progress_bar);
    }

    public FullscreenVideoView videoFile(@NonNull File videoFile) {
        this.videoFile = videoFile;
        setupMediaPlayer();
        return this;
    }

    public FullscreenVideoView videoPath(@NonNull String videoPath) {
        this.videoPath = videoPath;
        setupMediaPlayer();
        return this;
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
        handleOnDetach();
        super.onDetachedFromWindow();
    }

    public void handleOnDetach() {
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
                Log.d(FullscreenVideoView.class.getSimpleName(), "onPrepared: ");
                if (((Activity) getContext()).isDestroyed()) {
                    return;
                }
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

    private void setupProgressBarColor() {
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
        Activity activity = ((Activity) getContext());
        setOrientation(landscapeOrientation.getValue());

        UiUtils.hideOtherViews((ViewGroup) getParent());

        // Save the video player original width and height
        this.originalWidth = getWidth();
        this.originalHeight = getHeight();
        // TODO: Add check if the video should be landscape or portrait in isFullscreen
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

    public boolean shouldHandleOnBackPressed() {
        if (isFullscreen) {
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
    public boolean isFullscreen() {
        return isFullscreen;
    }

    @Override
    public void toggleFullscreen() {
        if (isFullscreen) {
            isFullscreen = false;
            setOrientation(portraitOrientation.getValue());
        } else {
            isFullscreen = true;
            setOrientation(landscapeOrientation.getValue());
        }
    }
}
