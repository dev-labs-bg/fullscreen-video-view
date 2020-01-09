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

import android.annotation.TargetApi;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;

/**
 * Created by Slavi Petrov on 20.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
class VideoMediaPlayer extends MediaPlayer {
    private boolean isAutoStartEnabled;
    private boolean canPause = true;
    private boolean showSeekBackwardButton = false;
    private boolean showSeekForwardButton = false;
    private boolean showPlaybackSpeedButton = false;

    public int getBufferPercentage() {
        return 0;
    }

    public boolean canPause() {
        return canPause;
    }

    public boolean showSeekForwardButton() {
        return showSeekForwardButton;
    }

    public boolean showSeekBackwardButton() {
        return showSeekBackwardButton;
    }

    public boolean showPlaybackSpeedButton() {
        return showPlaybackSpeedButton;
    }

    public void onPauseResume() {
        if (isPlaying()) {
            pause();
        } else {
            start();
        }
    }

    public void onDetach() {
        setOnPreparedListener(null);
        if (isPlaying()) {
            stop();
        }
        release();
    }

    public boolean isAutoStartEnabled() {
        return isAutoStartEnabled;
    }

    public void enableAutoStart() {
        isAutoStartEnabled = true;
    }

    public void disablePause() {
        this.canPause = false;
    }

    public void addSeekForwardButton() {
        this.showSeekForwardButton = true;
    }

    public void addSeekBackwardButton() {
        this.showSeekBackwardButton = true;
    }

    public void addPlaybackSpeedButton() {
        this.showPlaybackSpeedButton = true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void changePlaybackSpeed(float speed) {
        PlaybackParams playbackParams = new PlaybackParams();
        playbackParams.setSpeed(speed);
        setPlaybackParams(playbackParams);
    }
}
