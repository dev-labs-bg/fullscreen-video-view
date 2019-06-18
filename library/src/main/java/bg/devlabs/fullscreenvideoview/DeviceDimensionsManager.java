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

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by Slavi Petrov on 29.05.2019
 * Dev Labs
 * slavi@devlabs.bg
 */

public class DeviceDimensionsManager {

    private static volatile DeviceDimensionsManager INSTANCE;

    private DeviceDimensionsManager() {
        if (INSTANCE != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static DeviceDimensionsManager getInstance() {
        if (INSTANCE == null) {
            synchronized (DeviceDimensionsManager.class) {
                if (INSTANCE == null) INSTANCE = new DeviceDimensionsManager();
            }
        }

        return INSTANCE;
    }

    // DeviceDimensionsManager.getDisplayWidth(context) => (display width in pixels)
    public int getDisplayWidth(Context context) {
        return getDisplayMetrics(context).widthPixels;
    }

    // DeviceDimensionsManager.getDisplayHeight(context) => (display height in pixels)
    public int getDisplayHeight(Context context) {
        return getDisplayMetrics(context).heightPixels;
    }

    public int getRealHeight(Context context) {
        DisplayMetrics realDisplayMetrics = getRealDisplayMetrics(context);

        if (realDisplayMetrics == null) {
            return 0;
        } else {
            return realDisplayMetrics.heightPixels;
        }
    }

    public int getRealWidth(Context context) {
        DisplayMetrics realDisplayMetrics = getRealDisplayMetrics(context);

        if (realDisplayMetrics == null) {
            return 0;
        } else {
            return realDisplayMetrics.widthPixels;
        }
    }

    private DisplayMetrics getRealDisplayMetrics(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return null;
        }

        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics realMetrics = new DisplayMetrics();
        display.getRealMetrics(realMetrics);

        return realMetrics;
    }

    private DisplayMetrics getDisplayMetrics(Context context) {
        return context.getResources().getDisplayMetrics();
    }

    // DeviceDimensionsManager.convertDpToPixel(25f, context) => (25dp converted to pixels)
    public static float convertDpToPixel(float dp, Context context) {
        Resources r = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    // DeviceDimensionsManager.convertPixelsToDp(25f, context) => (25px converted to dp)
    public static float convertPixelsToDp(float px, Context context) {
        Resources r = context.getResources();
        DisplayMetrics metrics = r.getDisplayMetrics();
        return px / (metrics.densityDpi / 160f);
    }
}
