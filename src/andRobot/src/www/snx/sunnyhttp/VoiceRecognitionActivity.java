package www.snx.sunnyhttp;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.widget.Toast;

public class VoiceRecognitionActivity extends Activity {
	private static final int REQUEST_CODE = 0;
	public static final String PREF_FILE = "sensorFile";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_recognition);
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // ACTION_WEB_SEARCH
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Voice Recognition");
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this,"ActivityNotFoundException", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            String resultsString = "<div>";
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            for (int i = 0; i< results.size(); i++) resultsString += "<p>"+results.get(i)+"</p>";
            resultsString += "</div>";
            SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
	        SharedPreferences.Editor editor = settings.edit();
	        editor.putString("voicerecognition", resultsString);
	        editor.commit();
        }
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

}
