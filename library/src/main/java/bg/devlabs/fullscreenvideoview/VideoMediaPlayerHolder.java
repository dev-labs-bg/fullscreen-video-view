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

import android.media.MediaPlayer;

import bg.devlabs.fullscreenvideoview.model.MediaPlayerError;

/**
 * Created by Slavi Petrov on 09.01.2020
 * Dev Labs
 * slavi@devlabs.bg
 */
public interface VideoMediaPlayerHolder {

    boolean isPlaying();

    void seekBy(int duration);

    void changePlaybackSpeed(float speed);

    boolean canPause();

    boolean shouldShowSeekBackwardButton();

    boolean shouldShowSeekForwardButton();

    boolean shouldShowPlaybackSpeedButton();

    int getCurrentPosition();

    int getDuration();

    int getBufferPercentage();

    void onPauseResume();

    void seekTo(int position);

    void onMediaPlayerPrepared(
            MediaPlayer mediaPlayer,
            int videoWidth,
            int videoHeight,
            boolean isAutoStartEnabled
    );

    void onMediaPlayerError(MediaPlayerError error);

    void onMediaPlayerCompletion();
}
