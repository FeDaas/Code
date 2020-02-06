package de.moviemanager.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import de.moviemanager.core.storage.RuntimeStorageAccess;
import de.moviemanager.data.Portrayable;
import de.util.operationflow.ReversibleOperations.ReversibleTransformation;

import static android.graphics.Bitmap.createScaledBitmap;

public enum  DrawableUtils {
    ;

    private static final RuntimeStorageAccess STORAGE = RuntimeStorageAccess.getInstance();

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap crop(Bitmap inp, int wScale, int hScale) {
        int width = inp.getWidth();
        int height = inp.getHeight();
        int smallestSegment = width / wScale < height /hScale ? width /wScale : height /hScale;
        int newWidth = smallestSegment * wScale;
        int newHeight = smallestSegment * hScale;
        Log.d("DU", "W: " + width + ", H: " + height + ", w: " + newWidth + ", h: " + newHeight);
        Bitmap result = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        result.setDensity(Bitmap.DENSITY_NONE);
        Canvas c = new Canvas(result);
        Log.d("DU", "W: " + result.getWidth() +
                ", H: " + result.getHeight() +
                ", w: " + (newWidth/2 - width/2) +
                ", h: " + (newHeight/2  - height/2));
        c.drawBitmap(inp, newWidth/2f - width/2f, newHeight/2f  - height/2f, null);
        return result;
    }

    public static Bitmap rescale(Bitmap b, int w, int h) {
        return createScaledBitmap(b, w, h, false);
    }

    public static <T extends Portrayable> ReversibleTransformation<T> setImageInStorage(Drawable image, boolean customImage) {
        return new ReversibleTransformation<T>() {

            @Override
            public T forward(T obj) {
                if(customImage) {
                    STORAGE.setImageForPortrayable(obj, drawableToBitmap(image));
                } else {
                    STORAGE.setImageForPortrayable(obj, null);
                }
                return obj;
            }

            @Override
            public T backward(T obj) {
                throw new UnsupportedOperationException("This action can't be undone!");
            }
        };
    }
}
