package bg.devlabs.fullscreenvideoview;

import android.content.Context;
import android.support.v4.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Slavi Petrov on 27.08.2018
 * Dev Labs
 * slavi@devlabs.bg
 */
public class PlaybackSpeedPopupMenu extends android.support.v7.widget.PopupMenu {

    private PlaybackSpeedOptions playbackSpeedOptions;

    PlaybackSpeedPopupMenu(Context context, View anchor) {
        super(context, anchor);
    }

    public void setOnSpeedSelectedListener(final OnSpeedSelectedListener listener) {
        setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Pair<Float, Integer> pair = getSpeedDrawablePair(item.getItemId());
                if (pair.first != null && pair.second != null) {
                    float speed = pair.first;
                    int drawable = pair.second;
                    listener.onSpeedSelected(speed, drawable);
                }
                return true;
            }
        });
    }

    private Pair<Float, Integer> getSpeedDrawablePair(int itemId) {
        return new Pair<>(0.25f, R.drawable.ic_playback_speed_0_25);

//        if (itemId == R.id.speed_0_25_button) {
//            return new Pair<>(0.25f, R.drawable.ic_playback_speed_0_25);
//        } else if (itemId == R.id.speed_0_5_button) {
//            return new Pair<>(0.5f, R.drawable.ic_playback_speed_0_5);
//        } else if (itemId == R.id.speed_0_75_button) {
//            return new Pair<>(0.75f, R.drawable.ic_playback_speed_0_75);
//        } else if (itemId == R.id.speed_1_button) {
//            return new Pair<>(1f, R.drawable.ic_playback_speed_1);
//        } else if (itemId == R.id.speed_1_25_button) {
//            return new Pair<>(1.25f, R.drawable.ic_playback_speed_1_25);
//        } else if (itemId == R.id.speed_1_5_button) {
//            return new Pair<>(1.5f, R.drawable.ic_playback_speed_1_5);
//        } else { // R.id.speed_2_button
//            return new Pair<>(2f, R.drawable.ic_playback_speed_2);
//        }
    }

    public void setPlaybackSpeedOptions(PlaybackSpeedOptions playbackSpeedOptions) {
//        this.playbackSpeedOptions = playbackSpeedOptions;

        ArrayList<Pair<PlaybackSpeed, Integer>> values = playbackSpeedOptions.getValues();
        int size = values.size();
        int id = -1;
        for (int i = 0; i < size; i++) {
            id++;

            String title = String.valueOf(values.get(i).first.getValue()) + "x";
            getMenu().add(0, id, Menu.NONE, title);
        }

//        getMenu().add(0, 0, Menu.NONE, "0.25x");
//        getMenu().add(0, 1, Menu.NONE, "0.5x");
//        getMenu().add(0, 2, Menu.NONE, "0.75x");
//        getMenu().add(0, 3, Menu.NONE, "1x");
//        getMenu().add(0, 4, Menu.NONE, "1.25x");
//        getMenu().add(0, 5, Menu.NONE, "1.5x");
//        getMenu().add(0, 6, Menu.NONE, "2x");
    }

    interface OnSpeedSelectedListener {
        void onSpeedSelected(float speed, int drawableResId);
    }
}
