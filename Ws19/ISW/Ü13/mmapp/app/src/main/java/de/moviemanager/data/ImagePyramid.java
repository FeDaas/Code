package de.moviemanager.data;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

import de.moviemanager.core.storage.JsonAttr;
import de.moviemanager.util.FileUtils;
import de.storage.StorageException;
import de.util.Identifiable;
import de.util.Traits;
import de.util.annotations.Trait;

import static android.graphics.Bitmap.createScaledBitmap;
import static android.graphics.BitmapFactory.decodeFile;
import static de.moviemanager.util.AndroidStringUtils.join;
import static java.io.File.separator;
import static java.lang.String.format;
import static java.util.Locale.US;

public class ImagePyramid implements Identifiable {
    public enum ImageSize {
        SMALL("small", 50, 75),
        MEDIUM("medium", 100, 150),
        LARGE("large", 200, 300);

        final String folder;
        final int width;
        final int height;

        ImageSize(String folder, int width, int height) {
            this.folder = folder;
            this.width = width;
            this.height = height;
        }
    }

    private static final Bitmap.CompressFormat FORMAT = Bitmap.CompressFormat.PNG;
    private static final Traits TRAITS = new Traits(ImagePyramid.class);

    @Trait @JsonAttr private final int id;
    @Trait @JsonAttr private String prefix;
    private String fileName;

    public ImagePyramid(int id) {
        this.id = id;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        this.fileName = prefix + format(US, "-%08d", id) + "." + FORMAT.name().toLowerCase();
    }

    public String getPrefix() {
        return prefix;
    }

    public Optional<Bitmap> loadBitmap(final String directory, final ImageSize size) {
        final String path = join(separator, directory, size.folder, fileName);

        if(!new File(path).exists())
            return Optional.empty();
        return Optional.ofNullable(decodeFile(path));
    }

    public void updateImage(final String directory, final Bitmap bitmap) {
        for(final ImageSize size : ImageSize.values()) {
            final String path = join(separator, directory, size.folder, fileName);
            final File file = new File(path);
            if(bitmap == null) {
                try {
                    FileUtils.delete(file);
                } catch (IOException e) {
                    throw new StorageException(format("Couldn't delete '%s'.", file));
                }
                continue;
            }
            final Bitmap scaled = createScaledBitmap(bitmap, size.width, size.height, false);

            try {
                FileUtils.createDirectory(file);
            } catch (IOException e) {
                throw new StorageException(format("Failed to create '%s'.", file.getParentFile()));
            }

            try(FileOutputStream out = new FileOutputStream(file)) {
                scaled.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (IOException e) {
                Log.e("ImagePyramid", "Couldn't write size " + size, e);
            }
        }
    }

    @Override
    public int id() {
        return id;
    }

    @NonNull
    @Override
    public String toString() {
        return "ImagePyramid{id=" + id + ", fileName='" + fileName + "'}";
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return TRAITS.testEqualityBetween(this, obj);
    }

    @Override
    public int hashCode() {
        return TRAITS.createImmutableHashFor(this);
    }
}
