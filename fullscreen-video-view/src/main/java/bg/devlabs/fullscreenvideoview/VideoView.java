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

/**
 * Manages the communication between the FullscreenVideoView and its related classes.
 */
public interface VideoView extends VideoMediaPlayer, OrientationController {

    /**
     * Gets the parent ViewGroup of the view.
     *
     * @return the parent ViewGroup
     */
//    ViewGroup getParentLayout();

    /**
     * Hides the thumbnail of the video.
     */
//    void hideThumbnail();

    /**
     * Called when the orientation is changed.
     */
//    void onOrientationChanged();

    /**
     * Called when the video view is in fullscreen mode.
     */
//    void onFullscreenActivated();

    /**
     * Called when the video view is not in fullscreen mode anymore.
     */
//    void onFullscreenDeactivated();

    /**
     * Toggles the visibility of system navigation bar and status bar.
     */
//    void toggleSystemUiVisibility();

    /**
     * Toggles the visibility of the toolbar.
     *
     * @param isVisible Indicates whether the toolbar should become visible or not.
     */
//    void toggleToolbarVisibility(boolean isVisible);

    /**
     * Changes the orientation of the device.
     *
     * @param orientation The orientation to use for the change
     *
     * @see bg.devlabs.fullscreenvideoview.orientation.PortraitOrientation
     * @see bg.devlabs.fullscreenvideoview.orientation.LandscapeOrientation
     */
//    void changeOrientation(int orientation);

    /**
     * Focuses the video view.
     */
//    void focus();

    /**
     * Clears the tag of the fullscreen button.
     */
//    void clearFullscreenButtonTag();
}
