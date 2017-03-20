package com.dant.centersnapreyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

/**
 * An extension of RecyclerView which provides the ability for an anchor to which views will snap
 * too. Once a scroll has been completed the RecyclerView will calculate which of it's Views
 * (ViewHolders) is closest to it's anchor and, using the smoothScroll method provided by
 * CenterLayoutManager, scroll said View to the correct position on screen.
 */
public class SnappingRecyclerView extends RecyclerView {

    @IntDef({HORIZONTAL, VERTICAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface OrientationMode {}

    public static final int VERTICAL = 0;
    public static final int HORIZONTAL = 1;

    @IntDef({CENTER, START, END})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnchorMode {}

    public static final int CENTER = 0;
    public static final int START = 1;
    public static final int END = 2;

    private static final int DEFAULT_FLING_THRESHOLD = 1000;

    public interface SnappingRecyclerViewListener {
        void onPositionChange(int position);
        void onScroll(int dx, int dy);
    }

    private WeakReference<SnappingRecyclerViewListener> listener;

    private int orientation;

    /**
     * The anchor to which a View (ViewHolder) should snap too, the START, CENTER or END
     */
    private int anchor;

    /**
     * The smooth scroll speed, in ms per inch, this is 100 by default in our custom smooth
     * scroller
     */
    private float scrollSpeed;

    /**
     * If the velocity of the user's fling is below a set threshold, finish fling and scroll to the
     * appropriate View
     */
    private int flingThreshold;

    private CenterLayoutManager layoutManager;

    public SnappingRecyclerView(Context context) {
        super(context);
        initialise(context, null);
    }

    public SnappingRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialise(context, attrs);
    }

    public SnappingRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialise(context, attrs);
    }

    private void initialise(Context context, @Nullable AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SnappingRecyclerView, 0, 0);
        try {
            orientation = a.getInt(R.styleable.SnappingRecyclerView_orientation, VERTICAL);
            anchor = a.getInt(R.styleable.SnappingRecyclerView_anchor, CENTER);
            scrollSpeed = a.getFloat(R.styleable.SnappingRecyclerView_scrollSpeed, -1);
            flingThreshold = a.getInt(R.styleable.SnappingRecyclerView_flingThreshold, DEFAULT_FLING_THRESHOLD);
        } finally {
            a.recycle();
        }

        layoutManager = new CenterLayoutManager(getContext());
        layoutManager.setOrientation(orientation == VERTICAL ? LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL);
        layoutManager.setAnchor(anchor);
        layoutManager.setScrollSpeed(scrollSpeed);
        setLayoutManager(layoutManager);
    }

    public SnappingRecyclerViewListener getListener() {
        return listener != null ? listener.get() : null;
    }

    public void setListener(SnappingRecyclerViewListener listener) {
        this.listener = new WeakReference<>(listener);
    }

    public void setOrientation(@OrientationMode int orientation) {
        if (this.orientation != orientation) {
            this.orientation = orientation;

            layoutManager.setOrientation(orientation == VERTICAL ? LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL);
            requestLayout();
        }
    }

    @OrientationMode
    public int getOrientation() {
        return orientation;
    }

    public void setAnchor(@AnchorMode int anchor) {
        if (this.anchor != anchor) {
            this.anchor = anchor;

            layoutManager.setAnchor(anchor);
            requestLayout();
        }
    }

    @AnchorMode
    public int getAnchor() {
        return anchor;
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);

        if(getListener() != null) {
            getListener().onScroll(dx, dy);
        }
    }

    @Override
    public void scrollToPosition(final int position) {
        super.scrollToPosition(position);

        // Use the smoothScroll provided by the CenterLayoutManager
        post(new Runnable() {
            @Override
            public void run() {
                smoothScrollToPosition(position);
            }
        });
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        if(Math.abs(orientation == VERTICAL ? velocityY : velocityX) < flingThreshold) {
            int centerViewPosition = calculateSnapViewPosition();
            smoothScrollToPosition(centerViewPosition);

            if(getListener() != null) {
                getListener().onPositionChange(centerViewPosition);
            }

            return true;
        } else {
            return super.fling(velocityX, velocityY);
        }
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);

        /* Once a scroll has been completed smooth scroll to the nearest View based on the anchor */
        if (state == SCROLL_STATE_IDLE) {
            int centerViewPosition = calculateSnapViewPosition();
            smoothScrollToPosition(centerViewPosition);

            if(getListener() != null) {
                getListener().onPositionChange(centerViewPosition);
            }
        }
    }

    /**
     * Provides the anchor of the parent, the RecyclerView, based on the provided orientation and
     * anchor mode
     *
     * @param orientation The orientation of the RecyclerView, VERTICAL or HORIZONTAL
     * @param anchor The RecyclerView's anchor mode, START, CENTER or END
     *
     * @return The anchor of the parent, the RecyclerView
     */
    private int getParentAnchor(@OrientationMode int orientation, @AnchorMode int anchor) {
        switch (anchor) {
            case START:
                return 0;
            case END:
                return orientation == VERTICAL ? getHeight() : getWidth();
            case CENTER:
            default:
                return (orientation == VERTICAL ? getHeight() : getWidth()) / 2;
        }
    }

    /**
     * Provides the anchor or the given view relative to the provided orientation and anchor.
     * This will be the View's start (top or left), center, or end (bottom or right).
     *
     * @param view
     * @param orientation The orientation of the RecyclerView, VERTICAL or HORIZONTAL
     * @param anchor The RecyclerView's anchor mode, START, CENTER or END
     *
     * @return The anchor of the given View relative to the provided orientation and anchor
     */
    private int getViewAnchor(View view, @OrientationMode int orientation, @AnchorMode int anchor) {
        switch (anchor) {
            case START:
                return orientation == VERTICAL ? view.getTop() : view.getLeft();
            case END:
                return orientation == VERTICAL ? view.getBottom() : view.getRight();
            case CENTER:
            default:
                return (orientation == VERTICAL ? view.getTop() + (view.getHeight() / 2) : view.getLeft() + (view.getWidth() / 2));
        }
    }

    /**
     * Calculates the distance between the RecyclerView's anchor, either the start, center or end,
     * and the View which is closest to the anchor.
     *
     * @return The distance between RecyclerView anchor and View closest to anchor
     */
    private int calculateSnapDistance() {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) getLayoutManager();

        int parentAnchor = getParentAnchor(orientation, anchor);

        int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
        int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

        View currentViewClosestToAnchor = linearLayoutManager.findViewByPosition(firstVisibleItemPosition);

        int currentViewClosestToAnchorDistance = parentAnchor - getViewAnchor(currentViewClosestToAnchor, orientation, anchor);

        for(int i = firstVisibleItemPosition + 1; i <= lastVisibleItemPosition; i++) {
            View view = linearLayoutManager.findViewByPosition(i);
            int distanceToAnchor = parentAnchor - getViewAnchor(view, orientation, anchor);

            if (Math.abs(distanceToAnchor) < Math.abs(currentViewClosestToAnchorDistance)) {
                currentViewClosestToAnchorDistance = distanceToAnchor;
            }
        }

        return currentViewClosestToAnchorDistance;
    }

    /**
     * Finds the position of the View which is closest to the RecyclerView's anchor, either the
     * RecyclerView's start, center or end
     *
     * @return The position of the View closest the to RecyclerView's anchor
     */
    private int calculateSnapViewPosition() {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) getLayoutManager();

        int parentAnchor = getParentAnchor(orientation, anchor);

        int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
        int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

        View currentViewClosestToAnchor = linearLayoutManager.findViewByPosition(firstVisibleItemPosition);

        int currentViewClosestToAnchorDistance = parentAnchor - getViewAnchor(currentViewClosestToAnchor, orientation, anchor);

        int currentViewClosestToAnchorPosition = firstVisibleItemPosition;

        for(int i = firstVisibleItemPosition + 1; i <= lastVisibleItemPosition; i++) {
            View view = linearLayoutManager.findViewByPosition(i);

            int distanceToCenter = parentAnchor - getViewAnchor(view, orientation, anchor);

            if (Math.abs(distanceToCenter) < Math.abs(currentViewClosestToAnchorDistance)) {
                currentViewClosestToAnchorPosition = i;
                currentViewClosestToAnchorDistance = distanceToCenter;
            }
        }

        return currentViewClosestToAnchorPosition;
    }
}
