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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This activity is used during the active race and displays information that are calculated during the race.
 * @author freinsberg
 *
 */
public class RaceActivity extends Activity implements CvCameraViewListener2{	
	
		public MyTimer mRaceTimer;
		private static MyTimer mCountdown;
		
		public MillisecondChronometer mChronometer;	
		
		private static CameraBridgeViewBase mOpenCvCameraView;
		
		protected PowerManager.WakeLock mWakeLock;		

		private List<String> mCountdownValues = new ArrayList<String>();
		
		private boolean ichdarf = true;
		
		private Button end_race;

		private View visual_speed_x_axis;
		private View visual_speed_y_axis;

		private TextView raceview_countdown = null;
		private TextView raceview_time_updater = null;
		private TextView raceview_chronometer;
		private TextView raceview_left_name;
		private TextView raceview_right_name;
		private TextView raceview_round_updater_left = null;
		private TextView raceview_round_updater_right = null;
		private TextView raceview_round_time_left = null;
		private TextView raceview_round_time_right = null;
		private TextView raceview_speed_updater_left = null;
		private TextView raceview_speed_updater_right = null;
		private TextView raceview_finished = null;
		private TextView raceview_best_time_updater = null;
		public TextView raceview_game_mode;
		public TextView raceview_track_name;
		
		private ImageView visual_speed_faster = null;
		private ImageView visual_speed_slower = null;
		private ImageView left_car_color;
		private ImageView right_car_color;

		private Bitmap[] mCarColorBitmaps;
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			//filling static array for the race- countdown
			mCountdownValues.add("3");
			mCountdownValues.add("2");
			mCountdownValues.add("1");	
			mCountdownValues.add("Los!!!!!");
			
			setContentView(R.layout.race);
			
		    Race.getInstance().setRaceActivity(this);
			
			mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_stream_race);				
				
			mOpenCvCameraView.setCvCameraViewListener(this);
			Log.i("debug", "setCVCameraViewListener for Race properly");
			
			//Keep the screen on in this activity
			 final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "debug");
		        this.mWakeLock.acquire();
				
			
			//Making Views and Buttons from XML-View accessible via Java Cod
			
			raceview_countdown = (TextView) findViewById(R.id.raceview_countdown);
			raceview_finished = (TextView) findViewById(R.id.raceview_finished);
			raceview_game_mode = (TextView) findViewById(R.id.game_mode);
			raceview_track_name = (TextView) findViewById(R.id.track_name);
			visual_speed_faster = (ImageView) findViewById(R.id.visual_speed_faster);
			visual_speed_slower = (ImageView) findViewById(R.id.visual_speed_slower);
			visual_speed_x_axis = (View) findViewById(R.id.visual_speed_x_axis);
			visual_speed_y_axis = (View) findViewById(R.id.visual_speed_y_axis);
			left_car_color = (ImageView) findViewById(R.id.left_car_color);
			right_car_color = (ImageView) findViewById(R.id.right_car_color);
			raceview_time_updater = (TextView) findViewById(R.id.raceview_time_updater);
			raceview_chronometer = (TextView) findViewById(R.id.raceview_chronometer);
			raceview_left_name = (TextView) findViewById(R.id.raceview_left_name);
			raceview_right_name = (TextView) findViewById(R.id.raceview_right_name);
			raceview_round_updater_left = (TextView) findViewById(R.id.raceview_round_updater_left);
			raceview_round_updater_right = (TextView) findViewById(R.id.raceview_round_updater_right);
			raceview_speed_updater_left = (TextView) findViewById(R.id.raceview_speed_updater_left);
			raceview_speed_updater_right = (TextView) findViewById(R.id.raceview_speed_updater_right);
			raceview_round_time_left =(TextView) findViewById(R.id.raceview_round_time_left);
			raceview_round_time_right = (TextView) findViewById(R.id.raceview_round_time_right);
			raceview_best_time_updater = (TextView) findViewById(R.id.raceview_best_time_updater);			
			end_race = (Button) findViewById(R.id.end_race);		
			raceview_best_time_updater.setText("");
			raceview_track_name.setText(Race.getInstance().getTrackName());
			raceview_game_mode.setText(Race.getInstance().getNumberOfPlayers()+" Spieler Rennen");	
				
			mCountdown = new MyTimer(4001, 1000, mCountdownValues, raceview_countdown);				
			
			//starting the race
			start();
			
			switch(ObjectDetector.getInstance().car_status()){
			case ObjectDetector.BOTH_CAR:
				disableVisualSpeedUpdater();				
				raceview_left_name.setText(Race.getInstance().getPlayerName(0));
				raceview_right_name.setText(Race.getInstance().getPlayerName(1));
				raceview_speed_updater_left.setTextColor(getResources().getColor(R.color.white));
				raceview_speed_updater_left.setText(R.string.raceview_speed_text);			
				raceview_speed_updater_right.setTextColor(getResources().getColor(R.color.white));
				raceview_speed_updater_right.setText(R.string.raceview_speed_text);	
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
				mCarColorBitmaps = ObjectDetector.getInstance().get_cars_colors();			
				left_car_color.setImageBitmap(mCarColorBitmaps[0]);					
				right_car_color.setImageBitmap(mCarColorBitmaps[1]);					
				left_car_color.setVisibility(View.VISIBLE);								
				right_car_color.setVisibility(View.VISIBLE);	
				break;
				
			case ObjectDetector.LEFT_CAR:			
				if(Race.getInstance().mGhostMode)
				{
					raceview_right_name.setText(R.string.raceview_ghost_name);
					raceview_speed_updater_right.setTextColor(getResources().getColor(R.color.white));
					raceview_speed_updater_right.setText(R.string.raceview_speed_text);	
				}
				else
				{
					raceview_right_name.setVisibility(View.INVISIBLE);
					raceview_speed_updater_right.setVisibility(View.INVISIBLE);
				}
				
				raceview_left_name.setText(Race.getInstance().getPlayerName(0));				
				raceview_speed_updater_left.setTextColor(getResources().getColor(R.color.white));
				raceview_speed_updater_left.setText(R.string.raceview_speed_text);		
				
				//initializing View depending on the GameMode(TimerMode or RoundMode)
				if(Race.getInstance().getGameMode() == Race.TIMER_MODE){
					raceview_round_updater_left.setTextColor(getResources().getColor(R.color.white));
					raceview_round_updater_left.setText(R.string.raceview_round_text);
					
					if(Race.getInstance().mGhostMode)
					{
						raceview_round_updater_right.setTextColor(getResources().getColor(R.color.white));
						raceview_round_updater_right.setText(R.string.raceview_round_text);
					}
				}
				else if(Race.getInstance().getGameMode() == Race.ROUND_MODE)
				{
					raceview_round_updater_left.setTextColor(getResources().getColor(R.color.white));
					raceview_round_updater_left.setText(" / "+Race.getInstance().getCount());
					
					if(Race.getInstance().mGhostMode)
					{
						raceview_round_updater_right.setTextColor(getResources().getColor(R.color.white));
						raceview_round_updater_right.setText(" / "+Race.getInstance().getCount());						
					}
				}
				mCarColorBitmaps = ObjectDetector.getInstance().get_cars_colors();			
				left_car_color.setImageBitmap(mCarColorBitmaps[0]);				
				left_car_color.setVisibility(View.VISIBLE);
				break;
				
			case ObjectDetector.RIGHT_CAR:		
				if(Race.getInstance().mGhostMode){
					raceview_left_name.setText(R.string.raceview_ghost_name);
					raceview_speed_updater_left.setTextColor(getResources().getColor(R.color.white));
					raceview_speed_updater_left.setText(R.string.raceview_speed_text);	
				}
				else
				{					
					raceview_left_name.setVisibility(View.INVISIBLE);
					raceview_speed_updater_left.setVisibility(View.INVISIBLE);					
				}
				raceview_right_name.setText(Race.getInstance().getPlayerName(0));	
				
				raceview_speed_updater_right.setTextColor(getResources().getColor(R.color.white));
				raceview_speed_updater_right.setText(R.string.raceview_speed_text);	
				//initializing View depending on the GameMode(TimerMode or RoundMode)
				if(Race.getInstance().getGameMode() == Race.TIMER_MODE){
					raceview_round_updater_right.setTextColor(getResources().getColor(R.color.white));
					raceview_round_updater_right.setText(R.string.raceview_round_text);
					
					if(Race.getInstance().mGhostMode)
					{
						raceview_round_updater_right.setTextColor(getResources().getColor(R.color.white));
						raceview_round_updater_right.setText(R.string.raceview_round_text);
					}
				}
				else if(Race.getInstance().getGameMode() == Race.ROUND_MODE)
				{
					raceview_round_updater_right.setTextColor(getResources().getColor(R.color.white));
					raceview_round_updater_right.setText(" / "+Race.getInstance().getCount());
					
					if(Race.getInstance().mGhostMode)
					{
						raceview_round_updater_left.setTextColor(getResources().getColor(R.color.white));
						raceview_round_updater_left.setText(" / "+Race.getInstance().getCount());						
					}				
				}
				
				mCarColorBitmaps = ObjectDetector.getInstance().get_cars_colors();					
				right_car_color.setImageBitmap(mCarColorBitmaps[1]);									
				right_car_color.setVisibility(View.VISIBLE);
				break;
				
			default:
				Log.i("debug","No Cars on Lane");
				break;
			}			
			
			end_race.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Log.i("debug", "go to end race manually");
					//cancel();
					Intent intent = new Intent().setClass(v.getContext(), FinishActivity.class);
					startActivity(intent);
					finish();
				}
			});							
		
		}				
		
		@Override		
		public void onPause() {
			this.mWakeLock.release();
			
			cancel();
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
		public void onCameraViewStarted(int width, int height) {}
	
		@Override
		public void onCameraViewStopped() {}
	
		@Override
		public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
			final Mat m = inputFrame.rgba();			
			final Scalar sLeftPlayerColor = Race.getInstance().getPlayerColor(Race.LEFT_LANE);
			final Scalar sRightPlayerColor = Race.getInstance().getPlayerColor(Race.RIGHT_LANE);
			
			//testing
			//final ColorThresher threshedFrame = new ColorThresher(m, sLeftPlayerColor, sRightPlayerColor);
			//testing
			
			if(Race.getInstance().hasRaceBeenStarted())
			{
				Thread thr = new Thread(new Runnable() {				
					@Override
					public void run() {
						MovementDetector md;
							if(!m.empty()) {				
								md = new MovementDetector(m.clone());				
								//detecting cars											
								if(sLeftPlayerColor != null)
								{
									boolean recognized = md.detectColor(sLeftPlayerColor);	
									countMovement(recognized, Race.LEFT_LANE);
								}								
								if(sRightPlayerColor != null)
								{
									boolean recognized = md.detectColor(sRightPlayerColor);			
									countMovement(recognized, Race.RIGHT_LANE);
								}				
								md.clear();			
							}	
					}
				});
				thr.start();
			}
			//return threshedFrame.getThreshedImage();
			return m;
		}
		
		@Override
		public void onResume() {
			this.mWakeLock.acquire();
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
	        			cancel();
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
		

		/**
		 * Start the race countdown and calls init race to set the noe active context of the raceactity to the race class.
		 */
		public void start(){			
			mCountdown.start();			
			Race.getInstance().initRace(this, raceview_time_updater);
		}
		
		/**
		 * This stops the race countdown and calls the method cancel() of the race class.
		 */
		public void cancel(){
			mCountdown.stop();
			Race.getInstance().cancel();
		}		
		
		private void countMovement(boolean recognized, int lane){
			int isOver = Race.getInstance().isOver();
			if((isOver != lane) && (isOver != Race.GHOST_LANE))
			{							
				
				if(Race.getInstance().isCorrectMovement(lane, recognized))
				{
					Log.i("debug", "Korrektes Movement "+lane);
					Race.getInstance().updateRoundsAndUI(lane);					
				}
			}
			else
			{	
				while(!ichdarf){
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {						
						e.printStackTrace();
					}					
				}
				ichdarf = false;
				if(Race.getInstance().hasRaceBeenStarted()) //This is done to tell the Thread that he don't have to do this if a thread before has done this.
				{
					if(isOver == Race.GHOST_LANE)
						lane = Race.GHOST_LANE;								
					Race.getInstance().endRaceAndUpdateUI(lane);
					Log.i("debug", "Race is Over for LANE: " + lane);
					Log.i("debug", "Race stopped properly");
				}
				ichdarf = true;
				
				
			}		
		}
		
		/**
		 * This method is used to update the UI- Elements.
		 * The updateprocedere for the user interface runs on the UI Thread to make this work out of the class onCreate(). 
		 * @param lane The lane for which the ui- Updates are needed.
		 */
		public void updateGUIElements(final int lane) {
			if(lane == Race.GHOST_LANE){		
				MyShotTask st = new MyShotTask(this, R.id.raceview_ghost_overlay);
				st.perform();
			}
			
			runOnUiThread(new Runnable() {
			     public void run() {
			    	 if(Race.getInstance().getBestTime() != null)
			    	 {
			    	 	raceview_best_time_updater.setText(Race.getInstance().getBestTime().getL());	
			    	 	if(Race.getInstance().getBestTime().getR() == Race.getInstance().getUsedLane() || Race.getInstance().getNumberOfPlayers() == 2)
			    	 	{
			    	 		raceview_best_time_updater.setTextColor(Race.getInstance().getPlayerRGBColor(Race.getInstance().getBestTime().getR()));
			    	 	}
			    	 	else			    	 		
			    	 		raceview_best_time_updater.setTextColor(getResources().getColor(R.color.dummy_gray));			    	 		
			    	 }
			    	 
			    	 final float scale = getBaseContext().getResources().getDisplayMetrics().density;
			    	 final Pair<Integer,Integer> pair_visual_speed = Race.getInstance().getVisualSpeedValue(scale);			    	 
			    	 if(pair_visual_speed != null){
			    		 if(pair_visual_speed.getR() == Race.SLOWER){
			    			 Log.i("debug", "Pixels slower: " + pair_visual_speed.getL());
			    			 visual_speed_slower.getLayoutParams().width = pair_visual_speed.getL();
			    			 visual_speed_faster.getLayoutParams().width = 0;
			    		 }
			    		 else if(pair_visual_speed.getR() == Race.FASTER)
			    		 {
			    			 Log.i("debug", "Pixels faster: " + pair_visual_speed.getL());
			    			 visual_speed_faster.getLayoutParams().width = pair_visual_speed.getL();
			    			 visual_speed_slower.getLayoutParams().width = 0;
			    		 }
			    		 else
			    			 Log.i("debug", "Pixels zero on both: ");
			    	 }					
			    	 	if(Race.getInstance().getGameMode() == Race.ROUND_MODE)
			    	 	{
							if(lane == Race.LEFT_LANE)	
							{
								raceview_round_updater_left.setText(Race.getInstance().getCurrentRound(Race.LEFT_LANE)+" /"+Race.getInstance().getCount());								
								raceview_round_time_left.setText(Race.getInstance().getActRoundTime(Race.LEFT_LANE)+"");
								raceview_speed_updater_left.setText(String.format("%.2f", Race.getInstance().getCurrentSpeed(Race.LEFT_LANE))+" m/s");
							}
							else if (lane == Race.RIGHT_LANE)
							{
								raceview_round_updater_right.setText(Race.getInstance().getCurrentRound(Race.RIGHT_LANE)+" /"+Race.getInstance().getCount());
								raceview_round_time_right.setText(Race.getInstance().getActRoundTime(Race.RIGHT_LANE)+"");
								raceview_speed_updater_right.setText(String.format("%.2f", Race.getInstance().getCurrentSpeed(Race.RIGHT_LANE))+" m/s");
							}
							else if (lane == Race.GHOST_LANE)
							{
								double speed;
								if(Race.getInstance().getUsedLane() == Race.LEFT_LANE)
								{
									raceview_round_updater_right.setText(Race.getInstance().getCurrentRound(Race.GHOST_LANE)+" /"+Race.getInstance().getCount());
									raceview_round_time_right.setText(Race.getInstance().getActRoundTime(Race.GHOST_LANE)+"");
									if((speed = Race.getInstance().getGhostSpeed()) != 0)
										raceview_speed_updater_right.setText(String.format("%.2f", speed)+" m/s");
									
								}
								else if(Race.getInstance().getUsedLane() == Race.RIGHT_LANE)
								{
									raceview_round_updater_left.setText(Race.getInstance().getCurrentRound(Race.GHOST_LANE)+" /"+Race.getInstance().getCount());
									raceview_round_time_left.setText(Race.getInstance().getActRoundTime(Race.GHOST_LANE)+"");
									if((speed = Race.getInstance().getGhostSpeed()) != 0)
										raceview_speed_updater_left.setText(String.format("%.2f", speed)+" m/s");
								}
							}
			    	 	}
			    	 	else if(Race.getInstance().getGameMode() == Race.TIMER_MODE)
			    	 	{
							if(lane == Race.LEFT_LANE)	
							{
								raceview_round_updater_left.setText(Race.getInstance().getCurrentRound(Race.LEFT_LANE)+".te");
								raceview_round_time_left.setText(Race.getInstance().getActRoundTime(Race.LEFT_LANE)+"");
								raceview_speed_updater_left.setText(String.format("%.2f", Race.getInstance().getCurrentSpeed(Race.LEFT_LANE))+" m/s");
							}
							else if (lane == Race.RIGHT_LANE)
							{
								raceview_round_updater_right.setText(Race.getInstance().getCurrentRound(Race.RIGHT_LANE)+".te");
								raceview_round_time_right.setText(Race.getInstance().getActRoundTime(Race.RIGHT_LANE)+"");
								raceview_speed_updater_right.setText(String.format("%.2f", Race.getInstance().getCurrentSpeed(Race.RIGHT_LANE))+" m/s");
							}
							else if (lane == Race.GHOST_LANE)
							{
								double speed;
								if(Race.getInstance().getUsedLane() == Race.LEFT_LANE)
								{
									raceview_round_updater_right.setText(Race.getInstance().getCurrentRound(Race.GHOST_LANE)+".te");
									raceview_round_time_right.setText(Race.getInstance().getActRoundTime(Race.GHOST_LANE)+"");
									if((speed = Race.getInstance().getGhostSpeed()) != 0)
										raceview_speed_updater_right.setText(String.format("%.2f", speed)+" m/s");
								}
								else if(Race.getInstance().getUsedLane() == Race.RIGHT_LANE)
								{
									raceview_round_updater_left.setText("Runde "+Race.getInstance().getCurrentRound(Race.GHOST_LANE));
									raceview_round_time_left.setText(Race.getInstance().getActRoundTime(Race.GHOST_LANE)+"");
									if((speed = Race.getInstance().getGhostSpeed()) != 0)
										raceview_speed_updater_left.setText(String.format("%.2f", speed)+" m/s");
								}
							}
			    	 	}
			    }
			});
		
			
		}
		/**
		 * This method should be called when the race has ended properly.
		 * The finishing of the user interface runs on the UI Thread to make this work out of the class onCreate(). 
		 * @param lane The lane for which the ui- finishing is needed.
		 */
		public void finishGUIElements(final int lane){
			runOnUiThread(new Runnable() {
			     public void run() {
			    	 if(Race.getInstance().getGameMode() == Race.ROUND_MODE)
			    	 {			    	 
						if(lane == Race.LEFT_LANE) {						
							raceview_finished.bringToFront();
							raceview_finished.setText(R.string.raceview_left_finished);
							raceview_chronometer.setText(Race.getInstance().getFinishedTime());
							end_race.setVisibility(View.VISIBLE);
						}
						else if(lane == Race.GHOST_LANE){
							raceview_finished.bringToFront();
							raceview_finished.setText(R.string.raceview_ghost_finished);
							raceview_chronometer.setText(Race.getInstance().getRoundGhostFinishedTime());
							end_race.setVisibility(View.VISIBLE);
							
						}
						else
						{
							raceview_finished.bringToFront();
							raceview_finished.setText(R.string.raceview_right_finished);
							raceview_chronometer.setText(Race.getInstance().getFinishedTime());
							end_race.setVisibility(View.VISIBLE);
						}
			    	 }
			    	 else
			    	 {			    	 
							if(lane == Race.LEFT_LANE) {						
								raceview_finished.bringToFront();
								raceview_finished.setText(R.string.raceview_left_finished);
								raceview_time_updater.setText(Race.getInstance().getFinishedTime());
								end_race.setVisibility(View.VISIBLE);
							}
							else if(lane == Race.GHOST_LANE){
								raceview_finished.bringToFront();
								raceview_finished.setText(R.string.raceview_ghost_finished);
								raceview_time_updater.setText(Race.getInstance().getRoundGhostFinishedTime());
								end_race.setVisibility(View.VISIBLE);
								
							}
							else
							{
								raceview_finished.bringToFront();
								raceview_finished.setText(R.string.raceview_right_finished);
								raceview_time_updater.setText(Race.getInstance().getFinishedTime());
								end_race.setVisibility(View.VISIBLE);
							}
				    	 }
			    }
			});

		}
		
		/**
		 * This method is called to disable the coordination-axis that show distance to best actual round time in 2 player mode.
		 */
		public void disableVisualSpeedUpdater(){
			
			visual_speed_slower.setVisibility(View.INVISIBLE);
			visual_speed_faster.setVisibility(View.INVISIBLE);
			visual_speed_x_axis.setVisibility(View.INVISIBLE);
			visual_speed_y_axis.setVisibility(View.INVISIBLE);		
		}
	}
