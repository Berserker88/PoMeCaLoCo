package de.freinsberg.pomecaloco;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Mat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class RaceActivity extends Activity implements CvCameraViewListener2{
	

		private Context mContext;		
		private static MyTimer mCountdown;
		private Bundle mData = null;
		private List<String> mCountdownValues = new ArrayList<String>();
		private int mMinLapCount;
		private int mMode;
		private int mPlayer;
		private TextView raceview_countdown = null;
		private ImageView faster = null;
		private ImageView slower = null;
		private TextView raceview_time_updater = null;
		private TextView raceview_round_updater = null;
		private TextView raceview_speed_updater = null;
		private TextView raceview_best_time_updater = null;
		private Button manual_end_race = null;
		public static CameraBridgeViewBase mOpenCvCameraView;
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			//filling static array for the race- countdown
			mCountdownValues.add("3");
			mCountdownValues.add("2");
			mCountdownValues.add("1");	
			mCountdownValues.add("Los!!!!!");
			
			setContentView(R.layout.race);
			
			mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_stream_race);				
				
			mOpenCvCameraView.setCvCameraViewListener(this);
			Log.i("debug", "setCVCameraViewListener for Race properly");
			
			//get arguments from previous Fragment
//			mData = getArguments();
//			
//			mMinLapCount = Integer.parseInt(mData.getString("count"));
//			mMode = Integer.parseInt(mData.getString("mode"));
//			mPlayer = Integer.parseInt(mData.getString("player"));			
//			Log.i("debug", "Players: "+mPlayer+", Mode: "+mMode+", Count: "+mMinLapCount);
			
		
				
			
			//Making Views and Buttons from XML-View accessible via Java Code
			
			raceview_countdown = (TextView) findViewById(R.id.raceview_countdown);			
			faster = (ImageView) findViewById(R.id.faster);
			slower = (ImageView) findViewById(R.id.slower);			
			raceview_time_updater = (TextView) findViewById(R.id.raceview_time_updater);
			raceview_round_updater = (TextView) findViewById(R.id.raceview_round_updater);
			raceview_speed_updater = (TextView) findViewById(R.id.raceview_speed_updater);
			raceview_best_time_updater = (TextView) findViewById(R.id.raceview_best_time_updater);			
			manual_end_race = (Button) findViewById(R.id.manual_end_race);			
			
			mCountdown = new MyTimer(5000, 1000, mCountdownValues, raceview_countdown);							
			Log.i("debug", "setting values for race countdown");
			
			//starting the race
			start();
			
			
			
			manual_end_race.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Log.i("debug", "go to end race manually");
					Intent intent = new Intent().setClass(v.getContext(), FinishActivity.class);
					startActivity(intent);
				}
			});							
		
		}				
		@Override
		public void onPause() {
			super.onPause();
			mCountdown.cancel();
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
			return inputFrame.rgba();
		}
		
		@Override
		public void onResume() {
			
			super.onResume();			
			Log.i("debug", "RaceActivity onResume()");
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, this,
					mLoaderCallback);			
		}
	
		private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
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
		
		public void start(){
			mCountdown.start();
			Race.startRace(raceview_time_updater, raceview_speed_updater, raceview_round_updater);
		}

}
	
