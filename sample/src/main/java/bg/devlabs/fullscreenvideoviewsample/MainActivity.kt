package bg.devlabs.fullscreenvideoviewsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        actionBarButton.setOnClickListener {
            ActionBarActivity.start(this)
        }

        toolbarButton.setOnClickListener {
            ToolbarActivity.start(this)
        }

        noActionBarButton.setOnClickListener {
            NoActionBarActivity.start(this)
        }

        regularActivityButton.setOnClickListener {
            RegularActivity.start(this)
        }
    }
}
