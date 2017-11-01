package bg.devlabs.fullscreenvideoviewexample;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;
import android.view.ViewGroup;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

/**
 * Created by Slavi Petrov on 27.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
class CustomChecks {
    static ViewAction clickNoConstraints() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isEnabled(); // No constraints, isEnabled and isClickable are checked
            }

            @Override
            public String getDescription() {
                return "click plus button";
            }

            @Override
            public void perform(UiController uiController, View view) {
                view.performClick();
            }
        };
    }

    static boolean isOrientationLandscape(Matcher<View> matcher) {
        final boolean[] isLandscape = {false};
        onView(matcher).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Check if orientation is landscape";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();
                int orientation = getActivityOrientation(view);
                if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        || orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                        || orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        || orientation == ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE) {
                    isLandscape[0] = true;
                }
            }


        });
        return isLandscape[0];
    }

    static boolean isOrientationPortrait(Matcher<View> matcher) {
        final boolean[] isPortrait = {false};
        onView(matcher).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Check if orientation is portrait";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();
                int orientation = getActivityOrientation(view);
                if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        || orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                        || orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                        || orientation == ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT) {
                    isPortrait[0] = true;
                }
            }


        });
        return isPortrait[0];
    }

    private static int getActivityOrientation(View view) {
        Activity activity = getActivity(view.getContext());

        if (activity == null && view instanceof ViewGroup) {
            ViewGroup v = (ViewGroup) view;
            int c = v.getChildCount();
            for (int i = 0; i < c && activity == null; ++i) {
                activity = getActivity(v.getChildAt(i).getContext());
            }
        }

        return activity.getRequestedOrientation();
    }

    private static Activity getActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    // TODO: Add method for checking if the rotation is enabled
    static ViewAction setOrientation(final int orientation) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Check if orientation is portrait";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();
                changeOrientation(view, orientation);
            }
        };
    }

    private static void changeOrientation(View view, int orientation) {
        Activity activity = getActivity(view.getContext());
        if (activity == null && view instanceof ViewGroup) {
            ViewGroup v = (ViewGroup) view;
            int c = v.getChildCount();
            for (int i = 0; i < c && activity == null; ++i) {
                activity = getActivity(v.getChildAt(i).getContext());
            }
        }
        activity.setRequestedOrientation(orientation);
    }
}
