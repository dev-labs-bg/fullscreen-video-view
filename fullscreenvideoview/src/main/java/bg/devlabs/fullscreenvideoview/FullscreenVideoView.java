package bg.devlabs.fullscreenvideoview;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;

/**
 * Created by Slavi Petrov on 05.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
public class FullscreenVideoView extends FrameLayout {
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private ProgressBar progressBar;
    private MediaPlayer mediaPlayer;
    private VideoControllerView controller;
    private boolean isFullscreen;
    private String videoPath;
    private File videoFile;
    private boolean landscape = false;

    // Listeners
    private OrientationEventListener orientationEventListener;
    private MediaPlayer.OnPreparedListener onPreparedListener;
    private View.OnTouchListener onTouchListener;

    private ActionBar supportActionBar;
    private android.app.ActionBar actionBar;
    private int originalWidth;
    private int originalHeight;
    private ViewGroup parentLayout;

    private boolean isAutoStartEnabled;


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

    public FullscreenVideoView init(@NonNull File videoFile, ViewGroup parentLayout, Lifecycle lifecycle) {
        this.videoFile = videoFile;
        init(parentLayout, lifecycle);
        return this;
    }

    public FullscreenVideoView init(@NonNull String videoPath, ViewGroup parentLayout, Lifecycle lifecycle) {
        this.videoPath = videoPath;
        init(parentLayout, lifecycle);
        return this;
    }

    private void init(ViewGroup parentLayout, Lifecycle lifecycle) {
        setupBar();
        this.parentLayout = parentLayout;
        lifecycle.addObserver(new LifecycleEventObserver());

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(surfaceHolderCallback);
        mediaPlayer = new MediaPlayer();
        controller = new VideoControllerView(getContext(), getLayoutInflater(), false, false);

        setupProgressBar();
        initOrientationListener();
        setupVideoView();
    }

    private LayoutInflater getLayoutInflater() {
        Context context = getContext();
        return LayoutInflater.from(context);
    }

    private void setupBar() {
        Context context = getContext();
        if (context instanceof AppCompatActivity) {
            this.supportActionBar = ((AppCompatActivity) context).getSupportActionBar();
        } else if (context instanceof Activity) {
            this.actionBar = ((Activity) context).getActionBar();
        }
    }

    private List<View> getAllChildren(ViewGroup parentLayout) {
        List<View> visited = new ArrayList<>();
        List<View> unvisited = new ArrayList<>();
        unvisited.add(parentLayout);

        while (!unvisited.isEmpty()) {
            View child = unvisited.remove(0);
            visited.add(child);

            if (child instanceof FullscreenVideoView) {
                continue;
            }

            if (!(child instanceof ViewGroup)) {
                continue;
            }

            ViewGroup group = (ViewGroup) child;
            final int childCount = group.getChildCount();
            for (int i = 0; i < childCount; i++) {
                unvisited.add(group.getChildAt(i));
            }
        }

        return visited;
    }

    protected void setupVideoView() {
        setupOnTouchListener();
        setupOnPreparedListener();
        setupMediaPlayer();
        setOnTouchListener(onTouchListener);
    }

    private void setupOnPreparedListener() {
        onPreparedListener = new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                if (((Activity) getContext()).isDestroyed()) {
                    return;
                }
                hideProgress();

                //Get the dimensions of the video
                int videoWidth = mediaPlayer.getVideoWidth();
                int videoHeight = mediaPlayer.getVideoHeight();

                DisplayMetrics displayMetrics = DeviceUtils.getDisplayMetrics(getContext());

                //Get the width of the screen
                int screenWidth = displayMetrics.widthPixels;
                int screenHeight = displayMetrics.heightPixels;// - additionalDimens;

                //Get the SurfaceView layout parameters
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) surfaceView.getLayoutParams();
                if (videoHeight / screenHeight > videoWidth / screenWidth) {
                    lp.height = screenHeight;
                    //Set the width of the SurfaceView to match the aspect ratio of the video
                    //be sure to cast these as floats otherwise the calculation will likely be 0
                    lp.width = (int) (((float) videoWidth / (float) videoHeight) * (float) screenHeight);
                } else {
                    //Set the width of the SurfaceView to the width of the screen
                    lp.width = screenWidth;
                    //Set the height of the SurfaceView to match the aspect ratio of the video
                    //be sure to cast these as floats otherwise the calculation will likely be 0
                    lp.height = (int) (((float) videoHeight / (float) videoWidth) * (float) screenWidth);
                }

                lp.gravity = Gravity.CENTER;

                //Commit the layout parameters
                surfaceView.setLayoutParams(lp);

                controller.setMediaPlayer(mediaPlayerControl);
                controller.setAnchorView(FullscreenVideoView.this);
                if (mediaPlayerControl != null && isAutoStartEnabled) {
                    mediaPlayerControl.start();
                }
            }
        };
    }

    private void setupMediaPlayer() {
        try {
            mediaPlayer.setDataSource(getVideoPath());
            showProgress();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(onPreparedListener);
            mediaPlayer.prepareAsync();
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
    }

    private void setupProgressBar() {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        progressBar.animate().setDuration(shortAnimTime);
    }

    MediaPlayerControl mediaPlayerControl = new MediaPlayerControl() {
        @Override
        public void start() {
            mediaPlayer.start();
        }

        @Override
        public void pause() {
            mediaPlayer.pause();
        }

        @Override
        public int getDuration() {
            if (mediaPlayer != null) {
                return mediaPlayer.getDuration();
            }
            return 0;
        }

        @Override
        public int getCurrentPosition() {
            if (mediaPlayer != null) {
                return mediaPlayer.getCurrentPosition();
            }
            return 0;
        }

        @Override
        public void seekTo(int pos) {
            mediaPlayer.seekTo(pos);
        }

        @Override
        public boolean isPlaying() {
            return mediaPlayer != null && mediaPlayer.isPlaying();
        }

        @Override
        public int getBufferPercentage() {
            return 0;
        }

        @Override
        public boolean canPause() {
            return true;
        }

        @Override
        public boolean canSeekBackward() {
            return true;
        }

        @Override
        public boolean canSeekForward() {
            return true;
        }

        @Override
        public boolean isFullScreen() {
            return isFullscreen;
        }

        @Override
        public void toggleFullScreen() {
            Activity activity = (Activity) getContext();
            if (isFullscreen) {
                isFullscreen = false;
                activity.setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
            } else {
                isFullscreen = true;
                activity.setRequestedOrientation(SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
    };

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

    public void handleConfigurationChange(Configuration newConfig) {
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            makeVideoViewFullscreen();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            resetVideoViewSize();
        }
    }

    private void makeVideoViewFullscreen() {
        // Update isFullscreen flag
        if (!isFullscreen) {
            isFullscreen = true;
        }

        // Update the fullscreen button drawable
        controller.updateFullScreenDrawable();

        // Change the screen orientation to SENSOR_LANDSCAPE
        Activity activity = ((Activity) getContext());
        activity.setRequestedOrientation(SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        hideOtherViews();

        // Save the video player original width and height
        this.originalWidth = getWidth();
        this.originalHeight = getHeight();
        // Change the orientation to landscape

        // TODO: Implement
//        onVideoFullScreen();
        // TODO: Add check if the video should be landscape or portrait in isFullscreen
        updateLayoutParams(activity);

        // Hiding the supportToolbar
        hideToolbarOrActionBar();

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

    private void resetVideoViewSize() {
        // Update isFullscreen flag
        if (isFullscreen) {
            isFullscreen = false;
        }

        // Update the fullscreen button drawable
        controller.updateFullScreenDrawable();

        // Change the screen orientation to PORTRAIT
        Activity activity = (Activity) getContext();
        // TODO: Calculating the size according to if the view is on the whole screen or not
        activity.setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);

        showOtherViews();

        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = originalWidth;
        params.height = originalHeight;
        setLayoutParams(params);

        // Show the supportToolbar again
        // TODO: Implement
        showToolbarOrActionBar();
        toggleSystemUiVisibility(activity.getWindow());
    }

    private void hideOtherViews() {
        List<View> views = getAllChildren(parentLayout);
        int size = views.size();
        for (int i = 1; i < size; i++) {
            View view = views.get(i);
            if (view instanceof FullscreenVideoView) {
                continue;
            }

            view.setVisibility(GONE);
        }
    }

    private void showOtherViews() {
        List<View> views = getAllChildren(parentLayout);
        int size = views.size();
        for (int i = 1; i < size; i++) {
            View view = views.get(i);
            if (view instanceof FullscreenVideoView) {
                continue;
            }

            view.setVisibility(VISIBLE);
        }
    }

    private void showToolbarOrActionBar() {
        if (supportActionBar != null) {
            supportActionBar.show();
        }

        if (actionBar != null) {
            actionBar.show();
        }
    }

    private void hideToolbarOrActionBar() {
        if (supportActionBar != null) {
            supportActionBar.hide();
        }

        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private void initOrientationListener() {
        orientationEventListener = new OrientationEventListener(getContext()) {
            @Override
            public void onOrientationChanged(int orientation) {
                int epsilon = 10;
                int leftLandscape = 90;
                int rightLandscape = 270;
                int portrait = 0;
                Activity activity = (Activity) getContext();
                if ((epsilonCheck(orientation, leftLandscape, epsilon) ||
                        epsilonCheck(orientation, rightLandscape, epsilon)) && !landscape) {
                    activity.setRequestedOrientation(SCREEN_ORIENTATION_SENSOR);
                    landscape = true;
                }

                if (epsilonCheck(orientation, portrait, epsilon) && landscape) {
                    activity.setRequestedOrientation(SCREEN_ORIENTATION_SENSOR);
                    landscape = false;
                }
            }

            private boolean epsilonCheck(int a, int b, int epsilon) {
                return a > b - epsilon && a < b + epsilon;
            }
        };
        orientationEventListener.enable();
    }

    private void handleOnDestroy() {
        controller.onDestroy();
        onPreparedListener = null;
        onTouchListener = null;
        mediaPlayer = null;
        mediaPlayerControl = null;
        // Disable and null the OrientationEventListener
        if (orientationEventListener != null) {
            orientationEventListener.disable();
            orientationEventListener = null;
        }

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        if (surfaceHolder != null) {
            surfaceHolder.removeCallback(surfaceHolderCallback);
            surfaceHolder.getSurface().release();
        }
    }

    public boolean shouldHandleOnBackPressed() {
        if (isFullscreen) {
            // Locks the screen orientation to portrait
            ((Activity) getContext()).setRequestedOrientation(SCREEN_ORIENTATION_SENSOR_PORTRAIT);
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

    private class LifecycleEventObserver implements LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        void onDestroy() {
            handleOnDestroy();
        }
    }

    private SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (mediaPlayer == null) {
                return;
            }
            mediaPlayer.setDisplay(surfaceView.getHolder());
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }
        }
    };
}
