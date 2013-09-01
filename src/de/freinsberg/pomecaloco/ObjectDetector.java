package de.freinsberg.pomecaloco;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

/**
 * This class represents the ObjectDetector
 * @author freinsberg
 *
 */
public class ObjectDetector{
	final private static float CANNY_THRESHOLD = (float) 0.1;
	final private static int SEPARATORLINE_TOLERATION = 30;
	final private static int HALF_SQUARE_SIDE_LENGTH = 15;
	final private static int SQUARE_SIDE_LENGTH = 2*HALF_SQUARE_SIDE_LENGTH;
	final private static int SQUARE_AREA = SQUARE_SIDE_LENGTH * SQUARE_SIDE_LENGTH;
	final private static int COLOR_DIFFENRENCE = 50;	
	final public static int NO_CAR = 0x00;
	final public static int RIGHT_CAR = 0x01;
	final public static int LEFT_CAR = 0x10;
	final public static int BOTH_CAR = 0x11;
	final private static int LEFT_LANE_THRESHOLD= 25;
	final private static int RIGHT_LANE_THRESHOLD= -25;
	
	private int mHLthreshold = 5;
    private int mHLminLineSize = 100;
    private int mHLlineGap = 30; 	
	
	private static Mat mInputFrame;	
	private Mat mRgba;
	private Mat mGray;
	private Mat mEdges; 
	private Mat mHoughLines;
	private Mat mEmptyTrack;
	private Mat mMatTrackOverlay;
	private Mat mCarRecognizerOverlay;
	private Mat mLeftCarColor;
	private Mat mRightCarColor;
	
    private int mSeparatorX;
    private int mLeftX;
    private int mRightX;
	private int mLowerThreshold;
	private int mUpperThreshold;
	
    private boolean mFoundSeparatorLine;
    private boolean mFoundLeftLine;
    private boolean mFoundRightLine;     
    private boolean mColorsOnLeftLane;
    private boolean mColorsOnRightLane;

	private Bitmap mBitmapTrackOverlay;
	private Bitmap mCarRecognizer;
	private Bitmap mLeftCarColorImage;
	private Bitmap mRightCarColorImage;
	private Bitmap[] mCarColorImages;	
	
	private Scalar mLeftCarColorScalar;
	private Scalar mRightCarColorScalar;

	double[] mScannedTrackPixelColor;
	double[] mEmptyTrackPixelColor;
	List <double[]> mFoundColors = new ArrayList<double[]>();	
	
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
	 * This Function is used to get the center x-axis values of the 2 lanes. 
	 * It creates an Integer Array for the 2 Integer values. 
	 * First  Position(0) takes the x-axis value for the center between the Left- and SeparatorLine. 
	 * Second Position(1) takes the x-axis value for the center between Separator- and RightLine.	
	 * @return The Integer Array with the x-axis center values for both lanes. (0: left-center | 1: right-center).
	 */
	public int[] getCenterOfLanes(){
		int[] arr = new int[2];
		arr[0] = (int) (((mSeparatorX - mLeftX) / 2) + mLeftX);
		arr[1] = (int) (((mRightX -mSeparatorX) /2)+mSeparatorX);
		
		return arr;
	}
	
	/**
	 * This Function draws 2 rectangles onto a Bitmap. A Mat gets the size of the input Frame. The position for the rectangles are determined by calling getCenterofLanes(). After setting an offset for them and drawing with color 'white' the Mat is converted to a Bitmap of the same size.
	 * @return The Bitmap with drawed rectangles centered in between every lane, null if Function matToBitmap() fails.
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
	 * @return The Rectangle Array with rectangles and there position information. (index 0: left-rectangle| index 1: right-rectangle)
	 */
	public Rect[] getLanesToScanColor(){
		Rect[] arr = new Rect[2];
		arr[0] = new Rect(new Point(getCenterOfLanes()[0] - HALF_SQUARE_SIDE_LENGTH, (mInputFrame.rows()/2) - HALF_SQUARE_SIDE_LENGTH), new Point(getCenterOfLanes()[0] + HALF_SQUARE_SIDE_LENGTH, (mInputFrame.rows()/2) + HALF_SQUARE_SIDE_LENGTH));
		arr[1] = new Rect(new Point(getCenterOfLanes()[1] - HALF_SQUARE_SIDE_LENGTH, (mInputFrame.rows()/2) - HALF_SQUARE_SIDE_LENGTH), new Point(getCenterOfLanes()[1] + HALF_SQUARE_SIDE_LENGTH, (mInputFrame.rows()/2) + HALF_SQUARE_SIDE_LENGTH));
		
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
	 * The used method isDifferent() checks, if the calculated average color differs from the calculated average color of the empty track.
	 * If a color difference is found, this function creates a Bitmap with the size of the rectangle given by getLanesToScanColor(), filled with the color.
	 * @param xOffset The x- offset to get the x-axis position to start color detection.
	 * @param yOffset The y- offset to get the y-axis position to start color detection.
	 * @param lane The lane to scan.
	 * @return The Bitmap with the found average color, Null if no color difference has been found.
	 */
	private Bitmap getColorInOffset(int xOffset, int yOffset, int lane){
		
		Log.i("debug", "Into getColorInOffset for lane :"+lane);	
//		if(mInputFrame.empty()){
//			Log.i("debug","mInputFrame is empty!");
//			return null;
//		}			
		mRgba = mInputFrame.clone();
		
		double[] oldColors = new double[4];
		double[] newColors = new double[4];
		double[] avg_oldColors = new double[4];
		double[] avg_newColors = new double[4];					
		if(lane == Race.LEFT_LANE)
			mColorsOnLeftLane = false;
		else if(lane == Race.RIGHT_LANE)
			mColorsOnRightLane = false;
		Log.i("debug", "Before FOR getColorInOffset for lane :"+lane);			
		for(int x = xOffset;x< xOffset +SQUARE_SIDE_LENGTH;x++){
			for (int y = yOffset;y < yOffset+SQUARE_SIDE_LENGTH;y++){
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
			avg_oldColors[i] = oldColors[i] / SQUARE_AREA;
			avg_newColors[i] = newColors[i] / SQUARE_AREA;

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
	public Bitmap generateTrackOverlay() {
		
		mFoundSeparatorLine = false;
		mFoundLeftLine = false;
		mFoundRightLine = false;
		mGray = new Mat();
		mEdges = new Mat();
		mHoughLines = new Mat();
		mEmptyTrack = new Mat();
	
//		if(mInputFrame.empty()){
//			Log.i("debug","mInputFrame is empty!");
//			return null;
//		}		
		
		ArrayList<Point> separatorPointsList = new ArrayList<Point>();
		ArrayList<Point> leftPointsList = new ArrayList<Point>();
		ArrayList<Point> rightPointsList = new ArrayList<Point>();
		//mStaticImage = Highgui.imread(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+ "track.jpg");
		Imgproc.cvtColor(mInputFrame, mGray, Imgproc.COLOR_RGBA2GRAY);		
		int avgGray;
		double wert = 0;	
				
		for (int i = 0; i < mGray.rows(); i++) {
			for (int j = 0; j < mGray.cols(); j++) {			
				wert += mGray.get(i, j)[0];							
			}
		}
		avgGray = (int) (wert / (mGray.rows() * mGray.cols()));
		
		Log.i("debug", "avg_gray: " + avgGray);

		mLowerThreshold = (int) (avgGray * ( 1 - CANNY_THRESHOLD));
		mUpperThreshold = (int) (avgGray * ( 1 + CANNY_THRESHOLD));

		Imgproc.Canny(mGray, mEdges, mLowerThreshold, mUpperThreshold);
		//mGray.release();
		//Highgui.imwrite("/houghlines.png", mEdges);
		Log.i("debug","Status Sd-Karte: "+ Environment.getExternalStorageState());
		File mStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

		File mCannyEdgeImage = new File(mStorageDir, "canny.bmp");
		String mPath = mCannyEdgeImage.toString();
		Boolean bool = Highgui.imwrite(mPath, mEdges);
		if (bool)
			Log.i("debug", "SUCCESS writing canny.bmp to external storage");
		else
			Log.i("debug", "Fail writing canny.bmp to external storage");
		
		mMatTrackOverlay = new Mat(new Size(mInputFrame.cols(), mInputFrame.rows()), mInputFrame.type(), new Scalar(0, 0, 0,0));
		
		if((!mEdges.empty() && !mInputFrame.empty()))
		{	
			Imgproc.HoughLinesP(mEdges, mHoughLines, 1, Math.PI / 180, mHLthreshold, mHLminLineSize, mHLlineGap);
			
			for (int x = 0; x < mHoughLines.cols(); x++) {
				double[] vec = mHoughLines.get(0, x);
				double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
				//check for usable Lines at the center of the frame
				if ((x1 > mInputFrame.cols() / 2 - SEPARATORLINE_TOLERATION) && (x1 < mInputFrame.cols() / 2 + SEPARATORLINE_TOLERATION) && (x2 > mInputFrame.cols() / 2 - SEPARATORLINE_TOLERATION)	&& (x2 < mInputFrame.cols() / 2 + SEPARATORLINE_TOLERATION)) 
				{					
					separatorPointsList.add(new Point(x1, y1));
					separatorPointsList.add(new Point(x2, y2));		
					mFoundSeparatorLine = true;						
				}
				
				//check for usable Lines at the left side of the frame
				else if(x1 < 50 && x2 < 50)
				{				
					leftPointsList.add(new Point(x1, y1));
					leftPointsList.add(new Point(x2, y2));				
					mFoundLeftLine = true;
				}
				
				//check for usable Lines at the right side of the frame
				else if(x1 > mInputFrame.cols() - 50 && x2 > mInputFrame.cols() - 50)
				{			
					rightPointsList.add(new Point(x1, y1));
					rightPointsList.add(new Point(x2, y2));								
					mFoundRightLine = true;
				}
			}		
			mSeparatorX = getTheMostlyMiddleX(separatorPointsList);				
			Core.line(mMatTrackOverlay, new Point(mSeparatorX, 0), new Point(mSeparatorX, mInputFrame.rows()-1),new Scalar(0, 0, 255, 255), 3);
			mLeftX = getTheMostlyRightLeftX(leftPointsList);
			Core.line(mMatTrackOverlay, new Point(mLeftX, 0), new Point(mLeftX, mInputFrame.rows()-1),new Scalar(0, 0, 255, 255), 3);
			mRightX = getTheMostlyLeftRightX(rightPointsList);
			Core.line(mMatTrackOverlay, new Point(mRightX, 0), new Point(mRightX, mInputFrame.rows()-1),new Scalar(0, 0, 255, 255), 3);			
		}						
			
			mBitmapTrackOverlay = Bitmap.createBitmap(mMatTrackOverlay.cols(), mMatTrackOverlay.rows(), Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(mMatTrackOverlay, mBitmapTrackOverlay);
			
			mInputFrame.copyTo(mEmptyTrack); 	
			return mBitmapTrackOverlay;
		}	
	
	/**
	 * This method is called with a list of point in a certain area (near the separator line) of the input frame. 
	 * @param separatorPointsList The List containing the point near the separator line.
	 * @return The x-axis value of the point whose x-axis values is mostly in the middle. Returns 0, if the list null or has no points in it.
	 */
	private int getTheMostlyMiddleX(ArrayList<Point> separatorPointsList){
		
		int distanceToTheMiddle = Integer.MAX_VALUE;
		boolean leftFromTheMiddle = false;
		boolean rightFromTheMiddle = false;
		if(separatorPointsList != null)
		{
			for(Point p : separatorPointsList)
			{
				
				if(p.x > mInputFrame.cols()/2)
				{
					Log.i("debug", "Separator Point Right to the Middle!");
					if((p.x - mInputFrame.cols()/2) < distanceToTheMiddle){
						distanceToTheMiddle = (int) (p.x - mInputFrame.cols()/2);
						leftFromTheMiddle = false;
						rightFromTheMiddle = true;
						Log.i("debug", "It is '" + distanceToTheMiddle + "' Pixels away!");
					}
						
				}
				else if(p.x < mInputFrame.cols()/2)
				{
					Log.i("debug", "Separator Point Left to the Middle!");
					if((mInputFrame.cols()/2 - p.x) < distanceToTheMiddle)
					{						
						distanceToTheMiddle = (int) (mInputFrame.cols()/2 - p.x);
						rightFromTheMiddle = false;
						leftFromTheMiddle = true;
						Log.i("debug", "It is '" + distanceToTheMiddle + "' Pixels away!");
					}
				}
			}
			if(rightFromTheMiddle)
				return mInputFrame.cols()/2 + distanceToTheMiddle;
			else if(leftFromTheMiddle)
				return mInputFrame.cols()/2 - distanceToTheMiddle;
			else
				return 0;			
		}
		else
			return 0;
	}
	
	/**
	 * This method is called with a list of point in a certain area (near the left separator line) of the input frame. 
	 * @param separatorPointsList The List containing the point near the left separator line.
	 * @return The x-axis value of the point whose x-axis values is mostly on the right side in this area. Returns 0, if the list is null.
	 */
	private int getTheMostlyRightLeftX(ArrayList<Point> leftPointsList){
		int mostlyRightLeftX = Integer.MIN_VALUE;
		
		if(leftPointsList != null)
		{
			for(Point p : leftPointsList)
			{
				if(p.x > mostlyRightLeftX)
				{
					mostlyRightLeftX = (int) p.x;
				}
			}
			return mostlyRightLeftX + LEFT_LANE_THRESHOLD;
		}
		else
			return 0;		
	}
	
	/**
	 * This method is called with a list of point in a certain area (near the right separator line) of the input frame. 
	 * @param separatorPointsList The List containing the point near the right separator line.
	 * @return The x-axis value of the point whose x-axis values is mostly on the left side in this area. Returns 0, if the list is null.
	 */
	private int getTheMostlyLeftRightX(ArrayList<Point> rightPointsList){
		int mostlyLeftRightX = Integer.MAX_VALUE;
		
		if(rightPointsList != null)
		{
			for(Point p : rightPointsList)
			{
				if(p.x < mostlyLeftRightX)
				{
					mostlyLeftRightX = (int) p.x;
				}
			}
			return mostlyLeftRightX + RIGHT_LANE_THRESHOLD;
		}
		else
			return 0;		
	}
	
	/**
	 * This Function checks two given double[]'s for a value difference of certain amount in at least one color value.
	 * @param avgOldColors The double[] with color values from the track lane.
	 * @param avgNewColors The double[] with color values from a possible car.
	 * @return TRUE if colors are different, FALSE if colors are the same.
	 */
	private boolean isDifferent(double[] avgOldColors, double[] avgNewColors){
		
//		if((avgNewColors[0] == 0.0) && (avgNewColors[1] == 0.0) && (avgNewColors[2] == 0.0) && (avgNewColors[3] == 0.0))
//			return false;
		
		for(int i = 0; i < 4; i++)
		{
			if(avgNewColors[i] > avgOldColors[i] + COLOR_DIFFENRENCE ||avgNewColors[i] < avgOldColors[i] - COLOR_DIFFENRENCE)			
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
		return mRightX;
	}
	
	/**
	 * This Function gets the x-axis value of the leftLine.
	 * @return X-axis value for the leftLine.
	 */
	public int getLeftSeparator(){
		return mLeftX;
	}

	
	/**
	 * This Function gets the x-axis value of the middleLine.
	 * @return X-axis value for the middleLine.
	 */
	public int getMiddleSeparator(){
		return mSeparatorX;
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
	
	/*
	private boolean validInputFrame(Mat inputFrame)
	{
		if(inputFrame.get(1, 1)[3] != 0.0)
			return true;
		else
			return false;
	}
	*/

}
