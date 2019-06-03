package bg.devlabs.fullscreenvideoview;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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

    public static Bitmap scaleImage(Resources resources, int thumbnailResId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, thumbnailResId, options);

        options.inSampleSize = BitmapScaler.calculateInSampleSize(options, 500, 500);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeResource(resources, thumbnailResId, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth,
                                            int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    // Scale and maintain aspect ratio given a desired height
    // BitmapScaler.scaleToFitHeight(bitmap, 100);
    public static Bitmap scaleToFitHeight(Bitmap bitmap, int height) {
        float factor = height / (float) bitmap.getHeight();
        return Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * factor), height, true);
    }

}
