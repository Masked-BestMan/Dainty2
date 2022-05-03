package com.bm2cy.dainty2.component.overview.views;

import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Interpolator;


/* The transform state for a task view */
public class OverviewCardTransform {
    public int startDelay = 0;
    public int translationY = 0;
    public float translationZ = 0;
    public float scale = 1f;
    public float alpha = 1f;
    public boolean visible = false;
    public Rect rect = new Rect();
    float p = 0f;

    public OverviewCardTransform() {
        // Do nothing
    }

    public OverviewCardTransform(OverviewCardTransform o) {
        startDelay = o.startDelay;
        translationY = o.translationY;
        translationZ = o.translationZ;
        scale = o.scale;
        alpha = o.alpha;
        visible = o.visible;
        rect.set(o.rect);
        p = o.p;
    }

    /** Resets the current transform */
    public void reset() {
        startDelay = 0;
        translationY = 0;
        translationZ = 0;
        scale = 1f;
        alpha = 1f;
        visible = false;
        rect.setEmpty();
        p = 0f;
    }

    /** Convenience functions to compare against current property values */
    public boolean hasAlphaChangedFrom(float v) {
        return (Float.compare(alpha, v) != 0);
    }
    public boolean hasScaleChangedFrom(float v) {
        return (Float.compare(scale, v) != 0);
    }
    public boolean hasTranslationYChangedFrom(float v) {
        return (Float.compare(translationY, v) != 0);
    }
    public boolean hasTranslationZChangedFrom(float v) {
        return (Float.compare(translationZ, v) != 0);
    }

    /** Applies this transform to a view. */
    public void applyToTaskView(View v, int duration, Interpolator interp, boolean allowLayers,
            boolean allowShadows, ValueAnimator.AnimatorUpdateListener updateCallback) {
        // Check to see if any properties have changed, and update the task view
        if (duration > 0) {
            ViewPropertyAnimator anim = v.animate();
            boolean requiresLayers = false;

            // Animate to the final state
            if (hasTranslationYChangedFrom(v.getTranslationY())) {
                anim.translationY(translationY);
            }
            if (hasScaleChangedFrom(v.getScaleX())) {
                anim.scaleX(scale)
                    .scaleY(scale);
                requiresLayers = true;
            }
            if (hasAlphaChangedFrom(v.getAlpha())) {
                // Use layers if we animate alpha
                anim.alpha(alpha);
                requiresLayers = true;
            }
            if (requiresLayers && allowLayers) {
                anim.withLayer();
            }
            anim.setStartDelay(startDelay)
                    .setDuration(duration)
                    .setInterpolator(interp)
                    .start();
        } else {
            // Set the changed properties
            if (hasTranslationYChangedFrom(v.getTranslationY())) {
                v.setTranslationY(translationY);
            }
            if (hasScaleChangedFrom(v.getScaleX())) {
                v.setScaleX(scale);
                v.setScaleY(scale);
            }
            if (hasAlphaChangedFrom(v.getAlpha())) {
                v.setAlpha(alpha);
            }
        }
    }

    /** Reset the transform on a view. */
    public static void reset(View v) {
        v.setTranslationX(0f);
        v.setTranslationY(0f);
        v.setScaleX(1f);
        v.setScaleY(1f);
        v.setAlpha(1f);
    }

    @Override
    public String toString() {
        return "TaskViewTransform delay: " + startDelay + " y: " + translationY + " z: " + translationZ +
                " scale: " + scale + " alpha: " + alpha + " visible: " + visible + " rect: " + rect +
                " p: " + p;
    }
}
