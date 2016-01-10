package com.ixunta.client;

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
import com.ixunta.client.db.Feedback;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

/**
 * feedback
 * 
 * @author tom @date 2014-2-15
 * 
 */
public class FeedBackActivity extends ActionBarActivity {

	private IwantUApp app;
	private Feedback feedback;
	private EditText et_email;
	private EditText et_content;
	
	private String taID;
	private String content;
	private String email;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback);
		app = (IwantUApp) this.getApplication();
		IwantUApp.addActivity(this);

		et_email = (EditText) findViewById(R.id.feedback_email);
		et_content = (EditText) findViewById(R.id.feedback_content);

		feedback = new Feedback();

		if (savedInstanceState != null){
			taID = savedInstanceState.getString("taID");
			content = savedInstanceState.getString("content");
			email = savedInstanceState.getString("email");
			et_content.setText(content);
			et_email.setText(email);
		}else{
			Intent i = getIntent();
			taID = i.getStringExtra(IwantUApp.ONTOLOGY_TAID);
		}

		feedback.setTaID(taID);
	}

	public void onStart() {
		super.onStart();
		Log.d("feedback", "onStart is invoked");
	}

	public void onResume() {
		super.onResume();
		Log.d("feedback", "onResume is invoked");
	}

	public void onPause() {
		super.onPause();
		Log.d("feedback", "onPause is invoked");
	}

	public void onStop() {
		super.onStop();
		Log.d("feedback", "onStop is invoked");
	}

	public void onRestart() {
		super.onRestart();
		Log.d("feedback", "onRestart is invoked");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("taID", taID);
		savedInstanceState.putString("email", et_email.getText().toString());
		savedInstanceState.putString("content", et_content.getText().toString());
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.feedback_actionbar, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.feedback_actionbar_send:
			content = et_content.getText().toString();
			email = et_email.getText().toString();
			if (content.length() > 0) {
				feedback.setContent(content);
				feedback.setEmail(email);
				new FeedbackTask().execute();
			}else{
				Toast.makeText(getApplicationContext(),
						R.string.toast_feedback_input, Toast.LENGTH_SHORT).show();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class FeedbackTask extends AsyncTask<MediaType, Void, String> {

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected String doInBackground(MediaType... params) {

			final String url = app.getServerBaseURL() + "/feedback";
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setContentType(MediaType.APPLICATION_XML);
			HttpEntity<Feedback> requestEntity = new HttpEntity<Feedback>(
					feedback, requestHeaders);
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
				return null;
			}
			return (String) response.getBody();
		}

		@Override
		protected void onPostExecute(String responseBody) {
			if (null == responseBody) {
				IwantUApp.msgHandler
						.sendEmptyMessage(IwantUApp.MSG_TO_FEEDBACK_EX_UNKNOWN);
				return;
			}
			try {
				int responseCode = Integer.parseInt(responseBody, 16);
				switch (responseCode) {
				case IwantUApp.RESPONSE_CODE_FEEDBACK_SUCCESS:
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_FEEDBACK_SUCCESS);
					break;
				case IwantUApp.RESPONSE_CODE_FEEDBACK_FAIL:
					IwantUApp.msgHandler
							.sendEmptyMessage(IwantUApp.MSG_TO_FEEDBACK_FAIL);
					break;
				default:
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
		}
	}

	public void handleMsg(Message msg) {
		switch (msg.what) {
		case IwantUApp.MSG_TO_FEEDBACK_EX_UNKNOWN:
		case IwantUApp.MSG_TO_FEEDBACK_FAIL:
			Toast.makeText(getApplicationContext(),
					R.string.toast_feedback_fail, Toast.LENGTH_SHORT).show();
			break;
		case IwantUApp.MSG_TO_FEEDBACK_SUCCESS:
			Toast.makeText(getApplicationContext(),
					R.string.toast_feedback_success, Toast.LENGTH_SHORT).show();
			finish();
			break;
		default:
		}
	}
}
