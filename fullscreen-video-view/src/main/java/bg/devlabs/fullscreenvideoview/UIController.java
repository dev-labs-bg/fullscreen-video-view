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

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class UIController {
    public void toggleToolbarVisibility(Context context, boolean isVisible) {
        if (context instanceof AppCompatActivity) {
            toggleSupportActionBarVisibility(context, isVisible);
        }
        if (context instanceof Activity) {
            toggleActionBarVisibility(context, isVisible);
        }
    }

    public void toggleSystemUiVisibility(Context context) {
        Window activityWindow = ((Activity) context).getWindow();
        View decorView = activityWindow.getDecorView();
        int newUiOptions = decorView.getSystemUiVisibility();
        newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(newUiOptions);
    }

    private void toggleSupportActionBarVisibility(Context context, boolean isVisible) {
        // AppCompatActivity support action bar
        ActionBar supportActionBar = ((AppCompatActivity) context)
                .getSupportActionBar();
        if (supportActionBar != null) {
            if (isVisible) {
                supportActionBar.show();
            } else {
                supportActionBar.hide();
            }
        }
    }

    private void toggleActionBarVisibility(Context context, boolean isVisible) {
        // Activity action bar
        android.app.ActionBar actionBar = ((Activity) context).getActionBar();
        if (actionBar != null) {
            if (isVisible) {
                actionBar.show();
            } else {
                actionBar.hide();
            }
        }
    }
}
