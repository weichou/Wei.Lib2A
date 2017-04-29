/*
 * Copyright (C) 2015-present, Wei Chou (weichou2010@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hobby.wei.c.widget;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * 通用触摸事件追踪器。
 * 
 * <pre>
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final Result result = mSEvTracker.preDispatchTouchEvent(ev);
        return result.performSuper ? mSEvTracker.postDispatchTouchEvent(ev, super.dispatchTouchEvent(result.superEvent)) : result.consume();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final Result result = mSEvTracker.preOnInterceptTouchEvent(ev);
        return result.performSuper ? mSEvTracker.postOnInterceptTouchEvent(ev, super.onInterceptTouchEvent(result.superEvent)) : result.consume();
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent ev) {
        final Result result = mSEvTracker.preOnTouchEvent(ev);
        return result.performSuper ? mSEvTracker.postOnTouchEvent(ev, super.onTouchEvent(result.superEvent)) : result.consume();
    }
 *</pre>
 * 
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class SlideEventTracker {
    public static final int ANIMATED_SCROLL_GAP             = 250;

    private final Result mResult = new Result();
    private final View mView;

    private final int mTouchSlop;
    private final int mMinimumFlingVelocity;
    private final int mMaximumFlingVelocity;
    private final float mStickyFactorX, mStickyFactorY;
    private final int mFlingSensitivityX, mFlingSensitivityY;
    private final boolean mSingleDirection;

    private final Scroller mScrollerX, mScrollerY;
    private final OnSlideListener mOnSlideListener;
    private final OnScrollListener mOnScrollListener;
    /**-1:忽略x方向，1:忽略Y方向，0:都不忽略**/
    private final int mIgnoreXorY;

    private long mTimeDown;
    private long mTimeLastScrollX, mTimeLastScrollY;
    private float mIntentionRadio;
    private boolean mDisallowInterceptRequest = false, mChildTapTimeout = false, mSuperCancelled = false;
    private VelocityTracker mVelocityTracker;

    private int mScrollByX, mScrollByY;
    private int mDownMotionX, mDownMotionY;
    private boolean mHaveSlideIntentionX = false, mHaveSlideIntentionY = false;
    private boolean mPrevAnimInterruptedX = false, mPrevAnimInterruptedY = false;
    private boolean mSlideEventEndDoneX = false, mSlideEventEndDoneY = false;
    private boolean mTouching;

    /**
     * @param view                  要处理事件的view
     * @param angle                 滑动意向判定角度，基于X轴的夹角，单位：(度°)
     * @param stickyFactorX
     * @param stickyFactorY
     * @param flingSensitivityX
     * @param flingSensitivityY
     * @param interpolatorX
     * @param interpolatorY
     * @param onSlide
     * @param onScroll
     * @param singleDirection       单向滑动模式
     */
    public SlideEventTracker(View view, float angle, float stickyFactorX, float stickyFactorY,
            int flingSensitivityX, int flingSensitivityY,
            Interpolator interpolatorX, Interpolator interpolatorY,
            OnSlideListener onSlide, OnScrollListener onScroll, boolean singleDirection) {
        mView = view;
        mIntentionRadio = (float)Math.tan(Math.toRadians(angle));
        mStickyFactorX = stickyFactorX;
        mStickyFactorY = stickyFactorY;
        mFlingSensitivityX = flingSensitivityX;
        mFlingSensitivityY = flingSensitivityY;
        mOnSlideListener = onSlide;
        mOnScrollListener = onScroll;

        mScrollerX = new Scroller(mView.getContext(), interpolatorX);
        mScrollerY = new Scroller(mView.getContext(), interpolatorY);

        mSingleDirection = singleDirection;
        mIgnoreXorY = 0;

        ViewConfiguration configuration = ViewConfiguration.get(mView.getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    public SlideEventTracker(View view, float angle, float stickyFactor,
            int flingSensitivity, Interpolator interpolator,
            OnSlideListener onSlide, OnScrollListener onScroll, boolean directionTrueXFalseY) {
        mView = view;
        mIntentionRadio = (float)Math.tan(Math.toRadians(angle));
        mOnSlideListener = onSlide;
        mOnScrollListener = onScroll;
        if (directionTrueXFalseY) {
            mStickyFactorX = stickyFactor;
            mStickyFactorY = 0;
            mFlingSensitivityX = flingSensitivity;
            mFlingSensitivityY = 0;
            mScrollerX = new Scroller(mView.getContext(), interpolator);
            mScrollerY = new Scroller(mView.getContext(), interpolator);    //若为null，很多判断会比较麻烦
        } else {
            mStickyFactorX = 0;
            mStickyFactorY = stickyFactor;
            mFlingSensitivityX = 0;
            mFlingSensitivityY = flingSensitivity;
            mScrollerX = new Scroller(mView.getContext(), interpolator);    //若为null，很多判断会比较麻烦
            mScrollerY = new Scroller(mView.getContext(), interpolator);
        }
        mSingleDirection = true;
        mIgnoreXorY = directionTrueXFalseY ? 1 : -1;

        ViewConfiguration configuration = ViewConfiguration.get(mView.getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    public Result preDispatchTouchEvent(MotionEvent ev) {
        if (!mSlideEventEndDoneX || !mSlideEventEndDoneY) {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(ev);
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {    //执行要放在super的前面
            mTouching = true;
            /* 放在最靠前的地方，拉远计时位置时间差，以确保在子View判定为TapTimeout的时候，本View一定判定为TapTimeout，
             * 防止在本View决定拦截的时候，实际子View已经处理了点击事件。*/
            mTimeDown = SystemClock.elapsedRealtime();
            mPrevAnimInterruptedX = !mScrollerX.isFinished();
            if (mPrevAnimInterruptedX) mScrollerX.abortAnimation();
            mPrevAnimInterruptedY = !mScrollerY.isFinished();
            if (mPrevAnimInterruptedY) mScrollerY.abortAnimation();
            //注释掉，不保持滑动意向。以确保任何时刻的滑动意图（包括在横向动画还未结束时的纵向意图）都能够被正确处理。
            //if (!mPrevAnimInterruptedX) { //被中断就继续保持滑动意向
            mHaveSlideIntentionX = false;
            //}
            //if (!mPrevAnimInterruptedY) {
            mHaveSlideIntentionY = false;
            //}
            mChildTapTimeout = false;
            mSlideEventEndDoneX = isXIgnored();
            mSlideEventEndDoneY = isYIgnored();
            mDisallowInterceptRequest = false;
            mScrollByX = 0;
            mScrollByY = 0;
            /* 不用ev.getX()，因为有些View需要被拖动，随着其移动，ev.getX()的值的相对位置会发生变化，
             * 导致移动的绝对位移不准确，而本组件仅向外反馈scrollBy这个绝对位移。
             */
            mDownMotionX = (int)ev.getRawX();
            mDownMotionY = (int)ev.getRawY();
            mSuperCancelled = false;
            deliverOnSlideEventBegin(ev);
        }
        return mResult.set(true, false, false, false, ev);
    }

    public boolean postDispatchTouchEvent(MotionEvent ev, boolean superConsume) {
        boolean consume = superConsume;
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            consume = true;
            break;
        case MotionEvent.ACTION_MOVE:
            mScrollByX = (int)ev.getRawX() - mDownMotionX;
            mScrollByY = (int)ev.getRawY() - mDownMotionY;
            //为true，必然是在拦截之后
            if (mHaveSlideIntentionX || mHaveSlideIntentionY) {
                requestDisallowIntercept();
                if (mSingleDirection) {
                    if (mHaveSlideIntentionX) {
                        deliverOnSlideEventMoveInnerX();
                        deliverOnSlideEventEndInnerY(ev);
                    } else if (mHaveSlideIntentionY) {
                        deliverOnSlideEventMoveInnerY();
                        deliverOnSlideEventEndInnerX(ev);
                    }
                } else {
                    deliverOnSlideEventMoveInnerX();
                    deliverOnSlideEventMoveInnerY();
                }
                consume = true;
            } else {    //没有任何滑动意向
                if (mSingleDirection) {
                    if (mPrevAnimInterruptedX) {    //被中断的，那么就总是跟随手指
                        requestDisallowIntercept();
                        deliverOnSlideEventMoveInnerX();
                        consume = true;
                    } else {    //不是被中断的
                        /* 超时了，没机会拦截了；
                         * 又没有滑动意向，没机会再有了，因为：ACTION_MOVE只要不拦截，
                         * 永远到不了onTouchEvent()，即使子View不消化，而滑动意向是在onTouchEvent()里被确定的。
                         * 那么，子View消化并响应了事件，这里就滚回去吧！
                         */
                        //注意：如果不走拦截（如没有子View），则mChildTapTimeout总为false，则事件必定会到达onTouchEvent()，那么无论是否有滑动意向，本逻辑处理方法一致
                        if (mChildTapTimeout) {
                            deliverOnSlideEventEndInnerX(ev);
                        } else {    //还没超时，还有机会再走走看
                            deliverOnSlideEventMoveInnerX();
                            consume = true;
                        }
                    }
                    if (mPrevAnimInterruptedY) {
                        requestDisallowIntercept();
                        deliverOnSlideEventMoveInnerY();
                        consume = true;
                    } else {
                        if (mChildTapTimeout) {
                            deliverOnSlideEventEndInnerY(ev);
                        } else {
                            deliverOnSlideEventMoveInnerY();
                            consume = true;
                        }
                    }
                } else {
                    if (mPrevAnimInterruptedX || mPrevAnimInterruptedY) {
                        requestDisallowIntercept();
                        deliverOnSlideEventMoveInnerX();
                        deliverOnSlideEventMoveInnerY();
                        consume = true;
                    } else {
                        if (mChildTapTimeout) {
                            deliverOnSlideEventEndInnerX(ev);
                            deliverOnSlideEventEndInnerY(ev);
                        } else {
                            deliverOnSlideEventMoveInnerX();
                            deliverOnSlideEventMoveInnerY();
                            consume = true;
                        }
                    }
                }
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            deliverOnSlideEventEndInnerX(ev);
            deliverOnSlideEventEndInnerY(ev);
            mTouching = false;
            break;
        }
        return consume;
    }

    /**
     * ACTION_DOWN只要拦截或子View不消化，就可以到达onTouchEvent();
     * ACTION_MOVE只要不拦截，永远到不了onTouchEvent()，即使子View不消化。
     */
    public Result preOnInterceptTouchEvent(MotionEvent ev) {
        //如果有保持（DOWN事件时没有重置）滑动意向，就直接拦截
        if (mHaveSlideIntentionX || mHaveSlideIntentionY) return mResult.set(false, false, true, true, null);
        //如果动画被中止，也要拦截
        if (mPrevAnimInterruptedX || mPrevAnimInterruptedY) return mResult.set(false, false, true, true, null);
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            return mResult.set(false, false, true, false, null);    //子View可能需要处理点击事件
        case MotionEvent.ACTION_MOVE:
            //if(mHaveSlideIntentionX || mHaveSlideIntentionY) return true; //有一次返回true，则不再继续走该方法
            if (mChildTapTimeout) {
                //nothing...    //还没有被拦截，那就不拦截了，应该让子View处理点击事件
            } else {
                mChildTapTimeout = SystemClock.elapsedRealtime() - mTimeDown >= ViewConfiguration.getTapTimeout();
                if (mChildTapTimeout) {
                    //nothing...    //还没有被拦截，那就不拦截了，应该让子View处理点击事件
                } else {    //在点击事件判定时间内，已经有了滑动意向，则拦截，只处理滑动事件，而取消其他事件
                    if(haveSlideIntentionX() || haveSlideIntentionY()) return mResult.set(false, false, true, true, null);
                }
            }
            return mResult.set(false, false, true, false, null);
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            return mResult.set(false, false, true, false, null);    //都到这里了还没拦截，说明没有滑动意向，就让子View好好处理点击事件吧
        default:
            return mResult.set(false, false, true, false, null);
        }
    }

    public boolean postOnInterceptTouchEvent(MotionEvent ev, boolean superConsume) {
        if (!mResult.mConsumeSetted) return superConsume;
        return mResult.consume();
    }

    public Result preOnTouchEvent(MotionEvent ev) {
        /* 当拦截了事件或者Down事件没有子View消化的时候，才会走这里。
         * 那么，只要有滚动意向就算滚动，不用等待子View的点击事件超时，判定其是否消化了。
         * 
         * 虽然在onInterceptTouchEvent()里面也执行了haveSlideIntentionX()，但是那次会不会执行是不确定的，无妨。
         * 
         * 注意：水平或竖直的滑动意向只能有一个。
         */
        if (mHaveSlideIntentionX || mHaveSlideIntentionY) {
            //nothing...
        } else {
            boolean hslx = haveSlideIntentionX();
            boolean hsly = haveSlideIntentionY();
            if (hslx && hsly) { //同时由没意向变为有意向
                if (mScrollByY >= mScrollByX * mIntentionRadio) {
                    mHaveSlideIntentionX = true;
                } else {
                    mHaveSlideIntentionY = true;
                }
            } else if (hslx) {
                mHaveSlideIntentionX = true;
            } else if (hsly) {
                mHaveSlideIntentionY = true;
            }
        }
        switch (ev.getAction()) {
        case MotionEvent.ACTION_MOVE:
            //没有走拦截，child没有消化
            //if (!mChildTapTimeout) mChildTapTimeout = SystemClock.elapsedRealtime() - mDownTime >= ViewConfiguration.getTapTimeout();
            if(mHaveSlideIntentionX || mHaveSlideIntentionY) {
                mResult.set(!mSuperCancelled, true, true, true, ev);
                mSuperCancelled = true;
                return mResult;
            }
            break;
        case MotionEvent.ACTION_UP:
            if(mHaveSlideIntentionX || mHaveSlideIntentionY) {
                mResult.set(!mSuperCancelled, true, true, true, ev);
                mSuperCancelled = true;
                return mResult;
            }
            break;
        }
        return mResult.set(true, false, false, false, ev);
    }

    public boolean postOnTouchEvent(MotionEvent ev, boolean superConsume) {
        if (!mResult.mConsumeSetted) return superConsume;
        return mResult.consume();
    }

    private void requestDisallowIntercept() {
        ViewParent vParent = mView.getParent();
        if(!mDisallowInterceptRequest && vParent != null) {
            vParent.requestDisallowInterceptTouchEvent(true);
            mDisallowInterceptRequest = true;
        }
    }

    private boolean haveSlideIntentionX() { //判定是否有滑动意向
        return !isXIgnored() && Math.abs(mScrollByX) >= mTouchSlop;
    }

    private boolean haveSlideIntentionY() {
        return !isYIgnored() && Math.abs(mScrollByY) >= mTouchSlop;
    }

    private boolean isXIgnored() {
        return mIgnoreXorY == -1;
    }

    private boolean isYIgnored() {
        return mIgnoreXorY == 1;
    }

    protected void deliverOnSlideEventBegin(MotionEvent ev) {
        if (!mSlideEventEndDoneX) deliverOnSlideEventBeginX(ev);
        if (!mSlideEventEndDoneY) deliverOnSlideEventBeginY(ev);
    }

    protected void deliverOnSlideEventMoveInnerX() {
        if (!mSlideEventEndDoneX) deliverOnSlideEventMoveX();
    }

    protected void deliverOnSlideEventMoveInnerY() {
        if (!mSlideEventEndDoneY) deliverOnSlideEventMoveY();
    }

    private void deliverOnSlideEventEndInnerX(MotionEvent ev) {
        if (!mSlideEventEndDoneX) {
            mVelocityTracker.computeCurrentVelocity(mFlingSensitivityX, mMaximumFlingVelocity);
            deliverOnSlideEventEndX(mVelocityTracker.getXVelocity(ev.getPointerId(0)));
            mSlideEventEndDoneX = true;
            clearVelocityTracker();
        }
    }

    private void deliverOnSlideEventEndInnerY(MotionEvent ev) {
        if (!mSlideEventEndDoneY) {
            mVelocityTracker.computeCurrentVelocity(mFlingSensitivityY, mMaximumFlingVelocity);
            deliverOnSlideEventEndY(mVelocityTracker.getYVelocity(ev.getPointerId(0)));
            mSlideEventEndDoneY = true;
            clearVelocityTracker();
        }
    }

    private void clearVelocityTracker() {
        if (mSlideEventEndDoneX && mSlideEventEndDoneY) mVelocityTracker.clear();
    }

    @Override
    protected void finalize() throws Throwable {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    };

    protected void deliverOnSlideEventBeginX(MotionEvent ev) {
        mOnSlideListener.onSlideEventBeginX(ev, mPrevAnimInterruptedX);
    }

    protected void deliverOnSlideEventBeginY(MotionEvent ev) {
        mOnSlideListener.onSlideEventBeginY(ev, mPrevAnimInterruptedY);
    }

    protected void deliverOnSlideEventMoveX() {
        mOnSlideListener.onSlideEventMoveX(mHaveSlideIntentionX, (int)(mScrollByX * mStickyFactorX));
    }

    protected void deliverOnSlideEventMoveY() {
        mOnSlideListener.onSlideEventMoveY(mHaveSlideIntentionY, (int)(mScrollByY * mStickyFactorY));
    }

    protected void deliverOnSlideEventEndX(float velocity) {
        mOnSlideListener.onSlideEventEndX(mHaveSlideIntentionX, mPrevAnimInterruptedX, velocity, mHaveSlideIntentionX && Math.abs(velocity) > mMinimumFlingVelocity);
    }

    protected void deliverOnSlideEventEndY(float velocity) {
        mOnSlideListener.onSlideEventEndY(mHaveSlideIntentionY, mPrevAnimInterruptedY, velocity, mHaveSlideIntentionY && Math.abs(velocity) > mMinimumFlingVelocity);
    }

    public void smoothScrollX(int start, int end) {
        smoothScrollX(start, end, null);
    }

    public void smoothScrollX(int start, int end, OnRequestInvalidationListener onReqInvListener) {
        //为什么是时间间隔而不是距离间隔？时间间隔太长会有卡顿感，但是通常为了快速响应，无论距离间隔大小都应该即时到位
        long duration = AnimationUtils.currentAnimationTimeMillis() - mTimeLastScrollX;
        if (duration > ANIMATED_SCROLL_GAP) {
            mScrollerX.startScroll(start, 0, end - start, 0);
            if (onReqInvListener != null) {
                onReqInvListener.onRequestInvalidate();
            } else {
                mView.invalidate();
            }
        } else {
            if (!mScrollerX.isFinished()) mScrollerX.abortAnimation();
            mOnScrollListener.onScrollToX(end, 0, mSlideEventEndDoneX, mTouching);
        }
        mTimeLastScrollX = AnimationUtils.currentAnimationTimeMillis();
    }

    public void smoothScrollY(int start, int end) {
        smoothScrollY(start, end, null);
    }

    public void smoothScrollY(int start, int end, OnRequestInvalidationListener onReqInvListener) {
        //为什么是时间间隔而不是距离间隔？时间间隔太长会有卡顿感，但是通常为了快速响应，无论距离间隔大小都应该即时到位
        long duration = AnimationUtils.currentAnimationTimeMillis() - mTimeLastScrollY;
        if (duration > ANIMATED_SCROLL_GAP) {
            mScrollerY.startScroll(0, start, 0, end - start);
            if (onReqInvListener != null) {
                onReqInvListener.onRequestInvalidate();
            } else {
                mView.invalidate();
            }
        } else {
            if (!mScrollerY.isFinished()) mScrollerY.abortAnimation();
            mOnScrollListener.onScrollToY(end, 0, mSlideEventEndDoneY, mTouching);
        }
        mTimeLastScrollY = AnimationUtils.currentAnimationTimeMillis();
    }

    public void startScrollX(int start, int end, int duration) {
        startScrollX(start, end, duration, null);
    }

    public void startScrollX(int start, int end, int duration, OnRequestInvalidationListener onReqInvListener) {
        mScrollerX.startScroll(start, 0, end - start, 0, duration);
        if (onReqInvListener != null) {
            onReqInvListener.onRequestInvalidate();
        } else {
            mView.invalidate();
        }
    }

    public void startScrollY(int start, int end, int duration) {
        startScrollY(start, end, duration, null);
    }

    public void startScrollY(int start, int end, int duration, OnRequestInvalidationListener onReqInvListener) {
        mScrollerY.startScroll(0, start, 0, end - start, duration);
        if (onReqInvListener != null) {
            onReqInvListener.onRequestInvalidate();
        } else {
            mView.invalidate();
        }
    }

    public void onComputeScroll() {
        onComputeScrollX(null);
        onComputeScrollY(null);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void onComputeScrollX(OnRequestInvalidationListener onReqInvListener) {
        if (mScrollerX.computeScrollOffset()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mOnScrollListener.onScrollToX(mScrollerX.getCurrX(), mScrollerX.getCurrVelocity(), mSlideEventEndDoneX, mTouching);
            } else {
                mOnScrollListener.onScrollToX(mScrollerX.getCurrX(), 0, mSlideEventEndDoneX, mTouching);
            }
            if (onReqInvListener != null) {
                onReqInvListener.onRequestInvalidate();
            } else {
                mView.invalidate();
            }
            if (mScrollerX.isFinished()) mOnScrollListener.onScrollFinishedX(mSlideEventEndDoneX, mTouching);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void onComputeScrollY(OnRequestInvalidationListener onReqInvListener) {
        if (mScrollerY.computeScrollOffset()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mOnScrollListener.onScrollToY(mScrollerY.getCurrY(), mScrollerY.getCurrVelocity(), mSlideEventEndDoneY, mTouching);
            } else {
                mOnScrollListener.onScrollToY(mScrollerY.getCurrY(), 0, mSlideEventEndDoneY, mTouching);
            }
            if (onReqInvListener != null) {
                onReqInvListener.onRequestInvalidate();
            } else {
                mView.invalidate();
            }
            if (mScrollerY.isFinished()) mOnScrollListener.onScrollFinishedY(mSlideEventEndDoneY, mTouching);
        }
    }

    public boolean abortScrollAndDeliverX() {
        if (mScrollerX.isFinished()) return true;
        mScrollerX.abortAnimation();
        mOnScrollListener.onScrollToX(mScrollerX.getCurrX(), 0, mSlideEventEndDoneX, mTouching);
        mOnScrollListener.onScrollFinishedX(mSlideEventEndDoneX, mTouching);
        return false;
    }

    public boolean abortScrollAndDeliverY() {
        if (mScrollerY.isFinished()) return true;
        mScrollerY.abortAnimation();
        mOnScrollListener.onScrollToY(mScrollerY.getCurrY(), 0, mSlideEventEndDoneY, mTouching);
        mOnScrollListener.onScrollFinishedY(mSlideEventEndDoneY, mTouching);
        return false;
    }

    public boolean abortScrollX() {
        if (mScrollerX.isFinished()) return true;
        mScrollerX.abortAnimation();
        return false;
    }

    public boolean abortScrollY() {
        if (mScrollerY.isFinished()) return true;
        mScrollerY.abortAnimation();
        return false;
    }

    public boolean isScrollFinishedX() {
        return mScrollerX.isFinished();
    }

    public boolean isScrollFinishedY() {
        return mScrollerY.isFinished();
    }

    public static interface OnSlideListener {
        /**
         * 滑动开始
         * @param ev
         * @param prevAnimInterrupted   本次触摸事件是否中止了还未结束的动画
         */
        void onSlideEventBeginX(MotionEvent ev, boolean prevAnimInterrupted);
        void onSlideEventBeginY(MotionEvent ev, boolean prevAnimInterrupted);
        /**
         * 正在滑动
         * @param haveSlideIntention    是否已判定为有滑动意向（有可能用户只是想点击，但是手有轻微抖动。而本回调即使是抖动也会执行）
         * @param scrollBy              相对于down事件时的总位移量，不过已作了粘滞处理
         */
        void onSlideEventMoveX(boolean haveSlideIntention, int scrollBy);
        void onSlideEventMoveY(boolean haveSlideIntention, int scrollBy);
        /**
         * @param haveSlideIntention    是否有拖动意向
         * @param prevAnimInterrupted   本次触摸事件是否中止了还未结束的动画
         * @param velocity              手指滑动速度
         * @param sysDefFling           是不是系统默认的fling动作
         */
        void onSlideEventEndX(boolean haveSlideIntention, boolean prevAnimInterrupted, float velocity, boolean sysDefFling);
        void onSlideEventEndY(boolean haveSlideIntention, boolean prevAnimInterrupted, float velocity, boolean sysDefFling);
    }

    public static interface OnScrollListener {
        void onScrollToX(int target, float velocity, boolean eventEnd, boolean touching);
        void onScrollToY(int target, float velocity, boolean eventEnd, boolean touching);
        void onScrollFinishedX(boolean eventEnd, boolean touching);
        void onScrollFinishedY(boolean eventEnd, boolean touching);
    }

    public static interface OnRequestInvalidationListener {
        void onRequestInvalidate();
    }

    public static class Result {
        /**被追踪的View是否应执行super.xxx()**/
        public boolean performSuper;
        public MotionEvent superEvent;
        private boolean mConsume;
        private boolean mConsumeSetted;
        private boolean mNewEvent;

        private Result set(boolean pSuper, boolean superCancel, boolean consSetted, boolean consume, MotionEvent ev) {
            performSuper = pSuper;
            mConsumeSetted = consSetted;
            mConsume = consume;

            recycle();
            if (pSuper) {
                if (superCancel) {
                    superEvent = MotionEvent.obtain(ev);
                    superEvent.setAction(MotionEvent.ACTION_CANCEL);
                    mNewEvent = true;
                } else {
                    superEvent = ev;
                    mNewEvent = false;
                }
            }
            return this;
        }

        public boolean consume() {
            recycle();
            return mConsume;
        }

        private void recycle() {
            if (mNewEvent && superEvent != null) {
                superEvent.recycle();
                mNewEvent = false;
                superEvent = null;
            }
        }
    }
}
