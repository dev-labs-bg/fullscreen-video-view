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
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;

import java.io.IOException;

import bg.devlabs.fullscreenvideoview.model.MediaPlayerError;
import bg.devlabs.fullscreenvideoview.model.MediaPlayerErrorType;

class FullscreenVideoMediaPlayer extends MediaPlayer {

    private VideoMediaPlayerListener listener;

    private boolean isAutoStartEnabled;
    private boolean canPause = true;

    FullscreenVideoMediaPlayer(VideoMediaPlayerListener listener) {
        this.listener = listener;
    }

    void init(String videoPath) {
        try {
            setDataSource(videoPath);
            setupAudio();
            setupOnPreparedListener();
            setupOnErrorListener();
            setupOnCompletionListener();
        } catch (IOException exception) {
            listener.onMediaPlayerError(
                    new MediaPlayerError(
                            MediaPlayerErrorType.DATA_SOURCE_READ,
                            exception.getLocalizedMessage()
                    )
            );
        }
    }

    private void setupAudio() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                    .build();
            setAudioAttributes(audioAttributes);
        } else {
            setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }

    private void setupOnPreparedListener() {
        setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                listener.onMediaPlayerPrepared(
                        mediaPlayer,
                        getVideoWidth(),
                        getVideoHeight(),
                        mediaPlayer != null && isAutoStartEnabled
                );
            }
        });
    }

    private void setupOnErrorListener() {
        setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                listener.onMediaPlayerError(
                        new MediaPlayerError(MediaPlayerErrorType.ASYNC_OPERATION, what)
                );
                return false;
            }
        });
    }

    private void setupOnCompletionListener() {
        setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                listener.onMediaPlayerCompletion();
            }
        });
    }

    public int getBufferPercentage() {
        return 0;
    }

    public boolean canPause() {
        return canPause;
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

    public void enableAutoStart() {
        isAutoStartEnabled = true;
    }

    public void disablePause() {
        this.canPause = false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void changePlaybackSpeed(float speed) {
        PlaybackParams playbackParams = new PlaybackParams();
        playbackParams.setSpeed(speed);
        setPlaybackParams(playbackParams);
    }
}
