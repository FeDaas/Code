package de.moviemanager.ui.detail.modifiable;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;

import androidx.annotation.IdRes;

import java.util.Optional;
import java.util.function.Function;

import de.moviemanager.R;
import de.moviemanager.core.storage.RuntimeStorageAccess;
import de.moviemanager.ui.detail.modifications.Modification;
import de.util.Pair;
import de.util.operationflow.ReversibleOperations;

import static android.app.Activity.RESULT_OK;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static de.moviemanager.data.ImagePyramid.ImageSize.LARGE;
import static de.moviemanager.util.DrawableUtils.crop;
import static de.moviemanager.util.DrawableUtils.drawableToBitmap;
import static de.moviemanager.util.ScrollViewUtils.isScrollViewAbove;
import static de.moviemanager.util.ScrollViewUtils.isScrollViewBelow;
import static de.util.Pair.paired;

public class ImageAttribute<X> extends ModifiableAttribute<X, Pair<Drawable, Boolean>> {
    private static final RuntimeStorageAccess STORAGE = RuntimeStorageAccess.getInstance();

    private final int imageId;
    private final int resetButtonId;
    private final ScrollView root;
    private ImageView editImage;
    private ImageButton resetImage;
    private boolean customImage;

    private ImageAttribute(ModifiableAppCompatActivity modContext,
                           ScrollView root,
                           @IdRes int imageId,
                           @IdRes int resetButtonId) {
        super(modContext);
        this.root = root;
        this.imageId = imageId;
        this.resetButtonId = resetButtonId;
        this.customImage = false;
    }

    @Override
    public void bindViews() {
        editImage = getContext().findViewById(imageId);
        resetImage = getContext().findViewById(resetButtonId);
    }

    @Override
    public void bindListeners() {
        editImage.setOnClickListener(v -> {
            final Intent chooserIntent = createImageChooserIntent();
            getContext().startActivityForResult(chooserIntent, RESULT_OK, this::setImageFromIntent);
        });
        resetImage.setOnClickListener(v -> {
            getContext().addModification(new Modification<>(editImage.getDrawable(), p -> {
                editImage.setImageDrawable(p);
                setCustomImageFlag(true);
            }));
            editImage.setImageDrawable(STORAGE.getDefaultImage(getContext(), LARGE));
            setCustomImageFlag(false);
        });
    }

    public void setCustomImageFlag(boolean b) {
        customImage = b;
        resetImage.setEnabled(b);
        int color = getContext().getColor(b ? R.color.dark_red : R.color.light_gray);
        resetImage.setBackgroundColor(color);
    }


    private Intent createImageChooserIntent() {
        final Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        final Intent pickIntent = new Intent(Intent.ACTION_PICK);
        pickIntent.setDataAndType(EXTERNAL_CONTENT_URI, "image/*");

        final Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        return chooserIntent;
    }


    @Override
    protected void setContent(Pair<Drawable, Boolean> content) {
        editImage.setImageDrawable(content.first);
        setCustomImageFlag(content.second);
        resetImage.setEnabled(customImage);
    }

    @Override
    public Pair<Drawable, Boolean> getContent() {
        return Pair.paired(editImage.getDrawable(), customImage);
    }

    private void setImageFromIntent(final Intent data) {
        final Optional<Bitmap> opt = loadFromIntent(getContext(), data);
        if (!opt.isPresent()) {
            return;
        }
        final Bitmap b = opt.get();

        boolean wasCustom = customImage;
        setCustomImageFlag(true);

        final Bitmap oldBitmap = drawableToBitmap(editImage.getDrawable());
        final Bitmap c = crop(b, 2, 3);
        editImage.setImageBitmap(c);

        getContext().addModification(new Modification<>(
                paired(wasCustom, oldBitmap),
                p -> {
                    setCustomImageFlag(p.first);
                    editImage.setImageBitmap(p.second);

                    if (isScrollViewAbove(root, editImage))
                        root.scrollTo(0, resetImage.getBottom());
                    else if (isScrollViewBelow(root, editImage))
                        root.scrollTo(0, editImage.getTop());
                })
        );
    }

    private static Optional<Bitmap> loadFromIntent(final Context context, final Intent data) {
        Uri pickedImage = data.getData();
        if (pickedImage == null)
            return Optional.empty();

        String[] filePath = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(pickedImage, filePath, null, null, null);
        if (cursor == null)
            return Optional.empty();
        cursor.moveToFirst();
        String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap b = BitmapFactory.decodeFile(imagePath, options);
        cursor.close();
        return Optional.of(b);
    }

    public static class Builder<X> {
        private final ModifiableAppCompatActivity context;
        private int imageId;
        private int resetButtonId;
        private ScrollView root;
        private Function<Pair<Drawable, Boolean>, ReversibleOperations.ReversibleTransformation<X>> source;
        private Function<X, Pair<Drawable, Boolean>> contentExtractor;

        public Builder(ModifiableAppCompatActivity context) {
            this.context = context;
        }

        public Builder<X> setImageId(@IdRes int imageId) {
            this.imageId = imageId;
            return this;
        }

        public Builder<X> setResetButtonId(@IdRes int resetButtonId) {
            this.resetButtonId = resetButtonId;
            return this;
        }

        public Builder<X> setRoot(ScrollView root) {
            this.root = root;
            return this;
        }

        public Builder<X> setSource(Function<Pair<Drawable, Boolean>, ReversibleOperations.ReversibleTransformation<X>> source) {
            this.source = source;
            return this;
        }

        public Builder<X> setContentExtractor(Function<X, Pair<Drawable, Boolean>> contentExtractor) {
            this.contentExtractor = contentExtractor;
            return this;
        }

        public ImageAttribute<X> build() {
            ImageAttribute<X> attr = new ImageAttribute<>(context, root, imageId, resetButtonId);
            attr.setTransformationSource(source);
            attr.setContentExtractor(contentExtractor);
            return attr;
        }
    }
}
