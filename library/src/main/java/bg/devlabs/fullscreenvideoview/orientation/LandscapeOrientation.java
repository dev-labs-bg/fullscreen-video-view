package bg.devlabs.fullscreenvideoview.orientation;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE;

/**
 * Created by Slavi Petrov on 24.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
@SuppressWarnings("unused")
public enum LandscapeOrientation {
    SENSOR(SCREEN_ORIENTATION_SENSOR_LANDSCAPE),
    DEFAULT(SCREEN_ORIENTATION_LANDSCAPE),
    REVERSE(SCREEN_ORIENTATION_LANDSCAPE),
    USER(SCREEN_ORIENTATION_USER_LANDSCAPE);

    private final int value;

    LandscapeOrientation(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
