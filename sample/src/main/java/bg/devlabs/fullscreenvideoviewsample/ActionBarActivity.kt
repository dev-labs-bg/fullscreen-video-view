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
import android.support.v7.app.AppCompatActivity
import bg.devlabs.fullscreenvideoview.orientation.LandscapeOrientation
import bg.devlabs.fullscreenvideoview.orientation.PortraitOrientation
import bg.devlabs.fullscreenvideoview.playbackspeed.PlaybackSpeedOptions
import kotlinx.android.synthetic.main.activity_action_bar.*


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

        secondFullscreenVideoView.videoUrl("https://storage.googleapis.com/coverr-main/mp4%2FFifa.mp4?X-Goog-Algorithm=GOOG4-RSA-SHA256&X-Goog-Credential=coverr-183014%40appspot.gserviceaccount.com%2F20190618%2Fauto%2Fstorage%2Fgoog4_request&X-Goog-Date=20190618T165621Z&X-Goog-Expires=301&X-Goog-SignedHeaders=host&X-Goog-Signature=79457d1c604d4ad61457e2f453d1dcf477067ab52c1e3f203abe55d48297483deba56a7e3fbe9807fe4dd10be295b938a1fb0a81fe10b6168c341af6eb5cde6249ff34be5928efa046f9117c83ccbf592a2d77ce5734eff128080e3b13531069686bcb865e635ece3e8fae931fd3717a47c36a95f179cfd31eac6c7441ddd8d4ce8e065b6e885d46a51a260714eb47c45328a403c0f8b575a3ba28e7b55193cc0c273c2b4f84704dfc2219135ea5240e2de5814eb919e48ba9a6f1ee486082f8c74633d08a69a8d8a9a611fac464817649642f7ebb555031eadf91179f78be38bd8a950c0f6e23eafed934323d4c6573c4c84fdb1bc206ed6efe97962428d8c7")
                .progressBarColor(R.color.colorAccent)
                .landscapeOrientation(LandscapeOrientation.SENSOR)
                .portraitOrientation(PortraitOrientation.DEFAULT)
                .enableAutoStart()
                .addSeekForwardButton()
                .addSeekBackwardButton()
                .addPlaybackSpeedButton()
                .playbackSpeedOptions(
                        PlaybackSpeedOptions()
                                .addSpeeds(arrayListOf(0.25f, 0.5f, 0.75f, 1f))
                )
    }
}