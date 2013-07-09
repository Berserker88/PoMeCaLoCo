package de.freinsberg.pomecaloco;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import android.content.Context;
import android.graphics.Color;
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
	private boolean mLeftMovement = false;
	private boolean mRightMovement = false;
	private boolean mRaceStarted = false;
	private boolean mTimeIsUp = false;
	private int mMode;
	private int mCount;
	private int mActLeftRound;
	private int mActRightRound;
	private double mActLeftSpeed;
	private double mActRightSpeed;
	private double mOldBestTime;
	private String mActLeftRoundTime;		
	private String mActRightRoundTime;
	private String mBestTime;
	private String mOldTimeLeft;
	private String mOldTimeRight;
	private Track mTrack;
	private double mLength;
	private int mNumberOfPlayers;
	private Pair<String,Integer> mBestTimeOnLane;
	public List<Player> mPlayerArray = new ArrayList<Player>();
	public MyTimer mRaceTimer;
	public MillisecondChronometer mChronometer;	
	private TextView mTimer;
	private TextView mSpeedUpdater;
	private TextView mBestTimeUpdater;
	private List<String> mLeftTimes = new ArrayList<String>();
	private List<String> mRightTimes = new ArrayList<String>();
	
	private static Race mInstance = new Race();
	
	
	public static Race getInstance(){
		return mInstance;
	};
	
	private Race() {
		
	}
	public void newRace(int count, int mode, Track track, int numberofplayers) {	
		mNumberOfPlayers = numberofplayers;
		mPlayerArray.clear();
		mMode = mode;
		mCount = count;
		mTrack = track;
		Log.i("debug","Neues Rennen:(1=Rundenrennen,2=Zeitrennen)Spielmodus"+mMode+", Counter: "+mCount+", Track: "+mTrack+", NumberOfPlayers: "+mNumberOfPlayers+" erstellt");		
	}

	
	public void createPlayer(int carStatus, int mode, int count, List<String> names) {
		switch(carStatus){
		case ObjectDetector.NO_CAR:			
			break;
		case ObjectDetector.RIGHT_CAR:			
			mPlayerArray.add(new Player(names.get(0), RIGHT_LANE,mode, ObjectDetector.getInstance().getCarColor(RIGHT_LANE)));
			break;
		case ObjectDetector.LEFT_CAR:							
			mPlayerArray.add(new Player(names.get(0), LEFT_LANE,mode, ObjectDetector.getInstance().getCarColor(LEFT_LANE)));
			break;
		case ObjectDetector.BOTH_CAR:
			mPlayerArray.add(new Player(names.get(0), LEFT_LANE,mode, ObjectDetector.getInstance().getCarColor(LEFT_LANE)));
			mPlayerArray.add(new Player(names.get(1), RIGHT_LANE,mode, ObjectDetector.getInstance().getCarColor(RIGHT_LANE)));
			
			break;	
		default:
			break;		
		}
	}	

	public void initRace(Context c, TextView time, TextView besttime) {		
		if(!mLeftTimes.isEmpty())
			mLeftTimes.clear();
		if(!mRightTimes.isEmpty())
			mRightTimes.clear();
		mOldBestTime = Double.MAX_VALUE;
		mRaceStarted = false;		
		mTimer = time;
		mActLeftRound = 0;
		mActRightRound = 0;
		mActLeftRoundTime = "00:00:00";
		mActRightRoundTime = "00:00:00";
		mLength = mTrack.getLength();
		if(mMode == TIMER_MODE)
		{
			mRaceTimer = new MyTimer(mCount*60000, 10, mTimer);
			mOldTimeLeft = mRaceTimer.getCurrentTime();
			mOldTimeRight = mRaceTimer.getCurrentTime();
		}
		else
		{
			mChronometer = ((MillisecondChronometer)((RaceActivity) c).findViewById(R.id.raceview_chronometer));
			mOldTimeLeft = "00:00:00";
			mOldTimeRight = "00:00:00";
			((RaceActivity) c).findViewById(R.id.raceview_time_updater).setVisibility(View.GONE);
			((RaceActivity) c).findViewById(R.id.raceview_chronometer).setVisibility(View.VISIBLE);
		}		
		//TODO Start Race
		//TODO Rennen beginnt
		//Timer läuft
		//Es muss bei jeder Fahrzeugerkennung ein Zähler hochgezählt werden
		// Bei Ende processResults()		
	}
	
	public void go() {
		mRaceStarted = true;
		if(mMode == TIMER_MODE)
		{
			if(mRaceTimer != null)
				mRaceTimer.start();
		}else if(mMode == ROUND_MODE){
			if(mChronometer != null)
				mChronometer.start();
		}else
			Log.i("debug", "Fehler mein Starten des Timers, da kein korrekter Modus vorhanden ist.");			
	}
	
	public boolean isCorrectMovement(int lane, boolean recognized) {
		
		if(lane == LEFT_LANE)
		{
			boolean correctmovement = false;
			if(recognized) 
			{
				mLeftMovement = true;					
			}
			else 
			{				
				if (mLeftMovement)
				{
					mLeftMovement = false;	
					correctmovement = true;
					Log.i("debug", "Linkes movement!");
					if(getGameMode() == TIMER_MODE)
						calcStatistics(lane, mMode, mRaceTimer.getCurrentTime());
					else
						calcStatistics(lane, mMode, mChronometer.getTimeElapsedString());
					return correctmovement;
				}
				else
				{
					mLeftMovement = false;					
				}
			}
			return correctmovement;
		}
		else
		{
			boolean correctmovement = false;
			if(recognized) 
			{
				mRightMovement = true;			
			}			
			else
			{
				if(mRightMovement)
				{
					mRightMovement = false;		
					correctmovement = true;
					Log.i("debug", "Rechtes movement!");
					if(getGameMode() == TIMER_MODE)
						calcStatistics(lane, mMode, mRaceTimer.getCurrentTime());
					else
						calcStatistics(lane, mMode, mChronometer.getTimeElapsedString());
					return correctmovement;
				}
				else
				{
					mRightMovement = false;					
				}
			}
			return correctmovement;			
		}
	}	
	
	public boolean hasRaceBeenStarted(){
		return mRaceStarted;
	}
	
	public void countRounds(int lane) {
		
		if(lane == LEFT_LANE)
			mActLeftRound++;
		else
			mActRightRound++;	
	}

	public int isOver() {
		if(mMode == ROUND_MODE)
		{
			if(mActLeftRound == mCount)
				return LEFT_LANE;
			if (mActRightRound == mCount)
				return RIGHT_LANE;
		}
		else if(mMode == TIMER_MODE)			
		{
			
			if(mRaceTimer.isFinished())
			{
				if(mActLeftRound > mActRightRound)
					return LEFT_LANE;
				else
					return RIGHT_LANE;
			}				
		}
		return 0;		
	}
	
	public int getCurrentRound(int lane) {		
		if(lane == LEFT_LANE)			
			return mActLeftRound;		
		else
			return mActRightRound;
	}
	
	public double getCurrentSpeed (int lane) {
		if(lane == LEFT_LANE)
			return mActLeftSpeed;		
		else
			return mActRightSpeed;
	}
	
	public String getTrackName() {
		return mTrack.getName();
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
	
	public String getAttempt(int lane){
		String s;
		s = "attmepttemp";
		
		return s;
	}
	
	public String getDrivenMeters(int lane){
		String s;
		s = "metertemp";
		return s;
	}
	
	public String getFastestRound(int lane){
		String s;
		s = "fastesttemp";
		
		return s;		
	}
	
	public String getAvgSpeed(int lane){
		String s;
		s = "avgspeedtemp";
		
		return s;
	}
	
	private void setBestTime(String s, int lane){
		
		mBestTimeOnLane = new Pair<String, Integer>(s,lane);		
	}
	public Pair<String,Integer> getBestTime(){	
		
		return mBestTimeOnLane;		
	}
	
	
	private void calcStatistics(int lane, int mode, String time){		
		calcSpeedAndTime(lane, mode, time);
		storeRoundTime(lane);
		
	}
	private void storeRoundTime(int lane){
		if(lane == LEFT_LANE)
			mLeftTimes.add(mActLeftRoundTime);
		else
			mRightTimes.add(mActRightRoundTime);	
	}
	
	private void calcSpeedAndTime(int lane, int mode, String time) {		
		
		int[] _new = parseTimeString(time);
		int[] curr= new int[3];
		double _curr;
		if(lane == LEFT_LANE)
		{
			int[] old = parseTimeString(mOldTimeLeft);
			
			if(mode == TIMER_MODE)
			{				
				curr[0] = old[0] - _new[0];
				curr[1] = old[1] - _new[1];				
				curr[2] = old[2] - _new[2];		
			}
			else
			{			
				curr[0] = _new[0] - old[0];
				curr[1] = _new[1] - old[1];
				curr[2] = _new[2] - old[2];				
			}			
			if(curr[1] <0){
				curr[1] = curr[1] + 60;
				curr[0] = curr[0] - 1;
			}
			if(curr[2] <0){
				curr[2] = curr[2] + 100;
				curr[1] = curr[1] - 1;
			}
			Log.i("debug", "Rundenzeit:"+curr[0]+":"+curr[1]+":"+curr[2]);
			_curr = (curr[0]*60)+(curr[1])+ (curr[2] / 100.0); 
			if(mOldBestTime > _curr){
				mOldBestTime = _curr;
				setBestTime(curr[0]+":"+curr[1]+":"+curr[2], lane);
			}
			Log.i("debug", "Rundenzeit Links: "+_curr);
			mActLeftSpeed = mTrack.getLength()/ _curr;
			mActLeftRoundTime = (curr[0])+":"+(curr[1])+":"+(curr[2]); 			
			mOldTimeLeft = time;
			Log.i("debug","Alte Zeit links: "+mOldTimeLeft);
		}
		else
		{
			int[] old = parseTimeString(mOldTimeRight);
			if(mode == TIMER_MODE)
			{								
				curr[0] = old[0] - _new[0];
				curr[1] = old[1] - _new[1];
				curr[2] = old[2] - _new[2];
			}
			else
			{								
				curr[0] = _new[0] - old[0];
				curr[1] = _new[1] - old[1];
				curr[2] = _new[2] - old[2];
			}	
			if(curr[1] <0){
				curr[1] = curr[1] + 60;
				curr[0] = curr[0] - 1;
			}				
			if(curr[2] <0){
				curr[2] = curr[2] + 100;
				curr[1] = curr[1] - 1;
			}				
			_curr = (curr[0]*60) + (curr[1]) + (curr[2] / 100.0); 
			if(mOldBestTime > _curr){
				mOldBestTime = _curr;
				setBestTime(curr[0]+":"+curr[1]+":"+curr[2], lane);
			}
			Log.i("debug", "Rundenzeit Rechts: "+_curr);
			mActRightSpeed = mTrack.getLength() / _curr;
			mActRightRoundTime = (curr[0])+":"+(curr[1])+":"+(curr[2]); 
			mOldTimeRight = time;
			Log.i("debug","Alte Zeit rechts: "+mOldTimeRight);
		}		
	}	

	private int[] parseTimeString(String time) {
		int[] i = new int[3];
		String[] split;
		String s = time;
		Log.i("debug","Time unparsed.."+s);
		split = s.split(":");
		i[0] = Integer.parseInt(split[0]);
		i[1] = Integer.parseInt(split[1]);
		i[2] = Integer.parseInt(split[2]);
		return i;
	}
	public void processResults() {
		Log.i("debug", "Processing Results.....");
		Log.i("debug", "Rundenzeiten links: "+mLeftTimes);
		Log.i("debug","Rundenzeiten rechts: "+mRightTimes);		
		//Gibt das Ergebnis an Results-Klasse weiter.
	}
	
	public void cancel() {
		mRaceStarted = false;
		if(mMode == TIMER_MODE){
			mRaceTimer.stop();
			//mTimer.setText(mRaceTimer.getCurrentTime());
			Log.i("debug", "Race cancelled! at:"+mRaceTimer.getCurrentTime());
			
		}
		else
		{
			mChronometer.stop();
			//mChronometer.setText(Long.toString(mChronometer.getTimeElapsed()));
			Log.i("debug", "Race cancelled at:"+mChronometer.getTimeElapsed());
			
		}	
	}
	
	public void stop(){		
		if(mMode == TIMER_MODE){			
			processResults();
			mRaceStarted = false;
		}
		else
		{
			mChronometer.stop();
			processResults();	
			mRaceStarted = false;
		}	
		
	}
	/**
	 * 
	 * @param lane
	 * @return null when mPlayerArray is empty for this lane, else the color of the player according to the lane.
	 */
	public Scalar getPlayerColor(int lane){
		
		for(Player p : mPlayerArray) {
			if(p.getLane() == lane)
				return p.getColor();
		}		
		return null;
	}
	
	public int getPlayerRGBColor(int lane){
		
		for(Player p : mPlayerArray) {
			if(p.getLane() == lane)
				return p.getRGBColor();
		}		
		return 0;
	}
	
	/**
	 * 
	 * @param index
	 * @returns null if PlayerArray is smaller that the index, this getter is Zero-Based, counting begins from zero (0,1..), first position gives the left Player, second position the right Player
	 */
	public String getPlayerName(int index){
		if(mPlayerArray.size() > index)
			return mPlayerArray.get(index).getName();
		return null;
	}
	
	public String getFinishedTime() {
		String finishedTime;
		if(mMode == TIMER_MODE)
			finishedTime = "00:00:00";
		else
			finishedTime = Long.toString(mChronometer.getTimeElapsed());		
		return finishedTime;
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
