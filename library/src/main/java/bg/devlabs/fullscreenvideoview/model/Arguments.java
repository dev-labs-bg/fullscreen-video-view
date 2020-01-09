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

import android.graphics.drawable.Drawable;

import bg.devlabs.fullscreenvideoview.orientation.LandscapeOrientation;
import bg.devlabs.fullscreenvideoview.orientation.PortraitOrientation;
import bg.devlabs.fullscreenvideoview.playbackspeed.PlaybackSpeedOptions;

/**
 * Created by Slavi Petrov on 11.12.2019
 * Dev Labs
 * slavi@devlabs.bg
 */
public class Arguments {
    public boolean autoStartEnabled = false;
    public Drawable enterFullscreenDrawable = null;
    public Drawable exitFullscreenDrawable = null;
    public Drawable playDrawable = null;
    public Drawable pauseDrawable = null;
    public Drawable fastForwardDrawable = null;
    public Drawable rewindDrawable = null;
    public int progressBarColor = -1;
    public int fastForwardSeconds = -1;
    public int rewindSeconds = -1;
    public LandscapeOrientation landscapeOrientation = null;
    public PortraitOrientation portraitOrientation = null;
    public boolean disablePause = false;
    public boolean addSeekForwardButton = false;
    public boolean addSeekBackwardButton = false;
    public boolean addPlaybackSpeedButton = false;
    public PlaybackSpeedOptions playbackSpeedOptions = null;
    public int thumbnailResId = -1;
    public boolean hideProgress = false;
    public boolean hideFullscreenButton = false;
    public int seekToTimeMillis = -1;
}
