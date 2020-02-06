package de.moviemanager.ui.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import de.moviemanager.R;
import de.moviemanager.util.AndroidUtils;
import de.moviemanager.util.autobind.AutoBind;
import de.moviemanager.util.autobind.Bind;

public class SearchResultBlock extends FrameLayout {
    private Context context;
    @Bind(R.id.result_name) private TextView resultName;
    @Bind(R.id.portrayables) private RecyclerView resultList;
    @Bind(R.id.continue_button) private Button continueButton;
    @Bind(R.id.continue_image) private ImageView continueImage;

    public SearchResultBlock(Context context) {
        super(context);
        init(context);
    }

    private void init(final Context context) {
        this.context = context;
        inflate(context, R.layout.view_search_result_block, this);

        bindViews();
    }

    private void bindViews() {
        AutoBind.bindAll(this);
        resultList.setLayoutManager(createLinearLayoutManager());
    }

    private LinearLayoutManager createLinearLayoutManager() {
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        return linearLayoutManager;
    }

    public void setName(@StringRes int nameId) {
        setName(context.getString(nameId));
    }

    private void setName(final String name) {
        resultName.setText(name);
    }

    public void setAdapter(final Adapter<ViewHolder> listAdapter) {
        resultList.setAdapter(listAdapter);
    }

    public void setContinueText(@StringRes int textId) {
        setContinueText(context.getString(textId));
    }

    private void setContinueText(final String text) {
        continueButton.setText(text);
    }

    public void setContinueImage(@DrawableRes int imageId) {
        setContinueImage(context.getDrawable(imageId));
    }

    private void setContinueImage(final Drawable image) {
        continueImage.setImageDrawable(image);
    }

    public void setContinueListener(final OnClickListener listener) {
        continueButton.setOnClickListener(listener);
        continueImage.setOnClickListener(listener);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        AndroidUtils.closeKeyboard((Activity) context);
        return super.onInterceptTouchEvent(ev);
    }
}
