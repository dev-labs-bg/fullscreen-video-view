package bg.devlabs.fullscreenvideoview.orientation;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT;

/**
 * Created by Slavi Petrov on 24.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
@SuppressWarnings("unused")
public enum PortraitOrientation {
    SENSOR(SCREEN_ORIENTATION_SENSOR_PORTRAIT),
    DEFAULT(SCREEN_ORIENTATION_PORTRAIT),
    REVERSE(SCREEN_ORIENTATION_REVERSE_PORTRAIT),
    USER(SCREEN_ORIENTATION_USER_PORTRAIT);

    private final int value;

    PortraitOrientation(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
