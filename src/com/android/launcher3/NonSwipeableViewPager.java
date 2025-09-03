package com.android.launcher3;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.viewpager.widget.ViewPager;

public class NonSwipeableViewPager extends ViewPager {
    public NonSwipeableViewPager(Context context) {
        super(context);
    }

    public NonSwipeableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // 不拦截触摸事件
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 不处理触摸事件
        return false;
    }
}
