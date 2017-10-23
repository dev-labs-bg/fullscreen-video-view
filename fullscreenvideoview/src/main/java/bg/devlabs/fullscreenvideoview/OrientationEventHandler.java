package bg.devlabs.fullscreenvideoview;

import android.content.ContentResolver;
import android.content.Context;
import android.view.OrientationEventListener;

import bg.devlabs.fullscreenvideoview.util.DeviceUtils;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;


/**
 * Created by Slavi Petrov on 23.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
public class OrientationEventHandler extends OrientationEventListener {
    private ContentResolver contentResolver;//todo this is not needed in my opinion
    private boolean isLandscape;
    private IFullscreenVideoView videoView;
    public OrientationEventHandler(Context context, FullscreenVideoView videoView) {
        super(context);
        this.contentResolver = context.getContentResolver();//todo this is not needed in my opinion
        this.videoView = videoView;
    }
    private boolean epsilonCheck(int a, int b, int epsilon) {
        return a > b - epsilon && a < b + epsilon;
    }
    //if not remove context
    @Override
    public void onOrientationChanged(int orientation) {
        // If the device's rotation is not enabled do not proceed further with the logic
        if (!DeviceUtils.isRotationEnabled(contentResolver)) {//todo this is not needed in my opinion
            return;
        }
        int epsilon = 10;
        int leftLandscape = 90;
        int rightLandscape = 270;
        int portrait = 0;
        if ((epsilonCheck(orientation, leftLandscape, epsilon) ||
                epsilonCheck(orientation, rightLandscape, epsilon)) && !isLandscape) {
            isLandscape = true;
            videoView.setOrientation(SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
        if (epsilonCheck(orientation, portrait, epsilon) && isLandscape) {
            isLandscape = false;
            videoView.setOrientation(SCREEN_ORIENTATION_PORTRAIT);
        }
    }
}
