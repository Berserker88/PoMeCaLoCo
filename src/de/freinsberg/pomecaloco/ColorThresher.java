package de.freinsberg.pomecaloco;

import java.io.File;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

public class ColorThresher {
	Mat mM;
	Scalar mLeftPlayerColor;
	Scalar mRightPlayerColor;

	public ColorThresher(Mat m, Scalar leftPlayerColor, Scalar rightPlayerColor) {
		mM = m;
		mM = mM.t();		
		Core.flip(mM, mM, 1);	
		mLeftPlayerColor = leftPlayerColor;
		mRightPlayerColor = rightPlayerColor;
	}
	
	private Mat createThreshedImage(){
		Mat matLeftThreshed = new Mat();
		Mat matRightThreshed = new Mat();
		Mat matThreshed = new Mat(mM.size(),mM.type());
		Mat matLeftPlayerColor = new Mat(new Size(1,1),mM.type(),mLeftPlayerColor);
		Mat matRightPlayerColor = new Mat(new Size(1,1),mM.type(),mRightPlayerColor);
		Mat matHsvImage = new Mat();
		double[] doubleLeftPlayerColor;
		double[] doubleRightPlayerColor;
		
		Imgproc.cvtColor(matLeftPlayerColor, matLeftPlayerColor, Imgproc.COLOR_RGB2HSV);
		Imgproc.cvtColor(matRightPlayerColor, matRightPlayerColor, Imgproc.COLOR_RGB2HSV);
		
		doubleLeftPlayerColor = matLeftPlayerColor.get(0,0);
		doubleRightPlayerColor = matRightPlayerColor.get(0,0);
		
		Log.i("testing","Right Player Color: "+doubleRightPlayerColor[0] + ", "+doubleRightPlayerColor[1] + ", "+ doubleRightPlayerColor[2]);
		
		Imgproc.cvtColor(mM, matHsvImage, Imgproc.COLOR_RGB2HSV);		
		
		Core.inRange(matHsvImage, new Scalar(doubleLeftPlayerColor[0] - MovementDetector.HUE_THRESHOLD,100,100), new Scalar(doubleLeftPlayerColor[0] + MovementDetector.HUE_THRESHOLD,255,255), matLeftThreshed);
		Core.inRange(matHsvImage, new Scalar(doubleRightPlayerColor[0] - MovementDetector.HUE_THRESHOLD,100,100), new Scalar(doubleRightPlayerColor[0] + MovementDetector.HUE_THRESHOLD,255,255), matRightThreshed);
		
		File mStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
		Imgproc.cvtColor(matLeftThreshed, matLeftThreshed, Imgproc.COLOR_GRAY2BGR, 0);
		Imgproc.cvtColor(matLeftThreshed, matLeftThreshed, Imgproc.COLOR_BGR2RGBA, 0);
		File bgrFile = new File(mStorageDir, "car in here.bmp");
		String mPath = bgrFile.toString();
		Highgui.imwrite(mPath, matLeftThreshed);
		
		Log.i("testing", ""+matLeftThreshed.type());
		
		matLeftThreshed.copyTo(matThreshed);
		matRightThreshed.copyTo(matThreshed);
		
		matLeftThreshed.release();
		matRightThreshed.release();
		matHsvImage.release();

		return matThreshed;
	}
	
	public Mat getThreshedImage(){
		return createThreshedImage();
	}

}
