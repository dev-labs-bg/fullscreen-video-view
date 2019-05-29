package bg.devlabs.fullscreenvideoviewsample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import bg.devlabs.fullscreenvideoview.PlaybackSpeedOptions
import bg.devlabs.fullscreenvideoview.orientation.LandscapeOrientation
import bg.devlabs.fullscreenvideoview.orientation.PortraitOrientation
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
//                .enableAutoStart()
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