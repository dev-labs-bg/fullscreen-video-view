package bg.devlabs.fullscreenvideoview;

/**
 * Created by Slavi Petrov on 27.08.2018
 * Dev Labs
 * slavi@devlabs.bg
 */
public enum PlaybackSpeed {
    SPEED_0_25(0.25f),
    SPEED_0_50(0.50f),
    SPEED_0_75(0.75f),
    SPEED_1_00(1.00f),
    SPEED_1_25(1.25f),
    SPEED_1_50(1.50f),
    SPEED_2_00(2.00f);

    private final float value;

    PlaybackSpeed(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }
}
