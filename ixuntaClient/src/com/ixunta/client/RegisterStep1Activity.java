/**
 * 注册页面。
 */

package com.ixunta.client;


import com.ixunta.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;

import android.widget.EditText;
import android.widget.Toast;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;

import com.ixunta.client.db.Register;
import com.ixunta.client.util.AppUtil;

public class RegisterStep1Activity extends Activity {


	private Register register;

	private Button bt_next = null;
	private EditText et_phoneNum = null;
	private IwantUApp app = null;
	
	private long lastPressedTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (IwantUApp) this.getApplication();
		IwantUApp.addActivity(this);

		setContentView(R.layout.registerstep1);
		bt_next = (Button) findViewById(R.id.rstep1_bt_next);
		et_phoneNum = (EditText) findViewById(R.id.rstep1_et);
		app = (IwantUApp) this.getApplication();
		bt_next.setOnClickListener(new OnClickListener_next());
		
		register = (Register) getIntent()
				.getSerializableExtra(IwantUApp.ONTOLOGY_REGISTER);

		if (register == null){
			register = new Register();
		}
		String phoneNum = register.getPhoneNum();
		if (null != phoneNum && phoneNum.length() == 11){
			et_phoneNum.setText(phoneNum.toCharArray(), 0, phoneNum.length());
		}
	}

	private class OnClickListener_next implements OnClickListener {
		public void onClick(View v) {
			String phoneNum = et_phoneNum.getText().toString();
			//检测是否是正确的手机号
			if (false == AppUtil.isPhoneNumber(phoneNum)) {
				Toast.makeText(getApplicationContext(),
						R.string.toast_phonenum_illegal, Toast.LENGTH_SHORT)
						.show();
				return;
			}
			register.setPhoneNum(phoneNum);
			register.setImsi(app.getIMSI());
			register.setOSversion(Integer.toString(android.os.Build.VERSION.SDK_INT));
			register.setPhoneManufacturer(android.os.Build.MANUFACTURER);
			register.setPhoneModel(android.os.Build.MODEL);
			Intent i = new Intent();
			i.setClass(getApplicationContext(), RegisterStep2Activity.class);
			i.putExtra(IwantUApp.ONTOLOGY_REGISTER, register);
			ComponentName cn = getCallingActivity();
			if (null != cn) {
				if (RegisterStep2Activity.class.getName().equals(cn.getClassName())) {
					setResult(Activity.RESULT_OK, i);
					finish();
				}
			}else{
				startActivity(i);
				finish();
			}		
		}
	}
	
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
		} else {
			lastPressedTime = timeNow;
			Toast.makeText(this, R.string.toast_doubleclick_exit,
					Toast.LENGTH_SHORT).show();
		}
	}
}
