package com.youme.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.TextView;

import com.youme.R;
import com.youme.util.ScreenUtils;

import java.util.List;

/**
 * 整体下拉刷新的布局
 * Created by leihuating on 2017/12/28.
 */

public class PullRefreshView extends LinearLayout {
    Context context;
    //流动条对象
    Scroller scroller;

    //自定义接口对象
    public PullRefreshListener pullRefreshListener;

    //下拉刷新头部布局对象
    View refreshView;
    ImageView image_arrow;
    ImageView image_loadding;
    TextView tv_refreshState;
    TextView tv_refreshTime;

    int refreshTop = 50;

    //touch事件中的参数
    int lastY;
    final int MIN_PULL_DISTANCE = 20;
    boolean isRefreshing = false;

    int listviewPosotion = 2;

    public PullRefreshView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        scroller = new Scroller(context);
        // 将下拉刷新的view的margin值转换成px,(主要目的为兼容)
        refreshTop = ScreenUtils.dip2px(context, refreshTop);

        //初始化布局
        initRefreshView();
    }

    /**
     * 初始化显示刷新的头标布局，并添加到PullRefreshView中
     */
    private void initRefreshView() {
        refreshView = LayoutInflater.from(context).inflate(R.layout.header_pull_refresh, null);
        image_arrow = (ImageView) refreshView.findViewById(R.id.img_pullRefresh_arrow);
        image_loadding = (ImageView) refreshView.findViewById(R.id.img_pullRefresh_loading);
        tv_refreshState = (TextView) refreshView.findViewById(R.id.tv_pullRefresh_refreshState);
        tv_refreshTime = (TextView) refreshView.findViewById(R.id.tv_pullRefresh_refreshTime);

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, -refreshTop);
        lp.topMargin = refreshTop;
        lp.gravity = Gravity.CENTER;
        refreshView.setLayoutParams(lp);

//        setBackgroundColor(getResources().getColor(R.color.black));
        addView(refreshView);
    }


    /**
     * onInterceptTouchEvent判断是否将这个事件传递给子控件，即ScrollView
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int touchY = (int) ev.getRawY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastY = touchY;
                break;
            case MotionEvent.ACTION_MOVE:
                int moveY = touchY - lastY;
                // 判断拉动的距离是不是超过了最小距离,并且是向上拉动的,然后屏蔽子项
                if (moveY > MIN_PULL_DISTANCE && canScroll()) {
                    return true;
                }
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 触摸事件监听，改变布局状态
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int touchY = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastY = touchY;
                break;
            case MotionEvent.ACTION_MOVE:
                int moveY = touchY - lastY;
                //改变头标布局参数
                dropDown(moveY);
                break;
            case MotionEvent.ACTION_UP:
                fling();
                break;
        }

        return true;
    }

    /**
     * 下拉松开后,调用此函数,用来判断是刷新还是复位
     */
    private void fling() {
        LayoutParams lp = (LayoutParams) refreshView.getLayoutParams();
        if (lp.topMargin > 0) {
            //正在刷新
            refresh();
        } else {
            //松开返回原位
            recoverView();
        }
    }

    private void recoverView() {
        
    }

    /**
     * 松开刷新
     */
    private void refresh() {
        isRefreshing = true;
       
    }

    /**
     * 通过下拉的距离改变下拉刷新的布局显示位置及参数
     *
     * @param moveY
     */
    private void dropDown(int moveY) {
        LayoutParams lp = (LayoutParams) refreshView.getLayoutParams();
        int f1 = lp.topMargin;
        float f2 = moveY * 0.2f;
        int i = (int) (f1 + f2); // 将Margin不断变大,能够看到

        if (i <= ScreenUtils.dip2px(context, 80))
            lp.topMargin = i;
        //修改后刷新
        refreshView.setLayoutParams(lp);
        refreshView.invalidate();//进行子View重新绘制
        invalidate(); // 进行整个控件的重新绘制

        //当把下拉刷新的布局全部显示出来时====可松手刷新
        //否则===仍显示正在下拉刷新
        if (lp.topMargin > 0) {
            //可松手刷新
            releaseToRefresh();
        } else {
            //下拉刷新
            pullToRefresh();
        }


    }

    /**
     * 下拉刷新
     */
    private void pullToRefresh() {
        isRefreshing = false;
        image_arrow.setVisibility(View.VISIBLE);
        image_loadding.clearAnimation();
        image_loadding.setVisibility(View.GONE);
        tv_refreshState.setText(R.string.pullRefresh_pull);
        if (tv_refreshState.getText().equals(R.string.pullRefresh_release)) {
            ObjectAnimator.ofFloat(image_arrow, "rotation", 180, 0).setDuration(500).start();
        }
    }

    /**
     * 释放立刻刷新
     */
    private void releaseToRefresh() {
        isRefreshing = false;
        image_arrow.setVisibility(View.VISIBLE);
        image_loadding.clearAnimation();
        image_loadding.setVisibility(View.GONE);
        tv_refreshState.setText(R.string.pullRefresh_release);
        ObjectAnimator.ofFloat(image_arrow, "rotation", 180, 0).setDuration(500).start();
    }

    /**
     * 判断Listview是否已经拉到了最上边
     * 已经拉到了最上边，即未滑动，则不用往下传递事件，返回true
     * 否则，要往下传递事件返回false
     * <p>
     * <p>
     * 此处是为了兼容Listview，和某一种特定的布局
     * 所以getChildAt()里面的参数是2
     * 如果布局结构改变，或者要兼容其他类型的控件，都可以在上面添加改动
     *
     * @return
     */
    private boolean canScroll() {
        View childView = null;
        if (getChildCount() > 1) {
            childView = getChildAt(listviewPosotion);
            if (childView instanceof ListView) {// 此处写可以兼容ListView
                if (((ListView) childView).getFirstVisiblePosition() == 0) {// 说明Listview已经能看到第一行,但是有可能listview的第一列只显示了一半
                    View firstItemView = ((ListView) childView).getChildAt(0);
                    if (firstItemView == null || (firstItemView != null && firstItemView.getTop() == 0)) {//第一列全部显示完毕
                        return true;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 自定义接口
     */
    public interface PullRefreshListener {
        void onRefresh(PullRefreshView view);
    }

}
