package bg.devlabs.fullscreenvideoviewexample

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import bg.devlabs.fullscreenvideoview.FullscreenVideoView
import kotlinx.android.synthetic.main.activity_action_bar.*


/**
 * Created by Slavi Petrov on 13.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
class ActionBarActivity : AppCompatActivity(), FullscreenVideoView.OnVideoSizeResetListener {
    companion object {
        fun start(context: Context) {
            val starter = Intent(context, ActionBarActivity::class.java)
            context.startActivity(starter)
        }
    }

    private lateinit var fullscreenVideoView: FullscreenVideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action_bar)

        fullscreenVideoView = findViewById(R.id.video_player)
//        fullscreenVideoView.init("http://clips.vorwaerts-gmbh.de/VfE_html5.mp4", supportActionBar, this)
        fullscreenVideoView.init("http://clips.vorwaerts-gmbh.de/VfE_html5.mp4", parentLayout, this)
        fullscreenVideoView.setAutoStartEnabled(true)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        fullscreenVideoView.handleConfigurationChange(this, newConfig)
    }

    override fun onVideoSizeReset() {

    }
}