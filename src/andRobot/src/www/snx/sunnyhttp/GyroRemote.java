package www.snx.sunnyhttp;

import java.util.Date;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class GyroRemote extends Activity {
	public static final String PREF_FILE = "sensorFile";
	public static final int STOP = 0;
	public static final int START = 1;
	public static final int TRANS = 2;
	public static final int ROTATE = 3;
	public static int GYRO_MODE = STOP;
	boolean isSet = false;
	SensorManager sensorService;
	Sensor sensor;
	
	public void setStop(View view) {
		GYRO_MODE = STOP;
		isSet = false;
	}
	public void setStart(View view) {
		GYRO_MODE = START;
	}
	public void setRotate(View view) {
		GYRO_MODE = ROTATE;
	}
	public void setTrans(View view) {
		GYRO_MODE = TRANS;
	}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_gyro_remote);
        accelerometerUpdate();
    }
    
    @Override
	protected void onPause() {
	    Date  date = new Date();
		long dtime= date.getTime();
		SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("vertical", "0");
        editor.putString("horizontal", "0");
        editor.putLong("date", dtime);
        editor.putString("mode", "AGV");
        editor.commit();
    	sensorService.unregisterListener(myAccelerometerEventListener);
		super.onPause();
	}

	private void accelerometerUpdate() {
    	sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorService.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (sensor != null) {
            sensorService.registerListener(myAccelerometerEventListener,sensor,SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "No Gyroscope",Toast.LENGTH_LONG).show();
        }
    }
    
    private SensorEventListener myAccelerometerEventListener = new SensorEventListener() {
    	float defY = 0;
    	float defZ = 0;
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
        @Override
        public void onSensorChanged(SensorEvent event) {
        	
        	if(GYRO_MODE==STOP) return;
        	
  	        float y = event.values[1];
  	        float z = event.values[2];
  	        TextView textview = (TextView)findViewById(R.id.mText);
  	        
  	        if(!isSet) {
  	        	defY = y;
  	        	defZ = z;
  	  	        textview.setText("Setting Default");
  	  	        isSet = true;
  	        	return;
  	        }
  	        float newY = y-defY;
  	        float newZ = z-defZ;
  	        newY = ((float)newY/25)*-1;
  	        newZ = ((float)newZ/25)*-1;
  	        
  	        if(newZ>1) newZ=1;
  	        if(newZ<-1) newZ=-1;
  	        if(newY>1) newY=1;
  	        if(newY<-1) newY=-1;
  	        
  	        if(GYRO_MODE==ROTATE) newZ = 2;
  	        else if(GYRO_MODE==TRANS) newY = 0;
  	        
  	        String mStrZ = newZ+"";
  	        String mStrY = newY+"";
  	        
  	        Date  date = new Date();
			long dtime= date.getTime();
			SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
	        SharedPreferences.Editor editor = settings.edit();
	        editor.putString("vertical", mStrZ);
	        editor.putString("horizontal", mStrY);
	        editor.putLong("date", dtime);
	        editor.putString("mode", "AGV");
	        editor.commit();
  	        	        
  	        textview.setText("V : "+mStrZ+" H : "+mStrY);
        }
    };
}