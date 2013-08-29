package www.snx.sunnyhttp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;

public class PhotograpyActivity extends Activity {
	public static final String PREF_FILE = "sensorFile";
	private static final String TAG = "openCV - 2";
    private MenuItem menuRemoteSurveillance;
    private MenuItem menuExit;
    private Sample1View         mView;

    private BaseLoaderCallback  mOpenCVCallBack = new BaseLoaderCallback(this) {
    	@Override
    	public void onManagerConnected(int status) {
    		switch (status) {
				case LoaderCallbackInterface.SUCCESS:
				{
					Log.i(TAG, "OpenCV loaded successfully");
					// Create and set View
					mView = new Sample1View(mAppContext);
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
    
    public PhotograpyActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
	protected void onPause() {
        Log.i(TAG, "onPause");
		super.onPause();
		if (null != mView)
			mView.releaseCamera();
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
        }
        else if (item == menuExit)
            finish();
        
        return true;
    }
    
    
    class Sample1View extends SampleViewBase {
    	
        private Mat mYuv;
        private Mat mRgba;
    	private Bitmap mBitmap;

        public Sample1View(Context context) {
            super(context);
        }

    	@Override
    	protected void onPreviewStarted(int previewWidth, int previewHeight) {
    	    synchronized (this) {
            	// initialize Mats before usage
            	mYuv = new Mat(getFrameHeight() + getFrameHeight() / 2, getFrameWidth(), CvType.CV_8UC1);
            
            	mRgba = new Mat();
            
            	mBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888); 
        	    }
    	}

    	@Override
    	protected void onPreviewStopped() {
    		if(mBitmap != null) {
    			mBitmap.recycle();
    		}

    		synchronized (this) {
                // Explicitly deallocate Mats
                if (mYuv != null)
                    mYuv.release();
                if (mRgba != null)
                    mRgba.release();
             
                mYuv = null;
                mRgba = null;
             
            }
        }

        @Override
        protected Bitmap processFrame(byte[] data) {
            mYuv.put(0, 0, data);
            Imgproc.cvtColor(mYuv, mRgba, Imgproc.COLOR_YUV420sp2RGB, 4);
            Bitmap bmp = mBitmap;

            try {
                Utils.matToBitmap(mRgba, bmp);
            } catch(Exception e) {
                Log.e("org.opencv.samples.tutorial1", "Utils.matToBitmap() throws an exception: " + e.getMessage());
                bmp.recycle();
                bmp = null;
            }

            if(MainActivity.stream_perm) {
            	ByteArrayOutputStream baos = new ByteArrayOutputStream();
            	bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);   
            	byte[] b = baos.toByteArray();
            	String pStr = Base64.encodeToString(b, Base64.DEFAULT);
            
            	SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
            	SharedPreferences.Editor editor = settings.edit();
            	editor.putString("image", pStr);
            	editor.commit();
            }
            return bmp;
        }
    }

    
    public abstract class SampleViewBase extends SurfaceView implements SurfaceHolder.Callback, Runnable {
        private static final String TAG = "Sample::SurfaceView";

        private Camera              mCamera;
        private SurfaceHolder       mHolder;
        private int                 mFrameWidth;
        private int                 mFrameHeight;
        private byte[]              mFrame;
        private boolean             mThreadRun;
        private byte[]              mBuffer;


        public SampleViewBase(Context context) {
            super(context);
            mHolder = getHolder();
            mHolder.addCallback(this);
            Log.i(TAG, "Instantiated new " + this.getClass());
        }

        public int getFrameWidth() {
            return mFrameWidth;
        }

        public int getFrameHeight() {
            return mFrameHeight;
        }

        public void setPreview() throws IOException {
            	mCamera.setPreviewDisplay(null);
    	}
        
        public boolean openCamera() {
            Log.i(TAG, "openCamera");
            releaseCamera();
            mCamera = Camera.open();
            if(mCamera == null) {
            	Log.e(TAG, "Can't open camera!");
            	return false;
            }

            mCamera.setPreviewCallbackWithBuffer(new PreviewCallback() {
                public void onPreviewFrame(byte[] data, Camera camera) {
                    synchronized (SampleViewBase.this) {
                        System.arraycopy(data, 0, mFrame, 0, data.length);
                        SampleViewBase.this.notify(); 
                    }
                    camera.addCallbackBuffer(mBuffer);
                }
            });
            return true;
        }
        
        public void releaseCamera() {
            Log.i(TAG, "releaseCamera");
            mThreadRun = false;
            synchronized (this) {
    	        if (mCamera != null) {
                    mCamera.stopPreview();
                    mCamera.setPreviewCallback(null);
                    mCamera.release();
                    mCamera = null;
                }
            }
            onPreviewStopped();
        }
        
        public void setupCamera(int width, int height) {
            Log.i(TAG, "setupCamera");
            synchronized (this) {
                if (mCamera != null) {
                    Camera.Parameters params = mCamera.getParameters();
                    List<Camera.Size> sizes = params.getSupportedPreviewSizes();
                    mFrameWidth = width;
                    mFrameHeight = height;

                    // selecting optimal camera preview size
                    {
                        int  minDiff = Integer.MAX_VALUE;
                        for (Camera.Size size : sizes) {
                            if (Math.abs(size.height - height) < minDiff) {
                                mFrameWidth = size.width;
                                mFrameHeight = size.height;
                                minDiff = Math.abs(size.height - height);
                            }
                        }
                    }

                    params.setPreviewSize(getFrameWidth(), getFrameHeight());
                    
                    List<String> FocusModes = params.getSupportedFocusModes();
                    if (FocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
                    {
                    	params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    }            
                    
                    mCamera.setParameters(params);
                    
                    /* Now allocate the buffer */
                    params = mCamera.getParameters();
                    int size = params.getPreviewSize().width * params.getPreviewSize().height;
                    size  = size * ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
                    mBuffer = new byte[size];
                    /* The buffer where the current frame will be copied */
                    mFrame = new byte [size];
                    mCamera.addCallbackBuffer(mBuffer);

        			try {
        				setPreview();
        			} catch (IOException e) {
        				Log.e(TAG, "mCamera.setPreviewDisplay/setPreviewTexture fails: " + e);
        			}

                    /* Notify that the preview is about to be started and deliver preview size */
                    onPreviewStarted(params.getPreviewSize().width, params.getPreviewSize().height);

                    /* Now we can start a preview */
                    mCamera.startPreview();
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

        /* The bitmap returned by this method shall be owned by the child and released in onPreviewStopped() */
        protected abstract Bitmap processFrame(byte[] data);

        /**
         * This method is called when the preview process is being started. It is called before the first frame delivered and processFrame is called
         * It is called with the width and height parameters of the preview process. It can be used to prepare the data needed during the frame processing.
         * @param previewWidth - the width of the preview frames that will be delivered via processFrame
         * @param previewHeight - the height of the preview frames that will be delivered via processFrame
         */
        protected abstract void onPreviewStarted(int previewWidtd, int previewHeight);

        /**
         * This method is called when preview is stopped. When this method is called the preview stopped and all the processing of frames already completed.
         * If the Bitmap object returned via processFrame is cached - it is a good time to recycle it.
         * Any other resources used during the preview can be released.
         */
        protected abstract void onPreviewStopped();

        public void run() {
            mThreadRun = true;
            Log.i(TAG, "Starting processing thread");
            while (mThreadRun) {
                Bitmap bmp = null;

                synchronized (this) {
                    try {
                        this.wait();
                        bmp = processFrame(mFrame);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (bmp != null) {
                    Canvas canvas = mHolder.lockCanvas();
                    if (canvas != null) {
                        canvas.drawBitmap(bmp, (canvas.getWidth() - getFrameWidth()) / 2, (canvas.getHeight() - getFrameHeight()) / 2, null);
                        mHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
}
