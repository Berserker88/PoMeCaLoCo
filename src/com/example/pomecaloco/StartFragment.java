package com.example.pomecaloco;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

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

public class StartFragment extends Fragment implements CvCameraViewListener2{
	
	

	private Context c;
	public static CameraBridgeViewBase mOpenCvCameraView;
	ObjectDetector mFrame_to_process;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		c = RaceFragmentActivity.mContext;
		View v = inflater.inflate(R.layout.start, null);
		

		mOpenCvCameraView = (CameraBridgeViewBase) v
				.findViewById(R.id.camera_stream_prepare);		
		if((RaceFragment.mOpenCvCameraView == null) ||(RaceFragment.mOpenCvCameraView.getVisibility() != SurfaceView.VISIBLE))
			mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		else
			mOpenCvCameraView.setVisibility(SurfaceView.INVISIBLE);		
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


	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Automatisch generierter Methodenstub
		
	}

	@Override
	public void onCameraViewStopped() {
		// TODO Automatisch generierter Methodenstub
		
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		
		mFrame_to_process = new ObjectDetector(inputFrame);
		
		return mFrame_to_process.draw_colorrange_on_frame(new Scalar(0,0,0,100), new Scalar(100,100,100,255));
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
