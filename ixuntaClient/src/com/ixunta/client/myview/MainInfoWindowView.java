package com.ixunta.client.myview;

import com.ixunta.R;
import com.ixunta.client.IwantUApp;
import com.ixunta.client.db.PubWithMem;
import com.ixunta.client.db.Publication;
import com.ixunta.client.util.AppUtil;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 该类的主要目的是重写Relativelayout的onsizechange方法，以在Infowindow显示的时候，得到其大小。
 * InfoWindowView会把main_infowindow做为一个子视图加入，但是在地图上显示的时候，地图默认的infowindow背景框总是出现。
 * 如果用InfoWindowView。getchild(0)得到子视图的话，必须首先removeallchildview才能够让hideInfoWindow不报错，
 * 但调用removechild后，onsizechange就不会起作用。所以决定放弃该方法。20140308
 * 
 * 20140310,加入setBackgroundResource以后，该方法是可行的。
 * 
 * @author tom @date 2014-3-8
 *
 */
@Deprecated
public class MainInfoWindowView extends RelativeLayout {
	private ImageView iv;
	private TextView tv_gender_and_age;
	private TextView tv_space_and_time_and_dest;
	private Button bt_call;
	private Button bt_sms;
	private Activity activity;

	public MainInfoWindowView(Context context) {
		super(context);
		if (context instanceof Activity) {
			this.activity = (Activity) context;
		}
		
	    LayoutInflater inflater = (LayoutInflater) context
	            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    inflater.inflate(R.layout.main_infowindow, this, true);
//		LayoutInflater.from(context).inflate(R.layout.main_infowindow, this);
	    init();
	}
	
	public MainInfoWindowView(Context context, AttributeSet attributeSet ) {
		super(context, attributeSet);
		if (context instanceof Activity) {
			this.activity = (Activity) context;
		}
		init();
	}
	public void init(){
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		this.setLayoutParams(lp);
		this.setBackgroundResource(R.drawable.infowindow_bg);
		iv = (ImageView) this.findViewById(R.id.main_infoWindow_iv_portrait);
		tv_gender_and_age = (TextView) this
				.findViewById(R.id.main_infoWindow_tv_gender_and_age);
		tv_space_and_time_and_dest = (TextView) this
				.findViewById(R.id.main_infoWindow_tv_space_and_time_and_dest);
		bt_call = (Button) this.findViewById(R.id.main_infoWindow_bt_call);
		bt_sms = (Button) this.findViewById(R.id.main_infoWindow_bt_sms);
		this.invalidate();
	}
	

	public void createViewContent(Publication myPub, PubWithMem taPubWithMem) {
		int width = AppUtil.dipRes2Px(activity,
				R.dimen.main_infowindow_portrait_width);
		int height = AppUtil.dipRes2Px(activity,
				R.dimen.main_infowindow_portrait_height);
		IwantUApp app = (IwantUApp) activity.getApplication();
		Drawable d = app.getDrawableFromFileName(taPubWithMem.getPortraitFileName(),
				width, height);

		if (null == d) {
			d = getResources().getDrawable(
					R.drawable.portrait_default_ta);
		}
		iv.setImageDrawable(d);

		String gender;
		int age;
		if (taPubWithMem.getGender() != null
				&& taPubWithMem.getGender().equals(IwantUApp.CONS_GENDER_FEMALE)) {
			gender = getResources().getString(
					R.string.main_infowindow_ta_female);
		} else {
			gender = getResources().getString(R.string.main_infowindow_ta_male);
		}
		age = taPubWithMem.getAge();
		tv_gender_and_age.setText(String.format(
				getResources().getString(
						R.string.main_infowindow_gender_and_age), gender, age));

		String spaceAndTimeInfo = getSpaceAndTimeInfo(myPub,
				taPubWithMem.getPublication());
		String destInfo = (String.format(
				getResources().getString(
						R.string.main_infowindow_ta_destination),
				taPubWithMem.getDestName()));
		tv_space_and_time_and_dest.setText(spaceAndTimeInfo + destInfo);

	}
	
	public void setOnClickListener_bt_call(OnClickListener listener){
		bt_call.setOnClickListener(listener);
	}
	public void setOnClickListener_bt_sms(OnClickListener listener){
		bt_sms.setOnClickListener(listener);
	}

	private String getSpaceAndTimeInfo(Publication myPub, Publication taPub) {
		long timeDistance = (myPub.getDatetime() - taPub.getDatetime()) / 60000;
		Location myLoc = new Location("GPS");
		myLoc.setLatitude(myPub.getLatitude());
		myLoc.setLongitude(myPub.getLongitude());
		Location taLoc = new Location("GPS");
		taLoc.setLatitude(taPub.getLatitude());
		taLoc.setLongitude(taPub.getLongitude());
		int spaceDistanceMeter = (int) myLoc.distanceTo(taLoc);
		int spaceDistanceKilo = (int) (spaceDistanceMeter / 1000);
		String spaceInfo;
		if (spaceDistanceKilo >= 1) {
			spaceInfo = spaceDistanceKilo + "公里";
		} else {
			spaceInfo = spaceDistanceMeter + "米";
		}
		return String.format(
				getResources().getString(
						R.string.main_infowindow_space_and_time), spaceInfo,
				timeDistance);
	}
	
	protected void onSizeChanged (int w, int h, int oldw, int oldh){
		super.onSizeChanged(w, h, oldw, oldh);
		
		Bundle b = new Bundle();
		b.putInt("infowindowheight", h);
		Message msg = new Message();
		msg.what = IwantUApp.MSG_TO_MAIN_INFOWINDOW_DRAWN;
		IwantUApp.msgHandler.sendMessage(msg);
	}

	
}
