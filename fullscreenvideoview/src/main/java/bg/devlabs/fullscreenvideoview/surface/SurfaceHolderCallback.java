package bg.devlabs.fullscreenvideoview.surface;

import android.view.SurfaceHolder;

/**
 * Created by Slavi Petrov on 19.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
public class SurfaceHolderCallback implements SurfaceHolder.Callback {
    private OnSurfaceEventListener listener;

    public SurfaceHolderCallback(OnSurfaceEventListener listener) {
        this.listener = listener;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        listener.onSurfaceCreated();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        listener.onSurfaceDestroyed();
    }
}
