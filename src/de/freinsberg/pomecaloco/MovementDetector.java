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
	final public static int HUE_THRESHOLD = 3;
	final public static int SATURATION_THRESHOLD = 80;
	final public static int VALUE_THRESHOLD = 80;
	public Mat mInputFrame;	
	public boolean mColorDetected;
	
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
	public boolean colorDetected(Scalar color){
		//Log.i("debug", "Jetzt check ich hier mal color Movement in colorDetected");
		Mat bgrColored = new Mat(new Size(1,1),mInputFrame.type(),color);
		Mat hsvColored = new Mat();
		Mat hsvImage = new Mat();
		Mat rgbImage = new Mat();
		int huePlus;
		int saturationPlus;
		int valuePlus;
		int hueMinus;
		int saturationMinus;
		int valueMinus;
		Mat thresholdedImage = new Mat();
		Scalar upperColorThreshold;
		Scalar lowerColorThreshold;
		double[] exactColor;
		
		Imgproc.cvtColor(bgrColored, hsvColored, Imgproc.COLOR_BGR2HSV);
		exactColor = hsvColored.get(0, 0);	
		huePlus = (int) (exactColor[0]+HUE_THRESHOLD);
		saturationPlus = (int) (exactColor[1]+SATURATION_THRESHOLD);
		valuePlus = (int) (exactColor[2]+VALUE_THRESHOLD);
		hueMinus = (int) (exactColor[0]-HUE_THRESHOLD);
		saturationMinus = (int) (exactColor[1]-SATURATION_THRESHOLD);
		valueMinus = (int) (exactColor[2]-VALUE_THRESHOLD);
		if(huePlus <0)
			huePlus = 0;
		if(saturationPlus <0)
			saturationPlus = 0;
		if(valuePlus <0)
			valuePlus = 0;
		if(hueMinus <0)
			hueMinus = 0;
		if(saturationMinus <0)
			hueMinus = 0;
		if(valueMinus <0)
			huePlus = 0;
		
			
		upperColorThreshold = new Scalar(huePlus,255,255);
		lowerColorThreshold = new Scalar(hueMinus,100,100);		
		
		Imgproc.cvtColor(mInputFrame, hsvImage, Imgproc.COLOR_BGR2HSV);
		Core.inRange(hsvImage, lowerColorThreshold, upperColorThreshold, thresholdedImage);
		
		Imgproc.cvtColor(thresholdedImage, rgbImage, Imgproc.COLOR_GRAY2BGR);
		
		double[] tmp;
		boolean found = false;
		
		for(int i = 0; i < rgbImage.rows();i+=50){			
			for(int j = ObjectDetector.getInstance().getLeftSeparator(); j < ObjectDetector.getInstance().getRightSeparator(); j+=50){
				tmp = rgbImage.get(i, j);
				if(tmp[0] == 255){
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
