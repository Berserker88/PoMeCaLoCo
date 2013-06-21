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
	private static CvCameraViewFrame mInputFrame;	
	private Mat mStaticImage = null;
	private Mat mRgba = null;
	private Mat mGray = null;
	private Mat mHSV = null; 
	private Mat mEdges = null; 
	private Mat mHoughLines = null;
	private int mHLthreshold = 30;
    private int mHLminLineSize = 200;
    private int mHLlineGap = 20;
    private Point mLowerPoint;
    private Scalar mUpperLaneColor;
    private Scalar mLowerLaneColor;
    private Point mSeparatorPoint;
    private Point mUpperPoint;
    private boolean mFoundSeparatorLine;
    private boolean mFoundUpperLine;
    private boolean mFoundLowerLine;
    private boolean mDrawedLinesOnFrame;    
	private int mLowerThreshold;
	private int mUpperThreshold;
	private Mat mThreshed = null; 
	private Mat mLowerRangeImage = null;
	private Mat mUpperRangeImage = null;
	private Mat mEmptyTrack = null;
	private Mat track_overlay = null;
	private Bitmap mTrackOverlay= null;
	private int avg_gray;
	private File mStorageDir;
	private File mHoughLinesImage;
	private File mCannyEdgeImage;
	private File mTrackColor;
	private String mPath;
	private static ObjectDetector mObjectDetector = new ObjectDetector();
	
	private ObjectDetector(){				
		
	}
	
	public static ObjectDetector getInstance(CvCameraViewFrame inputFrame){
		
		mInputFrame = inputFrame;		
		return mObjectDetector;
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
		
//	public Mat draw_colorrange_on_frame (Display d, Scalar lowerLimit, Scalar upperLimit){
//		//Log.i("debug", "onCameraFrame");
//		mRgba = correct_rotation(d, mInputFrame.rgba());
//		if(mThreshed != null)
//			mThreshed.release();
//		mThreshed = new Mat(mRgba.size(),mRgba.type());
//		//Log.i("debug",Integer.toString(mRgba.cols()));
//		//mEdges = new Mat(mRgba.size(),mRgba.type());
//		mHSV = new Mat(mRgba.size(),mRgba.type());	
//		Imgproc.cvtColor(mRgba, mHSV, Imgproc.COLOR_RGB2HSV);	
//		Core.inRange(mHSV, lowerLimit, upperLimit, mThreshed);
//		
//		mHSV.release();
//	/*		Imgproc.cvtColor((Mat) inputFrame, mColoredFrame, Imgproc.COLOR_RGB2HSV);
//			Log.i("debug", "Color to HSV");
//			Core.inRange(mColoredFrame, lowerColorLimit, upperColorLimit, mColoredResult);
//			Log.i("debug", "Colored Frame!");
//			mFilteredFrame.setTo(new Scalar(0, 0, 0));
//			Log.i("debug", "cleared new Frame");
//			((Mat) inputFrame).copyTo(mFilteredFrame, mColoredResult);
//			Log.i("debug", "Result!");*/	
//			//Log.i("debug", "Grayscaled");
//		return mThreshed;
//	}
	public boolean getFoundLines(){
		return mFoundSeparatorLine && mFoundUpperLine && mFoundLowerLine;
	}
	
	public int[] getCenterOfLanes(){
		int[] arr = new int[2];
		arr[0] = (int) (((mSeparatorPoint.y - mUpperPoint.y) / 2) + mUpperPoint.y);
		arr[1] = (int) (((mLowerPoint.y -mSeparatorPoint.y) /2)+mSeparatorPoint.y);
		
		return arr;
	}
	
	public Bitmap generate_track_overlay() {
		// Load an Image to try operations on local stored files
		mFoundSeparatorLine = false;
		mFoundUpperLine = false;
		mFoundLowerLine = false;
		mGray = new Mat();
		mEdges = new Mat();
		mHoughLines = new Mat();
		mEmptyTrack = new Mat();
		mStaticImage = Highgui
				.imread(Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
						+ "track.jpg");

		track_overlay = new Mat(new Size(mInputFrame.rgba().cols(), mInputFrame
				.rgba().rows()), mInputFrame.rgba().type(), new Scalar(0, 0, 0,
				0));
		Log.i("debug", "Channels: " + track_overlay.channels());

		Imgproc.cvtColor(mInputFrame.rgba(), mGray, Imgproc.COLOR_RGBA2GRAY);

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
		mGray.release();
		// Highgui.imwrite("/houghlines.png", mHoughLines);
		Log.i("debug",
				"Status Externer Speciher: "
						+ Environment.getExternalStorageState());
		mStorageDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

		mCannyEdgeImage = new File(mStorageDir, "canny.bmp");
		mPath = mCannyEdgeImage.toString();
		Boolean bool = Highgui.imwrite(mPath, mEdges);
		if (bool)
			Log.i("debug", "SUCCESS writing canny.bmp to external storage");
		else
			Log.i("debug", "Fail writing canny.bmp to external storage");

		Imgproc.HoughLinesP(mEdges, mHoughLines, 1, Math.PI / 180,
				mHLthreshold, mHLminLineSize, mHLlineGap);
		for (int x = 0; x < mHoughLines.cols(); x++) {
			double[] vec = mHoughLines.get(0, x);
			double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];

			if (y1 > mInputFrame.rgba().rows() / 2 - 30
					&& y1 < mInputFrame.rgba().rows() / 2 + 30
					&& y2 > mInputFrame.rgba().rows() / 2 - 30
					&& y2 < mInputFrame.rgba().rows() / 2 + 30) {
				mFoundSeparatorLine = true;
				Point start = new Point(x1, y1);
				Point end = new Point(x2, y2);
				mDrawedLinesOnFrame = true;
				Core.line(track_overlay, start, end,new Scalar(0, 0, 255, 255), 3);
				mSeparatorPoint = new Point(x1,y1);
				
			}
			else if(y1 < 50 && y2 < 50)
			{
				mFoundUpperLine = true;
				Point start = new Point(x1, y1);
				Point end = new Point(x2, y2);
				mDrawedLinesOnFrame = true;
				Core.line(track_overlay, start, end,new Scalar(0, 0, 255, 255), 3);	
				mUpperPoint = new Point(x1,y1);
			}
			else if(y1 > mInputFrame.rgba().rows() - 50 && y2 > mInputFrame.rgba().rows() - 50)
			{
				mFoundLowerLine = true;
				Point start = new Point(x1, y1);
				Point end = new Point(x2, y2);
				mDrawedLinesOnFrame = true;
				Core.line(track_overlay, start, end,new Scalar(0, 0, 255, 255), 3);			
				mLowerPoint = new Point(x1,y1);
			}
			

		}
		mHoughLinesImage = new File(mStorageDir, "houghed.png");
		mPath = mHoughLinesImage.toString();
		bool = Highgui.imwrite(mPath, track_overlay);
		if (bool)
			Log.i("debug", "SUCCESS writing houghed.png to external storage");
		else
			Log.i("debug", "Fail writing houghed.png to external storage");
		
		
		//Getting average color for every lane
		
		//Upper lane
		mInputFrame.rgba().copyTo(mEmptyTrack); 	
		
		double[] rgba = null;
		double R, G, B, A;
		double RCount = 0;
		double GCount = 0;
		double BCount = 0;
		double ACount = 0;
		double avgR = 0;
		double avgG = 0;
		double avgB = 0;
		double avgA = 0;
		
		//Upper lane		
		if(mFoundUpperLine){				
			for(int i = ((int)mUpperPoint.y+10); i < mSeparatorPoint.y-10; i++){
				for (int j = 0;j < mEmptyTrack.cols();j++){
				if (rgba != null)
					rgba = null;
				rgba = mEmptyTrack.get(i, j);
				R = rgba[0];
				G = rgba[1];
				B = rgba[2];	
				A = rgba[3];
				divider++;
				RCount += R;
				GCount += G;
				BCount += B;
				ACount += A;						
				}
			}	
			
			mUpperLaneColor = new Scalar(avgR,avgG,avgB,avgA);
			avgR = RCount /divider;
			avgG = GCount /divider;
			avgB = BCount/divider;
			avgA = ACount/ divider;
			Log.i("debug", "Start: "+mUpperPoint.y);
			Log.i("debug", "Ende: "+mSeparatorPoint.y);
			Log.i("debug", "UPPER LANE: ");
			Log.i("debug", "avg R: "+avgR);
			Log.i("debug", "avg G: "+avgG);
			Log.i("debug", "avg B: "+avgB);
			Log.i("debug", "avg Alpha: "+avgA);	
			
		}
		//Lower lane
		if(mFoundLowerLine){				
			for(int i = ((int)mLowerPoint.y-10); i > mSeparatorPoint.y+10; i--){
				for (int j = 0;j < mEmptyTrack.cols();j++){
				if (rgba != null)
					rgba = null;
				rgba = mEmptyTrack.get(i, j);
				R = rgba[0];
				G = rgba[1];
				B = rgba[2];	
				A = rgba[3];
				divider++;
				RCount += R;
				GCount += G;
				BCount += B;
				ACount += A;						
				}
			}	
					
			mLowerLaneColor = new Scalar(avgR,avgG,avgB,avgA);
			avgR = RCount /divider;
			avgG = GCount /divider;
			avgB = BCount/divider;
			avgA = ACount/ divider;
			Log.i("debug", "Start: "+mLowerPoint.y);
			Log.i("debug", "Ende: "+mSeparatorPoint.y);
			Log.i("debug", "LOWER LANE: ");
			Log.i("debug", "avg R: "+avgR);
			Log.i("debug", "avg G: "+avgG);
			Log.i("debug", "avg B: "+avgB);
			Log.i("debug", "avg Alpha: "+avgA);	
		}
		

		mTrackOverlay = Bitmap.createBitmap(track_overlay.cols(),
				track_overlay.rows(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(track_overlay, mTrackOverlay);

		return mTrackOverlay;
	}
		
	public Mat get_cars_position_and_colors (){
		Mat mDiff = new Mat();
		mRgba = mInputFrame.rgba();
		Mat hsva = new Mat();
		Mat hsvb = new Mat();
				
		Imgproc.cvtColor(mRgba, hsva, Imgproc.COLOR_BGR2HSV, 3);		
		Imgproc.cvtColor(mEmptyTrack, hsvb, Imgproc.COLOR_BGR2HSV, 3);
		Core.absdiff(hsva, hsvb, mDiff);
		Imgproc.cvtColor(mDiff, mDiff, Imgproc.COLOR_HSV2BGR);
		//Imgproc.cvtColor(mDiff, mDiff, Imgproc.COLOR_BGR2RGB);
		
		Highgui.imwrite(new File(mStorageDir, "mDiff.png").toString(), mDiff);
		Highgui.imwrite(new File(mStorageDir, "hsva.png").toString(), hsva);
		Highgui.imwrite(new File(mStorageDir, "hsvb.png").toString(), hsvb);
		Highgui.imwrite(new File(mStorageDir, "mRgba.png").toString(), mRgba);
		Highgui.imwrite(new File(mStorageDir, "mEmptyTrack.png").toString(), mEmptyTrack);
		double[] rgba = null, temp;
		List <double[]> foundcolors = new ArrayList<double[]>();
		
		
		
		for(int i = (int) mUpperPoint.y;i< mSeparatorPoint.y;i++){
			for (int j = 0;j < mRgba.cols();j++){
				
				temp = mEmptyTrack.get(i, j);
				rgba = mRgba.get(i, j);
				
				double min = 256;
				double max = -1;
				
				for(int k = 0; k < 3; k++) {
					if(rgba[k] < min)
						min = rgba[k];
					if(rgba[k] > max)
						max = rgba[k];
				}
				
				double min2 = 256;
				double max2 = -1;
				
				for(int k = 0; k < 3; k++) {
					if(temp[k] < min2)
						min2 = temp[k];
					if(temp[k] > max2)
						max2 = temp[k];
				}
				
				if((max - min) > (max2 - min2) + 10)
					mRgba.put(i, j, new double[]{0,0,255,255});
					
				
//				rgba = hsva.get(i,j);
//				
//				temp = hsvb.get(i,j);		
				
//				Log.i("debug", "rgba: "+rgba[0]+", "+rgba[1]+", "+rgba[2]);
//				Log.i("debug", "temp: "+temp[0]+", "+temp[1]+", "+temp[2]);
//				if(((rgba[0] > (temp[0]+5)) || (rgba[0] < (temp[0]-5))) && (rgba[1] > 120) && (rgba[2] >80)){
//					mRgba.put(i, j, new double[]{0,0,255,255});
//					foundcolors.add(rgba);
//					}
//				
			}
			//Log.i("debug", "Zeile: "+i);
		}
		Highgui.imwrite(new File(mStorageDir, "mRgbaRedPoints.png").toString(), mRgba);
		for(double[] d: foundcolors)
		Log.i("debug","Different Colors: "+d[0]+", "+d[1]+", "+d[2]);
//		mHSV = new Mat();
//		mLowerRangeImage = new Mat();
//		mUpperRangeImage = new Mat();
//		mThreshed = new Mat();
//		Imgproc.cvtColor(mEmptyTrack, mHSV, Imgproc.COLOR_BGR2HSV);	
//		double[] rgba = null;
//		double R, G, B, A;
//		double RCount = 0;
//		double GCount = 0;
//		double BCount = 0;
//		double ACount = 0;
//		double avgR = 0;
//		double avgG = 0;
//		double avgB = 0;
//		double avgA = 0;
//		
//		double divider = 0;
//		for (int i = 0; i < mHSV.rows(); i++) {
//			for (int j = 0; j < mHSV.cols(); j++) {
//				if (rgba != null)
//					rgba = null;
//				rgba = mEmptyTrack.get(i, j);
//				R = rgba[0];
//				G = rgba[1];
//				B = rgba[2];	
//				A = rgba[3];
//				divider++;
//				RCount += R;
//				GCount += G;
//				BCount += B;
//				ACount += A;
//								
//			}
//	
//		}	
//		avgR = RCount /divider;
//		avgG = GCount /divider;
//		avgB = BCount/divider;
//		avgA = ACount/ divider;
//		Log.i("debug", "avg R: "+avgR);
//		Log.i("debug", "avg G: "+avgG);
//		Log.i("debug", "avg B: "+avgB);
//		Log.i("debug", "avg Alpha: "+avgA);	
//
//		
//		Core.inRange(mHSV, new Scalar(0,0,0), new Scalar(avgR-10,1,1), mLowerRangeImage);
//		Core.inRange(mHSV, new Scalar(avgR+10,0,0), new Scalar(255,1,1), mUpperRangeImage);
//		Core.add(mLowerRangeImage, mUpperRangeImage, mThreshed);
//		
//		mTrackColor = new File(mStorageDir, "track.png");
//		mPath = mTrackColor.toString();
//		Boolean bool = Highgui.imwrite(mPath, mThreshed);
//		if (bool)
//			Log.i("debug", "SUCCESS writing track.png to external storage");
//		else
//			Log.i("debug", "Fail writing track.png to external storage");
//		
		
		//Hier muss ein Bild mit den Fahrzeugen auf dem Streckenausschnitt ankommen
		//Dann werden die Positionen gesetzt.
		return mThreshed;
	}
	
	public void draw_lanes(){
		
		
		
		
	}
	
	
	

}
