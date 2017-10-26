package bg.devlabs.fullscreenvideoview.util;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by Slavi Petrov on 05.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
public class DeviceUtils {
    /**
     * Private constructor which prevents accidentally instantiation of the class.
     */
    private DeviceUtils() {
    }

    /**
     * Check if the device has software keys.
     *
     * @param windowManager the Activity's windowManager from the display
     * @return true or false according to whether the device has software keys or not
     */
    private static boolean hasSoftKeys(WindowManager windowManager) {
        Display display = windowManager.getDefaultDisplay();

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        display.getRealMetrics(realDisplayMetrics);

        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        return (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
    }

    /**
     * Get the navigation bar height
     *
     * @param resources the resources from the corresponding context
     * @return the navigation bar height in pixels
     */
    private static int getNavigationBarHeight(Resources resources) {
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen",
                "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    /**
     * Check the position of the system bar
     *
     * @param windowManager the Activity's windowManager from the display
     * @param resources     the resources from the corresponding context
     * @return true or false according to whether the system bar is on bottom or on top
     */
    private static boolean isNavigationBarOnBottom(WindowManager windowManager,
                                                   Resources resources) {
        Point realPoint = new Point();
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            display.getRealSize(realPoint);
            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);
            Configuration config = resources.getConfiguration();
            // TODO: Rename
            boolean canMove = !(metrics.widthPixels != metrics.heightPixels &&
                    config.smallestScreenWidthDp < 600);

            return (canMove || metrics.widthPixels < metrics.heightPixels);
        }
        return true;
    }

    /**
     * Get the device's screen width
     *
     * @param windowManager the Activity's windowManager from the display
     * @param resources     the resources from the corresponding context
     * @return the device's screen width in pixels
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public static int getScreenWidth(WindowManager windowManager, Resources resources) {
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        boolean hasSoftKeys = hasSoftKeys(windowManager);
        boolean isSystemBarOnSide = !isNavigationBarOnBottom(windowManager, resources);
        int navBarHeight = getNavigationBarHeight(resources);

        int width = displayMetrics.widthPixels;
        if (hasSoftKeys) {
            if (isSystemBarOnSide) {
                width += navBarHeight;
            }
        }

        return width;
    }

    /**
     * Get the device's screen height
     *
     * @param windowManager the Activity's windowManager from the display
     * @param resources     the resources from the corresponding context
     * @return the device's screen height in pixels
     */
    public static int getScreenHeight(WindowManager windowManager, Resources resources) {
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        boolean hasSoftKeys = hasSoftKeys(windowManager);
        boolean isSystemBarOnBottom = isNavigationBarOnBottom(windowManager, resources);
        int navBarHeight = getNavigationBarHeight(resources);

        int height = displayMetrics.heightPixels;
        if (hasSoftKeys) {
            if (isSystemBarOnBottom) {
                height += navBarHeight;
            }
        }

        return height;
    }
}
