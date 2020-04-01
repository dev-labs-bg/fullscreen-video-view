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
 * This adapter class provides empty implementations of the methods
 * from [{@link MediaControllerListener}]. Any custom listener that cares only about a subset of
 * the methods of this listener can simply subclass this adapter class instead of implementing
 * the interface directly.
 */
@SuppressWarnings("unused")
public class MediaControllerListenerAdapter implements MediaControllerListener {
    @Override
    public void onPlayClicked() {
    }

    @Override
    public void onPauseClicked() {
    }

    @Override
    public void onRewindClicked() {
    }

    @Override
    public void onFastForwardClicked() {
    }

    @Override
    public void onFullscreenClicked() {
    }

    @Override
    public void onSeekBarProgressChanged(long progressMs) {
    }
}
