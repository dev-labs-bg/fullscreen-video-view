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

package bg.devlabs.fullscreenvideoviewsample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import bg.devlabs.fullscreenvideoview.orientation.LandscapeOrientation
import bg.devlabs.fullscreenvideoview.orientation.PortraitOrientation
import bg.devlabs.fullscreenvideoview.playbackspeed.PlaybackSpeedOptions
import kotlinx.android.synthetic.main.activity_action_bar.*
import java.io.File


/**
 * Created by Slavi Petrov on 13.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
class ActionBarActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context) {
            val starter = Intent(context, ActionBarActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action_bar)

        // Change the ActionBar title
        supportActionBar?.title = getString(R.string.action_bar_activity)
        val videoPath = "https://clips.vorwaerts-gmbh.de/VfE_html5.mp4"

        fullscreenVideoView.videoUrl(videoPath)
                .progressBarColor(R.color.colorAccent)
                .landscapeOrientation(LandscapeOrientation.SENSOR)
                .portraitOrientation(PortraitOrientation.DEFAULT)
                .thumbnail(R.drawable.video_thumbnail)
                .addSeekForwardButton()
                .addSeekBackwardButton()
                .addPlaybackSpeedButton()
                .playbackSpeedOptions(
                        PlaybackSpeedOptions()
                                .addSpeeds(arrayListOf(0.25f, 0.5f, 0.75f, 1f))
                )
    }
}