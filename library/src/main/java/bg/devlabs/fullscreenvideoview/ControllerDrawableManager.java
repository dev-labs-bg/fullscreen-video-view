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
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

/**
 * Contains the drawables for the video controller and operates with them.
 */
public class ControllerDrawableManager {
    // Drawables for the buttons of the controller
    private Drawable exitFullscreenDrawable;
    private Drawable enterFullscreenDrawable;
    private Drawable playDrawable;
    private Drawable pauseDrawable;
    private Drawable fastForwardDrawable;
    private Drawable rewindDrawable;

    ControllerDrawableManager(Context context, TypedArray styledAttrs) {
        Drawable exitFullscreenDrawableXml = styledAttrs.getDrawable(
                R.styleable.VideoControllerView_exit_fullscreen_drawable
        );
        if (exitFullscreenDrawableXml != null) {
            exitFullscreenDrawable = exitFullscreenDrawableXml;
        } else {
            exitFullscreenDrawable = getDrawable(context, R.drawable.ic_fullscreen_exit_white_48dp);
        }

        Drawable enterFullscreenDrawableXml = styledAttrs.getDrawable(
                R.styleable.VideoControllerView_enter_fullscreen_drawable
        );
        if (enterFullscreenDrawableXml != null) {
            enterFullscreenDrawable = enterFullscreenDrawableXml;
        } else {
            enterFullscreenDrawable = getDrawable(context, R.drawable.ic_fullscreen_white_48dp);
        }

        Drawable playDrawableXml = styledAttrs.getDrawable(
                R.styleable.VideoControllerView_play_drawable
        );
        if (playDrawableXml != null) {
            playDrawable = playDrawableXml;
        } else {
            playDrawable = getDrawable(context, R.drawable.ic_play_arrow_white_48dp);
        }

        Drawable pauseDrawableXml = styledAttrs.getDrawable(
                R.styleable.VideoControllerView_pause_drawable
        );
        if (pauseDrawableXml != null) {
            pauseDrawable = pauseDrawableXml;
        } else {
            pauseDrawable = getDrawable(context, R.drawable.ic_pause_white_48dp);
        }

        Drawable fastForwardDrawableXml = styledAttrs.getDrawable(
                R.styleable.VideoControllerView_ffwd_drawable
        );
        if (fastForwardDrawableXml != null) {
            fastForwardDrawable = fastForwardDrawableXml;
        } else {
            fastForwardDrawable = getDrawable(context, R.drawable.ic_fast_forward_white_48dp);
        }

        Drawable rewindDrawableXml = styledAttrs.getDrawable(
                R.styleable.VideoControllerView_rew_drawable
        );
        if (rewindDrawableXml != null) {
            rewindDrawable = rewindDrawableXml;
        } else {
            rewindDrawable = getDrawable(context, R.drawable.ic_fast_rewind_white_48dp);
        }
    }

    Drawable getPlayDrawable() {
        return playDrawable;
    }

    void setPlayDrawable(Drawable playDrawable) {
        if (playDrawable != null) {
            this.playDrawable = playDrawable;
        }
    }

    void setPauseDrawable(Drawable pauseDrawable) {
        if (pauseDrawable != null) {
            this.pauseDrawable = pauseDrawable;
        }
    }

    Drawable getEnterFullscreenDrawable() {
        return enterFullscreenDrawable;
    }

    void setEnterFullscreenDrawable(Drawable enterFullscreenDrawable) {
        if (enterFullscreenDrawable != null) {
            this.enterFullscreenDrawable = enterFullscreenDrawable;
        }
    }

    void setExitFullscreenDrawable(Drawable exitFullscreenDrawable) {
        if (exitFullscreenDrawable != null) {
            this.exitFullscreenDrawable = exitFullscreenDrawable;
        }
    }

    Drawable getRewindDrawable() {
        return rewindDrawable;
    }

    void setRewindDrawable(Drawable rewindDrawable) {
        if (rewindDrawable != null) {
            this.rewindDrawable = rewindDrawable;
        }
    }

    Drawable getFastForwardDrawable() {
        return fastForwardDrawable;
    }

    void setFastForwardDrawable(Drawable fastForwardDrawable) {
        if (fastForwardDrawable != null) {
            this.fastForwardDrawable = fastForwardDrawable;
        }
    }

    Drawable getPlayPauseDrawable(boolean isPlaying) {
        return isPlaying ? pauseDrawable : playDrawable;
    }

    Drawable getFullscreenDrawable(boolean isLandscape) {
        return isLandscape ? exitFullscreenDrawable : enterFullscreenDrawable;
    }

    private Drawable getDrawable(Context context, int drawableResId) {
        return ContextCompat.getDrawable(context, drawableResId);
    }
}
