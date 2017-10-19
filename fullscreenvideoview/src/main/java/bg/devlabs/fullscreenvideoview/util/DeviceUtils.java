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
    public static boolean hasSoftKeys(Display display) {
        boolean hasSoftwareKeys;

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        display.getRealMetrics(realDisplayMetrics);

        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        hasSoftwareKeys = (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;

        return hasSoftwareKeys;
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
     * @param context the app's context
     * @return true or false according to whether the system bar is on bottom or on top
     */
    public static boolean isSystemBarOnBottom(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point realPoint = new Point();
        Display display = wm.getDefaultDisplay();
        display.getRealSize(realPoint);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        Configuration cfg = context.getResources().getConfiguration();
        boolean canMove = (metrics.widthPixels != metrics.heightPixels &&
                cfg.smallestScreenWidthDp < 600);

        return (!canMove || metrics.widthPixels < metrics.heightPixels);
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
}
