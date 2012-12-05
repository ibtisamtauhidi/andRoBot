package com.perseus.scorpius;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import android.widget.TextView;
import android.widget.Toast;

public class RoverVoiceEngine extends Activity implements OnInitListener {
    
	private TextToSpeech myTTS;
    private int MY_DATA_CHECK_CODE = 0;
	public static final String PREF_FILE = "cmdFile";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rover_voice_engine);
            Intent checkTTSIntent = new Intent();
            checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
            setText();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
            	myTTS = new TextToSpeech(this, this);
            }
            else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    public void setText() {
    	SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
        String param = settings.getString("param", "0");    	
        TextView textview = (TextView)findViewById(R.id.intro);
        textview.setText(param);
        TextView enteredText = (TextView)findViewById(R.id.intro);
        final String words = enteredText.getText().toString();
    	final Handler handler = new Handler(); 
        Timer t = new Timer(); 
        t.schedule(new TimerTask() { 
        	public void run() { 
        		handler.post(new Runnable() { 
        			public void run() { 
        				myTTS.speak(words, TextToSpeech.QUEUE_FLUSH, null);
        				finish();
        			}
        		}); 
        	} 
        }, 300);
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
}