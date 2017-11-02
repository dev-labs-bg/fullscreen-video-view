package bg.devlabs.fullscreenvideoview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import java.io.File;

import bg.devlabs.fullscreenvideoview.orientation.LandscapeOrientation;
import bg.devlabs.fullscreenvideoview.orientation.OrientationDelegate;
import bg.devlabs.fullscreenvideoview.orientation.PortraitOrientation;

/**
 * Created by Slavi Petrov on 25.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
@SuppressWarnings("unused")
public class Builder {
    private final FullscreenVideoView fullscreenVideoView;
    private final VideoControllerView controller;
    private final OrientationDelegate orientationDelegate;
    private final VideoMediaPlayer videoMediaPlayer;

    Builder(FullscreenVideoView fullscreenVideoView, VideoControllerView controller,
            OrientationDelegate orientationDelegate, VideoMediaPlayer videoMediaPlayer) {
        this.fullscreenVideoView = fullscreenVideoView;
        this.controller = controller;
        this.orientationDelegate = orientationDelegate;
        this.videoMediaPlayer = videoMediaPlayer;
    }

    void videoFile(@NonNull final File videoFile) {
        fullscreenVideoView.setupMediaPlayer(videoFile.getPath());
    }

    void videoPath(@NonNull final String videoPath) {
        fullscreenVideoView.setupMediaPlayer(videoPath);
    }

    public Builder autoStartEnabled(boolean isAutoStartEnabled) {
        fullscreenVideoView.setAutoStartEnabled(isAutoStartEnabled);
        return this;
    }

    public Builder enterFullscreenDrawable(@NonNull Drawable enterFullscreenDrawable) {
        controller.setEnterFullscreenDrawable(enterFullscreenDrawable);
        return this;
    }

    public Builder enterFullscreenDrawable(@DrawableRes int enterFullscreenDrawableResId) {
        controller.setEnterFullscreenDrawable(getDrawable(enterFullscreenDrawableResId));
        return this;
    }

    public Builder exitFullscreenDrawable(@NonNull Drawable exitFullscreenDrawable) {
        controller.setExitFullscreenDrawable(exitFullscreenDrawable);
        return this;
    }

    public Builder exitFullscreenDrawable(@DrawableRes int exitFullscreenDrawable) {
        controller.setExitFullscreenDrawable(getDrawable(exitFullscreenDrawable));
        return this;
    }

    public Builder playDrawable(@NonNull Drawable playDrawable) {
        controller.setPlayDrawable(playDrawable);
        return this;
    }

    public Builder playDrawable(@DrawableRes int playDrawable) {
        controller.setPlayDrawable(getDrawable(playDrawable));
        return this;
    }

    public Builder pauseDrawable(@NonNull Drawable pauseDrawable) {
        controller.setPauseDrawable(pauseDrawable);
        return this;
    }

    public Builder pauseDrawable(@DrawableRes int pauseDrawable) {
        controller.setPauseDrawable(getDrawable(pauseDrawable));
        return this;
    }

    public Builder fastForwardDrawable(@NonNull Drawable fastForwardDrawable) {
        controller.setFastForwardDrawable(fastForwardDrawable);
        return this;
    }

    public Builder fastForwardDrawable(@DrawableRes int fastForwardDrawable) {
        controller.setFastForwardDrawable(getDrawable(fastForwardDrawable));
        return this;
    }

    public Builder rewindDrawable(@NonNull Drawable rewindDrawable) {
        controller.setRewindDrawable(rewindDrawable);
        return this;
    }

    public Builder rewindDrawable(@DrawableRes int rewindDrawable) {
        controller.setRewindDrawable(getDrawable(rewindDrawable));
        return this;
    }

    public Builder progressBarColor(int progressBarColor) {
        controller.setProgressBarColor(progressBarColor);
        return this;
    }

    public Builder playIcon(Drawable playDrawable) {
        controller.setPlayDrawable(playDrawable);
        return this;
    }

    public Builder pauseIcon(Drawable pauseDrawable) {
        controller.setPauseDrawable(pauseDrawable);
        return this;
    }

    public Builder fastForwardSeconds(int fastForwardSeconds) {
        controller.setFastForwardDuration(fastForwardSeconds);
        return this;
    }

    public Builder rewindSeconds(int rewindSeconds) {
        controller.setRewindDuration(rewindSeconds);
        return this;
    }

    public Builder landscapeOrientation(LandscapeOrientation landscapeOrientation) {
        orientationDelegate.setLandscapeOrientation(landscapeOrientation);
        return this;
    }

    public Builder portraitOrientation(PortraitOrientation portraitOrientation) {
        orientationDelegate.setPortraitOrientation(portraitOrientation);
        return this;
    }

    public Builder setCanPause(boolean isPauseEnabled) {
        videoMediaPlayer.setCanPause(isPauseEnabled);
        return this;
    }

    public Builder setCanSeekBackward(boolean isSeekBackwardEnabled) {
        videoMediaPlayer.setCanSeekBackward(isSeekBackwardEnabled);
        return this;
    }

    public Builder setCanSeekForward(boolean isSeekForwardEnabled) {
        videoMediaPlayer.setCanSeekForward(isSeekForwardEnabled);
        return this;
    }

    private Drawable getDrawable(int drawableResId) {
        Context context = fullscreenVideoView.getContext();
        return ContextCompat.getDrawable(context, drawableResId);
    }
}
