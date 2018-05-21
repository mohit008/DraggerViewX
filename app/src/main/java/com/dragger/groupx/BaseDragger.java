package com.dragger.groupx;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InflateException;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Author @ Mohit Soni on 01-05-2018 17:11.
 * This class works on resizing parameter while scolling
 */

public class BaseDragger extends ViewGroup {

    ViewDragHelper viewDragHelper;

    private View topView;
    private View bottomView;

    int view_id1, view_id2;

    private int mDragRange, mTop, mLeft;

    private float mInitialMotionX, mInitialMotionY, mDragOffset,
            scale_X = 0f, scale_Y = 0f, pivot_X = 0f, pivot_Y = 0f;

    boolean status = false, horizontal = false;

    public BaseDragger(Context context) {
        this(context, null);
    }

    public BaseDragger(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseDragger(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        viewDragHelper = ViewDragHelper.create(this, 1f,
                new BaseDragger.EnyWhere());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        try{
            topView = findViewById(view_id1);
            bottomView = findViewById(view_id2);
        }catch (InflateException e){
            e.printStackTrace();
        }

    }

    public void setID(int R1,int R2){
        this.view_id1 = R1;
        this.view_id2 = R2;
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

   /* *//*
     * revert status of horizontal view
     *//*
    public void maximize() {
        status = false;
        slideTo(0f);
    }

    *//*
     * revert status of horizontal view
     *//*
    public void minimize() {
        slideTo(1f);
    }*/

    /**
     * get slide to position
     *
     * @param slideOffset
     * @return
     */
    boolean slideTo(float slideOffset) {
        final int topBound = getPaddingTop();
        int y = (int) (topBound + slideOffset * mDragRange);

        if (viewDragHelper.smoothSlideViewTo(topView, 0, y)) {
            ViewCompat.postInvalidateOnAnimation(this);
            return true;
        }
        return false;
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
                final float dx = x - mInitialMotionX;
                final float dy = y - mInitialMotionY;
                final int slop = viewDragHelper.getTouchSlop();
                if (dx * dx + dy * dy < slop * slop && isHeaderViewUnder) {
                    if (mDragOffset == 0) {
                        slideTo(1f);
                    } else {
                        slideTo(0f);
                    }
                }
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

//    @Override
//    public void computeScroll() {
//        if (viewDragHelper.continueSettling(true)) {
//            ViewCompat.postInvalidateOnAnimation(this);
//        }
//    }

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

//            bottomView.setAlpha(1 - mDragOffset);

            Log.d("PositionChanged", "mTop : " + mTop + ", mLeft :" + mLeft
                    + ", scale_X :" + scale_X + ", scale_Y :" + scale_Y
                    + ", pivot_X :" + pivot_X + ", pivot_Y :" + pivot_Y);

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
                Log.d("ViewPositionHorizontal", "left : " + left + ", leftBound : " + leftBound);
                return newLeft;
            }
            Log.d("ViewPositionHorizontal", "newLeft : " + newLeft);
            return newLeft;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            // scroll vertically
            int original_height = (topView.getMeasuredHeight());
            float reduced_height = original_height * topView.getScaleY();
            final int topBound = (getHeight() - ((int) reduced_height)) - topView.getPaddingTop();

            final int newTop = Math.min(topBound, Math.max(top, 0));
            Log.d("ViewPositionVertical", "newTop : " + newTop + ", top : " + top + ", topBound : " + topBound);
            return newTop;
        }
        /*@Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            mTop = top;
            mLeft = left;

            mDragOffset = (float) top / mDragRange;
            pivot_X = pivot_Y = 0;

            // change status when reach bottom every time
            if (!status) {
                pivot_X = topView.getWidth();
                pivot_Y = topView.getHeight();
                scale_X = scale_Y = (1 - (mDragOffset / 2));
            }

            topView.setScaleX(scale_X);
            topView.setScaleY(scale_Y);

            topView.setPivotX(pivot_X);
            topView.setPivotY(pivot_Y);

            Log.d("PositionChanged", "mTop : " + mTop + ", mLeft :" + mLeft
                    + ", scale_X :" + scale_X + ", scale_Y :" + scale_Y
                    + ", pivot_X :" + pivot_X + ", pivot_Y :" + pivot_Y);

            // change status when reach bottom for first time
            if (mTop >= getHeight() - topView.getMeasuredHeight()) {
                status = true;
                if (!horizontal) {
                    // reach bottom at first, locate it at proper left
                    float reduced_width = topView.getMeasuredWidth() * topView.getScaleX();
                    // proper top
                    mLeft = (getWidth() - ((int) reduced_width)) - topView.getPaddingLeft();
                    mTop = mTop + topView.getMeasuredHeight();
                    horizontal = true;
                }
            }
            requestLayout();
        }*/
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
        int height = mTop + topView.getHeight();
        if (status) {
            height = getHeight();
        }
        // create layout
        topView.layout(mLeft, mTop,
                mLeft + topView.getMeasuredWidth(),
                mTop + topView.getMeasuredHeight());

        Log.d("onLayout : topView",
                "left : " + mLeft
                        + ", mTop : " + mTop
                        + ", right : " + mLeft + topView.getMeasuredWidth()
                        + ", bottom : " + mTop + topView.getMeasuredHeight() + ", b :" + b);

        bottomView.layout(0, height, r, mTop + b);
        Log.d("onLayout : bottomView",
                "left : " + 0
                        + ", top : " + height
                        + ", right : " + r
                        + ", bottom : " + mTop + b);

        Log.d("status", status + "");
        Log.d("onLayout", "---------------------------------------------------------------");
    }

    /**
     * add current view to window
     */
//    public void addToWindow() {
//        ;
//        final WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
//        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//                PixelFormat.TRANSLUCENT);
//
//
//        // position to current frame
//        params.x = mLeft;
//        params.y = mTop;
//
//        final RelativeLayout relativeLayout = new RelativeLayout(context);
//        relativeLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT));
//        relativeLayout.setOnTouchListener(new OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_MOVE: {
//                        status = true;
//                        // update param of window
//                        params.x = _x + ((int) event.getRawX() - x_);
//                        params.y = _y + ((int) event.getRawY() - y_);
//
//                        windowManager.updateViewLayout(relativeLayout, params);
//                        log("ACTION_MOVE", params.x + ":" + params.y);
//                        break;
//                    }
//                    case MotionEvent.ACTION_UP: {
//                        break;
//                    }
//                    case MotionEvent.ACTION_DOWN: {
//                        status = true;
//                        _y = params.y;
//                        y_ = (int) event.getRawY();
//                        log("ACTION_DOWN _ Y", _y + ":" + y_);
//
//                        _x = params.x;
//                        x_ = (int) event.getRawX();
//                        log("ACTION_DOWN _ X", _x + ":" + x_);
//                        break;
//                    }
//                    case MotionEvent.ACTION_CANCEL:
//                        status = false;
//                        break;
//                }
//                return status;
//            }
//        });
//
//        VideoView videoView = new VideoView(context);
//        videoView.setVideoURI(SwipeActivity.uri);
//        videoView.setLayoutParams(new LinearLayout.LayoutParams(topView.getWidth(), topView.getHeight()));
//        videoView.start();
//        videoView.seekTo(video_position);
//
//        relativeLayout.addView(videoView);
//
//        windowManager.addView(relativeLayout, params);
//    }

}
