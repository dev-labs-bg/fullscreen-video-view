package bg.devlabs.fullscreenvideoview;

import android.content.Context;
import android.content.res.Resources;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * Created by Slavi Petrov on 20.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
public interface IFullscreenVideoView {
    boolean isLandscape();

    void toggleFullscreen();

    Context getContext();

    ViewParent getParent();

    int getWidth();

    int getHeight();

    ViewGroup.LayoutParams getLayoutParams();

    void setLayoutParams(ViewGroup.LayoutParams params);

    Resources getResources();
}
