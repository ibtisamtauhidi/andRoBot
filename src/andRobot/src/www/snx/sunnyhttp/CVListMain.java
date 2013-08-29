package www.snx.sunnyhttp;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class CVListMain extends Activity {

	private MenuItem menuFD;
    private MenuItem menuCB;
    private MenuItem menuSettings;
    private MenuItem menuExit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_cvlist_main);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuFD = menu.add("Face Detection");
        menuCB = menu.add("Color Blob Detection Activity");
        menuSettings = menu.add("Settings");
        menuExit = menu.add("Exit");
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == menuFD) {
    		Intent openCVIntent = new Intent(this, FaceDetectionActivity.class);
    		startActivity(openCVIntent);
        } else if (item == menuCB) {
    		Intent remoteIntent = new Intent(this, ColorBlobDetectionActivity.class);
    		startActivity(remoteIntent);
        } else if (item == menuSettings) {
    		Intent remoteIntent = new Intent(this, MyPreferenceActivity.class);
    		startActivity(remoteIntent);
        } else if (item == menuExit)
            finish();
        return true;
    }
}
