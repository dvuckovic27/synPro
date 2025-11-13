package com.metalac.scanner.app.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ViewAnimationUtils {

    private static final int ANIMATION_DURATION = 300;
    private static final float ARROW_ROTATION_COLLAPSED = 0f;
    private static final float ARROW_ROTATION_EXPANDED = 180f;
    private static final String PROPERTY_ROTATION = "rotation";

    /**
     * Expands the given view by animating its height from 0 to its measured height.
     * <p>
     * The view's visibility is set to VISIBLE at the start of the animation. When the
     * animation completes, the optional {@code onEnd} callback is invoked.
     *
     * @param view  The view to expand.
     * @param onEnd A runnable to execute when the animation finishes (nullable).
     */
    public static void expand(final View view, Runnable onEnd) {
        view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = view.getMeasuredHeight();

        view.getLayoutParams().height = 0;
        view.setVisibility(View.VISIBLE);

        ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
        animator.addUpdateListener(animation -> {
            view.getLayoutParams().height = (int) animation.getAnimatedValue();
            view.requestLayout();
        });
        animator.setDuration(ANIMATION_DURATION);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onEnd != null) onEnd.run();
            }
        });
        animator.start();
    }

    /**
     * Collapses the given view by animating its height from its current height down to 0.
     * <p>
     * The view's visibility is set to GONE when the animation completes. When the
     * animation finishes, the optional {@code onEnd} callback is invoked.
     *
     * @param view  The view to collapse.
     * @param onEnd A runnable to execute when the animation finishes (nullable).
     */
    public static void collapse(final View view, Runnable onEnd) {
        final int initialHeight = view.getMeasuredHeight();

        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 0);
        animator.addUpdateListener(animation -> {
            view.getLayoutParams().height = (int) animation.getAnimatedValue();
            view.requestLayout();
            if ((int) animation.getAnimatedValue() == 0) {
                view.setVisibility(View.GONE);
            }
        });
        animator.setDuration(ANIMATION_DURATION);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onEnd != null) onEnd.run();
            }
        });
        animator.start();
    }

    /**
     * Animates the rotation of the arrow ImageView to indicate expanded or collapsed state.
     *
     * @param arrow    The ImageView representing the arrow to rotate.
     * @param expanded True if the arrow should rotate to expanded position; false for collapsed.
     */
    public static void rotateArrow(ImageView arrow, boolean expanded) {
        float fromRotation = expanded ? ARROW_ROTATION_COLLAPSED : ARROW_ROTATION_EXPANDED;
        float toRotation = expanded ? ARROW_ROTATION_EXPANDED : ARROW_ROTATION_COLLAPSED;

        ObjectAnimator animator = ObjectAnimator.ofFloat(arrow, PROPERTY_ROTATION, fromRotation, toRotation);
        animator.setDuration(ANIMATION_DURATION);
        animator.start();
    }
}
