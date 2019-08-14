/*
 * Copyright 2017 Dev Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bg.devlabs.fullscreenvideoview.playbackspeed;

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
public class PlaybackSpeedPopupMenu extends android.support.v7.widget.PopupMenu {

    private ArrayList<Float> values = new ArrayList<>(
            Arrays.asList(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)
    );

    public PlaybackSpeedPopupMenu(Context context, View anchor) {
        super(context, anchor);
        addMenuButtons();
    }

    public void setOnSpeedSelectedListener(final OnPlaybackSpeedSelectedListener listener) {
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

    public void setPlaybackSpeedOptions(PlaybackSpeedOptions playbackSpeedOptions) {
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
}
