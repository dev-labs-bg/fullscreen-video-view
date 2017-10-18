package bg.devlabs.fullscreenvideoviewexample

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
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
        // Initialize the FullscreenVideoView
        fullscreenVideoView.Builder(
                "http://clips.vorwaerts-gmbh.de/VfE_html5.mp4", parentLayout, lifecycle)
                .exitFullscreenDrawable(R.drawable.ic_launcher_background)
                .enterFullscreenDrawable(R.drawable.ic_media_pause)
                .autoStartEnabled(true)
                .build()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        fullscreenVideoView.handleConfigurationChange(newConfig)
    }

    override fun onBackPressed() {
        // Must be without super.onBackPressed(), because it is called inside the library
        if (!fullscreenVideoView.shouldHandleOnBackPressed()) {
            super.onBackPressed()
        }
    }
}