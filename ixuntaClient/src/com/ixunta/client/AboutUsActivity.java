
package com.ixunta.client;

import com.ixunta.R;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * feedback
 * @author tom @date 2014-2-15
 *
 */
public class AboutUsActivity extends ActionBarActivity {
	private IwantUApp app;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aboutus);
		app = (IwantUApp) this.getApplication();
		IwantUApp.addActivity(this);
		
		TextView tv = (TextView) findViewById(R.id.aboutus_tv);
		String content = getResources().getString(R.string.aboutus_content);
		tv.setText(Html.fromHtml(content));
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.aboutus_actionbar, menu);

		return super.onCreateOptionsMenu(menu);
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.aboutus_actionbar_back:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
