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

/**
 * This class represents the ObjectDetector
 * @author freinsberg
 *
 */
public class ObjectDetector{
	
	final public static int NO_CAR = 0x00;
	final public static int RIGHT_CAR = 0x01;
	final public static int LEFT_CAR = 0x10;
	final public static int BOTH_CAR = 0x11;
	private static Mat mInputFrame;	
//	private Mat mStaticImage = null;
	private Mat mRgba = null;
	private Mat mGray = null;

	private Mat mEdges = null; 
	private Mat mHoughLines = null;
	private int mHLthreshold = 30;
    private int mHLminLineSize = 50;
    private int mHLlineGap = 20;    

    private Point mSeparatorPoint;
    private Point mLeftPoint;
    private Point mRightPoint;
    private boolean mFoundSeparatorLine;
    private boolean mFoundLeftLine;
    private boolean mFoundRightLine;
     
    private boolean mColorsOnLeftLane;
    private boolean mColorsOnRightLane;
	private int mLowerThreshold;
	private int mUpperThreshold;


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
	private static ObjectDetector mObjectDetector = new ObjectDetector();
	
	/**
	 * Constructor: This private Constructor makes this Class a Singleton- Class.
	 */
	private ObjectDetector(){}
	
	/**
	 * Constructor: This private Constructor return an Instance of an ObjectDetector for a given Mat. This Mat is rotated 90° clock-wise by doing a transpose followed by a horizontal flip. This is done because the given Mat comes in landscape format and the Mat to work with should be the same format as it is seen on the camera frame.
	 * @param inputFrame The input frame, delivered by the camera.
	 * @return An Instance of an ObjectDetector.
	 */
	public static ObjectDetector getInstance(Mat inputFrame){		
		
		mInputFrame = inputFrame;	
		mInputFrame = inputFrame.t();		
		Core.flip(mInputFrame, mInputFrame, 1);	
		
		return mObjectDetector;
	}
	
	/**
	 * This Function is used to get an Instance of the ObjectDetector. So the Activities have the ability to use Methods of this class without creating an Object from it.
	 * @return An Instance of an ObjectDetector.
	 */
	public static ObjectDetector getInstance() {	
		return mObjectDetector;
	}	
	
	/**
	 * This Function is used to check, if there are all needed Lines found in the Frame.
	 * @return TRUE if Left-, Right- and a SeparatorLine is true, FALSE if at least one of the three is false.
	 */
	public boolean getFoundLines(){
		return mFoundSeparatorLine && mFoundLeftLine && mFoundRightLine;
	}
	
	/**
	 * This Function is used to get the center x-axis values of the 2 lanes. It creates an Integer Array for the 2 Integer values. First Position(0) takes the x-axis value for the center between the Left- and SeparatorLine. Second Position(1) takes the x-axis value for the center between Separator- and RightLine.	
	 * @return The Integer Array with the x-axis center values for both lanes. (0: left-center | 1: right-center).
	 */
	public int[] getCenterOfLanes(){
		int[] arr = new int[2];
		arr[0] = (int) (((mSeparatorPoint.x - mLeftPoint.x) / 2) + mLeftPoint.x);
		arr[1] = (int) (((mRightPoint.x -mSeparatorPoint.x) /2)+mSeparatorPoint.x);
		
		return arr;
	}
	
	/**
	 * This Function draws 2 rectangles onto a Bitmap. A Mat gets the size of the input Frame. The position for the rectangles are determined by calling getCenterofLanes(). After setting an offset for them and drawing with color 'white' the Mat is converted to a Bitmap of the same size.
	 * @return The Bitmap with drawed rectangles centered in between every lane. NULL if Function matToBitmap fails.
	 */
	public Bitmap draw_car_recognizer(){
		try{
		mCarRecognizerOverlay = new Mat(new Size(mInputFrame.cols(), mInputFrame.rows()), mInputFrame.type(), new Scalar(0, 0, 0, 0));
		Core.rectangle(mCarRecognizerOverlay, new Point(getCenterOfLanes()[0] - 15, (mInputFrame.rows()/2) - 15), new Point(getCenterOfLanes()[0] + 15, (mInputFrame.rows()/2) + 15), new Scalar(255,255,255,255));
		Core.rectangle(mCarRecognizerOverlay, new Point(getCenterOfLanes()[1] - 15, (mInputFrame.rows()/2) - 15), new Point(getCenterOfLanes()[1] + 15, (mInputFrame.rows()/2) + 15), new Scalar(255,255,255,255));
		mCarRecognizer = Bitmap.createBitmap(mCarRecognizerOverlay.cols(), mCarRecognizerOverlay.rows(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(mCarRecognizerOverlay, mCarRecognizer);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}		
		return mCarRecognizer;
	}
	
	/**
	 * This Function is used to get 2 rectangles with position informations how they would be positioned on the Frame. 
	 * @return The Rectangle Array with rectangles and there position information. (0: left-rectangle|1: right-rectangle)
	 */
	public Rect[] getLanesToScanColor(){
		Rect[] arr = new Rect[2];
		arr[0] = new Rect(new Point(getCenterOfLanes()[0] - 15, (mInputFrame.rows()/2) - 15), new Point(getCenterOfLanes()[0] + 15, (mInputFrame.rows()/2) + 15));
		arr[1] = new Rect(new Point(getCenterOfLanes()[1] - 15, (mInputFrame.rows()/2) - 15), new Point(getCenterOfLanes()[1] + 15, (mInputFrame.rows()/2) + 15));
		
		return arr;
	}
	
	/**
	 * This Function gets a Scalar with color information according to a given lane.  
	 * @param lane The lane for which the color is needed.
	 * @return The Scalar for the given lane.
	 */
	public Scalar getCarColor(int lane) {
		if (lane == Race.LEFT_LANE)
			return mLeftCarColorScalar;
		else 
			return mRightCarColorScalar;
	}
	
	/**
	 * This Function finds a color in an area of the input Frame. 
	 * The Area is determined by the x- and y-offset which represents the position where a certain color should appear.
	 * The used Method isDifferent is used to check, if the calculated average color differs from the calculated average color of the empty track.
	 * If a color difference is found, this function creates a Bitmap with the size of the rectangle given by getLanesToScanColor(), filled with the color.
	 * @param x_offset The x_offset to get the x-axis position to start color detection.
	 * @param y_offset The y_offset to get the y-axis position to start color detection.
	 * @param lane The lane to scan.
	 * @return The Bitmap with the found average color, Null if no color difference has been found.
	 */
	public Bitmap getColorInOffset(int x_offset, int y_offset, int lane){
		mLeftCarColorScalar = null;
		mRightCarColorScalar = null;
		Log.i("debug", "Into getColorInOffset for lane :"+lane);	
		if(mInputFrame.empty())
			Log.i("debug","mInputFramePortrait is empty!");
		mRgba = mInputFrame.clone();
		
		double[] oldColors = new double[4];
		double[] newColors = new double[4];
		double[] avg_oldColors = new double[4];
		double[] avg_newColors = new double[4];		
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

				
				mScannedTrackPixelColor = mRgba.get(y, x);
				
				mEmptyTrackPixelColor = mEmptyTrack.get(y, x);
				
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
		mRgba.release();
		Log.i("debug", "Between FOR getColorInOffset for lane :"+lane);	
		for(int i = 0; i <= 3; i++) {
			avg_oldColors[i] = oldColors[i] / scan_counter;
			avg_newColors[i] = newColors[i] / scan_counter;

		}
		Log.i("debug", "After FOR getColorInOffset for lane :"+lane);	
		Log.i("debug", "avg_Old: r"+avg_oldColors[0]+ ", g"+avg_oldColors[1]+ ", b"+avg_oldColors[2]);
		Log.i("debug", "avg_New: r"+avg_newColors[0]+ ", g"+avg_newColors[1]+ ", b"+avg_newColors[2]);
		boolean colorDetected = isDifferent(avg_oldColors, avg_newColors);	
		Log.i("farbeee", "color on left lane AFTER for loop and BEFORE ifelses: "+ mLeftCarColorScalar);
		Log.i("farbeee", "color on right lane AFTER for loop and BEFORE ifelses: "+ mRightCarColorScalar);
		if(lane == Race.LEFT_LANE)
		{
			if(!colorDetected)
				mColorsOnLeftLane = false;
			else{
				mColorsOnLeftLane = true;
				if(mLeftCarColorScalar == null)
					mLeftCarColorScalar = new Scalar(avg_newColors);
				Log.i("farbeee", "color on left lane IN ifelses: "+ mLeftCarColorScalar);
				mLeftCarColor = new Mat(getLanesToScanColor()[0].x,getLanesToScanColor()[0].y, mInputFrame.type(),mLeftCarColorScalar);				
				mLeftCarColorImage = Bitmap.createBitmap(mLeftCarColor.cols(), mLeftCarColor.rows(), Bitmap.Config.ARGB_8888);
				Utils.matToBitmap(mLeftCarColor, mLeftCarColorImage);					
				//mFoundColors.clear();
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
				if(mRightCarColorScalar == null)
					mRightCarColorScalar = new Scalar(avg_newColors);
				Log.i("farbeee", "color on right lane IN ifelses: "+ mRightCarColorScalar);
				mRightCarColor = new Mat(getLanesToScanColor()[0].x,getLanesToScanColor()[0].y, mInputFrame.type(),mRightCarColorScalar);
				mRightCarColorImage = Bitmap.createBitmap(mRightCarColor.cols(), mRightCarColor.rows(), Bitmap.Config.ARGB_8888);
				Utils.matToBitmap(mRightCarColor, mRightCarColorImage);				
				//mFoundColors.clear();
				if(mColorsOnRightLane)
					Log.i("debug", "color on rightlane");
				return mRightCarColorImage;
			}
		}
		return null;		
	}
	
	/**
	 * This Function generates an Overlay for the Track.
	 * The input Frame is scanned with the Canny- Algorithm including automatic thresholding.
	 * Then this cannied Frame is processed within an HoughLines- Algorithm.
	 * Using the probabilistic version of the HoughLines- Algorithm makes calculation a bit less accurate but certainly faster, by choosing randomized pixels at the edges.
	 * After that the returned (1 dimensional) Mat is checked for lines whose start and end point is within wether at the left side, centered or at the right side of the Frame.
	 * Only these lines are drawed onto the track overlay. 
	 * @return The generated track overlay with blue colored lines. If no Lines are found, the overlay is just transparent. 
	 */
	public Bitmap generate_track_overlay() {
		// Load an Image to try operations on local stored files
		mFoundSeparatorLine = false;
		mFoundLeftLine = false;
		mFoundRightLine = false;
		mGray = new Mat();
		mEdges = new Mat();
		mHoughLines = new Mat();
		mEmptyTrack = new Mat();
		//mStaticImage = Highgui.imread(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+ "track.jpg");

		track_overlay = new Mat(new Size(mInputFrame.cols(), mInputFrame.rows()), mInputFrame.type(), new Scalar(0, 0, 0,0));
		Log.i("debug", "Channels: " + track_overlay.channels());

		Imgproc.cvtColor(mInputFrame, mGray, Imgproc.COLOR_RGBA2GRAY);

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
		//Log.i("debug","Status Sd-Karte: "+ Environment.getExternalStorageState());
		//mStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

//		mCannyEdgeImage = new File(mStorageDir, "canny.bmp");
//		mPath = mCannyEdgeImage.toString();
//		Boolean bool = Highgui.imwrite(mPath, mEdges);
//		if (bool)
//			Log.i("debug", "SUCCESS writing canny.bmp to external storage");
//		else
//			Log.i("debug", "Fail writing canny.bmp to external storage");
		
		if((!mEdges.empty() && !mInputFrame.empty()))
		{	
			Imgproc.HoughLinesP(mEdges, mHoughLines, 1, Math.PI / 180, mHLthreshold, mHLminLineSize, mHLlineGap);
			
			for (int x = 0; x < mHoughLines.cols(); x++) {
				double[] vec = mHoughLines.get(0, x);
				double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
				//check for usable Lines at the center of the frame
				if (x1 > mInputFrame.cols() / 2 - 30
						&& x1 < mInputFrame.cols() / 2 + 30
						&& x2 > mInputFrame.cols() / 2 - 30
						&& x2 < mInputFrame.cols() / 2 + 30) {					
					Point start = new Point(x1, y1);
					Point end = new Point(x2, y2);				
					Core.line(track_overlay, start, end,new Scalar(0, 0, 255, 255), 3);
					mSeparatorPoint = new Point(x1,y1);
					
					mFoundSeparatorLine = true;						
				}
				
				//check for usable Lines at the left side of the frame
				else if(x1 < 50 && x2 < 50)
				{				
					Point start = new Point(x1, y1);
					Point end = new Point(x2, y2);				
					Core.line(track_overlay, start, end,new Scalar(0, 0, 255, 255), 3);	
					mLeftPoint = new Point(x1,y1);
					
					mFoundLeftLine = true;
				}
				
				//check for usable Lines at the right side of the frame
				else if(x1 > mInputFrame.cols() - 50 && x2 > mInputFrame.cols() - 50)
				{				
					Point start = new Point(x1, y1);
					Point end = new Point(x2, y2);				
					Core.line(track_overlay, start, end,new Scalar(0, 0, 255, 255), 3);			
					mRightPoint = new Point(x1,y1);
					
					mFoundRightLine = true;
				}
			}
			
			mTrackOverlay = Bitmap.createBitmap(track_overlay.cols(),
					track_overlay.rows(), Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(track_overlay, mTrackOverlay);
			
			mInputFrame.copyTo(mEmptyTrack); 	
		}

		return mTrackOverlay;
	}
		
	/**
	 * This Function checks two given double[]'s for a value difference of certain amount in at least one value.
	 * @param avg_oldColors The double[] with color values from the track lane.
	 * @param avg_newColors The double[] with color values from a possible car.
	 * @return TRUE if colors are different, FALSE if colors are the same.
	 */
	public boolean isDifferent(double[] avg_oldColors, double[] avg_newColors){
		
		if((avg_newColors[0] == 0.0) && (avg_newColors[1] == 0.0) && (avg_newColors[2] == 0.0) && (avg_newColors[3] == 0.0))
			return false;
		
		for(int i = 0; i < 4; i++)
		{
			if(avg_newColors[i] > avg_oldColors[i] + 50 ||avg_newColors[i] < avg_oldColors[i] - 50)			
				return true;			
		}
		return false;
	}	
	
	/**
	 * This Function sets the car status of the raceable track. 
	 * If there are colors found on both lanes, the status is set to 0x11
	 * If there is color found on the left lane, the status is set so 0x10.
	 * If there is color found on the right lane, the status is set to 0x01.
	 * When there is no color found, the car status is set to 0x00.
	 * @return The hexadecimal value for the available car status.
	 */
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
	
	/**
	 * This function gets the number of cars for the race determined by found colors on the lanes.
	 * If there are colors found on both lanes, the value is set to 2.
	 * If there is color found on the left lane, the value is set to 1.
	 * If there is color found on the right lane, the value is set to 1.
	 * Elsewhere the value is set to 0.
	 * @return
	 */
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
	
	/**
	 * This Function gets the x-axis value of the rightLine.
	 * @return X-axis value for the rightLine.
	 */
	public int getRightSeparator(){
		return (int) mRightPoint.x;
	}
	
	/**
	 * This Function gets the x-axis value of the leftLine.
	 * @return X-axis value for the leftLine.
	 */
	public int getLeftSeparator(){
		return (int) mLeftPoint.x;
	}

	/**
	 * This Function creates a Bitmap Array with the found color determined by getColorInOffset().
	 * @return The Bitmap[] with 2 Bitmaps for the lanes ([0]: left-color|[1]: right-color). Index might be Null if there was no color found.
	 */
	public Bitmap[] get_cars_colors (){
		Log.i("debug", "Into get_car_colors!");		
		int x_offset_left = getCenterOfLanes()[0]-15;
		int y_offset_left = (mInputFrame.rows()/2)-15;	
		int x_offset_right = getCenterOfLanes()[1]-15;
		int y_offset_right = (mInputFrame.rows()/2)-15;
		mCarColorImages = new Bitmap[2];				
		Log.i("debug", "In the middle of get_car_colors!");	
		mCarColorImages[0] = getColorInOffset(x_offset_left, y_offset_left, Race.LEFT_LANE);
		mCarColorImages[1] = getColorInOffset(x_offset_right, y_offset_right, Race.RIGHT_LANE);		
		Log.i("debug", "Tschüss get_cars_colors!");	
		return mCarColorImages;	
		
	}
	
	private boolean validInputFrame(Mat inputFrame)
	{
		if(inputFrame.get(1, 1)[3] != 0.0)
			return true;
		else
			return false;
	}

}
