package com.dragger.groupx;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InflateException;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.io.IOException;

/**
 * Author @ Mohit Soni on 10-05-2018 05:32 PM.
 */

public class SwipeDragger extends ViewGroup {
    ViewDragHelper viewDragHelper;
    Context context;

    private View topView;
    private View bottomView;

    int view_id1, view_id2, layout_id;

    private int mDragRange, mTop, mLeft, horizontal_alpha;
    private static int width, heigth;

    private float mInitialMotionX, mInitialMotionY, mDragOffset,
            scale_X = 0f, scale_Y = 0f, pivot_X = 0f, pivot_Y = 0f;

    boolean status = false, horizontal = false, vertical = true;

    public SwipeDragger(Context context) {
        this(context, null);
    }

    public SwipeDragger(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeDragger(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        width = metrics.widthPixels;
        heigth = metrics.heightPixels;
        viewDragHelper = ViewDragHelper.create(this, 1f,
                new SwipeDragger.EnyWhere());
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

    /**
     * revert status of horizontal view
     */
    public void maximize() {
        status = false;
        vertical = true;
        horizontal = false;
        new SwipeTop().execute();
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
                break;
            }
            case MotionEvent.ACTION_UP: {
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
            int x = getWidth();
            LayoutParams dragParam = (LayoutParams) topView.getLayoutParams();
            float h = scale_X * x;

            // change status when reach bottom every time
            if (!status) {
                pivot_X = getWidth() - topView.getWidth();
                pivot_Y = getHeight() - topView.getHeight();
                scale_X = scale_Y = (1 - (mDragOffset / 2));

                mLeft = x - (int) h;
            }
            if (scale_X == 1.0f && mTop == 0) {
                h = width;
                mLeft = 0;
            }
            // scale and position
            dragParam.width = (int) h;
            topView.setLayoutParams(dragParam);

            topView.setPivotX(pivot_X);
            topView.setPivotY(pivot_Y);

//            float xf = horizontal_alpha - mLeft + 1;
//            float y = horizontal_alpha + 1;
//            float j = xf / y;
//
//            topView.setAlpha(1 - j);

           /* log("PositionChanged", "mTop : " + mTop + ", mLeft :" + mLeft
                    + ", scale_X :" + scale_X + ", scale_Y :" + scale_Y
                    + ", pivot_X :" + pivot_X + ", pivot_Y :" + pivot_Y);*/

            // change status when reach bottom for first time
            if (mTop >= getHeight() - topView.getMeasuredHeight()) {
                status = true;
                if (!horizontal) {
                    // reach bottom at first, locate it at proper top
                    mTop = getHeight() - topView.getMeasuredHeight();
                    horizontal = true;
                    vertical = false;
                }
            }
            pivot_X = pivot_Y = 0;
            requestLayout();
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            int newLeft = mLeft;
            // at bottom scroll horizontal
            if (status) {
                vertical = false;
                final int leftBound = (getWidth() - ((int) topView.getWidth())) - topView.getPaddingLeft();
                newLeft = Math.min(leftBound, (Math.max(left, 0)));
                horizontal_alpha = leftBound;

//                ViewGroup viewGroup = (ViewGroup) topView;
//                viewGroup.removeAllViews();

                if (newLeft == 0) {
                    ((Activity) context).onBackPressed();
                }
                /*log("ViewPositionHorizontal", "newLeft : " + newLeft + ", left : " + left + ", leftBound : " + leftBound);*/
            }
            return newLeft;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            int newTop = mTop;
            // scroll vertically
            if (vertical) {
                int original_height = (topView.getMeasuredHeight());
                float reduced_height = original_height * topView.getScaleY();
                final int topBound = (getHeight() - ((int) reduced_height)) - topView.getPaddingTop();
                newTop = Math.min(topBound, Math.max(top, 0));
                /*log("ViewPositionVertical", "newTop : " + newTop + "top : " + top + ", topBound : " + topBound);*/
            }
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
        // range to drag
        mDragRange = getHeight() - topView.getHeight();
        int height = topView.getHeight() - mTop;
        // wait till top
        if (height < 0 || status) {
            height = 0;
        }
        // create layout
        topView.layout(mLeft, mTop,
                mLeft + topView.getMeasuredWidth(),
                mTop + topView.getMeasuredHeight());
        /*log("onLayout : topView",
                "left : " + mLeft
                        + ", top : " + mTop
                        + ", right : " + mLeft + topView.getMeasuredWidth()
                        + ", bottom : " + mTop + topView.getMeasuredHeight() + ", b :" + b);*/

        bottomView.layout(0, height, r, mTop + b);
        /*log("onLayout : bottomView",
                "left : " + 0
                        + ", top : " + height
                        + ", right : " + r
                        + ", bottom : " + mTop + b);

        log("status", status + "");
        log("onLayout", "------------------------------------------------------------");*/
    }

    public class SwipeTop extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            int width = getWidth() - 100;
            int height = getHeight() - 100;
            // input swipe with (ms)
            String cmd = "input swipe " + width + " " + height + " " + width + " 50";
            try {
                Runtime.getRuntime().exec(cmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void log(String tag, String msg) {
        Log.d(tag, msg);
    }
}
