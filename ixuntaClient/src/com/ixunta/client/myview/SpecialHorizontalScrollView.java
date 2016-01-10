package com.ixunta.client.myview;

import java.lang.reflect.Field;
import java.util.Timer;

import com.ixunta.client.IwantUApp;
import com.ixunta.client.MainActivity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.os.Bundle;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.OverScroller;
import android.widget.Scroller;

@SuppressLint("NewApi")
public class SpecialHorizontalScrollView extends HorizontalScrollView {

	public static final String MSG_TO_MAINACTIVITY = "Current_Ta_Index";
	private boolean currentlyScrolling = false;
	private boolean currentlyTouching = false;
	private int lastTouchedX = 0;
	private int currentX = 0;
	private int itemWidth = 64;
	private Field mScrollerField;
	private GestureDetector mGestureDetector;
	private static final int SWIPE_MIN_DISTANCE = 5;
	private static final int SWIPE_THRESHOLD_VELOCITY = 300;

	private MainActivity mainActivity;

	SpecialScroller myScroller = null;

	private int current_TA_index = 0;
	public SpecialHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mainActivity = (MainActivity) context;

		// centerDrawable.setCallback(this);
		// centerDrawable.setBounds(0, 0, itemWidth, itemHeight);
		initScroller();
	}

	public SpecialHorizontalScrollView(Context context) {
		super(context);
	}

	public SpecialHorizontalScrollView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setItemWidth(int itemWidth) {

		this.itemWidth = itemWidth;
	}

	public boolean shouldDelayChildPressedState() {
		return false;
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		// TODO Auto-generated method stub

		if (Math.abs(l - oldl) > 1) {
			currentlyScrolling = true;
		} else {
			currentlyScrolling = false;
			if (!currentlyTouching) {
				adjustX(l);
				myScroller.forceFinished();
			}
		}

		super.onScrollChanged(l, t, oldl, oldt);

	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			currentlyTouching = true;
			lastTouchedX = (int) event.getX();
			break;
		}

		return super.onInterceptTouchEvent(event);
	}

	public int adjustX(int currentX) {
		current_TA_index = currentX / itemWidth;
		int item_residual = currentX % itemWidth;
		if (item_residual >= itemWidth / 2) {
			current_TA_index += 1;
		}
		int ajustedX = current_TA_index * itemWidth;
		scrollTo(ajustedX, 0);

		Bundle b = new Bundle();
		b.putInt("taIndex", current_TA_index - 1);
		Message msg = new Message();
		msg.setData(b);
		msg.what = IwantUApp.MSG_TO_MAIN_HPICKER_CHANGED;
		IwantUApp.msgHandler.sendMessage(msg);

		return ajustedX;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP
				|| event.getAction() == MotionEvent.ACTION_CANCEL) {

			int currentX = getScrollX();
			adjustX(currentX);
			currentlyTouching = false;
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	private void initScroller() {
		try {
			mScrollerField = HorizontalScrollView.class
					.getDeclaredField("mScroller");
			mScrollerField.setAccessible(true);

			myScroller = new SpecialScroller();
			
			DecelerateInterpolator dip = new DecelerateInterpolator(1);

			myScroller.create(getContext(), dip);
			try {
				mScrollerField.set(this, myScroller.getScroller());
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception ex) {
		}
	}

	public int getCurrent_item_index() {
		return current_TA_index;
	}

	public void setCurrent_item_index(int current_item_index) {
		this.current_TA_index = current_item_index;
	}

	public int getCurrent_TA_index() {
		return current_TA_index;
	}

	public void setCurrent_TA_index(int current_TA_index) {
		this.current_TA_index = current_TA_index;
	}


}
