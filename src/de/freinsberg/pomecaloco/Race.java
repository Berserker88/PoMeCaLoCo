package de.freinsberg.pomecaloco;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import android.text.format.Time;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class Race {
	
	private Mat mPreparedTrack;	
	public static int ROUND_MODE = 1;
	public static int TIMER_MODE = 2;	
	public static int LEFT_LANE = 1;
	public static int RIGHT_LANE = 2;
	private static int mMode;
	private static int mCount;
	public static List<Player> mPlayerArray = new ArrayList<Player>();
	public static MyTimer mRaceTimer;
	private static Race mInstance = new Race();
	
	
	public static Race getInstance(){
		return mInstance;
	};
	
	private Race() {
		
	}
	public void newRace(int count, int mode) {	
		mMode = mode;
		mCount = count;
		Log.i("debug","Neues Rennen:(1=Rundenrennen,2=Zeitrennen)Spielmodus"+mode+" und Counter: "+count+" erstellt");		
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
	public static void startRace(TextView time, TextView speed, TextView round) {
		if(mMode == TIMER_MODE)
		{
			mRaceTimer = new MyTimer(mCount*60000, 10, time);
		}
		else
		{
			
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
	
	
	
	public static void processResults() {
		Log.i("debug", "Processing Results.....");
		//Gibt das Ergebnis an Results-Klasse weiter.
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
