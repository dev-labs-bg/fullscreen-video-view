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

package bg.devlabs.fullscreenvideoview.listener.mediacontroller;

/**
 * Created by Slavi Petrov on 14.08.2019
 * Dev Labs
 * slavi@devlabs.bg
 *
 * Listener for media controller events.
 */
public interface MediaControllerListener {

    /**
     * The play button has been clicked.
     */
    void onPlayClicked();

    /**
     * The pause button has been clicked.
     */
    void onPauseClicked();

    /**
     * The rewind button has been clicked.
     */
    void onRewindClicked();

    /**
     * The fast forward button has been clicked.
     */
    void onFastForwardClicked();

    /**
     * The fullscreen button is clicked.
     */
    void onFullscreenClicked();

    /**
     * The SeekBar progress is changed from user interaction.
     *
     * @param progressMs the progress in milliseconds
     */
    void onSeekBarProgressChanged(long progressMs);
}
