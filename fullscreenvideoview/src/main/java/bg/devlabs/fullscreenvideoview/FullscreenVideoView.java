package bg.devlabs.fullscreenvideoview;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

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
    private boolean landscape = false;
    private boolean isAutoStartEnabled;

    // Listeners
    private OrientationEventListener orientationEventListener;
    private MediaPlayer.OnPreparedListener onPreparedListener;
    private View.OnTouchListener onTouchListener;

    private ActionBar supportActionBar;
    private android.app.ActionBar actionBar;
    private int originalWidth;
    private int originalHeight;
    private ViewGroup parentLayout;

    public FullscreenVideoView(@NonNull Context context) {
        super(context);
        init();
    }

    public FullscreenVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FullscreenVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View root = inflater.inflate(R.layout.video_player, this, true);
        this.surfaceView = root.findViewById(R.id.surface_view);
        this.progressBar = root.findViewById(R.id.progress_bar);
    }

    // There is no ActionBar or Toolbar
    public void init(String videoPath, ViewGroup parentLayout, Lifecycle lifecycle) {
        setupBar();
        lifecycle.addObserver(new LifecycleEventObserver());
        this.parentLayout = parentLayout;
        this.videoPath = videoPath;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(surfaceHolderCallback);
        mediaPlayer = new MediaPlayer();
        controller = new VideoControllerView(getContext(), inflater, false, false);
        setupProgressBar();
        initOrientationListener();
        setupVideoView();
    }

    private void setupBar() {
        if (getContext() instanceof Activity) {
            this.actionBar = ((Activity) getContext()).getActionBar();
        }

        if (getContext() instanceof AppCompatActivity) {
            this.supportActionBar = ((AppCompatActivity) getContext()).getSupportActionBar();
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
        setupMediaPlayer(videoPath);
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

//                    int additionalDimens = getExtraDisplayItemsSize(toolbarHeight);
                //Get the width of the screen
                int screenWidth = displayMetrics.widthPixels;
                int screenHeight = displayMetrics.heightPixels;// - additionalDimens;

                //Get the SurfaceView layout parameters
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) surfaceView.getLayoutParams();
                if (videoHeight / screenHeight > videoWidth / screenWidth) {// &&
//                            videoHeight - videoWidth > additionalDimens) {
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

    private void setupMediaPlayer(String videoUrl) {
        try {
            mediaPlayer.setDataSource(videoUrl);
            showProgress();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(onPreparedListener);
            mediaPlayer.prepareAsync();
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
    }

    private void setupProgressBar() {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        progressBar.animate().setDuration(shortAnimTime);
    }

    VideoControllerView.MediaPlayerControl mediaPlayerControl =
            new VideoControllerView.MediaPlayerControl() {
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
                        activity.setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
                    } else {
                        activity.setRequestedOrientation(SCREEN_ORIENTATION_LANDSCAPE);
                    }

                }
            };

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

    public void makeVideoViewFullscreen() {
        hideOtherViews();

        Activity activity = ((Activity) getContext());
        // Save the video player original width and height
        this.originalWidth = getWidth();
        this.originalHeight = getHeight();
        // Change the orientation to landscape
        activity.setRequestedOrientation(SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        // TODO: Implement
//        onVideoFullScreen();
        isFullscreen = true;
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        if (DeviceUtils.hasSoftKeys(activity.getWindowManager().getDefaultDisplay()) &&
                DeviceUtils.isSystemBarOnBottom(getContext())) {
            height += DeviceUtils.getNavigationBarHeight(getResources());
        } else {
            width += DeviceUtils.getNavigationBarHeight(getResources());
        }

        ViewGroup.LayoutParams params = getLayoutParams();
        // TODO: Add check if the video should be landscape or portrait in isFullscreen
        params.width = width;
        params.height = height;
        setLayoutParams(params);

        // Hiding the supportToolbar
        hideToolbarOrActionBar();

        // Hide status bar
        toggleSystemUiVisibility(activity.getWindow());
    }

    private void resetVideoViewSize() {
        showOtherViews();
        Activity activity = (Activity) getContext();
        // TODO: Calculating the size according to if the view is on the whole screen or not
        activity.setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        isFullscreen = false;

        int width = originalWidth;
        int height = originalHeight;

        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = width;
        params.height = height;

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

    public void setAutoStartEnabled(boolean isAutoStartEnabled) {
        this.isAutoStartEnabled = isAutoStartEnabled;
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
            ((Activity) getContext()).setRequestedOrientation(
                    SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            controller.updateFullScreen();
            return true;
        }

        return false;
    }

    private class LifecycleEventObserver implements LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        void onDestroy() {
            handleOnDestroy();
        }
    }
}
