package com.example.pomecaloco;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import android.widget.Spinner;

public class Race extends Fragment implements CvCameraViewListener2 {
	private Context c;
	private CameraBridgeViewBase mOpenCvCameraView;
	Mat mCurrentFrame, mColoredFrame, mColoredResult, mFilteredFrame;
	Scalar lowerColorLimit = new Scalar(0,200,50);
	Scalar upperColorLimit =  new Scalar(150,255,200);
	
	Mat mEdges, mRgba, mHSV;
	
	
	public Race() {
		c = MainActivity.mContext;
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.race, null);

		mOpenCvCameraView = (CameraBridgeViewBase) v
				.findViewById(R.id.camera_stream);		
		
			mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);		
		mOpenCvCameraView.setCvCameraViewListener(this);
		Log.i("debug", "setCVCameraViewListener properly");	

		Spinner set_track = (Spinner) v.findViewById(R.id.choose_track);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(c,
				R.array.tracks, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		set_track.setAdapter(adapter);
		return v;

	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
			
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
			
	}

	public void onCameraViewStarted(int width, int height) {
		

	}

	public void onCameraViewStopped() {
	}

	
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
				
	//Log.i("debug", "onCameraFrame");
	mRgba = inputFrame.rgba();
	Mat mThreshed = new Mat(mRgba.size(),mRgba.type());

	//Log.i("debug",Integer.toString(mRgba.cols()));
	//Core.line(mRgba, new Point(10,10), new Point(200,200), new Scalar(255, 0, 0, 255));
	mEdges = new Mat(mRgba.size(),mRgba.type());
	mHSV = new Mat(mRgba.size(),mRgba.type());
	
	Imgproc.Canny(mRgba, mEdges, 50, 200);
	Imgproc.cvtColor(mRgba, mHSV, Imgproc.COLOR_BGR2HSV);
	
	Core.inRange(mHSV, new Scalar(0,0,0,100), new Scalar(0,0,0 ,255), mThreshed);
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

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, c,
				mLoaderCallback);
		
	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(c) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i("debug", "OpenCV loaded successfully");
				
				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};
}
