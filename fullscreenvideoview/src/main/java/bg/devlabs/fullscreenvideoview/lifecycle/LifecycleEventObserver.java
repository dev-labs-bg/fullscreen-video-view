package bg.devlabs.fullscreenvideoview.lifecycle;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;


/**
 * Created by Slavi Petrov on 19.10.2017
 * Dev Labs
 * slavi@devlabs.bg
 */
public class LifecycleEventObserver implements LifecycleObserver {
    private OnLifecycleEventListener listener;

    public LifecycleEventObserver(OnLifecycleEventListener listener) {
        this.listener = listener;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void onDestroy() {
        listener.onDestroy();
    }
}
