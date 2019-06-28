package bg.devlabs.fullscreenvideoview.listener;

/**
 * Created by Slavi Petrov on 28.06.2019
 * Dev Labs
 * slavi@devlabs.bg
 */
public class FullscreenVideoViewException {

    @SuppressWarnings("unused")
    private int code;
    @SuppressWarnings("unused")
    private String message;

    public FullscreenVideoViewException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public FullscreenVideoViewException(String message) {
        this.code = -1;
        this.message = message;
    }
}
