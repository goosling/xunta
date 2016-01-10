/**
 * iwant页面。
 * 用户在该页面获取自己当前的位置信息，并选择目的地。两种方法：1，在地图上选择。2，选择常用地点。常用地点从服务器获得。
 * 20140107
 * 
 */
package com.ixunta.client;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.http.conn.ConnectTimeoutException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.xml.SimpleXmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;

import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.geocoder.StreetNumber;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.Tip;
import com.amap.api.services.help.Inputtips.InputtipsListener;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.road.Crossroad;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.WalkRouteResult;
import com.ixunta.R;

import com.ixunta.client.db.Destination;
import com.ixunta.client.db.DestinationList;

import com.ixunta.client.db.Member;

import com.ixunta.client.db.Publication;
import com.ixunta.client.util.AppUtil;
import com.ixunta.client.util.MapUtil;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class IWantActivity extends Activity implements AMapLocationListener,
		OnMapLoadedListener, OnMapClickListener, OnGeocodeSearchListener,
		OnRouteSearchListener, OnPoiSearchListener, OnMarkerClickListener,
		TextWatcher, OnClickListener, InfoWindowAdapter, OnEditorActionListener{

	// 其它
	private DestinationTask destinationTask;
	private IwantUApp app;
	private LocationManagerProxy mAMapLocManager;

	private PoiSearch.Query poiQuery;
	private RegeocodeQuery reGeoQuery;
	private ProgressDialog progressDialog;

	private long lastPressedTime;

	// 页面布局。
	// 显示目的地信息。同时需要显示路线距离和时长。
	private TextView tv_map_info;
	// 常用地址区域的提示信息。
	private TextView tv_common_dest;
	private AutoCompleteTextView actv_search_keyword;
	private Button bt_search;
	private Button bt_xunta;
	private Button bt_refresh;
	private RadioGroup commDestRadioGroup;
	private View view_commDest;
	private MapView mapView;
	private AMap aMap;
	private Marker destMarker;
	private ArrayList<Marker> poiMarkerList;

	// 页面数据

	private ArrayList<Destination> dList;
	private ArrayList<RadioButton> rbList = new ArrayList<RadioButton>();
	private LatLonPoint myLatLng, destLatLng;
	// 目的地的名称
	private String destName;
	// 我的发布。将会传到mainactivity中。
	private Publication myPub;
	// 我的会员信息
	private Member member;
	// 路线查询
	private RouteSearch routeSearch;
	private GeocodeSearch geocoderSearch;
	// 用户当前的信息，经纬度、城市。
	private String myCityName;

	// 页面控制
	private int drivingMode = RouteSearch.DrivingDefault;
	// 地图显示的默认zoom
	public static final int MAP_ZOOM_DEFAULT = 13;
	public static boolean hasLocationUpdated = false;
	private int currentZoom = MAP_ZOOM_DEFAULT;

	// 位置更新的时间门槛，单位是毫秒，在requestLocationUpdates方法中使用。
	private static final int CONS_LOC_UPDATE_TIME_THRESHOLD = 60000;

	// 位置更新的距离门槛，单位是米，在requestLocationUpdates方法中使用。
	private static final int CONS_LOC_UPDATE_SPACE_THRESHOLD = 500;

	// 常用地址区域，每行显示的radio button的数量
	private static final int CONS_COMMON_DEST_RB_EACH_LINE = 3;

	// RegeocodeQuery构造函数中的参数，指搜索多大范围内的POI，单位是米。
	private static final float CONS_REGEOCODER_RADIUS = 200;

	// 每次搜索最多返回结果。
	private static final int CONS_SEARCH_ITEM_NUM = 3;

	public static final int PUB_SERVICECODE_DEFAULT = 0x1010000;

	// 当搜索结果只有一个POI时的ZOOM。
	private static final int CONS_ZOOM_ONE_POI = 17;
	
	// 路线视图下或者多POI视图下到屏幕边缘的空隙
	private static final int CONS_CAMERA_PADDING_TO_SCREEN = 48;

	// //////////////////////////////////////////////////////////////////////////////////////
	// Activity生命周期
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (IwantUApp) this.getApplication();
		IwantUApp.addActivity(this);

		// get all views
		setContentView(R.layout.iwant);
		bt_xunta = (Button) findViewById(R.id.iwant_bt_xunta);
		bt_refresh = (Button) findViewById(R.id.iwant_bt_refresh);
		tv_map_info = (TextView) findViewById(R.id.iwant_tv_map_info);
		tv_common_dest = (TextView) findViewById(R.id.iwant_ll2_tv);
		actv_search_keyword = (AutoCompleteTextView) findViewById(R.id.iwant_ll1_actv);
		bt_search = (Button) findViewById(R.id.iwant_bt_search);
		view_commDest = findViewById(R.id.iwant_ll_common_dest);
		commDestRadioGroup = (RadioGroup) findViewById(R.id.iwant_ll2_ll1_rg);
		commDestRadioGroup.setOrientation(RadioGroup.VERTICAL);

		bt_refresh.setOnClickListener(this);
		bt_xunta.setOnClickListener(this);
		actv_search_keyword.addTextChangedListener(this);
		bt_search.setOnClickListener(this);
		actv_search_keyword.setOnEditorActionListener(this);

		// 初始化地图。
		mapView = (MapView) findViewById(R.id.iwant_map);
		mapView.onCreate(savedInstanceState);// 此方法必须重写
		aMap = mapView.getMap();
		initMap();

		// 初始化其它显示区域
		tv_map_info.setVisibility(View.INVISIBLE);
		view_commDest.setVisibility(View.INVISIBLE);

		// initiate the data in the activity
		if (savedInstanceState != null) {
			member = savedInstanceState.getParcelable("member");
			myPub = savedInstanceState.getParcelable("myPub");
		} else {
			Intent i = this.getIntent();
			member = (Member) i.getSerializableExtra(IwantUApp.ONTOLOGY_MEMBER);
			myPub = new Publication();
			myPub.setTaID(member.getId());
			myPub.setServiceCode(PUB_SERVICECODE_DEFAULT);
			myPub.setSearchSpaceDistance(MainActivity.SEARCH_CRITERIA_SPACE_DISTANCE_DEFAULT);
			myPub.setSearchTimeDistance(MainActivity.SEARCH_CRITERIA_TIME_DISTANCE_DEFAULT);
			myPub.setLatitude(0);
			myPub.setLatitude(0);
		}

		destLatLng = new LatLonPoint(0, 0);
		poiMarkerList = new ArrayList<Marker>();

		mAMapLocManager = LocationManagerProxy.getInstance(this);

		// 开始定位
		startLocating();

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
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
		outState.putParcelable("member", member);
		outState.putParcelable("myPub", myPub);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	// 初始化区域
	/*
	 * 初始化地图
	 */
	private void initMap() {
		aMap.clear();

		// 地图设置
		UiSettings uiSettings = aMap.getUiSettings();
		uiSettings.setZoomControlsEnabled(false);
		uiSettings.setRotateGesturesEnabled(false);
		uiSettings.setZoomGesturesEnabled(true);
		uiSettings.setScrollGesturesEnabled(true);
		uiSettings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_RIGHT);

		routeSearch = new RouteSearch(this);
		routeSearch.setRouteSearchListener(this);
		geocoderSearch = new GeocodeSearch(this);
		geocoderSearch.setOnGeocodeSearchListener(this);

		aMap.setOnMapLoadedListener(this);
		aMap.setOnMapClickListener(this);
		aMap.setInfoWindowAdapter(this);
		aMap.setOnMarkerClickListener(this);
		moveMapCameraToStartPosition(0, 0);
	}

	private void initProgressDialog() {
		progressDialog = new ProgressDialog(this, R.style.progressdialog);
		progressDialog.setCancelable(true);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				mAMapLocManager.removeUpdates(IWantActivity.this);
				disableAllView();
				bt_refresh.setEnabled(true);
			}
		});
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////
	// 接口实现和方法重构区域
	// amaplocationlistner接口
	/**
	 * 处理用户地理位置发生变化的事件
	 */
	public void onLocationChanged(AMapLocation location) {

		// 定位成功，提示信息不再显示
		if (progressDialog.isShowing()) {
			progressDialog.dismiss();
		}

		// 一次性请求地理位置。
		mAMapLocManager.removeUpdates(this);

		if (location == null) {
			return;
		}

		myCityName = new String(location.getCity());
		myPub.setLatitude(location.getLatitude());
		myPub.setLongitude(location.getLongitude());
		myLatLng = new LatLonPoint(location.getLatitude(),
				location.getLongitude());

		// 移动地图视图
		moveMapCameraToStartPosition(location.getLatitude(), location.getLongitude());

		// 获取所在城市的常用地址，如果有，就显示常用地址区域。
		dList = app.getCommonDestFromResource(R.raw.common_dest, myCityName);
		if (dList != null && dList.size() > 0) {
			view_commDest.setVisibility(View.VISIBLE);
			createCommDestRadioGroup(dList);
		}

		// 除刷新按钮外，所有按钮都可用。
		enableAllView();
		bt_refresh.setText(R.string.iwant_bt_refresh_reset_destination);
		bt_refresh.setEnabled(false);
		bt_xunta.setEnabled(false);
	}

	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	// infowindowadapter接口
	/**
	 * 监听自定义infowindow窗口的infocontents事件回调 InfoWindowAdapter中实现的方法
	 * 
	 */
	public View getInfoContents(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 监听自定义infowindow窗口的infowindow事件回调 InfoWindowAdapter中实现的方法
	 * 
	 */
	public View getInfoWindow(Marker marker) {
		LatLng latlng = marker.getPosition();
		destName = (String) marker.getObject();
		destLatLng = new LatLonPoint(latlng.latitude, latlng.longitude);
		if (destName == null) {
			return null;
		}
		View infowindow = getLayoutInflater().inflate(
				R.layout.iwant_infowindow, null);
		TextView tv = (TextView) infowindow
				.findViewById(R.id.iwant_infowindow_tv);
		ImageButton ib = (ImageButton) infowindow
				.findViewById(R.id.iwant_infowindow_ib);
		tv.setText(destName);
		// 如果tv被点击则查询路线。
		tv.setOnClickListener(new OnClickListener() {		
			public void onClick(View v) {
				queryRoute();
			}
		});
		// 如果ib被点击就查询路线。
		ib.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				queryRoute();
			}
		});

		return infowindow;
	}
	public boolean onMarkerClick(Marker marker) {
		marker.showInfoWindow();
		return false;
	}

	/*
	 * 处理地图被点击的事件。点击后，显示点击位置，并向服务器查询点击位置的POI信息
	 */
	public void onMapClick(LatLng latlng) {
		// TODO Auto-generated method stub

		// 如果是在路线视图下，就直接返回
		// if (isRouteCamera) {
		// return;
		// }
		if (mapView.isClickable() == false) {
			return;
		}

		clearMapMarkers();

		// 添加目的地marker
		destMarker = aMap.addMarker(new MarkerOptions()
				.anchor(0.5f, 1)
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.iwant_marker_dest))
				.position(latlng).title("title"));

		// 查询POI信息
		destLatLng = new LatLonPoint(latlng.latitude, latlng.longitude);
		reGeoQuery = new RegeocodeQuery(destLatLng, CONS_REGEOCODER_RADIUS,
				GeocodeSearch.AMAP);
		geocoderSearch.getFromLocationAsyn(reGeoQuery);
		tv_map_info.setText(R.string.iwant_info_querying_poi);
		tv_map_info.setVisibility(View.VISIBLE);
	}

	public void onGeocodeSearched(GeocodeResult result, int rCode) {
		// TODO Auto-generated method stub
	}

	/**
	 * 获得了POI的信息。
	 * 
	 */
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
		// TODO Auto-generated method stub
		switch (rCode) {
		case 0:
			// 没有返回结果。
			if (result == null || result.getRegeocodeAddress() == null) {
				tv_map_info.setVisibility(View.VISIBLE);
				tv_map_info.setText(R.string.iwant_info_no_poi);
				break;
			}
			// 获得此次结果时，用户又一次点击了地图。
			if (!result.getRegeocodeQuery().equals(reGeoQuery)) {
				tv_map_info.setVisibility(View.INVISIBLE);
				break;
			}

			tv_map_info.setVisibility(View.INVISIBLE);

			destName = getGeoInfo(result.getRegeocodeAddress());

			destMarker.setObject(destName);
			destMarker.showInfoWindow();
			break;
		// 不能获得位置信息就提示。
		// rCode有很多可能返回值，这里就简单忽略其它值。20140228
		default:
			tv_map_info.setText(R.string.iwant_info_cannt_get_poi);
		}
	}

	// OnRouteSearchListener
	/**
	 * 查询route收到服务器返回值时的处理，显示路线。
	 * 
	 * 
	 */
	public void onDriveRouteSearched(DriveRouteResult result, int rCode) {
		// TODO Auto-generated method stub
		switch (rCode) {
		case 0:
			// 没有返回结果
			if (result == null || result.getPaths() == null
					|| result.getPaths().size() == 0) {
				tv_map_info.setVisibility(View.VISIBLE);
				tv_map_info.setText(R.string.iwant_info_no_route);
				// 刷新按钮不可用
				enableAllView();
				bt_xunta.setEnabled(false);
				bt_refresh.setEnabled(false);
			}

			DrivePath drivePath = result.getPaths().get(0);
			float distance = drivePath.getDistance();
			float duration = drivePath.getDuration();

			// 清空地图
			aMap.clear();

			LatLng startLatLng = MapUtil.convertToLatLng(result.getStartPos());
			LatLng endLatLng = MapUtil.convertToLatLng(result.getTargetPos());

			// 增加起点和终点marker.		
			aMap.addMarker(new MarkerOptions()
					.position(startLatLng)
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.iwant_marker_start))
					.title("title"));
			aMap.addMarker(new MarkerOptions()
					.position(endLatLng)
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.iwant_marker_dest))
					.title("title"));
			
		
			// 添加路线覆盖
			MapUtil.addRouteLineToMap(aMap, drivePath, startLatLng, endLatLng);

			// !!!!!!!!!!!!!!!!!!!!!!!!目前只加入了起点和终点，会存在路线显示不全的情况。可以考虑加入更多的点。
			LatLngBounds.Builder b = LatLngBounds.builder();
			b.include(startLatLng);
			b.include(endLatLng);

			aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(b.build(), CONS_CAMERA_PADDING_TO_SCREEN));

			String info = createRouteInfo(distance, duration);
			tv_map_info.setText(info);
			tv_map_info.setVisibility(View.VISIBLE);

			myPub.setDestName(destName);
			myPub.setDestLatitude(destLatLng.getLatitude());
			myPub.setDestLongitude(destLatLng.getLongitude());

			// 只有寻TA和刷新按钮可用。
			disableAllView();
			bt_xunta.setEnabled(true);
			bt_refresh.setEnabled(true);
			
			// marker点击无效。
			aMap.setOnMarkerClickListener(null);
			break;
		default:
			tv_map_info.setVisibility(View.VISIBLE);
			tv_map_info.setText(R.string.iwant_info_no_route);
			// 刷新按钮不可用。
			enableAllView();
			bt_xunta.setEnabled(false);
			bt_refresh.setEnabled(false);
			break;
		}

	}

	public void onWalkRouteSearched(WalkRouteResult arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	public void onBusRouteSearched(BusRouteResult arg0, int arg1) {
		// TODO Auto-generated method stub
	}

	// poisearchlistne
	public void onPoiItemDetailSearched(PoiItemDetail arg0, int arg1) {
	}

	/**
	 * 收到poi search 的返回结果。 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!其它返回值处理。
	 */
	public void onPoiSearched(PoiResult result, int rCode) {
		// TODO Auto-generated method stub
		switch (rCode) {
		case 0:
			if (result == null || result.getQuery() == null
					|| !result.getQuery().equals(poiQuery)) {
				tv_map_info.setText(R.string.iwant_info_no_search_result);
				enableAllView();
				bt_refresh.setEnabled(false);
				break;
			}
			// 取得第一页的poiitem数据，页数从数字0开始
			List<PoiItem> poiItems = result.getPois();
			if (null == poiItems || 0 == poiItems.size()) {
				tv_map_info.setText(R.string.iwant_info_no_search_result);
				enableAllView();
				bt_refresh.setEnabled(false);
				bt_xunta.setEnabled(false);
				break;
			}

			tv_map_info.setVisibility(View.INVISIBLE);
			enableAllView();
			bt_refresh.setEnabled(false);
			bt_xunta.setEnabled(false);
			restoreMap();

			LatLngBounds.Builder b = LatLngBounds.builder();

			for (PoiItem poiItem : poiItems) {
				LatLng latlng = new LatLng(poiItem.getLatLonPoint()
						.getLatitude(), poiItem.getLatLonPoint().getLongitude());
				Marker poiMarker = aMap.addMarker(new MarkerOptions()
						.anchor(0.5f, 1)
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.iwant_marker_dest))
						.position(latlng).title("title"));
				poiMarker.setObject(poiItem.getSnippet() + poiItem.getTitle());
				poiMarkerList.add(poiMarker);
				b.include(latlng);
			}
			poiMarkerList.get(0).showInfoWindow();

			// 如果仅有一个搜索结果。
			if (poiItems.size() == 1){
				aMap.moveCamera(CameraUpdateFactory
						.newCameraPosition(new CameraPosition.Builder()
								.target(new LatLng(poiItems.get(0).getLatLonPoint()
										.getLatitude(), poiItems.get(0).getLatLonPoint().getLongitude()))
								.zoom(CONS_ZOOM_ONE_POI).build()));
			}else{
				// CameraUpdate newLatLngBounds(LatLngBounds bounds, int padding)
				// padding - 设置区域和view之间的空白距离，单位像素。这个值适用于区域的四个边。
				aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(b.build(), 16));
			}
			break;
		default:
			tv_map_info.setText(R.string.iwant_info_no_search_result);
			enableAllView();
			bt_refresh.setEnabled(false);
		}

	}

	// OnMapLoadedListener
	public void onMapLoaded() {
		// TODO Auto-generated method stub

	}

	// TextWatcher
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub

	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		String searchKeyword = s.toString().trim();
		Inputtips inputTips = new Inputtips(IWantActivity.this,
				new InputtipsListener() {
					public void onGetInputtips(List<Tip> tipList, int rCode) {
						if (rCode == 0) {// 正确返回
							List<String> listString = new ArrayList<String>();
							for (int i = 0; i < tipList.size(); i++) {
								listString.add(tipList.get(i).getName());
							}
							ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(
									getApplicationContext(),
									R.layout.route_inputs, listString);
							actv_search_keyword.setAdapter(aAdapter);
							aAdapter.notifyDataSetChanged();
						}
					}
				});
		try {
			inputTips.requestInputtips(searchKeyword, myCityName);// 第一个参数表示提示关键字，第二个参数默认代表全国，也可以为城市区号
		} catch (AMapException e) {
			// do nothing
		}
	}

	// OnEditorActionListener
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		// EditorInfo.IME_ACTION_SEARCH doesn't work, don't know why.
		switch (actionId) {
		default:
			return bt_search.performClick();
		}
	}

	/**
	 * 后退键点击。
	 */
	@Override
	public void onBackPressed() {
		long timeNow = System.currentTimeMillis();
		long timeOff = timeNow - lastPressedTime;

		//结束APP
		if (timeOff < IwantUApp.CONS_DOUBLE_CLICK_EXIT_INTERVAL) {
			Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("EXIT", true);
			startActivity(intent);
			finish();
		} else {
			lastPressedTime = timeNow;
			Toast.makeText(this, R.string.toast_doubleclick_exit,
					Toast.LENGTH_SHORT).show();
		}
	}

	// OnClickerListner
	/**
	 * 处理各种点击事件。
	 */
	public void onClick(View v) {

		switch (v.getId()) {

		// 点击刷新按钮，冲新定位
		case R.id.iwant_bt_refresh:
			Button bt = (Button) v;
			// 如果是重新定位，点击后重新定位
			if (bt.getText().equals(
					getResources()
							.getString(R.string.iwant_bt_refresh_relocate))) {
				startLocating();
			}
			// 如果是重新搜索目的地
			else if (bt.getText().equals(
					getResources().getString(
							R.string.iwant_bt_refresh_reset_destination))) {
				aMap.clear();
				// isRouteCamera = false;
				enableMap();
				restoreMap();
				moveMapCameraToStartPosition(myLatLng.getLatitude(), myLatLng.getLongitude());

				tv_map_info.setVisibility(View.INVISIBLE);
				enableAllView();
				bt_refresh.setEnabled(false);
				bt_xunta.setEnabled(false);
				//marker点击有效。
				aMap.setOnMarkerClickListener(this);
			}
			break;
		// 点击 寻TA按钮。
		case R.id.iwant_bt_xunta:
			Intent i = new Intent();
			i.setClass(v.getContext(), MainActivity.class);
			i.putExtra(IwantUApp.ONTOLOGY_MEMBER, (Parcelable) member);
			i.putExtra(IwantUApp.ONTOLOGY_PUBLICATION, (Parcelable) myPub);
			startActivity(i);
			break;
		// 点击搜索按钮。
		case R.id.iwant_bt_search:
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(actv_search_keyword.getWindowToken(), 0);

			// isRouteCamera = false;

			String keyWord = AppUtil.checkEditText(actv_search_keyword);
			if ("".equals(keyWord)) {
				Toast.makeText(getApplicationContext(),
						R.string.toast_input_search_keyword, Toast.LENGTH_SHORT)
						.show();
				return;
			}
			// 所有视图均不可用。
			disableAllView();
			restoreMap();
			this.clearMapMarkers();

			tv_map_info.setText(R.string.iwant_info_querying_poi);

			// !!!!!!!!!!!!!!!!!!!!!!!!!!每次搜索的结果可能不一样。
			poiQuery = new PoiSearch.Query(keyWord, "", myCityName);
			poiQuery.setPageSize(CONS_SEARCH_ITEM_NUM);
			poiQuery.setPageNum(0);// 设置查第一页

			PoiSearch poiSearch = new PoiSearch(getApplicationContext(),
					poiQuery);
			poiSearch.setOnPoiSearchListener(IWantActivity.this);
			poiSearch.searchPOIAsyn();
			break;
		default:
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////
	// 自定义类区域
	/**
	 * 处理radiobutton被点击事件。
	 * 
	 * @author pom.sul @date 2014-1-12
	 * 
	 */
	private class OnClickListener_rb implements OnClickListener {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			for (RadioButton rb : rbList) {
				rb.setChecked(false);
			}
			RadioButton rb = (RadioButton) v;
			rb.setChecked(true);

			String theDestName = rb.getText().toString();

			Iterator<Destination> it = dList.iterator();
			while (it.hasNext()) {
				Destination d = it.next();
				if (d.getDestName().equals(theDestName)) {
					destLatLng.setLatitude(d.getLatitude());
					destLatLng.setLongitude(d.getLongitude());
					destName = theDestName;
					break;
				}
			}
			// 查询路线。
			queryRoute();
		}
	}

	/**
	 * 用户获取常用目的地时的progress dialog。
	 * 
	 * @author tom @date 2014-2-10
	 * 
	 */
	/*
	 * private class DestinationTaskProgressDialog extends ProgressDialog {
	 * 
	 * public DestinationTaskProgressDialog(Context context) { super(context); }
	 * 
	 * // 用户点击返回键
	 * 
	 * @Override public void onBackPressed() { IwantUApp.msgHandler
	 * .sendEmptyMessage(IwantUApp.MSG_TO_IWANT_CANCEL_GET_DESTINATION); }
	 * 
	 * // 用户点击窗口无效
	 * 
	 * @Override public boolean onTouchEvent(MotionEvent event) { return true; }
	 * }
	 */

	// //////////////////////////////////////////////////////////////////////////////////
	// 消息处理区域
	/**
	 * 消息处理
	 * 
	 * @param msg
	 */
	public void handleMsg(Message msg) {
		switch (msg.what) {

		// 用户取消定位，提示用户，只有刷新按钮可用。
		case IwantUApp.MSG_TO_IWANT_CANCEL_LOCATING:
			if (null != progressDialog) {
				progressDialog.dismiss();
				Toast.makeText(this, R.string.iwant_toast_locating_cancelled,
						Toast.LENGTH_SHORT).show();
			}
			// 停止定位
			mAMapLocManager.removeUpdates(this);

			moveMapCameraToStartPosition(0, 0);
			disableAllView();
			bt_refresh.setEnabled(true);
			bt_refresh.setText(R.string.iwant_bt_refresh_relocate);
			break;
		case IwantUApp.MSG_TO_IWANT_EX_CONN_TIMEOUT:
			Toast.makeText(getApplicationContext(), R.string.ex_conn_timeout,
					Toast.LENGTH_SHORT).show();
			break;
		case IwantUApp.MSG_TO_IWANT_EX_UNKNOWN:
			Toast.makeText(getApplicationContext(), R.string.ex_unknown,
					Toast.LENGTH_SHORT).show();
			break;
		// 用户取消获得常用目的地
		case IwantUApp.MSG_TO_IWANT_CANCEL_GET_DESTINATION:
			if (destinationTask != null && !destinationTask.isCancelled()) {
				destinationTask.cancel(true);
			}
			break;
		// 不能获得常用目的地
		case IwantUApp.MSG_TO_IWANT_CANNT_GET_DESTINATION:
			tv_common_dest.setText(R.string.iwant_common_dest_tv_2);
			break;
		// 获得常用目的地，创建常用目的地区域
		case IwantUApp.MSG_TO_IWANT_GOT_DESTINATION:
			Bundle b = msg.getData();
			DestinationList destList = (DestinationList) b
					.getSerializable("dList");
			ArrayList<Destination> dList = (ArrayList<Destination>) destList
					.getdList();
			commDestRadioGroup.removeAllViews();
			rbList.clear();
			createCommDestRadioGroup(dList);
			break;
		default:
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////////
	// 与服务器交互区域

	/**
	 * 
	 * 获取当前城市的常用打车地址。
	 * 20140216,该方法暂时被弃用。因为需要与服务器交互，客户端开销很大。该为将常用目的地写入在本地资源文件中直接读取。
	 * 
	 */
	@Deprecated
	private class DestinationTask extends
			AsyncTask<MediaType, Void, DestinationList> {
		private final ProgressDialog dialog = new ProgressDialog(
				IWantActivity.this);

		private final Charset UTF8 = Charset.forName("utf-8");

		@Override
		protected void onPreExecute() {
			this.dialog.setMessage(getResources().getString(
					R.string.progress_getting_common_destinations));
			this.dialog.show();
		}

		@Override
		protected DestinationList doInBackground(MediaType... params) {
			try {
				final String url = app.getServerBaseURL() + "/destinations";
				HttpHeaders requestHeaders = new HttpHeaders();
				requestHeaders.setContentType(new MediaType("text", "plain",
						UTF8));
				HttpEntity<String> requestEntity = new HttpEntity<String>(
						myCityName, requestHeaders);
				StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();
				stringConverter.setSupportedMediaTypes(Arrays
						.asList(new MediaType("text", "plain", UTF8)));

				HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
				requestFactory
						.setConnectTimeout(IwantUApp.CONS_CONNECTTING_TIMEOUT);
				RestTemplate restTemplate = new RestTemplate(requestFactory);
				restTemplate.getMessageConverters().add(stringConverter);
				restTemplate.getMessageConverters().add(
						new SimpleXmlHttpMessageConverter());
				ResponseEntity<DestinationList> response = restTemplate
						.exchange(url, HttpMethod.POST, requestEntity,
								DestinationList.class);
				return (DestinationList) response.getBody();
			} catch (Exception e) {
				if (e instanceof ConnectTimeoutException) {
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_IWANT_EX_CONN_TIMEOUT);
				} else {
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_IWANT_EX_UNKNOWN);
				}
				return null;
			}

		}

		@Override
		protected void onPostExecute(DestinationList dList) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			if (dList == null) {
				IwantUApp.msgHandler
						.sendEmptyMessage(IwantUApp.MSG_TO_IWANT_CANNT_GET_DESTINATION);
			} else {
				Bundle b = new Bundle();
				b.putSerializable("dList", dList);
				Message msg = new Message();
				msg.setData(b);
				msg.what = IwantUApp.MSG_TO_IWANT_GOT_DESTINATION;
				IwantUApp.msgHandler.sendMessage(msg);
			}
		}

		@Override
		protected void onCancelled() {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			IwantUApp.msgHandler
					.sendEmptyMessage(IwantUApp.MSG_TO_IWANT_CANNT_GET_DESTINATION);
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////////////
	// 其它方法区域
	/**
	 * 开始定位
	 */
	private void startLocating() {
		mAMapLocManager.requestLocationUpdates(
				LocationProviderProxy.AMapNetwork,
				CONS_LOC_UPDATE_TIME_THRESHOLD,
				CONS_LOC_UPDATE_SPACE_THRESHOLD, this);

		initProgressDialog();
		progressDialog.setMessage(getResources().getString(
				R.string.iwant_toast_locating));
		progressDialog.setCancelMessage(Message.obtain(IwantUApp.msgHandler,
				IwantUApp.MSG_TO_IWANT_CANCEL_LOCATING));
		progressDialog.show();
	}

	/**
	 * 查询路线。要求destLatLng和myLatLng必须准备好。
	 * 
	 */
	private void queryRoute() {

		tv_map_info.setText(R.string.iwant_info_querying_route);
		tv_map_info.setVisibility(View.VISIBLE);
		// 所有视图均不可用
		disableAllView();

		final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
				myLatLng, destLatLng);

		DriveRouteQuery q = new DriveRouteQuery(fromAndTo, drivingMode, null,
				null, "");
		routeSearch.calculateDriveRouteAsyn(q);// 异步路径规划驾车模式查询

	}

	/**
	 * 移动视图到起始地点。
	 * 两个参数可以为0，或者用户的位置。
	 * 
	 * @param latitude,
	 * @param longitude
	 */
	private void moveMapCameraToStartPosition(double latitude, double longitude) {
		aMap.moveCamera(CameraUpdateFactory
				.newCameraPosition(new CameraPosition.Builder()
						.target(new LatLng(latitude, longitude))
						.zoom(currentZoom).build()));

		aMap.addMarker(new MarkerOptions()
				.position(new LatLng(latitude, longitude))
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.iwant_marker_start))
				.title("title"));
	}

	/**
	 * 重新设定地图，清空所有的marker,重新加载用户位置的marker。
	 */
	private void restoreMap() {
		aMap.clear();
		aMap.addMarker(new MarkerOptions()
				.position(
						new LatLng(myLatLng.getLatitude(), myLatLng
								.getLongitude()))
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.iwant_marker_start))
				.title("title"));
	}

	/**
	 * 地图不可用。
	 */
	private void disableMap() {
		// UiSettings uiSettings = aMap.getUiSettings();
		// uiSettings.setAllGesturesEnabled(false);
		mapView.setClickable(false);
	}

	/**
	 * 地图可用
	 */
	private void enableMap() {
		// UiSettings uiSettings = aMap.getUiSettings();
		// uiSettings.setAllGesturesEnabled(true);
		mapView.setClickable(true);
	}

	/**
	 * 除刷新按钮（重新搜索目的地、重新定位）外，其它按钮都不可用
	 */
	private void disableAllView() {

		disableMap();
		bt_xunta.setEnabled(false);
		bt_search.setEnabled(false);
		if (null != rbList) {
			for (RadioButton rb : rbList) {
				rb.setEnabled(false);
			}
		}
		actv_search_keyword.setEnabled(false);
		mapView.setEnabled(false);
		bt_refresh.setEnabled(false);
	}

	/**
	 * 所有视图都可用
	 */
	private void enableAllView() {
		enableMap();
		bt_xunta.setEnabled(true);
		bt_search.setEnabled(true);
		if (null != rbList) {
			for (RadioButton rb : rbList) {
				rb.setChecked(false);
				rb.setEnabled(true);
			}
		}
		actv_search_keyword.setEnabled(true);
		mapView.setEnabled(true);
		bt_refresh.setEnabled(true);
	}

	/**
	 * 清空所有marker
	 */
	private void clearMapMarkers() {
		if (null != destMarker) {
			destMarker.destroy();
		}
		for (Marker poiMarker : poiMarkerList) {
			poiMarker.destroy();
		}
		poiMarkerList.clear();
	}

	/**
	 * 创建常用目的地的视图。
	 * 
	 * @param radioGroup
	 * @param commDestNameList
	 */
	public void createCommDestRadioGroup(ArrayList<Destination> dList) {
		// List<Destination> dList = destList.getdList();

		// 清除已有
		rbList.clear();
		commDestRadioGroup.removeAllViews();

		int cnt = dList.size();
		Iterator<Destination> it = dList.iterator();
		int i = 0;
		LinearLayout ll_eachline = new LinearLayout(this);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER;
		// lp.leftMargin = (int)
		// getResources().getDimension(R.dimen.margin_iwant_rb_left);
		lp.weight = 1;
		while (it.hasNext()) {
			if (i % CONS_COMMON_DEST_RB_EACH_LINE == 0) {
				ll_eachline = new LinearLayout(this);
				ll_eachline.setOrientation(LinearLayout.HORIZONTAL);
				ll_eachline.setLayoutParams(lp);
				commDestRadioGroup.addView(ll_eachline);
			}
			i++;
			Destination d = (Destination) it.next();
			LinearLayout ll_sub = new LinearLayout(this);
			ll_sub.setLayoutParams(lp);
			RadioButton rb = new RadioButton(this);
			rb.setButtonDrawable(R.drawable.mytheme_btn_radio_holo_light);
			rb.setText(d.getDestName());
			rb.setTextColor(getResources().getColor(R.color.rb_text));

			TypedValue textSizeValue = new TypedValue();
			getResources().getValue(R.dimen.iwant_rb_text_size, textSizeValue,
					true);
			rb.setTextSize(textSizeValue.getFloat());
			// api 需要16
			// rb.setBackground(getResources().getDrawable(R.drawable.rb_default));
			rb.setOnClickListener(new OnClickListener_rb());
			ll_sub.addView(rb);
			ll_eachline.addView(ll_sub);
			rbList.add(rb);
		}
		int residual = CONS_COMMON_DEST_RB_EACH_LINE - cnt
				% CONS_COMMON_DEST_RB_EACH_LINE;
		// 如果还有空缺，就补齐
		if (residual < CONS_COMMON_DEST_RB_EACH_LINE) {
			for (int j = 0; j < residual; j++) {
				LinearLayout ll_sub = new LinearLayout(this);
				ll_sub.setLayoutParams(lp);
				ll_eachline.addView(ll_sub);
			}
		}

	}

	/**
	 * 通过distance和duration生成路线信息，显示在map上的信息区域。
	 * 
	 * @param distance
	 *            路途，单位米。
	 * @param duration
	 *            所需时间，单位秒。
	 */
	private String createRouteInfo(float distance, float duration) {
		String info;
		int min;
		int hour;
		// 将路程单位米转换为千米
		int dis = Math.round(distance / 1000);

		if (duration < 60) {
			info = getResources().getString(R.string.iwant_info2);
			return info;
		}

		String info_time;
		// 对需要时间进行判断以提供不同的时间显示
		if (duration < 3600) {
			min = Math.round(duration / 60);
			info_time = min + getResources().getString(R.string.minute);
		} else {
			hour = Math.round(duration / 3600);
			min = Math.round(duration % 3600) / (60);
			info_time = hour + getResources().getString(R.string.hour) + min
					+ getResources().getString(R.string.minute);
		}
		return String.format(getResources().getString(R.string.iwant_info1), destName, dis, info_time);
	}

	/**
	 * 提取RegeocodeAddress中的位置信息。
	 * 
	 * @param RegeocodeAddress
	 * @return
	 */
	private String getGeoInfo(RegeocodeAddress ra) {

		String returnStr;
		// 区
		String dis = ra.getDistrict();
		// 镇
		String townShip = ra.getTownship();

		returnStr = dis + townShip;

		StreetNumber strNum = ra.getStreetNumber();
		java.util.List<PoiItem> poiList = ra.getPois();
		java.util.List<Crossroad> cRoad = ra.getCrossroads();

		String strNumber = strNum.getNumber();
		String strName = strNum.getStreet();
		String secondRoadName = "";
		if (cRoad.size() > 0) {
			secondRoadName = cRoad.get(0).getSecondRoadName();
		}

		if (strName != "" && strNumber != "") {
			returnStr = returnStr + strName + strNumber
					+ getResources().getString(R.string.iwant_geo_info_1);
			if (secondRoadName != "" && !strName.equals(secondRoadName)) {
				return returnStr
						+ getResources().getString(R.string.iwant_geo_info_2)
						+ secondRoadName;
			}
			return returnStr;
		}
		PoiItem poiItem = null;
		if (poiList.size() > 0) {
			poiItem = poiList.get(0);
		}
		if (null != poiItem) {
			return returnStr + poiItem;
		}
		return returnStr;

	}



}