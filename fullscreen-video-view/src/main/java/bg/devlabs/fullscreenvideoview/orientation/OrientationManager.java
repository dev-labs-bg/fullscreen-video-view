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

package bg.devlabs.fullscreenvideoview.orientation;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.provider.Settings;
import android.view.OrientationEventListener;

import bg.devlabs.fullscreenvideoview.VideoView;
import bg.devlabs.fullscreenvideoview.VisibilityManager;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;

/**
 * Handles orientation change.
 */
public class OrientationManager extends OrientationEventListener {
    private static final int LEFT_LANDSCAPE = 90;
    private static final int RIGHT_LANDSCAPE = 270;
    private static final int PORTRAIT = 0;
    private static final int ROTATE_THRESHOLD = 10;

    private VideoView videoView;
    private boolean isLandscape;
    private final ContentResolver contentResolver;
    private final VisibilityManager visibilityManager;
    // Orientation
    private LandscapeOrientation landscapeOrientation = LandscapeOrientation.SENSOR;
    private PortraitOrientation portraitOrientation = PortraitOrientation.DEFAULT;
    private boolean shouldEnterPortrait;
    private int previousOrientation;

    public OrientationManager(Context context, VideoView interactor) {
        super(context);
        this.videoView = interactor;
        contentResolver = context.getContentResolver();
        visibilityManager = new VisibilityManager();
    }

    private void activateFullscreen() {
        // Update isLandscape flag
        isLandscape = true;

        // Fullscreen active
        videoView.onOrientationChanged();

        // Change the screen orientation to SENSOR_LANDSCAPE
        setOrientation(landscapeOrientation.getValue());

        visibilityManager.hideVisibleViews(videoView.getParentLayout());

        videoView.onFullscreenActivated();

        // Hide the toolbar
        videoView.toggleToolbarVisibility(false);

        // Hide the status bar
        videoView.toggleSystemUiVisibility();
    }

    private void exitFullscreen() {
        // Update isLandscape flag
        isLandscape = false;

        // Update the fullscreen button drawable
        videoView.onOrientationChanged();

        // Change the screen orientation to PORTRAIT
        setOrientation(portraitOrientation.getValue());

        visibilityManager.showHiddenViews();

        videoView.onFullscreenDeactivated();

        // Show the toolbar
        videoView.toggleToolbarVisibility(true);

        // Show the status bar
        videoView.toggleSystemUiVisibility();
    }

    private void setOrientation(int orientation) {
        videoView.changeOrientation(orientation);
    }

    public boolean shouldHandleOnBackPressed() {
        if (isLandscape) {
            // Locks the screen orientation to portrait
            setOrientation(portraitOrientation.getValue());
            videoView.onOrientationChanged();
            return true;
        }

        return false;
    }

    public void toggleFullscreen() {
        isLandscape = !isLandscape;
        int newOrientation = portraitOrientation.getValue();
        if (isLandscape) {
            newOrientation = landscapeOrientation.getValue();
        }
        setOrientation(newOrientation);
    }

    public void setLandscapeOrientation(LandscapeOrientation landscapeOrientation) {
        this.landscapeOrientation = landscapeOrientation;
    }

    public void setPortraitOrientation(PortraitOrientation portraitOrientation) {
        this.portraitOrientation = portraitOrientation;
    }

    private boolean shouldChangeOrientation(int a, int b) {
        return a > b - ROTATE_THRESHOLD && a < b + ROTATE_THRESHOLD;
    }

    @Override
    public void onOrientationChanged(int orientation) {
        // If the device's rotation is not enabled do not proceed further with the logic
        if (!isRotationEnabled(contentResolver)) {
            return;
        }

        if ((shouldChangeOrientation(orientation, LEFT_LANDSCAPE)
                || shouldChangeOrientation(orientation, RIGHT_LANDSCAPE))
                && !shouldEnterPortrait) {
            shouldEnterPortrait = true;
            setOrientation(SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }

        if (shouldChangeOrientation(orientation, PORTRAIT)
                && shouldEnterPortrait) {
            shouldEnterPortrait = false;
            setOrientation(SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    /**
     * Check if the device's rotation is enabled
     *
     * @param contentResolver from the app's context
     * @return true or false according to whether the rotation is enabled or disabled
     */
    private boolean isRotationEnabled(ContentResolver contentResolver) {
        return Settings.System.getInt(
                contentResolver,
                Settings.System.ACCELEROMETER_ROTATION,
                0
        ) == 1;
    }

    public boolean isLandscape() {
        return isLandscape;
    }

    public void handleConfigurationChange(Configuration newConfig) {
        // Avoid calling onConfigurationChanged twice
        if (previousOrientation == newConfig.orientation) {
            return;
        }
        previousOrientation = newConfig.orientation;

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            activateFullscreen();

            // Focus the view which is in fullscreen mode, because otherwise the Activity will
            // handle the back button
            videoView.focus();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            exitFullscreen();
            videoView.clearFullscreenButtonTag();
        }
    }
}
