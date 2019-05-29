package bg.devlabs.fullscreenvideoview;

import android.graphics.Bitmap;

/**
 * Created by Slavi Petrov on 29.05.2019
 * Dev Labs
 * slavi@devlabs.bg
 */
public class BitmapScaler {

    // Scale and maintain aspect ratio given a desired width
    // BitmapScaler.scaleToFitWidth(bitmap, 100);
    public static Bitmap scaleToFitWidth(Bitmap bitmap, int width) {
        float factor = width / (float) bitmap.getWidth();
        return Bitmap.createScaledBitmap(bitmap, width, (int) (bitmap.getHeight() * factor), true);
    }


    // Scale and maintain aspect ratio given a desired height
    // BitmapScaler.scaleToFitHeight(bitmap, 100);
    public static Bitmap scaleToFitHeight(Bitmap bitmap, int height) {
        float factor = height / (float) bitmap.getHeight();
        return Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * factor), height, true);
    }

}
