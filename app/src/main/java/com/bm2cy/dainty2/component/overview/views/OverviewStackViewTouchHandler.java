package com.bm2cy.dainty2.component.overview.views;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;

import com.bm2cy.dainty2.component.overview.misc.OverviewConfiguration;

class OverviewStackViewTouchHandler implements SwipeHelper.Callback {
    static int INACTIVE_POINTER_ID = -1;

    static final int TaskStackOverscrollRange = 150;

    OverviewConfiguration mConfig;
    OverviewStackView mSv;
    OverviewStackViewScroller mScroller;
    VelocityTracker mVelocityTracker;

    boolean mIsScrolling;

    float mInitialP;
    float mLastP;
    float mTotalPMotion;
    int mInitialMotionX, mInitialMotionY;
    int mLastMotionX, mLastMotionY;
    int mActivePointerId = INACTIVE_POINTER_ID;
    OverviewCard mActiveTaskView = null;

    int mMinimumVelocity;
    int mMaximumVelocity;
    // The scroll touch slop is used to calculate when we start scrolling
    int mScrollTouchSlop;
    // The page touch slop is used to calculate when we start swiping
    float mPagingTouchSlop;

    SwipeHelper mSwipeHelper;
    boolean mInterceptedBySwipeHelper;

    public OverviewStackViewTouchHandler(Context context, OverviewStackView sv,
                                         OverviewConfiguration config, OverviewStackViewScroller scroller) {
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mScrollTouchSlop = configuration.getScaledTouchSlop();
        mPagingTouchSlop = configuration.getScaledPagingTouchSlop();
        mSv = sv;
        mScroller = scroller;
        mConfig = config;

        float densityScale = context.getResources().getDisplayMetrics().density;
        mSwipeHelper = new SwipeHelper(SwipeHelper.X, this, densityScale, mPagingTouchSlop, mConfig);
    }

    /** Velocity tracker helpers */
    void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }
    void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }
    void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    /** Returns the view at the specified coordinates */
    OverviewCard findViewAtPoint(int x, int y) {
        int childCount = mSv.getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            OverviewCard tv = (OverviewCard) mSv.getChildAt(i);
            if (tv.getVisibility() == View.VISIBLE) {
                if (mSv.isTransformedTouchPointInView(x, y, tv)) {
                    return tv;
                }
            }
        }
        return null;
    }

    /** Constructs a simulated motion event for the current stack scroll. */
    MotionEvent createMotionEventForStackScroll(MotionEvent ev) {
        MotionEvent pev = MotionEvent.obtainNoHistory(ev);
        pev.setLocation(0, mScroller.progressToScrollRange(mScroller.getStackScroll()));
        return pev;
    }

    /** Touch preprocessing for handling below */
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Return early if we have no children
        boolean hasChildren = (mSv.getChildCount() > 0);
        if (!hasChildren) {
            return false;
        }

        // Pass through to swipe helper if we are swiping
        mInterceptedBySwipeHelper = mSwipeHelper.onInterceptTouchEvent(ev);
        if (mInterceptedBySwipeHelper) {
            return true;
        }

        boolean wasScrolling = mScroller.isScrolling() ||
                (mScroller.mScrollAnimator != null && mScroller.mScrollAnimator.isRunning());
        int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                // Save the touch down info
                mInitialMotionX = mLastMotionX = (int) ev.getX();
                mInitialMotionY = mLastMotionY = (int) ev.getY();
                mInitialP = mLastP = mSv.mLayoutAlgorithm.screenYToCurveProgress(mLastMotionY);
                mActivePointerId = ev.getPointerId(0);
                mActiveTaskView = findViewAtPoint(mLastMotionX, mLastMotionY);
                // Stop the current scroll if it is still flinging
                mScroller.stopScroller();
                mScroller.stopBoundScrollAnimation();
                // Initialize the velocity tracker
                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(createMotionEventForStackScroll(ev));
                // Check if the scroller is finished yet
                mIsScrolling = mScroller.isScrolling();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mActivePointerId == INACTIVE_POINTER_ID) break;

                int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                int y = (int) ev.getY(activePointerIndex);
                int x = (int) ev.getX(activePointerIndex);
                if (Math.abs(y - mInitialMotionY) > mScrollTouchSlop) {
                    // Save the touch move info
                    mIsScrolling = true;
                    // Initialize the velocity tracker if necessary
                    initVelocityTrackerIfNotExists();
                    mVelocityTracker.addMovement(createMotionEventForStackScroll(ev));
                    // Disallow parents from intercepting touch events
                    final ViewParent parent = mSv.getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }

                mLastMotionX = x;
                mLastMotionY = y;
                mLastP = mSv.mLayoutAlgorithm.screenYToCurveProgress(mLastMotionY);
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                // Animate the scroll back if we've cancelled
                mScroller.animateBoundScroll();
                // Reset the drag state and the velocity tracker
                mIsScrolling = false;
                mActivePointerId = INACTIVE_POINTER_ID;
                mActiveTaskView = null;
                mTotalPMotion = 0;
                recycleVelocityTracker();
                break;
            }
        }

        return wasScrolling || mIsScrolling;
    }

    /** Handles touch events once we have intercepted them */
    public boolean onTouchEvent(MotionEvent ev) {

        // Short circuit if we have no children
        boolean hasChildren = (mSv.getChildCount() > 0);
        if (!hasChildren) {
            return false;
        }

        // Pass through to swipe helper if we are swiping
        if (mInterceptedBySwipeHelper && mSwipeHelper.onTouchEvent(ev)) {
            return true;
        }

        // Update the velocity tracker
        initVelocityTrackerIfNotExists();

        int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                // Save the touch down info
                mInitialMotionX = mLastMotionX = (int) ev.getX();
                mInitialMotionY = mLastMotionY = (int) ev.getY();
                mInitialP = mLastP = mSv.mLayoutAlgorithm.screenYToCurveProgress(mLastMotionY);
                mActivePointerId = ev.getPointerId(0);
                mActiveTaskView = findViewAtPoint(mLastMotionX, mLastMotionY);
                // Stop the current scroll if it is still flinging
                mScroller.stopScroller();
                mScroller.stopBoundScrollAnimation();
                // Initialize the velocity tracker
                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(createMotionEventForStackScroll(ev));
                // Disallow parents from intercepting touch events
                final ViewParent parent = mSv.getParent();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = ev.getActionIndex();
                mActivePointerId = ev.getPointerId(index);
                mLastMotionX = (int) ev.getX(index);
                mLastMotionY = (int) ev.getY(index);
                mLastP = mSv.mLayoutAlgorithm.screenYToCurveProgress(mLastMotionY);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mActivePointerId == INACTIVE_POINTER_ID) break;

                int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                int x = (int) ev.getX(activePointerIndex);
                int y = (int) ev.getY(activePointerIndex);
                int yTotal = Math.abs(y - mInitialMotionY);
                float curP = mSv.mLayoutAlgorithm.screenYToCurveProgress(y);
                float deltaP = mLastP - curP;
                if (!mIsScrolling) {
                    if (yTotal > mScrollTouchSlop) {
                        mIsScrolling = true;
                        // Initialize the velocity tracker
                        initOrResetVelocityTracker();
                        mVelocityTracker.addMovement(createMotionEventForStackScroll(ev));
                        // Disallow parents from intercepting touch events
                        final ViewParent parent = mSv.getParent();
                        if (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
                    }
                }
                if (mIsScrolling) {
                    float curStackScroll = mScroller.getStackScroll();
                    float overScrollAmount = mScroller.getScrollAmountOutOfBounds(curStackScroll + deltaP);
                    if (Float.compare(overScrollAmount, 0f) != 0) {
                        // Bound the overscroll to a fixed amount, and inversely scale the y-movement
                        // relative to how close we are to the max overscroll
                        float maxOverScroll = mConfig.taskStackOverscrollPct;
                        deltaP *= (1f - (Math.min(maxOverScroll, overScrollAmount)
                                / maxOverScroll));
                    }
                    mScroller.setStackScroll(curStackScroll + deltaP);
                    if (mScroller.isScrollOutOfBounds()) {
                        mVelocityTracker.clear();
                    } else {
                        mVelocityTracker.addMovement(createMotionEventForStackScroll(ev));
                    }
                }
                mLastMotionX = x;
                mLastMotionY = y;
                mLastP = mSv.mLayoutAlgorithm.screenYToCurveProgress(mLastMotionY);
                mTotalPMotion += Math.abs(deltaP);
                break;
            }
            case MotionEvent.ACTION_UP: {
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocity = (int) velocityTracker.getYVelocity(mActivePointerId);
                if (mIsScrolling && (Math.abs(velocity) > mMinimumVelocity)) {
                    int overscrollRange = (int) (Math.min(1f,
                            Math.abs((float) velocity / mMaximumVelocity)) *
                            TaskStackOverscrollRange);
                    // Fling scroll
                    mScroller.mScroller.fling(0, mScroller.progressToScrollRange(mScroller.getStackScroll()),
                            0, velocity,
                            0, 0,
                            mScroller.progressToScrollRange(mSv.mLayoutAlgorithm.mMinScrollP),
                            mScroller.progressToScrollRange(mSv.mLayoutAlgorithm.mMaxScrollP),
                            0, overscrollRange);
                    // Invalidate to kick off computeScroll
                    mSv.invalidate();
                } else if (mScroller.isScrollOutOfBounds()) {
                    // Animate the scroll back into bounds
                    mScroller.animateBoundScroll();
                }

                mActivePointerId = INACTIVE_POINTER_ID;
                mIsScrolling = false;
                mTotalPMotion = 0;
                recycleVelocityTracker();
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                int pointerIndex = ev.getActionIndex();
                int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // Select a new active pointer id and reset the motion state
                    final int newPointerIndex = (pointerIndex == 0) ? 1 : 0;
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                    mLastMotionX = (int) ev.getX(newPointerIndex);
                    mLastMotionY = (int) ev.getY(newPointerIndex);
                    mLastP = mSv.mLayoutAlgorithm.screenYToCurveProgress(mLastMotionY);
                    mVelocityTracker.clear();
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                if (mScroller.isScrollOutOfBounds()) {
                    // Animate the scroll back into bounds
                    mScroller.animateBoundScroll();
                }
                mActivePointerId = INACTIVE_POINTER_ID;
                mIsScrolling = false;
                mTotalPMotion = 0;
                recycleVelocityTracker();
                break;
            }
        }
        return true;
    }

    /**** SwipeHelper Implementation ****/

    @Override
    public View getChildAtPosition(MotionEvent ev) {
        return findViewAtPoint((int) ev.getX(), (int) ev.getY());
    }

    @Override
    public boolean canChildBeDismissed(View v) {
        return true;
    }

    @Override
    public void onBeginDrag(View v) {
        OverviewCard tv = (OverviewCard) v;
        // Disallow touch events from this task view
        tv.setTouchEnabled(false);
        // Disallow parents from intercepting touch events
        final ViewParent parent = mSv.getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
    }

    @Override
    public void onSwipeChanged(View v, float delta) {
        // Do nothing
    }

    @Override
    public void onChildDismissed(View v) {
        OverviewCard tv = (OverviewCard) v;
        // Re-enable touch events from this task view
        tv.setTouchEnabled(true);
        // Remove the task view from the stack
        mSv.onCardDismissed(tv);
    }

    @Override
    public void onSnapBackCompleted(View v) {
        OverviewCard tv = (OverviewCard) v;
        // Re-enable touch events from this task view
        tv.setTouchEnabled(true);
    }

    @Override
    public void onDragCancelled(View v) {
        // Do nothing
    }
}
