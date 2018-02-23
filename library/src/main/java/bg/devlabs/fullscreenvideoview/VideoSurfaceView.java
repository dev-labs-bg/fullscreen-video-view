package bg.devlabs.fullscreenvideoview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.SurfaceView;
import android.widget.FrameLayout;


/**
 * Created by Slavi Petrov on 23.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
class VideoSurfaceView extends SurfaceView {
    private int previousHeight;
    private int previousWidth;

    public VideoSurfaceView(Context context) {
        super(context);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void updateLayoutParams(int videoWidth, int videoHeight) {
        resetLayoutParams();
        previousHeight = getLayoutParams().height;
        previousWidth = getLayoutParams().width;
        // Get the Display Metrics
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        // Get the width of the screen
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        // Get the SurfaceView layout parameters
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) getLayoutParams();
        if ((float) videoHeight / screenHeight > (float) videoWidth / screenWidth) {
            lp.height = screenHeight;
            // Set the width of the SurfaceView to match the aspect ratio of the video
            // be sure to cast these as floats otherwise the calculation will likely be 0
            lp.width = (int) (((float) videoWidth / videoHeight) * screenHeight);
        } else {
            // Set the width of the SurfaceView to the width of the screen
            lp.width = screenWidth;
            // Set the height of the SurfaceView to match the aspect ratio of the video
            // be sure to cast these as floats otherwise the calculation will likely be 0
            lp.height = (int) (((float) videoHeight / videoWidth) * screenWidth);
        }
        // Change the gravity to center
        lp.gravity = Gravity.CENTER;
        // Commit the layout parameters
        setLayoutParams(lp);
    }

    public void resetLayoutParams() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        layoutParams.height = previousHeight;
        layoutParams.width = previousWidth;
        setLayoutParams(layoutParams);
    }
}
