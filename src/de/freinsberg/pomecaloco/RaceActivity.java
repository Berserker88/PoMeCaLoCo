package de.freinsberg.pomecaloco;

import java.math.RoundingMode;
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
import android.os.PowerManager;
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
		private boolean mRaceStopped =false;
		private TextView raceview_countdown = null;
		private ImageView faster = null;
		private ImageView slower = null;
		private TextView raceview_time_updater = null;
		private TextView raceview_round_updater_left = null;
		private TextView raceview_round_updater_right = null;
		private TextView raceview_speed_updater_left = null;
		private TextView raceview_speed_updater_right = null;
		private TextView raceview_finished = null;
		private TextView raceview_best_time_updater = null;
		public TextView raceview_game_mode;
		public TextView raceview_track_name;
		private Button manual_end_race = null;
		public MyTimer mRaceTimer;
		public MillisecondChronometer mChronometer;	
		public static CameraBridgeViewBase mOpenCvCameraView;
		protected PowerManager.WakeLock mWakeLock;
		
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
			
			//Keep the screen on in this activity
			 final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "debug");
		        this.mWakeLock.acquire();
		        
		        
			//get arguments from previous Fragment
//			mData = getArguments();
//			
//			mMinLapCount = Integer.parseInt(mData.getString("count"));
//			mMode = Integer.parseInt(mData.getString("mode"));
//			mPlayer = Integer.parseInt(mData.getString("player"));			
//			Log.i("debug", "Players: "+mPlayer+", Mode: "+mMode+", Count: "+mMinLapCount);
			
		
				
			
			//Making Views and Buttons from XML-View accessible via Java Code
			
			raceview_countdown = (TextView) findViewById(R.id.raceview_countdown);
			raceview_finished = (TextView) findViewById(R.id.raceview_finished);
			raceview_game_mode = (TextView) findViewById(R.id.game_mode);
			raceview_track_name = (TextView) findViewById(R.id.track_name);
			faster = (ImageView) findViewById(R.id.faster);
			slower = (ImageView) findViewById(R.id.slower);			
			raceview_time_updater = (TextView) findViewById(R.id.raceview_time_updater);
			raceview_round_updater_left = (TextView) findViewById(R.id.raceview_round_updater_left);
			raceview_round_updater_right = (TextView) findViewById(R.id.raceview_round_updater_right);
			raceview_speed_updater_left = (TextView) findViewById(R.id.raceview_speed_updater_left);
			raceview_speed_updater_right = (TextView) findViewById(R.id.raceview_speed_updater_right);
			raceview_best_time_updater = (TextView) findViewById(R.id.raceview_best_time_updater);			
			manual_end_race = (Button) findViewById(R.id.manual_end_race);			
			
			mCountdown = new MyTimer(4001, 1000, mCountdownValues, raceview_countdown);							
			Log.i("debug", "setting values for race countdown");
			
			//starting the race
			start();
			
			//initializing View depending on the GameMode(TimerMode or RoundMode)
			if(Race.getInstance().getGameMode() == Race.TIMER_MODE){
				raceview_round_updater_left.setTextColor(getResources().getColor(R.color.white));
				raceview_round_updater_left.setText(R.string.raceview_round_text);
				raceview_round_updater_right.setTextColor(getResources().getColor(R.color.white));
				raceview_round_updater_right.setText(R.string.raceview_round_text);
			}else if(Race.getInstance().getGameMode() == Race.ROUND_MODE){
				raceview_round_updater_left.setTextColor(getResources().getColor(R.color.white));
				raceview_round_updater_left.setText(" / "+Race.getInstance().getCount());
				raceview_round_updater_right.setTextColor(getResources().getColor(R.color.white));
				raceview_round_updater_right.setText(" / "+Race.getInstance().getCount());
			}
			//initialize the other Textviews
			raceview_speed_updater_left.setTextColor(getResources().getColor(R.color.white));
			raceview_speed_updater_left.setText(R.string.raceview_speed_text);			
			raceview_speed_updater_right.setTextColor(getResources().getColor(R.color.white));
			raceview_speed_updater_right.setText(R.string.raceview_speed_text);			
			raceview_best_time_updater.setText("");
			raceview_track_name.setText(Race.getInstance().getTrackName());
			raceview_game_mode.setText(Race.getInstance().getNumberOfPlayers()+" Spieler Rennen");		
			
			manual_end_race.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Log.i("debug", "go to end race manually");
					cancel();
					Intent intent = new Intent().setClass(v.getContext(), FinishActivity.class);
					startActivity(intent);
				}
			});							
		
		}				
		@Override
		public void onPause() {
			this.mWakeLock.release();
			
			mCountdown.cancel();
			if (mOpenCvCameraView != null)
				mOpenCvCameraView.disableView();	
			super.onPause();
		}
	
		public void onDestroy() {
			//this.mWakeLock.release();
			if (mOpenCvCameraView != null)
				mOpenCvCameraView.disableView();		
			super.onDestroy();
		
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
				if(s != null)
				{
					boolean recognized = md.colorDetected(s);						
					if(Race.getInstance().getGameMode() == Race.ROUND_MODE)
					{						
						if(!(Race.getInstance().isOver() == Race.LEFT_LANE))
						{						
							if(Race.getInstance().isCorrectMovement(Race.LEFT_LANE, recognized))
							{
								Log.i("debug", "Korrektes Movement links!");
								Race.getInstance().countRounds(Race.LEFT_LANE);							
								updateGUIElements(Race.LEFT_LANE);
							}
						}
						else
						{
							if(mRaceStopped = false)
							{							
								finishGUIElements(Race.LEFT_LANE);								
								stop();
								mRaceStopped =true;
							}
						}
					}
				}
				s = Race.getInstance().getPlayerColor(Race.RIGHT_LANE);
				if(s != null){
					boolean recognized = md.colorDetected(s);			
					if(Race.getInstance().getGameMode() == Race.ROUND_MODE)
					{
						if(!(Race.getInstance().isOver() == Race.RIGHT_LANE))
						{
							if(Race.getInstance().isCorrectMovement(Race.RIGHT_LANE, recognized))
							{
								Log.i("debug", "Korrektes Movement rechts!");
								Race.getInstance().countRounds(Race.RIGHT_LANE);									
								updateGUIElements(Race.RIGHT_LANE);
							}
						}
						else
						{
							if(mRaceStopped = false)
							{							
								finishGUIElements(Race.LEFT_LANE);								
								stop();
								mRaceStopped =true;
							}
						}
					}					
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
			Race.getInstance().startRace(this, raceview_time_updater, raceview_best_time_updater);
		}
		
		public void cancel(){
			mCountdown.stop();
			Race.getInstance().cancel();
		}
		
		public void stop(){
			Race.getInstance().stop();
		}
		
		private void updateGUIElements(final int lane) {
			runOnUiThread(new Runnable() {
			     public void run() {

						if(lane == Race.LEFT_LANE)	
						{
							raceview_round_updater_left.setText(Race.getInstance().getCurrentRound(Race.LEFT_LANE)+" / "+Race.getInstance().getCount());
							raceview_speed_updater_left.setText(String.format("%.2f", Race.getInstance().getCurrentSpeed(Race.LEFT_LANE))+" m/s");
						}
						else
						{
							raceview_round_updater_right.setText(Race.getInstance().getCurrentRound(Race.RIGHT_LANE)+" / "+Race.getInstance().getCount());
							raceview_speed_updater_right.setText(String.format("%.2f", Race.getInstance().getCurrentSpeed(Race.RIGHT_LANE))+" m/s");
						}
			    }
			});
		
			
		}
		private void finishGUIElements(final int lane){
			runOnUiThread(new Runnable() {
			     public void run() {
						if(lane == Race.LEFT_LANE) {							
							raceview_finished.setText(R.string.raceview_left_finished);
							raceview_time_updater.setText(Race.getInstance().getFinishedTime(Race.getInstance().getGameMode()));
						}
						else
						{
							raceview_finished.setText(R.string.raceview_right_finished);
							raceview_time_updater.setText(Race.getInstance().getFinishedTime(Race.getInstance().getGameMode()));
						}
			    }
			});

		}
		


}
	

