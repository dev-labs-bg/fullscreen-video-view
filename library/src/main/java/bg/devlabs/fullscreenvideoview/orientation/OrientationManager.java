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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import bg.devlabs.fullscreenvideoview.DeviceDimensionsManager;
import bg.devlabs.fullscreenvideoview.FullscreenVideoView;
import bg.devlabs.fullscreenvideoview.VisibilityManager;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;

/**
 * Created by Slavi Petrov on 24.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 * <p>
 * Handles orientation changes. Updates the VideoView layout params. Hides/shows the toolbar.
 */
public class OrientationManager extends OrientationEventListener {
    private static final int LEFT_LANDSCAPE = 90;
    private static final int RIGHT_LANDSCAPE = 270;
    private static final int PORTRAIT = 0;
    private static final int ROTATE_THRESHOLD = 10;

    private final FullscreenVideoView videoView;
    private int originalWidth;
    private int originalHeight;
    private boolean isLandscape;
    private final ContentResolver contentResolver;
    private final VisibilityManager visibilityManager;
    // Orientation
    private LandscapeOrientation landscapeOrientation = LandscapeOrientation.SENSOR;
    private PortraitOrientation portraitOrientation = PortraitOrientation.DEFAULT;
    private boolean shouldEnterPortrait;

    public OrientationManager(Context context, FullscreenVideoView fullscreenVideoView) {
        super(context);
        videoView = fullscreenVideoView;
        contentResolver = context.getContentResolver();
        visibilityManager = new VisibilityManager();
    }

    public void activateFullscreen() {
        // Update isLandscape flag
        isLandscape = true;

        // Fullscreen active
        videoView.onOrientationChanged();

        // Change the screen orientation to SENSOR_LANDSCAPE
        setOrientation(landscapeOrientation.getValue());

        visibilityManager.hideVisibleViews(getParent());

        // Save the video player original width and height
        originalWidth = videoView.getWidth();
        originalHeight = videoView.getHeight();
        updateLayoutParams();

        // Hide the supportToolbar
        toggleToolbarVisibility(false);

        // Hide status bar
        toggleSystemUiVisibility();
    }

    private void updateLayoutParams() {
        ViewGroup.LayoutParams params = videoView.getLayoutParams();
        Context context = videoView.getContext();
        DeviceDimensionsManager deviceDimensionsManager = DeviceDimensionsManager.getInstance();

        params.width = deviceDimensionsManager.getRealWidth(context);
        params.height = deviceDimensionsManager.getRealHeight(context);

        videoView.setLayoutParams(params);
    }

    public void exitFullscreen() {
        // Update isLandscape flag
        isLandscape = false;

        // Update the fullscreen button drawable
        videoView.onOrientationChanged();

        // Change the screen orientation to PORTRAIT
        setOrientation(portraitOrientation.getValue());

        visibilityManager.showHiddenViews();

        ViewGroup.LayoutParams params = videoView.getLayoutParams();
        params.width = originalWidth;
        params.height = originalHeight;
        videoView.setLayoutParams(params);

        toggleToolbarVisibility(true);
        toggleSystemUiVisibility();
    }

    private ViewGroup getParent() {
        Window window = ((Activity) videoView.getContext()).getWindow();
        ViewGroup decorView = (ViewGroup) window.getDecorView();
        return decorView.findViewById(android.R.id.content);
    }

    private void toggleSystemUiVisibility() {
        Window activityWindow = ((Activity) videoView.getContext()).getWindow();
        View decorView = activityWindow.getDecorView();
        int newUiOptions = decorView.getSystemUiVisibility();
        newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(newUiOptions);
    }

    private void toggleToolbarVisibility(boolean visible) {
        if (videoView.getContext() instanceof AppCompatActivity) {
            toggleSupportActionBarVisibility(visible);
        }
        if (videoView.getContext() instanceof Activity) {
            toggleActionBarVisibility(visible);
        }
    }

    private void toggleActionBarVisibility(boolean visible) {
        // Activity action bar
        android.app.ActionBar actionBar = ((Activity) videoView.getContext()).getActionBar();
        if (actionBar != null) {
            if (visible) {
                actionBar.show();
            } else {
                actionBar.hide();
            }
        }
    }

    private void toggleSupportActionBarVisibility(boolean visible) {
        // AppCompatActivity support action bar
        ActionBar supportActionBar = ((AppCompatActivity) videoView.getContext())
                .getSupportActionBar();
        if (supportActionBar != null) {
            if (visible) {
                supportActionBar.show();
            } else {
                supportActionBar.hide();
            }
        }
    }

    private void setOrientation(int orientation) {
        ((Activity) videoView.getContext()).setRequestedOrientation(orientation);
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
        return Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION,
                0) == 1;
    }

    public boolean isLandscape() {
        return isLandscape;
    }
}
