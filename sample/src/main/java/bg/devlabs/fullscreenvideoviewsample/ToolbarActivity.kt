package bg.devlabs.fullscreenvideoviewsample

import android.content.Context
import android.content.Intent
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
        val videoPath = "https://clips.vorwaerts-gmbh.de/VfE_html5.mp4"
        fullscreenVideoView.videoUrl(videoPath)
                .thumbnail(R.drawable.video_thumbnail)
    }
}
