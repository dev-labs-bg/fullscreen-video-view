package bg.devlabs.fullscreenvideoview;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by Slavi Petrov on 27.08.2018
 * Dev Labs
 * slavi@devlabs.bg
 */
class PlaybackSpeedPopupMenu extends android.support.v7.widget.PopupMenu {

    private ArrayList<Float> values = new ArrayList<>(Arrays.asList(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f));

    PlaybackSpeedPopupMenu(Context context, View anchor) {
        super(context, anchor);
        addMenuButtons();
    }

    public void setOnSpeedSelectedListener(final OnSpeedSelectedListener listener) {
        setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                float speed = values.get(item.getItemId());
                String text = String.format(Locale.getDefault(), "%.2f", speed) + "x";
                listener.onSpeedSelected(speed, text);
                return true;
            }
        });
    }

    void setPlaybackSpeedOptions(PlaybackSpeedOptions playbackSpeedOptions) {
        values.clear();
        values = playbackSpeedOptions.getSpeeds();
        removeMenuButtons();
        addMenuButtons();
    }

    private void removeMenuButtons() {
        getMenu().removeGroup(0);
    }

    private void addMenuButtons() {
        int size = values.size();
        int id = -1;
        for (int i = 0; i < size; i++) {
            id++;
            String title = String.format(Locale.getDefault(), "%.2f", values.get(i)) + "x";
            getMenu().add(0, id, Menu.NONE, title);
        }
    }

    interface OnSpeedSelectedListener {
        void onSpeedSelected(float speed, String text);
    }
}
