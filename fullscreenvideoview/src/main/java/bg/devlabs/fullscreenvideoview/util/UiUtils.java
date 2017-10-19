package bg.devlabs.fullscreenvideoview.util;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import bg.devlabs.fullscreenvideoview.FullscreenVideoView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by Slavi Petrov on 19.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
public class UiUtils {
    /**
     * Shows all views except the parent layout.
     *
     * @param parentLayout the top layout in the XML file
     */
    public static void showOtherViews(ViewGroup parentLayout) {
        List<View> views = getAllChildViews(parentLayout);
        int size = views.size();
        for (int i = 1; i < size; i++) {
            View view = views.get(i);
            if (view instanceof FullscreenVideoView) {
                continue;
            }

            view.setVisibility(VISIBLE);
        }
    }

    /**
     * Hides all views except the parent layout.
     *
     * @param parentLayout the top layout in the XML file
     */
    public static void hideOtherViews(ViewGroup parentLayout) {
        List<View> views = getAllChildViews(parentLayout);
        int size = views.size();
        for (int i = 1; i < size; i++) {
            View view = views.get(i);
            if (view instanceof FullscreenVideoView) {
                continue;
            }

            view.setVisibility(GONE);
        }
    }

    /**
     * Get all child views from the parent layout except the VideoView.
     *
     * @param parentLayout the top layout in the XML file
     * @return a list child views
     */
    private static List<View> getAllChildViews(ViewGroup parentLayout) {
        List<View> visited = new ArrayList<>();
        List<View> unvisited = new ArrayList<>();
        unvisited.add(parentLayout);

        while (!unvisited.isEmpty()) {
            View child = unvisited.remove(0);
            visited.add(child);

            if (child instanceof FullscreenVideoView) {
                continue;
            }

            if (!(child instanceof ViewGroup)) {
                continue;
            }

            ViewGroup group = (ViewGroup) child;
            final int childCount = group.getChildCount();
            for (int i = 0; i < childCount; i++) {
                unvisited.add(group.getChildAt(i));
            }
        }

        return visited;
    }
}
