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
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class RaceFragment extends Fragment implements CvCameraViewListener2{
	
		final public static int PREPARE_RACE = 0;
		final public static int RACE = 1;
		final public static int END_RACE = 2;
		private Context c;		
		private CountDownTimer mCountdown_updater;
		private int mCountdown;		
		private Race race;
		private List<String> countdown = new ArrayList<String>();

		public static CameraBridgeViewBase mOpenCvCameraView;
		
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			c = RaceFragmentActivity.mContext;
			View v = inflater.inflate(R.layout.race, null);
			mOpenCvCameraView = (CameraBridgeViewBase) v
					.findViewById(R.id.camera_stream_race);					
				mOpenCvCameraView.setVisibility(SurfaceView.INVISIBLE);		
			mOpenCvCameraView.setCvCameraViewListener(this);
			Log.i("debug", "setCVCameraViewListener for Race properly");
			final TextView race_countdown = (TextView) v.findViewById(R.id.race_countdown);
			countdown.add("3");
			countdown.add("2");
			countdown.add("1");	
			countdown.add("Los!!!!!");
			mCountdown = 0;
			mCountdown_updater = new CountDownTimer(6000, 1000) {
				
				@Override
				public void onTick(long millisUntilFinished) {
						Log.i("debug", "Size: "+countdown.size()+", Counter: "+mCountdown);
						if(mCountdown >= countdown.size()) {
							onFinish();
							return;
						}
						race_countdown.setTextColor(getResources().getColor(R.color.winning_green));
						race_countdown.setText(countdown.get(mCountdown));						
						mCountdown++;
						
				}
				
				@Override
				public void onFinish() {
					race_countdown.setText(" Fertig!");
					
				}				
			};
			
			
//			mCountdown_updater = new GUIUpdater(new Runnable() {
//				
//				@Override
//				public void run() {
//					
//					if(mCountdown < countdown.size()){
//					race_countdown.setText(countdown.get(mCountdown));
//					mCountdown++;
//					}
//					else
//					{
//						race_countdown.setText(" Fertig!");
//					}
//					
//				}
//			});
//			mCountdown_updater.startUpdates();
			
			//Making Views and Buttons from XML-View accessible via Java Code
			
			Button manual_end_race = (Button) v.findViewById(R.id.manual_end_race);
			
			manual_end_race.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Log.i("debug", "go to end race manually");
					((RaceFragmentActivity) getActivity()).getViewPager().setCurrentItem(END_RACE);
				}
			});
			
				
			return v;

		}
		
		
		@Override
		public void onPause() {
			super.onPause();
			mCountdown_updater.cancel();
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
			
			Log.i("debug", "Race Fragment onResume()");
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
		
		public void startCountdown(){
			mCountdown_updater.start();
		}
		
		
	}
	

