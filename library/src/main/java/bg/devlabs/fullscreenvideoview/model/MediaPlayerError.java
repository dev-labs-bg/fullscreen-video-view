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
 * Contains information about an error in VideoMediaPlayer.
 */
public class MediaPlayerError {

    /**
     * The type of the error.
     */
    private MediaPlayerErrorType type;

    /**
     * The error code received when an error from an async operation is present.
     *
     * @see MediaPlayerErrorType#ASYNC_OPERATION
     */
    private int code = -1;

    /**
     * The error message received when there is an error when setting the media player data source.
     *
     * @see MediaPlayerErrorType#DATA_SOURCE_READ
     */
    private String message = null;

    /**
     * Creates a new MediaPlayerError with type and code.
     *
     * @param type The error type
     * @param code The error code
     */
    public MediaPlayerError(MediaPlayerErrorType type, int code) {
        this.type = type;
        this.code = code;
    }

    /**
     * Creates a new MediaPlayerError with type and message.
     *
     * @param type The error type
     * @param message The error message
     */
    public MediaPlayerError(MediaPlayerErrorType type, String message) {
        this.type = type;
        this.message = message;
    }

    /**
     * Gets the error type.
     *
     * @return the error type
     */
    public MediaPlayerErrorType getType() {
        return type;
    }

    /**
     * Gets the error code.
     *
     * @return the error code
     */
    public int getCode() {
        return code;
    }

    /**
     * Gets the error message.
     *
     * @return the error message
     */
    public String getMessage() {
        return message;
    }
}
