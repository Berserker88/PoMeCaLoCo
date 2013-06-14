package de.freinsberg.pomecaloco;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class ObjectDetector {
	
	private CvCameraViewFrame inputFrame;	
	private Mat mRgba, mHSV, mEdges, removed_track_overlay;
	
	public ObjectDetector(CvCameraViewFrame inputFrame){
		this.inputFrame = inputFrame;	
		
	}
	
	public Mat draw_colorrange_on_frame (Scalar lowerLimit, Scalar upperLimit){
		//Log.i("debug", "onCameraFrame");
		mRgba = inputFrame.rgba();
		Mat mThreshed = new Mat(mRgba.size(),mRgba.type());
		//Log.i("debug",Integer.toString(mRgba.cols()));
		//Core.line(mRgba, new Point(10,10), new Point(200,200), new Scalar(255, 0, 0, 255));
		mEdges = new Mat(mRgba.size(),mRgba.type());
		mHSV = new Mat(mRgba.size(),mRgba.type());	
		//Imgproc.Canny(mRgba, mEdges, 50, 200);
		Imgproc.cvtColor(mRgba, mHSV, Imgproc.COLOR_BGR2HSV);	
		Core.inRange(mHSV, lowerLimit, upperLimit, mThreshed);
		
		mHSV.release();
	/*		Imgproc.cvtColor((Mat) inputFrame, mColoredFrame, Imgproc.COLOR_RGB2HSV);
			Log.i("debug", "Color to HSV");
			Core.inRange(mColoredFrame, lowerColorLimit, upperColorLimit, mColoredResult);
			Log.i("debug", "Colored Frame!");
			mFilteredFrame.setTo(new Scalar(0, 0, 0));
			Log.i("debug", "cleared new Frame");
			((Mat) inputFrame).copyTo(mFilteredFrame, mColoredResult);
			Log.i("debug", "Result!");*/	
			//Log.i("debug", "Grayscaled");
		return mThreshed;
	}
	
	public Mat remove_track_overlay (Mat m){
		//Hier muss ein Bild des leeren Streckenausschnitts ankommen
				//Dann muss für die Weiterverarbeitung das Bild von ankommenden Frames abgezogen werden.
		
		
		return m;
	}
	
	public Mat get_position_and_colors (Mat m){
		//Hier muss ein Bild mit den Fahrzeugen auf dem Streckenausschnitt ankommen
		//Dann werden die Positionen gesetzt.
		
		
		return m;
	}
	
	
	

}
