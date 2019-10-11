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
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;

import java.io.File;

import bg.devlabs.fullscreenvideoview.listener.OnErrorListener;
import bg.devlabs.fullscreenvideoview.listener.mediacontroller.MediaControllerListener;
import bg.devlabs.fullscreenvideoview.orientation.LandscapeOrientation;
import bg.devlabs.fullscreenvideoview.orientation.OrientationManager;
import bg.devlabs.fullscreenvideoview.orientation.PortraitOrientation;
import bg.devlabs.fullscreenvideoview.playbackspeed.PlaybackSpeedOptions;

/**
 * Created by Slavi Petrov on 25.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
@SuppressWarnings({"unused", "ClassWithTooManyMethods", "ClassNamingConvention"})
public class Builder {
    private final FullscreenVideoView fullscreenVideoView;
    private final VideoControllerView controller;
    private final OrientationManager orientationManager;
    private final VideoMediaPlayer videoMediaPlayer;

    Builder(FullscreenVideoView fullscreenVideoView,
            VideoControllerView controller,
            OrientationManager orientationManager,
            VideoMediaPlayer videoMediaPlayer) {
        this.fullscreenVideoView = fullscreenVideoView;
        this.controller = controller;
        this.orientationManager = orientationManager;
        this.videoMediaPlayer = videoMediaPlayer;
    }

    public Builder videoFile(@NonNull File videoFile) {
        fullscreenVideoView.setupMediaPlayer(videoFile.getPath());
        return this;
    }

    public Builder videoUrl(@NonNull String videoUrl) {
        fullscreenVideoView.setupMediaPlayer(videoUrl);
        return this;
    }

    /**
     * When called the video will start automatically when it's loaded and ready to be played.
     *
     * @return the builder instance
     */
    public Builder enableAutoStart() {
        fullscreenVideoView.enableAutoStart();
        return this;
    }

    /**
     * Changes the enter fullscreen drawable.
     *
     * @param drawable the drawable which will replace the default one
     * @return the builder instance
     */
    public Builder enterFullscreenDrawable(@NonNull Drawable drawable) {
        controller.setEnterFullscreenDrawable(drawable);
        return this;
    }

    /**
     * Changes the enter fullscreen drawable.
     *
     * @param drawableResId the resource id of the drawable which will replace the default one
     * @return the builder instance
     */
    public Builder enterFullscreenDrawable(@DrawableRes int drawableResId) {
        return enterFullscreenDrawable(getDrawable(drawableResId));
    }

    /**
     * Changes the exit fullscreen drawable.
     *
     * @param drawable the drawable which will replace the default one
     * @return the builder instance
     */
    public Builder exitFullscreenDrawable(@NonNull Drawable drawable) {
        controller.setExitFullscreenDrawable(drawable);
        return this;
    }

    /**
     * Changes the exit fullscreen drawable
     *
     * @param drawableResId the resource id of the drawable which will replace the default one
     * @return the builder instance
     */
    public Builder exitFullscreenDrawable(@DrawableRes int drawableResId) {
        return exitFullscreenDrawable(getDrawable(drawableResId));
    }

    /**
     * Changes the play drawable.
     *
     * @param drawable the drawable which will replace the default one
     * @return the builder instance
     */
    public Builder playDrawable(@NonNull Drawable drawable) {
        controller.setPlayDrawable(drawable);
        return this;
    }

    /**
     * Changes the play drawable.
     *
     * @param drawableResId the resource id of the drawable which will replace the default one
     * @return the builder instance
     */
    public Builder playDrawable(@DrawableRes int drawableResId) {
        return playDrawable(getDrawable(drawableResId));
    }

    /**
     * Changes the pause drawable.
     *
     * @param drawable the drawable which will replace the default one
     * @return the builder instance
     */
    public Builder pauseDrawable(@NonNull Drawable drawable) {
        controller.setPauseDrawable(drawable);
        return this;
    }

    /**
     * Changes the pause drawable.
     *
     * @param drawableResId the resource id of the drawable which will replace the default one
     * @return the builder instance
     */
    public Builder pauseDrawable(@DrawableRes int drawableResId) {
        return pauseDrawable(getDrawable(drawableResId));
    }

    /**
     * Changes the fast forward drawable.
     *
     * @param drawable the drawable which will replace the default one
     * @return the builder instance
     */
    public Builder fastForwardDrawable(@NonNull Drawable drawable) {
        controller.setFastForwardDrawable(drawable);
        return this;
    }

    /**
     * Changes the fast forward drawable.
     *
     * @param drawableResId the resource id of the drawable which will replace the default one
     * @return the builder instance
     */
    public Builder fastForwardDrawable(@DrawableRes int drawableResId) {
        return fastForwardDrawable(getDrawable(drawableResId));
    }

    /**
     * Changes the rewind drawable.
     *
     * @param drawable the drawable which will replace the default one
     * @return the builder instance
     */
    public Builder rewindDrawable(@NonNull Drawable drawable) {
        controller.setRewindDrawable(drawable);
        return this;
    }

    /**
     * Changes the rewind drawable.
     *
     * @param drawableResId the resource id of the drawable which will replace the default one
     * @return the builder instance
     */
    public Builder rewindDrawable(@DrawableRes int drawableResId) {
        return rewindDrawable(getDrawable(drawableResId));
    }

    /**
     * Gets a drawable by it's resource id.
     *
     * @param drawableResId the drawable resource id
     * @return the builder instance
     */
    private Drawable getDrawable(int drawableResId) {
        Context context = fullscreenVideoView.getContext();
        return ContextCompat.getDrawable(context, drawableResId);
    }

    /**
     * Changes the progress bar color.
     *
     * @param progressBarColor the progress bar color which will replace the default one
     * @return the builder instance
     */
    public Builder progressBarColor(int progressBarColor) {
        controller.setProgressBarColor(progressBarColor);
        return this;
    }

    /**
     * Changes the fast forward duration in seconds.
     *
     * @param fastForwardSeconds the fast forward duration in seconds
     * @return the builder instance
     */
    public Builder fastForwardSeconds(int fastForwardSeconds) {
        controller.setFastForwardDuration(fastForwardSeconds);
        return this;
    }

    /**
     * Changes the rewind duration in seconds.
     *
     * @param rewindSeconds the rewind duration in seconds
     * @return the builder instance
     */
    public Builder rewindSeconds(int rewindSeconds) {
        controller.setRewindDuration(rewindSeconds);
        return this;
    }

    /**
     * Sets the landscape orientation of the view.
     *
     * @param landscapeOrientation the preferred orientation in landscape
     * @return the builder instance
     * @see LandscapeOrientation#SENSOR
     * @see LandscapeOrientation#DEFAULT
     * @see LandscapeOrientation#REVERSE
     * @see LandscapeOrientation#USER
     */
    public Builder landscapeOrientation(LandscapeOrientation landscapeOrientation) {
        orientationManager.setLandscapeOrientation(landscapeOrientation);
        return this;
    }

    /**
     * Sets the portrait orientation of the view.
     *
     * @param portraitOrientation the preferred orientation in portrait
     * @return the builder instance
     * @see PortraitOrientation#SENSOR
     * @see PortraitOrientation#DEFAULT
     * @see PortraitOrientation#REVERSE
     * @see PortraitOrientation#USER
     */
    public Builder portraitOrientation(PortraitOrientation portraitOrientation) {
        orientationManager.setPortraitOrientation(portraitOrientation);
        return this;
    }

    /**
     * Disables the pause of the video.
     *
     * @return the builder instance
     */
    public Builder disablePause() {
        videoMediaPlayer.disablePause();
        return this;
    }

    /**
     * Adds a seek forward button.
     *
     * @return the builder instance
     */
    public Builder addSeekForwardButton() {
        videoMediaPlayer.addSeekForwardButton();
        return this;
    }

    /**
     * Adds a seek backward button.
     *
     * @return the builder instance
     */
    public Builder addSeekBackwardButton() {
        videoMediaPlayer.addSeekBackwardButton();
        return this;
    }

    /**
     * Adds a playback speed button.
     * <p>
     * Supports devices with Android API version 23 and above.
     *
     * @return the builder instance
     */
    @RequiresApi(Build.VERSION_CODES.M)
    public Builder addPlaybackSpeedButton() {
        videoMediaPlayer.addPlaybackSpeedButton();
        return this;
    }

    /**
     * Changes the playback speed options.
     *
     * @param playbackSpeedOptions the playback speed options which will replace the default ones
     * @return the builder instance
     */
    public Builder playbackSpeedOptions(PlaybackSpeedOptions playbackSpeedOptions) {
        controller.setPlaybackSpeedOptions(playbackSpeedOptions);
        return this;
    }

    /**
     * Adds a thumbnail to the video.
     *
     * @param thumbnailResId the thumbnail image resource id
     * @return the builder instance
     */
    public Builder thumbnail(int thumbnailResId) {
        fullscreenVideoView.setVideoThumbnail(thumbnailResId);
        return this;
    }

    /**
     * Hides all progress related views.
     *
     * @return the builder instance
     */
    public Builder hideProgress() {
        controller.hideProgress();
        return this;
    }

    /**
     * Hides the fullscreen button.
     *
     * @return the builder instance
     */
    public Builder hideFullscreenButton() {
        controller.hideFullscreenButton();
        return this;
    }

    /**
     * Adds an error listener which is called when an error occurs.
     *
     * @param onErrorListener listener for errors
     * @return the builder instance
     */
    public Builder addOnErrorListener(OnErrorListener onErrorListener) {
        fullscreenVideoView.addOnErrorListener(onErrorListener);
        return this;
    }

    /**
     * Adds a listener for media controller events.
     *
     * @return the builder instance
     */
    public Builder mediaControllerListener(MediaControllerListener mediaControllerListener) {
        controller.setOnMediaControllerListener(mediaControllerListener);
        return this;
    }

    /**
     * Seeks to a specified point of the video.
     *
     * @param timeMillis the value for the seek position
     */
    public Builder seekTo(int timeMillis) {
        fullscreenVideoView.setSeekToTimeMillis(timeMillis);
        return this;
    }
}
