package bg.devlabs.fullscreenvideoviewexample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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
        val videoPath = "http://clips.vorwaerts-gmbh.de/VfE_html5.mp4"
        fullscreenVideoView.init(videoPath, parentLayout)
                .exitFullscreenDrawable(R.drawable.ic_launcher_background)
                .enterFullscreenDrawable(R.drawable.ic_media_pause)
                .progressBarColor(R.color.colorAccent)
                .pauseIcon(R.drawable.ic_launcher_background)
                .isAutoStartEnabled(true)
    }
}