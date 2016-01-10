/**
 * 登陆页面。如果注册过就自动登录并转到iwant页面，否则需要注册。
 * 
 * 2013/8/10
 */

package com.ixunta.client;

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
import com.ixunta.client.db.Login;
import com.ixunta.client.db.Member;
import com.umeng.update.UmengDialogButtonListener;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {

	private IwantUApp app;
	private Button bt_register;
	private Button bt_login;

	private LoginTask loginTask;
	private Login login;
	private Member member;

	private ProgressDialog progressDialog;

	private boolean forceUpdate = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		//结束APP
	    if (getIntent().getBooleanExtra("EXIT", false)) {
	         finish();
	         return;
	    }
	    
		app = (IwantUApp) this.getApplication();
		IwantUApp.addActivity(this);

		bt_register = (Button) findViewById(R.id.login_bt_register);
		bt_register.setOnClickListener(this);
		bt_login = (Button) findViewById(R.id.login_bt_login);
		bt_login.setOnClickListener(this);

		bt_register.setVisibility(View.INVISIBLE);
		bt_login.setVisibility(View.INVISIBLE);
		
		// 测试用
//		TextView tv = (TextView)findViewById(R.id.login_tv);
//		String content = getResources().getString(R.string.login_introduce);
//		tv.setText(Html.fromHtml(content));

		// 初始化umeng的自动更新
		initUmengUpdateAgent();

		initProgressDialog();

		// 请求命令
		new CommanderTask().execute();

	}

	public void onStart() {
		super.onStart();
		Log.d("login", "onStart is invoked");
	}

	public void onResume() {
		super.onResume();
	    if (getIntent().getBooleanExtra("EXIT", false)) {
	         finish();
	    }
		Log.d("login", "onResume is invoked");
	}

	public void onPause() {
		super.onPause();
		Log.d("login", "onPause is invoked");

	}

	public void onStop() {
		super.onStop();
		Log.d("login", "onStop is invoked");
	}

	public void onRestart() {
		super.onRestart();
		Log.d("login", "onRestart is invoked");
	}

	/**
	 * 登录或者注册。
	 */
	private void loginOrRegister() {
		member = app.getMember();
		String id = member.getId();
		String imsi = member.getImsi();
		String phoneNum = member.getPhoneNum();
		
		Log.e("login", "id is " + id);
		Log.e("login", "imsi is " + imsi);
		Log.e("login", "phoneNum is " + phoneNum);

		// 用户没有注册或者用户更换了手机。
		if (null == id) {
			setShouldRegisterLayout();
			return;
		}
		// 没有手机号或者没有IMSI
		if (null == imsi || phoneNum == null || phoneNum.length() < 11) {
			setShouldRegisterLayout();
			return;
		}
		// 用户更换SIM卡
		if (!imsi.equals(app.getIMSI())) {
			setShouldRegisterLayout();
			return;
		}
		
		// 注册用户，检测会员的合法性，
		login = new Login();
		login.setTaID(member.getId());
		login.setPhoneNum(member.getPhoneNum());
		login.setDatetime(System.currentTimeMillis());
		new LoginTask().execute();
	}

	private void initUmengUpdateAgent() {
		// 检查版本更新
		UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
			public void onUpdateReturned(int updateStatus,
					UpdateResponse updateInfo) {
				// 如果需要更新，而且用户没有忽略本次更新
				if (updateStatus == UpdateStatus.Yes
						&& !UmengUpdateAgent.isIgnore(LoginActivity.this,
								updateInfo)) {
					// 这里不需要任何代码
				} else {
					// 没有新版本就走注册或者登录流程。
					loginOrRegister();
				}
			}
		});

		UmengUpdateAgent.setDialogListener(new UmengDialogButtonListener() {
			public void onClick(int status) {
				switch (status) {
				case UpdateStatus.Update:
					break;
				case UpdateStatus.Ignore:
				case UpdateStatus.NotNow:
					// 如果是强制更新就提示用户
					if (forceUpdate) {
						Toast.makeText(getApplicationContext(),
								R.string.toast_force_update, Toast.LENGTH_LONG)
								.show();
					} else {
						// 不需要强制更新就注册或者登录。
						loginOrRegister();
					}
					break;
				default:
				}
			}
		});
	}

	/**
	 * 当需要登录时的界面，登录按钮可用，注册按钮不可用
	 */
	private void setShouldLoginLayout() {
		bt_login.setVisibility(View.VISIBLE);
		bt_register.setVisibility(View.INVISIBLE);
	}

	/**
	 * 当需要注册时的界面，登录按钮不可用，注册按钮可用
	 */
	private void setShouldRegisterLayout() {
		bt_login.setVisibility(View.INVISIBLE);
		bt_register.setVisibility(View.VISIBLE);
	}

	private void startIwantActivity() {
		Intent i = new Intent();
		i.setClass(this, IWantActivity.class);		 
		i.putExtra(IwantUApp.ONTOLOGY_MEMBER, (Parcelable) member);
		startActivity(i);
		this.finish();
	}

	/**
	 * 向服务器请求命令。 当前只有一个命令，就是强制更新客户端。未来可扩展 。 20140317
	 * 
	 * @author pom.sul
	 * 
	 */
	private class CommanderTask extends AsyncTask<MediaType, Void, String> {

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected String doInBackground(MediaType... params) {

			final String url = app.getServerBaseURL() + "commander";
			HttpHeaders requestHeaders = new HttpHeaders();
			HttpEntity<String> requestEntity = new HttpEntity<String>("",
					requestHeaders);
			requestHeaders.set("Connection", "Close");
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			requestFactory.setConnectTimeout(IwantUApp.CONNETION_TIMEOUT);
			RestTemplate restTemplate = new RestTemplate(requestFactory);
			restTemplate.getMessageConverters().add(
					new StringHttpMessageConverter());
			ResponseEntity<String> response;
			try {
				response = restTemplate.exchange(url, HttpMethod.GET,
						requestEntity, String.class);
			} catch (Exception e) {
				// 连接服务器超时
				if (e instanceof ConnectTimeoutException
						|| e.getCause() instanceof ConnectTimeoutException) {
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_LOGIN_EX_CONN_TIMEOUT);
					return null;
					// 未知错误
				} else {
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_LOGIN_EX_UKNOWN);
					return null;
				}
				// 其它异常
			}
			return (String) response.getBody();
		}

		@Override
		protected void onPostExecute(String responseBody) {
			try {
				String[] responseCodeStrList = responseBody
						.split(IwantUApp.CONS_COMMAND_SPLITTER);
				for (String responseCodeStr : responseCodeStrList) {
					int responseCode = Integer.parseInt(responseCodeStr, 16);
					switch (responseCode) {
					case IwantUApp.RESPONSE_CODE_COMMAND_FORCE_UPDATE:
						IwantUApp.msgHandler
								.sendEmptyMessage(IwantUApp.MSG_TO_LOGIN_COMMAND_FORCE_UPDATE);
						break;
					case IwantUApp.RESPONSE_CODE_COMMAND_NOTHING:
					default:
					}
				}
				IwantUApp.msgHandler
						.sendEmptyMessage(IwantUApp.MSG_TO_LOGIN_COMMAND_ACCOMPLISHED);
			} catch (Exception e) {
				// do nothing
				IwantUApp.msgHandler
						.sendEmptyMessage(IwantUApp.MSG_TO_LOGIN_COMMAND_ACCOMPLISHED);
			}
		}

		@Override
		protected void onCancelled() {
		}
	}

	/**
	 * 用户登录任务处理，向服务器发送一个login对象，并处理登录结果及异常。
	 * 
	 * @author tom @date 2014-1-20
	 * 
	 */
	private class LoginTask extends AsyncTask<MediaType, Void, String> {


		@Override
		protected void onPreExecute() {
			
			progressDialog.setMessage(getResources().getString(
					R.string.login_toast_logining));
			progressDialog.show();
		}

		@Override
		protected String doInBackground(MediaType... params) {

			final String url = app.getServerBaseURL() + "login";
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setContentType(MediaType.APPLICATION_XML);
			requestHeaders.set("Connection", "Close");
			HttpEntity<Login> requestEntity = new HttpEntity<Login>(login,
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
							.sendEmptyMessage(IwantUApp.MSG_TO_LOGIN_EX_CONN_TIMEOUT);
					return null;
					// 未知错误
				} else {
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_LOGIN_EX_UKNOWN);
					return null;
				}
				// 其它异常
			}
			return (String) response.getBody();
		}

		@Override
		protected void onPostExecute(String responseBody) {
			if (progressDialog != null && progressDialog.isShowing() ) {
				progressDialog.dismiss();
			}
			if (null == responseBody) {
				return;
			}
			try {
				int responseCode = Integer.parseInt(responseBody, 16);
				switch (responseCode) {
				case IwantUApp.RESPONSE_CODE_LOGIN_FAIL:
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_LOGIN_LOGIN_FAIL);
					break;
				case IwantUApp.RESPONSE_CODE_LOGIN_SUCCESS:
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_LOGIN_LOGIN_SUCCESS);
					break;
				default:
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_LOGIN_EX_UKNOWN);
					break;
				}
			} catch (Exception e) {
				IwantUApp.msgHandler
						.sendEmptyMessage(IwantUApp.MSG_TO_LOGIN_EX_UKNOWN);
				return;
			}
		}

		@Override
		protected void onCancelled() {
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			setShouldLoginLayout();
		}
	}

	public void handleMsg(Message msg) {
		switch (msg.what) {
		case IwantUApp.MSG_TO_LOGIN_EX_CONN_TIMEOUT:
			Toast.makeText(getApplicationContext(), R.string.ex_conn_timeout,
					Toast.LENGTH_SHORT).show();
			break;
		case IwantUApp.MSG_TO_LOGIN_EX_UKNOWN:
			Toast.makeText(getApplicationContext(), R.string.ex_unknown,
					Toast.LENGTH_SHORT).show();
			break;
		case IwantUApp.MSG_TO_LOGIN_LOGIN_FAIL:
			Toast.makeText(getApplicationContext(),
					R.string.login_toast_login_fail, Toast.LENGTH_LONG).show();
			setShouldRegisterLayout();
			break;
		case IwantUApp.MSG_TO_LOGIN_LOGIN_SUCCESS:
			startIwantActivity();
			break;
		case IwantUApp.MSG_TO_LOGIN_COMMAND_FORCE_UPDATE:
			this.forceUpdate = true;
			break;
		case IwantUApp.MSG_TO_LOGIN_COMMAND_ACCOMPLISHED:
			UmengUpdateAgent.update(getApplicationContext());
			break;
		default:
		}
	}

	/**
	 * 处理按钮点击事件
	 */
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		// 点击登录按钮，登录
		case R.id.login_bt_login:
			new LoginTask().execute();
			break;

		// 点击注册按钮，启动注册页面。
		case R.id.login_bt_register:
			Intent i = new Intent();
			i.setClass(getApplicationContext(), RegisterStep1Activity.class);
			startActivity(i);
			break;
		default:
		}
	}
	

	private void initProgressDialog() {
		progressDialog = new ProgressDialog(LoginActivity.this,
				R.style.progressdialog);

		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage(getResources().getString(
				R.string.login_toast_logining));
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				if (loginTask != null && !loginTask.isCancelled()) {
					loginTask.cancel(true);
					setShouldLoginLayout();
				}
			}
		});
	}
}
