package de.moviemanager.ui.masterlist.swipe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.function.Consumer;

import de.moviemanager.R;
import de.moviemanager.util.DimensionUtils;

import static android.view.MotionEvent.ACTION_DOWN;
import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE;
import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;
import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;
import static de.moviemanager.ui.masterlist.elements.Type.CONTENT;
import static de.moviemanager.ui.masterlist.swipe.ButtonsState.GONE;
import static de.moviemanager.ui.masterlist.swipe.ButtonsState.LEFT_VISIBLE;
import static de.moviemanager.ui.masterlist.swipe.ButtonsState.RIGHT_VISIBLE;
import static java.lang.Math.max;
import static java.lang.Math.min;

enum ButtonsState {
    GONE,
    LEFT_VISIBLE,
    RIGHT_VISIBLE
}

public class SwipeController extends ItemTouchHelper.Callback {
 private final int buttonWidth;
    private final int iconSize;

    private final Drawable icon;
    private ButtonsState buttonShowedState;
    private final Context context;
    private boolean swipeBack;
    private Rect buttonInstance;
    private ViewHolder currentItemViewHolder = null;
    private final Consumer<ViewHolder> action;

    public SwipeController(final Context context,
                           @DrawableRes int drawable,
                           @NonNull final Consumer<ViewHolder> action) {
        this.context = context;
        buttonWidth = (int) DimensionUtils.dpToPixels(context, 72);
        iconSize = (int) DimensionUtils.dpToPixels(context, 16);

        this.action = action;
        icon = ContextCompat.getDrawable(context, drawable);
        buttonShowedState = GONE;
        swipeBack = false;
    }


    @Override
    public int getMovementFlags(@NonNull final RecyclerView recyclerView,
                                @NonNull final ViewHolder viewHolder) {
        return makeMovementFlags(0, LEFT | RIGHT);
    }

    @Override
    public boolean onMove(@NonNull final RecyclerView recyclerView,
                          @NonNull final ViewHolder viewHolder,
                          @NonNull final ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull ViewHolder viewHolder, int direction) {
        // not needed
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        if (swipeBack) {
            swipeBack = buttonShowedState != GONE;
            return 0;
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    @Override
    public void onChildDraw(@NonNull Canvas canvas,
                            @NonNull RecyclerView recyclerView,
                            @NonNull ViewHolder viewHolder,
                            float dX,
                            float dY,
                            int actionState,
                            boolean isCurrentlyActive) {
        if (viewHolder.getItemViewType() != CONTENT.ordinal())
            return;

        if (actionState == ACTION_STATE_SWIPE) {
            if (buttonShowedState != GONE) {
                if (buttonShowedState == LEFT_VISIBLE)
                    dX = max(dX, buttonWidth);
                if (buttonShowedState == RIGHT_VISIBLE)
                    dX = min(dX, -buttonWidth);

                super.onChildDraw(
                        canvas,
                        recyclerView,
                        viewHolder,
                        capHorizontalSwipeDistance(dX),
                        dY,
                        actionState,
                        isCurrentlyActive);
            } else {
                setTouchListener(canvas, recyclerView, viewHolder,
                        dX, dY,
                        actionState, isCurrentlyActive);
            }
        }

        if (buttonShowedState == GONE) {
            super.onChildDraw(canvas, recyclerView, viewHolder,
                    capHorizontalSwipeDistance(dX), dY,
                    actionState, isCurrentlyActive);
        }
        currentItemViewHolder = viewHolder;
    }

    private float capHorizontalSwipeDistance(float dX) {
        if (dX < 0)
            return max(dX, -buttonWidth);
        return min(dX, buttonWidth);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setTouchListener(final Canvas canvas,
                                  final RecyclerView recyclerView,
                                  final ViewHolder viewHolder,
                                  final float dX,
                                  final float dY,
                                  final int actionState,
                                  final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener((v, event) -> {
            swipeBack = event.getAction() == MotionEvent.ACTION_CANCEL
                    || event.getAction() == MotionEvent.ACTION_UP;
            if (swipeBack) {
                if (dX < -buttonWidth)
                    buttonShowedState = RIGHT_VISIBLE;
                else if (dX > buttonWidth)
                    buttonShowedState = LEFT_VISIBLE;

                if (buttonShowedState != GONE) {
                    setItemsClickable(recyclerView, false);
                    setTouchDownListener(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }
            return false;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setTouchDownListener(final Canvas canvas,
                                      final RecyclerView recyclerView,
                                      final ViewHolder viewHolder,
                                      final float dX,
                                      final float dY,
                                      final int actionState,
                                      final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener((v, event) -> {
            if (event.getAction() == ACTION_DOWN) {
                setTouchUpListener(
                        canvas,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                );
            }
            return false;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setTouchUpListener(final Canvas c,
                                    final RecyclerView recyclerView,
                                    final ViewHolder viewHolder,
                                    final float dX,
                                    final float dY,
                                    final int actionState,
                                    final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                recyclerView.performClick();
                SwipeController.super.onChildDraw(c,
                        recyclerView,
                        viewHolder,
                        0F,
                        dY,
                        actionState,
                        isCurrentlyActive);
                recyclerView.setOnTouchListener((v1, event1) -> false);
                setItemsClickable(recyclerView, true);
                swipeBack = false;


                if (action != null &&
                        buttonInstance != null &&
                        buttonInstance.contains((int) event.getX(), (int) event.getY())) {
                    action.accept(viewHolder);
                }

                buttonShowedState = GONE;
                currentItemViewHolder = null;
            }
            return false;
        });
    }

    private void setItemsClickable(final RecyclerView recyclerView,
                                   boolean isClickable) {
        for (int i = 0; i < recyclerView.getChildCount(); ++i) {
            recyclerView.getChildAt(i).setClickable(isClickable);
        }
    }

    private void drawButtons(final Canvas c,
                             final ViewHolder viewHolder) {
        int buttonWidthWithoutPadding = buttonWidth - 20;

        View itemView = viewHolder.itemView;
        Paint p = new Paint();

        Rect boundsLeft = new Rect(itemView.getLeft(),
                itemView.getTop(),
                itemView.getLeft() + buttonWidthWithoutPadding,
                itemView.getBottom()
        );

        Rect boundsRight = new Rect(itemView.getRight() - buttonWidthWithoutPadding,
                itemView.getTop(),
                itemView.getRight(),
                itemView.getBottom()
        );

        RectF button = new RectF(itemView.getLeft(),
                itemView.getTop(),
                itemView.getRight(),
                itemView.getBottom()
        );
        p.setColor(context.getColor(R.color.dark_red));
        c.drawRect(button, p);
        drawIcon(c, boundsLeft);
        drawIcon(c, boundsRight);

        buttonInstance = null;

        if (buttonShowedState == LEFT_VISIBLE) {
            buttonInstance = boundsLeft;
        } else if (buttonShowedState == RIGHT_VISIBLE) {
            buttonInstance = boundsRight;
        }
    }

    private void drawIcon(Canvas c, Rect bounds) {
        if (icon == null)
            return;

        int width = bounds.width();
        int height = bounds.height();
        int centerX = bounds.left + width / 2;
        int centerY = bounds.top + height / 2;
        icon.setBounds(centerX - iconSize,
                centerY - iconSize,
                centerX + iconSize,
                centerY + iconSize);
        icon.draw(c);
    }

    public void onDraw(Canvas c) {
        if (currentItemViewHolder != null) {
            drawButtons(c, currentItemViewHolder);
        }
    }
}