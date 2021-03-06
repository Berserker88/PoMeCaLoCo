package de.freinsberg.pomecaloco;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Scalar;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * This singletom class represents a race.
 * @author freinsberg
 *
 */
public class Race {
	final public static int ROUND_MODE = 1;
	final public static int TIMER_MODE = 2;	
	final public static int LEFT_LANE = 1;
	final public static int RIGHT_LANE = 2;
	final public static int GHOST_LANE = 3;
	final public static int SLOWER = -1;
	final public static int SAME = 0;
	final public static int FASTER = 1;
	
	private DBHelper mDbHelper;
	
	private RaceActivity mRaceActivity;	
	
	private TextView mTimer;
	private MyTimer mRaceTimer;
	
	private MillisecondChronometer mChronometer;
	
	private Pair<Double, Integer> mActVisualSpeedValue;	
	private Pair<String,Integer> mBestTimeOnLane;
	private Pair<Boolean, Integer> mNewRecord;
	
	private boolean mLeftMovement = false;
	private boolean mRightMovement = false;
	private boolean mRaceStarted = false;
	public boolean mGhostMode = false;


	private ArrayList<String> mGhostTimes;
	private ArrayList<Double> mGhostSpeeds;
	private ArrayList<String> mDriveThroughTimesGhost;
	private ArrayList<String> mLeftTimes = new ArrayList<String>();
	private ArrayList<String> mRightTimes = new ArrayList<String>();
	
	private List<Player> mPlayerArray = new ArrayList<Player>();
	
	private int mWinner;	
	private int mMode;
	private int mCount;
	private int mCarStatus;
	private int mActLeftRound;
	private int mActRightRound;
	private int mActGhostRound;
	
	private float mTrackLength;

	private double mActLeftSpeed;
	private double mActRightSpeed;
	private double mActGhostSpeed;
	
	private String mOldBestTimeLeft;
	private String mOldBestTimeRight;
	private String mActLeftRoundTime;		
	private String mActRightRoundTime;	
	private String mRoundGhostFinishedTime;
	private String mOldTimeLeft;
	private String mOldTimeRight;
	private String mTrackName;
	
	private static Race mInstance = new Race();	
	
	/**
	 * Constructor: This private Constructor makes this Class a Singleton- Class.
	 */
	private Race() {}
	
	/**
	 * This Function is used to get an Instance of the Race. So the Activities have the ability to use Methods of this class without creating an Object from it.
	 * @return An Instance of a Race.
	 */
	public static Race getInstance(){
		return mInstance;
	};

	/**
	 * This Method generates a new race with the given parameters.
	 * It is called before the StartActivity is finished and assigns the data from the race preparation into variables of this class.
	 * @param context The context to open the database connection.
	 * @param count The Number of Round (Roundmode) or the Number of Minutes (Timermode) the race will have.
	 * @param mode The gamemode for this race, wether Roundmode or Timermode.
	 * @param track The track to drive on.
	 * @param carStatus Where where the cars scanned and how many cars have been scanned.
	 * @param names The names of the player/s active in this race
	 * @param ghostMode Is this a race againt the besttime? (Yes(true) or no(false)).
	 */
	@SuppressWarnings("unchecked")
	public void newRace(Context context, int count, int mode, Object track, int carStatus, List<String> names, boolean ghostMode) {				
		mDbHelper = new DBHelper(context);
		mDbHelper.openDB();
		mPlayerArray.clear();		
		mMode = mode;
		mCount = count;
		mTrackName = ((Pair<String, byte[]>) track).getL();
		mTrackLength = mDbHelper.getTrackLength(mTrackName);
		mCarStatus = carStatus;
		mGhostMode = ghostMode;
		createPlayer(names);
		Log.i("debug","New race prepared! ");		
		Log.i("debug","       racemode(roundrace = '1' | timerrace = '2'): " + mMode);
		Log.i("debug","       how many rounds | minutes:                     " + mCount);
		Log.i("debug","       which track:                                   " + mTrackName);
		Log.i("debug","       car status(no_car = '0' | left_car = '10' | " +
			          "	      right_car = '01' | both_car = '11'):           " + mCarStatus);
		Log.i("debug","       race against besttime:                         " + mGhostMode);
	}

	/**
	 * This Method creates players for a race, the given parameter carstatus defines the lane for the player. 
	 * The parameter names has to contain a List of names with name at position 0 in oneplayer mode or with name for left player at position 0 and right player at position 1 in twoplayer mode.   
	 * @param carStatus The carstatus on the track.
	 * @param names A List of names containig the player driving this race.
	 */
	private void createPlayer(List<String> names) {
		switch(mCarStatus){
		case ObjectDetector.NO_CAR:			
			break;
		case ObjectDetector.RIGHT_CAR:			
			mPlayerArray.add(new Player(names.get(0), RIGHT_LANE,mMode, ObjectDetector.getInstance().getCarColor(RIGHT_LANE)));
			break;
		case ObjectDetector.LEFT_CAR:							
			mPlayerArray.add(new Player(names.get(0), LEFT_LANE,mMode, ObjectDetector.getInstance().getCarColor(LEFT_LANE)));
			break;
		case ObjectDetector.BOTH_CAR:
			mPlayerArray.add(new Player(names.get(0), LEFT_LANE,mMode, ObjectDetector.getInstance().getCarColor(LEFT_LANE)));
			mPlayerArray.add(new Player(names.get(1), RIGHT_LANE,mMode, ObjectDetector.getInstance().getCarColor(RIGHT_LANE)));			
			break;	
		default:
			break;		
		}
	}	

	/**
	 * This Method sets variables to initialize the race. The given Context is used to initialize the MillisecondChronometer(roundmode) with this context.
	 * The parameter time is used to set the timer value(timermode). 
	 * @param c The Context of the actual activity
	 * @param time The TextView that represents the Timer during Timermode.
	 */
	public void initRace(Context c, TextView time) {		
		if(!mLeftTimes.isEmpty())
			mLeftTimes.clear();
		if(!mRightTimes.isEmpty())
			mRightTimes.clear();
		mBestTimeOnLane  = null;
		mNewRecord = new Pair<Boolean, Integer>(false, 0);
		mWinner = 0;
		mActVisualSpeedValue = null;
		mOldBestTimeLeft = "99:99:99";
		mOldBestTimeRight ="99:99:99";
		mRaceStarted = false;		
		mTimer = time;
		mActLeftRound = 0;
		mActRightRound = 0;
		mActGhostRound = 0;
		mOldTimeLeft = "00:00:00";
		mOldTimeRight = "00:00:00";
		mActLeftRoundTime = "00:00:00";
		mActRightRoundTime = "00:00:00";	
		mRoundGhostFinishedTime = "";
		if(mMode == TIMER_MODE)
		{
			mRaceTimer = new MyTimer(mCount*60000, 10, mTimer);
			Log.i("debug", "TimerMode with Ghostmode? " + mGhostMode);
			if(mGhostMode){
				mGhostTimes = mDbHelper.getTimeGhost(mTrackName, mCount);
				mDriveThroughTimesGhost = calcDriveThroughTimesGhost(mMode);
				mGhostSpeeds = calcGhostSpeeds();
			}
			Log.i("debug", "ghostTimes: " + mGhostTimes);
			mOldTimeLeft = mRaceTimer.getCurrentTime();
			mOldTimeRight = mRaceTimer.getCurrentTime();
		}
		else
		{
			mChronometer = ((MillisecondChronometer)((RaceActivity) c).findViewById(R.id.raceview_chronometer));
			Log.i("debug", "RoundMode with Ghostmode? " + mGhostMode);
			if(mGhostMode){
				mGhostTimes = mDbHelper.getRoundGhost(mTrackName, mCount);
				mDriveThroughTimesGhost = calcDriveThroughTimesGhost(mMode);
				mGhostSpeeds = calcGhostSpeeds();
				mRoundGhostFinishedTime = mDbHelper.getRoundGhostTotalTime(mTrackName, mCount);
			}
			Log.i("debug", "ghostTimes: " + mGhostTimes);
			
			((RaceActivity) c).findViewById(R.id.raceview_time_updater).setVisibility(View.GONE);
			((RaceActivity) c).findViewById(R.id.raceview_chronometer).setVisibility(View.VISIBLE);
		}		
	}	
	
	public void setRaceActivity(RaceActivity ra){		
		mRaceActivity = ra;				
	}	
	
	/**
	 * This Method starts the Timer (Timermode) or the Counter (Roundmode).
	 * The truth value of mRaceStarted is set to TRUE.
	 */
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
			Log.i("debug", "no proper mode to to decide which timer should be started.");			
	}
	
	/**
	 * This Method determines if there is a correct movement for the given lane and logical value of recognized.
	 * For specific lane a correct movement happens when recognized was TRUE before it gets FALSE.  
	 * When correct movement happens calcStatistics() is called.
	 * @param lane The lane to find correct movement.
	 * @param recognized The truth value of recognized object on the frame.
	 * @return TRUE if there is a correct movement found, FALSE if no correct Movement is found.
	 */
	public boolean isCorrectMovement(int lane, boolean recognized) {
		
		if(lane == LEFT_LANE)
		{			
			boolean correctMovement = false;
			if(recognized) 
			{
				mLeftMovement = true;					
			}
			else 
			{				
				if (mLeftMovement)
				{
					mLeftMovement = false;	
					correctMovement = true;
					Log.i("movement", "left lane: thread id = "+ Thread.currentThread().getId() +"' and correct move = "+ correctMovement);
					Log.i("debug", "left movement");
					if(getGameMode() == TIMER_MODE)
						calcStatistics(lane, mMode, mRaceTimer.getCurrentTime());
					else
						calcStatistics(lane, mMode, mChronometer.getTimeElapsedString());
					return correctMovement;
				}
				else
				{
					mLeftMovement = false;					
				}
			}
			return correctMovement;
		}
		else
		{
			boolean correctMovement = false;
			if(recognized) 
			{
				mRightMovement = true;			
			}			
			else
			{
				if(mRightMovement)
				{
					mRightMovement = false;		
					correctMovement = true;
					Log.i("movement", "right lane: thread id = "+ Thread.currentThread().getId() +"' and correct move = "+ correctMovement);
					Log.i("debug", "right movement");
					if(getGameMode() == TIMER_MODE)
						calcStatistics(lane, mMode, mRaceTimer.getCurrentTime());
					else
						calcStatistics(lane, mMode, mChronometer.getTimeElapsedString());
					return correctMovement;
				}
				else
				{
					mRightMovement = false;					
				}
			}
			
			return correctMovement;			
		}
	}	
	
	/**
	 * This Method is used to returns wether the race has been started or not.
	 * @return TRUE if race has been started, FALSE if race has not been started, has been canceled or stopped.
	 */
	public boolean hasRaceBeenStarted(){
		return mRaceStarted;
	}
	
	/**
	 * This Method sets the actual Roundcounter + 1 for a specific lane and update the GUI-Elements.
	 * @param lane The lane on which the round has to be added.
	 */
	public void updateRoundsAndUI(int lane) {
		
		if(lane == LEFT_LANE)
			mActLeftRound++;
		else
			mActRightRound++;
		mRaceActivity.updateGUIElements(lane);
	}

	/**
	 * This Method is used to check if the race is over and gets the lane which matches the condition. 
	 * During Roundmode this is implemented by a comparison of the actual round on every lane with the estimated rounds for the race.
	 * During Timermode this is realised by checking which lane got more rounds after isFinished() from MyTimer class returned True.
	 * @return Return the lane which ends the race first, 0 if race is still running.
	 */
	public int isOver() {
		if(mMode == ROUND_MODE)
		{
			if(mActLeftRound == mCount)
				return LEFT_LANE;
			if (mActRightRound == mCount)
				return RIGHT_LANE;
			//Log.i("debug", "Chronometer elapsed Time: " + mChronometer.getTimeElapsedString());
			if(mGhostMode && (mChronometer.getTimeElapsedString().compareTo(mRoundGhostFinishedTime) > 0 ))
				return GHOST_LANE;
				
		}
		else if(mMode == TIMER_MODE)			
		{
			
			if(mRaceTimer.isFinished())
			{
				if(mGhostMode)
				{
					int timeGhostRounds = mDbHelper.getTimeGhostRounds(mTrackName, mCount);
					if(Race.getInstance().getUsedLane() == LEFT_LANE){
						if(timeGhostRounds > mActLeftRound){
							return GHOST_LANE;
						}
					}
					else if(Race.getInstance().getUsedLane() == RIGHT_LANE)
					{
						 if(timeGhostRounds < mActRightRound)
						 {
							 return GHOST_LANE;
						 }
					}
						
				}
				if(mActLeftRound > mActRightRound)
					return LEFT_LANE;
				else
					return RIGHT_LANE;
			}				
		}
		return 0;		
	}
	
	public int getUsedLane(){
		switch(mCarStatus){		
		case ObjectDetector.RIGHT_CAR:			
			return RIGHT_LANE;			
		case ObjectDetector.LEFT_CAR:							
			return LEFT_LANE;		
		default:
			return 0;				
		}
	}
	
	/**
	 * This Method return the actual round according to the given lane
	 * @param lane The lane for which the round is needed.
	 * @return The value of the actual round.
	 */
	public int getCurrentRound(int lane) {		
		if(lane == LEFT_LANE)			
			return mActLeftRound;		
		else if(lane == RIGHT_LANE)	
			return mActRightRound;
		else 
			return mActGhostRound;
	}
	
	/**
	 * This Method gets the current speed according to the given lane. 
	 * @param lane The lane for which the speed is needed.
	 * @return The double value of the speed.
	 */
	public double getCurrentSpeed (int lane) {
		if(lane == LEFT_LANE)
			return mActLeftSpeed;		
		else if(lane == RIGHT_LANE)
			return mActRightSpeed;
		else
			return mActGhostSpeed;
	}
	
	/**
	 * This Method gets the raced track name.
	 * @return The track name.
	 */
	public String getTrackName() {
		return mTrackName;
	}
	
	/**
	 * This Method gets the game mode of this race.
	 * @return The game mode(ROUNDMODE|TIMERMODE).
	 */
	public int getGameMode() {
		return mMode;
	}
	
	/**
	 * This Method returns the Number of Players that are driving in this race according to the car status.	
	 * @return The number of players, 0 if there are no Player in the race.
	 */
	public int getNumberOfPlayers() {
		switch(mCarStatus){
		case ObjectDetector.NO_CAR:	
			return 0;			
		case ObjectDetector.RIGHT_CAR:			
			return 1;			
		case ObjectDetector.LEFT_CAR:							
			return 1;			
		case ObjectDetector.BOTH_CAR:
			return 2;	
		default:
			return 0;
				
		}
	}
	
	/**
	 * This Method gets the number of Rounds (ROUNDMODE) respectively the number of minutes (TIMERMODE).
	 * @return The number.
	 */
	public int getCount() {
		return mCount;
	}
	
	/**
	 * This Method gets the driven meters during this race for the lane.
	 * @param lane The lane for which the driven meters is needed.
	 * @return The driven meters.
	 */
	public double getDrivenMeters(int lane){
		
		return calcDrivenMeters(lane);		
	}
	
	/**
	 * This Method gets the fastest round for the given lane.
	 * @param lane The lane for which the fastest round is needed.
	 * @return The fastest round after the race.
	 */
	public String getFastestRound(int lane){
		if (lane == LEFT_LANE)
			return mOldBestTimeLeft;
		else
			return mOldBestTimeRight;

	}
	
	/**
	 * This Method gets the average speed during the race according to the given lane.
	 * @param lane The lane for which the avg speed is needed.
	 * @return The average speed.
	 */
	public double getAvgSpeed(int lane){				
				return calcAvgSpeed(lane);

	}
	
	/**
	 * This Method sets the best Time during a race into a Pair of the given String and the lane.
	 * @param s The time to set to the pair.
	 * @param lane The lane to set to the pair.
	 */
	private void setBestTime(String s, int lane){
		
		mBestTimeOnLane = new Pair<String, Integer>(s,lane);		
	}
	
	/**
	 * This Method gets the best time measured as a pair of the time string and the according lane. 
	 * @return The Pair of the time string and the lane.
	 */
	public Pair<String,Integer> getBestTime(){	
		
		return mBestTimeOnLane;		
	}
	
	/**
	 * This Method is used to do store statistic stuff and calulations during the race.
	 * It uses the given parameters lane, mode and time to set them to the accessed methods calcSpeedAndTime() and storeRoundTime().
	 * @param lane The lane for which this method is needed.
	 * @param mode The game mode.
	 * @param time The current time, measured when a correct movement was recognized. 
	 */
	private void calcStatistics(int lane, int mode, String time){	
	
		calcSpeedAndTime(lane, mode, time);
		storeRoundTime(lane);
		
		
	}
	
	/**
	 * This Method stores the measured actual round time according to the given lane into a List of Strings.
	 * @param lane The lane for which the actual round time should be saved.
	 */
	private void storeRoundTime(int lane){
		if(lane == LEFT_LANE)
			mLeftTimes.add(mActLeftRoundTime);
		else
			mRightTimes.add(mActRightRoundTime);	
	}
	
	/**
	 * This Method calculates speed and round times based on the given time string. The time string is parsed to an integer array to work with it internally. 
	 * Every actual given time is stored after time measuring to make continuos calculation possible. 
	 * On every call of this function the member variables for actual round time and actual round speed is refreshed.
	 * @param lane The lane for which the calculation is needed.
	 * @param mode The game mode.
	 * @param time The time string with the actual time.
	 */
	private void calcSpeedAndTime(int lane, int mode, String time) {		
		
		DecimalFormat df = new DecimalFormat("00");
		int[] _new = parseTimeString(time);
		
		int[] curr= new int[3];
		double _curr;
		String __curr;
		
		if(lane == LEFT_LANE)
		{
			int[] old = parseTimeString(mOldTimeLeft);
			curr = calcRoundTime(mode, old, _new);
			mActLeftRoundTime = df.format((curr[0]))+":"+df.format((curr[1]))+":"+df.format((curr[2])); 			
			Log.i("debug", "Rundenzeit:"+curr[0]+":"+curr[1]+":"+curr[2]);
			
			_curr = (curr[0]*60)+(curr[1])+ (curr[2] / 100.0); 
			__curr = df.format(curr[0])+":"+df.format(curr[1])+":"+df.format(curr[2]);

			calcVisualSpeedValue(getBestTime(),mActLeftRoundTime);
			if(mBestTimeOnLane == null)
				setBestTime(__curr, lane);
			else if(mBestTimeOnLane.getL().compareTo(__curr) > 0){
				Log.i("debug", "Left lane is '" + mBestTimeOnLane.getL().compareTo(__curr) + "' faster than besttime");
				setBestTime(__curr, lane);
			}
			if(mOldBestTimeLeft.compareTo(__curr) > 0)
				mOldBestTimeLeft = __curr;
			
			Log.i("debug", "Rundenzeit Links: "+__curr);
			mActLeftSpeed = mTrackLength/ _curr;
			
			mOldTimeLeft = time;
			Log.i("debug","Alte Zeit links: "+mOldTimeLeft);
		}
		else if(lane == RIGHT_LANE)
		{
			int[] old = parseTimeString(mOldTimeRight);			
			curr = calcRoundTime(mode, old, _new);
			mActRightRoundTime = df.format((curr[0]))+":"+df.format((curr[1]))+":"+df.format((curr[2])); 
			_curr = (curr[0]*60) + (curr[1]) + (curr[2] / 100.0); 
			__curr = df.format(curr[0])+":"+df.format(curr[1])+":"+df.format(curr[2]);

			calcVisualSpeedValue(getBestTime(),mActRightRoundTime);;
			if(mBestTimeOnLane == null)
				setBestTime(__curr, lane);
			else if(mBestTimeOnLane.getL().compareTo(__curr) > 0){
				Log.i("debug", "Right lane is '" + mBestTimeOnLane.getL().compareTo(__curr) + "' faster than besttime");
				setBestTime(__curr, lane);
			}
			if(mOldBestTimeRight.compareTo(__curr) > 0)
				mOldBestTimeRight = __curr;
			
			
			Log.i("debug", "Rundenzeit Rechts: "+__curr);
			mActRightSpeed = mTrackLength / _curr;
			
			mOldTimeRight = time;
			Log.i("debug","Alte Zeit rechts: "+mOldTimeRight);
		}		

	}	

	private int[] calcRoundTime(int mode, int[] old, int[] _new){
		int[] curr = new int[3];
		
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
		return curr;
	}
	
	private double calcDrivenMeters(int lane){
		
		
		if(lane == LEFT_LANE)
			return mActLeftRound * mTrackLength;
		else
			return mActRightRound * mTrackLength;		
	}
	
	private double calcAvgSpeed(int lane){
		if(lane == LEFT_LANE){
			if(mMode == ROUND_MODE)
				return calcDrivenMeters(lane)/(mChronometer.getTimeElapsed()/1000);			
			else
				return calcDrivenMeters(lane)/(mCount*60);
		}
		else
			if(mMode == ROUND_MODE)
				return calcDrivenMeters(lane)/(mChronometer.getTimeElapsed()/1000);			
			else
				return calcDrivenMeters(lane)/(mCount*60);
	}

	/**
	 * This Method parses a given String with format mm:ss:ms to an integer array with 3 values
	 * @param time The time string to parse.
	 * @return The integer array with minutes at [0], seconds at[1], millis at [2].
	 */
	private int[] parseTimeString(String time) {
		int[] i = new int[3];
		String[] split;
		String s = time;
		//Log.i("debug","Time unparsed: "+s);
		split = s.split(":");
		i[0] = Integer.parseInt(split[0]);
		i[1] = Integer.parseInt(split[1]);
		i[2] = Integer.parseInt(split[2]);		
		//Log.i("debug","Time parsed: "+i[0] + ":" + i[1] + ":" + i[2]);
		return i;
	}
	

	public String getRoundGhostFinishedTime() {
		
		return mRoundGhostFinishedTime;		
	}
	
	/**
	 * This method is used to show the ghost in the ui when he drives through the start. 
	 * @param time The time to compare the Ghost time with to decide wether the ghost is shown or not.
	 */
	public void checkGhostTime(String time) {
				
		if(!mDriveThroughTimesGhost.isEmpty())
		{
			
				if((time.compareTo(mDriveThroughTimesGhost.get(0)) >= 0 && (mMode == ROUND_MODE)) || (time.compareTo(mDriveThroughTimesGhost.get(0)) <= 0 && (mMode == TIMER_MODE))){
					Log.i("debug", "Verstrichene Zeit: " + time + ", Geistzeit: " + mDriveThroughTimesGhost.get(0));
					Log.i("debug", "Geist erscheint!!!!");
					String actGhostTime = mGhostTimes.get(mActGhostRound);				
					if (mBestTimeOnLane == null || mBestTimeOnLane.getL().compareTo(actGhostTime) > 0){
						if(getUsedLane() == LEFT_LANE){
							setBestTime(actGhostTime, RIGHT_LANE);
						}
						else{
							setBestTime(actGhostTime, LEFT_LANE);
						}
					}
					if(mRaceActivity!=null){
						mActGhostRound++;
						mRaceActivity.updateGUIElements(GHOST_LANE);
						
					}				
					mDriveThroughTimesGhost.remove(0);
				}
		}			
	}
	
	/**
	 * This method is used to get the ghost speed out of the calculated list.
	 * @return The actual speed.
	 */
	public double getGhostSpeed(){
		
		if(!mGhostSpeeds.isEmpty()){
			double speed = mGhostSpeeds.get(0);
			mGhostSpeeds.remove(0);
			return speed;
		}
		else
			return 0;
	}
	
	/**
	 * This method calculates the speed of the ghost to access them during the race.
	 * @return The ArrayList containing the speeds.
	 */
	private ArrayList<Double> calcGhostSpeeds(){
		
		ArrayList<Double> ghostSpeeds = new ArrayList<Double>();
		double time = 0.0;
		
		for(String s : mGhostTimes){
			Log.i("debug", "ghost time: " + s);
			int[] temp = parseTimeString(s);
			time = (temp[0] * 60) + (temp[1]) + (temp[2] / 100.0);
			ghostSpeeds.add(mTrackLength / time);
//			Log.i("debug", "ghost speeds: " + ghostSpeeds);
		}	
		return ghostSpeeds;
	}
	
	/**
	 * This method puts the drivethrough times of the ghost into an array of strings to make them usable during the race.
	 * @param mode
	 * @return The list of roundtimes.
	 */
	private ArrayList<String> calcDriveThroughTimesGhost(int mode){
		DecimalFormat df = new DecimalFormat("00");
		ArrayList <String> drivethroughtimes = new ArrayList <String>();
		int[] accumulator = new int[3];
		accumulator[0] = 0;
		accumulator[1] = 0;
		accumulator[2] = 0;
		
		if(mode == ROUND_MODE)
		{
		for(String s : mGhostTimes){
			
			accumulator[0] += parseTimeString(s)[0];
			accumulator[1] += parseTimeString(s)[1];
			accumulator[2] += parseTimeString(s)[2];
			
			accumulator[1] += accumulator[2] / 100;
			accumulator[2] = accumulator[2] % 100;
			accumulator[0] += accumulator[1] / 100;
			accumulator[1] = accumulator[1] % 100;
			
			drivethroughtimes.add(df.format(accumulator[0]) + ":" + df.format(accumulator[1]) + ":" + df.format(accumulator[2]));		
		}	
		Log.i("debug", "DriveThroughTimesGhost: " + drivethroughtimes);
		return drivethroughtimes;	
		}
		else if (mode == TIMER_MODE)
		{
			accumulator[0] = mCount;
			accumulator[1] = 0;
			accumulator[2] = 0;
			for(String s : mGhostTimes)
			{
				accumulator[0] -=  parseTimeString(s)[0];
				accumulator[1] -=  parseTimeString(s)[1];
				accumulator[2] -=  parseTimeString(s)[2];
				
				if(accumulator[2] < 0)
				{
					accumulator[1] -= 1;
					accumulator[2] +=100;					
				}
				
				if(accumulator[1] < 0)
				{
					if(accumulator[0] > 0)
					{
						accumulator[0] -= 1;
						accumulator[1] += 60;	
					}
				}
				drivethroughtimes.add(df.format(accumulator[0]) + ":" + df.format(accumulator[1]) + ":" + df.format(accumulator[2]));	
			}			
			return drivethroughtimes;
		}		
		else
			return null;		
	}
	
	/**
	 * This Method is used to put measured values into a database to use them after the race.
	 */
	public void processResults(int winner) {
		Log.i("debug", "Processing Results.....");
		boolean i_win = false;
		mWinner = winner;
		
		//Insert new Players into Database
		switch(mCarStatus){
		case ObjectDetector.NO_CAR:			
			break;
		case ObjectDetector.RIGHT_CAR:	
			
				
			if(mDbHelper.isPlayerPresent(mPlayerArray.get(0).getName()))
			{
				if(mMode == Race.ROUND_MODE && mActRightRound == mCount)
				{
					mNewRecord.setL(mDbHelper.createRoundGhost(mTrackName, mCount, mRightTimes, getFinishedTime()));
					mNewRecord.setR(winner);
				}
				else if(mMode == Race.TIMER_MODE)
				{
					mNewRecord.setL(mDbHelper.createTimeGhost(mTrackName, mCount, mRightTimes, mActRightRound));
					mNewRecord.setR(winner);
				}
				if(mDbHelper.isPlayerTrackPresent(mPlayerArray.get(0).getName(), mTrackName, getGameMode()))
				{				
					mDbHelper.updatePlayerStats(mPlayerArray.get(0).getName(), mTrackName, getGameMode(), i_win, getFastestRound(RIGHT_LANE), (float) getAvgSpeed(RIGHT_LANE), (float) getDrivenMeters(RIGHT_LANE));
				}
				else
				{
					mDbHelper.createPlayerTrack(mPlayerArray.get(0).getName(), mTrackName, getGameMode(), i_win, getFastestRound(RIGHT_LANE), (float) getAvgSpeed(RIGHT_LANE), (float) getDrivenMeters(RIGHT_LANE));
				}
				break;
			}
			else				
				break;
		case ObjectDetector.LEFT_CAR:							
			if(mDbHelper.isPlayerPresent(mPlayerArray.get(0).getName()))
			{
				if(mMode == Race.ROUND_MODE && mActLeftRound == mCount)
				{
					mNewRecord.setL(mDbHelper.createRoundGhost(mTrackName, mCount, mLeftTimes, getFinishedTime()));
					mNewRecord.setR(winner);
				}
				else if(mMode == Race.TIMER_MODE)
				{
					mNewRecord.setL(mDbHelper.createTimeGhost(mTrackName, mCount, mLeftTimes, mActLeftRound));
					mNewRecord.setR(winner);
				}
				
				if(mDbHelper.isPlayerTrackPresent(mPlayerArray.get(0).getName(), mTrackName, getGameMode()))
				{				
					mDbHelper.updatePlayerStats(mPlayerArray.get(0).getName(), mTrackName, getGameMode(), i_win, getFastestRound(LEFT_LANE), (float) getAvgSpeed(LEFT_LANE), (float) getDrivenMeters(LEFT_LANE));
				}
				else
				{
					mDbHelper.createPlayerTrack(mPlayerArray.get(0).getName(), mTrackName, getGameMode(), i_win, getFastestRound(LEFT_LANE), (float) getAvgSpeed(LEFT_LANE), (float) getDrivenMeters(LEFT_LANE));
				}
				break;
			}
			else				
				break;
		case ObjectDetector.BOTH_CAR:
			if(mDbHelper.isPlayerPresent(mPlayerArray.get(0).getName()))
			{
				if(winner == LEFT_LANE)
					i_win = true;
				else
					i_win = false;
				if(mMode == Race.ROUND_MODE && mActLeftRound == mCount)
				{
					final boolean new_record = mDbHelper.createRoundGhost(mTrackName, mCount, mLeftTimes, getFinishedTime());
					mNewRecord.setL(new_record);
					mNewRecord.setR(winner);
				}
				else if(mMode == Race.TIMER_MODE)
				{
					final boolean new_record = mDbHelper.createTimeGhost(mTrackName, mCount, mLeftTimes, mActLeftRound);
					mNewRecord.setL(new_record);
					mNewRecord.setR(winner);
				}
				if(mDbHelper.isPlayerTrackPresent(mPlayerArray.get(0).getName(), mTrackName, getGameMode()))
				{				
					mDbHelper.updatePlayerStats(mPlayerArray.get(0).getName(), mTrackName, getGameMode(), i_win, getFastestRound(LEFT_LANE), (float) getAvgSpeed(LEFT_LANE), (float) getDrivenMeters(LEFT_LANE));
				}
				else
				{
					mDbHelper.createPlayerTrack(mPlayerArray.get(0).getName(), mTrackName, getGameMode(), i_win ,getFastestRound(LEFT_LANE), (float) getAvgSpeed(LEFT_LANE), (float) getDrivenMeters(LEFT_LANE));
				}
			}
			if(mDbHelper.isPlayerPresent(mPlayerArray.get(1).getName()))
			{			
				if(winner == RIGHT_LANE)
					i_win = true;
				else
					i_win = false;
				if(mMode == Race.ROUND_MODE && mActRightRound == mCount)
				{
					
					if(mDbHelper.createRoundGhost(mTrackName, mCount, mRightTimes, getFinishedTime()))
					{
						mNewRecord.setL(true);
						mNewRecord.setR(winner);						
					}				
				}
				else if(mMode == Race.TIMER_MODE)
				{
					if(mDbHelper.createTimeGhost(mTrackName, mCount, mRightTimes, mActRightRound))
					{
						mNewRecord.setL(true);
						mNewRecord.setR(winner);
					}
					
				}
				if(mDbHelper.isPlayerTrackPresent(mPlayerArray.get(1).getName(), mTrackName, getGameMode()))
				{				
					mDbHelper.updatePlayerStats(mPlayerArray.get(1).getName(), mTrackName, getGameMode(), i_win, getFastestRound(RIGHT_LANE), (float) getAvgSpeed(RIGHT_LANE), (float) getDrivenMeters(RIGHT_LANE));
				}
				else
				{
					mDbHelper.createPlayerTrack(mPlayerArray.get(1).getName(), mTrackName, getGameMode(), i_win , getFastestRound(RIGHT_LANE), (float) getAvgSpeed(RIGHT_LANE), (float) getDrivenMeters(RIGHT_LANE));
				}
			}
			break;	
		default:
			break;		
		}
		
		Log.i("debug", "Rundenzeiten links: "+mLeftTimes);
		Log.i("debug","Rundenzeiten rechts: "+mRightTimes);		
		//Gibt das Ergebnis an Results-Klasse weiter.
	}
	
	/**
	 * Gets the winner
	 * @return Integer representative for the started lane of the player who win.
	 */
	public int whoWin(){
		return mWinner;
	}
	
	/**
	 * Was this race a new record on this combination of track-mode-count?
	 * @return
	 */
	public Pair<Boolean, Integer>isRecord(){		
		return mNewRecord;		
	}
	/**
	 * This Method is used to stop the race timers, when the race is cancelled manually. 
	 * The truth value of mRaceStarted is set to FALSE.
	 */
	public void cancel() {
		mRaceStarted = false;
		if(mMode == TIMER_MODE){
			mRaceTimer.stop();
			//mTimer.setText(mRaceTimer.getCurrentTime());
			Log.i("debug", "Race cancelled at:"+mRaceTimer.getCurrentTime());			
		}
		else
		{
			mChronometer.stop();
			//mChronometer.setText(Long.toString(mChronometer.getTimeElapsed()));
			Log.i("debug", "Race cancelled at:"+mChronometer.getTimeElapsed());			
		}	
	}
	
	/**
	 * This Method is used, when the race has ended properly. 
	 * The Method processResults() is called.
	 * The truth value of mRaceStarted is set to FALSE.
	 */
	public void endRaceAndUpdateUI(int lane){		
		mRaceStarted = false;
		if(mMode == TIMER_MODE){
			
//				for(Player p :mPlayerArray){
//					p.incAttempt();
//				}
				processResults(lane);
				mRaceActivity.finishGUIElements(lane);
				
			
		}
		else
		{
			mChronometer.stop();			
//				for(Player p :mPlayerArray){
//					p.incAttempt();
//				}
				processResults(lane);	
				mRaceActivity.finishGUIElements(lane);
			
		}	
		
	}
	
	
	
	/**
	 * This Method gets the players color as a Scalar according to the given lane. The value is get by using the method of the Player class.
	 * @param lane The lane for which the color Scalar is needed.
	 * @return null when mPlayerArray is empty for this lane, else the color of the player according to the lane.
	 */
	public Scalar getPlayerColor(int lane){
		
		for(Player p : mPlayerArray) {
			if(p.getLane() == lane)
				return p.getColor();
		}		
		return null;
	}
	
	/**
	 * This Method gets the players color as an integer-representative according to the given lane. The value is get by using the method of the Player class.
	 * @param lane The lane for which the integer-representative is needed.
	 * @return 0 when mPlayerArray is empty for this lane, else the colors integer-representative of the player according to the lane.
	 */
	public int getPlayerRGBColor(int lane){
		
		for(Player p : mPlayerArray) {
			if(p.getLane() == lane)
				return p.getRGBColor();
		}		
		return 0;
	}
	
	/**
	 * This Method gets the player name for the given index in the player array.
	 * @param index The index of the needed player.
	 * @returns null if PlayerArray is smaller that the index, this getter is Zero-Based, counting begins from zero (0,1..), first position gives the left Player, second position the right Player
	 */
	public String getPlayerName(int index){
		if(mPlayerArray.size() > index)
			return mPlayerArray.get(index).getName();
		return null;
	}
	
	/**
	 * This Method gets a String that represents the Time when race is finished. When in a timer mode race, this String is set to "00:00:00", in round mode this String is set to the elapsed time on the Chronometer
	 * @return
	 */
	public String getFinishedTime() {
		String finishedTime;
		if(mMode == TIMER_MODE)
			finishedTime = "00:00:00";
		else
			finishedTime = mChronometer.getTimeElapsedString();		
		return finishedTime;
	}
	
	/**
	 * This method calculates the visual speed updater and show the result in the ui everytime it is called.
	 * @param lane The lane for which the calculation is needed.
	 */
	private void calcVisualSpeedValue(Pair<String, Integer> bestTime, String lastDrivenTime) {
		int[] curr = new int[3];
		double _curr;
		int[] best = new int[3];
		double _best;
		
			if(bestTime != null && !lastDrivenTime.isEmpty()){
				best = parseTimeString(bestTime.getL());			
				curr = parseTimeString(lastDrivenTime);
				
				_best = (best[0]*60) + (best[1]) + (best[2] / 100.0);
				_curr = (curr[0]*60) + (curr[1]) + (curr[2] / 100.0);
				Log.i("debug", "Besttime: " + _best + ", Currenttime: " + _curr);
				if((_best - _curr) < 0){
					mActVisualSpeedValue = new Pair<Double, Integer>(((_best - _curr)*-1), SLOWER);
				} else if((_best - _curr) > 0){
					mActVisualSpeedValue = new Pair<Double, Integer>((_best - _curr), FASTER);
				}else{
					mActVisualSpeedValue = new Pair<Double, Integer>(0.0, SAME);
				}
			}		
	}
	
	/**
	 * This method is used to get the amount of pixels and the direction of the bar in the visual speed updater.
	 * @param scale The display-density.
	 * @param lane The lane for 
	 * @return
	 */	
	public Pair<Integer, Integer> getVisualSpeedValue(float scale){	
		//The Maximum Pixelvalue is 50 
		int pixels;		
			if(mActVisualSpeedValue != null){
				int value = (int) (mActVisualSpeedValue.getL() * 10);
				if(value > 50)
					value = 50;
				pixels = (int) (value * scale + 0.5f);
				return new Pair<Integer, Integer>(pixels, mActVisualSpeedValue.getR());  		
			}		
			return null;
	}

	public String getActRoundTime(int lane) {
		if(lane == LEFT_LANE){
			Log.i("debug", "mActLeftRoundTime:"+mActLeftRoundTime);
			return mActLeftRoundTime;			
		}
		else if(lane == RIGHT_LANE){
			return mActRightRoundTime;
		}
		else if(lane == GHOST_LANE){
			return mDriveThroughTimesGhost.get(0);
		}
		else
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
