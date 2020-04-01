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
 * Manages interactions between the VideoControllerView and other classes.
 */
interface MediaController {

    /**
     * Removes the controller from the screen.
     */
    void hide();

    /**
     * Sets progress to the SeekBar.
     *
     * @return progress current position
     */
    int setProgress();

    /**
     * Checks if the SeekBar is currently being dragged.
     *
     * @return true if it's dragging, otherwise false
     */
    boolean isDragging();

    /**
     * Checks if the controller is showing.
     *
     * @return true if it's showing, otherwise false
     */
    boolean isShowing();

    /**
     * Checks if the player is playing.
     *
     * @return true if it's playing, otherwise false
     */
    boolean isPlaying();
}
