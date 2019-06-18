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

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.support.annotation.Nullable;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;
import android.view.ViewGroup;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static bg.devlabs.fullscreenvideoviewsample.CustomChecks.OrientationViewAction.OrientationType.LANDSCAPE;
import static bg.devlabs.fullscreenvideoviewsample.CustomChecks.OrientationViewAction.OrientationType.PORTRAIT;

/**
 * Created by Slavi Petrov on 27.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
class CustomChecks {
    private CustomChecks() {
    }

    static ViewAction clickNoConstraints() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isEnabled(); // No constraints, isEnabled and isClickable are checked
            }

            @Override
            public String getDescription() {
                return "Click a view with no constraints.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                view.performClick();
            }
        };
    }

    static boolean isOrientationLandscape(Matcher<View> matcher) {
        final boolean[] isLandscape = {false};
        onView(matcher).perform(new OrientationViewAction(isLandscape, LANDSCAPE));
        return isLandscape[0];
    }

    static boolean isOrientationPortrait(Matcher<View> matcher) {
        final boolean[] isPortrait = {false};
        onView(matcher).perform(new OrientationViewAction(isPortrait, PORTRAIT));
        return isPortrait[0];
    }

    private static int getActivityOrientation(View view) {
        return getActivityByView(view).getRequestedOrientation();
    }

    private static Activity getActivityByView(View view) {
        Activity activity = getActivityByView(view.getContext());

        if (activity == null && view instanceof ViewGroup) {
            ViewGroup v = (ViewGroup) view;
            int childCount = v.getChildCount();
            for (int i = 0; i < childCount && activity == null; ++i) {
                activity = getActivityByView(v.getChildAt(i).getContext());
            }
        }

        return activity;
    }

    @Nullable
    private static Activity getActivityByView(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    static class OrientationViewAction implements ViewAction {
        enum OrientationType {PORTRAIT, LANDSCAPE}

        private final boolean[] isOrientation;
        private final OrientationType orientationType;

        OrientationViewAction(boolean[] isOrientation, OrientationType orientationType) {
            this.isOrientation = isOrientation;
            this.orientationType = orientationType;
        }

        @Override
        public Matcher<View> getConstraints() {
            return isRoot();
        }

        @Override
        public String getDescription() {
            String orientation = orientationType == PORTRAIT ? "portrait" : "landscape";
            return "Check if orientation is " + orientation;
        }

        @Override
        public void perform(UiController uiController, View view) {
            uiController.loopMainThreadUntilIdle();
            int orientation = getActivityOrientation(view);
            boolean checkOrientation = false;
            switch (orientationType) {
                case PORTRAIT:
                    checkOrientation = orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            || orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                            || orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                            || orientation == ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT;
                    break;

                case LANDSCAPE:
                    checkOrientation = orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            || orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                            || orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            || orientation == ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE;
                    break;
            }

            if (checkOrientation) {
                isOrientation[0] = true;
            }
        }
    }
}
