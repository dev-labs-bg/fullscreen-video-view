package bg.devlabs.fullscreenvideoview;

import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by Slavi Petrov on 19.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
public class UiUtils {
    private UiUtils() {
    }

    /**
     * Shows all views except the parent layout
     *
     * @param parentLayout the top layout in the XML file
     */
    public static void showOtherViews(ViewGroup parentLayout) {
        List<View> views = getAllChildViews(parentLayout);
        int size = views.size();
        for (int i = 0; i < size; i++) {
            View view = views.get(i);
            view.setVisibility(VISIBLE);
        }
    }

    /**
     * Hides all views except the parent layout
     *
     * @param parentLayout the top layout in the XML file
     */
    public static void hideOtherViews(ViewGroup parentLayout) {
        List<View> views = getAllChildViews(parentLayout);
        int size = views.size();
        for (int i = 0; i < size; i++) {
            View view = views.get(i);
            view.setVisibility(GONE);
        }
    }

    /**
     * Search recursively through all children of the parent layout and checks their class.
     * If they are ViewGroup classes, continues the recursion,
     * if they are View classes, terminates the recursion
     * <p>
     * Used in {@link #hideOtherViews(ViewGroup)} to get all the Views that should be hidden
     * Used in {@link #showOtherViews(ViewGroup)} to get all the Views that should be shown
     *
     * @param parentLayout the top layout in XML file
     * @return a list of all non-ViewGroup views from the parent layout except the VideoView,
     * but including Toolbar
     */
    private static List<View> getAllChildViews(View parentLayout) {
        if (!shouldCheckChildren(parentLayout)) {
            return Collections.singletonList(parentLayout);
        }

        int childCount = ((ViewGroup) parentLayout).getChildCount();
        List<View> children = new ArrayList<>(childCount);
        for (int i = 0; i < childCount; i++) {
            View view = ((ViewGroup) parentLayout).getChildAt(i);
            if (shouldCheckChildren(view)) {
                children.addAll(getAllChildViews(view));
            } else {
                if (!(view instanceof FullscreenVideoView)) {
                    children.add(view);
                }
            }
        }
        return children;
    }

    /**
     * Check if a view has children to iterate
     * <p>
     * Used in {@link #getAllChildViews(View)} as a terminating case
     *
     * @param view the {@link View} that should be checked
     * @return true if the View is a ViewGroup, but not FullscreenVideoView or Toolbar
     */
    private static boolean shouldCheckChildren(View view) {
        return view instanceof ViewGroup &&
                !(view instanceof Toolbar) &&
                !(view instanceof android.widget.Toolbar) &&
                !(view instanceof FullscreenVideoView);
    }
}
