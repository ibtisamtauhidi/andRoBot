package com.perseus.scorpius;

import android.os.Bundle;
import android.widget.TextView;
import android.app.Activity;
import android.content.SharedPreferences;

public class MobilityEngine extends Activity {

	public static final String PREF_FILE = "cmdFile";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobility_engine);
    	SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
        String mode = settings.getString("mode", "0");    	
        String param = settings.getString("param", "0");    	
        TextView textviewmode = (TextView)findViewById(R.id.mobility_mode);
        textviewmode.setText(mode);
        TextView textviewparam = (TextView)findViewById(R.id.mobility_param);
        textviewparam.setText(param);
   }
    
}
