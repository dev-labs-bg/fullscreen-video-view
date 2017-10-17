package bg.devlabs.fullscreenvideoviewexample

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_toolbar.*

class ToolbarActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context) {
            val starter = Intent(context, ToolbarActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toolbar)
        fullscreenVideoView.init("http://clips.vorwaerts-gmbh.de/VfE_html5.mp4", parentLayout)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        fullscreenVideoView.handleConfigurationChange(this, newConfig)
    }

    override fun onDestroy() {
        super.onDestroy()
        fullscreenVideoView.handleOnDestroy()
    }
}
