package bg.devlabs.fullscreenvideoviewexample

import android.content.Context
import android.content.Intent
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
        val videoPath = "http://clips.vorwaerts-gmbh.de/VfE_html5.mp4"
        fullscreenVideoView.build(videoPath)
    }
}
