package com.ixunta.client.myview;

import java.lang.reflect.Field;

import com.ixunta.client.IwantUApp.MsgHandler;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;


@SuppressLint("NewApi")
public abstract class MyHorizontalScrollView extends HorizontalScrollView {

	
	/*
	 * ��ǰ�û��Ƿ��ڴ���hsv
	 */
	private boolean currentlyTouching = false;
	/*
	 * subview�Ŀ�ȡ�
	 */
	private int itemWidth;
	/*
	 * hsv �����LinearLayout��������װ��subview��
	 */
	private LinearLayout ll;

	SpecialScroller myScroller = null;
	
	private MsgHandler msgHandler;
	private int msgWhat;
	

	/*
	 * ��ǰsubview��index,��ʼֵΪ0
	 */
	private int currentIndex = 0;

	public MyHorizontalScrollView(Context context) {
		this(context, null);
	}
	
	public MyHorizontalScrollView(Context context, MsgHandler msgHandler, int msgWhat) {
		this(context, null);
		this.msgHandler = msgHandler;
		this.msgWhat = msgWhat;
	}

	public MyHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		ll = new LinearLayout(context);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
				android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
		this.addView(ll, layoutParams);
		
		//!!!!!!!!!!!!!!!!!!!!!!!!!!
		// api16
		// this.setScrollBarDefaultDelayBeforeFade(0);
		
		initScroller();
	}
	

	/**
	 * ����subview
	 * 
	 * @param view
	 * @param layoutParams
	 */
	public void addSubView(View view, LinearLayout.LayoutParams layoutParams) {
		ll.addView(view, layoutParams);
	}

	public void addSubView(View view, int pos,
			LinearLayout.LayoutParams layoutParams) {
		ll.addView(view, pos, layoutParams);
	}

	/**
	 * �ƶ���ͼ��һ��ָ����subview�ϡ� ���ֱ����scrollBy,û���κ�Ч����������post��
	 */
	public void moveToSubView(int index) {
		this.currentIndex = index;
		this.post(new Runnable() {
			public void run() {
				// TODO Auto-generated method stub
				scrollTo(currentIndex * itemWidth, 0);
			}
		});
	}

	public MyHorizontalScrollView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		if (Math.abs(l - oldl) <= 1 && !currentlyTouching) {
			adjustX(l);
			myScroller.forceFinished();
		}
		super.onScrollChanged(l, t, oldl, oldt);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			currentlyTouching = true;
			break;
		}
		return super.onInterceptTouchEvent(event);
	}

	public int adjustX(int currentX) {
		currentIndex = currentX / itemWidth;
		int item_residual = currentX % itemWidth;
		if (item_residual >= itemWidth / 2) {
			currentIndex += 1;
		}
		int ajustedX = currentIndex * itemWidth;
		scrollTo(ajustedX, 0);
		
		onCurrentIndexChanged();
		
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

	private void initScroller() {
		Field mScrollerField;
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

	public int getCurrentIndex() {
		return currentIndex;
	}

	public void setCurrentIndex(int index) {
		this.currentIndex = index;
	}

	public void setSubTextViewColorGray(int index) {
		TextView view = (TextView) this.getChildAt(index);
		view.setTextColor(Color.GRAY);
	}
	public int getItemCnt(){
		return ll.getChildCount();
	}
	public MsgHandler getMsgHandler() {
		return msgHandler;
	}

	public void setMsgHandler(MsgHandler msgHandler) {
		this.msgHandler = msgHandler;
	}

	public int getMsgWhat() {
		return msgWhat;
	}

	public void setMsgWhat(int msgWhat) {
		this.msgWhat = msgWhat;
	}

	/**
	 * sub view�Ŀ��
	 * 
	 * @param width
	 */
	public void setItemWidth(int width) {
		this.itemWidth = width;
	}
	
	/**
	 * ɾ�����е�subview
	 */
	public void removeAllItemViews(){
		ll.removeAllViews();
	}

	public View getSubviewAt(int index) {
		// TODO Auto-generated method stub
		
		return ll.getChildAt(index);
	}
	
	public abstract void onCurrentIndexChanged();

}
