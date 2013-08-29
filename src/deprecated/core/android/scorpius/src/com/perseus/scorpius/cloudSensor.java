package com.perseus.scorpius;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class cloudSensor extends IntentService {
	public static final String PREF_FILE = "sensorFile";
	public cloudSensor() {
		super("Scorpius : cloudSensor");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    return super.onStartCommand(intent,flags,startId);
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		postHTTPSensor();
	}
    private void postHTTPSensor() {
    	HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://192.168.43.192:8080/writeSensor");

        try {
        	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);

        	SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
            float accX = settings.getFloat("accX", 0);
            float accY = settings.getFloat("accY", 0);
            float accZ = settings.getFloat("accZ", 0);
            float compass = settings.getFloat("compass", 0);
            float lat = settings.getFloat("lat", 0);
            float lon = settings.getFloat("lon", 0);
            float proximity = settings.getFloat("proximity", 0);
            int temp = settings.getInt("temp", 0);
            int batt = settings.getInt("batt", 0);
             
        	nameValuePairs.add(new BasicNameValuePair("batt", ""+batt));
        	nameValuePairs.add(new BasicNameValuePair("gyroX",""+accX));
        	nameValuePairs.add(new BasicNameValuePair("gyroY",""+accY));
        	nameValuePairs.add(new BasicNameValuePair("gyroZ", ""+accZ));
        	nameValuePairs.add(new BasicNameValuePair("compass", ""+compass));
        	nameValuePairs.add(new BasicNameValuePair("temp", ""+temp/10));
        	nameValuePairs.add(new BasicNameValuePair("lat", ""+lat));
        	nameValuePairs.add(new BasicNameValuePair("lon", ""+lon));
        	nameValuePairs.add(new BasicNameValuePair("proximity", ""+proximity));
        	nameValuePairs.add(new BasicNameValuePair("irdown","0"));
        	nameValuePairs.add(new BasicNameValuePair("sonarfront", "0"));
        	nameValuePairs.add(new BasicNameValuePair("sonarleft", "0"));
        	nameValuePairs.add(new BasicNameValuePair("sonarright", "0"));
        	post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        	
        	HttpResponse response = client.execute(post);
        	BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        	
        	while (rd.readLine() != null) {
        		return;
        	}
        } catch (IOException e) {
        		Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();        
        }
    }
}