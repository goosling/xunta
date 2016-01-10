/**
 * 主页面
 */

package com.ixunta.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import org.apache.http.conn.ConnectTimeoutException;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.xml.SimpleXmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.ixunta.R;
import com.ixunta.client.db.ActionResult;
import com.ixunta.client.db.Member;
import com.ixunta.client.db.PubWithMem;
import com.ixunta.client.db.PubWithMemList;
import com.ixunta.client.db.Publication;
import com.ixunta.client.myview.MyHorizontalPicker;
import com.ixunta.client.myview.SeekBarWithText;
import com.ixunta.client.util.AppUtil;

public class MainActivity extends ActionBarActivity implements
		AMapLocationListener, OnMapLoadedListener, OnClickListener,
		InfoWindowAdapter {

	// 页面布局
	private MyHorizontalPicker taPicker;
	private View hpicker_subview_me;
	private MapView mapView;
	private AMap aMap;
	private SeekBarWithText seekBarSpace;
	private SeekBarWithText seekBarTime;
	private Button bt_xunta;
	private Button bt_start;
	private RelativeLayout rl_guide;

	private ProgressDialog progressDialog;
	private AlertDialog alertDialog;

	// data
	private Publication myPub;
	private Member member;
	private PubWithMem currentTa;
	private boolean isPhoneCallMadeToCurrentTa = false;

	private ArrayList<View> hPickerSubViewList;

	private ArrayList<PubWithMem> taList;
	
	

	// 已经联系过，但不合适的pubwithmem
	private ArrayList<PubWithMem> inproperTaList;

	// 所有的marker，包含me和ta
	private ArrayList<Marker> markerList;
	private int taCount = 0;
	private int taIndex = 0;
	private int currentZoom = MAP_ZOOM_DEFAULT;

	private IwantUApp app;

	private ActionResult actionResult;
	private LocationManagerProxy mAMapLocManager;
	private PublishAndSearchTask publishAndSearchTask;

	// 常亮
	// HSV界面
	public static final int HPICKER_ITEM_CNT = 5;
	public static final int HPICKER_DUMMY_ITEM_CNT = 4;
	public static final int HPICKER_ITEM_GAP = 2;

	public static final float MAP_TILT = 15.0f;
	public static final int MAP_ZOOM_20m = 20;
	public static final int MAP_ZOOM_200m = 17;
	public static final int MAP_ZOOM_500m = 15;
	public static final int MAP_ZOOM_2km = 14;
	public static final int MAP_ZOOM_10km = 12;
	public static final int MAP_ZOOM_city = 11;
	public static final int MAP_ZOOM_region = 8;
	public static final int MAP_ZOOM_country = 4;
	
	
	public static final int MAP_ZOOM_LVL0 = MAP_ZOOM_city;
	public static final int MAP_ZOOM_LVL1 = MAP_ZOOM_10km;
	public static final int MAP_ZOOM_LVL2 = MAP_ZOOM_2km;
	public static final int MAP_ZOOM_DEFAULT = MAP_ZOOM_LVL1;

	public static final int SEEKBAR_PROGRESS_TIME_DEFAULT = 1;
	public static final int SEEKBAR_PROGRESS_SPACE_DEFAULT = 1;
	public static final int SEEKBAR_PROGRESS_TIME_MAX = 2;
	public static final int SEEKBAR_PROGRESS_SPACE_MAX = 2;


	public static String SEEKBAR_SPACE_200M_STR = "200米";
	public static String SEEKBAR_SPACE_500M_STR = "500米";
	public static String SEEKBAR_SPACE_2KM_STR = "2公里";
	public static String SEEKBAR_SPACE_10KM_STR = "10公里";
	public static String SEEKBAR_SPACE_CITY_STR = "30公里";
	
	public static String SEEKBAR_SPACE_LVL0_STR = SEEKBAR_SPACE_CITY_STR;;
	public static String SEEKBAR_SPACE_LVL1_STR = SEEKBAR_SPACE_10KM_STR;;
	public static String SEEKBAR_SPACE_LVL2_STR = SEEKBAR_SPACE_2KM_STR;
	public static String SEEKBAR_SPACE_DEFAULT_STR = SEEKBAR_SPACE_LVL1_STR;

	// unit meter
	public static float SEARCH_CRITERIA_SPACE_DISTANCE_20m = 20;
	public static float SEARCH_CRITERIA_SPACE_DISTANCE_200m = 200;
	public static float SEARCH_CRITERIA_SPACE_DISTANCE_500m = 500;
	public static float SEARCH_CRITERIA_SPACE_DISTANCE_2km = 2000;
	public static float SEARCH_CRITERIA_SPACE_DISTANCE_10km = 10000;
	public static float SEARCH_CRITERIA_SPACE_DISTANCE_city = 30000;
	public static float SEARCH_CRITERIA_SPACE_DISTANCE_region = 100000;
	public static float SEARCH_CRITERIA_SPACE_DISTANCE_country = 1565430;
	
	public static float SEARCH_CRITERIA_SPACE_DISTANCE_LVL0 = SEARCH_CRITERIA_SPACE_DISTANCE_city;
	public static float SEARCH_CRITERIA_SPACE_DISTANCE_LVL1 = SEARCH_CRITERIA_SPACE_DISTANCE_10km;
	public static float SEARCH_CRITERIA_SPACE_DISTANCE_LVL2 = SEARCH_CRITERIA_SPACE_DISTANCE_2km;
	public static float SEARCH_CRITERIA_SPACE_DISTANCE_DEFAULT = SEARCH_CRITERIA_SPACE_DISTANCE_LVL1;

	public static String SEEKBAR_TIME_5MIN_STR = "5分钟";
	public static String SEEKBAR_TIME_10MIN_STR = "10分钟";
	public static String SEEKBAR_TIME_30MIN_STR = "30分钟";
	public static String SEEKBAR_TIME_1HOUR_STR = "1小时";
	public static String SEEKBAR_TIME_6HOUR_STR = "6小时";
	public static String SEEKBAR_TIME_12HOUR_STR = "12小时";
	public static String SEEKBAR_TIME_24HOUR_STR = "24小时";
	
	public static String SEARCH_CRITERIA_TIME_DISTANCE_LVL0_STR = SEEKBAR_TIME_24HOUR_STR;
	public static String SEARCH_CRITERIA_TIME_DISTANCE_LVL1_STR = SEEKBAR_TIME_12HOUR_STR;
	public static String SEARCH_CRITERIA_TIME_DISTANCE_LVL2_STR = SEEKBAR_TIME_6HOUR_STR;
	public static String SEEKBAR_TIME_DEFAULT_STR = SEARCH_CRITERIA_TIME_DISTANCE_LVL1_STR;
	
	// unit minute;
	// 一年的时间范围。
	public static long SEARCH_CRITERIA_TIME_DISTANCE_1min = 1;
	public static long SEARCH_CRITERIA_TIME_DISTANCE_5min = 5;
	public static long SEARCH_CRITERIA_TIME_DISTANCE_10min = 10;
	public static long SEARCH_CRITERIA_TIME_DISTANCE_30min = 30;
	public static long SEARCH_CRITERIA_TIME_DISTANCE_1hour = 60;
	public static long SEARCH_CRITERIA_TIME_DISTANCE_6hour = 360;
	public static long SEARCH_CRITERIA_TIME_DISTANCE_12hour = 720;
	public static long SEARCH_CRITERIA_TIME_DISTANCE_24hour = 1440;
	
	public static long SEARCH_CRITERIA_TIME_DISTANCE_LVL0 = SEARCH_CRITERIA_TIME_DISTANCE_24hour;
	public static long SEARCH_CRITERIA_TIME_DISTANCE_LVL1 = SEARCH_CRITERIA_TIME_DISTANCE_12hour;
	public static long SEARCH_CRITERIA_TIME_DISTANCE_LVL2 = SEARCH_CRITERIA_TIME_DISTANCE_6hour;
	public static long SEARCH_CRITERIA_TIME_DISTANCE_DEFAULT = SEARCH_CRITERIA_TIME_DISTANCE_LVL1;

	// //位置更新的时间门槛，单位是毫秒，在requestLocationUpdates方法中使用。
	public static final int CONS_LOC_UPDATE_TIME_THRESHOLD = 30000;
	// 位置更新的距离门槛，单位是米，在requestLocationUpdates方法中使用。
	private static final int CONS_LOC_UPDATE_SPACE_THRESHOLD = 200;

	private static final int REQUEST_CODE_MEMBERINFO = 0x01;
	private static final int REQUEST_CODE_FEEDBACK = 0x02;
	private static final int REQUEST_CODE_ABOUTUS = 0x03;
	private static final int REQUEST_CODE_IWANT = 0x04;
	private static final int REQUEST_CODE_CALL = 0x05;
	private static final int REQUEST_CODE_SMS = 0x06;

	private static final float METERS_PER_LATITUDE = 110000;
	 

	// /////////////////////////////////////////////////////////////////////////
	// MainActivity生命周期控制。
	/**
	 * oncreate
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (IwantUApp) this.getApplication();
		IwantUApp.addActivity(this);

		// 初始化页面
		setContentView(R.layout.main);
		mapView = (MapView) findViewById(R.id.main_map);
		mapView.onCreate(savedInstanceState);// 此方法必须重写
		aMap = mapView.getMap();
		seekBarSpace = (SeekBarWithText) findViewById(R.id.main_seekbar_space_1);
		seekBarTime = (SeekBarWithText) findViewById(R.id.main_seekbar_time_1);
		bt_xunta = (Button) findViewById(R.id.main_bt_xunta);
		bt_start = (Button) findViewById(R.id.main_bt_start);
		taPicker = (MyHorizontalPicker) findViewById(R.id.main_hpicker);
		rl_guide =(RelativeLayout) findViewById(R.id.main_layout_guide);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// init listener
		aMap.setOnMapLoadedListener(this);
		aMap.setInfoWindowAdapter(this);
		seekBarSpace
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener_space());
		seekBarTime
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener_time());
		bt_xunta.setOnClickListener(this);
		bt_start.setOnClickListener(this);
		// 当rl_guide出现时，拦截touch事件，使得其它控件不能够被点击。
		rl_guide.setOnTouchListener(new OnTouchListener() {		
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		
		// 监听打电话
		PhoneCallListener phoneListener = new PhoneCallListener();
		TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(phoneListener,
				PhoneStateListener.LISTEN_CALL_STATE);

		// 通用的初始化
		initProgressDialog();
		initAlertDialog();
		initMap();	
		initTaPicker();
		
		// 请求地理位置
		mAMapLocManager = LocationManagerProxy.getInstance(this);
		try {
			mAMapLocManager.requestLocationUpdates(
					LocationProviderProxy.AMapNetwork,
					CONS_LOC_UPDATE_TIME_THRESHOLD,
					CONS_LOC_UPDATE_SPACE_THRESHOLD, this);
		} catch (Exception e) {
			// 发生异常不做任何处理。
		}

		// 根据来源的不同初始化数据
		// 初始化mypub和member
		// 如果是从系统资源回收恢复过来，读取savedInstanceState
		if (savedInstanceState != null) {
			myPub = savedInstanceState.getParcelable("myPub");
			member = savedInstanceState.getParcelable("member");
		} else {
			// 从iwant页面过来
			Intent i = getIntent();
			member = (Member) i.getParcelableExtra(IwantUApp.ONTOLOGY_MEMBER);
			myPub = (Publication) i
					.getParcelableExtra(IwantUApp.ONTOLOGY_PUBLICATION);
		}
		// 初始化其它数据
		markerList = new ArrayList<Marker>();
		hPickerSubViewList = new ArrayList<View>();
		inproperTaList = new ArrayList<PubWithMem>();
		actionResult = new ActionResult();
		actionResult.setMyID(member.getId());

		// 根据Mypub的信息重置地图和tapicker	
		restoreMap();
		restoreTaPicker();
		addMeToMap();

		if (savedInstanceState == null) {
			// 用默认数据初始化个性化的数据
			restoreDefaultData();
			// 移动视图到用户的位置
			aMap.moveCamera(CameraUpdateFactory
					.newCameraPosition(new CameraPosition.Builder()
							.target(new LatLng(myPub.getLatitude(), myPub
									.getLongitude())).zoom(currentZoom)
							.tilt(MAP_TILT).build()));
			// 发布并且搜索
			publishAndSearchTask = new PublishAndSearchTask();
			publishAndSearchTask.execute();
		} else {
			// 由onRestoreInstanceState处理
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
		Log.d("main", "onPause is invoked");
	}

	@Override
	protected void onResume() {
		super.onResume();
		

		mapView.onResume();
	

		Log.d("main", "onResume is invoked");
	}

	public void onStart() {
		super.onStart();
		Log.d("main", "onStart is invoked");
	}

	public void onStop() {
		super.onStop();
		Log.d("main", "onStop is invoked");

	}

	public void onRestart() {
		super.onRestart();
		Log.d("main", "onRestart is invoked");
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		Log.d("main", "onSaveInstanceState is invoked");
		mapView.onSaveInstanceState(savedInstanceState);

		if (taList == null) {
			return;
		}

		savedInstanceState.putParcelable("myPub", myPub);
		savedInstanceState.putParcelable("member", member);

		Log.d("main, onSaveInstanceState", "member, " + myPub.toString());
		Log.d("main, onSaveInstanceState", "member, " + member.toString());

		savedInstanceState.putParcelableArrayList("taList", taList);
		savedInstanceState.putParcelableArrayList("inproperTaList", inproperTaList);
		
		savedInstanceState
				.putInt("taIndex", taPicker.getAdjustedCurrentIndex());
		Log.e("main, onSaveInstanceState",
				"taIndex, " + taPicker.getAdjustedCurrentIndex());

		savedInstanceState.putInt("seekBarSpace_progress",
				seekBarSpace.getProgress());
		savedInstanceState.putInt("seekBarTime_progress",
				seekBarTime.getProgress());
		savedInstanceState.putBoolean("isPhoneCallMadeToCurrentTa",
				isPhoneCallMadeToCurrentTa);
		
		Log.d("main, onSaveInstanceState", "isPhoneCallMadeToCurrentTa, " + isPhoneCallMadeToCurrentTa);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		taList = savedInstanceState
				.getParcelableArrayList("taList");
		inproperTaList = savedInstanceState.getParcelableArrayList("inproperTaList");
		Log.d("main onRestoreInstanceState", "inproperTaList: " + inproperTaList.toString());
		
		for (PubWithMem pmx : inproperTaList){
			Log.d("main onRestoreInstanceState", "inproper TA" + pmx.toString());
		}
		
		int space_progress = savedInstanceState.getInt("seekBarSpace_progress");
		int time_progress = savedInstanceState.getInt("seekBarTime_progress");
		
		isPhoneCallMadeToCurrentTa = savedInstanceState.getBoolean("isPhoneCallMadeToCurrentTa");

		// 读取数据出现空数据，或者上次没有搜索结果，就重新去数据库查询
		if (member == null || myPub == null || taList == null
				|| taList.size() == 0) {
			// 用默认数据初始化个性化的数据
			restoreDefaultData();
			// 移动视图到用户的位置
			aMap.moveCamera(CameraUpdateFactory
					.newCameraPosition(new CameraPosition.Builder()
							.target(new LatLng(myPub.getLatitude(), myPub
									.getLongitude())).zoom(currentZoom)
							.tilt(MAP_TILT).build()));
			// 发布并且搜索
			publishAndSearchTask = new PublishAndSearchTask();
			publishAndSearchTask.execute();
		} else {
			for (PubWithMem pm : taList) {
				updateTaList(pm);
				Log.d("main, onRestoreInstanceState",
						"PubWithMem, " + pm.toString());
			}
			
			taIndex = savedInstanceState.getInt("taIndex");
			Log.e("main onRestoreInstanceState", "taIndex: " + taIndex);
			if (taIndex > 0  && taList.size() > 0){
				currentTa = taList.get(taIndex - 1);
			}			
			taPicker.moveToSubView(taIndex);
			updateHpickerPortrait(taIndex);
			addMeToMap();
			updateMap(taIndex);

			// 先将progressBar移动到一个和当前位置不同的位置，再移动回来，以调用onprogresschanged。
			seekBarSpace.setProgress((space_progress + 1) % 3);
			seekBarTime.setProgress((time_progress + 1) % 3);
			seekBarSpace.setProgress(space_progress);
			seekBarTime.setProgress(time_progress);
			
			Log.d("main, onRestoreInstanceState", "isPhoneCallMadeToCurrentTa, " + isPhoneCallMadeToCurrentTa);
			
			if (isPhoneCallMadeToCurrentTa) {
				alertDialog.show();
				isPhoneCallMadeToCurrentTa = false;
			}
		}
	}

	// ////////////////////////////////////////////////////////////////////////////
	// 初始化区域
	/**
	 * 用默认数据初始化seekbar和mypub
	 */
	private void restoreDefaultData() {

		seekBarSpace.setProgress(SEEKBAR_PROGRESS_SPACE_DEFAULT);
		seekBarTime.setProgress(SEEKBAR_PROGRESS_TIME_DEFAULT);
		seekBarSpace.setThumbWithText(SEEKBAR_SPACE_DEFAULT_STR);
		seekBarTime.setThumbWithText(SEEKBAR_TIME_DEFAULT_STR);

		myPub.setSearchSpaceDistance(SEARCH_CRITERIA_SPACE_DISTANCE_DEFAULT);
		myPub.setSearchTimeDistance(SEARCH_CRITERIA_TIME_DISTANCE_DEFAULT);
		myPub.setAvailable(true);
	}

	/**
	 * 初始化地图
	 */
	private void initMap() {

		// 地图设置
		UiSettings uiSettings = aMap.getUiSettings();
		uiSettings.setZoomControlsEnabled(false);
		uiSettings.setRotateGesturesEnabled(false);
		uiSettings.setZoomGesturesEnabled(false);
		uiSettings.setScrollGesturesEnabled(true);
		uiSettings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_RIGHT);

		aMap.setMapType(AMap.MAP_TYPE_NORMAL);

	}

	/**
	 * 初始化taPicker
	 */
	private void initTaPicker() {

		// getsize的方法需要api level 13,所以没有使用。
		@SuppressWarnings("deprecation")
		int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
		int margin = (int) getResources().getDimension(
				R.dimen.main_hpicker_subview_margin);

		int height = (int) (screenWidth / HPICKER_ITEM_CNT);

		// 根据屏幕的宽度来设置picker的宽度和高度。
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) taPicker
				.getLayoutParams();
		lp.height = height;
		lp.width = screenWidth;
		taPicker.setLayoutParams(lp);
		
		int maskBorderWidth = (int) getResources().getDimension(
				R.dimen.main_hpicker_mask_border_width);
//		int maskBorderWidth = 0;

		taPicker.init(HPICKER_ITEM_CNT, R.drawable.main_hpicker_mask_border, 
				RelativeLayout.ALIGN_PARENT_LEFT, maskBorderWidth, margin, IwantUApp.msgHandler,
				IwantUApp.MSG_TO_MAIN_HPICKER_CHANGED);

	}
	/**
	 * 重置地图
	 */
	private void restoreMap(){		
		aMap.clear();
		markerList.clear();
	}
	
	/**
	 * 加入表示自己的marker
	 */
	private void addMeToMap(){
		Marker markerMe = aMap.addMarker(new MarkerOptions()
		.position(new LatLng(myPub.getLatitude(), myPub
				.getLongitude()))
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.main_marker_me))
		.title("title"));
		markerMe.setObject(new PubWithMem(myPub));
		markerList.add(markerMe);
	}
	
	/**
	 * 重置taPicker
	 */
	private void restoreTaPicker() {
		taCount = 0;
		hPickerSubViewList.clear();
		taPicker.removeAllItemViews();
		Drawable myDrawable;
		String portraitName = member.getPortraitFileName();
		if (portraitName == null
				|| portraitName.equals(IwantUApp.CONS_PORTRAIT_DEFAULT_NAME)) {
			hpicker_subview_me = createTaPickerSubView(
							R.drawable.portrait_default_me,
							(int) getResources().getDimension(
									R.dimen.main_hpicker_subview_margin), member.getId(),
							0, member.getPortraitFileName(), this);
		} else {
			myDrawable = getDrawableByName(portraitName);
			hpicker_subview_me = createTaPickerSubView(
					myDrawable,
					(int) getResources().getDimension(
							R.dimen.main_hpicker_subview_margin), member.getId(),
					0, member.getPortraitFileName(), this);
		}

		hPickerSubViewList.add(hpicker_subview_me);
		taPicker.addSubView(hpicker_subview_me);
		taCount++;

		createTaPickerDummySubView();
	}

	/**
	 * 初始化actionbar的菜单。
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_actionbar, menu);
		return super.onCreateOptionsMenu(menu);
	}

	private void initProgressDialog() {
		progressDialog = new ProgressDialog(this, R.style.progressdialog);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				if (publishAndSearchTask != null
						&& !publishAndSearchTask.isCancelled()) {
					publishAndSearchTask.cancel(true);
				}
			}
		});
	}

	@SuppressLint("NewApi")
	private void initAlertDialog() {
		AlertDialog.Builder alertDialogBuilder;
		if (Build.VERSION.SDK_INT >= 11) {
			alertDialogBuilder = new AlertDialog.Builder(this,
					R.style.progressdialog);
		} else {
			alertDialogBuilder = new AlertDialog.Builder(this);
		}
		alertDialogBuilder
				.setMessage(R.string.main_alertdialog_askifwork)
				.setCancelable(false)
				.setPositiveButton(R.string.main_alertdialog_bt_yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								IwantUApp.msgHandler
										.sendEmptyMessage(IwantUApp.MSG_TO_MAIN_IT_IS_TA);
								dialog.dismiss();
							}
						})
				.setNegativeButton(R.string.main_alertdialog_bt_no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								IwantUApp.msgHandler
										.sendEmptyMessage(IwantUApp.MSG_TO_MAIN_NOT_TA);
								dialog.dismiss();
							}
						});

		alertDialog = alertDialogBuilder.create();
	}

	// /////////////////////////////////////////////////////////////////////////
	// taPicker操作区域
	// TA选择操作区域
	/**
	 * 通过image_res_id创建 taPicker的子视图。
	 * 
	 * @param image_res_id
	 *            image id
	 * @param taID
	 *            , ta id
	 * @param index
	 *            , 显示位置
	 * @param onClickListener
	 * @return
	 */
	private View createTaPickerSubView(int image_res_id, int margin,
			String taID, int index, String portraitName,
			OnClickListener onClickListener) {
		View view = createTaPickerSubView(null, margin, taID, index,
				portraitName, onClickListener);
		ImageView iv = (ImageView) view
				.findViewById(R.id.main_hpicker_subview_iv);
		iv.setImageResource(image_res_id);
		return view;
	}

	/**
	 * 通过drawable创建hsv的子视图。
	 * 
	 * @param drawable
	 * @param margin
	 *            ， subview左右两边的间隔。
	 * @param taID
	 * @param index
	 *            subview在Hpicker中的编号，me为0，依次递增。
	 * @param onClickListener
	 * @return RelativeLayout
	 */
	private View createTaPickerSubView(Drawable drawable, int margin,
			String taID, int index, String portraitName,
			OnClickListener onClickListener) {

		RelativeLayout rl = (RelativeLayout) getLayoutInflater().inflate(
				R.layout.main_hpicker_subview, null);
		RelativeLayout.LayoutParams lp_rl = new RelativeLayout.LayoutParams(
				taPicker.getItemWidth(), taPicker.getItemHeight());
		lp_rl.setMargins(margin, 0, margin, 0);
		rl.setLayoutParams(lp_rl);

		ImageView iv = (ImageView) rl
				.findViewById(R.id.main_hpicker_subview_iv);
		if (drawable != null){
			iv.setImageDrawable(drawable);
		}	
		iv.setOnClickListener(onClickListener);

		RelativeLayout.LayoutParams lp_iv = new RelativeLayout.LayoutParams(
				taPicker.getItemWidth(), taPicker.getItemWidth());
		iv.setLayoutParams(lp_iv);

		TextView tv_index = (TextView) rl
				.findViewById(R.id.main_hpicker_subview_tv_index);
		tv_index.setText(Integer.toString(index));

		TextView tv_portraitName = (TextView) rl
				.findViewById(R.id.main_hpicker_subview_tv_portrait_name);
		tv_portraitName.setText(portraitName);

		TextView tv_taID = (TextView) rl
				.findViewById(R.id.main_hpicker_subview_tv_taID);
		tv_taID.setText(taID);
		return rl;

	}

	/**
	 * 4 dummy hsvlayout to make sure the last ta could be dragged to the left
	 * most ;
	 */
	private void createTaPickerDummySubView() {
		for (int i = 1; i <= HPICKER_DUMMY_ITEM_CNT; i++) {
			taPicker.addSubView(
					createTaPickerSubView(
							R.drawable.main_hpicker_vacuum,
							(int) getResources().getDimension(
									R.dimen.main_hpicker_subview_margin),
							"dummy", -i, "null", null), taPicker.getItemCnt());
		}
	}

	// ////////////////////////////////////////////////////////////////////////
	// 与服务器交互的任务
	/**
	 * 获取头像的任务。
	 * 
	 * @author tom @date 2014-2-11
	 * 
	 */
	private class GetPortraitTask extends AsyncTask<MediaType, Void, byte[]> {

		private String taID;
		private String portraitFileName;

		@Override
		protected void onPreExecute() {
		}

		public GetPortraitTask(String taID, String portraitFileName) {
			super();
			this.taID = taID;
			this.portraitFileName = portraitFileName;
		}

		@Override
		protected byte[] doInBackground(MediaType... params) {
			try {
				final String url = app.getServerBaseURL() + "/portraits/{taID}";
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
				restTemplate.getMessageConverters().add(
						new StringHttpMessageConverter());
				restTemplate.getMessageConverters().add(
						new ByteArrayHttpMessageConverter());
				restTemplate.getMessageConverters().add(
						new ByteArrayHttpMessageConverter());
				byte[] response = restTemplate.getForObject(url, byte[].class,
						taID);
				return response;
			} catch (Exception e) {
				Log.e("tag", e.getMessage());
			}
			return null;
		}

		protected void onPostExecute(byte[] bytes) {
			if (null == bytes) {
				return;
			}
			Bundle b = new Bundle();
			b.putByteArray("portraitBytes", bytes);
			b.putString("portraitFileName", portraitFileName);
			b.putString("taID", taID);
			Message msg = new Message();
			msg.setData(b);
			msg.what = IwantUApp.MSG_TO_MAIN_GOT_PORTRAIT_BYTES;
			IwantUApp.msgHandler.sendMessage(msg);
		}
	}

	/**
	 * 发布并且搜索
	 * 
	 * @author tom @date 2014-2-11
	 * 
	 */
	private class PublishAndSearchTask extends
			AsyncTask<MediaType, Void, PubWithMemList> {
		private Publication pub;

		@Override
		protected void onPreExecute() {
			progressDialog.setMessage(getResources().getString(
					R.string.main_toast_publishandsearch));
			progressDialog.show();
			pub = new Publication(myPub);
		}

		@Override
		protected PubWithMemList doInBackground(MediaType... params) {
			try {
				final String url = app.getServerBaseURL() + "/publish";
				HttpHeaders requestHeaders = new HttpHeaders();
				requestHeaders.setContentType(MediaType.APPLICATION_XML);
				requestHeaders.set("Connection", "Close");
				HttpEntity<Publication> requestEntity = new HttpEntity<Publication>(
						pub, requestHeaders);
				Serializer serializer = new Persister(new AnnotationStrategy());
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
				restTemplate.getMessageConverters().add(
						new StringHttpMessageConverter());
				restTemplate.getMessageConverters().add(
						new SimpleXmlHttpMessageConverter(serializer));
				restTemplate.getMessageConverters().add(
						new ByteArrayHttpMessageConverter());
				ResponseEntity<PubWithMemList> response = restTemplate
						.exchange(url, HttpMethod.POST, requestEntity,
								PubWithMemList.class);
				return (PubWithMemList) response.getBody();
			} catch (Exception e) {
				// 连接服务器超时
				if (e instanceof ConnectTimeoutException
						|| e.getCause() instanceof ConnectTimeoutException) {
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_MAIN_EX_CONN_TIMEOUT);
					// 未知错误
				} else {
					Log.e("aaaa", e.getMessage());
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_MAIN_EX_UNKNOWN);

				}
				// 其它异常
				return null;
			}
		}

		protected void onPostExecute(PubWithMemList pmList) {
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}

			if ((pmList == null) || (pmList.getPList() == null)) {
				return;
			}
			Bundle b = new Bundle();
			b.putSerializable("pmList", pmList);
			Message msg = new Message();
			msg.setData(b);
			msg.what = IwantUApp.MSG_TO_MAIN_GOT_PLIST;
			IwantUApp.msgHandler.sendMessage(msg);

		}
	}

	/**
	 * 发送操作结果任务。
	 * 
	 * @author tom @date 2014-2-11
	 * 
	 */
	private class ActionResultTask extends AsyncTask<MediaType, Void, String> {

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected String doInBackground(MediaType... params) {
			try {
				final String url = app.getServerBaseURL() + "/actionresult";
				HttpHeaders requestHeaders = new HttpHeaders();
				requestHeaders.setContentType(MediaType.APPLICATION_XML);
				HttpEntity<ActionResult> requestEntity = new HttpEntity<ActionResult>(
						actionResult, requestHeaders);
				requestHeaders.set("Connection", "Close");
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
				restTemplate.getMessageConverters().add(
						new StringHttpMessageConverter());
				restTemplate.getMessageConverters().add(
						new SimpleXmlHttpMessageConverter());
				ResponseEntity<String> response = restTemplate.exchange(url,
						HttpMethod.POST, requestEntity, String.class);
				return (String) response.getBody();
			} catch (Exception e) {
				// 对actionresult的异常不做任何处理。
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			Log.i("main", "action result done");
		}
	}

	// /////////////////////////////////////////////////////////////////////
	// 接口实现和方法重构区域

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case REQUEST_CODE_MEMBERINFO:
			member = (Member) data
					.getSerializableExtra(IwantUApp.ONTOLOGY_MEMBER);
			
			ImageView iv = (ImageView) hpicker_subview_me
					.findViewById(R.id.main_hpicker_subview_iv);
			TextView tv = (TextView) hpicker_subview_me
					.findViewById(R.id.main_hpicker_subview_tv_portrait_name);
		
			Drawable myDrawable = Drawable.createFromPath(app
					.getPortraitFilesDir()
					+ File.separator
					+ member.getPortraitFileName());
			iv.setImageDrawable(myDrawable);
			tv.setText( member.getPortraitFileName());
			break;
		case REQUEST_CODE_FEEDBACK:
			break;
		case REQUEST_CODE_ABOUTUS:
			break;
		// 从iwant页面返回，获得新的publicaton，发布并且搜索。
		case REQUEST_CODE_IWANT:
			break;
		case REQUEST_CODE_CALL:
			Log.d("main onActivityResult" , "REQUEST_CODE_CALL");
			alertDialog.show();
			break;
		case REQUEST_CODE_SMS:
			break;
		default:
		}
	}

	// infowindowAdapter

	public View getInfoWindow(Marker marker) {
		if (marker.getObject() == null) {
			return null;
		}
		PubWithMem taPubWithMem = (PubWithMem) marker.getObject();
		
		boolean isInproperTA = false;
		for (PubWithMem pm : inproperTaList) {
			if (pm.getId().equals(taPubWithMem.getId())) {
				isInproperTA = true;
				break;
			}
		}

		final View view = getLayoutInflater().inflate(R.layout.main_infowindow,
				null);

		ImageView iv = (ImageView) view
				.findViewById(R.id.main_infoWindow_iv_portrait);
		TextView tv_gender_and_age = (TextView) view
				.findViewById(R.id.main_infoWindow_tv_gender_and_age);
		TextView tv_space_and_time_and_dest = (TextView) view
				.findViewById(R.id.main_infoWindow_tv_space_and_time_and_dest);
		Button bt_call = (Button) view
				.findViewById(R.id.main_infoWindow_bt_call);
		Button bt_sms = (Button) view.findViewById(R.id.main_infoWindow_bt_sms);
		bt_call.setOnClickListener(new OnClickListener_call(taPubWithMem));
		bt_sms.setOnClickListener(new OnClickListener_sms(taPubWithMem));

		if (isInproperTA) {
			bt_call.setText(R.string.main_infowindow_bt_called);
			bt_call.setEnabled(false);
			bt_sms.setText(R.string.main_infowindow_bt_smsed);
			bt_sms.setEnabled(false);
		}

		int width = (int) getResources().getDimension(
				R.dimen.main_infowindow_portrait_width);
		int height = (int) getResources().getDimension(
				R.dimen.main_infowindow_portrait_height);
		Drawable d = app.getDrawableFromFileName(
				taPubWithMem.getPortraitFileName(), width, height);

		if (null == d) {
			d = getResources().getDrawable(R.drawable.portrait_default_ta);
		}
		iv.setImageDrawable(d);

		String gender;
		int age;
		if (taPubWithMem.getGender() != null
				&& taPubWithMem.getGender()
						.equals(IwantUApp.CONS_GENDER_FEMALE)) {
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

		return view;
	}

	public View getInfoContents(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	// OnMapLoadedListener
	public void onMapLoaded() {
		// TODO Auto-generated method stub
	}

	/**
	 * 生成space和time的信息，在infowindow中显示
	 * 
	 * @param myPub
	 * @param taPub
	 * @return
	 */
	private String getSpaceAndTimeInfo(Publication myPub, Publication taPub) {
		long timeDistance = myPub.getDatetime() / 60000- taPub.getDatetime() / 60000;
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

	// AMapLocationListener
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub

	}

	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	public void onLocationChanged(AMapLocation location) {
		// TODO Auto-generated method stub
		if (location != null) {
			myPub.setLatitude(location.getLongitude());
			myPub.setLatitude(location.getLatitude());
		}
	}

	// 用户操作行为处理。
	/**
	 * 用户点击了后退键。
	 */
	@Override
	public void onBackPressed() {
		// 把本acitity从locmananger的更新列表中删除。
		mAMapLocManager.removeUpdates(this);
		// 把本activity从APP中删除。
		IwantUApp.removeActivity(this);
		
		//关闭这个activity，然后从iwantactivity重新进入。
		this.finish();
		
		// super的处理方式是结束该activity
		super.onBackPressed();
	}

	// onclicklistner接口
	/**
	 * 处理点击事件
	 */
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.main_bt_xunta:
			restoreMap();
			restoreTaPicker();
			addMeToMap();

			//移动视图
			aMap.moveCamera(CameraUpdateFactory
					.newCameraPosition(new CameraPosition.Builder()
							.target(new LatLng(myPub.getLatitude(), myPub
									.getLongitude())).zoom(currentZoom)
							.tilt(MAP_TILT).build()));
			
			publishAndSearchTask = new PublishAndSearchTask();
			publishAndSearchTask.execute();
			break;
		case R.id.main_bt_start:
			rl_guide.setVisibility(View.INVISIBLE);
			app.setUserIsNotRookie();
			break;
		default:
		}
	}

	/**
	 * 用户点击actionbar的菜单时的
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		Intent i;
		switch (item.getItemId()) {
		case R.id.main_actionbar_memberinfo:
			i = new Intent();
			i.putExtra(IwantUApp.ONTOLOGY_MEMBER, (Parcelable) member);
			i.setClass(this, MemberInfoActivity.class);
			startActivityForResult(i, REQUEST_CODE_MEMBERINFO);
			return true;
		case R.id.main_actionbar_more:
			return true;
		case R.id.main_actionbar_aboutus:
			i = new Intent();
			i.setClass(this, AboutUsActivity.class);
			startActivityForResult(i, REQUEST_CODE_ABOUTUS);
			return true;
		case R.id.main_actionbar_feedback:
			i = new Intent();
			i.putExtra(IwantUApp.ONTOLOGY_TAID, member.getId());
			i.setClass(this, FeedBackActivity.class);
			startActivityForResult(i, REQUEST_CODE_FEEDBACK);
			return true;
		case R.id.main_actionbar_exit:
			app.onTerminate();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * 消息处理
	 * 
	 * @param msg
	 */
	public void handleMsg(Message msg) {
		switch (msg.what) {

		// HSV发生改变，显示选中的TA的infowindow.
		case IwantUApp.MSG_TO_MAIN_HPICKER_CHANGED:
			taIndex = msg.getData().getInt(MyHorizontalPicker.MSG_KEY);

			updateHpickerPortrait(taIndex);
			updateMap(taIndex);
			break;
		case IwantUApp.MSG_TO_MAIN_GOT_PLIST:
			PubWithMemList pmList = (PubWithMemList) msg.getData()
					.getSerializable("pmList");
			PubWithMem pm0 = pmList.getPList().get(0);

			// 如果登录失效，返回login页面
			if (pm0.getId().equals(
					String.format("%02x", IwantUApp.RESPONSE_CODE_LOGIN_FAIL))) {
				Toast.makeText(getApplicationContext(),
						R.string.toast_need_relogin, Toast.LENGTH_SHORT).show();
				Intent i = new Intent();
				i.setClass(this, LoginActivity.class);
				startActivity(i);
				break;
			}
			// pm0为自己的发布，用户获取发布时间，发布时间由服务器设置，以避免不同手机的时间不同的情况。
			myPub.setDatetime(pm0.getDatetime());
			// 把自己移除掉
			taList =  pmList.getPList();
			taList.remove(0);
			
			Log.d("main handle msg", "pmList_'s size is:" + taList.size());
			
			//没有合适的TA
			if (taList.size() == 0){
				Toast.makeText(this, R.string.main_toast_not_found_ta, Toast.LENGTH_SHORT).show();
				break;
			}
			
			// 对结果排序
			// 按照时间越近，距离越近， 排名越靠前的顺序排序
			Collections.sort(taList, new Comparator<PubWithMem>(){
				public int compare(PubWithMem p1, PubWithMem p2) {
					// TODO Auto-generated method stub
					// 在一分钟之内认为相同
					if ((p1.getDatetime() / 60000) > (p2.getDatetime() / 60000)){
						return -1;
					}
					if ((p1.getDatetime() / 60000) < (p2.getDatetime() / 60000)){
						return 1;
					}
					if (p1.getDistance() < p2.getDistance()){
						return -1;
					}
					if (p1.getDistance() > p2.getDistance()){
						return 1;
					}
					return 0;
				}
			});
			
			for (PubWithMem pm : taList) {
				updateTaList(pm);
			}
			updateHpickerPortrait(0);
			
			//最后在加上一个自己，以保证自己的marker在所有marker之上。
			//当前高德地图对于marker不提供z轴的函数，只能做如此处理。20140325
			addMeToMap();
			
			if (app.isUserRookie()){
				rl_guide.setVisibility(View.VISIBLE);
			}			
			break;
		case IwantUApp.MSG_TO_MAIN_EX_CONN_TIMEOUT:
			Toast.makeText(getApplicationContext(), R.string.ex_conn_timeout,
					Toast.LENGTH_SHORT).show();
			break;
		case IwantUApp.MSG_TO_MAIN_EX_UNKNOWN:
			Toast.makeText(getApplicationContext(), R.string.ex_unknown,
					Toast.LENGTH_SHORT).show();
			break;
		case IwantUApp.MSG_TO_MAIN_GOT_PORTRAIT_BYTES:
			byte[] bytes = msg.getData().getByteArray("portraitBytes");
			String portraitFileName = msg.getData().getString(
					"portraitFileName");
			String taID = msg.getData().getString("taID");
			// 保存文件
			app.createPortraitFile(portraitFileName, bytes);
			// 更新视图
			updatePortraitByPub(taID, portraitFileName);
		case IwantUApp.MSG_TO_MAIN_IT_IS_TA:
			actionResult.setResult(ActionResult.RESULT_SUCCESS);
			new ActionResultTask().execute();
			break;
		case IwantUApp.MSG_TO_MAIN_NOT_TA:
			inproperTaList.add(currentTa);
			actionResult.setResult(ActionResult.RESULT_FAIL);
			new ActionResultTask().execute();
			break;
		default:
		}
	}

	/**
	 * 点击actionbar的logo的处理方法，与setDisplayHomeAsUpEnabled一起使用。
	 */
	@Override
	public Intent getSupportParentActivityIntent() {
//		Intent i = new Intent();
//		i.setClass(this, IWantActivity.class);
//		i.putExtra(IwantUApp.ONTOLOGY_MEMBER, (Parcelable) member);
//	    return i;
		this.onBackPressed();
		return null;
	}
	// /////////////////////////////////////////////////////////////////////////
	// 其它方法

	/**
	 * 处理收到的发布信息。主要有：更新数据，在地图上加入marker，在taPicker的尾端（在dummy之前）插入视图。
	 * 
	 * @param taPub
	 */
	private void updateTaList(PubWithMem taPub) {
		// update marker list;
		LatLng taLatLng = new LatLng(taPub.getLatitude(), taPub.getLongitude());
		Marker taMarker = aMap.addMarker(new MarkerOptions()
				.position(taLatLng)
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.main_marker_ta))
				.title(taPub.getId()));
		taMarker.setObject(taPub);
		markerList.add(taMarker);

		// 获取头像文件
		String portraitName = taPub.getPortraitFileName();
		if (portraitName != null) {
			File portraitFile = new File(app.getPortraitFilesDir().getPath()
					+ File.separator + portraitName);
			// get the portrait file from server
			if (!portraitFile.exists()) {
				new GetPortraitTask(taPub.getTaID(),
						taPub.getPortraitFileName()).execute();
			}
		}

		taCount++;

		int margin = (int) getResources().getDimension(
				R.dimen.main_hpicker_subview_margin);
		View subView = (View) createTaPickerSubView(
				R.drawable.main_hpicker_subview_blank, margin, taPub.getTaID(),
				taCount, portraitName, this);
		hPickerSubViewList.add(subView);

		// add sub view before the dummy views;
		taPicker.addSubView(subView, taPicker.getItemCnt()
				- HPICKER_DUMMY_ITEM_CNT);
	}

	/**
	 * 用fileName所代表的图像文件来更新hPicker中taID所对应的头像 该方法用于用户获得Portrait头像后。
	 * 
	 * @param taID
	 * @param fileName
	 */
	public void updatePortraitByPub(String taID, String fileName) {
		Iterator<View> it = hPickerSubViewList.iterator();
		while (it.hasNext()) {
			View subview = it.next();
			TextView tv_taID = (TextView) subview
					.findViewById(R.id.main_hpicker_subview_tv_taID);
			ImageView iv = (ImageView) subview
					.findViewById(R.id.main_hpicker_subview_iv);
			TextView tv_index = (TextView) subview
					.findViewById(R.id.main_hpicker_subview_tv_index);
			int index = Integer.parseInt(tv_index.getText().toString());
			if (tv_taID.getText().equals(taID) && index < HPICKER_ITEM_CNT) {
				String filePath = app.getPortraitFilesDir() + File.separator
						+ fileName;
				Bitmap bmp = AppUtil
						.decodeBitmapFromFile(filePath,
								taPicker.getSubViewWidth(),
								taPicker.getSubViewHeight());
				BitmapDrawable bd = new BitmapDrawable(getResources(), bmp);
				iv.setImageDrawable(bd);
			}
		}

	}

	public void startMemberinfoActivity() {
		Intent i = new Intent();
		i.putExtra(IwantUApp.ONTOLOGY_MEMBER, (Parcelable) member);
		i.setClass(this, MemberInfoActivity.class);
		startActivityForResult(i, REQUEST_CODE_MEMBERINFO);
	}

	/**
	 * 当用户滑动Hpicker的时候，更新视图范围内的subview的头像，其余不显示的头像回收内存
	 * 
	 * @param hPickerItemIndex
	 *            , item 的index，起始值为0.
	 */
	public void updateHpickerPortrait(int hPickerItemIndex) {
		int i;
		View subview;
		ImageView iv;
		// hPickerItemIndex之前的资源释放掉。
		for (i = 0; i < hPickerItemIndex; i++) {
			subview = hPickerSubViewList.get(i);
			iv = (ImageView) subview.findViewById(R.id.main_hpicker_subview_iv);
			AppUtil.recycleImageViewBitmap(iv);
			iv.setImageResource(R.drawable.main_hpicker_subview_blank);
		}
		// hPickerItemIndex到hPickerItemIndex+HPICKER_ITEM_CNT之间的显示头像。
		for (i = hPickerItemIndex; (i < hPickerItemIndex + HPICKER_ITEM_CNT)
				&& (i < taCount); i++) {
			subview = hPickerSubViewList.get(i);
			iv = (ImageView) subview.findViewById(R.id.main_hpicker_subview_iv);
			TextView tv = (TextView) subview
					.findViewById(R.id.main_hpicker_subview_tv_portrait_name);
			String portraitName = tv.getText().toString();
			if (i == 0
					&& portraitName
							.equals(IwantUApp.CONS_PORTRAIT_DEFAULT_NAME)) {
				iv.setImageResource(R.drawable.portrait_default_me);
				continue;
			}
			Drawable drawable = getDrawableByName(portraitName);
			iv.setImageDrawable(drawable);
		}
		// hPickerItemIndex + HPICKER_ITEM_CNT 之后的图片资源释放掉。
		for (i = hPickerItemIndex + HPICKER_ITEM_CNT; i < taCount; i++) {
			subview = hPickerSubViewList.get(i);
			iv = (ImageView) subview.findViewById(R.id.main_hpicker_subview_iv);
			AppUtil.recycleImageViewBitmap(iv);
			iv.setImageResource(R.drawable.main_hpicker_subview_blank);
		}
	}

	/**
	 * 根据hpicker的taindex来更新地图视图
	 * 
	 * @param taIndex
	 */
	private void updateMap(int taIndex) {
		Log.d("main, updateMap", "taIndex is:" + taIndex);
		if (taIndex == 0) {
			hideAllInfoWindow();
			CameraPosition myPos = new CameraPosition.Builder()
					.target(new LatLng(myPub.getLatitude(), myPub
							.getLongitude())).zoom(currentZoom).build();
			aMap.moveCamera(CameraUpdateFactory.newCameraPosition(myPos));

		} else {
			Marker m = markerList.get(taIndex);
			PubWithMem taPub = (PubWithMem) m.getObject();

			// 修正屏幕中心的纬度，使得infowindow可以完全显示在屏幕上。在一些小屏幕手机上，Infowindow不能够完全被显示
			TypedValue scrollByValue = new TypedValue();
			getResources().getValue(R.dimen.main_map_movecamera_scrollby_y,
					scrollByValue, true);
			float y = AppUtil.dip2px(this, scrollByValue.getFloat());
			float latitude_y = aMap.getScalePerPixel() * y
					/ METERS_PER_LATITUDE;

			Log.d("main, updateMap", "ta latitude:" + taPub.getLatitude()
					+ ", ta longitude:" + taPub.getLongitude());
			aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(taPub
					.getLatitude() + latitude_y, taPub.getLongitude())));

			m.showInfoWindow();
		}
	}

	private Drawable getDrawableByName(String portraitName) {
		Drawable drawable;
		File portraitFile = new File(app.getPortraitFilesDir().getPath()
				+ File.separator + portraitName);
		if (portraitName == null || portraitFile.isDirectory()
				|| !portraitFile.exists()) {
			drawable = getResources().getDrawable(
					R.drawable.portrait_default_ta);
		} else {
			drawable = app.getDrawableFromFile(portraitFile,
					taPicker.getSubViewWidth(), taPicker.getSubViewHeight());
		}
		return drawable;
	}

	private void hideAllInfoWindow() {
		for (Marker m : markerList) {
			m.hideInfoWindow();
		}
	}

	// ///////////////////////////////////////////////////////////////////////////
	// 内部类

	/**
	 * 处理打入的电话，暂时用不上。20140325
	 * 监听电话状态。用户打完咨询电话之后，询问用户是否拼车成功。 date: 20131220
	 */
	private class PhoneCallListener extends PhoneStateListener {
		
		private boolean isPhoneCallMade =false;
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {

			Log.d("main PhoneCallListener", "incomingNumber is:" + incomingNumber + ", state is:" + state);
			if (TelephonyManager.CALL_STATE_RINGING == state) {
			}
			if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
				isPhoneCallMade = true;
			}
			if (TelephonyManager.CALL_STATE_IDLE == state) {
				Log.d("main PhoneCallListener", "CALL_STATE_IDLE, isPhoneCallMade is:" + isPhoneCallMade + ", isCurrentTaCalled is:" + isPhoneCallMadeToCurrentTa);
				if (isPhoneCallMade && isPhoneCallMadeToCurrentTa) {
					alertDialog.show();
					isPhoneCallMade = false;
					isPhoneCallMadeToCurrentTa = false;
				}
			}
		}
	}

	/**
	 * 时间seekbar变更处理。
	 * 
	 * @author tom @date 2014-2-11
	 * 
	 */
	private class OnSeekBarChangeListener_time implements
			OnSeekBarChangeListener {
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			int i = seekBar.getProgress();
			String searchTimeDistanceStr;
			switch (i) {
			case 0:
				myPub.setSearchTimeDistance(SEARCH_CRITERIA_TIME_DISTANCE_LVL0);
				searchTimeDistanceStr = SEARCH_CRITERIA_TIME_DISTANCE_LVL0_STR;
				break;
			case 1:
				myPub.setSearchTimeDistance(SEARCH_CRITERIA_TIME_DISTANCE_LVL1);
				searchTimeDistanceStr = SEARCH_CRITERIA_TIME_DISTANCE_LVL1_STR;
				break;
			case 2:
				myPub.setSearchTimeDistance(SEARCH_CRITERIA_TIME_DISTANCE_LVL2);
				searchTimeDistanceStr = SEARCH_CRITERIA_TIME_DISTANCE_LVL2_STR;
				break;
			default:
				myPub.setSearchTimeDistance(SEARCH_CRITERIA_TIME_DISTANCE_LVL1);
				searchTimeDistanceStr = SEARCH_CRITERIA_TIME_DISTANCE_LVL1_STR;
				break;
			}
			((SeekBarWithText) seekBar).setThumbWithText(searchTimeDistanceStr);

		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}
	}

	/**
	 * 空间seekbar 变更处理。
	 * 
	 * @author tom @date 2014-2-11
	 * 
	 */
	private class OnSeekBarChangeListener_space implements
			OnSeekBarChangeListener {

		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {

			int i = seekBar.getProgress();
			CameraPosition myPos;
			String searchSpaceDistanceStr;
			switch (i) {
			case 0:
				currentZoom = MAP_ZOOM_LVL0;
				myPub.setSearchSpaceDistance(SEARCH_CRITERIA_SPACE_DISTANCE_LVL0);
				searchSpaceDistanceStr = SEEKBAR_SPACE_LVL0_STR;
				break;
			case 1:
				currentZoom = MAP_ZOOM_LVL1;
				myPub.setSearchSpaceDistance(SEARCH_CRITERIA_SPACE_DISTANCE_LVL1);
				searchSpaceDistanceStr = SEEKBAR_SPACE_LVL1_STR;
				break;
			case 2:
				currentZoom = MAP_ZOOM_LVL2;
				myPub.setSearchSpaceDistance(SEARCH_CRITERIA_SPACE_DISTANCE_LVL2);
				searchSpaceDistanceStr = SEEKBAR_SPACE_LVL2_STR;
				break;
			default:
				currentZoom = MAP_ZOOM_DEFAULT;
				myPub.setSearchSpaceDistance(SEARCH_CRITERIA_SPACE_DISTANCE_LVL1);
				searchSpaceDistanceStr = SEEKBAR_SPACE_LVL1_STR;
			}
			LatLng myLatLng = new LatLng(myPub.getLatitude(),
					myPub.getLongitude());
			myPos = new CameraPosition.Builder().target(myLatLng)
					.zoom(currentZoom).tilt(MAP_TILT).build();

			aMap.moveCamera(CameraUpdateFactory.newCameraPosition(myPos));

			((SeekBarWithText) seekBar)
					.setThumbWithText(searchSpaceDistanceStr);
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}

	}

	/**
	 * 用户点击打电话按钮的处理
	 * 
	 * @author tom @date 2014-2-14
	 * 
	 */
	public class OnClickListener_call implements OnClickListener {
		private PubWithMem taPub;

		public OnClickListener_call(PubWithMem taPub1) {
			this.taPub = taPub1;
		}

		public void onClick(View v) {
			Uri uri = Uri.parse("tel:" + taPub.getPhoneNum());
			Intent intent = new Intent(Intent.ACTION_DIAL, uri);
			startActivityForResult(intent, REQUEST_CODE_CALL);
			
			currentTa = taPub;
			isPhoneCallMadeToCurrentTa = true;
			
			actionResult.setTaID(taPub.getTaID());
			actionResult.setActionTime(System.currentTimeMillis());
			actionResult.setTheAction(ActionResult.ACTION_CALL);
		}
	}

	/**
	 * 用户点击发短信的行为处理。
	 * 
	 * @author tom @date 2014-2-14
	 * 
	 */
	public class OnClickListener_sms implements OnClickListener {
		private PubWithMem taPub;
		private String smsContentFormatted = getResources().getString(R.string.sms_content_formatted);

		public OnClickListener_sms(PubWithMem taPub1) {
			this.taPub = taPub1;
		}

		public void onClick(View v) {
			currentTa = taPub;

			Uri uri = Uri.parse("smsto:" + taPub.getPhoneNum());
			Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
			intent.putExtra("sms_body", String.format(smsContentFormatted, myPub.getDestName()));
			startActivityForResult(intent, REQUEST_CODE_SMS);

			actionResult.setTaID(taPub.getTaID());
			actionResult.setActionTime(System.currentTimeMillis());
			actionResult.setTheAction(ActionResult.ACTION_SMS);
			new ActionResultTask().execute();
		}
	}

}
