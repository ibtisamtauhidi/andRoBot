package www.snx.sunnyhttp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Formatter;
import java.util.Properties;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

public class httpServerService extends IntentService{

	HelloServer myServer;
	public static final String PREF_FILE = "sensorFile";
	
	public httpServerService() {
		super("httpServerService");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    return super.onStartCommand(intent,flags,startId);
	}
	
	@Override
	protected void onHandleIntent(Intent arg0) {
		startHTTPServer();
	}

    private static String byteArray2Hex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
	
	private void startremote() {
		Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ComponentName cn = new ComponentName(this,GyroRemote.class);
        intent.setComponent(cn);
        startActivity(intent);
	}
	
	private void startcam() {
		Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ComponentName cn = new ComponentName(this,OpenCVActivity.class);
        intent.setComponent(cn);
        startActivity(intent);
		Intent sensorUpdaterIntent = new Intent(this, sensorUpdaterService.class);
		startService(sensorUpdaterIntent);
	}
	
	private void startphoto() {
		Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ComponentName cn = new ComponentName(this,OpenCVActivity.class);
        intent.setComponent(cn);
        startActivity(intent);
		Intent sensorUpdaterIntent = new Intent(this, sensorUpdaterService.class);
		startService(sensorUpdaterIntent);
	}
	
	private void startvoicerecognition() {
		Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ComponentName cn = new ComponentName(this, VoiceRecognitionActivity.class);
        intent.setComponent(cn);
        startActivity(intent);
	}
	
	private void startHTTPServer() {
		try {
        	myServer = new HelloServer();
		}
		catch(Exception e) {
			Log.e("mServer","Oops -- >"+e);
		}	
	}
	private class HelloServer extends NanoHTTPD {
    	public HelloServer() throws IOException
    	{
    		super(MainActivity.portno, Environment.getExternalStorageDirectory());
    	}

    	public Response serve( String uri, String method, Properties header, Properties parms, Properties files )
    	{
    		if(!MainActivity.httpd_perm) return new NanoHTTPD.Response( HTTP_OK, MIME_HTML,"HTTP SERVER IS OFF. TURN ON HTTP SERVER.");
    		try{
    			String resource = URLDecoder.decode(uri, "UTF-8");
    			if(resource.equals("/")){
    				try {
    					InputStream mbuffer = getAssets().open("index");
    					return new NanoHTTPD.Response( HTTP_OK, MIME_HTML,mbuffer);
    				}
    				catch(IOException err) {
    					return new NanoHTTPD.Response( HTTP_OK, MIME_HTML,"Server Error");
    				}
				}
    			else{
    				String resourceSplitted[] = resource.split("/"); //Example of expected resource download
    				if(resourceSplitted[1].equals("readImage")){
    					SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
    		            String pImg = settings.getString("image", "0");
        				return new NanoHTTPD.Response( HTTP_OK, MIME_HTML,"data:image/jpeg;base64,"+pImg);
    				}
    				else if(resourceSplitted[1].equals("readSensor")){
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
    		            String msg = "{\n\t\"BATTERY\":"+batt+",\n\t\"GYROX\":"+accX+",\n\t\"GYROY\":"+accY+",\n\t\"GYROZ\":"+accZ+",\n\t\"COMPASS\":"+compass+",\n\t\"TEMPERATURE\":"+temp+",\n\t\"LATITUDE\":"+lat+",\n\t\"LONGITUDE\":"+lon+",\n\t\"PROXIMITY\":"+proximity+"\n}";
        				return new NanoHTTPD.Response( HTTP_OK, MIME_HTML,msg);
    				}
    				else if(resourceSplitted[1].equals("readStatus")){
    					return new NanoHTTPD.Response( HTTP_OK, MIME_HTML,"Check Status");
    				}
    				else if((resourceSplitted[1].equals("viewmode"))&&(resourceSplitted[2]!=null)){
    					if(resourceSplitted[2].equals("remote")) {
    						SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
	    			        SharedPreferences.Editor editor = settings.edit();
	    			        editor.putString("mode", "REMOTE");
	    			        editor.commit();
    						OpenCVActivity.viewMode = OpenCVActivity.VIEW_MODE_REMOTE_SURVEILLANCE;
    						return new NanoHTTPD.Response( HTTP_OK, MIME_HTML,"OK");
    					}
    					else if(resourceSplitted[2].equals("face")) {
    						SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
	    			        SharedPreferences.Editor editor = settings.edit();
	    			        editor.putString("mode", "FACE");
	    			        editor.commit();
    						OpenCVActivity.viewMode = OpenCVActivity.VIEW_MODE_FACE_DETECTION;
    						return new NanoHTTPD.Response( HTTP_OK, MIME_HTML,"OK");
    					}
    					else if(resourceSplitted[2].equals("blob")) {    					
	    					SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
	    			        SharedPreferences.Editor editor = settings.edit();
	    			        editor.putString("mode", "BLOB");
	    			        editor.commit();
    						OpenCVActivity.viewMode = OpenCVActivity.VIEW_MODE_BLOB;
    						OpenCVActivity.mIsColorSelected = false;
    						return new NanoHTTPD.Response( HTTP_OK, MIME_HTML,"OK");
    					}
    					else if(resourceSplitted[2].equals("motion")) {
    						SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
	    			        SharedPreferences.Editor editor = settings.edit();
	    			        editor.putString("mode", "MOTION");
	    			        editor.commit();
    						OpenCVActivity.viewMode = OpenCVActivity.VIEW_MODE_BLOB;
    						OpenCVActivity.mIsColorSelected = false;
    						return new NanoHTTPD.Response( HTTP_OK, MIME_HTML,"OK");
    					}
    					else {
    						return new NanoHTTPD.Response( HTTP_OK, MIME_HTML,"ERROR");
    					}
    				}
    				else if(resourceSplitted[1].equals("voice")) {
						if(resourceSplitted[2].equals("say")) {
							String text = parms.getProperty("text");
							MainActivity.sayText(text);
							return new NanoHTTPD.Response( HTTP_OK, MIME_HTML,"OK");
						}
						else if(resourceSplitted[2].equals("hearInit")) {
	    		            	SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
	    		            	startvoicerecognition();
	    		            	SharedPreferences.Editor editor = settings.edit();
	    		            	editor.putString("voicerecognition","waiting");
	    		            	editor.commit();
	            				return new NanoHTTPD.Response( HTTP_OK, MIME_HTML,"OK");
	    				}
	    				else if(resourceSplitted[2].equals("hearStat")) {
	    		            SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
	    		            String vstat = settings.getString("voicerecognition", "waiting");
			            	SharedPreferences.Editor editor = settings.edit();
			            	editor.commit();
			            	return new NanoHTTPD.Response( HTTP_OK, MIME_HTML,vstat);
	    				}
    				}			
    				else if(resourceSplitted[1].equals("start")) {
						if(resourceSplitted[2].equals("vision")) {
							startcam();
							return new NanoHTTPD.Response( HTTP_OK, MIME_HTML,"OK");
						}
	    				else if(resourceSplitted[2].equals("photo")) {
							startphoto();
							return new NanoHTTPD.Response( HTTP_OK, MIME_HTML,"OK");
	    				}
				}			
    				else if(resourceSplitted[1].equals("stop")){    					
    					Date  date = new Date();
    					long dtime= date.getTime();
    					SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
    			        SharedPreferences.Editor editor = settings.edit();
    			        editor.putString("vertical", "0");
    			        editor.putString("horizontal", "0");
    			        editor.putLong("date", dtime);
    			        editor.putString("mode", "0");
    			        editor.commit();
        				return new NanoHTTPD.Response( HTTP_OK, MIME_HTML,"OK");
    				}
    				else if(resourceSplitted[1].equals("gyroremote")) {
    					startremote();
        				return new NanoHTTPD.Response( HTTP_OK, MIME_HTML,"OK");
    				}
    				else if(resourceSplitted[1].equals("agv")){
						OpenCVActivity.viewMode = OpenCVActivity.VIEW_MODE_REMOTE_SURVEILLANCE;
    					String mode = "AGV";
    					String horizontal = parms.getProperty("horizontal");
    					String vertical = parms.getProperty("vertical");
    					Date  date = new Date();
    					long dtime= date.getTime();
    					SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
    			        SharedPreferences.Editor editor = settings.edit();
    			        editor.putString("vertical", vertical);
    			        editor.putString("horizontal", horizontal);
    			        editor.putLong("date", dtime);
    			        editor.putString("mode", mode);
    			        editor.commit();
        				return new NanoHTTPD.Response( HTTP_OK, MIME_HTML,"OK");
    				}
    				else if(resourceSplitted[1].equals("status")){
    					SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
    		            String vertical = settings.getString("vertical", "0");
    		            String horizontal = settings.getString("horizontal", "0");
    		            String mode = settings.getString("mode", "0");
    					Date  cdate = new Date();
    					long ctime= cdate.getTime();
    		            long date = settings.getLong("date", 0);
    		            long diff = ctime-date;
    		            String json = "{\n\t\"VERTICAL\":"+vertical+",\n\t\"HORIZONTAL\":"+horizontal+",\n\t\"MODE\":\""+mode+"\",\n\t\"TDIFF\":"+diff+"\n}";
						return new NanoHTTPD.Response( HTTP_OK, MIME_HTML,json);
    				}
    				else if(resourceSplitted[1].equals("passwd")){
    					String str = MainActivity.password;
    					String sha1 = "";
    					String md5 = "";
    					try {
    						MessageDigest md = MessageDigest.getInstance("SHA-1");
    						sha1 = byteArray2Hex(md.digest(str.getBytes()));
    					} catch (NoSuchAlgorithmException e) {
    						e.printStackTrace();
    					} 
    					try {
    						MessageDigest md = MessageDigest.getInstance("MD5");
    						md5 = byteArray2Hex(md.digest(str.getBytes()));
    					} catch (NoSuchAlgorithmException e) {
    						e.printStackTrace();
    					}
    		            String json = "{\n\t\"MD5\":\""+md5+"\",\n\t\"SHA1\":\""+sha1+"\"\n}";
						return new NanoHTTPD.Response( HTTP_OK, MIME_HTML,json);
    				}
    				else{
    					try {
    						resource = resource.substring(1);
    						InputStream mbuffer = getAssets().open(resource);
    						return new NanoHTTPD.Response( HTTP_OK, MIME_HTML,mbuffer);
    					} catch(IOException err) {
            				return new NanoHTTPD.Response( HTTP_OK, MIME_HTML,err.toString());
    					}
    				}
    			}
    		}
    		catch(UnsupportedEncodingException e){
    			e.printStackTrace();
    		}
    		return new NanoHTTPD.Response( HTTP_OK, MIME_HTML, "ERROR" );
    	}
    }
}