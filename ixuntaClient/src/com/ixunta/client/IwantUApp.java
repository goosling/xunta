package com.ixunta.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import com.ixunta.R;
import com.ixunta.client.db.Destination;
import com.ixunta.client.db.Member;
import com.ixunta.client.util.AppUtil;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import au.com.bytecode.opencsv.CSVReader;

/**

 *
 */
public class IwantUApp extends Application {

	private static final String PROPERTIES_MDN_NAME = "MDN_NAME";

	public static final String ONTOLOGY_IMEI = "IMEI";
	public static final String ONTOLOGY_IMSI = "IMSI";
	public static final String ONTOLOGY_SERVICE_NAME = "service";
	public static final String ONTOLOGY_SERVICE_TYPE = "serviceType";
	public static final String ONTOLOGY_SERVICE_LVL1 = "serviceLvl1";
	public static final String ONTOLOGY_SERVICE_LVL2 = "serviceLvl2";
	public static final String ONTOLOGY_SERVICE_DESCRIPTION = "serviceDescription";
	public static final String ONTOLOGY_SEARCH_SPACEDISTANCE = "searchSpaceDistance";
	public static final String ONTOLOGY_SEARCH_TIMEDISTANCE = "searchTimeDistance";
	public static final String ONTOLOGY_NICK = "nick";
	public static final String ONTOLOGY_PHONENUM = "phoneNum";
	public static final String ONTOLOGY_TAID = "taID";
	public static final String ONTOLOGY_LATITUDE = "latitude";
	public static final String ONTOLOGY_LONGITUDE = "longitude";
	public static final String ONTOLOGY_IMAGEURI = "imageUri";
	public static final String ONTOLOGY_MEMBER = "member";
	public static final String ONTOLOGY_PUBLICATION = "publication";
	public static final String ONTOLOGY_REGISTER = "register";
	public static final String ONTOLOGY_GENDER = "gender";
	public static final String ONTOLOGY_AGE = "age";
	public static final String ONTOLOGY_LASTNAME = "lastName";
	public static final String ONTOLOGY_PORTRAITFILE = "portraitFile";
	public static final String ONTOLOGY_ISROOKIE = "isRookie";

	public static final int CONS_SERVICECODE_DEFAULT = 0x0010000;

	/*
	 * ���ӷ�������ʱʱ�䡣
	 */
	public static final int CONS_CONNECTTING_TIMEOUT = 5000;

	public static final String CONS_SERVICE_TYPE_CUSTOMER = "c";
	public static final String CONS_SERVICE_TYPE_MERCHANT = "m";
	public static final String CONS_SERVICE_TYPE_MUTUAL = "cm";

	public static final String CONS_PORTRAIT_FILENAME_PRE = "IMG_";
	public static final String CONS_PORTRAIT_FILENAME_SUR = ".png";
	public static final String CONS_PORTRAIT_FOLDER_NAME = "portraits";
	public static final String CONS_PORTRAIT_DEFAULT_NAME = "portrait_default_me.png";
	public static final int CONS_AGE_DEFAULT = 25;
	public static final int CONS_AGE_MAX = 80;
	public static final int CONS_AGE_MIN = 8;
	public static final String CONS_GENDER_DEFAULT = "m";
	
	public static final int CONS_PORTRAIT_WIDTH = 256;
	public static final int CONS_PORTRAIT_HEIGHT = 256;

	public static final String CONS_GENDER_MALE = "m";
	public static final String CONS_GENDER_FEMALE = "f";

	public static final String CONS_COMMAND_SPLITTER = ",";
	
	// ���ε���˳�ʱ������
	public static final int CONS_DOUBLE_CLICK_EXIT_INTERVAL = 2000;
	
	public static final int RESPONSE_CODE_COMMAND_NOTHING = 0x01;
	//ǿ�Ƹ��£��ڼܹ������ش�仯��ʱ��ʹ�á�
	public static final int RESPONSE_CODE_COMMAND_FORCE_UPDATE = 0x02;
	
	public static final int RESPONSE_CODE_FEEDBACK_FAIL = 0x42;
	public static final int RESPONSE_CODE_FEEDBACK_SUCCESS = 0x41;
	public static final int RESPONSE_CODE_UPDATEMEMBER_FAIL = 0x32;
	public static final int RESPONSE_CODE_UPDATEMEMBER_SUCCESS = 0x31;
	public static final int RESPONSE_CODE_VCODE_SEND_FAIL = 0x23;
	public static final int RESPONSE_CODE_VCODE_FAIL = 0x22;
	public static final int RESPONSE_CODE_VCODE_SUCCESS = 0x21;
	public static final int RESPONSE_CODE_VCODE_GENERATE = 0x20;
	public static final int RESPONSE_CODE_LOGIN_FAIL = 0x11;
	public static final int RESPONSE_CODE_LOGIN_SUCCESS = 0x10;
	public static final int EXCEPTION_HTTP = -0x20;
	public static final int EXCEPTION_CONNECT_TIMEOUT = -0x30;
	public static final int EXCEPTION_UNKNOWN = -0xf0;

	/*
	 * MemberID��RegisterID���ַ����ȡ�ID����12�ֽڣ�16������24���ַ���ʾ���ʳ���Ϊ24.
	 */
	public static final int OBJECT_ID_LENGTH = 24;
	/*
	 * �����������ַ����볤�ȣ�Ϊ2�ַ���ʾ��
	 */
	public static final int RESPONSE_CODE_LENGTH = 2;



	/*
	 * ���λ�ȡ��֤��ʱ��������λ�롣
	 */
	public static final int INTERVAL_BETWEEN_GETVCODE = 60;

	/*
	 * ��Ϣ��֪ͨregister2ҳ������»����֤��İ�ť����ʣ��ʱ�䡣��
	 */
	public static final int MSG_TO_RSTEP2_SECONDS_LEFT_BEFORE_GETVCODE = 0x10;

	/*
	 * ��Ϣ��֪ͨregister2ҳ������»�ȡ��֤��İ�ť���á�
	 */
	public static final int MSG_TO_RSTEP2_ACTIVE_RESEND = 0x11;
	/*
	 * ��Ϣ��֪ͨregister2ҳ�����ӷ�������ʱ��
	 */
	public static final int MSG_TO_RSTEP2_EX_CONNECT_TIMEOUT = 0x12;
	/*
	 * ��Ϣ��֪ͨregister2ҳ�淢��δ֪�쳣
	 */
	public static final int MSG_TO_RSTEP2_EX_UNKNOWN = 0x13;
	/*
	 * ��֤�뷢�ͳɹ�
	 */
	public static final int MSG_TO_RSTEP2_VCODE_SENT = 0x14;
	/*
	 * ��֤�뷢��ʧ��
	 */
	public static final int MSG_TO_RSTEP2_VCODE_SENT_FAIL = 0x15;

	public static final int MSG_TO_RSTEP2_VCODE_FAIL = 0x16;
	public static final int MSG_TO_RSTEP2_VCODE_SUCCESS = 0x17;

	public static final int MSG_TO_LOGIN_EX_CONN_TIMEOUT = 0x02;
	public static final int MSG_TO_LOGIN_EX_UKNOWN = 0x03;
	public static final int MSG_TO_LOGIN_LOGIN_SUCCESS = 0x04;
	public static final int MSG_TO_LOGIN_LOGIN_FAIL = 0x05;
	public static final int MSG_TO_LOGIN_COMMAND_FORCE_UPDATE = 0x06;
	public static final int MSG_TO_LOGIN_COMMAND_ACCOMPLISHED = 0x07;

	public static final int MSG_TO_MEMBERINFO_EX_CONN_TIMEOUT = 0X22;
	public static final int MSG_TO_MEMBERINFO_EX_UNKNOWN = 0X23;
	public static final int MSG_TO_MEMBERINFO_UPDATEMEMBER_SUCCESS = 0x24;
	public static final int MSG_TO_MEMBERINFO_UPDATEMEMBER_FAIL = 0x25;
	public static final int MSG_TO_MEMBERINFO_LOGIN_FAIL = 0x26;
	public static final int MSG_TO_MEMBERINFO_HPICKER_CHANGED = 0x27;

	public static final int MSG_TO_IWANT_CANNT_GET_LOCATION = 0x31;
	public static final int MSG_TO_IWANT_EX_CONN_TIMEOUT = 0x32;
	public static final int MSG_TO_IWANT_EX_UNKNOWN = 0x33;
	public static final int MSG_TO_IWANT_CANNT_GET_DESTINATION = 0x34;
	public static final int MSG_TO_IWANT_GOT_DESTINATION = 0x35;
	public static final int MSG_TO_IWANT_GOT_LOCATION = 0x36;
	public static final int MSG_TO_IWANT_CANCEL_LOCATING = 0x37;
	public static final int MSG_TO_IWANT_CANCEL_GET_DESTINATION = 0x38;
	public static final int MSG_TO_IWANT_GET_LOCATION = 0x39;
	

	public static final int MSG_TO_MAIN_HPICKER_CHANGED = 0x41;
	public static final int MSG_TO_MAIN_GOT_PLIST = 0x42;
	public static final int MSG_TO_MAIN_EX_CONN_TIMEOUT = 0x43;
	public static final int MSG_TO_MAIN_EX_UNKNOWN = 0x44;
	public static final int MSG_TO_MAIN_GOT_PORTRAIT_BYTES = 0x45;
	public static final int MSG_TO_MAIN_INFOWINDOW_DRAWN = 0x46;
	//��ϵ���ҵ���TA
	public static final int MSG_TO_MAIN_IT_IS_TA = 0x47;
	//��ϵ�󣬲��Ǻ��ʵ�TA
	public static final int MSG_TO_MAIN_NOT_TA = 0x48;
	
	

	public static final int MSG_TO_FEEDBACK_EX_UNKNOWN = 0x51;
	public static final int MSG_TO_FEEDBACK_SUCCESS = 0x52;
	public static final int MSG_TO_FEEDBACK_FAIL = 0x53;
	

	public enum RESPONSE_CODE {
		RESPONSE_CODE_LOGIN_SUCCESS(0x10), RESPONSE_CODE_LOGIN_FAIL(0x20);
		private int code;

		private RESPONSE_CODE(int theCode) {
			this.code = theCode;
		}

		public int getCode() {
			return this.code;
		}
	}

	public enum MSG {
		MSG_REGISTER_UPDATE_BT_RESEND(0x10), MSG_IWANT_HSV(0x20);
		private int msgCode;

		private MSG(int theCode) {
			this.msgCode = theCode;
		}

		public int getMsgCode() {
			return this.msgCode;
		}
	}

	public static String SHARED_PREFERENCES_NAME = "properties";

	public static final String SMS_CONTENT = "һ��ƴ����";

	public static final int CONNETION_TIMEOUT = 10000;

	/**
	 * Constant used for the retrieving the last location provider
	 */
	public static final String LOCATION_PROVIDER = "LOCATION_PROVIDER";
	/**
	 * Key for retrieving the phone number
	 */
	public static final String PREFERENCE_PHONE_NUMBER = "PREFERENCE_PHONE_NUMBER";

	public static final String PREFERENCE_IMEI = "PREFERENCE_IMEI";

	private static List<Activity> activityList = new ArrayList<Activity>();
	public static MsgHandler msgHandler;

	public void onCreate() {
		super.onCreate();

		// ����Portrait�ļ��к�Ĭ��ͷ���ļ���
		// ��õķ�ʽ�Ǽ���û��Ƿ����״�ʹ�ã�����ǵĻ��������ļ����ļ��С�
		File myDefaultPortraitFile = new File(getPortraitFilesDir(),
				CONS_PORTRAIT_DEFAULT_NAME);
		if (!myDefaultPortraitFile.exists()) {
			createDefaultPortraitFile();
		}

		activityList = new ArrayList<Activity>();

		msgHandler = new MsgHandler();
	}

	public void createDefaultPortraitFile() {
		File portraitDir = getPortraitFilesDir();
		File defaultPortraitFile = new File(portraitDir,
				CONS_PORTRAIT_DEFAULT_NAME);
		if (!defaultPortraitFile.exists()){
			try {
				Bitmap bmp = BitmapFactory.decodeResource(getResources(),
						R.drawable.portrait_default_ta);
				FileOutputStream out;
				out = new FileOutputStream(defaultPortraitFile);
				bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
				out.close();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}

	}

	/**
	 * �˳����е�activity.
	 */
	public void onTerminate() {
		super.onTerminate();
		for (Activity activity : activityList) {
			Log.d("iwantApp onTerminate", "activity is:" + activity.getClass());
			if(activity != null){
				activity.finish();
				activityList.remove(activity);
			}			
		}
//		android.os.Process.killProcess(android.os.Process.myPid());
	}


	/**
	 * Returns the last stored value for a property (uses Android persistent
	 * store)
	 * 
	 * @param key
	 *            name of the property to be retrieved
	 * @return value of the property from the persistent store
	 */
	public String getProperty(String key) {
		SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME,
				MODE_PRIVATE);
		return prefs.getString(key, "");
	}

	/**
	 * Saves a property on persistent store (uses Android persistent store)
	 * 
	 * @param key
	 *            name of the property to be saved
	 * @param value
	 *            value of the property to be saved
	 */
	public void setProperty(String key, String value) {
		SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME,
				MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = prefs.edit();
		prefEditor.putString(key, value);
		prefEditor.commit();
	}

	public String getIMEI() {
		TelephonyManager telMgr = (TelephonyManager) this
				.getSystemService(TELEPHONY_SERVICE);
		String IMEI = telMgr.getDeviceId();
		return IMEI.toString();
	}

	public String getIMSI() {
		TelephonyManager telMgr = (TelephonyManager) this
				.getSystemService(TELEPHONY_SERVICE);
		String IMSI = telMgr.getSubscriberId();
		return IMSI;
	}

	public String getMdnPhone() {
		TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		// sImeiPhone = mTelephonyMgr.getDeviceId();
		// sImsiPhone = mTelephonyMgr.getSubscriberId();
		String mdnPhone = mTelephonyMgr.getLine1Number();

		return mdnPhone;
	}

	public String getMdnSaved() {
		SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME,
				MODE_PRIVATE);
		String mdnSaved = prefs.getString(PROPERTIES_MDN_NAME, null);

		return mdnSaved;
	}

	public void setMdnSaved(String mdnSaved) {
		SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME,
				MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = prefs.edit();
		prefEditor.putString(PROPERTIES_MDN_NAME, mdnSaved);
		prefEditor.commit();
		return;
	}

	public String getTaID() {
		SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME,
				MODE_PRIVATE);
		String taID = prefs.getString(ONTOLOGY_TAID, null);
		return taID;
	}

	public void setTaID(String taID) {
		SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME,
				MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = prefs.edit();
		prefEditor.putString(ONTOLOGY_TAID, taID);
		prefEditor.commit();
		return;
	}
	public boolean isUserRookie() {
		SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME,
				MODE_PRIVATE);
		return prefs.getBoolean(ONTOLOGY_ISROOKIE, true);
	}

	public void setUserIsNotRookie() {
		SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME,
				MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = prefs.edit();
		prefEditor.putBoolean(ONTOLOGY_ISROOKIE, false);
		prefEditor.commit();
		return;
	}

	public String getServerBaseURL() {

		return "http://"
				+ getApplicationContext().getResources().getString(
						R.string.host)
				+ ":"
				+ getApplicationContext().getResources().getString(
						R.string.port) 
						+getApplicationContext().getResources().getString(
								R.string.folder);
	}

	public void setMember(Member member) {
		SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME,
				MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = prefs.edit();
		prefEditor.putString(ONTOLOGY_TAID, member.getId());
		prefEditor.putString(ONTOLOGY_PHONENUM, member.getPhoneNum());
		prefEditor.putString(ONTOLOGY_IMSI, member.getImsi());
		prefEditor.putString(ONTOLOGY_GENDER, member.getGender());

		prefEditor.putInt(ONTOLOGY_AGE, member.getAge());
		prefEditor.putString(ONTOLOGY_PORTRAITFILE,
				member.getPortraitFileName());
		prefEditor.commit();
		return;
	}

	/**
	 * ��ȡpreference�еĻ�Ա��Ϣ������һ��member�������û��member��Ϣ��Ҳ�᷵��һ���ն���
	 * 
	 * @return
	 */
	public Member getMember() {
		Member m = new Member();
		SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME,
				MODE_PRIVATE);
		m.setId(prefs.getString(ONTOLOGY_TAID, null));
		m.setPhoneNum(prefs.getString(ONTOLOGY_PHONENUM, null));
		m.setAge(prefs.getInt(ONTOLOGY_AGE, 0));
		m.setGender(prefs.getString(ONTOLOGY_GENDER, null));
		m.setImsi(prefs.getString(ONTOLOGY_IMSI, null));
		m.setPortraitFileName(prefs.getString(ONTOLOGY_PORTRAITFILE, null));
		return m;
	}

	/**
	 * ���portrait���ļ�Ŀ¼����������ھƴ�����Ŀ¼ΪiwantuĿ¼�´��������ļ���portraits.
	 * 
	 * @return
	 */
	public File getPortraitFilesDir() {
		File mediaStorageDir = new File(this.getFilesDir(),
				CONS_PORTRAIT_FOLDER_NAME);

		if (!mediaStorageDir.exists()) {
			mediaStorageDir.mkdirs();
		}
		return mediaStorageDir;
	}

	/**
	 * ��ȡ����destiantion��csv�ļ�, ������ cityName��ص�destination list�� �ļ�������utf-8�ı��롣
	 * 
	 * @param file
	 * @return ArrayList<Destination>
	 */
	public ArrayList<Destination> getCommonDestFromResource(int res_id,
			String theCityName) {
		ArrayList<Destination> dList = new ArrayList<Destination>();
		try {
			CSVReader reader;
			InputStream is = getResources().openRawResource(res_id);
			reader = new CSVReader(new InputStreamReader(is));
			String[] nextLineArray;
			while (((nextLineArray = reader.readNext()) != null) && (nextLineArray.length > 0)) {
				String cityName = nextLineArray[0];
				if (cityName != null && cityName.equals(theCityName)) {
					Destination d = new Destination();
					String destName = nextLineArray[1];
					float latitude = Float.parseFloat(nextLineArray[2]);
					float longitude = Float.parseFloat(nextLineArray[3]);
					d.setCityName(cityName);
					d.setDestName(destName);
					d.setLatitude(latitude);
					d.setLongitude(longitude);
					dList.add(d);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return null;
		}
		return dList;
	}

	/**
	 * ���Ĭ�ϵ�portrait�ļ�
	 * 
	 * @return
	 * @throws IOException
	 */
	public File getDefaultPortraitFile() throws IOException {
		// File pDir = getPortraitFilesDir();
		// File pFile = new File(pDir, STR_DEFAULT_PORTRAIT_NAME);
		// if(!pFile.exists()){
		//
		// File sourceFile = getResourceFile(R.drawable.main_hsv_ta_default);
		// FileInputStream inStream = new FileInputStream(sourceFile);
		// FileOutputStream outStream = new FileOutputStream(pFile);
		// FileChannel inChannel = inStream.getChannel();
		// FileChannel outChannel = outStream.getChannel();
		// inChannel.transferTo(0, inChannel.size(), outChannel);
		// inStream.close();
		// outStream.close();
		// }
		// return pFile;
		return getResourceFile(R.drawable.portrait_default_ta);
	}

	public File getResourceFile(int resouceID) {
		File f = new File(this.getResources().getResourceName(resouceID));
		return f;

	}

	public static void addActivity(Activity activity) {
		activityList.add(activity);
	}

	public static void removeActivity(Activity activity) {
		for (Activity a : activityList) {
			if (a == activity) {
				activityList.remove(a);
				break;
			}
		}
	}

	public static Activity getActivity(Class<?> c) {
		for (Activity a : activityList) {
			if (c == a.getClass()) {
				return a;
			}
		}
		return null;
	}

	/**
	 * ��ȡ��ǰ��������״̬.
	 * 
	 * @return
	 */
	public boolean isNetWorkConnected() {
		Context context = getApplicationContext();
		// ��ȡ�ֻ��������ӹ�����󣨰�����wi-fi,net�����ӵĹ���
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				// ��ȡ�������ӹ���Ķ���
				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {
					// �жϵ�ǰ�����Ƿ��Ѿ�����
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			//
		}
		return false;
	}
	
	/**
	 * ��bytes�������ͼ������д�뵽fileName������ļ��С�
	 * @param fileName���ļ���
	 * @param bytes
	 * @return
	 */
	public File createPortraitFile(String fileName, byte[] bytes) {
		File file = new File(getPortraitFilesDir(), fileName);
		try {				 
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(bytes);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
		return file;
	}
/**
 * ͨ���ļ�����ȡdrawable����
 * @param fileName
 * @return
 */
	public Drawable getDrawableFromFileName(String fileName, int reqWidth, int reqHeight) {
		File portraitFile = new File(getPortraitFilesDir().getPath()
				+ File.separator + fileName);
		if (!portraitFile.exists()) {
			return null;
		}
		return getDrawableFromFile(portraitFile, reqWidth, reqHeight);
	}

	/**
	 * ͨ���ļ���ȡdrawable����
	 * @param file
	 * @return
	 */
	public Drawable getDrawableFromFile(File file, int reqWidth, int reqHeight) {
		if (!file.exists()) {
			return null;
		}
		Bitmap bmp = AppUtil.decodeBitmapFromFile(file.getPath(), reqWidth, reqHeight);
		if (null == bmp){
			return null;
		}
		return new BitmapDrawable(getResources(), bmp);
	}
	
	public static class MsgHandler extends Handler {
		public void handleMessage(Message msg) {
			int msgCode = msg.what;
			// ���͸�loginҳ�����Ϣ
			if (msgCode < 0x10) {
				LoginActivity a1 = (LoginActivity) getActivity(LoginActivity.class);
				if (null != a1) {
					a1.handleMsg(msg);
				}
				// ���͸�register step2����Ϣ
			} else if (msgCode < 0x20) {
				RegisterStep2Activity a2 = (RegisterStep2Activity) getActivity(RegisterStep2Activity.class);
				if (null != a2) {
					a2.handleMsg(msg);
				}
				// ���͸� member info����Ϣ
			} else if (msgCode < 0x30) {
				MemberInfoActivity a3 = (MemberInfoActivity) getActivity(MemberInfoActivity.class);
				if (null != a3) {
					a3.handleMsg(msg);
				}
				// ���͸� iwant����Ϣ
			} else if (msgCode < 0x40) {
				IWantActivity a4 = (IWantActivity) getActivity(IWantActivity.class);
				if (null != a4) {
					a4.handleMsg(msg);
				}
				// ���͸�main����Ϣ
			} else if (msgCode < 0x50) {
				MainActivity a5 = (MainActivity) getActivity(MainActivity.class);
				if (null != a5) {
					a5.handleMsg(msg);
				}
				// ���͸�feedback����Ϣ
			} else if (msgCode < 0x60) {
				FeedBackActivity a6 = (FeedBackActivity) getActivity(FeedBackActivity.class);
				if (null != a6) {
					a6.handleMsg(msg);
				}
			}
		};
	};

}
