package bg.devlabs.fullscreenvideoview;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import bg.devlabs.fullscreenvideoview.orientation.OrientationHelper;

/**
 * Created by Slavi Petrov on 05.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
@SuppressWarnings("unused")
public class FullscreenVideoView extends FrameLayout {
    @Nullable
    VideoSurfaceView surfaceView;
    @Nullable
    SurfaceHolder surfaceHolder;
    private SurfaceHolder.Callback surfaceHolderCallback;
    @Nullable
    private ProgressBar progressBar;
    @Nullable
    VideoControllerView controller;
    @Nullable
    VideoMediaPlayer videoMediaPlayer;
    boolean isMediaPlayerPrepared;
    private Builder builder;
    @Nullable
    private MediaPlayer.OnPreparedListener onPreparedListener;
    @Nullable
    private View.OnTouchListener onTouchListener;
    @Nullable
    OrientationHelper orientationHelper;

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
        // Skip this init rows - needed when changing FullscreenVideoView properties in XML
        if (!isInEditMode()) {
            videoMediaPlayer = new VideoMediaPlayer(this);
            initOrientationHandlers();
        }
        surfaceHolderCallback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (videoMediaPlayer != null) {
                    videoMediaPlayer.setDisplay(surfaceHolder);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (videoMediaPlayer != null && isMediaPlayerPrepared) {
                    videoMediaPlayer.pause();
                }
            }
        };
        if (surfaceView != null) {
            surfaceHolder = surfaceView.getHolder();
            surfaceHolder.addCallback(surfaceHolderCallback);
        }
        if (controller != null) {
            controller.init(this, videoMediaPlayer, attrs);
        }
        setupProgressBarColor();
        initOnBackPressedListener();

        // Setup VideoView
        setupOnTouchListener();
        onPreparedListener = new MediaPlayer.OnPreparedListener() {
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
                    if (mediaPlayer != null && videoMediaPlayer.isAutoStartEnabled()) {
                        isMediaPlayerPrepared = true;
                        mediaPlayer.start();
                    }
                }
            }
        };
    }

    private void initOrientationHandlers() {
        if (!isInEditMode()) {
            if (orientationHelper != null) {
                orientationHelper.enable();
            }
        }
    }

    private void findChildViews() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        layoutInflater.inflate(R.layout.fullscreen_video_view, this, true);
        surfaceView = findViewById(R.id.surface_view);
        progressBar = findViewById(R.id.progress_bar);
        controller = findViewById(R.id.video_controller);
    }

    private void initOnBackPressedListener() {
        setFocusableInTouchMode(true);
        requestFocus();
        setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return (event.getAction() == KeyEvent.ACTION_UP)
                        && (keyCode == KeyEvent.KEYCODE_BACK)
                        && orientationHelper.shouldHandleOnBackPressed();
            }
        });
    }

    public Builder videoFile(File videoFile) {
        builder = new Builder(this, controller, orientationHelper,
                videoMediaPlayer);
        builder.videoFile(videoFile);
        return builder;
    }

    public Builder videoUrl(String videoUrl) {
        builder = new Builder(this, controller, orientationHelper,
                videoMediaPlayer);
        builder.videoUrl(videoUrl);
        return builder;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (orientationHelper != null) {
                orientationHelper.activateFullscreen();
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (orientationHelper != null) {
                orientationHelper.exitFullscreen();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        handleOnDetach();
        super.onDetachedFromWindow();
    }

    private void handleOnDetach() {
        Log.d(FullscreenVideoView.class.getSimpleName(), "onDetachedFromWindow: ");
        if (controller != null) {
            controller.onDetach();
        }

        // Disable and null the OrientationEventListener
        if (orientationHelper != null) {
            orientationHelper.disable();
        }

        if (videoMediaPlayer != null) {
            videoMediaPlayer.onDetach();
        }

        if (surfaceHolder != null) {
            surfaceHolder.removeCallback(surfaceHolderCallback);
            surfaceHolder.getSurface().release();
        }

        if (surfaceView != null) {
            surfaceView.invalidate();
            surfaceView.destroyDrawingCache();
        }

        controller = null;
        orientationHelper = null;
        videoMediaPlayer = null;
        surfaceHolder = null;
        surfaceView = null;
        onTouchListener = null;
        onPreparedListener = null;
        progressBar = null;

        setOnKeyListener(null);
        setOnTouchListener(null);
    }

    void setupMediaPlayer(String videoPath) {
        try {
            showProgress();
            if (videoMediaPlayer != null) {
                videoMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                videoMediaPlayer.setDataSource(videoPath);
                videoMediaPlayer.setOnPreparedListener(onPreparedListener);
                videoMediaPlayer.prepareAsync();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupOnTouchListener() {
        onTouchListener = new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                view.performClick();
                if (controller != null) {
                    controller.show();
                }
                return false;
            }
        };
        setOnTouchListener(onTouchListener);
    }

    private void setupProgressBarColor() {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        if (progressBar != null) {
            progressBar.animate().setDuration(shortAnimTime);
        }
    }

    void hideProgress() {
        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showProgress() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    boolean isLandscape() {
        return orientationHelper.isLandscape();
    }

    void toggleFullscreen() {
        if (orientationHelper != null) {
            orientationHelper.toggleFullscreen();
        }
    }

    void enableAutoStart() {
        if (videoMediaPlayer != null) {
            videoMediaPlayer.enableAutoStart();
        }
    }

    public void onOrientationChanged() {
        // Update the fullscreen button drawable
        if (controller != null) {
            controller.updateFullScreenDrawable();
        }
        if (surfaceView != null) {
            if (orientationHelper.isLandscape()) {
                surfaceView.resetLayoutParams();
            } else {
                surfaceView.updateLayoutParams(videoMediaPlayer.getVideoWidth(),
                        videoMediaPlayer.getVideoHeight());
            }
        }
    }
}
