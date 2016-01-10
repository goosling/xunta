package com.ixunta.client;

import java.util.Timer;
import java.util.TimerTask;

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

import com.ixunta.R;

import com.ixunta.client.db.Member;
import com.ixunta.client.db.Register;
import com.ixunta.client.util.AppUtil;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.app.Activity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;

public class RegisterStep2Activity extends Activity implements OnClickListener {

	private Register register;
	private Member member;
	private long timeGotVcode;

	private Button bt_pre = null;
	private Button bt_next = null;
	private Button bt_reGetVcode = null;
	private EditText et_vcode = null;
	private TextView tv_info = null;
	private IwantUApp app = null;
	/*
	 * static timer.
	 */
	private static Timer timer;

	private static final int REQUEST_CODE_RSTEP1 = 0x01;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (IwantUApp) this.getApplication();
		IwantUApp.addActivity(this);

		setContentView(R.layout.registerstep2);
		Log.d("rstep2", "onCreate is invoked");

		tv_info = (TextView) findViewById(R.id.rstep2_tv);
		bt_pre = (Button) findViewById(R.id.rstep2_bt_pre);
		bt_next = (Button) findViewById(R.id.rstep2_bt_next);
		bt_reGetVcode = (Button) findViewById(R.id.rstep2_bt_resend);
		et_vcode = (EditText) findViewById(R.id.rstep2_et);

		bt_pre.setOnClickListener(this);
		bt_next.setOnClickListener(this);
		bt_reGetVcode.setOnClickListener(this);
		et_vcode.setInputType(InputType.TYPE_CLASS_NUMBER);

		Intent i = getIntent();
		register = (Register) i
				.getSerializableExtra(IwantUApp.ONTOLOGY_REGISTER);

		tv_info.setText(String.format(
				getResources().getString(R.string.tv_vcode_sent),
				register.getPhoneNum()));

		// APP从资源被系统回收中恢复过来，交由onRestoreInstanceState处理
		if (savedInstanceState != null) {
			return;
		}

		// 获取验证码
		bt_reGetVcode.setText(R.string.view_vcode_resend_2);
		bt_reGetVcode.setEnabled(false);
		bt_next.setEnabled(false);
		new GetVcodeTask().execute();

	}

	public void onStart() {
		super.onStart();
		Log.d("rstep2", "onStart is invoked");
	}

	public void onResume() {
		super.onResume();
		Log.d("rstep2", "onResume is invoked");
		if (timer != null) {
			restoreTimer();
		}
	}

	public void onPause() {
		super.onPause();
		Log.d("rstep2", "onPause is invoked");
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
	}

	public void onStop() {
		super.onStop();
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
		Log.d("rstep2", "onStop is invoked");
	}

	public void onRestart() {
		super.onRestart();
		Log.d("rstep2", "onRestart is invoked");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putLong("timeGotVcode", timeGotVcode);
		Log.d("rstep2", "onSaveInstanceState is invoked");
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		Log.d("rstep2", "onRestoreInstanceState is invoked");

		if (savedInstanceState == null) {
			return;
		}

		timeGotVcode = savedInstanceState.getLong("timeGotVcode");
		restoreTimer();
	}

	/**
	 * 进入会员信息页面。
	 */
	private void startMemberInfoActivity() {
		Intent i = new Intent();
		member.setAge(IwantUApp.CONS_AGE_DEFAULT);
		member.setGender(IwantUApp.CONS_GENDER_DEFAULT);
		member.setPortraitFileName(IwantUApp.CONS_PORTRAIT_DEFAULT_NAME);
		
		// 将member写入到本地文件
		app.setMember(member);
		
		i.putExtra(IwantUApp.ONTOLOGY_MEMBER, (Parcelable)member);		
		i.setClass(this, MemberInfoActivity.class);
		startActivity(i);
		
		finish();
	}

	/**
	 * 检测验证码。
	 * 
	 * @author tom @date 2014-1-23
	 * 
	 */
	private class CheckVcodeTask extends AsyncTask<MediaType, Void, String> {

		private Register r;

		protected void onPreExecute() {
			r = new Register();
			r.setId(register.getId());
			r.setvCode(register.getvCode());
		}

		@Override
		protected String doInBackground(MediaType... params) {

			// The URL for making the POST request
			final String url = app.getServerBaseURL() + "checkVcode";
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.set("Connection", "Close");
			requestHeaders.setContentType(MediaType.APPLICATION_XML);

			HttpEntity<Register> requestEntity = new HttpEntity<Register>(r,
					requestHeaders);
			
			RestTemplate restTemplate = new RestTemplate();
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
				if (e instanceof ConnectTimeoutException
						|| e.getCause() instanceof ConnectTimeoutException) {
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_RSTEP2_EX_CONNECT_TIMEOUT);
					// 未知错误
				} else {
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_RSTEP2_EX_UNKNOWN);

				}
				return null;
			}
			return response.getBody();

		}

		@Override
		protected void onPostExecute(String responseBody) {

			if (null == responseBody) {
				return;
			}
			// 验证失败
			if (IwantUApp.RESPONSE_CODE_LENGTH == responseBody.length()
					&& IwantUApp.RESPONSE_CODE_VCODE_FAIL == Integer.parseInt(
							responseBody, 16)) {
				IwantUApp.msgHandler
						.sendEmptyMessage(IwantUApp.MSG_TO_RSTEP2_VCODE_FAIL);
				return;
			}
			// 验证成功
			if (IwantUApp.OBJECT_ID_LENGTH == responseBody.length()) {
				Bundle b = new Bundle();
				b.putString("memberID", responseBody);
				Message msg = new Message();
				msg.setData(b);
				msg.what = IwantUApp.MSG_TO_RSTEP2_VCODE_SUCCESS;
				IwantUApp.msgHandler.sendMessage(msg);
			}
		}

	}

	/**
	 * 获取验证码
	 * 
	 * @author tom @date 2014-1-23
	 * 
	 */
	private class GetVcodeTask extends AsyncTask<MediaType, Void, String> {
		private Register r;

		@Override
		protected void onPreExecute() {
			r = new Register(register);
		}

		@Override
		protected String doInBackground(MediaType... params) {

			final String url = app.getServerBaseURL() + "getVcode";
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.set("Connection", "Close");
			requestHeaders.setContentType(MediaType.APPLICATION_XML);
			HttpEntity<Register> requestEntity = new HttpEntity<Register>(r,
					requestHeaders);
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			requestFactory.setConnectTimeout(IwantUApp.CONNETION_TIMEOUT);
			RestTemplate restTemplate = new RestTemplate(requestFactory);
			restTemplate.getMessageConverters().add(
					new StringHttpMessageConverter());
			restTemplate.getMessageConverters().add(
					new SimpleXmlHttpMessageConverter());
			ResponseEntity<String> response;
			try {
				response = restTemplate.exchange(url, HttpMethod.POST,
						requestEntity, String.class);
			} catch (Exception e) {
				// 连接服务器超时
				if (e instanceof ConnectTimeoutException
						|| e.getCause() instanceof ConnectTimeoutException) {
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_RSTEP2_EX_CONNECT_TIMEOUT);
					return null;
					// 未知错误
				} else {
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_RSTEP2_EX_UNKNOWN);
					return null;
				}
				// 其它异常
			}
			return response.getBody();

		}

		@Override
		protected void onPostExecute(String responseBody) {
			// 发生异常
			if (null == responseBody) {
				return;
			}
			if (responseBody.equals(String.format("%02x",
					IwantUApp.RESPONSE_CODE_VCODE_SEND_FAIL))) {
				IwantUApp.msgHandler
						.sendEmptyMessage(IwantUApp.MSG_TO_RSTEP2_VCODE_SENT_FAIL);
				return;
			}
			Message msg = new Message();
			msg.what = IwantUApp.MSG_TO_RSTEP2_VCODE_SENT;
			Bundle b = new Bundle();
			b.putString("registerID", responseBody);
			msg.setData(b);
			IwantUApp.msgHandler.sendMessage(msg);
		}
	}

	/**
	 * 定时任务。每秒钟更新重新发送按钮的显示内容，以显示仍需要等待多长时间才能够再次请求验证码。
	 * 
	 * @author tom @date 2014-1-23
	 * 
	 */
	private class TimerTask_SecondsLeft extends TimerTask {
		int secondsLeft;

		public TimerTask_SecondsLeft(int secondsLeft) {
			super();
			this.secondsLeft = secondsLeft;
		}

		@Override
		public void run() {
			secondsLeft--;
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("secondsLeft", secondsLeft);
			msg.setData(b);
			msg.what = IwantUApp.MSG_TO_RSTEP2_SECONDS_LEFT_BEFORE_GETVCODE;
			IwantUApp.msgHandler.sendMessage(msg);
			if (secondsLeft == 0) {
				cancel();
			}
		}
	}

	/**
	 * 用户输入验证码不符合规则。
	 */
	private void onInputVcodeIllegal() {
		Toast.makeText(getApplicationContext(), R.string.toast_vcode_illegal,
				Toast.LENGTH_SHORT).show();
	}

	/**
	 * 消息处理。
	 * 
	 * @param msg
	 */
	public void handleMsg(Message msg) {
		switch (msg.what) {
		// 连接服务器超时
		case IwantUApp.MSG_TO_RSTEP2_EX_CONNECT_TIMEOUT:
			Toast.makeText(getApplicationContext(), R.string.ex_conn_timeout,
					Toast.LENGTH_SHORT).show();
			bt_next.setEnabled(false);
			break;
		// 未知异常
		case IwantUApp.MSG_TO_RSTEP2_EX_UNKNOWN:
			Toast.makeText(getApplicationContext(), R.string.ex_unknown,
					Toast.LENGTH_SHORT).show();
			bt_next.setEnabled(false);
			break;

		// 根据剩余时间更新resend按钮。
		case IwantUApp.MSG_TO_RSTEP2_SECONDS_LEFT_BEFORE_GETVCODE:
			int secondsLeft = msg.getData().getInt("secondsLeft");
			if (secondsLeft == 0) {
				bt_reGetVcode.setText(R.string.view_vcode_resend_1);
				bt_reGetVcode.setEnabled(true);
			} else {
				bt_reGetVcode.setText(secondsLeft
						+ getResources().getString(R.string.view_vcode_resend));
			}
			break;
		case IwantUApp.MSG_TO_RSTEP2_VCODE_SENT_FAIL:
			Toast.makeText(getApplicationContext(),
					R.string.toast_vcode_sent_fail, Toast.LENGTH_SHORT).show();
			// this.onBackPressed();
			break;
		// 验证码发送成功
		case IwantUApp.MSG_TO_RSTEP2_VCODE_SENT:
			Toast.makeText(getApplicationContext(), R.string.toast_vcode_sent,
					Toast.LENGTH_SHORT).show();
			timeGotVcode = System.currentTimeMillis();

			String id = msg.getData().getString("registerID");
			register.setId(id);
			
			bt_reGetVcode.setEnabled(false);
			bt_next.setEnabled(true);

			// 每一秒钟更新一次重新发送按钮的剩余时间
			if (null != timer) {
				timer.cancel();
				timer.purge();
			}
			timer = new Timer();
			timer.schedule(new TimerTask_SecondsLeft(
					IwantUApp.INTERVAL_BETWEEN_GETVCODE), 0, 1000);
			break;
		// 验证码验证成功。
		case IwantUApp.MSG_TO_RSTEP2_VCODE_SUCCESS:
			Toast.makeText(getApplicationContext(),
					R.string.toast_vcode_verify_success, Toast.LENGTH_SHORT)
					.show();
			String memberID = msg.getData().getString("memberID");
			member = new Member();
			member.setId(memberID);
			member.setPhoneNum(register.getPhoneNum());
			member.setImsi(app.getIMSI());
			app.setMember(member);
			startMemberInfoActivity();
			break;
		// 验证码验证失败
		case IwantUApp.MSG_TO_RSTEP2_VCODE_FAIL:
			Toast.makeText(getApplicationContext(),
					R.string.toast_vcode_verify_fail, Toast.LENGTH_SHORT)
					.show();
			break;
		default:
		}
	}

	@Override
	public void onBackPressed() {
		Intent i = new Intent();
		i.setClass(getApplicationContext(), RegisterStep1Activity.class);
		i.putExtra(IwantUApp.ONTOLOGY_REGISTER, register);
		startActivityForResult(i, REQUEST_CODE_RSTEP1);
	}

	/**
	 * 处理按钮点击事件。
	 */
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.rstep2_bt_next:
			String vCode = et_vcode.getText().toString();
			if (false == AppUtil.preCheckVerCode(vCode)) {
				onInputVcodeIllegal();
				return;
			}
			register.setvCode(vCode);
			new CheckVcodeTask().execute();
			break;
		case R.id.rstep2_bt_pre:
			onBackPressed();
			break;
		case R.id.rstep2_bt_resend:
			register.setDatetime(System.currentTimeMillis());
			new GetVcodeTask().execute();
			break;
		default:
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case REQUEST_CODE_RSTEP1:
			Register r = (Register) data
					.getSerializableExtra(IwantUApp.ONTOLOGY_REGISTER);
			// 手机号码发生变化，重新开始
			if (!register.getPhoneNum().equals(r.getPhoneNum())) {
				register.setPhoneNum(r.getPhoneNum());

				if (null != timer) {
					timer.cancel();
					timer.purge();
				}
				tv_info.setText(String.format(
						getResources().getString(R.string.tv_vcode_sent),
						r.getPhoneNum()));
				bt_reGetVcode.setText(R.string.view_vcode_resend_2);
				bt_reGetVcode.setEnabled(false);
				bt_next.setEnabled(false);
				new GetVcodeTask().execute();
			} else {
				// 手机号码没有发生变动，继续原来的计时。
				restoreTimer();
			}
			break;
		default:
		}
	}

	private void restoreTimer() {
		int secondsElapsed = (int) ((System.currentTimeMillis() - timeGotVcode) / 1000);
		int secondsLeft = IwantUApp.INTERVAL_BETWEEN_GETVCODE - secondsElapsed;
		if (secondsLeft <= 0) {
			bt_reGetVcode.setText(R.string.view_vcode_resend_1);
			bt_reGetVcode.setEnabled(true);
		} else {
			bt_reGetVcode.setText(secondsLeft + R.string.view_vcode_resend);
			timer = new Timer();
			timer.schedule(new TimerTask_SecondsLeft(secondsLeft), 0, 1000);

			bt_reGetVcode.setEnabled(false);
			bt_next.setEnabled(true);
		}
	}

}
