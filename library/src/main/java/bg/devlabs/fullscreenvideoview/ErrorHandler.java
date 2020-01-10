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

import androidx.annotation.Nullable;

import bg.devlabs.fullscreenvideoview.listener.FullscreenVideoViewException;
import bg.devlabs.fullscreenvideoview.listener.OnErrorListener;

/**
 * Created by Slavi Petrov on 10.01.2020
 * Dev Labs
 * slavi@devlabs.bg
 */
public class ErrorHandler {
    @Nullable
    private OnErrorListener onErrorListener;

    public void setOnErrorListener(@Nullable OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    public void onDestroy() {
        onErrorListener = null;
    }

    public void onError(String message) {
        if (onErrorListener != null) {
            FullscreenVideoViewException exception = new FullscreenVideoViewException(message);
            onErrorListener.onError(exception);
        }
    }

    public void onError(int code, String message) {
        if (onErrorListener != null) {
            FullscreenVideoViewException exception = new FullscreenVideoViewException(code, message);
            onErrorListener.onError(exception);
        }
    }
}
