package com.perseus.scorpius;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class CmdReader extends IntentService {

	public static final String PREF_FILE = "cmdFile";

	public CmdReader() {
		super("Scorpius : CmdReader");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		 return super.onStartCommand(intent,flags,startId);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		startCmdReader();
	}
	
	private void startCmdReader() {
		try {
			  URL url = new URL("http://192.168.43.192:8080/readCmd");
			  HttpURLConnection con = (HttpURLConnection) url.openConnection();
			  readStream(con.getInputStream());
			  } catch (Exception e) {
					Log.i("hello", "Error : "+e);
			  }
	}
	private void readStream(InputStream in) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in));
		    SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
            SharedPreferences.Editor editor = settings.edit();
		    String mode = reader.readLine();
		    String param = reader.readLine();
		    String date = reader.readLine();
		    if((mode==null)||(param==null)||(date==null)) {
			    editor.putString("mode", "0");
			    editor.putString("param", "0");
			    editor.putString("date", "0");
			    editor.commit();
		    	return;
		    }
		    editor.putString("mode", mode);
		    editor.putString("param", param);
		    editor.putString("date", date);
		    editor.commit();
		} catch (Exception e) {
			Log.i("hello", "Error : "+e);
		}
	}
}