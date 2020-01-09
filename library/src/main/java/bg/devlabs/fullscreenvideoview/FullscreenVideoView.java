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
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
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

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import bg.devlabs.fullscreenvideoview.listener.FullscreenVideoViewException;
import bg.devlabs.fullscreenvideoview.listener.OnErrorListener;
import bg.devlabs.fullscreenvideoview.listener.OnVideoCompletedListener;
import bg.devlabs.fullscreenvideoview.listener.mediacontroller.MediaControllerListener;
import bg.devlabs.fullscreenvideoview.model.Arguments;
import bg.devlabs.fullscreenvideoview.orientation.LandscapeOrientation;
import bg.devlabs.fullscreenvideoview.orientation.OrientationManager;
import bg.devlabs.fullscreenvideoview.orientation.PortraitOrientation;
import bg.devlabs.fullscreenvideoview.playbackspeed.PlaybackSpeedOptions;

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
    @Nullable
    private AttributeSet attrs = null;
    private Arguments args = new Arguments();
    @Nullable
    private OnVideoCompletedListener onVideoCompletedListener;

    public FullscreenVideoView(@NonNull Context context) {
        super(context);
        init(null);
    }

    public FullscreenVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.attrs = attrs;
        init(attrs);
    }

    public FullscreenVideoView(@NonNull Context context,
                               @Nullable AttributeSet attrs,
                               int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.attrs = attrs;
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        findChildViews();
        // Skip this init rows - needed when changing FullscreenVideoView properties in XML
        if (!isInEditMode()) {
            videoMediaPlayer = new VideoMediaPlayer();
            orientationManager = new OrientationManager(getContext(), this);
            orientationManager.enable();
        }
        setUpSurfaceHolder();
        if (controller != null) {
            controller.init(
                    orientationManager,
                    videoMediaPlayer,
                    attrs,
                    new FullscreenVideoViewInteractor() {
                        @Override
                        public void toggleFullscreen() {
                            FullscreenVideoView.this.toggleFullscreen();
                        }

                        @Override
                        public void hideThumbnail() {
                            FullscreenVideoView.this.hideThumbnail();
                        }
                    }
            );
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

    public FullscreenVideoView videoFile(File videoFile) {
        setupMediaPlayer(videoFile.getPath());
        return this;
    }

    public FullscreenVideoView videoUrl(String videoUrl) {
        setupMediaPlayer(videoUrl);
        return this;
    }

    /**
     * When called the video will start automatically when it's loaded and ready to be played.
     *
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView enableAutoStart() {
        if (videoMediaPlayer != null) {
            videoMediaPlayer.enableAutoStart();
            args.autoStartEnabled = true;
        }

        return this;
    }

    /**
     * Changes the enter fullscreen drawable.
     *
     * @param drawable the drawable which will replace the default one
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView enterFullscreenDrawable(@NonNull Drawable drawable) {
        if (controller != null) {
            controller.setEnterFullscreenDrawable(drawable);
        }
        args.enterFullscreenDrawable = drawable;
        return this;
    }

    /**
     * Changes the enter fullscreen drawable.
     *
     * @param drawableResId the resource id of the drawable which will replace the default one
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView enterFullscreenDrawable(@DrawableRes int drawableResId) {
        return enterFullscreenDrawable(getDrawable(drawableResId));
    }

    /**
     * Changes the exit fullscreen drawable.
     *
     * @param drawable the drawable which will replace the default one
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView exitFullscreenDrawable(@NonNull Drawable drawable) {
        if (controller != null) {
            controller.setExitFullscreenDrawable(drawable);
        }
        args.exitFullscreenDrawable = drawable;
        return this;
    }

    /**
     * Changes the exit fullscreen drawable
     *
     * @param drawableResId the resource id of the drawable which will replace the default one
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView exitFullscreenDrawable(@DrawableRes int drawableResId) {
        return exitFullscreenDrawable(getDrawable(drawableResId));
    }

    /**
     * Changes the play drawable.
     *
     * @param drawable the drawable which will replace the default one
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView playDrawable(@NonNull Drawable drawable) {
        if (controller != null) {
            controller.setPlayDrawable(drawable);
        }
        args.playDrawable = drawable;
        return this;
    }

    /**
     * Changes the play drawable.
     *
     * @param drawableResId the resource id of the drawable which will replace the default one
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView playDrawable(@DrawableRes int drawableResId) {
        return playDrawable(getDrawable(drawableResId));
    }

    /**
     * Changes the pause drawable.
     *
     * @param drawable the drawable which will replace the default one
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView pauseDrawable(@NonNull Drawable drawable) {
        if (controller != null) {
            controller.setPauseDrawable(drawable);
        }
        args.pauseDrawable = drawable;
        return this;
    }

    /**
     * Changes the pause drawable.
     *
     * @param drawableResId the resource id of the drawable which will replace the default one
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView pauseDrawable(@DrawableRes int drawableResId) {
        return pauseDrawable(getDrawable(drawableResId));
    }

    /**
     * Changes the fast forward drawable.
     *
     * @param drawable the drawable which will replace the default one
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView fastForwardDrawable(@NonNull Drawable drawable) {
        if (controller != null) {
            controller.setFastForwardDrawable(drawable);
        }
        args.fastForwardDrawable = drawable;
        return this;
    }

    /**
     * Changes the fast forward drawable.
     *
     * @param drawableResId the resource id of the drawable which will replace the default one
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView fastForwardDrawable(@DrawableRes int drawableResId) {
        return fastForwardDrawable(getDrawable(drawableResId));
    }

    /**
     * Changes the rewind drawable.
     *
     * @param drawable the drawable which will replace the default one
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView rewindDrawable(@NonNull Drawable drawable) {
        if (controller != null) {
            controller.setRewindDrawable(drawable);
        }
        args.rewindDrawable = drawable;
        return this;
    }

    /**
     * Changes the rewind drawable.
     *
     * @param drawableResId the resource id of the drawable which will replace the default one
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView rewindDrawable(@DrawableRes int drawableResId) {
        return rewindDrawable(getDrawable(drawableResId));
    }

    /**
     * Changes the progress bar color.
     *
     * @param progressBarColor the progress bar color which will replace the default one
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView progressBarColor(int progressBarColor) {
        if (controller != null) {
            controller.setProgressBarColor(progressBarColor);
        }
        args.progressBarColor = progressBarColor;
        return this;
    }

    /**
     * Changes the fast forward duration in seconds.
     *
     * @param fastForwardSeconds the fast forward duration in seconds
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView fastForwardSeconds(int fastForwardSeconds) {
        if (controller != null) {
            controller.setFastForwardDuration(fastForwardSeconds);
        }
        args.fastForwardSeconds = fastForwardSeconds;
        return this;
    }

    /**
     * Changes the rewind duration in seconds.
     *
     * @param rewindSeconds the rewind duration in seconds
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView rewindSeconds(int rewindSeconds) {
        if (controller != null) {
            controller.setRewindDuration(rewindSeconds);
        }
        args.rewindSeconds = rewindSeconds;
        return this;
    }

    /**
     * Sets the landscape orientation of the view.
     *
     * @param landscapeOrientation the preferred orientation in landscape
     * @return the fullscreenVideoView instance
     * @see LandscapeOrientation#SENSOR
     * @see LandscapeOrientation#DEFAULT
     * @see LandscapeOrientation#REVERSE
     * @see LandscapeOrientation#USER
     */
    public FullscreenVideoView landscapeOrientation(LandscapeOrientation landscapeOrientation) {
        if (orientationManager != null) {
            orientationManager.setLandscapeOrientation(landscapeOrientation);
        }
        args.landscapeOrientation = landscapeOrientation;
        return this;
    }

    /**
     * Sets the portrait orientation of the view.
     *
     * @param portraitOrientation the preferred orientation in portrait
     * @return the fullscreenVideoView instance
     * @see PortraitOrientation#SENSOR
     * @see PortraitOrientation#DEFAULT
     * @see PortraitOrientation#REVERSE
     * @see PortraitOrientation#USER
     */
    public FullscreenVideoView portraitOrientation(PortraitOrientation portraitOrientation) {
        if (orientationManager != null) {
            orientationManager.setPortraitOrientation(portraitOrientation);
        }
        args.portraitOrientation = portraitOrientation;
        return this;
    }

    /**
     * Disables the pause of the video.
     *
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView disablePause() {
        if (videoMediaPlayer != null) {
            videoMediaPlayer.disablePause();
        }
        args.disablePause = true;
        return this;
    }

    /**
     * Adds a seek forward button.
     *
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView addSeekForwardButton() {
        if (videoMediaPlayer != null) {
            videoMediaPlayer.addSeekForwardButton();
        }
        args.addSeekForwardButton = true;
        return this;
    }

    /**
     * Adds a seek backward button.
     *
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView addSeekBackwardButton() {
        if (videoMediaPlayer != null) {
            videoMediaPlayer.addSeekBackwardButton();
        }
        args.addSeekBackwardButton = true;
        return this;
    }

    /**
     * Adds a playback speed button.
     * <p>
     * Supports devices with Android API version 23 and above.
     *
     * @return the fullscreenVideoView instance
     */
    @RequiresApi(Build.VERSION_CODES.M)
    public FullscreenVideoView addPlaybackSpeedButton() {
        if (videoMediaPlayer != null) {
            videoMediaPlayer.addPlaybackSpeedButton();
        }
        args.addPlaybackSpeedButton = true;
        return this;
    }

    /**
     * Changes the playback speed options.
     *
     * @param playbackSpeedOptions the playback speed options which will replace the default ones
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView playbackSpeedOptions(PlaybackSpeedOptions playbackSpeedOptions) {
        if (controller != null) {
            controller.setPlaybackSpeedOptions(playbackSpeedOptions);
        }
        args.playbackSpeedOptions = playbackSpeedOptions;
        return this;
    }

    /**
     * Adds a thumbnail to the video.
     *
     * @param thumbnailResId the thumbnail image resource id
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView thumbnail(int thumbnailResId) {
        if (thumbnailImageView != null) {
            Bitmap thumbnail = BitmapScaler.scaleImage(getResources(), thumbnailResId);
            thumbnailImageView.setImageBitmap(thumbnail);
            args.thumbnailResId = thumbnailResId;
        }
        return this;
    }

    /**
     * Hides all progress related views.
     *
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView hideProgress() {
        if (controller != null) {
            controller.hideProgress();
        }
        args.hideProgress = true;
        return this;
    }

    /**
     * Hides the fullscreen button.
     *
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView hideFullscreenButton() {
        if (controller != null) {
            controller.hideFullscreenButton();
        }
        args.hideFullscreenButton = true;
        return this;
    }

    /**
     * Adds an error listener which is called when an error occurs.
     *
     * @param onErrorListener listener for errors
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView addOnErrorListener(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
        return this;
    }

    /**
     * Adds a listener which is called when the video playback is completed.
     *
     * @param listener the listener for video completion
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView addOnVideoCompletedListener(OnVideoCompletedListener listener) {
        this.onVideoCompletedListener = listener;
        return this;
    }

    /**
     * Adds a listener for media controller events.
     *
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView mediaControllerListener(
            MediaControllerListener mediaControllerListener
    ) {
        if (controller != null) {
            controller.setOnMediaControllerListener(mediaControllerListener);
        }
        return this;
    }

    /**
     * Seeks to a specified point of the video.
     *
     * @param timeMillis the value for the seek position
     * @return the fullscreenVideoView instance
     */
    public FullscreenVideoView setSeekToTimeMillis(int timeMillis) {
        this.seekToTimeMillis = timeMillis;
        args.seekToTimeMillis = timeMillis;
        return this;
    }

    /**
     * Gets a drawable by it's resource id.
     *
     * @param drawableResId the drawable resource id
     * @return the fullscreenVideoView instance
     */
    private Drawable getDrawable(int drawableResId) {
        return ContextCompat.getDrawable(getContext(), drawableResId);
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
            controller = null;
        }

        if (orientationManager != null) {
            orientationManager.disable();
            orientationManager = null;
        }

        if (videoMediaPlayer != null) {
            videoMediaPlayer.onDetach();
            videoMediaPlayer = null;
        }

        if (surfaceHolder != null) {
            surfaceHolder.removeCallback(surfaceHolderCallback);
            surfaceHolder.getSurface().release();
            surfaceHolder = null;
        }

        if (surfaceView != null) {
            surfaceView.invalidate();
            surfaceView.destroyDrawingCache();
            surfaceView = null;
        }

        progressBar = null;
        surfaceHolderCallback = null;
        attrs = null;

        setOnKeyListener(null);
        setOnTouchListener(null);

        onErrorListener = null;

        detachAllViewsFromParent();
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

                videoMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        if (onVideoCompletedListener == null) return;

                        onVideoCompletedListener.onFinished();
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

    public void changeUrl(@NonNull final String url) {
        handleOnDetach();

        init(attrs);
        // TODO: Add save the selected Builder attributes from the user
        setupMediaPlayer(url);

        if (args.autoStartEnabled) {
            enableAutoStart();
        }

        Drawable enterFullscreenDrawable = args.enterFullscreenDrawable;
        if (enterFullscreenDrawable != null) {
            enterFullscreenDrawable(enterFullscreenDrawable);
        }

        Drawable exitFullscreenDrawable = args.exitFullscreenDrawable;
        if (exitFullscreenDrawable != null) {
            exitFullscreenDrawable(exitFullscreenDrawable);
        }

        Drawable playDrawable = args.playDrawable;
        if (playDrawable != null) {
            playDrawable(playDrawable);
        }

        Drawable pauseDrawable = args.pauseDrawable;
        if (pauseDrawable != null) {
            pauseDrawable(pauseDrawable);
        }

        Drawable fastForwardDrawable = args.fastForwardDrawable;
        if (fastForwardDrawable != null) {
            fastForwardDrawable(fastForwardDrawable);
        }

        Drawable rewindDrawable = args.rewindDrawable;
        if (rewindDrawable != null) {
            rewindDrawable(rewindDrawable);
        }

        int progressBarColor = args.progressBarColor;
        if (progressBarColor != -1) {
            progressBarColor(progressBarColor);
        }

        int fastForwardSeconds = args.fastForwardSeconds;
        if (fastForwardSeconds != -1) {
            fastForwardSeconds(fastForwardSeconds);
        }

        int rewindSeconds = args.rewindSeconds;
        if (rewindSeconds != -1) {
            rewindSeconds(rewindSeconds);
        }

        LandscapeOrientation landscapeOrientation = args.landscapeOrientation;
        if (landscapeOrientation != null) {
            landscapeOrientation(landscapeOrientation);
        }

        PortraitOrientation portraitOrientation = args.portraitOrientation;
        if (portraitOrientation != null) {
            portraitOrientation(portraitOrientation);
        }

        if (args.disablePause) {
            disablePause();
        }

        if (args.addSeekForwardButton) {
            addSeekForwardButton();
        }

        if (args.addSeekBackwardButton) {
            addSeekBackwardButton();
        }

        if (args.addPlaybackSpeedButton) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                addPlaybackSpeedButton();
            }
        }

        PlaybackSpeedOptions playbackSpeedOptions = args.playbackSpeedOptions;
        if (playbackSpeedOptions != null) {
            playbackSpeedOptions(playbackSpeedOptions);
        }

        int thumbnailResId = args.thumbnailResId;
        if (thumbnailResId != -1) {
            thumbnail(thumbnailResId);
        }

        if (args.hideProgress) {
            hideProgress();
        }

        if (args.hideFullscreenButton) {
            hideFullscreenButton();
        }

        int timeMillis = args.seekToTimeMillis;
        if (args.seekToTimeMillis != -1) {
            setSeekToTimeMillis(timeMillis);
        }
    }
}
