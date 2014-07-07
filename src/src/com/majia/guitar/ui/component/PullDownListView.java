package com.majia.guitar.ui.component;


import com.majia.guitar.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Scroller;
import android.widget.TextView;

public class PullDownListView extends FrameLayout implements GestureDetector.OnGestureListener,
        Animation.AnimationListener {

    public static int MAX_PADDING = 90;
    public static final int STATE_INVALID = -1;
    public static final int STATE_LOADING = 0;
    public static final int STATE_SCROLL_TO_CLOSE = 1;
    public static final int STATE_SCROLL_TO_REFRESH = 2;
    private static final int CLOSEDELAY = 500;
    private static final int REFRESHDELAY = 300;
    private Animation mAnimationDown;
    private Animation mAnimationUp;
    private View mRefreshBar;
    private ImageView mArrow;
    private ProgressBar mProgressBar;
    private int mState = STATE_INVALID;
    private TextView mTitle;
    public ListView mListView;
    private GestureDetector mDetector;
    private FlingRunnable mFlinger;
    private int mPadding;
    private int mDestPading;
    private int mLastTop;
    private OnRefreshListener mListener;
    private float mLastMotionY;

    class FlingRunnable implements Runnable {

        private void startCommon() {
            removeCallbacks(this);
        }

        public void run() {
            boolean noFinish = mScroller.computeScrollOffset();
            int curY = mScroller.getCurrY();
            int deltaY = curY - mLastFlingY;
            if (noFinish) {
                fling(deltaY);
                mLastFlingY = curY;
                post(this);
            } else {
                removeCallbacks(this);
                if (mState == STATE_SCROLL_TO_CLOSE) {
                    mState = STATE_INVALID;
                }
            }
        }

        public void startUsingDistance(int distance, int duration) {
            if (distance == 0)
                distance--;
            startCommon();
            mLastFlingY = 0;
            mScroller.startScroll(0, 0, 0, distance, duration);
            post(this);
        }

        private int mLastFlingY;
        private Scroller mScroller;

        public FlingRunnable() {
            mScroller = new Scroller(getContext());
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
    }

    public interface OnRefreshListener {
        void onRefresh();

        void onLoadMore();
    }
    
    public interface IHookTimeListener {
        String getLastUpDateTime();
    }

    public PullDownListView(Context context, AttributeSet att) {
        super(context, att);
        mDetector = new GestureDetector(getContext(), this);
        mFlinger = new FlingRunnable();
        mDetector.setIsLongpressEnabled(false);
        MAX_PADDING =  context.getResources().getDimensionPixelOffset(R.dimen.pull_down_refresh_bar_padding);
        mPadding = -MAX_PADDING;
        mLastTop = -MAX_PADDING;
        mAnimationUp = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_up);
        mAnimationUp.setAnimationListener(this);

        mAnimationDown = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_down);
        mAnimationDown.setAnimationListener(this);
    }
    

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mListView = (ListView) findViewById(android.R.id.list);
        mRefreshBar = findViewById(R.id.refresh_bar);
        mArrow = (ImageView) mRefreshBar.findViewById(R.id.arrow);
        mProgressBar = (ProgressBar) mRefreshBar.findViewById(R.id.progress_bar);
        mTitle = (TextView) mRefreshBar.findViewById(android.R.id.title);

    }

    /**
     * deltaY > 0 is move UP.
     */
    private void fling(float deltaY) {
        if (deltaY > 0 && mRefreshBar.getTop() == -MAX_PADDING) {
            mPadding = -MAX_PADDING;
            return;
        }

        if (mRefreshBar.getTop() - deltaY < mDestPading) {
            deltaY = mRefreshBar.getTop() - mDestPading;
        }
        mRefreshBar.offsetTopAndBottom((int) -deltaY);
        mListView.offsetTopAndBottom((int) -deltaY);
        mPadding = mRefreshBar.getTop();
        if (mDestPading == 0 && mRefreshBar.getTop() == 0 && mState == STATE_SCROLL_TO_REFRESH) {
            onRefresh();
        }
        invalidate();
        updateView();
    }

    /**
     * deltaY > 0 is move UP.
     */
    private boolean performDrag(float deltaY) {
        final int top = mRefreshBar.getTop();
        boolean handle = false;
        if (deltaY > 0 && top == -MAX_PADDING) {
            mPadding = -MAX_PADDING;
            return false;
        }

        if (mState != STATE_LOADING || (mState == STATE_LOADING && deltaY > 0)) {
            mRefreshBar.offsetTopAndBottom((int) -deltaY);
            mListView.offsetTopAndBottom((int) -deltaY);
            mPadding = mRefreshBar.getTop();
        } else if (mState == STATE_LOADING && deltaY < 0 && top <= 0) {
            if (top > deltaY) {
                deltaY = top;
            }
            mRefreshBar.offsetTopAndBottom((int) -deltaY);
            mListView.offsetTopAndBottom((int) -deltaY);
            mPadding = mRefreshBar.getTop();
        }
        if (deltaY > 0 && mRefreshBar.getTop() <= -MAX_PADDING) {
            deltaY = -MAX_PADDING - mRefreshBar.getTop();
            mRefreshBar.offsetTopAndBottom((int) deltaY);
            mListView.offsetTopAndBottom((int) deltaY);
            mPadding = mRefreshBar.getTop();
        } else {
            handle = true;
        }
        updateView();
        invalidate();
        return handle;
    }

    private void updateView() {
        if (mState != STATE_LOADING) {
            if (mRefreshBar.getTop() < 0) {
                mArrow.setVisibility(View.VISIBLE);
               
                
                mProgressBar.setVisibility(View.GONE);
                mTitle.setText(R.string.pull_down_refresh);
                
                

                if (mLastTop >= 0 && mState != STATE_SCROLL_TO_CLOSE) {
                    mArrow.startAnimation(mAnimationUp);
                }

            } else if (mRefreshBar.getTop() > 0) {
                mTitle.setText(R.string.release_to_refresh);
                mProgressBar.setVisibility(View.GONE);
                mArrow.setVisibility(View.VISIBLE);

                if (mLastTop <= 0) {
                    mArrow.startAnimation(mAnimationDown);
                }
            }
        }
        mLastTop = mRefreshBar.getTop();
    }

    private boolean release() {
        if (mRefreshBar.getTop() > 0) {
            scrollToUpdate(false);
        } else {
            scrollToClose();
        }
        invalidate();
        return false;
    }

    private void scrollToClose() {
        mDestPading = -MAX_PADDING;
        mFlinger.startUsingDistance(MAX_PADDING, CLOSEDELAY);
    }

    public void scrollToUpdate(boolean load) {
        mState = STATE_SCROLL_TO_REFRESH;

        mDestPading = 0;
        if (load) {
            mFlinger.startUsingDistance(50, REFRESHDELAY);
            load = false;
        } else
            mFlinger.startUsingDistance(mRefreshBar.getTop(), REFRESHDELAY);
    }

    private void onRefresh() {
        mState = STATE_LOADING;
        mTitle.setText(R.string.is_loading);
        mArrow.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        if (mListener != null) {
            mListener.onRefresh();
        }
    }

    public void onRefreshComplete() {
        onRefreshComplete(null);
    }

    public void onRefreshComplete(String date) {
        mState = STATE_SCROLL_TO_CLOSE;
        mArrow.setImageResource(R.drawable.arrow_down);
        scrollToClose();
    }

    public void onLoadMore() {
        mState = STATE_LOADING;
        if (mListener != null) {
            mListener.onLoadMore();
        }
    }

    public void onLoadMoreComplete(String date) {
        mState = STATE_INVALID;
    }

    public void onLoadMoreComplete() {
        onLoadMoreComplete(null);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        final float y = ev.getY();
        boolean handled = mDetector.onTouchEvent(ev);
        switch (action) {
        case MotionEvent.ACTION_DOWN: {
            super.dispatchTouchEvent(ev);
            break;
        }
        case MotionEvent.ACTION_MOVE: {
            float dy = mLastMotionY - y;
            mLastMotionY = y;

            if (!handled && mRefreshBar.getTop() == -MAX_PADDING) {
                return super.dispatchTouchEvent(ev);
            } else if (handled && mListView.getTop() > 0 && dy < 0) {
                ev.setAction(MotionEvent.ACTION_CANCEL);
                super.dispatchTouchEvent(ev);
            }
            break;
        }
        case MotionEvent.ACTION_UP: {
            boolean fin = y >= mListView.getTop() && y <= mListView.getBottom();
            if (!handled && mRefreshBar.getTop() == -MAX_PADDING && fin || mState == STATE_LOADING) {
                super.dispatchTouchEvent(ev);
            } else {
                handled = release();
            }
            break;
        }
        case MotionEvent.ACTION_CANCEL: {
            handled = release();
            super.dispatchTouchEvent(ev);
            break;
        }
        }

        return true;
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        int top = mRefreshBar.getTop();
        if (top < 0)
            mArrow.setImageResource(R.drawable.arrow_down);
        else if (top > 0)
            mArrow.setImageResource(R.drawable.arrow_up);
        else {
            if (top < mLastTop) {
                mArrow.setImageResource(R.drawable.arrow_down);
            } else {
                mArrow.setImageResource(R.drawable.arrow_up);
            }
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = right - left;
        final int height = bottom - top;
        mRefreshBar.layout(0, mPadding, width, mPadding + MAX_PADDING);

        mListView.layout(0, mPadding + MAX_PADDING, width, height + mPadding + MAX_PADDING);
    }

    @Override
    public boolean onScroll(MotionEvent curdown, MotionEvent cur, float deltaX, float deltaY) {
        boolean handled = false;
        View first = mListView.getChildAt(0);
        boolean flag = mListView.getCount() == 0
                || (first != null && first.getTop() == 0 && mListView.getFirstVisiblePosition() == 0);
        if (deltaY < 0 && flag || mRefreshBar.getTop() > -MAX_PADDING) {
            handled = performDrag(deltaY);
        }
        return handled;
    }

    // private float getSale(float deltaY) {
    // return deltaY * (deltaY < 0 ? (1.0f - mOffset / MAX_OFFSET) : 0.8f) *
    // 0.9f;
    // }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent motionevent, MotionEvent e, float f, float f1) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent ev) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent ev) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent ev) {
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mListener = listener;
    }
    

}
