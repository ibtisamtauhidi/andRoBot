package www.snx.sunnyhttp;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class sensorUpdaterService extends IntentService {
	public static final String PREF_FILE = "sensorFile";
	public sensorUpdaterService() {
		super("Scorpius : sensorUpdaterService");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    Log.d("AndRoBot", "sensorUpdaterService started");
	    return super.onStartCommand(intent,flags,startId);
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		startSensorUpdater();
	  }

	private void startSensorUpdater() {
		gpsUpdate();
        compassUpdate();
        accelerometerUpdate();
        proximityUpdate();
        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}
	
    private void compassUpdate() {
        SensorManager sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorService.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (sensor != null) {
          sensorService.registerListener(myCompassEventListener,sensor,SensorManager.SENSOR_DELAY_NORMAL);
        } else {
          Toast.makeText(this, "COMPASS not found",Toast.LENGTH_LONG).show();
        }
    }
    
    private SensorEventListener myCompassEventListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }  
        @Override
        public void onSensorChanged(SensorEvent event) {
          float azimuth = event.values[0];
          SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
          SharedPreferences.Editor editor = settings.edit();
          editor.putFloat("compass", azimuth);
          editor.commit();
        }
    };
      
    private void accelerometerUpdate() {
    	SensorManager sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorService.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sensor != null) {
            sensorService.registerListener(myAccelerometerEventListener,sensor,SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "ACCELEROMETER Sensor not found",Toast.LENGTH_LONG).show();
        }
    }
    
    private SensorEventListener myAccelerometerEventListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
        @Override
        public void onSensorChanged(SensorEvent event) {
            SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
            SharedPreferences.Editor editor = settings.edit();
        	float x = event.values[0];
  	        float y = event.values[1];
  	        float z = event.values[2];
            editor.putFloat("accX", x);
            editor.putFloat("accY", y);
            editor.putFloat("accZ", z);
            editor.commit();
        }
    };
            
    private void gpsUpdate() {
    	LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000 /* milliseconds */,1 /* meter */ ,new MyLocationListener());
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
            SharedPreferences.Editor editor = settings.edit();
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            editor.putFloat("lat", (float)lat);
            editor.putFloat("lon", (float)lon);
            editor.commit();
        }
    }
    private class MyLocationListener implements LocationListener {
    	public void onLocationChanged(Location location) {
            SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
            SharedPreferences.Editor editor = settings.edit();
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            editor.putFloat("lat", (float)lat);
            editor.putFloat("lon", (float)lon);
            editor.commit();
            MainActivity.sms_serv_lat = (float)lat;
            MainActivity.sms_serv_lon = (float)lon;
    	}
        public void onStatusChanged(String s, int i, Bundle b) {
        }
        public void onProviderDisabled(String s) {
        }
        public void onProviderEnabled(String s) {
        }
    }    
         
    private void proximityUpdate() {
        SensorManager sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorService.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (sensor != null) {
            sensorService.registerListener(myProximityEventListener,sensor,SensorManager.SENSOR_DELAY_NORMAL);
          } else {
            Toast.makeText(this, "PROXIMITY Sensor not found",Toast.LENGTH_LONG).show();
          }
    }
    private SensorEventListener myProximityEventListener = new SensorEventListener() {
          @Override
          public void onAccuracyChanged(Sensor sensor, int accuracy) {
          }
          @Override
          public void onSensorChanged(SensorEvent event) {
            SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
            SharedPreferences.Editor editor = settings.edit();
          	float val = event.values[0];
            editor.putFloat("proximity", val);
            editor.commit();
          }
    };
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
    	@Override
    	public void onReceive(Context c, Intent i) {
            SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
            SharedPreferences.Editor editor = settings.edit();
    		int battery = i.getIntExtra("level", 0);
	        int temperature = i.getIntExtra("temperature", 0);
            editor.putInt("batt", battery);
            editor.putInt("temp", temperature/10);
            editor.commit();
            MainActivity.sms_serv_batt = battery;
            MainActivity.sms_serv_temp = temperature;
    	}
    };
}