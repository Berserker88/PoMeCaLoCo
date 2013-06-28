package de.freinsberg.pomecaloco;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import android.content.Context;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

public class Race {
	
	private Mat mPreparedTrack;	
	public static int ROUND_MODE = 1;
	public static int TIMER_MODE = 2;	
	public static int LEFT_LANE = 1;
	public static int RIGHT_LANE = 2;
	private int mMode;
	private int mCount;
	private String mTrack;
	private int mNumberOfPlayers;
	public List<Player> mPlayerArray = new ArrayList<Player>();
	public MyTimer mRaceTimer;
	public MillisecondChronometer mChronometer;
	private static Race mInstance = new Race();
	
	
	public static Race getInstance(){
		return mInstance;
	};
	
	private Race() {
		
	}
	public void newRace(int count, int mode, String track, int numberofplayers) {	
		mNumberOfPlayers = numberofplayers;
		mMode = mode;
		mCount = count;
		mTrack = track;
		Log.i("debug","Neues Rennen:(1=Rundenrennen,2=Zeitrennen)Spielmodus"+mMode+", Counter: "+mCount+", Track: "+mTrack+", NumberOfPlayers: "+mNumberOfPlayers+" erstellt");		
	}

	
	public void createPlayer(int carStatus, int mode, int count) {
		switch(carStatus){
		case ObjectDetector.NO_CAR:			
			break;
		case ObjectDetector.RIGHT_CAR:			
			mPlayerArray.add(new Player(StartActivity.RIGHT_LANE,mode, ObjectDetector.getInstance().getCarColor(RIGHT_LANE)));
			break;
		case ObjectDetector.LEFT_CAR:							
			mPlayerArray.add(new Player(StartActivity.LEFT_LANE,mode, ObjectDetector.getInstance().getCarColor(LEFT_LANE)));
			break;
		case ObjectDetector.BOTH_CAR:
			mPlayerArray.add(new Player(StartActivity.RIGHT_LANE,mode, ObjectDetector.getInstance().getCarColor(RIGHT_LANE)));
			mPlayerArray.add(new Player(StartActivity.LEFT_LANE,mode, ObjectDetector.getInstance().getCarColor(LEFT_LANE)));
			break;	
		default:
			break;		
		}
	}
	
	public void go() {
		if(mMode == TIMER_MODE)
		{
			if(mRaceTimer != null)
				mRaceTimer.start();
		}else{
			if(mChronometer != null)
				mChronometer.start();
		}
	}
	public void startRace(Context c, TextView time, TextView speed, TextView round) {
		if(mMode == TIMER_MODE)
		{
			mRaceTimer = new MyTimer(mCount*60000, 10, time);
		}
		else
		{
			mChronometer = ((MillisecondChronometer)((RaceActivity) c).findViewById(R.id.raceview_chronometer));
			((RaceActivity) c).findViewById(R.id.raceview_time_updater).setVisibility(View.GONE);
			((RaceActivity) c).findViewById(R.id.raceview_chronometer).setVisibility(View.VISIBLE);
			
			
			//mRaceTimer = new MyTimer(Long.MAX_VALUE, 10, time);
		}
		
		
		
		
		
		//TODO Start Race
		//TODO Rennen beginnt
		//Timer läuft
		//Es muss bei jeder Fahrzeugerkennung ein Zähler hochgezählt werden
		// Bei Ende processResults()
		
	}
	
	public double getCurrentSpeed(Time time, int meters){		
		
		double speed = 0;
		
		return speed;
		
	}
	
	public int getCurrentRound() {
		int round = 1;
		
		return round;		
	}
	
	public String getTrackName() {
		return mTrack;
	}
	
	public int getGameMode() {
		return mMode;
	}
	
	public int getNumberOfPlayers() {
		return mNumberOfPlayers;
	}
	
	public int getCount() {
		return mCount;
	}
	
	
	
	public static void processResults() {
		Log.i("debug", "Processing Results.....");
		//Gibt das Ergebnis an Results-Klasse weiter.
	}
	
	public void cancel() {
		if(mMode == TIMER_MODE){
			mRaceTimer.stop();
			Log.i("debug", "Race cancelled! at:"+mRaceTimer.getCurrentTime());
		}
		else
		{
			mChronometer.stop();
			Log.i("debug", "Race cancelled at:"+mChronometer.getTimeElapsed());
		}
		
		
	}
	/**
	 * 
	 * @param lane
	 * @return returns null when mPlayerArray is empty for this lane, else the color of the player according to the lane.
	 */
	public Scalar getPlayerColor(int lane){
		
		for(Player p : mPlayerArray) {
			if(p.getLane() == lane)
				return p.getColor();
		}		
		return null;
	}
	
	
//	public void prepareRace(boolean twoplayer, int mode){
//		
//		if(twoplayer){
//			new Player(LEFT_LANE, mode, Color_Car1);
//			new Player(RIGHT_LANE, mode, Color_Car2);
//		}else{
//			new Player(LEFT_LANE, mode, Color_Car1);
//		}
//		//Hier wird angegeben wie viele Spieler das Rennen hat,außerdem welcher Spielmodus gewählt wurde.
//		//
//				
//	}

}
