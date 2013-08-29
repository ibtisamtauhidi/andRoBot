package www.snx.sunnyhttp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;

public class FaceDetectionActivity extends Activity {
	public static final String PREF_FILE = "sensorFile";
	private static final String TAG = "openCV";
	private ColorBlobDetectionView mView;
	private File                  mCascadeFile;
    private CascadeClassifier     mJavaDetector;
    private Scalar   			  FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    private float                 mRelativeFaceSize = 0;
    private int					  mAbsoluteFaceSize = 0;
	public static boolean mIsColorSelected = false;
	public boolean facedetectedinited = false;
	
	private BaseLoaderCallback  mOpenCVCallBack = new BaseLoaderCallback(this) {
	    	@Override
	    	public void onManagerConnected(int status) {
	    		switch (status) {
					case LoaderCallbackInterface.SUCCESS:
					{
						Log.i(TAG, "OpenCV loaded successfully");
						// Create and set View
						mView = new ColorBlobDetectionView(mAppContext);
						setContentView(mView);
						// Check native OpenCV camera
						if( !mView.openCamera() ) {
							AlertDialog ad = new AlertDialog.Builder(mAppContext).create();
							ad.setCancelable(false); // This blocks the 'BACK' button
							ad.setMessage("Fatal error: can't open camera!");
							ad.show();
						}
					} break;
					default:
					{
						super.onManagerConnected(status);
					} break;
				}
	    	}
		};
	
	public FaceDetectionActivity()
	{
		Log.i(TAG, "Instantiated new " + this.getClass());
	}
	
	public void finishActivity() {
		finish();
	}
	
    @Override
	protected void onPause() {
        Log.i(TAG, "onPause");
		super.onPause();
		if (null != mView) mView.releaseCamera();
	}

    public void initfacedetect() {
    	facedetectedinited = true;
        try {
            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            if (mJavaDetector.empty()) {
                Log.e(TAG, "Failed to load cascade classifier");
                mJavaDetector = null;
            } else
                Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
            
            cascadeDir.delete();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }
    }
    

	@Override
	protected void onResume() {
        Log.i(TAG, "onResume");
		super.onResume();
		if( (null != mView) && !mView.openCamera() ) {
			AlertDialog ad = new AlertDialog.Builder(this).create();
			ad.setCancelable(false); // This blocks the 'BACK' button
			ad.setMessage("Fatal error: can't open camera!");
			ad.show();
		}
	}
	    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Log.i(TAG, "Trying to load OpenCV library");
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mOpenCVCallBack))
        {
        	Log.e(TAG, "Cannot connect to OpenCV Manager");
        }   
    	SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("mode", "FACE");
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu");
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "Menu Item selected " + item);
        return true;
    }

    public abstract class SampleCvViewBase extends SurfaceView implements SurfaceHolder.Callback, Runnable {
        
        private SurfaceHolder       mHolder;
        private VideoCapture        mCamera;

        public SampleCvViewBase(Context context) {
            super(context);
            mHolder = getHolder();
            mHolder.addCallback(this);
            Log.i(TAG, "Instantiated new " + this.getClass());
        }

        public boolean openCamera() {
            Log.i(TAG, "openCamera");
            synchronized (this) {
    	        releaseCamera();
    	        mCamera = new VideoCapture(Highgui.CV_CAP_ANDROID);
    	        if (!mCamera.isOpened()) {
    	            mCamera.release();
    	            mCamera = null;
    	            Log.e(TAG, "Failed to open native camera");
    	            return false;
    	        }
    	    }
            return true;
        }
        
        public void releaseCamera() {
            Log.i(TAG, "releaseCamera");
            synchronized (this) {
    	        if (mCamera != null) {
    	                mCamera.release();
    	                mCamera = null;
                }
            }
        }
        
        public void setupCamera(int width, int height) {
            Log.i(TAG, "setupCamera("+width+", "+height+")");
            synchronized (this) {
                if (mCamera != null && mCamera.isOpened()) {
                    List<Size> sizes = mCamera.getSupportedPreviewSizes();
                    int mFrameWidth = width;
                    int mFrameHeight = height;

                    // selecting optimal camera preview size
                    {
                        double minDiff = Double.MAX_VALUE;
                        for (Size size : sizes) {
                            if (Math.abs(size.height - height) < minDiff) {
                                mFrameWidth = (int) size.width;
                                mFrameHeight = (int) size.height;
                                minDiff = Math.abs(size.height - height);
                            }
                        }
                    }

                    mCamera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, mFrameWidth);
                    mCamera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, mFrameHeight);
                }
            }

        }
        
        public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
            Log.i(TAG, "surfaceChanged");
            setupCamera(width, height);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            Log.i(TAG, "surfaceCreated");
            (new Thread(this)).start();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i(TAG, "surfaceDestroyed");
            releaseCamera();
        }

        protected abstract Bitmap processFrame(VideoCapture capture);

        public void run() {
            Log.i(TAG, "Starting processing thread");
            while (true) {
                Bitmap bmp = null;

                synchronized (this) {
                    if (mCamera == null)
                        break;

                    if (!mCamera.grab()) {
                        Log.e(TAG, "mCamera.grab() failed");
                        break;
                    }

                    bmp = processFrame(mCamera);
                }

                if (bmp != null) {
                    Canvas canvas = mHolder.lockCanvas();
                    if (canvas != null) {
                        canvas.drawBitmap(bmp, (canvas.getWidth() - bmp.getWidth()) / 2, (canvas.getHeight() - bmp.getHeight()) / 2, null);
                        mHolder.unlockCanvasAndPost(canvas);
                    }
                    bmp.recycle();
                }
            }

            Log.i(TAG, "Finishing processing thread");
        }
    }
    
    public class ColorBlobDetectionView extends SampleCvViewBase {

    	private Mat mRgba;
    	private Mat mGray;
    	private Mat mIntermediateMat;
    	
    	
    	public ColorBlobDetectionView(Context context)
    	{
            super(context);
    	}
    	
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            synchronized (this) {
                // initialize Mat before usage
                mRgba = new Mat();
                mGray = new Mat();
                mIntermediateMat = new Mat();
            }            
            super.surfaceCreated(holder);
        }
    	

    	@Override
    	protected Bitmap processFrame(VideoCapture capture) {
    		String PREF_FILE = "sensorFile";
    		capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
			Bitmap bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);    		
    		
	        		if(!facedetectedinited) initfacedetect();
	        		capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
	                capture.retrieve(mGray, Highgui.CV_CAP_ANDROID_GREY_FRAME);
	                if (mAbsoluteFaceSize == 0) {
	                	int height = mGray.rows();
	                	if (Math.round(height * mRelativeFaceSize) > 0);
	                	{
	                		mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
	                	}
	                }
	                MatOfRect faces = new MatOfRect();
	                if (mJavaDetector != null)
	                    mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
	                Rect[] facesArray = faces.toArray();
	                for (int i = 0; i < facesArray.length; i++) {
	                    Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
	                    Log.d("Scorpius","Face Detected");
	                }
            try {
            	Utils.matToBitmap(mRgba, bmp);
            } catch(Exception e) {
            	Log.e(TAG, "Utils.matToBitmap() throws an exception: " + e.getMessage());
                bmp.recycle();
                bmp = null;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);   
            byte[] b = baos.toByteArray();
            String pStr = Base64.encodeToString(b, Base64.DEFAULT);
            
            SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("image", pStr);
            editor.commit();
            return bmp;
    	}
    	
        @Override
        public void run() {
            super.run();
            synchronized (this) {
                // Explicitly deallocate Mats
                if (mRgba != null) mRgba.release();
                if (mGray != null) mGray.release();
                if (mIntermediateMat != null) mIntermediateMat.release();
                mRgba = null;
                mGray = null;
                mIntermediateMat = null;
            }
        }
    }    
    
}