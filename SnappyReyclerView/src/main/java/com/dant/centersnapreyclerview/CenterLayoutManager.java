package com.dant.centersnapreyclerview;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

/**
 * This extension to LinearLayoutManager allows the smoothScroll speed to be adjusted, additionally
 * it provides a custom implementation of the LinearSmoothScroller. The extension of
 * LinearSmoothScroller provides an additional snap preference, CENTER, this aligns the center of
 * the child's View with the center of the parent's View.
 */
public class CenterLayoutManager extends LinearLayoutManager {

    private SmoothScroller smoothScroller;

    private int anchor;

    public CenterLayoutManager(Context context) {
        super(context);

        smoothScroller = new SmoothScroller(context);
    }

    public void setAnchor(int anchor) {
        this.anchor = anchor;
    }

    public void setScrollSpeed(float scrollSpeed) {
        smoothScroller.setScrollSpeed(scrollSpeed);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView,
                                       RecyclerView.State state, final int position) {
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }

    private class SmoothScroller extends LinearSmoothScroller {

        private static final float MILLISECONDS_PER_INCH = 100f;

        /**
         * Use in calculating the scroll speed.
         * The time (in ms) it takes to scroll an inch.
         */
        private float milliSecondsPerInch = -1;

        public SmoothScroller(Context context) {
            super(context);
        }

        public void setScrollSpeed(float scrollSpeed) {
            this.milliSecondsPerInch = scrollSpeed;
        }

        /**
         * Controls the direction in which smoothScroll looks for a list item.
         * Use our custom LinearLayoutManager to calculate PointF.
         *
         * @param targetPosition
         *
         * @return PointF, a class holding two float coordinates
         */
        @Override
        public PointF computeScrollVectorForPosition(int targetPosition) {
            return CenterLayoutManager.this.computeScrollVectorForPosition(targetPosition);
        }

        @Override
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
            switch (anchor) {
                case SnappingRecyclerView.START:
                    return super.calculateDtToFit(viewStart, viewEnd, boxStart, boxEnd, SNAP_TO_START);
                case SnappingRecyclerView.END:
                    return super.calculateDtToFit(viewStart, viewEnd, boxStart, boxEnd, SNAP_TO_END);
                case SnappingRecyclerView.CENTER:
                default:
                    return ((boxStart + boxEnd) / 2) - ((viewStart + viewEnd) / 2);
            }
        }

        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
            return (milliSecondsPerInch > 0 ? milliSecondsPerInch : MILLISECONDS_PER_INCH) / displayMetrics.densityDpi;
        }
    }

}
