package de.freinsberg.pomecaloco;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;


public class ObjectDetector{
	
	private static final int ORIENTATION_0 = 0;
	private static final int ORIENTATION_90 = 90;
	private static final int ORIENTATION_270 = 270;
	final public static int NO_CAR = 0x00;
	final public static int RIGHT_CAR = 0x01;
	final public static int LEFT_CAR = 0x10;
	final public static int BOTH_CAR = 0x11;
	private static Mat mInputFrame;	
	private static Mat mInputFramePortrait;
	private Mat mStaticImage = null;
	private Mat mRgba = null;
	private Mat mGray = null;
	private Mat mHSV = null; 
	private Mat mEdges = null; 
	private Mat mHoughLines = null;
	private int mHLthreshold = 30;
    private int mHLminLineSize = 200;
    private int mHLlineGap = 20;    
    private Scalar mLeftLaneColor;
    private Scalar mRightLaneColor;
    private Point mSeparatorPoint;
    private Point mLeftPoint;
    private Point mRightPoint;
    private boolean mFoundSeparatorLine;
    private boolean mFoundLeftLine;
    private boolean mFoundRightLine;
    private boolean mDrawedLinesOnFrame;    
    private boolean mColorsOnLeftLane;
    private boolean mColorsOnRightLane;
	private int mLowerThreshold;
	private int mUpperThreshold;
	private Mat mThreshed = null; 
	private Mat mLowerRangeImage = null;
	private Mat mUpperRangeImage = null;
	private Mat mEmptyTrack = null;
	private Mat track_overlay = null;
	private Bitmap mTrackOverlay= null;
	private Mat mCarRecognizerOverlay;
	private Bitmap mCarRecognizer;
	private Scalar mLeftCarColorScalar;
	private Scalar mRightCarColorScalar;
	private Mat mLeftCarColor;
	private Mat mRightCarColor;
	private Bitmap mLeftCarColorImage;
	private Bitmap mRightCarColorImage;
	private Bitmap[] mCarColorImages;	
	double[] mScannedTrackPixelColor;
	double[] mEmptyTrackPixelColor;
	List <double[]> mFoundColors = new ArrayList<double[]>();	
	private int avg_gray;
	private File mStorageDir;
	private File mHoughLinesImage;
	private File mCannyEdgeImage;
	private File mTrackColor;
	private String mPath;
	private static ObjectDetector mObjectDetector = new ObjectDetector();
	
	private ObjectDetector(){				
		
	}
	
	public static ObjectDetector getInstance(Mat inputFrame){
		

			mInputFrame = inputFrame;	
			mInputFramePortrait = inputFrame.t();		
			Core.flip(mInputFramePortrait, mInputFramePortrait, 1);	

	
		return mObjectDetector;
	}
	
	public static ObjectDetector getInstance() {	
		return mObjectDetector;
	}	

	public boolean getFoundLines(){
		return mFoundSeparatorLine && mFoundLeftLine && mFoundRightLine;
	}
	
	public int[] getCenterOfLanes(){
		int[] arr = new int[2];
		arr[0] = (int) (((mSeparatorPoint.x - mLeftPoint.x) / 2) + mLeftPoint.x);
		arr[1] = (int) (((mRightPoint.x -mSeparatorPoint.x) /2)+mSeparatorPoint.x);
		
		return arr;
	}
	
	public Bitmap draw_car_recognizer(){
		try{
		mCarRecognizerOverlay = new Mat(new Size(mInputFramePortrait.cols(), mInputFramePortrait.rows()), mInputFramePortrait.type(), new Scalar(0, 0, 0, 0));
		Core.rectangle(mCarRecognizerOverlay, new Point(getCenterOfLanes()[0] - 15, (mInputFramePortrait.rows()/2) - 15), new Point(getCenterOfLanes()[0] + 15, (mInputFramePortrait.rows()/2) + 15), new Scalar(255,255,255,255));
		Core.rectangle(mCarRecognizerOverlay, new Point(getCenterOfLanes()[1] - 15, (mInputFramePortrait.rows()/2) - 15), new Point(getCenterOfLanes()[1] + 15, (mInputFramePortrait.rows()/2) + 15), new Scalar(255,255,255,255));
		mCarRecognizer = Bitmap.createBitmap(mCarRecognizerOverlay.cols(), mCarRecognizerOverlay.rows(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(mCarRecognizerOverlay, mCarRecognizer);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}		
		return mCarRecognizer;
	}
	
	public Rect[] getLanesToScanColor(){
		Rect[] arr = new Rect[2];
		arr[0] = new Rect(new Point(getCenterOfLanes()[0] - 15, (mInputFramePortrait.rows()/2) - 15), new Point(getCenterOfLanes()[0] + 15, (mInputFramePortrait.rows()/2) + 15));
		arr[1] = new Rect(new Point(getCenterOfLanes()[1] - 15, (mInputFramePortrait.rows()/2) - 15), new Point(getCenterOfLanes()[1] + 15, (mInputFramePortrait.rows()/2) + 15));
		
		return arr;
	}
	
	public Scalar getCarColor(int lane) {
		if (lane == Race.LEFT_LANE)
			return mLeftCarColorScalar;
		else 
			return mRightCarColorScalar;
	}
	
	public Bitmap getColorInOffset(int x_offset, int y_offset, int lane){
		Log.i("debug", "Into getColorInOffset for lane :"+lane);	
		if(mInputFramePortrait.empty())
			Log.i("debug","mInputFramePortrait is empty!");
		mRgba = mInputFramePortrait;
		double[] oldColors = new double[4];
		double[] newColors = new double[4];
		double[] avg_oldColors = new double[4];
		double[] avg_newColors = new double[4];
		double[] foundColor = new double[4];
		int scan_counter = 0;		
		if(lane == Race.LEFT_LANE)
			mColorsOnLeftLane = false;
		else if(lane == Race.RIGHT_LANE)
			mColorsOnRightLane = false;			
		Log.i("debug", "Before FOR getColorInOffset for lane :"+lane);			
		for(int x = x_offset;x< x_offset +30;x++){
			for (int y = y_offset;y < y_offset+30;y++){
				if(mEmptyTrack.empty())
					Log.i("debug","mEmptyTrack is empty");
				if(mRgba.empty())
					Log.i("debug","mRgba is empty");
				mEmptyTrackPixelColor = mEmptyTrack.get(y, x);
				mScannedTrackPixelColor = mRgba.get(y, x);
				if(mScannedTrackPixelColor == null)
					Log.i("debug", "mScannedTrackPixelColor == null, at: "+x+", "+y);
				if(mEmptyTrackPixelColor == null)
					Log.i("debug","mEmptyTrackPixelColor == null, at: "+x+", "+y);
				scan_counter++;

				for(int i = 0; i <= 3; i++) {
				newColors[i] += mScannedTrackPixelColor[i];
				}

			
				for(int i = 0; i <= 3; i++) {
					oldColors[i] += mEmptyTrackPixelColor[i];
				}
				
				
		
			}		
		}
		Log.i("debug", "Between FOR getColorInOffset for lane :"+lane);	
		for(int i = 0; i <= 3; i++) {
			avg_oldColors[i] = oldColors[i] / scan_counter;
			avg_newColors[i] = newColors[i] / scan_counter;

		}
		Log.i("debug", "After FOR getColorInOffset for lane :"+lane);	
		Log.i("debug", "avg_Old: r"+avg_oldColors[0]+ ", g"+avg_oldColors[1]+ ", b"+avg_oldColors[2]);
		Log.i("debug", "avg_New: r"+avg_newColors[0]+ ", g"+avg_newColors[1]+ ", b"+avg_newColors[2]);
		boolean colorDetected = isDifferent(avg_newColors, avg_oldColors);	
		Log.i("farbeee", "color on left lane AFTER for loop and BEFORE ifelses: "+ mLeftCarColorScalar);
		Log.i("farbeee", "color on right lane AFTER for loop and BEFORE ifelses: "+ mRightCarColorScalar);
		if(lane == Race.LEFT_LANE)
		{
			if(!colorDetected)
				mColorsOnLeftLane = false;
			else{
				mColorsOnLeftLane = true;
				mLeftCarColorScalar = new Scalar(avg_newColors);
				Log.i("farbeee", "color on left lane IN ifelses: "+ mLeftCarColorScalar);
				mLeftCarColor = new Mat(getLanesToScanColor()[0].x,getLanesToScanColor()[0].y, mInputFramePortrait.type(),mLeftCarColorScalar);				
				mLeftCarColorImage = Bitmap.createBitmap(mLeftCarColor.cols(), mLeftCarColor.rows(), Bitmap.Config.ARGB_8888);
				Utils.matToBitmap(mLeftCarColor, mLeftCarColorImage);					
				mFoundColors.clear();
				if(mColorsOnLeftLane)
					Log.i("debug", "color on leftlane");
				return mLeftCarColorImage;

			}
		}else if(lane == Race.RIGHT_LANE)
		{
			if(!colorDetected)
				mColorsOnRightLane = false;
			else{
				mColorsOnRightLane = true;
				mRightCarColorScalar = new Scalar(avg_newColors);
				Log.i("farbeee", "color on right lane IN ifelses: "+ mRightCarColorScalar);
				mRightCarColor = new Mat(getLanesToScanColor()[0].x,getLanesToScanColor()[0].y, mInputFramePortrait.type(),mRightCarColorScalar);
				mRightCarColorImage = Bitmap.createBitmap(mRightCarColor.cols(), mRightCarColor.rows(), Bitmap.Config.ARGB_8888);
				Utils.matToBitmap(mRightCarColor, mRightCarColorImage);				
				mFoundColors.clear();
				if(mColorsOnRightLane)
					Log.i("debug", "color on rightlane");
				return mRightCarColorImage;
			}
		}
		return null;		
	}
	
	public Bitmap generate_track_overlay() {
		// Load an Image to try operations on local stored files
		mFoundSeparatorLine = false;
		mFoundLeftLine = false;
		mFoundRightLine = false;
		mGray = new Mat();
		mEdges = new Mat();
		mHoughLines = new Mat();
		mEmptyTrack = new Mat();
		mStaticImage = Highgui
				.imread(Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
						+ "track.jpg");

		track_overlay = new Mat(new Size(mInputFramePortrait.cols(), mInputFramePortrait
				.rows()), mInputFramePortrait.type(), new Scalar(0, 0, 0,
				0));
		Log.i("debug", "Channels: " + track_overlay.channels());

		Imgproc.cvtColor(mInputFramePortrait, mGray, Imgproc.COLOR_RGBA2GRAY);

		double[] grays = null;
		double wert;
		double count = 0;
		double divider = 0;
		for (int i = 0; i < mGray.rows(); i++) {
			for (int j = 0; j < mGray.cols(); j++) {
				if (grays != null)
					grays = null;
				grays = mGray.get(i, j);
				wert = grays[0];

				divider++;
				count = count + wert;
			}
		}
		avg_gray = (int) (count / divider);
		Log.i("debug", "avg_gray: " + avg_gray);

		mLowerThreshold = (int) (avg_gray * 0.66);
		mUpperThreshold = (int) (avg_gray * 1.33);

		Imgproc.Canny(mGray, mEdges, mLowerThreshold, mUpperThreshold);
		//mGray.release();
		// Highgui.imwrite("/houghlines.png", mHoughLines);
		Log.i("debug","Status Sd-Karte: "+ Environment.getExternalStorageState());
		mStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

//		mCannyEdgeImage = new File(mStorageDir, "canny.bmp");
//		mPath = mCannyEdgeImage.toString();
//		Boolean bool = Highgui.imwrite(mPath, mEdges);
//		if (bool)
//			Log.i("debug", "SUCCESS writing canny.bmp to external storage");
//		else
//			Log.i("debug", "Fail writing canny.bmp to external storage");
		
		if((!mEdges.empty() && !mInputFramePortrait.empty()))
		{	
			Imgproc.HoughLinesP(mEdges, mHoughLines, 1, Math.PI / 180,
					mHLthreshold, mHLminLineSize, mHLlineGap);
			
			for (int x = 0; x < mHoughLines.cols(); x++) {
				double[] vec = mHoughLines.get(0, x);
				double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
	
				if (x1 > mInputFramePortrait.cols() / 2 - 30
						&& x1 < mInputFramePortrait.cols() / 2 + 30
						&& x2 > mInputFramePortrait.cols() / 2 - 30
						&& x2 < mInputFramePortrait.cols() / 2 + 30) {					
					Point start = new Point(x1, y1);
					Point end = new Point(x2, y2);				
					Core.line(track_overlay, start, end,new Scalar(0, 0, 255, 255), 3);
					mSeparatorPoint = new Point(x1,y1);
					mDrawedLinesOnFrame = true;
					mFoundSeparatorLine = true;						
				}
				else if(x1 < 50 && x2 < 50)
				{				
					Point start = new Point(x1, y1);
					Point end = new Point(x2, y2);				
					Core.line(track_overlay, start, end,new Scalar(0, 0, 255, 255), 3);	
					mLeftPoint = new Point(x1,y1);
					mDrawedLinesOnFrame = true;
					mFoundLeftLine = true;
				}
				else if(x1 > mInputFramePortrait.cols() - 50 && x2 > mInputFramePortrait.cols() - 50)
				{				
					Point start = new Point(x1, y1);
					Point end = new Point(x2, y2);				
					Core.line(track_overlay, start, end,new Scalar(0, 0, 255, 255), 3);			
					mRightPoint = new Point(x1,y1);
					mDrawedLinesOnFrame = true;
					mFoundRightLine = true;
				}
			}
			
			mTrackOverlay = Bitmap.createBitmap(track_overlay.cols(),
					track_overlay.rows(), Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(track_overlay, mTrackOverlay);
			
			mInputFramePortrait.copyTo(mEmptyTrack); 	
		}

		return mTrackOverlay;
	}
		
	public boolean isDifferent(double[] avg_oldColors, double[] avg_newColors){
		
		for(int i = 0; i < 4; i++)
		{
			if(avg_newColors[i] > avg_oldColors[i] + 50 ||avg_newColors[i] < avg_oldColors[i] - 50)			
				return true;			
		}
		return false;
	}
	
	public int car_status(){
		int carStatus = 0;
		if(mColorsOnLeftLane && mColorsOnRightLane)
			carStatus = BOTH_CAR;
		else if(mColorsOnLeftLane)
			carStatus = LEFT_CAR;
		else if(mColorsOnRightLane)
			carStatus = RIGHT_CAR;
		else  
			carStatus = NO_CAR;
		Log.i("debug", "carStatus: "+carStatus);
		return carStatus;
		}
	
	public int getNumberOfCars() {
		if(mColorsOnLeftLane && mColorsOnRightLane)
			return 2;
		else if(mColorsOnLeftLane)
			return 1;
		else if(mColorsOnRightLane)
			return 1;
		else
			return 0;		
	}
	
	public int getRightSeparator(){
		return (int) mRightPoint.x;
	}
	
	public int getLeftSeparator(){
		return (int) mLeftPoint.x;
	}

	public Bitmap[] get_cars_colors (){
		Log.i("debug", "Into get_car_colors!");		
		int x_offset_left = getCenterOfLanes()[0]-15;
		int y_offset_left = (mInputFramePortrait.rows()/2)-15;	
		int x_offset_right = getCenterOfLanes()[1]-15;
		int y_offset_right = (mInputFramePortrait.rows()/2)-15;
		mCarColorImages = new Bitmap[2];				
		Log.i("debug", "In the middle of get_car_colors!");	
		mCarColorImages[0] = getColorInOffset(x_offset_left, y_offset_left, Race.LEFT_LANE);
		mCarColorImages[1] = getColorInOffset(x_offset_right, y_offset_right, Race.RIGHT_LANE);		
		Log.i("debug", "Tschüss get_cars_colors!");	
		return mCarColorImages;	
		
	}

}
