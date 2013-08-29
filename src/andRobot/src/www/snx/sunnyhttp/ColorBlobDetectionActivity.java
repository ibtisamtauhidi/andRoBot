package www.snx.sunnyhttp;

import java.io.ByteArrayOutputStream;
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
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;

public class ColorBlobDetectionActivity extends Activity {

	public	static	final String PREF_FILE = "sensorFile";
	private	static	final String TAG = "Scorpius by sNY";
	private ColorBlobDetectionView mView;
	private static double mMinContourArea = 0.1;
   	private static Size SPECTRUM_SIZE = new Size(200, 32);
	private static final Scalar CONTOUR_COLOR = new Scalar(255,0,0,255);

	   private BaseLoaderCallback  mOpenCVCallBack = new BaseLoaderCallback(this) {
	    	@Override
	    	public void onManagerConnected(int status) {
	    		switch (status) {
					case LoaderCallbackInterface.SUCCESS:
					{
						mView = new ColorBlobDetectionView(mAppContext);
						setContentView(mView);
						if( !mView.openCamera() ) {
							AlertDialog ad = new AlertDialog.Builder(mAppContext).create();
							ad.setCancelable(false);
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
	
	public ColorBlobDetectionActivity()
	{
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

    @Override
	protected void onPause() {
		super.onPause();
		if (null != mView)
			mView.releaseCamera();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if( (null != mView) && !mView.openCamera() ) {
			AlertDialog ad = new AlertDialog.Builder(this).create();
			ad.setCancelable(false);
			ad.setMessage("Fatal error: can't open camera!");
			ad.show();
		}
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mOpenCVCallBack))
        {
        	Log.e(TAG, "Cannot connect to OpenCV Manager");
        }
    }
    
    public class ColorBlobDetectionView extends SampleCvViewBase implements OnTouchListener {

    	private Mat mRgba;

    	private boolean mIsColorSelected = false;
    	private Scalar mBlobColorRgba = new Scalar(255);
    	private Scalar mBlobColorHsv = new Scalar(255);
    	private ColorBlobDetector mDetector = new ColorBlobDetector();
    	private Mat mSpectrum = new Mat();
    	
    	public ColorBlobDetectionView(Context context) {
            super(context);
            setOnTouchListener(this);
    	}
    	
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            synchronized (this) {
                mRgba = new Mat();
            }            
            super.surfaceCreated(holder);
        }
    	
    	public boolean onTouch(View v, MotionEvent event) {
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
            
            mBlobColorHsv = Core.sumElems(touchedRegionHsv);
            int pointCount = touchedRect.width*touchedRect.height;
            for (int i = 0; i < mBlobColorHsv.val.length; i++) {
            	mBlobColorHsv.val[i] /= pointCount;
            }
            
            mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
            
       		mDetector.setHsvColor(mBlobColorHsv);
       		
       		Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);
       		
            mIsColorSelected = true;
            
            return false;
    	}

    	@Override
    	protected Bitmap processFrame(VideoCapture capture) {
			SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
	        SharedPreferences.Editor editor = settings.edit();

    		
    		capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
    		
            Bitmap bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
            
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
                while (each.hasNext()) {
                	MatOfPoint wrapper = each.next();
                	Rect m = Imgproc.boundingRect(wrapper);
                	x_total+=(((m.width/2)+m.x)*2)-1;
                	y_total+=(((m.height/2)+m.y)*2)-1;
                	total+=1;
                }                    
                if(total>0) {
                	int x = x_total/total;
                	int y = y_total/total;
                	float x_fuzzy = ((float)x/(float)mRgba.width()-1);
                	float y_fuzzy = ((float)y/(float)mRgba.height()-1);
                	
					String horizontal = x_fuzzy+"";
					String vertical = y_fuzzy+"";

                	String msg1 = "X : "+x_fuzzy+" Y : "+y_fuzzy;
                	Core.putText(mRgba, msg1, new Point(10, 100), 3, 0.5, new Scalar(255, 0, 0, 255), 1);
					
					Date  date = new Date();
					long dtime= date.getTime();
					
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

            try {
            	Utils.matToBitmap(mRgba, bmp);
            } catch(Exception e) {
                bmp.recycle();
                bmp = null;
            }
            if(MainActivity.stream_perm) {
	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);   
	            byte[] b = baos.toByteArray();
	            String pStr = Base64.encodeToString(b, Base64.DEFAULT);
	            editor.putString("image", pStr);
            }
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
                if (mRgba != null) mRgba.release();
                mRgba = null;
            }
        }
    }

    public abstract class SampleCvViewBase extends SurfaceView implements SurfaceHolder.Callback, Runnable {
        private SurfaceHolder       mHolder;
        private VideoCapture        mCamera;

        public SampleCvViewBase(Context context) {
            super(context);
            mHolder = getHolder();
            mHolder.addCallback(this);
        }

        public boolean openCamera() {
            synchronized (this) {
    	        releaseCamera();
    	        mCamera = new VideoCapture(Highgui.CV_CAP_ANDROID);
    	        if (!mCamera.isOpened()) {
    	            mCamera.release();
    	            mCamera = null;
    	            return false;
    	        }
    	    }
            return true;
        }
        
        public void releaseCamera() {
            synchronized (this) {
    	        if (mCamera != null) {
    	                mCamera.release();
    	                mCamera = null;
                }
            }
        }
        
        public void setupCamera(int width, int height) {
            synchronized (this) {
                if (mCamera != null && mCamera.isOpened()) {
                    List<Size> sizes = mCamera.getSupportedPreviewSizes();
                    int mFrameWidth = width;
                    int mFrameHeight = height;

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
            setupCamera(width, height);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            (new Thread(this)).start();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            releaseCamera();
        }

        protected abstract Bitmap processFrame(VideoCapture capture);

        public void run() {
            while (true) {
                Bitmap bmp = null;

                synchronized (this) {
                    if (mCamera == null)
                        break;

                    if (!mCamera.grab()) {
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
        }
    }
    
    public class ColorBlobDetector
    {
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

            double maxArea = 0;
            Iterator<MatOfPoint> each = contours.iterator();
            while (each.hasNext()) {
            	MatOfPoint wrapper = each.next();
            	double area = Imgproc.contourArea(wrapper);
            	if (area > maxArea)
            		maxArea = area;
            }

            mContours.clear();
            each = contours.iterator();
            while (each.hasNext())
            {
            	MatOfPoint contour = each.next();
            	if (Imgproc.contourArea(contour) > mMinContourArea*maxArea) {
            		Core.multiply(contour, new Scalar(4,4), contour);
            		mContours.add(contour);
            	}
            }
    	}

    	public List<MatOfPoint> getContours()
    	{
    		return mContours;
    	}

    	private Scalar mLowerBound = new Scalar(0);
    	private Scalar mUpperBound = new Scalar(0);
    	private Scalar mColorRadius = new Scalar(25,50,50,0);
    	private Mat mSpectrum = new Mat();
    	private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();
    }
}