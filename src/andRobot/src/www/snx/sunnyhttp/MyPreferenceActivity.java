package www.snx.sunnyhttp;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Window;

import www.snx.sunnyhttp.R;

public class MyPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);       
        addPreferencesFromResource(R.xml.preferences);
    }

	@Override
	protected void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals("edittext_pass")) MainActivity.password = sharedPreferences.getString(key, "password");
		else if(key.equals("edittext_port")) MainActivity.portno = Integer.parseInt(sharedPreferences.getString(key, "8080"));
		else if(key.equals("stream_pref")) MainActivity.stream_perm = sharedPreferences.getBoolean(key, false);
		else if(key.equals("sms_pref")) MainActivity.sms_perm = sharedPreferences.getBoolean(key, false);
		else if(key.equals("twitter_pref")) MainActivity.twitter_perm = sharedPreferences.getBoolean(key, false);
		else if(key.equals("httpd_pref")) MainActivity.httpd_perm = sharedPreferences.getBoolean(key, false);
	}
}
