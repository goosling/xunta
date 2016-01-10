package com.ixunta.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import org.apache.http.conn.ConnectTimeoutException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.xml.SimpleXmlHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.ixunta.R;
import com.ixunta.client.db.Member;
import com.ixunta.client.myview.MyHorizontalPicker;
import com.ixunta.client.util.AppUtil;

import eu.janmuller.android.simplecropimage.CropImage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MemberInfoActivity extends Activity implements OnClickListener, OnCheckedChangeListener {
	private static final int AGEPICKER_ITEM_CNT = 5;
	private static final int AGEPICKER_DUMMY_ITEM_CNT = 2;

	private Member member;
	private IwantUApp app;

	private MyHorizontalPicker agePicker;
	
	private int currentAgeIndex;
	private Button bt_confirm;
	private ImageView iv_portrait;
	private RadioButton rb_male;
	private RadioButton rb_female;
	private RadioGroup rg_gender;
	private File portraitFile;
	// 拍照的方式，临时存储图片
	private File tempPortraitFile;

	private long lastPressedTime;

	private static final int REQUEST_CODE_GALLERY = 0x01;
	private static final int REQUEST_CODE_TAKE_PICTURE = 0x02;
	private static final int REQUEST_CODE_CROP_IMAGE = 0x11;

	private static final long DOUBLE_CLICK_EXIT_INTERVAL = 2000;

	private static final String CONS_TEMP_PORTRAIT_FILE_NAME = "ixunta_temp_portrait.png";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		app = (IwantUApp) this.getApplication();
		IwantUApp.addActivity(this);

		setContentView(R.layout.memberinfo);

		agePicker = (MyHorizontalPicker) findViewById(R.id.memberinfo_agepicker);
		bt_confirm = (Button) findViewById(R.id.memberinfo_bt_confirm);
		rb_male = (RadioButton) findViewById(R.id.memberinfo_rb_male);
		rb_female = (RadioButton) findViewById(R.id.memberinfo_rb_female);
		rg_gender = (RadioGroup) findViewById(R.id.memberinfo_rg_gender);
		iv_portrait = (ImageView) findViewById(R.id.memberinfo_iv_image);

		iv_portrait.setOnClickListener(this);
		bt_confirm.setOnClickListener(this);
		rg_gender.setOnCheckedChangeListener(this);

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			tempPortraitFile = new File(
					Environment.getExternalStorageDirectory(),
					CONS_TEMP_PORTRAIT_FILE_NAME);
		} else {
			tempPortraitFile = new File(getFilesDir(),
					CONS_TEMP_PORTRAIT_FILE_NAME);
		}

		initAgePicker();

		// member信息来自于savedInstanceState,即系统从内存回收中恢复过来
		if (savedInstanceState != null) {
			member = savedInstanceState.getParcelable("member");
			Log.d("memberinfo oncreate", "member initialized from savedInstanceState");
		} else {
		// memmber信息来自于rstep2或者main
			Intent i = getIntent();
			member = (Member) i.getParcelableExtra(IwantUApp.ONTOLOGY_MEMBER);
			Log.d("memberinfo oncreate", "member initialized from intent");
		}

		// 设置头像
		if (null != member
				&& null != member.getPortraitFileName()
				&& !member.getPortraitFileName().equals(
						IwantUApp.CONS_PORTRAIT_DEFAULT_NAME)) {
			Log.d("memberinfo oncreate", "PortraitFileName is:" + member.getPortraitFileName());
			portraitFile = new File(app.getPortraitFilesDir() + File.separator
					+ member.getPortraitFileName());
			Drawable drawable = Drawable.createFromPath(portraitFile
					.getAbsolutePath());
			iv_portrait.setImageDrawable(drawable);
		} 
		
		// 重置年龄显示
		currentAgeIndex = member.getAge() - IwantUApp.CONS_AGE_MIN + AGEPICKER_DUMMY_ITEM_CNT;
		agePicker.moveToSubView(currentAgeIndex);
		adjustAgePickerTextSize(currentAgeIndex);
		
		// 重置性别显示
		if (member.getGender().equals(IwantUApp.CONS_GENDER_MALE)){
			rb_male.setChecked(true);
		}else{
			rb_female.setChecked(true);
		}

	}

	public void onStart() {
		super.onStart();
		Log.d("memberinfo", "onStart is invoked");
	}

	public void onResume() {
		super.onResume();
		Log.d("memberinfo", "onResume is invoked");
	}

	public void onPause() {
		super.onPause();
		Log.d("memberinfo", "onPause is invoked");
	}

	public void onStop() {
		super.onStop();
		Log.d("memberinfo", "onStop is invoked");
	}

	public void onRestart() {
		super.onRestart();
		Log.d("memberinfo", "onRestart is invoked");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putParcelable("member", member);
		Log.d("memberinfo", "onSaveInstanceState is invoked");
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		Log.d("memberinfo onRestoreInstanceState", "age is:" + member.getAge());

		
		Log.d("memberinfo", "onRestoreInstanceState is invoked");
	}

	/**
	 * 初始化age piker。显示每屏显示3个数字，左右各有一个dummy view，默认显示岁数是25.
	 */
	private void initAgePicker() {
		int agePickerWidth = (int) getResources().getDimension(
				R.dimen.memberinfo_hpicker_width);
		int margin = (int) getResources().getDimension(
				R.dimen.memberinfo_hpicker_subview_margin);

		int height = (int) (agePickerWidth / AGEPICKER_ITEM_CNT) - 2 * margin;

		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) agePicker
				.getLayoutParams();
		lp.height = height;
		lp.width = agePickerWidth;
		agePicker.setLayoutParams(lp);

		agePicker.setDummySubViewCnt(AGEPICKER_DUMMY_ITEM_CNT);
		agePicker.init(AGEPICKER_ITEM_CNT, R.drawable.memberinfo_iv_border,
				RelativeLayout.CENTER_HORIZONTAL, 0,  margin, IwantUApp.msgHandler,
				IwantUApp.MSG_TO_MEMBERINFO_HPICKER_CHANGED);
		agePicker.setDummySubViewCnt(AGEPICKER_DUMMY_ITEM_CNT);

		for (int i = 0; i < AGEPICKER_DUMMY_ITEM_CNT; i++) {
			agePicker.addSubView(new TextView(agePicker.getContext()),
					agePicker.getItemCnt());
		}

		for (int i = IwantUApp.CONS_AGE_MIN; i <=  IwantUApp.CONS_AGE_MAX; i++) {
			TextView tv = new TextView(agePicker.getContext());
			tv.setText(Integer.toString(i));
			tv.setGravity(Gravity.CENTER);
			agePicker.addSubView(tv, agePicker.getItemCnt());
		}
		for (int i = 0; i < AGEPICKER_DUMMY_ITEM_CNT; i++) {
			agePicker.addSubView(new TextView(agePicker.getContext()),
					agePicker.getItemCnt());
		}
	}
	/**
	 * 获取输出文件。文件名为memberid+时间戳的16进制表示，格式为png，存储目录为/portraits.
	 * 
	 * @return
	 */
	private File getOutputImageFile() {

		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			return null;
		}
		File theImageFile = new File(app.getPortraitFilesDir().getPath()
				+ File.separator + member.getId()
				+ String.format("%012x", System.currentTimeMillis())
				+ IwantUApp.CONS_PORTRAIT_FILENAME_SUR);

		return theImageFile;
	}

	/**
	 * 用戶是重新拍照或者在已有相片中选择。
	 * 
	 */
	@SuppressLint("NewApi")
	private void startSelectOpenImageActivity() {

		AlertDialog.Builder alertDialogBuilder;
		if (Build.VERSION.SDK_INT >= 11) {
			alertDialogBuilder = new AlertDialog.Builder(this,
					R.style.progressdialog);
		} else {
			alertDialogBuilder = new AlertDialog.Builder(this);
		}
		alertDialogBuilder
				.setTitle(R.string.memberinfo_open_image_title)
				.setCancelable(true)
				.setItems(R.array.memberinfo_open_image_array,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								switch (which) {
								case 0:
									Intent photoPickerIntent = new Intent(
											Intent.ACTION_PICK);
									photoPickerIntent.setType("image/*");
									startActivityForResult(photoPickerIntent,
											REQUEST_CODE_GALLERY);
									break;
								case 1:
									Intent intent = new Intent(
											MediaStore.ACTION_IMAGE_CAPTURE);
									Uri mImageCaptureUri = null;

									Log.d("memberinfo startSelectOpenImageActivity",
											"tempPortraitFile is:"
													+ tempPortraitFile
															.toString());
									mImageCaptureUri = Uri
											.fromFile(tempPortraitFile);
									intent.putExtra(
											android.provider.MediaStore.EXTRA_OUTPUT,
											mImageCaptureUri);
									intent.putExtra("return-data", true);
									startActivityForResult(intent,
											REQUEST_CODE_TAKE_PICTURE);
									break;
								default:
								}
							}

						});

		AlertDialog selectOpenImageDialog = alertDialogBuilder.create();
		selectOpenImageDialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		InputStream inputStream;
		FileOutputStream fileOutputStream;
		switch (requestCode) {
		case REQUEST_CODE_GALLERY:
			portraitFile = getOutputImageFile();
			try {
				inputStream = getContentResolver().openInputStream(
						data.getData());
				fileOutputStream = new FileOutputStream(portraitFile);
				AppUtil.copyStream(inputStream, fileOutputStream);
				fileOutputStream.close();
				inputStream.close();

				startCropImage();
			} catch (Exception e) {
				Toast.makeText(this, "文件打开失败", Toast.LENGTH_SHORT).show();
			}

			break;
		case REQUEST_CODE_TAKE_PICTURE:
			portraitFile = getOutputImageFile();
			try {
				Log.d("memberinfo REQUEST_CODE_TAKE_PICTURE", "tempFile is:"
						+ tempPortraitFile.toString());
				inputStream = new FileInputStream(tempPortraitFile);
				fileOutputStream = new FileOutputStream(portraitFile);
				AppUtil.copyStream(inputStream, fileOutputStream);
				fileOutputStream.close();
				inputStream.close();
				startCropImage();
			} catch (Exception e) {
				Toast.makeText(this, "文件打开失败", Toast.LENGTH_SHORT).show();
			}
			break;
		case REQUEST_CODE_CROP_IMAGE:
			String pathName = data.getStringExtra(CropImage.IMAGE_PATH);
			if (tempPortraitFile != null && tempPortraitFile.exists()) {
				tempPortraitFile.delete();
			}
			if (pathName == null) {
				return;
			}
			Bitmap bm = BitmapFactory.decodeFile(pathName);
			iv_portrait.setImageBitmap(bm);
			
			//设置member的头像文件
			if ( portraitFile != null && portraitFile.exists()) {
				member.setPortraitFileName(portraitFile.getName());
			}
			Log.d("memberinfo REQUEST_CODE_CROP_IMAGE", "portraitFile is:" + portraitFile.getAbsolutePath());
			break;
		}
	}

	private void startCropImage() {
		Intent intent = new Intent(this, CropImage.class);
		String filePath = portraitFile.getAbsolutePath();
		Log.d("memberinfo REQUEST_CODE_GALLERY", "selected file path is:"
				+ filePath);
		intent.putExtra(CropImage.IMAGE_PATH, filePath);
		intent.putExtra(CropImage.SCALE, true);
		intent.putExtra(CropImage.ASPECT_X, 1);
		intent.putExtra(CropImage.ASPECT_Y, 1);
		intent.putExtra(CropImage.ASPECT_Y, 1);
		intent.putExtra(CropImage.OUTPUT_X, 256);
		intent.putExtra(CropImage.OUTPUT_Y, 256);
		startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
	}

	private class UpdateMemberInfoTask extends
			AsyncTask<MediaType, Void, String> {

		private MultiValueMap<String, Object> formData;

		@Override
		protected void onPreExecute() {
			Resource resource;

			if (null == portraitFile) {
				portraitFile = new File(app.getPortraitFilesDir(),
						IwantUApp.CONS_PORTRAIT_DEFAULT_NAME);
			}
			resource = new FileSystemResource(portraitFile);
			// populate the data to post
			formData = new LinkedMultiValueMap<String, Object>();
			formData.add("id", member.getId());
			formData.add("age", Integer.toString(member.getAge()));
			formData.add("gender", member.getGender());
			formData.add("file", resource);
		}

		@Override
		protected String doInBackground(MediaType... params) {

			final String url = app.getServerBaseURL() + "/updatemember";
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
			requestHeaders.set("Connection", "Close");
			
			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(
					formData, requestHeaders);
			RestTemplate restTemplate = new RestTemplate(true);
			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
			restTemplate.getMessageConverters().add(
					new StringHttpMessageConverter());
			restTemplate.getMessageConverters().add(
					new SimpleXmlHttpMessageConverter());
			ResponseEntity<String> response;
			try {
				response = restTemplate.exchange(url, HttpMethod.POST,
						requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				// 连接服务器超时
				if (e instanceof ConnectTimeoutException
						|| e.getCause() instanceof ConnectTimeoutException) {
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_MEMBERINFO_EX_CONN_TIMEOUT);
					// 未知错误
				} else {
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_MEMBERINFO_EX_UNKNOWN);
				}
				// 其它异常
				return null;
			}
			return (String) response.getBody();
		}

		@Override
		protected void onPostExecute(String responseBody) {
			if (null == responseBody) {
				return;
			}
			try {
				int responseCode = Integer.parseInt(responseBody, 16);
				switch (responseCode) {
				case IwantUApp.RESPONSE_CODE_UPDATEMEMBER_SUCCESS:
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_MEMBERINFO_UPDATEMEMBER_SUCCESS);
					break;
				case IwantUApp.RESPONSE_CODE_UPDATEMEMBER_FAIL:
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_MEMBERINFO_UPDATEMEMBER_FAIL);
					break;
				case IwantUApp.RESPONSE_CODE_LOGIN_FAIL:
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_MEMBERINFO_LOGIN_FAIL);
					break;
				default:
				}
			} catch (Exception e) {
				IwantUApp.msgHandler
						.sendEmptyMessage(IwantUApp.MSG_TO_MEMBERINFO_EX_UNKNOWN);
				return;
			}
		}

		@Override
		protected void onCancelled() {

		}
	}

	/**
	 * 两次点击退出程序。
	 */
	@Override
	public void onBackPressed() {
		// 如果是main页面调用memberinfo,直接返回。
		ComponentName cn = getCallingActivity();

		if (null != cn
				&& MainActivity.class.getName().equals(cn.getClassName())) {
			finish();
			return;
		}
		// 如果是registerstep2调用本页面，两次点击退出。
		long timeNow = System.currentTimeMillis();
		long timeOff = timeNow - lastPressedTime;

		/* toast只能设置为2s和3.5s，对应 Toast.LENGTH_SHORT和Toast.LENGTH_LONG, 其它的值都无效 */
		if (timeOff < DOUBLE_CLICK_EXIT_INTERVAL) {
			this.finish();
		} else {
			lastPressedTime = timeNow;
			Toast.makeText(this, R.string.toast_doubleclick_exit,
					Toast.LENGTH_SHORT).show();
		}
	}

	public void handleMsg(Message msg) {
		switch (msg.what) {
		case IwantUApp.MSG_TO_MEMBERINFO_EX_CONN_TIMEOUT:
			Toast.makeText(getApplicationContext(), R.string.ex_conn_timeout,
					Toast.LENGTH_SHORT).show();
			break;
		case IwantUApp.MSG_TO_MEMBERINFO_EX_UNKNOWN:
			Toast.makeText(getApplicationContext(), R.string.ex_unknown,
					Toast.LENGTH_SHORT).show();
			break;
		case IwantUApp.MSG_TO_MEMBERINFO_UPDATEMEMBER_SUCCESS:
			Toast.makeText(getApplicationContext(),
					R.string.toast_update_member_success, Toast.LENGTH_SHORT)
					.show();

			break;
		case IwantUApp.MSG_TO_MEMBERINFO_UPDATEMEMBER_FAIL:
			Toast.makeText(getApplicationContext(),
					R.string.toast_update_member_fail, Toast.LENGTH_SHORT)
					.show();
			// startIwantActivity();
			break;
		case IwantUApp.MSG_TO_MEMBERINFO_LOGIN_FAIL:
			Toast.makeText(getApplicationContext(),
					R.string.toast_need_relogin, Toast.LENGTH_SHORT).show();
			Intent intent = new Intent();
			intent.setClass(getApplicationContext(), LoginActivity.class);
			break;
		case IwantUApp.MSG_TO_MEMBERINFO_HPICKER_CHANGED:
			Bundle b = msg.getData();
			int index = b.getInt(MyHorizontalPicker.MSG_KEY);
			adjustAgePickerTextSize(index);
			
			member.setAge(index - AGEPICKER_DUMMY_ITEM_CNT + IwantUApp.CONS_AGE_MIN);
			
			Log.d("memberinfo handlemsg", "MSG_TO_MEMBERINFO_HPICKER_CHANGED, index is:" +index);
			break;
		default:
		}
	}

	private void adjustAgePickerTextSize(int index) {
		TextView tv = (TextView) agePicker.getSubviewAt(index);
		TypedValue typedValue = new TypedValue();
		getResources().getValue(R.dimen.memberinfo_hpicker_subview_text_size,
				typedValue, true);
		float defaultTextSize = typedValue.getFloat();
		tv.setTextSize(defaultTextSize);
		for (int i = 1; i <= AGEPICKER_DUMMY_ITEM_CNT; i++) {
			tv = (TextView) agePicker.getSubviewAt(index - i);
			tv.setTextSize((float) (defaultTextSize * Math.pow(0.8, i)));
			tv.setGravity(Gravity.CENTER);
		}
		for (int i = 1; i <= AGEPICKER_DUMMY_ITEM_CNT; i++) {
			tv = (TextView) agePicker.getSubviewAt(index + i);
			tv.setTextSize((float) (defaultTextSize * Math.pow(0.8, i)));
			tv.setGravity(Gravity.CENTER);
		}
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.memberinfo_bt_confirm:

			// 将member写入到本地文件
			app.setMember(member);

			// 更新memmberinfo，无论更新是否成功，都进入下一个页面。
			new UpdateMemberInfoTask().execute();

			Intent i = new Intent();
			i.putExtra(IwantUApp.ONTOLOGY_MEMBER, (Parcelable) member);

			ComponentName cn = getCallingActivity();
			if (null != cn) {
				if (MainActivity.class.getName().equals(cn.getClassName())) {
					setResult(Activity.RESULT_OK, i);
					finish();
				}
			} else {
				i.setClass(MemberInfoActivity.this, IWantActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  
				startActivity(i);			
				finish();
			}

			break;
		case R.id.memberinfo_iv_image:
			startSelectOpenImageActivity();
			break;
		}
	}

	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		switch(checkedId){
		case R.id.memberinfo_rb_male:
			member.setGender(IwantUApp.CONS_GENDER_MALE);
			break;
		case R.id.memberinfo_rb_female:
			member.setGender(IwantUApp.CONS_GENDER_FEMALE);
			break;
		}
	}

}
