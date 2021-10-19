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
package bg.devlabs.fullscreenvideoview.orientation

import android.view.OrientationEventListener
import android.content.ContentResolver
import bg.devlabs.fullscreenvideoview.VisibilityManager
import bg.devlabs.fullscreenvideoview.UIController
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.provider.Settings

/**
 * Handles orientation change.
 */
class OrientationManager(
    private val context: Context,
    private val listener: OrientationListener
) : OrientationEventListener(context) {
    var isLandscape = false
        private set

    private val contentResolver = context.contentResolver
    private val visibilityManager = VisibilityManager()
    private val uiController = UIController()

    private var landscapeOrientation = LandscapeOrientation.SENSOR
    private var portraitOrientation = PortraitOrientation.DEFAULT
    private var shouldEnterPortrait = false
    private var previousOrientation = 0

    private val parentLayout: ViewGroup
        get() {
            val window = (context as Activity).window
            val decorView = window.decorView as ViewGroup
            return decorView.findViewById(android.R.id.content)
        }

    override fun onOrientationChanged(orientation: Int) {
        // If the device's rotation is not enabled do not proceed further with the logic
        if (!isRotationEnabled(contentResolver)) {
            return
        }
        if ((shouldChangeOrientation(orientation, LEFT_LANDSCAPE)
                    || shouldChangeOrientation(orientation, RIGHT_LANDSCAPE))
            && !shouldEnterPortrait
        ) {
            shouldEnterPortrait = true
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
        }
        if (shouldChangeOrientation(orientation, PORTRAIT)
            && shouldEnterPortrait
        ) {
            shouldEnterPortrait = false
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
    }

    fun shouldHandleOnBackPressed(): Boolean {
        if (isLandscape) {
            // Locks the screen orientation to portrait
            setOrientation(portraitOrientation.value)
            return true
        }
        return false
    }

    fun toggleFullscreen() {
        isLandscape = !isLandscape
        var newOrientation = portraitOrientation.value
        if (isLandscape) {
            newOrientation = landscapeOrientation.value
        }
        setOrientation(newOrientation)
    }

    fun setLandscapeOrientation(landscapeOrientation: LandscapeOrientation) {
        this.landscapeOrientation = landscapeOrientation
    }

    fun setPortraitOrientation(portraitOrientation: PortraitOrientation) {
        this.portraitOrientation = portraitOrientation
    }

    fun handleConfigurationChange(newConfig: Configuration) {
        // Avoid calling onConfigurationChanged twice
        if (previousOrientation == newConfig.orientation) {
            return
        }
        previousOrientation = newConfig.orientation
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            activateFullscreen()
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            exitFullscreen()
        }
    }

    private fun activateFullscreen() {
        // Update isLandscape flag
        isLandscape = true

        // Change the screen orientation to SENSOR_LANDSCAPE
        setOrientation(landscapeOrientation.value)
        visibilityManager.hideVisibleViews(parentLayout)

        // Hide the toolbar
        uiController.toggleToolbarVisibility(context, false)

        // Hide the status bar
        uiController.toggleSystemUiVisibility(context)

        // Notify that the fullscreen is activated
        listener.onOrientationChanged(Orientation.LANDSCAPE)
    }

    private fun exitFullscreen() {
        // Update isLandscape flag
        isLandscape = false

        // Change the screen orientation to PORTRAIT
        setOrientation(portraitOrientation.value)
        visibilityManager.showHiddenViews()

        // Show the toolbar
        uiController.toggleToolbarVisibility(context, true)

        // Show the status bar
        uiController.toggleSystemUiVisibility(context)

        // Notify that the fullscreen is deactivated
        listener.onOrientationChanged(Orientation.PORTRAIT)
    }

    private fun setOrientation(orientation: Int) {
        (context as Activity).requestedOrientation = orientation
    }

    private fun shouldChangeOrientation(a: Int, b: Int): Boolean {
        return a > b - ROTATE_THRESHOLD && a < b + ROTATE_THRESHOLD
    }

    /**
     * Check if the device's rotation is enabled
     *
     * @param contentResolver from the app's context
     * @return true or false according to whether the rotation is enabled or disabled
     */
    private fun isRotationEnabled(contentResolver: ContentResolver): Boolean {
        return Settings.System.getInt(
            contentResolver,
            Settings.System.ACCELEROMETER_ROTATION,
            0
        ) == 1
    }

    companion object {
        private const val LEFT_LANDSCAPE = 90
        private const val RIGHT_LANDSCAPE = 270
        private const val PORTRAIT = 0
        private const val ROTATE_THRESHOLD = 10
    }
}