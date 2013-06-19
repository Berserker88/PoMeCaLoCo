package de.freinsberg.pomecaloco;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;


public class ObjectDetector{
	
	private static final int ORIENTATION_0 = 0;
	private static final int ORIENTATION_90 = 90;
	private static final int ORIENTATION_270 = 270;
	private CvCameraViewFrame inputFrame;	
	private Mat mRgba = null;
	private Mat mHSV = null; 
	private Mat mEdges = null; 
	private Mat mThreshed = null; 
	private Mat removed_track_overlay = null;
	
	public ObjectDetector(CvCameraViewFrame inputFrame){
		
		this.inputFrame = inputFrame;	
		
	}
	
	private Mat correct_rotation(Display d, Mat m){
		//Log.i("debug", "Jetzt wird rotiert!");
		//Mat gray = new Mat(m.size(), m.type());
		//Mat rotated = new Mat(new Size(m.rows(),m.cols()), m.type());
		
		//Imgproc.cvtColor(m, gray, Imgproc.COLOR_RGB2GRAY);

		//Log.i("debug", "Found rotation: "+d.getRotation());
	    int screenOrientation = d.getRotation();
	    switch (screenOrientation){
	        default:
	        case ORIENTATION_0: // Portrait	
	        	/*Log.i("debug", "Original Channels: "+m.channels());
	        	Log.i("debug", "Original Size: "+m.size().toString());
	        	Log.i("debug", "Original Type: "+m.type());
	        	Log.i("debug", "Grayscale Channels: "+gray.channels());
	        	Log.i("debug", "Grayscale Size: "+gray.size().toString());
	        	Log.i("debug", "Grayscale Type: "+gray.type());
	            Core.flip(gray.t(), gray, 1);
	            Log.i("debug", "Grayscale after transpose Channels: "+gray.channels());
	        	Log.i("debug", "Grayscale after transpose Size: "+gray.size().toString());
	        	Log.i("debug", "Grayscale after transpose Type: "+gray.type());
	            Imgproc.cvtColor(gray, rotated, Imgproc.COLOR_GRAY2RGB, 4);
	        	Log.i("debug", "Rotated after cvt Channels: "+rotated.channels());
	        	Log.i("debug", "Rotated after cvt Size: "+rotated.size().toString());
	        	Log.i("debug", "Rotated after cvt Type: "+rotated.type());*/
	        	
	            //Log.i("debug", "Depth: "+m.depth()+ "Height: "+m.height()+"Width: "+m.width());
	            //Core.flip(m.t(), rotated, 1);
	            //Log.i("debug", "Depth: "+rotated.depth()+ "Height: "+rotated.height()+"Width: "+rotated.width());
	            //Log.i("debug", "to Portrait");
	            //rotated.release();
	            break;
	        case ORIENTATION_90: // Landscape right
	            // do smth.
	            break;
	        case ORIENTATION_270: // Landscape left
	            // do smth.
	            break;
	    }
	    return m;
	}
		
	public Mat draw_colorrange_on_frame (Display d, Scalar lowerLimit, Scalar upperLimit){
		//Log.i("debug", "onCameraFrame");
		mRgba = correct_rotation(d, inputFrame.rgba());
		if(mThreshed != null)
			mThreshed.release();
		mThreshed = new Mat(mRgba.size(),mRgba.type());
		//Log.i("debug",Integer.toString(mRgba.cols()));
		//Core.line(mRgba, new Point(10,10), new Point(200,200), new Scalar(255, 0, 0, 255));
		//mEdges = new Mat(mRgba.size(),mRgba.type());
		mHSV = new Mat(mRgba.size(),mRgba.type());	
		//Imgproc.Canny(mRgba, mEdges, 50, 200);
		Imgproc.cvtColor(mRgba, mHSV, Imgproc.COLOR_RGB2HSV);	
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
