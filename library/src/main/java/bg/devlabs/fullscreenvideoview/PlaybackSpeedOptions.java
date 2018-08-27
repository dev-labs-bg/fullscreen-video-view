package bg.devlabs.fullscreenvideoview;

import android.support.v4.util.Pair;

import java.util.ArrayList;

/**
 * Created by Slavi Petrov on 27.08.2018
 * Dev Labs
 * slavi@devlabs.bg
 */
public class PlaybackSpeedOptions {

    private ArrayList<Pair<PlaybackSpeed, Integer>> values = new ArrayList<>();

    public PlaybackSpeedOptions addPlaybackSpeed(PlaybackSpeed playbackSpeed, int drawableResId) {
        values.add(new Pair<>(playbackSpeed, drawableResId));
        return this;
    }

    public ArrayList<Pair<PlaybackSpeed, Integer>> getValues() {
        return values;
    }
}
