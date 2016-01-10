/**
 * ��½ҳ�档���ע������Զ���¼��ת��iwantҳ�棬������Ҫע�ᡣ
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

		//����APP
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
		
		// ������
//		TextView tv = (TextView)findViewById(R.id.login_tv);
//		String content = getResources().getString(R.string.login_introduce);
//		tv.setText(Html.fromHtml(content));

		// ��ʼ��umeng���Զ�����
		initUmengUpdateAgent();

		initProgressDialog();

		// ��������
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
	 * ��¼����ע�ᡣ
	 */
	private void loginOrRegister() {
		member = app.getMember();
		String id = member.getId();
		String imsi = member.getImsi();
		String phoneNum = member.getPhoneNum();
		
		Log.e("login", "id is " + id);
		Log.e("login", "imsi is " + imsi);
		Log.e("login", "phoneNum is " + phoneNum);

		// �û�û��ע������û��������ֻ���
		if (null == id) {
			setShouldRegisterLayout();
			return;
		}
		// û���ֻ��Ż���û��IMSI
		if (null == imsi || phoneNum == null || phoneNum.length() < 11) {
			setShouldRegisterLayout();
			return;
		}
		// �û�����SIM��
		if (!imsi.equals(app.getIMSI())) {
			setShouldRegisterLayout();
			return;
		}
		
		// ע���û�������Ա�ĺϷ��ԣ�
		login = new Login();
		login.setTaID(member.getId());
		login.setPhoneNum(member.getPhoneNum());
		login.setDatetime(System.currentTimeMillis());
		new LoginTask().execute();
	}

	private void initUmengUpdateAgent() {
		// ���汾����
		UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
			public void onUpdateReturned(int updateStatus,
					UpdateResponse updateInfo) {
				// �����Ҫ���£������û�û�к��Ա��θ���
				if (updateStatus == UpdateStatus.Yes
						&& !UmengUpdateAgent.isIgnore(LoginActivity.this,
								updateInfo)) {
					// ���ﲻ��Ҫ�κδ���
				} else {
					// û���°汾����ע����ߵ�¼���̡�
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
					// �����ǿ�Ƹ��¾���ʾ�û�
					if (forceUpdate) {
						Toast.makeText(getApplicationContext(),
								R.string.toast_force_update, Toast.LENGTH_LONG)
								.show();
					} else {
						// ����Ҫǿ�Ƹ��¾�ע����ߵ�¼��
						loginOrRegister();
					}
					break;
				default:
				}
			}
		});
	}

	/**
	 * ����Ҫ��¼ʱ�Ľ��棬��¼��ť���ã�ע�ᰴť������
	 */
	private void setShouldLoginLayout() {
		bt_login.setVisibility(View.VISIBLE);
		bt_register.setVisibility(View.INVISIBLE);
	}

	/**
	 * ����Ҫע��ʱ�Ľ��棬��¼��ť�����ã�ע�ᰴť����
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
	 * �������������� ��ǰֻ��һ���������ǿ�Ƹ��¿ͻ��ˡ�δ������չ �� 20140317
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
				// ���ӷ�������ʱ
				if (e instanceof ConnectTimeoutException
						|| e.getCause() instanceof ConnectTimeoutException) {
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_LOGIN_EX_CONN_TIMEOUT);
					return null;
					// δ֪����
				} else {
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_LOGIN_EX_UKNOWN);
					return null;
				}
				// �����쳣
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
	 * �û���¼�����������������һ��login���󣬲������¼������쳣��
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
				// ���ӷ�������ʱ
				if (e instanceof ConnectTimeoutException
						|| e.getCause() instanceof ConnectTimeoutException) {
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_LOGIN_EX_CONN_TIMEOUT);
					return null;
					// δ֪����
				} else {
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_LOGIN_EX_UKNOWN);
					return null;
				}
				// �����쳣
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
	 * ����ť����¼�
	 */
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		// �����¼��ť����¼
		case R.id.login_bt_login:
			new LoginTask().execute();
			break;

		// ���ע�ᰴť������ע��ҳ�档
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
