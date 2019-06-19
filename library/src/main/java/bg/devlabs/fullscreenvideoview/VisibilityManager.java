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

import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static bg.devlabs.fullscreenvideoview.Constants.VIEW_TAG_CLICKED;

/**
 * Created by Slavi Petrov on 19.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
public class VisibilityManager {

    private ArrayList<View> hiddenViews = new ArrayList<>();

    /**
     * Shows all views except the parent layout
     */
    public void showHiddenViews() {
        int size = hiddenViews.size();
        for (int i = 0; i < size; i++) {
            View view = hiddenViews.get(i);
            view.setVisibility(VISIBLE);
        }

        hiddenViews.clear();
    }

    /**
     * Hides all views except the parent layout
     *
     * @param parentLayout the top layout in the XML file
     */
    public void hideVisibleViews(ViewGroup parentLayout) {
        List<View> views = getVisibleChildViews(parentLayout);
        int size = views.size();
        for (int i = 0; i < size; i++) {
            View view = views.get(i);
            view.setVisibility(GONE);
            // Add the view in the hidden views
            hiddenViews.add(view);
        }
    }

    /**
     * Search recursively through all children of the parent layout and checks their class.
     * If they are ViewGroup classes, continues the recursion,
     * if they are View classes, terminates the recursion
     * <p>
     * Used in {@link #hideVisibleViews(ViewGroup)} to get all the Views that should be hidden
     * Used in {@link #showHiddenViews()} to get all the Views that should be shown
     *
     * @param parentLayout the top layout in XML file
     * @return a list of all non-ViewGroup views from the parent layout except the VideoView,
     * but including Toolbar
     */
    private List<View> getVisibleChildViews(View parentLayout) {
        if (!shouldCheckChildren(parentLayout)) {
            return Collections.singletonList(parentLayout);
        }

        int childCount = ((ViewGroup) parentLayout).getChildCount();
        List<View> children = new ArrayList<>(childCount);
        for (int i = 0; i < childCount; i++) {
            View view = ((ViewGroup) parentLayout).getChildAt(i);
            if (shouldCheckChildren(view)) {
                children.addAll(getVisibleChildViews(view));
            } else {
                if (view instanceof FullscreenVideoView) {
                    ImageButton fullscreenButton = view.findViewById(R.id.fullscreen_media_button);
                    String buttonTag = (String) fullscreenButton.getTag();

                    if (view.getVisibility() == VISIBLE &&
                            !Objects.equals(buttonTag, VIEW_TAG_CLICKED)) {
                        children.add(view);
                    }
                } else {
                    if (view.getVisibility() == VISIBLE) {
                        children.add(view);
                    }
                }
            }
        }
        return children;
    }

    /**
     * Check if a view has children to iterate
     * <p>
     * Used in {@link #getVisibleChildViews(View)} (View)} as a terminating case
     *
     * @param view the {@link View} that should be checked
     * @return true if the View is a ViewGroup, but not FullscreenVideoView or Toolbar
     */
    private boolean shouldCheckChildren(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return view instanceof ViewGroup &&
                    !(view instanceof Toolbar) &&
                    !(view instanceof android.widget.Toolbar) &&
                    !(view instanceof FullscreenVideoView);
        } else {
            return view instanceof ViewGroup &&
                    !(view instanceof Toolbar) &&
                    !(view instanceof FullscreenVideoView);
        }
    }
}
