package bg.devlabs.fullscreenvideoview.orientation;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import bg.devlabs.fullscreenvideoview.FullscreenVideoView;
import bg.devlabs.fullscreenvideoview.util.UiUtils;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;

/**
 * Created by Slavi Petrov on 24.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 * <p>
 * Handles orientation changes. Updates the VideoView layout params. Hides/shows the toolbar.
 */
public abstract class OrientationDelegate extends OrientationEventListener {
    private FullscreenVideoView videoView;
    private int originalWidth;
    private int originalHeight;
    private boolean isLandscape;
    private ContentResolver contentResolver;
    // Orientation
    private LandscapeOrientation landscapeOrientation = LandscapeOrientation.SENSOR;
    private PortraitOrientation portraitOrientation = PortraitOrientation.PORTRAIT;

    protected OrientationDelegate(Context context, FullscreenVideoView fullscreenVideoView) {
        super(context);
        videoView = fullscreenVideoView;
        contentResolver = context.getContentResolver();
    }

    public void activateFullscreen() {
        // Update isLandscape flag
        if (!isLandscape) {
            isLandscape = true;
        }

        // Fullscreen active
        onOrientationChanged();

        // Change the screen orientation to SENSOR_LANDSCAPE
        Activity activity = ((Activity) videoView.getContext());
        setOrientation(landscapeOrientation.getValue());

        UiUtils.hideOtherViews(getParent());

        // Save the video player original width and height
        originalWidth = videoView.getWidth();
        originalHeight = videoView.getHeight();
        // TODO: Add check if the video should be landscape or portrait in isLandscape
        updateLayoutParams();

        // Hiding the supportToolbar
        hideActionBar();

        // Hide status bar
        toggleSystemUiVisibility(activity.getWindow());
    }

    protected abstract void onOrientationChanged();

    @SuppressWarnings("SuspiciousNameCombination")
    private void updateLayoutParams() {
        ViewGroup.LayoutParams params = videoView.getLayoutParams();
        Context context = videoView.getContext();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return;
        }
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics realMetrics = new DisplayMetrics();
        display.getRealMetrics(realMetrics);
        params.width = realMetrics.widthPixels;
        params.height = realMetrics.heightPixels;
        videoView.setLayoutParams(params);
    }

    public void exitFullscreen() {
        // Update isLandscape flag
        if (isLandscape) {
            isLandscape = false;
        }

        // Update the fullscreen button drawable
        onOrientationChanged();

        // Change the screen orientation to PORTRAIT
        Activity activity = (Activity) videoView.getContext();
        // TODO: Calculating the size according to if the view is on the whole screen or not
        setOrientation(portraitOrientation.getValue());


        UiUtils.showOtherViews(getParent());

        ViewGroup.LayoutParams params = videoView.getLayoutParams();
        params.width = originalWidth;
        params.height = originalHeight;
        videoView.setLayoutParams(params);

        showActionBar();
        toggleSystemUiVisibility(activity.getWindow());
    }

    private ViewGroup getParent() {
        Window window = ((Activity) videoView.getContext()).getWindow();
        ViewGroup decorView = (ViewGroup) window.getDecorView();
        return decorView.findViewById(android.R.id.content);
    }

    private void toggleSystemUiVisibility(Window activityWindow) {
        int newUiOptions = activityWindow.getDecorView().getSystemUiVisibility();
        newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        activityWindow.getDecorView().setSystemUiVisibility(newUiOptions);
    }

    private void showActionBar() {
        if (videoView.getContext() instanceof AppCompatActivity) {
            ActionBar supportActionBar = ((AppCompatActivity) videoView.getContext())
                    .getSupportActionBar();
            if (supportActionBar != null) {
                supportActionBar.show();
            }
        }
        if (videoView.getContext() instanceof Activity) {
            android.app.ActionBar actionBar = ((Activity) videoView.getContext()).getActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    }

    private void hideActionBar() {
        if (videoView.getContext() instanceof AppCompatActivity) {
            ActionBar supportActionBar = ((AppCompatActivity) videoView.getContext())
                    .getSupportActionBar();
            if (supportActionBar != null) {
                supportActionBar.hide();
            }
        }

        if (videoView.getContext() instanceof Activity) {
            android.app.ActionBar actionBar = ((Activity) videoView.getContext()).getActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
        }
    }

    public void setOrientation(int orientation) {
        ((Activity) videoView.getContext()).setRequestedOrientation(orientation);
    }

    public boolean shouldHandleOnBackPressed() {
        if (isLandscape) {
            // Locks the screen orientation to portrait
            setOrientation(portraitOrientation.getValue());
            onOrientationChanged();
            return true;
        }

        return false;
    }

    public void toggleFullscreen() {
        isLandscape = !isLandscape;
        int newOrientation = portraitOrientation.getValue();
        if (isLandscape) {
            newOrientation = landscapeOrientation.getValue();
        }
        setOrientation(newOrientation);
    }

    public void setLandscapeOrientation(LandscapeOrientation landscapeOrientation) {
        this.landscapeOrientation = landscapeOrientation;
    }

    public void setPortraitOrientation(PortraitOrientation portraitOrientation) {
        this.portraitOrientation = portraitOrientation;
    }

    /**
     * @param a
     * @param b
     * @param epsilon
     * @return
     */
    private boolean epsilonCheck(int a, int b, int epsilon) {
        return a > b - epsilon && a < b + epsilon;
    }

    @Override
    public void onOrientationChanged(int orientation) {
        // If the device's rotation is not enabled do not proceed further with the logic
        if (!isRotationEnabled(contentResolver)) {
            return;
        }
        int epsilon = 10;
        int leftLandscape = 90;
        int rightLandscape = 270;
        int portrait = 0;

        if ((epsilonCheck(orientation, leftLandscape, epsilon) ||
                epsilonCheck(orientation, rightLandscape, epsilon)) && !isLandscape) {
            isLandscape = true;
            setOrientation(SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }

        if (epsilonCheck(orientation, portrait, epsilon) && isLandscape) {
            isLandscape = false;
            setOrientation(SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    /**
     * Check if the device's rotation is enabled
     *
     * @param contentResolver from the app's context
     * @return true or false according to whether the rotation is enabled or disabled
     */
    private boolean isRotationEnabled(ContentResolver contentResolver) {
        return Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION,
                0) == 1;
    }

    public boolean isLandscape() {
        return isLandscape;
    }
}
