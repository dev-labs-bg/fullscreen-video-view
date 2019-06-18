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
