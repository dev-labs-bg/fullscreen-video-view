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

package bg.devlabs.fullscreenvideoviewsample;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static bg.devlabs.fullscreenvideoviewsample.CustomChecks.clickNoConstraints;
import static bg.devlabs.fullscreenvideoviewsample.CustomChecks.isOrientationLandscape;
import static bg.devlabs.fullscreenvideoviewsample.CustomChecks.isOrientationPortrait;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

/**
 * Created by Slavi Petrov on 27.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
@RunWith(AndroidJUnit4.class)
public class FullscreenVideoViewTest {

    @Rule
    public final ActivityTestRule<NoActionBarActivity> activityTestRule = new ActivityTestRule<>(
            NoActionBarActivity.class);

    @Test
    public void fullscreenVideoViewShown() {
        onView(withId(R.id.fullscreenVideoView))
                .check(matches(isDisplayed()));
    }

    @Test
    public void surfaceViewShown() {
        onView(withId(R.id.surface_view))
                .check(matches(isDisplayed()));
    }

    @Test
    public void controllerHidden() {
        onView(withId(R.id.video_controller))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void controllerViewsShownAndClickable() {
        clickToShowController();

        onView(withId(R.id.video_controller))
                .check(matches(isDisplayed()));

        onView(withId(R.id.fullscreen_media_button))
                .check(matches(isDisplayed()))
                .check(matches(isClickable()));

        onView(withId(R.id.start_pause_media_button))
                .check(matches(isDisplayed()))
                .check(matches(isClickable()));

        onView(withId(R.id.progress_seek_bar))
                .check(matches(isDisplayed()));
    }

    @Test
    public void fullscreenButtonClick() {
        clickToShowController();

        onView(withId(R.id.fullscreen_media_button))
                .check(matches(allOf(isEnabled(), isClickable())))
                .perform(clickNoConstraints());

        assertTrue(isOrientationLandscape(isRoot()));

        onView(isRoot())
                .perform(pressBack());

        assertTrue(!activityTestRule.getActivity().isFinishing());

        assertTrue(isOrientationPortrait(isRoot()));
    }

    private static void clickToShowController() {
        onView(withId(R.id.fullscreenVideoView))
                .check(matches(isDisplayed()))
                .perform(click());
    }
}
