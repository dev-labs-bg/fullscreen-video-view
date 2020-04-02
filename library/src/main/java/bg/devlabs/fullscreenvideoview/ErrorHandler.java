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

import androidx.annotation.Nullable;

import bg.devlabs.fullscreenvideoview.listener.FullscreenVideoViewException;
import bg.devlabs.fullscreenvideoview.listener.OnErrorListener;
import bg.devlabs.fullscreenvideoview.model.MediaPlayerError;

import static android.media.MediaPlayer.MEDIA_ERROR_IO;
import static android.media.MediaPlayer.MEDIA_ERROR_MALFORMED;
import static android.media.MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK;
import static android.media.MediaPlayer.MEDIA_ERROR_SERVER_DIED;
import static android.media.MediaPlayer.MEDIA_ERROR_TIMED_OUT;
import static android.media.MediaPlayer.MEDIA_ERROR_UNKNOWN;
import static android.media.MediaPlayer.MEDIA_ERROR_UNSUPPORTED;
import static bg.devlabs.fullscreenvideoview.Constants.MEDIA_ERROR_GENERAL;

/**
 * Handles error events which occur in FullscreenVideoView. Uses OnErrorListener to return errors
 * to the caller.
 *
 * @see OnErrorListener
 */
class ErrorHandler {
    @Nullable
    private OnErrorListener onErrorListener;

    void setOnErrorListener(@Nullable OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    void onDestroy() {
        onErrorListener = null;
    }

    void handle(Context context, MediaPlayerError error) {
        switch(error.getType()) {
            case DATA_SOURCE_READ: {
                onError(error.getMessage());
            }

            case ASYNC_OPERATION: {
                handleAsyncOperationError(context, error.getCode());
            }
        }
    }

    private void onError(String message) {
        if (onErrorListener != null) {
            FullscreenVideoViewException exception = new FullscreenVideoViewException(message);
            onErrorListener.onError(exception);
        }
    }

    private void handleAsyncOperationError(Context context, int errorCode) {
        switch (errorCode) {
            case MEDIA_ERROR_IO: {
                onError(
                        MEDIA_ERROR_IO,
                        context.getString(R.string.media_error_io)
                );

                break;
            }

            case MEDIA_ERROR_MALFORMED: {
                onError(
                        MEDIA_ERROR_MALFORMED,
                        context.getString(R.string.media_error_malformed)
                );

                break;
            }

            case MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK: {
                onError(
                        MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK,
                        context.getString(R.string.media_error_not_valid_for_progressive_playback)
                );

                break;
            }

            case MEDIA_ERROR_SERVER_DIED: {
                onError(
                        MEDIA_ERROR_SERVER_DIED,
                        context.getString(R.string.media_error_server_died)
                );

                break;
            }

            case MEDIA_ERROR_TIMED_OUT: {
                onError(
                        MEDIA_ERROR_TIMED_OUT,
                        context.getString(R.string.media_error_timed_out)
                );

                break;
            }

            case MEDIA_ERROR_UNKNOWN: {
                onError(
                        MEDIA_ERROR_UNKNOWN,
                        context.getString(R.string.media_error_unknown)
                );

                break;
            }

            case MEDIA_ERROR_UNSUPPORTED: {
                onError(
                        MEDIA_ERROR_UNSUPPORTED,
                        context.getString(R.string.media_error_unsupported)
                );

                break;
            }

            default: {
                onError(
                        MEDIA_ERROR_GENERAL,
                        context.getString(R.string.media_error_general)
                );
            }
        }
    }

    private void onError(int code, String message) {
        if (onErrorListener != null) {
            FullscreenVideoViewException exception = new FullscreenVideoViewException(code, message);
            onErrorListener.onError(exception);
        }
    }
}
