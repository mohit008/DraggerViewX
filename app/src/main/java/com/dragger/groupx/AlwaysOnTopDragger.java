package com.dragger.groupx;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InflateException;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.dragger.service.OnTopService;
import com.dragger.util.WindowStat;

/**
 * Author @ Mohit Soni on 10-05-2018 06:19 PM.
 */

public class AlwaysOnTopDragger extends ViewGroup {
    ViewDragHelper viewDragHelper;

    private View topView;
    private View bottomView;
    Context context;
    WindowStat windowStat;

    int view_id1, view_id2, layout_id;

    private int mDragRange, mTop, mLeft;

    private float mInitialMotionX, mInitialMotionY, mDragOffset,
            scale_X = 0f, scale_Y = 0f, pivot_X = 0f, pivot_Y = 0f;

    boolean status = false, horizontal = false, onTop = false, touch = false;

    public AlwaysOnTopDragger(Context context) {
        this(context, null);
    }

    public AlwaysOnTopDragger(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlwaysOnTopDragger(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        viewDragHelper = ViewDragHelper.create(this, 1f,
                new AlwaysOnTopDragger.EnyWhere());
    }

    public void setWindow(WindowStat windowStat) {
        this.windowStat = windowStat;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        try {
            topView = findViewById(view_id1);
            bottomView = findViewById(view_id2);
        } catch (InflateException e) {
            e.printStackTrace();
        }
    }

    public void setID(int topView, int bottom, int layout_id) {
        this.view_id1 = topView;
        this.view_id2 = bottom;
        this.layout_id = layout_id;
        onFinishInflate();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        if ((action != MotionEvent.ACTION_DOWN)) {
            viewDragHelper.cancel();
            return super.onInterceptTouchEvent(event);
        }
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            viewDragHelper.cancel();
            return false;
        }

        final float x = event.getX();
        final float y = event.getY();
        boolean interceptTap = false;

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mInitialMotionX = x;
                mInitialMotionY = y;
                interceptTap = viewDragHelper.isViewUnder(topView, (int) x, (int) y);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final float adx = Math.abs(x - mInitialMotionX);
                final float ady = Math.abs(y - mInitialMotionY);
                final int slop = viewDragHelper.getTouchSlop();
                if (ady > slop && adx > ady) {
                    viewDragHelper.cancel();
                    return false;
                }
            }
        }
        return viewDragHelper.shouldInterceptTouchEvent(event) || interceptTap;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewDragHelper.processTouchEvent(event);

        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();

        boolean isHeaderViewUnder = viewDragHelper.isViewUnder(topView, (int) x, (int) y);
        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                mInitialMotionX = x;
                mInitialMotionY = y;

                windowStat.setDimen((int) event.getRawX(), (int) event.getRawY(), mLeft, mTop, MotionEvent.ACTION_DOWN);

                if (status) {
                    startTouch();
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                stopTouch();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                windowStat.setDimen((int) event.getRawX(), (int) event.getRawY(), mLeft, mTop, MotionEvent.ACTION_MOVE);
                stopTouch();
                break;
            }
        }
        return isHeaderViewUnder && isViewHit(topView, (int) x, (int) y) || isViewHit(bottomView, (int) x, (int) y);
    }

    /**
     * height stat
     *
     * @param view
     * @param x
     * @param y
     * @return
     */
    public boolean isViewHit(View view, int x, int y) {
        if (view != null) {
            int[] viewLocation = new int[2];
            view.getLocationOnScreen(viewLocation);
            int[] parentLocation = new int[2];
            getLocationOnScreen(parentLocation);
            int screenX = parentLocation[0] + x;
            int screenY = parentLocation[1] + y;
            return screenX >= viewLocation[0] && screenX < viewLocation[0] + view.getWidth() &&
                    screenY >= viewLocation[1] && screenY < viewLocation[1] + view.getHeight();
        }
        return false;
    }

    /**
     * class to scroll call back and re dimenssion
     */
    private class EnyWhere extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == topView;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            int top = getPaddingTop();
            if (yvel > 0 || (yvel == 0 && mDragOffset > 0.5f)) {
                top += mDragRange;
            }
            viewDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top);
            invalidate();
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return mDragRange;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            mTop = top;
            mLeft = left;

            mDragOffset = (float) top / mDragRange;
            LayoutParams dragParam = (LayoutParams) topView.getLayoutParams();
            float h = scale_X * getWidth();
            dragParam.width = (int) h;

            // change status when reach bottom every time
            if (!status) {
                pivot_X = getWidth() - topView.getWidth();
                pivot_Y = getHeight() - topView.getHeight();
                scale_X = scale_Y = (1 - (mDragOffset / 2));

                mLeft = getWidth() - (int) h;
            }
            // scale and position
            topView.setLayoutParams(dragParam);

            topView.setPivotX(pivot_X);
            topView.setPivotY(pivot_Y);

            /*log("PositionChanged", "mTop : " + mTop + ", mLeft :" + mLeft
                    + ", scale_X :" + scale_X + ", scale_Y :" + scale_Y
                    + ", pivot_X :" + pivot_X + ", pivot_Y :" + pivot_Y);*/

            // change status when reach bottom for first time
            if (mTop >= getHeight() - topView.getMeasuredHeight()) {
                status = true;
                if (!horizontal) {
                    // reach bottom at first, locate it at proper top
                    mTop = getHeight() - topView.getMeasuredHeight();
                    horizontal = true;
                }
            }
            pivot_X = pivot_Y = 0;
            requestLayout();
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            int newLeft = super.clampViewPositionHorizontal(child, left, dx);
            // after bottom scroll horizontal
            if (status) {
                final int leftBound = (getWidth() - ((int) topView.getWidth())) - topView.getPaddingLeft();
                newLeft = Math.min(leftBound, (Math.max(left, 0)));
                /*Log.d("ViewPositionHorizontal", "newLeft : " + newLeft + ", left : " + left + ", leftBound : " + leftBound);*/
            }
            return newLeft;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            // scroll vertically
            int original_height = (topView.getMeasuredHeight());
            float reduced_height = original_height * topView.getScaleY();
            final int topBound = (getHeight() - ((int) reduced_height)) - topView.getPaddingTop();

            final int newTop = Math.min(topBound, Math.max(top, 0));
            /*log("ViewPositionVertical", "newTop : " + newTop + ", top : " + top + ", topBound : " + topBound);*/
            return newTop;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
                resolveSizeAndState(maxHeight, heightMeasureSpec, 0));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (!onTop && topView != null && bottomView != null) {
            mDragRange = getHeight() - topView.getHeight();
            int height = mTop + topView.getHeight();
            if (status) {
                height = getHeight();
                onTop = true;
                windowStat.setDimen(mLeft, mTop, topView.getWidth(), topView.getHeight(), MotionEvent.ACTION_MASK);
            }
            topView.layout(mLeft, mTop,
                    mLeft + topView.getMeasuredWidth(),
                    mTop + topView.getMeasuredHeight());
            /*log("onLayout : topView",
                    "left : " + mLeft
                            + ", mTop : " + mTop
                            + ", right : " + mLeft + topView.getMeasuredWidth()
                            + ", bottom : " + mTop + topView.getMeasuredHeight() + ", b :" + b);*/

            bottomView.layout(0, height, r, mTop + b);
            /*log("onLayout : bottomView",
                    "left : " + 0
                            + ", top : " + height
                            + ", right : " + r
                            + ", bottom : " + mTop + b);*/

            /*log("status", status + "");
            log("onLayout", "---------------------------------------------------------------");*/
        }
    }

    public void log(String tag, String msg) {
        Log.d(tag, msg);
    }

    /**
     * start touch time
     */
    public void startTouch() {
        touch = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                int thread_ = 0;
                while (touch) {
                    try {
                        new Thread().sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    log("startTouch", thread_ + "");
                    thread_ = thread_ + 1;
                    // wait for sec and close window
                    if (thread_ == 4) {
                        context.stopService(new Intent(context, OnTopService.class));
                    }
                }
            }
        }).start();
    }

    /**
     * stop touch time
     */
    public void stopTouch() {
        touch = false;
    }
}
