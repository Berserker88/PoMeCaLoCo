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
import android.widget.Button;
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
		
		
		//Adding Layout Buttons to this onCreate to interact with them (fetching and setting data)
		Button one_lap = (Button) v.findViewById(R.id.one_lap);
		Button five_laps = (Button) v.findViewById(R.id.five_laps);
		Button ten_laps = (Button) v.findViewById(R.id.ten_laps);
		Button one_min = (Button) v.findViewById(R.id.one_min);
		Button five_mins = (Button) v.findViewById(R.id.five_mins);
		Button ten_mins = (Button) v.findViewById(R.id.ten_mins);		
		Button results = (Button) v.findViewById(R.id.results);		
		final Button scanner = (Button) v.findViewById(R.id.scanner);
		//Set Scanner- Button Text for first Start
		scanner.setText(R.string.scan_track);
		final Button rescan = (Button) v.findViewById(R.id.rescan);
		rescan.setVisibility(View.GONE);
		
		scanner.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(scanner.getText() == getString(R.string.scan_track)){
					Log.i("debug", "Track scanned!");					
					scanner.setText(R.string.scan_cars);
				}else if(scanner.getText() == getString(R.string.scan_cars)){
					Log.i("debug", "Cars scanned!");
					scanner.setEnabled(false);
					scanner.setText(R.string.complete);
					Log.i("debug", "Scanning COMPLETED!");
					rescan.setVisibility(View.VISIBLE);
				}				
			}
		});
		rescan.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				rescan.setVisibility(View.GONE);
				scanner.setEnabled(true);
				scanner.setText(R.string.scan_track);
				
				
			}
			
		});
		
			
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
