package com.perseus.scorpius;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final String PREF_FILE = "cmdFile";

	public void startRobot(View view) {
		SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
    	SharedPreferences.Editor editor = settings.edit();
        editor.putString("mode","0");
        editor.putString("param","0");
        editor.putString("date","0");
        editor.putString("current","0");
        editor.commit();
    	HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://192.168.43.192:8080/init");
        try {
        	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);          
        	nameValuePairs.add(new BasicNameValuePair("sonar", "0"));
        	post.setEntity(new UrlEncodedFormEntity(nameValuePairs));        	
        	HttpResponse response = client.execute(post);
        	BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        	if(rd.readLine() != null) {
        		initSuccess();
        	}
        } catch (IOException e) {
        		Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();        
        }
	}
	
	public void initSuccess() {
		Intent sensorUpdaterIntent = new Intent(this, sensorUpdaterService.class);
		startService(sensorUpdaterIntent);
		startSensor();
		modeCheckerLoop();
	}
	
    private void startSensor() {
    	final Handler handler = new Handler(); 
        Timer t = new Timer(); 
        t.schedule(new TimerTask() { 
        	public void run() { 
        		handler.post(new Runnable() { 
        			public void run() { 
        				sensorUpdate();
        				startSensor();
        			}
        		}); 
        	} 
        }, 100);
    }
    
	public void sensorUpdate() {
		Intent sensorCloud = new Intent(this, cloudSensor.class);
		startService(sensorCloud);
	}
	
    private void modeCheckerLoop() {
    	final Handler handler = new Handler(); 
        Timer t = new Timer(); 
        t.schedule(new TimerTask() { 
        	public void run() { 
        		handler.post(new Runnable() { 
        			public void run() { 
        				modeChecker();
        				modeCheckerLoop();
        			}
        		}); 
        	} 
        }, 1000); 
    }
    
    public void modeChecker() {
    	SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
    	SharedPreferences.Editor editor = settings.edit();
        String mode = settings.getString("mode", "0");
        String param = settings.getString("param", "0");
        String date = settings.getString("date", "0");
        String current = settings.getString("current", "0");
        if(current!=date) {
            current=date;
            editor.putString("current",current);
            editor.commit();
            startMode(mode,param);
        } else {
        }
		Intent cmdReaderIntent = new Intent(this, CmdReader.class);
		startService(cmdReaderIntent);
    }	
    
    public void startMode(String mode,String param) {
    	Intent intent;
    	if (mode.equals("0")) {
    		return;
    	} else if (mode.equals("1")) {
      		intent = new Intent(this,MobilityEngine.class);
    		startActivity(intent);
    	} else if (mode.equals("2")) {
      		intent = new Intent(this,MobilityEngine.class);
    		startActivity(intent);
    	} else if (mode.equals("3")) {
    		if(param.equals("PHOTO")) {
          		intent = new Intent(this,PhotographyEngine.class);
        		startActivity(intent);
    		}
    	} else if (mode.equals("4")) {
      		intent = new Intent(this,RoverVoiceEngine.class);
    		startActivity(intent);
    	}
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i("hello", "Menu Item selected " + item);
        if(item.toString().equals("Exit")) {
        	finish();
        } else if(item.toString().equals("Help")) {
            Toast.makeText(this, "To do : open wiki in browser", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "To do : close all service and activities. Reset mode and param.", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}