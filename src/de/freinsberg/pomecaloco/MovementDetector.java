package de.freinsberg.pomecaloco;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.util.Log;



public class MovementDetector {
	final public static int HUE_THRESHOLD = 5;
	final public static int SATURATION_THRESHOLD = 80;
	final public static int VALUE_THRESHOLD = 80;
	public Mat mInputFrame;
	
	public boolean mColorDetected;
	
	public MovementDetector (Mat inputFrame) {
		mInputFrame = inputFrame;			
		mInputFrame = inputFrame.t();		
		Core.flip(mInputFrame, mInputFrame, 1);	
		
	}
	
	public void clear(){
		mInputFrame.release();
	}
	
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
		for(int i = 0; i < rgbImage.rows();i+=25){			
			for(int j = 0; j < rgbImage.cols(); j+=25){
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
		
		//rgbImage.release();
		thresholdedImage.release();
		hsvImage.release();		
		
		return found;
	}
}
