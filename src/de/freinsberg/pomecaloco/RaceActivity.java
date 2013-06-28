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
import org.opencv.core.Scalar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
	
		private MovementDetector mMd;
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
		public TextView raceview_game_mode;
		public TextView raceview_track_name;
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
			raceview_game_mode = (TextView) findViewById(R.id.game_mode);
			raceview_track_name = (TextView) findViewById(R.id.track_name);
			faster = (ImageView) findViewById(R.id.faster);
			slower = (ImageView) findViewById(R.id.slower);			
			raceview_time_updater = (TextView) findViewById(R.id.raceview_time_updater);
			raceview_round_updater = (TextView) findViewById(R.id.raceview_round_updater);
			raceview_speed_updater = (TextView) findViewById(R.id.raceview_speed_updater);
			raceview_best_time_updater = (TextView) findViewById(R.id.raceview_best_time_updater);			
			manual_end_race = (Button) findViewById(R.id.manual_end_race);			
			
			mCountdown = new MyTimer(4001, 1000, mCountdownValues, raceview_countdown);							
			Log.i("debug", "setting values for race countdown");
			
			//starting the race
			start();
			
			//initializing View depending on the GameMode(TimerMode or RoundMode)
			if(Race.getInstance().getGameMode() == Race.TIMER_MODE){
				raceview_round_updater.setTextColor(raceview_round_updater.getResources().getColor(R.color.white));
				raceview_round_updater.setText(R.string.raceview_round_text);
			}else if(Race.getInstance().getGameMode() == Race.ROUND_MODE){
				raceview_round_updater.setTextColor(raceview_round_updater.getResources().getColor(R.color.white));
				raceview_round_updater.setText(R.string.raceview_round_text+" "+" / "+Race.getInstance().getCount());
			}
			//initialize the other Textviews
			raceview_speed_updater.setTextColor(raceview_speed_updater.getResources().getColor(R.color.white));
			raceview_speed_updater.setText(R.string.raceview_speed_text);			
			raceview_best_time_updater.setText("");
			raceview_track_name.setText(Race.getInstance().getTrackName());
			raceview_game_mode.setText(Race.getInstance().getNumberOfPlayers()+" Spieler Rennen");		
			
			manual_end_race.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Log.i("debug", "go to end race manually");
					stop();
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
			MovementDetector md;
			Mat m = inputFrame.rgba();
			if(!m.empty()) {				
				md = new MovementDetector(m.clone());
				
				//detecting cars
				Scalar s = Race.getInstance().getPlayerColor(Race.LEFT_LANE);				
				if(s != null){
					if(md.colorDetected(s))
						Log.i("debug", "Hab das linke Fahrzeug!");
				}
				s = Race.getInstance().getPlayerColor(Race.RIGHT_LANE);
				if(s != null){
					if(md.colorDetected(s))
						Log.i("debug", "Hab das rechte Fahrzeug!");
				}				
				md.clear();
			}			
			return m;
		}
		
		@Override
		public void onResume() {
			
			super.onResume();			
			Log.i("debug", "RaceActivity onResume()");
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, this,
					mLoaderCallback);			
		}
		@Override
	    public void onBackPressed() {
			new AlertDialog.Builder(this)
				.setTitle("Beenden?")
				.setMessage("Wollen Sie die App beenden?")
	        	.setCancelable(false)
	        	.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
	        		public void onClick(DialogInterface dialog, int id) {
	        			finish();
	        		}
	        	})
	        	.setNegativeButton("Abbrechen", null)
	        	.show();
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
			Race.getInstance().startRace(this, raceview_time_updater, raceview_speed_updater, raceview_round_updater);
		}
		
		public void stop(){
			mCountdown.stop();
			Race.getInstance().cancel();
		}

}
	

