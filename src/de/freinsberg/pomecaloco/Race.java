package de.freinsberg.pomecaloco;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class Race {
	
	Mat mPreparedTrack;
	
	
	
	final int LEFT_LANE = 1;
	final int RIGHT_LANE = 2;
	Scalar Color_Car1 = new Scalar(255,0,0,255); //Statische Farbe festgelegt
	Scalar Color_Car2 = new Scalar(0,0,255,255); //Statische Farbe festgelegt
	public Race() {	
		
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
