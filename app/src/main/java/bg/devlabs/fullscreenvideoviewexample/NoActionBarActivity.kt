package bg.devlabs.fullscreenvideoviewexample

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_no_action_bar.*

class NoActionBarActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context) {
            val starter = Intent(context, NoActionBarActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_action_bar)
        fullscreenVideoView.init("http://clips.vorwaerts-gmbh.de/VfE_html5.mp4",
                parentLayout, lifecycle)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        fullscreenVideoView.handleConfigurationChange(newConfig)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        fullscreenVideoView.handleOnBackPressed()
    }
}
