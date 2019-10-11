/*
 * Copyright 2017 Dev Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bg.devlabs.fullscreenvideoview;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import bg.devlabs.fullscreenvideoview.listener.FullscreenVideoViewException;
import bg.devlabs.fullscreenvideoview.listener.OnErrorListener;
import bg.devlabs.fullscreenvideoview.orientation.OrientationManager;

import static android.media.MediaPlayer.MEDIA_ERROR_IO;
import static android.media.MediaPlayer.MEDIA_ERROR_MALFORMED;
import static android.media.MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK;
import static android.media.MediaPlayer.MEDIA_ERROR_SERVER_DIED;
import static android.media.MediaPlayer.MEDIA_ERROR_TIMED_OUT;
import static android.media.MediaPlayer.MEDIA_ERROR_UNKNOWN;
import static android.media.MediaPlayer.MEDIA_ERROR_UNSUPPORTED;
import static bg.devlabs.fullscreenvideoview.Constants.MEDIA_ERROR_GENERAL;
import static bg.devlabs.fullscreenvideoview.Constants.VIEW_TAG_CLICKED;

/**
 * Created by Slavi Petrov on 05.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
@SuppressWarnings("unused")
public class FullscreenVideoView extends FrameLayout {
    @Nullable
    private VideoSurfaceView surfaceView;
    @Nullable
    private SurfaceHolder surfaceHolder;
    @Nullable
    private ProgressBar progressBar;
    @Nullable
    private ImageView thumbnailImageView;
    @Nullable
    private VideoControllerView controller;
    @Nullable
    private VideoMediaPlayer videoMediaPlayer;
    private boolean isMediaPlayerPrepared;
    @Nullable
    private OrientationManager orientationManager;
    private SurfaceHolder.Callback surfaceHolderCallback;
    private boolean isPaused;
    private int previousOrientation;
    private int seekToTimeMillis;
    @Nullable
    private OnErrorListener onErrorListener;

    public FullscreenVideoView(@NonNull Context context) {
        super(context);
        init(null);
    }

    public FullscreenVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public FullscreenVideoView(@NonNull Context context,
                               @Nullable AttributeSet attrs,
                               int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        findChildViews();
        // Skip this init rows - needed when changing FullscreenVideoView properties in XML
        if (!isInEditMode()) {
            videoMediaPlayer = new VideoMediaPlayer(this);
            orientationManager = new OrientationManager(getContext(), this);
            orientationManager.enable();
        }
        setUpSurfaceHolder();
        if (controller != null) {
            controller.init(orientationManager, videoMediaPlayer, attrs);
        }
        setupProgressBarColor();
        setFocusableInTouchMode(true);
        requestFocus();
        initOnBackPressedListener();
        // Setup onTouch listener
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                view.performClick();
                if (controller != null) {
                    controller.show();
                }
                return false;
            }
        });
    }

    private void setUpSurfaceHolder() {
        if (surfaceView != null) {
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
            surfaceHolder = surfaceView.getHolder();
            surfaceHolder.addCallback(surfaceHolderCallback);
        }
    }

    private void findChildViews() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        layoutInflater.inflate(R.layout.fullscreen_video_view, this, true);
        surfaceView = findViewById(R.id.surface_view);
        progressBar = findViewById(R.id.progress_bar);
        controller = findViewById(R.id.video_controller);
        thumbnailImageView = findViewById(R.id.thumbnail_image_view);
    }

    private void initOnBackPressedListener() {
        setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return (event.getAction() == KeyEvent.ACTION_UP)
                        && (keyCode == KeyEvent.KEYCODE_BACK)
                        && (orientationManager != null
                        && orientationManager.shouldHandleOnBackPressed());
            }
        });
    }

    public Builder videoFile(File videoFile) {
        return new Builder(this, controller, orientationManager, videoMediaPlayer)
                .videoFile(videoFile);
    }

    public Builder videoUrl(String videoUrl) {
        return new Builder(this, controller, orientationManager, videoMediaPlayer)
                .videoUrl(videoUrl);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        ImageButton fullscreenButton = findViewById(R.id.fullscreen_media_button);
        String fullscreenButtonTag = (String) fullscreenButton.getTag();
        // Do not proceed if the FullscreenVideoView is not the clicked one
        if (!Objects.equals(fullscreenButtonTag, VIEW_TAG_CLICKED)) {
            return;
        }

        if (orientationManager == null) {
            return;
        }

        // Avoid calling onConfigurationChanged twice
        if (previousOrientation == newConfig.orientation) {
            return;
        }
        previousOrientation = newConfig.orientation;

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            orientationManager.activateFullscreen();

            // Focus the view which is in fullscreen mode, because otherwise the Activity will
            // handle the back button
            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            orientationManager.exitFullscreen();
            // Clear the Clicked tag in the fullscreen button
            fullscreenButton.setTag(null);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        handleOnDetach();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        isPaused = visibility != View.VISIBLE;
    }

    private void handleOnDetach() {
        if (controller != null) {
            controller.onDetach();
        }

        // Disable and null the OrientationEventListener
        if (orientationManager != null) {
            orientationManager.disable();
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
        orientationManager = null;
        videoMediaPlayer = null;
        surfaceHolder = null;
        surfaceView = null;
        progressBar = null;

        setOnKeyListener(null);
        setOnTouchListener(null);

        onErrorListener = null;
    }

    public void setupMediaPlayer(String videoPath) {
        showProgress();
        try {
            if (videoMediaPlayer != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    AudioAttributes audioAttributes = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                            .build();
                    videoMediaPlayer.setAudioAttributes(audioAttributes);
                } else {
                    videoMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                }

                videoMediaPlayer.setDataSource(videoPath);
                videoMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        hideProgressBar();
                        // Get the dimensions of the video
                        int videoWidth = videoMediaPlayer.getVideoWidth();
                        int videoHeight = videoMediaPlayer.getVideoHeight();
                        if (surfaceView != null) {
                            surfaceView.updateLayoutParams(videoWidth, videoHeight);
                        }
                        if (!isPaused) {
                            isMediaPlayerPrepared = true;
                            // Start media player if auto start is enabled
                            if (mediaPlayer != null && videoMediaPlayer.isAutoStartEnabled()) {
                                mediaPlayer.start();
                                hideThumbnail();
                            }
                        }
                        // Seek to a specific time
                        videoMediaPlayer.seekTo(seekToTimeMillis);
                    }
                });
                videoMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        handleMediaPlayerError(what);
                        return false;
                    }
                });
                videoMediaPlayer.prepareAsync();
            }
        } catch (IOException exception) {
            if (onErrorListener != null) {
                FullscreenVideoViewException fullscreenVideoViewException =
                        new FullscreenVideoViewException(exception.getLocalizedMessage());

                onErrorListener.onError(fullscreenVideoViewException);
            }
        }
    }

    private void handleMediaPlayerError(int what) {
        if (onErrorListener == null) return;

        switch (what) {
            case MEDIA_ERROR_IO: {
                onErrorListener.onError(new FullscreenVideoViewException(
                        MEDIA_ERROR_IO,
                        getContext().getString(R.string.media_error_io)
                ));

                break;
            }

            case MEDIA_ERROR_MALFORMED: {
                onErrorListener.onError(new FullscreenVideoViewException(
                        MEDIA_ERROR_MALFORMED,
                        getContext().getString(R.string.media_error_malformed)
                ));

                break;
            }

            case MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK: {
                onErrorListener.onError(new FullscreenVideoViewException(
                        MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK,
                        getContext().getString(R.string.media_error_not_valid_for_progressive_playback)
                ));

                break;
            }

            case MEDIA_ERROR_SERVER_DIED: {
                onErrorListener.onError(new FullscreenVideoViewException(
                        MEDIA_ERROR_SERVER_DIED,
                        getContext().getString(R.string.media_error_server_died)
                ));

                break;
            }

            case MEDIA_ERROR_TIMED_OUT: {
                onErrorListener.onError(new FullscreenVideoViewException(
                        MEDIA_ERROR_TIMED_OUT,
                        getContext().getString(R.string.media_error_timed_out)
                ));

                break;
            }

            case MEDIA_ERROR_UNKNOWN: {
                onErrorListener.onError(new FullscreenVideoViewException(
                        MEDIA_ERROR_UNKNOWN,
                        getContext().getString(R.string.media_error_unknown)
                ));

                break;
            }

            case MEDIA_ERROR_UNSUPPORTED: {
                onErrorListener.onError(new FullscreenVideoViewException(
                        MEDIA_ERROR_UNSUPPORTED,
                        getContext().getString(R.string.media_error_unsupported)
                ));

                break;
            }

            default: {
                onErrorListener.onError(new FullscreenVideoViewException(
                        MEDIA_ERROR_GENERAL,
                        getContext().getString(R.string.media_error_general)
                ));
            }
        }
    }

    void hideThumbnail() {
        if (thumbnailImageView != null && thumbnailImageView.getVisibility() == View.VISIBLE) {
            thumbnailImageView.setVisibility(GONE);
        }
    }

    private void setupProgressBarColor() {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        if (progressBar != null) {
            progressBar.animate().setDuration(shortAnimTime);
        }
    }

    private void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showProgress() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void toggleFullscreen() {
        if (orientationManager != null) {
            orientationManager.toggleFullscreen();
        }
    }

    public void enableAutoStart() {
        if (videoMediaPlayer != null) {
            videoMediaPlayer.enableAutoStart();
        }
    }

    public void onOrientationChanged() {
        // Update the fullscreen button drawable
        if (controller != null) {
            controller.updateFullScreenDrawable();
        }
        if (surfaceView != null && videoMediaPlayer != null) {
            surfaceView.updateLayoutParams(videoMediaPlayer.getVideoWidth(),
                    videoMediaPlayer.getVideoHeight());
        }
    }

    public void setVideoThumbnail(int thumbnailResId) {
        if (thumbnailImageView != null) {
            Bitmap scaledBitmap = BitmapScaler.scaleImage(getResources(), thumbnailResId);
            thumbnailImageView.setImageBitmap(scaledBitmap);
        }
    }

    public void pause() {
        if (videoMediaPlayer != null) {
            videoMediaPlayer.pause();
        }
    }

    public void play() {
        if (videoMediaPlayer != null) {
            videoMediaPlayer.start();
            hideThumbnail();
        }
    }

    /**
     * Hides the progress views - the current time TextView, the ProgressBar and
     * the end time TextView.
     */
    public void hideProgress() {
        if (controller != null) {
            controller.hideProgress();
        }
    }

    public void hideFullscreenButton() {
        if (controller != null) {
            controller.hideFullscreenButton();
        }
    }

    public void addOnErrorListener(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    public void setSeekToTimeMillis(int seekToTimeMillis) {
        this.seekToTimeMillis = seekToTimeMillis;
    }
}
