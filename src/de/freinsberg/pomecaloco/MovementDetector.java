package de.freinsberg.pomecaloco;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import android.util.Log;


/**
 * This Class represents a MovementDetector
 * @author freinsberg
 *
 */
public class MovementDetector {
	final public static int HUE_THRESHOLD = 5;
	final public static int SATURATION_THRESHOLD = 80;
	final private static int LOWER_SATURATION_VALUE = 180;
	final private static int LOWER_BRIGHTNESS_VALUE = 180;
	final private static int UPPER_SATURATION_VALUE = 255;
	final private static int UPPER_BRIGHTNESS_VALUE = 255;	
	final public static int VALUE_THRESHOLD = 80;
	private static final int X_AXIS_STEP_SIZE = 25;
	private static final int Y_AXIS_STEP_SIZE = 25;
	private Mat mInputFrame;
	
	/**
	 * Constructor: Creates a MovementDetector for a given Mat. This Mat is rotated 90° clock-wise by doing a transpose followed by a horizontal flip. This is done because the given Mat comes in landscape format and the Mat to work with should be the same format as it is seen on the camera frame.
	 * @param inputFrame The input frame, delivered by the camera.
	 */
	public MovementDetector (Mat inputFrame) {
		mInputFrame = inputFrame;			
		mInputFrame = inputFrame.t();		
		Core.flip(mInputFrame, mInputFrame, 1);	
		
	}
	/**
	 * This Function is used to release the created input Frame after usage.
	 */
	public void clear(){
		mInputFrame.release();
	}
	/**
	 * This Function is used to detect a color in a given RGB Frame.
	 * @param color The color to detect.
	 * @return TRUE if the given Color was found in this Frame, FALSE if the given Color wasn't found.
	 */
	public boolean detectColor(Scalar color){
		
		Mat bgrColored = new Mat(new Size(10,10),mInputFrame.type(),color);		
//		File mStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
//		File bgrFile = new File(mStorageDir, "bgred.bmp");
//		String mPath = bgrFile.toString();
//		Highgui.imwrite(mPath, bgrColored);
		
		Mat hsvColored = new Mat();
		Mat hsvImage = new Mat();
		Mat rgbImage = new Mat();
		int upperHue;

		int lowerHue;

		Mat thresholdedImage = new Mat();
		Mat thresholdedImageUpperRange = new Mat();
		Mat thresholdedImageLowerRange = new Mat();
		Scalar upperColorThreshold;
		Scalar lowerColorThreshold;
		double[] exactColorHSV;
		
		Imgproc.cvtColor(bgrColored, hsvColored, Imgproc.COLOR_RGB2HSV);
		exactColorHSV = hsvColored.get(0, 0);

		
		upperHue = (int) (exactColorHSV[0]+HUE_THRESHOLD);
		lowerHue = (int) (exactColorHSV[0]-HUE_THRESHOLD);
		if(upperHue >179)
			upperHue -= 180;
		if(lowerHue <0)
			lowerHue += 180;
	
		Imgproc.cvtColor(mInputFrame, hsvImage, Imgproc.COLOR_RGB2HSV);
		lowerColorThreshold = new Scalar(lowerHue,LOWER_SATURATION_VALUE,LOWER_BRIGHTNESS_VALUE);				
		upperColorThreshold = new Scalar(upperHue,UPPER_SATURATION_VALUE,UPPER_BRIGHTNESS_VALUE);
		
		if (lowerHue < upperHue)
			Core.inRange(hsvImage, lowerColorThreshold, upperColorThreshold, thresholdedImage);		
		else{
			Log.i("color", "huhu!");
			Scalar sZeroDegrees = new Scalar(0,LOWER_SATURATION_VALUE,LOWER_BRIGHTNESS_VALUE);
			Scalar sFullDegrees = new Scalar(179,UPPER_SATURATION_VALUE,UPPER_BRIGHTNESS_VALUE);
			Core.inRange(hsvImage, lowerColorThreshold, sFullDegrees, thresholdedImageLowerRange);
			Core.inRange(hsvImage, sZeroDegrees, upperColorThreshold, thresholdedImageUpperRange);			
			Core.add(thresholdedImageLowerRange, thresholdedImageUpperRange, thresholdedImage);
		}
			
	
//		Imgproc.cvtColor(thresholdedImage, rgbImage, Imgproc.COLOR_GRAY2BGR);		
//		bgrFile = new File(mStorageDir, "car in here.bmp");
//		mPath = bgrFile.toString();
//		Highgui.imwrite(mPath, rgbImage);

		boolean found = false;		
		
		for(int i = 0; i < thresholdedImage.rows();i+=X_AXIS_STEP_SIZE){			
			for(int j = ObjectDetector.getInstance().getLeftSeparator(); j < ObjectDetector.getInstance().getRightSeparator(); j+=Y_AXIS_STEP_SIZE){				
				if(thresholdedImage.get(i, j)[0] == 255){
					found = true;
					break;
				}
				//Log.i("color", "i:"+i+", j:"+j+" = "+tmp[0]);
			}
			if(found)
				break;
		}	
		
		rgbImage.release();
		thresholdedImage.release();
		hsvImage.release();
		
		return found;
	}
}
