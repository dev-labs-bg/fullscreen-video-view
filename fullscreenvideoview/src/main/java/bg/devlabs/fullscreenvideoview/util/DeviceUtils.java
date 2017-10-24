package bg.devlabs.fullscreenvideoview.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.provider.Settings;
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
     * Check if the device has software keys
     *
     * @param display the display from the Activity class
     * @return true or false according to whether the device has software keys or not
     */
    public static boolean hasSoftKeys(WindowManager windowManager) {
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
     * @param resources the resources from the context
     * @return the navigation bar height in pixels
     */
    public static int getNavigationBarHeight(Resources resources) {
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen",
                "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    /**
     * Get the device display metrics
     *
     * @param context the app's context
     * @return the device display metrics
     */
    public static DisplayMetrics getDisplayMetrics(Context context) {
        return context.getResources().getDisplayMetrics();
    }

    /**
     * Check the position of the system bar
     *
     * @param
     * @return true or false according to whether the system bar is on bottom or on top
     */
    public static boolean isSystemBarOnBottom(WindowManager windowManager,
                                              Resources resources) {
        Point realPoint = new Point();
        Display display;
        if (windowManager != null) {
            display = windowManager.getDefaultDisplay();
            display.getRealSize(realPoint);
            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);
            Configuration cfg = resources.getConfiguration();
            boolean canMove = (metrics.widthPixels != metrics.heightPixels &&
                    cfg.smallestScreenWidthDp < 600);

            return (!canMove || metrics.widthPixels < metrics.heightPixels);
        }
        return true;
    }

    /**
     * Check if the device's rotation is enabled
     *
     * @param contentResolver from the app's context
     * @return true or false according to whether the rotation is enabled or disabled
     */
    public static boolean isRotationEnabled(ContentResolver contentResolver) {
        return Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION,
                0) == 1;
    }

    /**
     *
     * @param windowManager
     * @param resources
     * @return
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public static int getScreenWidth(WindowManager windowManager, Resources resources) {
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        boolean hasSoftKeys = hasSoftKeys(windowManager);
        boolean isSystemBarOnSide = !isSystemBarOnBottom(windowManager, resources);
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
     *
     * @param windowManager
     * @param resources
     * @return
     */
    public static int getScreenHeight(WindowManager windowManager, Resources resources) {
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        boolean hasSoftKeys = hasSoftKeys(windowManager);
        boolean isSystemBarOnBottom = isSystemBarOnBottom(windowManager, resources);
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
