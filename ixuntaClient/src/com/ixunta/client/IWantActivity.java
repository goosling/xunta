/**
 * iwantҳ�档
 * �û��ڸ�ҳ���ȡ�Լ���ǰ��λ����Ϣ����ѡ��Ŀ�ĵء����ַ�����1���ڵ�ͼ��ѡ��2��ѡ���õص㡣���õص�ӷ�������á�
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

	// ����
	private DestinationTask destinationTask;
	private IwantUApp app;
	private LocationManagerProxy mAMapLocManager;

	private PoiSearch.Query poiQuery;
	private RegeocodeQuery reGeoQuery;
	private ProgressDialog progressDialog;

	private long lastPressedTime;

	// ҳ�沼�֡�
	// ��ʾĿ�ĵ���Ϣ��ͬʱ��Ҫ��ʾ·�߾����ʱ����
	private TextView tv_map_info;
	// ���õ�ַ�������ʾ��Ϣ��
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

	// ҳ������

	private ArrayList<Destination> dList;
	private ArrayList<RadioButton> rbList = new ArrayList<RadioButton>();
	private LatLonPoint myLatLng, destLatLng;
	// Ŀ�ĵص�����
	private String destName;
	// �ҵķ��������ᴫ��mainactivity�С�
	private Publication myPub;
	// �ҵĻ�Ա��Ϣ
	private Member member;
	// ·�߲�ѯ
	private RouteSearch routeSearch;
	private GeocodeSearch geocoderSearch;
	// �û���ǰ����Ϣ����γ�ȡ����С�
	private String myCityName;

	// ҳ�����
	private int drivingMode = RouteSearch.DrivingDefault;
	// ��ͼ��ʾ��Ĭ��zoom
	public static final int MAP_ZOOM_DEFAULT = 13;
	public static boolean hasLocationUpdated = false;
	private int currentZoom = MAP_ZOOM_DEFAULT;

	// λ�ø��µ�ʱ���ż�����λ�Ǻ��룬��requestLocationUpdates������ʹ�á�
	private static final int CONS_LOC_UPDATE_TIME_THRESHOLD = 60000;

	// λ�ø��µľ����ż�����λ���ף���requestLocationUpdates������ʹ�á�
	private static final int CONS_LOC_UPDATE_SPACE_THRESHOLD = 500;

	// ���õ�ַ����ÿ����ʾ��radio button������
	private static final int CONS_COMMON_DEST_RB_EACH_LINE = 3;

	// RegeocodeQuery���캯���еĲ�����ָ�������Χ�ڵ�POI����λ���ס�
	private static final float CONS_REGEOCODER_RADIUS = 200;

	// ÿ��������෵�ؽ����
	private static final int CONS_SEARCH_ITEM_NUM = 3;

	public static final int PUB_SERVICECODE_DEFAULT = 0x1010000;

	// ���������ֻ��һ��POIʱ��ZOOM��
	private static final int CONS_ZOOM_ONE_POI = 17;
	
	// ·����ͼ�»��߶�POI��ͼ�µ���Ļ��Ե�Ŀ�϶
	private static final int CONS_CAMERA_PADDING_TO_SCREEN = 48;

	// //////////////////////////////////////////////////////////////////////////////////////
	// Activity��������
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

		// ��ʼ����ͼ��
		mapView = (MapView) findViewById(R.id.iwant_map);
		mapView.onCreate(savedInstanceState);// �˷���������д
		aMap = mapView.getMap();
		initMap();

		// ��ʼ��������ʾ����
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

		// ��ʼ��λ
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
	// ��ʼ������
	/*
	 * ��ʼ����ͼ
	 */
	private void initMap() {
		aMap.clear();

		// ��ͼ����
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
	// �ӿ�ʵ�ֺͷ����ع�����
	// amaplocationlistner�ӿ�
	/**
	 * �����û�����λ�÷����仯���¼�
	 */
	public void onLocationChanged(AMapLocation location) {

		// ��λ�ɹ�����ʾ��Ϣ������ʾ
		if (progressDialog.isShowing()) {
			progressDialog.dismiss();
		}

		// һ�����������λ�á�
		mAMapLocManager.removeUpdates(this);

		if (location == null) {
			return;
		}

		myCityName = new String(location.getCity());
		myPub.setLatitude(location.getLatitude());
		myPub.setLongitude(location.getLongitude());
		myLatLng = new LatLonPoint(location.getLatitude(),
				location.getLongitude());

		// �ƶ���ͼ��ͼ
		moveMapCameraToStartPosition(location.getLatitude(), location.getLongitude());

		// ��ȡ���ڳ��еĳ��õ�ַ������У�����ʾ���õ�ַ����
		dList = app.getCommonDestFromResource(R.raw.common_dest, myCityName);
		if (dList != null && dList.size() > 0) {
			view_commDest.setVisibility(View.VISIBLE);
			createCommDestRadioGroup(dList);
		}

		// ��ˢ�°�ť�⣬���а�ť�����á�
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

	// infowindowadapter�ӿ�
	/**
	 * �����Զ���infowindow���ڵ�infocontents�¼��ص� InfoWindowAdapter��ʵ�ֵķ���
	 * 
	 */
	public View getInfoContents(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * �����Զ���infowindow���ڵ�infowindow�¼��ص� InfoWindowAdapter��ʵ�ֵķ���
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
		// ���tv��������ѯ·�ߡ�
		tv.setOnClickListener(new OnClickListener() {		
			public void onClick(View v) {
				queryRoute();
			}
		});
		// ���ib������Ͳ�ѯ·�ߡ�
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
	 * �����ͼ��������¼����������ʾ���λ�ã������������ѯ���λ�õ�POI��Ϣ
	 */
	public void onMapClick(LatLng latlng) {
		// TODO Auto-generated method stub

		// �������·����ͼ�£���ֱ�ӷ���
		// if (isRouteCamera) {
		// return;
		// }
		if (mapView.isClickable() == false) {
			return;
		}

		clearMapMarkers();

		// ���Ŀ�ĵ�marker
		destMarker = aMap.addMarker(new MarkerOptions()
				.anchor(0.5f, 1)
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.iwant_marker_dest))
				.position(latlng).title("title"));

		// ��ѯPOI��Ϣ
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
	 * �����POI����Ϣ��
	 * 
	 */
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
		// TODO Auto-generated method stub
		switch (rCode) {
		case 0:
			// û�з��ؽ����
			if (result == null || result.getRegeocodeAddress() == null) {
				tv_map_info.setVisibility(View.VISIBLE);
				tv_map_info.setText(R.string.iwant_info_no_poi);
				break;
			}
			// ��ô˴ν��ʱ���û���һ�ε���˵�ͼ��
			if (!result.getRegeocodeQuery().equals(reGeoQuery)) {
				tv_map_info.setVisibility(View.INVISIBLE);
				break;
			}

			tv_map_info.setVisibility(View.INVISIBLE);

			destName = getGeoInfo(result.getRegeocodeAddress());

			destMarker.setObject(destName);
			destMarker.showInfoWindow();
			break;
		// ���ܻ��λ����Ϣ����ʾ��
		// rCode�кܶ���ܷ���ֵ������ͼ򵥺�������ֵ��20140228
		default:
			tv_map_info.setText(R.string.iwant_info_cannt_get_poi);
		}
	}

	// OnRouteSearchListener
	/**
	 * ��ѯroute�յ�����������ֵʱ�Ĵ�����ʾ·�ߡ�
	 * 
	 * 
	 */
	public void onDriveRouteSearched(DriveRouteResult result, int rCode) {
		// TODO Auto-generated method stub
		switch (rCode) {
		case 0:
			// û�з��ؽ��
			if (result == null || result.getPaths() == null
					|| result.getPaths().size() == 0) {
				tv_map_info.setVisibility(View.VISIBLE);
				tv_map_info.setText(R.string.iwant_info_no_route);
				// ˢ�°�ť������
				enableAllView();
				bt_xunta.setEnabled(false);
				bt_refresh.setEnabled(false);
			}

			DrivePath drivePath = result.getPaths().get(0);
			float distance = drivePath.getDistance();
			float duration = drivePath.getDuration();

			// ��յ�ͼ
			aMap.clear();

			LatLng startLatLng = MapUtil.convertToLatLng(result.getStartPos());
			LatLng endLatLng = MapUtil.convertToLatLng(result.getTargetPos());

			// ���������յ�marker.		
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
			
		
			// ���·�߸���
			MapUtil.addRouteLineToMap(aMap, drivePath, startLatLng, endLatLng);

			// !!!!!!!!!!!!!!!!!!!!!!!!Ŀǰֻ�����������յ㣬�����·����ʾ��ȫ����������Կ��Ǽ������ĵ㡣
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

			// ֻ��ѰTA��ˢ�°�ť���á�
			disableAllView();
			bt_xunta.setEnabled(true);
			bt_refresh.setEnabled(true);
			
			// marker�����Ч��
			aMap.setOnMarkerClickListener(null);
			break;
		default:
			tv_map_info.setVisibility(View.VISIBLE);
			tv_map_info.setText(R.string.iwant_info_no_route);
			// ˢ�°�ť�����á�
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
	 * �յ�poi search �ķ��ؽ���� !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!��������ֵ����
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
			// ȡ�õ�һҳ��poiitem���ݣ�ҳ��������0��ʼ
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

			// �������һ�����������
			if (poiItems.size() == 1){
				aMap.moveCamera(CameraUpdateFactory
						.newCameraPosition(new CameraPosition.Builder()
								.target(new LatLng(poiItems.get(0).getLatLonPoint()
										.getLatitude(), poiItems.get(0).getLatLonPoint().getLongitude()))
								.zoom(CONS_ZOOM_ONE_POI).build()));
			}else{
				// CameraUpdate newLatLngBounds(LatLngBounds bounds, int padding)
				// padding - ���������view֮��Ŀհ׾��룬��λ���ء����ֵ������������ĸ��ߡ�
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
						if (rCode == 0) {// ��ȷ����
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
			inputTips.requestInputtips(searchKeyword, myCityName);// ��һ��������ʾ��ʾ�ؼ��֣��ڶ�������Ĭ�ϴ���ȫ����Ҳ����Ϊ��������
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
	 * ���˼������
	 */
	@Override
	public void onBackPressed() {
		long timeNow = System.currentTimeMillis();
		long timeOff = timeNow - lastPressedTime;

		//����APP
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
	 * ������ֵ���¼���
	 */
	public void onClick(View v) {

		switch (v.getId()) {

		// ���ˢ�°�ť�����¶�λ
		case R.id.iwant_bt_refresh:
			Button bt = (Button) v;
			// ��������¶�λ����������¶�λ
			if (bt.getText().equals(
					getResources()
							.getString(R.string.iwant_bt_refresh_relocate))) {
				startLocating();
			}
			// �������������Ŀ�ĵ�
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
				//marker�����Ч��
				aMap.setOnMarkerClickListener(this);
			}
			break;
		// ��� ѰTA��ť��
		case R.id.iwant_bt_xunta:
			Intent i = new Intent();
			i.setClass(v.getContext(), MainActivity.class);
			i.putExtra(IwantUApp.ONTOLOGY_MEMBER, (Parcelable) member);
			i.putExtra(IwantUApp.ONTOLOGY_PUBLICATION, (Parcelable) myPub);
			startActivity(i);
			break;
		// ���������ť��
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
			// ������ͼ�������á�
			disableAllView();
			restoreMap();
			this.clearMapMarkers();

			tv_map_info.setText(R.string.iwant_info_querying_poi);

			// !!!!!!!!!!!!!!!!!!!!!!!!!!ÿ�������Ľ�����ܲ�һ����
			poiQuery = new PoiSearch.Query(keyWord, "", myCityName);
			poiQuery.setPageSize(CONS_SEARCH_ITEM_NUM);
			poiQuery.setPageNum(0);// ���ò��һҳ

			PoiSearch poiSearch = new PoiSearch(getApplicationContext(),
					poiQuery);
			poiSearch.setOnPoiSearchListener(IWantActivity.this);
			poiSearch.searchPOIAsyn();
			break;
		default:
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////
	// �Զ���������
	/**
	 * ����radiobutton������¼���
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
			// ��ѯ·�ߡ�
			queryRoute();
		}
	}

	/**
	 * �û���ȡ����Ŀ�ĵ�ʱ��progress dialog��
	 * 
	 * @author tom @date 2014-2-10
	 * 
	 */
	/*
	 * private class DestinationTaskProgressDialog extends ProgressDialog {
	 * 
	 * public DestinationTaskProgressDialog(Context context) { super(context); }
	 * 
	 * // �û�������ؼ�
	 * 
	 * @Override public void onBackPressed() { IwantUApp.msgHandler
	 * .sendEmptyMessage(IwantUApp.MSG_TO_IWANT_CANCEL_GET_DESTINATION); }
	 * 
	 * // �û����������Ч
	 * 
	 * @Override public boolean onTouchEvent(MotionEvent event) { return true; }
	 * }
	 */

	// //////////////////////////////////////////////////////////////////////////////////
	// ��Ϣ��������
	/**
	 * ��Ϣ����
	 * 
	 * @param msg
	 */
	public void handleMsg(Message msg) {
		switch (msg.what) {

		// �û�ȡ����λ����ʾ�û���ֻ��ˢ�°�ť���á�
		case IwantUApp.MSG_TO_IWANT_CANCEL_LOCATING:
			if (null != progressDialog) {
				progressDialog.dismiss();
				Toast.makeText(this, R.string.iwant_toast_locating_cancelled,
						Toast.LENGTH_SHORT).show();
			}
			// ֹͣ��λ
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
		// �û�ȡ����ó���Ŀ�ĵ�
		case IwantUApp.MSG_TO_IWANT_CANCEL_GET_DESTINATION:
			if (destinationTask != null && !destinationTask.isCancelled()) {
				destinationTask.cancel(true);
			}
			break;
		// ���ܻ�ó���Ŀ�ĵ�
		case IwantUApp.MSG_TO_IWANT_CANNT_GET_DESTINATION:
			tv_common_dest.setText(R.string.iwant_common_dest_tv_2);
			break;
		// ��ó���Ŀ�ĵأ���������Ŀ�ĵ�����
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
	// ���������������

	/**
	 * 
	 * ��ȡ��ǰ���еĳ��ô򳵵�ַ��
	 * 20140216,�÷�����ʱ�����á���Ϊ��Ҫ��������������ͻ��˿����ܴ󡣸�Ϊ������Ŀ�ĵ�д���ڱ�����Դ�ļ���ֱ�Ӷ�ȡ��
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
	// ������������
	/**
	 * ��ʼ��λ
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
	 * ��ѯ·�ߡ�Ҫ��destLatLng��myLatLng����׼���á�
	 * 
	 */
	private void queryRoute() {

		tv_map_info.setText(R.string.iwant_info_querying_route);
		tv_map_info.setVisibility(View.VISIBLE);
		// ������ͼ��������
		disableAllView();

		final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
				myLatLng, destLatLng);

		DriveRouteQuery q = new DriveRouteQuery(fromAndTo, drivingMode, null,
				null, "");
		routeSearch.calculateDriveRouteAsyn(q);// �첽·���滮�ݳ�ģʽ��ѯ

	}

	/**
	 * �ƶ���ͼ����ʼ�ص㡣
	 * ������������Ϊ0�������û���λ�á�
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
	 * �����趨��ͼ��������е�marker,���¼����û�λ�õ�marker��
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
	 * ��ͼ�����á�
	 */
	private void disableMap() {
		// UiSettings uiSettings = aMap.getUiSettings();
		// uiSettings.setAllGesturesEnabled(false);
		mapView.setClickable(false);
	}

	/**
	 * ��ͼ����
	 */
	private void enableMap() {
		// UiSettings uiSettings = aMap.getUiSettings();
		// uiSettings.setAllGesturesEnabled(true);
		mapView.setClickable(true);
	}

	/**
	 * ��ˢ�°�ť����������Ŀ�ĵء����¶�λ���⣬������ť��������
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
	 * ������ͼ������
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
	 * �������marker
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
	 * ��������Ŀ�ĵص���ͼ��
	 * 
	 * @param radioGroup
	 * @param commDestNameList
	 */
	public void createCommDestRadioGroup(ArrayList<Destination> dList) {
		// List<Destination> dList = destList.getdList();

		// �������
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
			// api ��Ҫ16
			// rb.setBackground(getResources().getDrawable(R.drawable.rb_default));
			rb.setOnClickListener(new OnClickListener_rb());
			ll_sub.addView(rb);
			ll_eachline.addView(ll_sub);
			rbList.add(rb);
		}
		int residual = CONS_COMMON_DEST_RB_EACH_LINE - cnt
				% CONS_COMMON_DEST_RB_EACH_LINE;
		// ������п�ȱ���Ͳ���
		if (residual < CONS_COMMON_DEST_RB_EACH_LINE) {
			for (int j = 0; j < residual; j++) {
				LinearLayout ll_sub = new LinearLayout(this);
				ll_sub.setLayoutParams(lp);
				ll_eachline.addView(ll_sub);
			}
		}

	}

	/**
	 * ͨ��distance��duration����·����Ϣ����ʾ��map�ϵ���Ϣ����
	 * 
	 * @param distance
	 *            ·;����λ�ס�
	 * @param duration
	 *            ����ʱ�䣬��λ�롣
	 */
	private String createRouteInfo(float distance, float duration) {
		String info;
		int min;
		int hour;
		// ��·�̵�λ��ת��Ϊǧ��
		int dis = Math.round(distance / 1000);

		if (duration < 60) {
			info = getResources().getString(R.string.iwant_info2);
			return info;
		}

		String info_time;
		// ����Ҫʱ������ж����ṩ��ͬ��ʱ����ʾ
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
	 * ��ȡRegeocodeAddress�е�λ����Ϣ��
	 * 
	 * @param RegeocodeAddress
	 * @return
	 */
	private String getGeoInfo(RegeocodeAddress ra) {

		String returnStr;
		// ��
		String dis = ra.getDistrict();
		// ��
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