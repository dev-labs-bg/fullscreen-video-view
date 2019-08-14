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

package bg.devlabs.fullscreenvideoview;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.view.View;
import android.widget.TextView;

import bg.devlabs.fullscreenvideoview.playbackspeed.PlaybackSpeedPopupMenuListener;
import bg.devlabs.fullscreenvideoview.playbackspeed.OnPlaybackSpeedSelectedListener;
import bg.devlabs.fullscreenvideoview.playbackspeed.PlaybackSpeedOptions;
import bg.devlabs.fullscreenvideoview.playbackspeed.PlaybackSpeedPopupMenu;

import static android.view.View.INVISIBLE;

/**
 * Created by Slavi Petrov on 14.08.2019
 * Dev Labs
 * slavi@devlabs.bg
 */
public class PlaybackSpeedManager {

    private TextView playbackSpeedButton;
    private PlaybackSpeedPopupMenu popupMenu;

    PlaybackSpeedManager(Context context, TextView playbackSpeedButton) {
        this.playbackSpeedButton = playbackSpeedButton;
        // Initialize the PopupMenu
        popupMenu = new PlaybackSpeedPopupMenu(context, playbackSpeedButton);
    }

    void setPlaybackSpeedButtonOnClickListener(
            final PlaybackSpeedPopupMenuListener playbackSpeedPopupMenuListener
    ) {
        playbackSpeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.setOnSpeedSelectedListener(new OnPlaybackSpeedSelectedListener() {
                    @Override
                    public void onSpeedSelected(float speed, String text) {
                        playbackSpeedPopupMenuListener.onSpeedSelected(speed, text);
                    }
                });

                popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu menu) {
                        playbackSpeedPopupMenuListener.onPopupMenuDismissed();
                    }
                });

                // Show the PopupMenu
                popupMenu.show();

                playbackSpeedPopupMenuListener.onPopupMenuShown();
            }
        });
    }

    public void setPlaybackSpeedButtonEnabled(boolean isEnabled) {
        if (playbackSpeedButton != null) {
            playbackSpeedButton.setEnabled(isEnabled);
        }
    }

    public void hidePlaybackButton(boolean showPlaybackSpeedButton) {
        if (playbackSpeedButton != null && !showPlaybackSpeedButton) {
            playbackSpeedButton.setEnabled(false);
            playbackSpeedButton.setVisibility(INVISIBLE);
        }
    }

    public void setPlaybackSpeedText(String text) {
        playbackSpeedButton.setText(text);
    }

    public void setPlaybackSpeedOptions(PlaybackSpeedOptions playbackSpeedOptions) {
        popupMenu.setPlaybackSpeedOptions(playbackSpeedOptions);
    }
}
