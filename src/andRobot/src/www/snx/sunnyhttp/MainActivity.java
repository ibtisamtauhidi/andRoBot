package www.snx.sunnyhttp;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity  implements OnInitListener {
	TextView tv;
    private MenuItem menuOpenCV;
    private MenuItem menuRemote;
    private MenuItem menuPhotography;
    private MenuItem menuSettings;
    private MenuItem menuExit;
	private static TextToSpeech myTTS;
    private int MY_DATA_CHECK_CODE = 0;

	public static final String PREF_FILE = "sensorFile";

	public static String password = "password"; 
	public static int portno = 8080;
	public static boolean stream_perm = false;
	public static boolean sms_perm = false;
	public static boolean twitter_perm = false;
	public static boolean httpd_perm = false;
	public static String ip = "";
	
	public static float sms_serv_lat = 0;
	public static float sms_serv_lon = 0;
	public static int sms_serv_batt = 0;
	public static int sms_serv_temp = 0;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
		
		Date  date = new Date();
		long dtime= date.getTime();
		SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("vertical", "0");
        editor.putString("horizontal", "0");
        editor.putLong("date", dtime);
        editor.putString("mode", "0");
        editor.commit();
        sms_serv_lat = settings.getFloat("lat", 0);
        sms_serv_lon = settings.getFloat("lon", 0);
        sms_serv_temp = settings.getInt("temp", 0);
        sms_serv_batt = settings.getInt("batt", 0);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        password = sharedPrefs.getString("edittext_pass", "password");
        portno = Integer.parseInt(sharedPrefs.getString("edittext_port", "8080"));
        stream_perm = sharedPrefs.getBoolean("stream_pref", false);
        sms_perm = sharedPrefs.getBoolean("sms_pref", false);
        twitter_perm = sharedPrefs.getBoolean("twitter_pref", false);
        httpd_perm = sharedPrefs.getBoolean("httpd_pref", true);

        if(httpd_perm) {
	        Intent httpServerIntent = new Intent(this, httpServerService.class);
			startService(httpServerIntent);
	        
	        try {
				for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
			         NetworkInterface intf = en.nextElement();
			         for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
			             InetAddress inetAddress = enumIpAddr.nextElement();
			             if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress())
			                 ip =  inetAddress.getHostAddress();
			             if(ip.equals("") && !inetAddress.isLoopbackAddress())
			            	 ip =  inetAddress.getHostAddress();
			         }
				}
				if(ip.equals("")) setText("No Internet Connectivity");
				else setText("Open browser at http://"+ip+":"+portno+"/");
			} catch(SocketException e) {
			}	
        } else {
        	setText("HTTP Server is turned OFF");
        	
        }
        
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

    }
	
	public void setText(String param) {
        TextView textview = (TextView)findViewById(R.id.textview);
        textview.setText(param);
	}
		
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
            	myTTS = new TextToSpeech(this, this);
            } else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    public static void sayText(String word) {
        final String words = word;
        myTTS.speak(words, TextToSpeech.QUEUE_FLUSH, null);
    }
    
    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            if(myTTS.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
                myTTS.setLanguage(Locale.US);
        }
        else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }

	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuOpenCV = menu.add("AI/IP Robot");
        menuPhotography = menu.add("Photography/Surveillance Robot");
        menuRemote = menu.add("RC Robot");
        menuSettings = menu.add("Settings");
        menuExit = menu.add("Exit");
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == menuOpenCV) {
    		Intent openCVIntent = new Intent(this, CVListMain.class);
    		startActivity(openCVIntent);
    		Intent sensorUpdaterIntent = new Intent(this, sensorUpdaterService.class);
    		startService(sensorUpdaterIntent);
        } else if (item == menuRemote) {
    		Intent remoteIntent = new Intent(this, GyroRemote.class);
    		startActivity(remoteIntent);
        } else if (item == menuPhotography) {
    		Intent remoteIntent = new Intent(this, PhotograpyActivity.class);
    		startActivity(remoteIntent);      
    		Intent sensorUpdaterIntent = new Intent(this, sensorUpdaterService.class);
    		startService(sensorUpdaterIntent);
        } else if (item == menuSettings) {
    		Intent preferenceIntent = new Intent(this, MyPreferenceActivity.class);
    		startActivity(preferenceIntent);      
        } else if (item == menuExit)
            finish();
        return true;
    }
}