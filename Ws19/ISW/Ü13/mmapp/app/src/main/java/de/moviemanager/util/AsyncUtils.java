package de.moviemanager.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

public enum  AsyncUtils {
    ;

    public static AsyncTask<URL, Void, Bitmap> imageUrlLoader(ImageView imageView, Consumer<Bitmap> onImageLoaded) {
        return new AsyncTask<URL, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(URL... urls) {
                return getBitmapFromURL(urls[0]);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if(imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }

                if(onImageLoaded != null) {
                    onImageLoaded.accept(bitmap);
                }
            }
        };
    }

    private static Bitmap getBitmapFromURL(final URL url) {
        Bitmap result;
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            result = BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            result = null;
        }

        return result;
    }
}
