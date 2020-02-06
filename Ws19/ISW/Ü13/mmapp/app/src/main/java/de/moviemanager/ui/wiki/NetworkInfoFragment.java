package de.moviemanager.ui.wiki;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.moviemanager.R;
import de.moviemanager.android.ResultHandlingFragment;
import de.moviemanager.util.autobind.AutoBind;
import de.moviemanager.util.autobind.Bind;

import static java.util.Optional.ofNullable;

public class NetworkInfoFragment extends ResultHandlingFragment {
    private static final String IMAGE_BUNDLE_ID = "image_id";
    private static final String MESSAGE_BUNDLE_ID = "message_id";
    private static final String ANIMATION_BUNDLE_ID = "animation_id";

    private static final String NO_ANIMATION = "no_animation";
    private static final String ROTATE_ANIMATION = "rotate_animation";

    public static NetworkInfoFragment noInternetFragment(final Context context) {
        return createInfoFragment(R.drawable.ic_no_internet,
                context.getString(R.string.warning_no_internet));
    }

    private static NetworkInfoFragment createInfoFragment(@DrawableRes int imageId, String message) {
        return createInfoFragment(imageId, message, NO_ANIMATION);
    }

    private static NetworkInfoFragment createInfoFragment(@DrawableRes int imageId, String message, String animation) {
        final NetworkInfoFragment result = new NetworkInfoFragment();
        final Bundle arguments = new Bundle();
        arguments.putInt(IMAGE_BUNDLE_ID, imageId);
        arguments.putString(MESSAGE_BUNDLE_ID, message);
        arguments.putString(ANIMATION_BUNDLE_ID, animation);
        result.setArguments(arguments);
        return result;
    }

    public static NetworkInfoFragment noWifiFragment(final Context context) {
        return createInfoFragment(R.drawable.ic_no_wifi,
                context.getString(R.string.warning_no_wifi));
    }

    public static NetworkInfoFragment loadingFragment(final Context context) {
        return createInfoFragment(
                R.drawable.ic_loading_circle,
                context.getString(R.string.info_loading),
                ROTATE_ANIMATION
        );
    }

    public static NetworkInfoFragment noResultFragment(final Context context) {
        return createInfoFragment(R.drawable.ic_search_unsuccessful,
                context.getString(R.string.warning_nothing_found));
    }

    @Bind(R.id.info_image)
    private ImageView showImage;
    @Bind(R.id.info_message)
    private TextView showMessage;
    @DrawableRes
    private int imageId;
    private String message;
    private String animation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        imageId = ofNullable(getArguments())
                .map(b -> b.getInt(IMAGE_BUNDLE_ID))
                .orElse(-1);

        message = ofNullable(getArguments())
                .map(b -> b.getString(MESSAGE_BUNDLE_ID))
                .orElse("");

        animation = ofNullable(getArguments())
                .map(b -> b.getString(ANIMATION_BUNDLE_ID))
                .orElse(NO_ANIMATION);

        return inflater.inflate(R.layout.fragment_wiki_sync_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AutoBind.bindAll(this, view);
        showImage();
        showMessage.setText(message);
    }

    private void showImage() {
        final Context context = getContext();
        if (context != null) {
            Drawable drawable = context.getDrawable(imageId);
            showImage.setImageDrawable(drawable);
            switch (animation) {
                case ROTATE_ANIMATION:
                    showImage.startAnimation(rotationAnimation());
                    break;
                case NO_ANIMATION:
                default:
                    break;
            }
        }
    }

    private static Animation rotationAnimation() {
        int fromDegrees = 0;
        int toDegrees = 360;
        float relativeXPivot = 0.5f;
        float relativeYPivot = 0.5f;

        final RotateAnimation rotate = new RotateAnimation(
                fromDegrees,
                toDegrees,
                Animation.RELATIVE_TO_SELF,
                relativeXPivot,
                Animation.RELATIVE_TO_SELF,
                relativeYPivot);
        rotate.setDuration(2000);
        rotate.setRepeatMode(RotateAnimation.RESTART);
        rotate.setRepeatCount(RotateAnimation.INFINITE);
        rotate.setInterpolator(new LinearInterpolator());
        return rotate;
    }

    @Override
    public void onPause() {
        super.onPause();
        showImage.clearAnimation();
    }
}
