package bg.devlabs.fullscreenvideoview.model;

/**
 * Contains information about an error in VideoMediaPlayer.
 */
public class MediaPlayerError {

    /**
     * The type of the error.
     */
    private MediaPlayerErrorType type;

    /**
     * The error code received when an error from an async operation is present.
     *
     * @see MediaPlayerErrorType#ASYNC_OPERATION
     */
    private int code = -1;

    /**
     * The error message received when there is an error when setting the media player data source.
     *
     * @see MediaPlayerErrorType#DATA_SOURCE_READ
     */
    private String message = null;

    /**
     * Creates a new MediaPlayerError with type and code.
     *
     * @param type The error type
     * @param code The error code
     */
    public MediaPlayerError(MediaPlayerErrorType type, int code) {
        this.type = type;
        this.code = code;
    }

    /**
     * Creates a new MediaPlayerError with type and message.
     *
     * @param type The error type
     * @param message The error message
     */
    public MediaPlayerError(MediaPlayerErrorType type, String message) {
        this.type = type;
        this.message = message;
    }

    /**
     * Gets the error type.
     *
     * @return the error type
     */
    public MediaPlayerErrorType getType() {
        return type;
    }

    /**
     * Gets the error code.
     *
     * @return the error code
     */
    public int getCode() {
        return code;
    }

    /**
     * Gets the error message.
     *
     * @return the error message
     */
    public String getMessage() {
        return message;
    }
}
