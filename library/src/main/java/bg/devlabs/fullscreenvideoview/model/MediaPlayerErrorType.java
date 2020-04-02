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

package bg.devlabs.fullscreenvideoview.model;

/**
 * Error type of media player.
 */
public enum MediaPlayerErrorType {

    /**
     * Occurs when reading the media player data source.
     */
    DATA_SOURCE_READ,

    /**
     * Occurs when there is an error while playing the video.
     */
    ASYNC_OPERATION
}
