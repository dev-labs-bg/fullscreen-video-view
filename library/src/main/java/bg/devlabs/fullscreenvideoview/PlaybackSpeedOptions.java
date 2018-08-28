package bg.devlabs.fullscreenvideoview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * Created by Slavi Petrov on 27.08.2018
 * Dev Labs
 * slavi@devlabs.bg
 */
public class PlaybackSpeedOptions {

    private ArrayList<Float> speeds = new ArrayList<>(Collections.singletonList(1f));

    @SuppressWarnings("unused")
    public PlaybackSpeedOptions addSpeeds(ArrayList<Float> speeds) {
        this.speeds.addAll(speeds);
        Collections.sort(this.speeds);
        if (containsIllegalNumbers()) {
            throw new IllegalArgumentException("The speeds array must contain only numbers between 0 and 4!");
        }
        return this;
    }

    private boolean containsIllegalNumbers() {
        int size = speeds.size();
        for (int i = 0; i < size; i++) {
            if (speeds.get(i) < 0 || speeds.get(i) > 4) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Float> getSpeeds() {
        return new ArrayList<>(new HashSet<>(speeds));
    }
}
