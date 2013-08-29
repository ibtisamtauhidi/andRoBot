package www.snx.sunnyhttp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
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
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;

public class OpenCVActivity extends Activity {
	public static final String PREF_FILE = "sensorFile";
	private static final String TAG = "openCV";
	private ColorBlobDetectionView mView;
    public static final int      VIEW_MODE_REMOTE_SURVEILLANCE  	=	0;
    public static final int      VIEW_MODE_FACE_DETECTION 			= 	1;
    public static final int      VIEW_MODE_BLOB  					= 	2;
    public static final int      VIEW_MODE_MOTION_DETECTION  		= 	3;
    public static final int      VIEW_MODE_CALL_FOR_EXIT	  		= 	4;
    public static int            viewMode = 	VIEW_MODE_REMOTE_SURVEILLANCE;
    private MenuItem menuBlob;
    private MenuItem menuRemoteSurveillance;
    private MenuItem menuFaceDetection;
    private MenuItem menuMotion;
    private MenuItem menuExit;
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
	
	public OpenCVActivity()
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu");
        menuRemoteSurveillance = menu.add("Remote Surveillance");
        menuFaceDetection = menu.add("Face Detection");
        menuBlob = menu.add("Blob");
        menuMotion = menu.add("Motion");
        menuExit = menu.add("Exit");
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "Menu Item selected " + item);
        if (item == menuRemoteSurveillance) {
			SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
	        SharedPreferences.Editor editor = settings.edit();
	        editor.putString("mode", "REMOTE");
	        editor.commit();
            viewMode = VIEW_MODE_REMOTE_SURVEILLANCE;
        }
        else if (item == menuFaceDetection) {
        	SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
	        SharedPreferences.Editor editor = settings.edit();
	        editor.putString("mode", "FACE");
	        editor.commit();
	        viewMode =  VIEW_MODE_FACE_DETECTION;
        }
        else if (item == menuBlob) {
        	SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
	        SharedPreferences.Editor editor = settings.edit();
	        editor.putString("mode", "BLOB");
	        editor.commit();
            viewMode = VIEW_MODE_BLOB;
        }
        else if (item == menuMotion) {
        	SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
	        SharedPreferences.Editor editor = settings.edit();
	        editor.putString("mode", "MOTION");
	        editor.commit();
            viewMode =  VIEW_MODE_MOTION_DETECTION;
        }
        else if (item == menuExit)
            finish();
        
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
    
    public class ColorBlobDetectionView extends SampleCvViewBase implements OnTouchListener {

    	private Mat mRgba;
    	private Mat mGray;
    	private Mat mIntermediateMat;
    	private boolean mIsColorSelected = false;
    	private Scalar mBlobColorRgba; // = new Scalar(255);
    	private Scalar mBlobColorHsv; // = new Scalar(255);
    	private ColorBlobDetector mDetector; // = new ColorBlobDetector();
    	private Mat mSpectrum; // = new Mat();
    	private Size SPECTRUM_SIZE;// = new Size(200, 32);    	
    	private Scalar CONTOUR_COLOR; // = new Scalar(255,0,0,255);
    	
    	
    	public ColorBlobDetectionView(Context context)
    	{
            super(context);
            setOnTouchListener(this);
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
    	
    	public boolean onTouch(View v, MotionEvent event)
    	{
            int cols = mRgba.cols();
            int rows = mRgba.rows();
            int xOffset = (getWidth() - cols) / 2;
            int yOffset = (getHeight() - rows) / 2;
            int x = (int)event.getX() - xOffset;
            int y = (int)event.getY() - yOffset;
                        
            if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;
      
            Rect touchedRect = new Rect();
            touchedRect.x = (x>4) ? x-4 : 0;
            touchedRect.y = (y>4) ? y-4 : 0;
            touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
            touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;
            	
            Mat touchedRegionRgba = mRgba.submat(touchedRect);        
            Mat touchedRegionHsv = new Mat();
            
            Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);
            
            // Calculate average color of touched region
            mBlobColorHsv = Core.sumElems(touchedRegionHsv);
            int pointCount = touchedRect.width*touchedRect.height;
            for (int i = 0; i < mBlobColorHsv.val.length; i++) mBlobColorHsv.val[i] /= pointCount;
            
            mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
       		mDetector.setHsvColor(mBlobColorHsv);       		
       		Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);
            mIsColorSelected = true;
            
            return false; // don't need subsequent touch events
    	}

    	@Override
    	protected Bitmap processFrame(VideoCapture capture) {
    		String PREF_FILE = "sensorFile";
    		capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
			Bitmap bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);    		
    		switch (OpenCVActivity.viewMode) {
    		
	        	case OpenCVActivity.VIEW_MODE_REMOTE_SURVEILLANCE:
	        		capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
	        		break;
	        		
	        	case OpenCVActivity.VIEW_MODE_FACE_DETECTION:
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
	        		break;
	        		
	    		case OpenCVActivity.VIEW_MODE_BLOB:
	                if (mIsColorSelected)
	                {            
	                	mDetector.process(mRgba);
	                	List<MatOfPoint> contours = mDetector.getContours();
	                    Log.e(TAG, "Contours count: " + contours.size());
	                	Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);
	                    Iterator<MatOfPoint> each = contours.iterator();
	                    int x_total = 0;
	                    int y_total = 0;
	                    int total = 0;
	                    while (each.hasNext())
	                    {
	                    	MatOfPoint wrapper = each.next();
	                    	Rect m = Imgproc.boundingRect(wrapper);
	                    	x_total+=(((m.width/2)+m.x)*2)-1;
	                    	y_total+=(((m.height/2)+m.y)*2)-1;
	                    	total+=1;
	                    }                    
	                    if(total>0) {
	                    	int x = x_total/total;
	                    	int y = y_total/total;
	                    	float x_fuzzy = (float)x/(float)mRgba.width();
	                    	float y_fuzzy = (float)y/(float)mRgba.height();
	                    	String msg1 = "X : "+x_fuzzy+" Y : "+y_fuzzy;
	                    	Core.putText(mRgba, msg1, new Point(10, 100), 3, 0.5, new Scalar(255, 0, 0, 255), 1);
	    					String horizontal = x_fuzzy+"";
	    					String vertical = y_fuzzy+"";
	    					Date  date = new Date();
	    					long dtime= date.getTime();
	    					SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
	    			        SharedPreferences.Editor editor = settings.edit();
	    			        editor.putString("vertical", vertical);
	    			        editor.putString("horizontal", horizontal);
	    			        editor.putLong("date", dtime);
	    			        editor.commit();
	                    }
	                    Mat colorLabel = mRgba.submat(2, 34, 2, 34);
	                    colorLabel.setTo(mBlobColorRgba);
	                    
	                    Mat spectrumLabel = mRgba.submat(2, 2 + mSpectrum.rows(), 38, 38 + mSpectrum.cols());
	                    mSpectrum.copyTo(spectrumLabel);
	                }
	                break;
	                
	    		case OpenCVActivity.VIEW_MODE_CALL_FOR_EXIT:
	    			finishActivity();
	    			break;
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
    	
    	private Scalar converScalarHsv2Rgba(Scalar hsvColor)
    	{	
            Mat pointMatRgba = new Mat();
            Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
            Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);
            return new Scalar(pointMatRgba.get(0, 0));
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
    
    public class ColorBlobDetector {
    	// Lower and Upper bounds for range checking in HSV color space
    	private Scalar mLowerBound = new Scalar(0);
    	private Scalar mUpperBound = new Scalar(0);
    	// Minimum contour area in percent for contours filtering
    	private double mMinContourArea = 0.1;
    	// Color radius for range checking in HSV color space
    	private Scalar mColorRadius = new Scalar(25,50,50,0);
    	private Mat mSpectrum = new Mat();
    	private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();
    	
    	public void setColorRadius(Scalar radius)
    	{
    		mColorRadius = radius;
    	}
    	
    	public void setHsvColor(Scalar hsvColor)
    	{
    	    double minH = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0]-mColorRadius.val[0] : 0; 
        	double maxH = (hsvColor.val[0]+mColorRadius.val[0] <= 255) ? hsvColor.val[0]+mColorRadius.val[0] : 255;

      		mLowerBound.val[0] = minH;
       		mUpperBound.val[0] = maxH;

      		mLowerBound.val[1] = hsvColor.val[1] - mColorRadius.val[1];
       		mUpperBound.val[1] = hsvColor.val[1] + mColorRadius.val[1];

      		mLowerBound.val[2] = hsvColor.val[2] - mColorRadius.val[2];
       		mUpperBound.val[2] = hsvColor.val[2] + mColorRadius.val[2];

       		mLowerBound.val[3] = 0;
       		mUpperBound.val[3] = 255;

       		Mat spectrumHsv = new Mat(1, (int)(maxH-minH), CvType.CV_8UC3);

     		for (int j = 0; j < maxH-minH; j++)
       		{
       			byte[] tmp = {(byte)(minH+j), (byte)255, (byte)255};
       			spectrumHsv.put(0, j, tmp);
       		}

       		Imgproc.cvtColor(spectrumHsv, mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);

    	}
    	
    	public Mat getSpectrum()
    	{
    		return mSpectrum;
    	}
    	
    	public void setMinContourArea(double area)
    	{
    		mMinContourArea = area;
    	}
    	
    	public void process(Mat rgbaImage)
    	{
        	Mat pyrDownMat = new Mat();

        	Imgproc.pyrDown(rgbaImage, pyrDownMat);
        	Imgproc.pyrDown(pyrDownMat, pyrDownMat);

          	Mat hsvMat = new Mat();
        	Imgproc.cvtColor(pyrDownMat, hsvMat, Imgproc.COLOR_RGB2HSV_FULL);

        	Mat Mask = new Mat();
        	Core.inRange(hsvMat, mLowerBound, mUpperBound, Mask);
        	Mat dilatedMask = new Mat();
        	Imgproc.dilate(Mask, dilatedMask, new Mat());

            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Mat hierarchy = new Mat();

            Imgproc.findContours(dilatedMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            // Find max contour area
            double maxArea = 0;
            Iterator<MatOfPoint> each = contours.iterator();
            while (each.hasNext())
            {
            	MatOfPoint wrapper = each.next();
            	double area = Imgproc.contourArea(wrapper);
            	if (area > maxArea)
            		maxArea = area;
            }

            // Filter contours by area and resize to fit the original image size
            mContours.clear();
            each = contours.iterator();
            while (each.hasNext())
            {
            	MatOfPoint contour = each.next();
            	if (Imgproc.contourArea(contour) > mMinContourArea*maxArea)
            	{
            		Core.multiply(contour, new Scalar(4,4), contour);
            		mContours.add(contour);
            	}
            }
    	}

    	public List<MatOfPoint> getContours()
    	{
    		return mContours;
    	}
    }
}