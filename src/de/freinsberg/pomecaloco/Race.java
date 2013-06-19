package de.freinsberg.pomecaloco;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import android.util.Log;

public class Race {
	
	Mat mPreparedTrack;
	
	public static final int ROUND_MODE = 1;
	public static final int TIMER_MODE = 2;	
	final int LEFT_LANE = 1;
	final int RIGHT_LANE = 2;
	private int mMode;
	private int mCount;
	Scalar Color_Car1 = new Scalar(255,0,0,255); //Statische Farbe festgelegt
	Scalar Color_Car2 = new Scalar(0,0,255,255); //Statische Farbe festgelegt
	public Race(int count, int mode) {	
		mMode = mode;
		mCount = count;
		Log.i("debug","Neues Rennen:(1=Rundenrennen,2=Zeitrennen)Spielmodus"+mode+" und Counter: "+count+" erstellt");
	}

	
	
	
	public void prepareRace(boolean twoplayer, int mode){
		
		if(twoplayer){
			new Player(LEFT_LANE, mode, Color_Car1);
			new Player(RIGHT_LANE, mode, Color_Car2);
		}else{
			new Player(LEFT_LANE, mode, Color_Car1);
		}
		//Hier wird angegeben wie viele Spieler das Rennen hat,außerdem welcher Spielmodus gewählt wurde.
		//
				
	}
	public void startRace() {
		
		//Rennen beginnt
		//Timer läuft
		//Es muss bei jeder Fahrzeugerkennung ein Zähler hochgezählt werden
		// Bei Ende processResults()
		
	}
	
	public void processResults() {
		//Gibt das Ergebnis an Results-Klasse weiter.
	}
	
	

	



}
