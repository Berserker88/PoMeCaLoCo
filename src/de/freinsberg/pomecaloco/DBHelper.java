package de.freinsberg.pomecaloco;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * This class represents the database including the databasescheme, methods and testdata. 
 * @author freinsberg
 *
 */
public class DBHelper extends SQLiteOpenHelper{
	
	private static final String DB_NAME = "pomecaloco.db";
	private static final int DB_VERSION = 1;
	private Context mContext;	
	private SQLiteDatabase mDB;
	
	/**
	 * Creates the database object
	 * @param context The given context where the database is been used.
	 */
	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		mContext = context;
		
	}
	//the following section declares the database layout
	
	private class TblTrack {
		
		private static final String NAME = "tracks";
		private static final String COL_NAME = "name";
		private static final String COL_LENGTH = "length";		
		private static final String COL_ISCROSSED = "iscrossed";
		private static final String COL_IMAGE = "image";		
	}
	
	private class TblPlayer_Track {
		
		private static final String NAME = "player_track";
		private static final String COL_TRACKNAME = "track_name";
		private static final String COL_PLAYERNAME = "player_name";
		private static final String COL_MODE = "mode";
		private static final String COL_ATTEMPT = "attempt";
		private static final String COL_WINS = "wins";
		private static final String COL_FASTESTROUND = "fastestround";
		private static final String COL_LASTDRIVENMETERS = "lastdrivenmeters";		
		private static final String COL_WHOLEDRIVENMETERS = "wholedrivenmeters";	
		private static final String COL_LASTAVERAGESPEED = "lastaveragespeed";
		private static final String COL_WHOLEAVERAGESPEED = "wholeaveragespeed";		
	}
	private class TblPlayer {
		
		private static final String NAME = "player";
		private static final String COL_NAME = "name";
		private static final String COL_LIFETIMEMETERS ="lifetimemeters";
	}
	
	private class TblRoundGhost {
		
		private static final String NAME = "roundghost";
		private static final String COL_NAME = "track_name";		
		private static final String COL_ROUNDS = "rounds";		
		private static final String COL_TIMES = "times";
		private static final String COL_TOTAL_TIME = "total_time";
	}
	
	private class TblTimeGhost {
		
		private static final String NAME = "timeghost";
		private static final String COL_NAME = "track_name";
		private static final String COL_TIME = "time";
		private static final String COL_TIMES = "times";
		private static final String COL_TOTAL_ROUNDS = "total_rounds";
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i("debug", "DBHelper: onCreate()");
		
		//Creating the Tracks Table
		db.execSQL(
				"CREATE TABLE " + TblTrack.NAME + 
				"("+ TblTrack.COL_NAME + " VARCHAR(30) PRIMARY KEY,"
				+ TblTrack.COL_LENGTH + " INTEGER,"				
				+ TblTrack.COL_ISCROSSED + " BOOLEAN,"				
				+ TblTrack.COL_IMAGE + " BLOB)"				
			);
		
		//Creating the Player Table
		db.execSQL(
				"CREATE TABLE " + TblPlayer.NAME + 
				"("+ TblPlayer.COL_NAME + " VARCHAR(30) PRIMARY KEY,"	
				+ TblPlayer.COL_LIFETIMEMETERS + " FLOAT(5))"
			);
		
		//Creating the RoundGhost Table
		db.execSQL(
				"CREATE TABLE " + TblRoundGhost.NAME + 
				"(" + TblRoundGhost.COL_NAME + " VARCHAR(30), "							
				+TblRoundGhost.COL_ROUNDS + " INTEGER, "				
				+TblRoundGhost.COL_TIMES + " VARCHAR(10000), "
				+TblRoundGhost.COL_TOTAL_TIME + " VARCHAR(9), "
				+ "PRIMARY KEY (" + TblRoundGhost.COL_NAME + ", " + TblRoundGhost.COL_ROUNDS + "), "
				+ "FOREIGN KEY (" + TblRoundGhost.COL_NAME+ ") REFERENCES " + TblTrack.NAME + "(" + TblTrack.COL_NAME + "))"
			);
		
		//Creating the TimeGhost Table
		db.execSQL(
				"CREATE TABLE " + TblTimeGhost.NAME + 
				"(" + TblTimeGhost.COL_NAME + " VARCHAR(30), "	
				+TblTimeGhost.COL_TIME + " INTEGER, "
				+TblTimeGhost.COL_TIMES + " VARCHAR(10000), "
				+TblTimeGhost.COL_TOTAL_ROUNDS + " INTEGER, "
				+ "PRIMARY KEY (" + TblTimeGhost.COL_NAME + ", " + TblTimeGhost.COL_TIME + "), "
				+ "FOREIGN KEY (" + TblTimeGhost.COL_NAME + ")  REFERENCES " + TblTrack.NAME + "(" + TblTrack.COL_NAME + "))"
			);
		
		
		//Creating the Player-Track Table
		db.execSQL(
				"CREATE TABLE " + TblPlayer_Track.NAME +
				"(" + TblPlayer_Track.COL_PLAYERNAME + " VARCHAR(30), "
				+ TblPlayer_Track.COL_TRACKNAME + " VARCHAR(30), "
				+ TblPlayer_Track.COL_MODE + " INTEGER, "
				+ TblPlayer_Track.COL_ATTEMPT + " INTEGER,"
				+ TblPlayer_Track.COL_WINS + " INTEGER,"
				+ TblPlayer_Track.COL_FASTESTROUND + " VARCHAR(9),"
				+ TblPlayer_Track.COL_LASTDRIVENMETERS + " FLOAT(5),"	
				+ TblPlayer_Track.COL_WHOLEDRIVENMETERS + " FLOAT(5),"
				+ TblPlayer_Track.COL_LASTAVERAGESPEED + " FLOAT(5),"
				+ TblPlayer_Track.COL_WHOLEAVERAGESPEED + " FLOAT(5),"
				+ "PRIMARY KEY (" + TblPlayer_Track.COL_PLAYERNAME + ", " + TblPlayer_Track.COL_TRACKNAME + ", " + TblPlayer_Track.COL_MODE + "), "
				+ "FOREIGN KEY (" + TblPlayer_Track.COL_PLAYERNAME + ")  REFERENCES " + TblTrack.NAME + "(" + TblTrack.COL_NAME + "), "
				+ "FOREIGN KEY (" + TblPlayer_Track.COL_TRACKNAME + ")  REFERENCES " + TblPlayer.NAME + "(" + TblPlayer.COL_NAME + "))"
			);
			mDB = db;
			initTracks();
	}

	/** 
	 * This method is used to open the database connection. the running database gets assigned to be writable. 
	 */
	public void openDB(){
		
		mDB = getWritableDatabase();
	}
	
	private void initTracks(){
		Bitmap bridge_image;
		Bitmap crossed_image;
		ByteArrayOutputStream stream;
		
		stream = new ByteArrayOutputStream();
		
		bridge_image = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bridge_image);
		crossed_image = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.crossed_image);
		
		bridge_image.compress(Bitmap.CompressFormat.PNG, 90, stream);		
		
		createTrack("Brückenbahn", false, (float)3.75, stream.toByteArray());
		stream.reset();
		crossed_image.compress(Bitmap.CompressFormat.PNG, 90, stream);
		createTrack("Kreuzungsbahn", true, (float)5.50, stream.toByteArray());		
	}
	
/**
 * This Method creates a player in the player-table.
 * @param name The name of the player to be inserted into the database.
 */
	public void createPlayer(String name){
		mDB.execSQL(
				"INSERT INTO "+ TblPlayer.NAME + "(" 
				+ TblPlayer.COL_NAME + ", " 
				+ TblPlayer.COL_LIFETIMEMETERS + ")"
				+ "VALUES('" + name + "', " + 0 +")"
			);
		//TODO: exception handling!
	}
		
	/**
	 * This Method creates a track in the track-table.
	 * @param name The Track name.
	 * @param iscrossed Is it a crossed Track.
	 * @param length How long is this track in meters.
	 * @param image A picture of the track.
	 */
	private void createTrack(String name, boolean iscrossed, float length, byte[] image){
		
		Log.i("debug", "Byte[] before insertion into tracks: " + image);
		Log.i("debug", "mDb: " + mDB);
		ContentValues vals = new ContentValues();
		vals.put(TblTrack.COL_NAME, name);
		vals.put(TblTrack.COL_ISCROSSED, iscrossed);
		vals.put(TblTrack.COL_LENGTH, length);
		vals.put(TblTrack.COL_IMAGE, image);
		mDB.insert(TblTrack.NAME, null, vals);
		//TODO: exception handling!
	}
	
	/**
	 * This Method creates a new ghost in Roundmode for the given value combination.
	 * @param name The name of the track.
	 * @param rounds How many round have been set for the race.
	 * @param timesList A list of the calculated round times.
	 * @param total_time The total time the ghost has needed.
	 * @return true if Roundghost has been successfully stored into the database, false if not.
	 */
	public boolean createRoundGhost(String name, int rounds, ArrayList<String> timesList, String total_time) {
		
		String times = "";
		boolean isPresent;
		for(String s : timesList){
			times += s + ";";			
		}
		if(times.length() > 1)
			times = times.substring(0, times.length() -1);
		String whereClause = TblRoundGhost.COL_NAME + "=? AND " + TblRoundGhost.COL_ROUNDS + "=?";
		Cursor cursor = mDB.query(TblRoundGhost.NAME, new String[]{TblRoundGhost.COL_TOTAL_TIME}, whereClause , new String[]{name, ""+rounds}, null, null, null);
		isPresent = cursor.moveToFirst();
		if(isPresent){			
			if(total_time.compareTo(cursor.getString(0)) < 0)
			{	
				ContentValues values = new ContentValues();
				values.put(TblRoundGhost.COL_TIMES, times);
				values.put(TblRoundGhost.COL_TOTAL_TIME, total_time);
				mDB.update(TblRoundGhost.NAME, values, whereClause, new String[]{name, ""+rounds});
				Log.i("debug", "Updating new ghost time into database for track: " + name + " with count: " + rounds);			
				return true;
			}
			else if (total_time.compareTo(cursor.getString(0)) == 0)
			{
				Log.i("debug", "Same Ghost Times for track: " + name + "with count: " + rounds + ", no database action!");
				return false;
			}
			else
			{
				Log.i("debug", "Player is slower than Ghost for database on track: " + name + " with count: " + rounds + ", no database action!");
				return false;
			}
		}
		else
		{			
			
			mDB.execSQL(
					"INSERT INTO " + TblRoundGhost.NAME + "("
					+ TblRoundGhost.COL_NAME + ", " + TblRoundGhost.COL_ROUNDS + ", " +  TblRoundGhost.COL_TIMES + ", " +  TblRoundGhost.COL_TOTAL_TIME +  ")"
					+ "VALUES('" + name + "', " + rounds + ", '" + times + "', '" + total_time + "')"				
				);
			Log.i("debug", "New Ghost Data Row written into database for track: " + name + " with count: " + rounds);
			return true;
		}
	}
	
	/**
	 * This Method creates a new ghost in Timermode for the given value combination.
	 * @param name The name of the track.
	 * @param time How many minutes have been set for the race.
	 * @param timesList A list of the calculated round times.
	 * @param total_rounds How many round have been completed.
	 * @return true if Timerghost has been successfully stored into the database, false if not.
	 */
	public boolean createTimeGhost(String name, int time, ArrayList<String> timesList, int total_rounds) {
		
		String times = "";
		boolean isPresent;
		for(String s : timesList){
			times += s + ";";			
		}
		if(times.length() > 1)
			times = times.substring(0, times.length() -1);
		String whereClause = TblTimeGhost.COL_NAME + "=? AND " + TblTimeGhost.COL_TIME + "=?";
		Cursor cursor = mDB.query(TblTimeGhost.NAME, new String[]{TblTimeGhost.COL_TOTAL_ROUNDS}, whereClause , new String[]{name, ""+time}, null, null, null);
		isPresent = cursor.moveToFirst();
		if(isPresent){			
			if(total_rounds > cursor.getInt(0))
			{	
				ContentValues values = new ContentValues();
				values.put(TblTimeGhost.COL_TIMES, times);
				values.put(TblTimeGhost.COL_TOTAL_ROUNDS, total_rounds);
				mDB.update(TblTimeGhost.NAME, values, whereClause, new String[]{name, ""+times});
				Log.i("debug", "Updating new ghost Times with more Rounds into database for track: " + name + " with count: " + time);
				return true;
			}
			else if (total_rounds == cursor.getInt(0))
			{
				Log.i("debug", "Same Ghost Rounds for track: " + name + "with count: " + time + ", no database action!");
				return false;
			}
			else
			{
				Log.i("debug", "Player has less Rounds than Ghost in database for track: " + name + " with count: " + time + ", no database action!");
				return false;
			}
		}
		else
		{				
			mDB.execSQL(
					"INSERT INTO " + TblTimeGhost.NAME + "("
					+ TblTimeGhost.COL_NAME + ", " + TblTimeGhost.COL_TIME + ", " + TblTimeGhost.COL_TIMES + ", "  + TblTimeGhost.COL_TOTAL_ROUNDS + ")"
					+ "VALUES('" + name + "', " + time + ", '" + times + "', " + total_rounds + ")"				
				);
			Log.i("debug", "New Ghost Data Row written into database for track: " + name + " with count: " + time);
			return true;
		}
	}
	
	/**
	 * Gets the total_time of a Roundghost for the given parameters.
	 * @param name The name of the track.
	 * @param rounds The number of round the ghost has been stored with.
	 * @return The total_time.
	 */
	public String getRoundGhostTotalTime(String name, int rounds) {
		String total_time="";
		
		String whereClause = TblRoundGhost.COL_NAME + "=? AND " + TblRoundGhost.COL_ROUNDS + "=?";
		Cursor cursor = mDB.query(TblRoundGhost.NAME, new String[]{TblRoundGhost.COL_TOTAL_TIME}, whereClause, new String[]{name, ""+rounds}, null, null, null);
		cursor.moveToFirst();
		total_time = cursor.getString(0);
		cursor.close();
		return total_time;
		
	}
	
	/**
	 * Gets the total_rounds of a Timerghost for the given parameters.
	 * @param name The name of the Track.
	 * @param time The amount of minutes the ghost has been stores with.
	 * @return The number of rounds.
	 */
	public int getTimeGhostRounds(String name, int time) {
		int rounds;
				
		String whereClause = TblTimeGhost.COL_NAME + "=? AND " + TblTimeGhost.COL_TIME + "=?";
		Cursor cursor = mDB.query(TblTimeGhost.NAME, new String[]{TblTimeGhost.COL_TIMES}, whereClause, new String[]{name, ""+time}, null, null, null);
		cursor.moveToFirst();
		
		rounds = new ArrayList<String>(Arrays.asList(cursor.getString(0).split(";"))).size();
		Log.i("debug", "TimeGhost has '" + rounds + "' rounds.");
		return rounds;				
	}
	
	/**
	 * This method has only to be called when the method isPlayerTrackPresent() returns false.
	 * Creates  and stores a new dataset for a combination of player-track-mode into the database. 	 * 
	 * @param playername The name of the player.
	 * @param trackname The name of the track.
	 * @param mode The racemode (roundmode or rimermode).	
	 * @param win Did this player win the race ?
	 * @param fastestround The fastest round in this race.
	 * @param averagespeed The average speed in this race.
	 * @param drivenmeters The driven meters in this race.
	 */
	public void createPlayerTrack(String playername, String trackname, int mode, boolean win, String fastestround, float averagespeed, float drivenmeters){
		int i_win;
		if(win)
			i_win= 1;
		else
			i_win = 0;
			
		mDB.execSQL(
				"INSERT INTO "+ TblPlayer_Track.NAME + "("
				+ TblPlayer_Track.COL_PLAYERNAME + ", "
				+ TblPlayer_Track.COL_TRACKNAME + ", "
				+ TblPlayer_Track.COL_MODE + ", "
				+ TblPlayer_Track.COL_ATTEMPT + ", "
				+ TblPlayer_Track.COL_WINS + ", "
				+ TblPlayer_Track.COL_FASTESTROUND + ", "
				+ TblPlayer_Track.COL_LASTAVERAGESPEED + ", "
				+ TblPlayer_Track.COL_WHOLEAVERAGESPEED + ", "
				+ TblPlayer_Track.COL_LASTDRIVENMETERS + ", "	
				+ TblPlayer_Track.COL_WHOLEDRIVENMETERS + ") "	
				+ "VALUES('" + playername + "', '" + trackname + "', " + mode + ", " + 1 + ", " + i_win + ", '" + fastestround + "', '" + averagespeed + "','" + averagespeed + "','" + drivenmeters + "', '" + drivenmeters + "')"
			);
		ContentValues values = new ContentValues();
		values.put(TblPlayer.COL_LIFETIMEMETERS, drivenmeters);
		int howManyRowsAffected = mDB.update(TblPlayer.NAME, values, TblPlayer.COL_NAME + "=?", new String[]{playername});
		if(howManyRowsAffected < 1){
			Log.i("debug","No Player to update Lifetimemeters for the first time!");
			
		}
		
	}
	
	/**
	 * This Method returns all tracks with its images.
	 * @return All pairs of track and image.
	 */
	public ArrayList<Pair<String,byte[]>> getAllTracksWithImages(){
		ArrayList<Pair<String,byte[]>> tracks = new ArrayList<Pair<String,byte[]>>();
		Cursor cursor = mDB.query(TblTrack.NAME, new String[]{TblTrack.COL_NAME,TblTrack.COL_IMAGE}, null, null, null, null, TblTrack.COL_NAME);
		
		cursor.moveToFirst();
		
		while(!cursor.isAfterLast()){
			
			tracks.add(new Pair<String, byte[]>(cursor.getString(0), cursor.getBlob(1)));
			cursor.moveToNext();			
		}
		
		cursor.close();	
		
		return tracks;	
		
	}
	
	/**
	 * Gets the track length in meters.
	 * @param name The track which length is needed.
	 * @return The track length in meters.
	 */
	public float getTrackLength(String name){
		Log.i("debug", "Get Track Length for: "+ name);
		float length;
		
		Cursor cursor = mDB.query(TblTrack.NAME, new String[]{TblTrack.COL_LENGTH}, TblTrack.COL_NAME + "=?", new String[]{name}, null, null, null);
		
		cursor.moveToFirst();
		
		length = cursor.getFloat(0);
		cursor.close();
		return length;
		
	}
	
	/**
	 * This method gets all names of the stored players in the player-table.
	 * @return A list of all found player names.
	 */
	public ArrayList<String> getAllPlayernames(){
		ArrayList<String> names = new ArrayList<String>();
		Cursor cursor = mDB.query(TblPlayer.NAME, new String[]{TblPlayer.COL_NAME}, null, null, null, null, TblPlayer.COL_NAME);
		
		cursor.moveToFirst();
		
		while(!cursor.isAfterLast()){
			
			names.add(cursor.getString(0));
			cursor.moveToNext();			
		}
		cursor.close();
		return names;
	}
	
	/**
	 * This method checks if a player is present in the database.
	 * @param name The name of the player to be searched.
	 * @return true If a player with the given name is present in the player-table, false if not. 
	 */
	public boolean isPlayerPresent(String name){
		
		boolean isPresent = false;
		
		Cursor cursor = mDB.query(TblPlayer.NAME, new String[]{TblPlayer.COL_NAME}, TblPlayer.COL_NAME + "=?", new String[]{name}, null, null, null);
		
		if(cursor.moveToFirst())
			isPresent = true;
		cursor.close();
		return isPresent;	
	}
	
	/**
	 * This method has to be called before createPlayerTrack() is been called. 
	 * It checks if a dataset for the given combination of player, track, and mode is created before and is available in the database.
	 * @param player The player name.
	 * @param track The track name.
	 * @param mode The Racemode.
	 * @return true, if a dataset for this combination has been found in the database, false if not.
	 */
	public boolean isPlayerTrackPresent(String player, String track, int mode) {
	
		boolean isPresent = false;
		String whereClause = TblPlayer_Track.COL_PLAYERNAME + "=? AND " + TblPlayer_Track.COL_TRACKNAME + "=? AND " + TblPlayer_Track.COL_MODE + "=?"; 
		
		Cursor cursor = mDB.query(TblPlayer_Track.NAME, new String[]{TblPlayer_Track.COL_ATTEMPT}, whereClause, new String[]{player, track, ""+mode}, null, null, null);
		
		if(cursor.moveToFirst())
			isPresent = true;
		cursor.close();
		return isPresent;
	}
	
	/**
	 * Gets a result set for a given combination of the player, the track and the racemode.
	 * @param player The player name.
	 * @param track The track name.
	 * @param mode The racemode (roundmode or timermode).
	 * @return The resultset including the values: attempt, wins, fastest round, last average speed, whole average speed an last driven meters. They are stored with a description at the first place following the value in the array list.
	 */
	public ArrayList<String> getResultSet(String player, String track, int mode){
		
		ArrayList<String> resultSet = new ArrayList<String>();
		
		String whereClause = TblPlayer.COL_NAME + "=?";
		
		Cursor cursor = mDB.query(TblPlayer.NAME, new String[]{TblPlayer.COL_LIFETIMEMETERS}, whereClause, new String[]{player}, null, null, null);
		
		if(!cursor.moveToFirst()){
			Log.i("debug", "No Player '"+player+"' in Player table!");
			return null;
		}		
		//Insert the whole driven Meters into the ArrayList
		resultSet.add(mContext.getResources().getString(R.string.db_results_wholedrivenmeters));
		resultSet.add(""+cursor.getFloat(0));
		
		whereClause = TblPlayer_Track.COL_PLAYERNAME + "=? AND " + TblPlayer_Track.COL_TRACKNAME + "=? AND " + TblPlayer_Track.COL_MODE + "=?";
		
		
		
		cursor = mDB.query(TblPlayer_Track.NAME, new String[]{TblPlayer_Track.COL_ATTEMPT, TblPlayer_Track.COL_WINS, TblPlayer_Track.COL_FASTESTROUND, TblPlayer_Track.COL_LASTAVERAGESPEED, TblPlayer_Track.COL_WHOLEAVERAGESPEED, TblPlayer_Track.COL_LASTDRIVENMETERS}, whereClause, new String[]{player, track, ""+mode}, null, null, null);
		
		if(!cursor.moveToFirst()){
			Log.i("debug", "No Infos for Player '"+player+"' on Track '" + track + "' with mode" + mode +" in Player_Track table!");
			return resultSet;
		}
		
		//Insert the #Attempt into the ArrayList
		resultSet.add(mContext.getResources().getString(R.string.db_results_attempt));
		resultSet.add(""+cursor.getInt(0));
		
		//Insert the #Wins into the ArrayList
		resultSet.add(mContext.getResources().getString(R.string.db_results_wins));
		resultSet.add(""+cursor.getInt(1));
		
		//Insert the Fastest Round into the ArrayList
		resultSet.add(mContext.getResources().getString(R.string.db_results_fastestround));
		resultSet.add(cursor.getString(2));
		
		//Insert the last average Speed into the ArrayList
		resultSet.add(mContext.getResources().getString(R.string.db_results_lastaveragespeed));
		resultSet.add(""+String.format("%.2f", cursor.getFloat(3)) + " m/s");
		
		//Insert the whole average Speed into the ArrayList
		resultSet.add(mContext.getResources().getString(R.string.db_results_wholeaveragespeed));
		resultSet.add(""+String.format("%.2f", cursor.getFloat(4)) + " m/s");
		
		//Insert the last driven Meters into the ArrayList
		resultSet.add(mContext.getResources().getString(R.string.db_results_lastdrivenmeters));
		resultSet.add(""+cursor.getFloat(5) + " Meter");		

		
		cursor.close();
		
		return resultSet;		
	}	

	/**
	 * Gets the total meters a player has been driven.
	 * @param name The player name
	 * @return The total meters in meters.
	 */
	public float getPlayerLifetimeMeters(String name) {
		float meters;
		
		Cursor cursor = mDB.query(TblPlayer.NAME, new String[]{TblPlayer.COL_LIFETIMEMETERS}, TblPlayer.COL_NAME + "=?", new String[]{name}, null, null, null);
		
		cursor.moveToFirst();
		
		meters = cursor.getFloat(0);
		
		cursor.close();
		return meters;	
		
	}
	
	/**
	 * Checks, if a roundghost with the given parameters has been stored into the database.
	 * @param name The track name.
	 * @param rounds The rounds the ghost has been stored with.
	 * @return true If a ghost has been found in the database, false if not.
	 */
	public boolean isRoundGhostPresent(String name, int rounds){
		
		boolean isPresent;		
		Cursor cursor = mDB.query(TblRoundGhost.NAME, new String[]{TblRoundGhost.COL_TIMES}, TblRoundGhost.COL_NAME + "=? AND " + TblRoundGhost.COL_ROUNDS + "=?", new String[]{name, ""+rounds}, null, null, null);
		isPresent = cursor.moveToFirst();
		cursor.close();
		
		if(isPresent)
			return true;		
		else
			return false;		
	}
	
	/**
	 * Checks, if a timerghost with the given parameters has been stored into the database.
	 * @param track The track name.
	 * @param time The number of minutes the ghost has been stored with.
	 * @return true If a ghost has been found in the database, false if not.
	 */
	public boolean isTimeGhostPresent(String track, int time) {
		
		boolean isPresent;
		Log.i("debug", "DbHelper: isTimeGhostPresent for, Trackname: " + track + " and Minutes: " + time);
		Cursor cursor = mDB.query(TblTimeGhost.NAME, new String[]{TblTimeGhost.COL_TIMES}, TblTimeGhost.COL_NAME + "=? AND " + TblTimeGhost.COL_TIME + "=?", new String[]{track, ""+time}, null, null, null);
		isPresent = cursor.moveToFirst();
		cursor.close();
		
		if(isPresent)
			return true;		
		else
			return false;		
	}
	
	/**
	 * This method gets a roundghost out of the database.
	 * @param track The track name.
	 * @param rounds The number of rounds that have been stored with the ghost.
	 * @return A list of the round_times represented as a String Array.
	 */
	public ArrayList<String> getRoundGhost(String track, int rounds) {
		String times;
		ArrayList<String> timesList;
		
		String whereClause = TblRoundGhost.COL_ROUNDS+"=? AND "+ TblRoundGhost.COL_NAME +"=?";
		
		Cursor cursor = mDB.query(TblRoundGhost.NAME, new String[]{TblRoundGhost.COL_TIMES}, whereClause, new String[]{""+rounds, track}, null, null, null);		
		cursor.moveToFirst();
		
		times = cursor.getString(0);
		timesList = new ArrayList<String>(Arrays.asList(times.split(";")));
		
		cursor.close();
		return timesList;		
	}
	
	/**
	 * This method gets a timerghost out of the database.
	 * @param name The track name.
	 * @param time The number of minutes that have been stored with the ghost.
	 * @return
	 */
	public ArrayList<String> getTimeGhost(String name, int time) {
		String times;
		ArrayList<String> timesList;
		
		String whereClause = TblTimeGhost.COL_TIME+"=? AND "+ TblTimeGhost.COL_NAME +"=?";
		
		Cursor cursor = mDB.query(TblTimeGhost.NAME, new String[]{TblTimeGhost.COL_TIMES}, whereClause, new String[]{""+time, name}, null, null, null);		
		cursor.moveToFirst();
		
		times = cursor.getString(0);
		timesList = new ArrayList<String>(Arrays.asList(times.split(";")));
		
		cursor.close();
		return timesList;		
	}
	
	/**
	 * This method has only to be called when the method isPlayerTrackPresent() returns true.
	 * The player-track-mode dataset is been updated with the given parameters.
	 * @param player The player name.
	 * @param track The track name.
	 * @param mode The racemode (roundmode oder timermode).
	 * @param win Did the player win the race?
	 * @param fastestround The fastest round the player has driven in the race.
	 * @param avgspeed The average speed the player has driven in the race.
	 * @param drivenmeters The driven meters the player has driven in the race.
	 */
	public void updatePlayerStats(String player, String track, int mode, boolean win, String fastestround, float avgspeed, float drivenmeters){		
		
		float lifetimemeters = getPlayerLifetimeMeters(player) + drivenmeters;
		int i_win;
		if(win)
			i_win= 1;
		else
			i_win = 0;
		ContentValues values = new ContentValues();
		
		values.put(TblPlayer.COL_LIFETIMEMETERS, lifetimemeters);
		
		mDB.update(TblPlayer.NAME, values, TblPlayer.COL_NAME + "=?", new String[]{player});
		
		values.clear();
		
		int attempt = getPlayerTrackAttempt(player,track, mode) + 1;
		i_win = getPlayerTrackWins(player,track,mode) + i_win;
		float wholedrivenmeters = getPlayerTrackWholeDrivenMeters(player, track, mode) + drivenmeters;
		float wholeavgspeed = ((getPlayerTrackWholeDrivenMeters(player, track, mode) + drivenmeters) / ((getPlayerTrackWholeDrivenMeters(player, track, mode) / getPlayerTrackWholeAverageSpeed(player, track, mode)) + (drivenmeters /avgspeed)));
				
		values.put(TblPlayer_Track.COL_ATTEMPT, attempt);
		values.put(TblPlayer_Track.COL_WINS, i_win);
		values.put(TblPlayer_Track.COL_FASTESTROUND, fastestround);
		values.put(TblPlayer_Track.COL_LASTAVERAGESPEED, avgspeed);
		values.put(TblPlayer_Track.COL_WHOLEAVERAGESPEED, wholeavgspeed);
		values.put(TblPlayer_Track.COL_LASTDRIVENMETERS, drivenmeters);
		values.put(TblPlayer_Track.COL_WHOLEDRIVENMETERS, wholedrivenmeters);
		
		String whereClause = TblPlayer_Track.COL_PLAYERNAME + "=? AND " + TblPlayer_Track.COL_TRACKNAME + "=? AND " + TblPlayer_Track.COL_MODE + "=?";
		mDB.update(TblPlayer_Track.NAME, values, whereClause, new String[]{player, track, ""+mode});
		
		
	}

	/**
	 * Gets the attempt the player made on the combination of player-track-mode.
	 * @param player The player name.
	 * @param track THe track name.
	 * @param mode The racemode (roundmode or timermode)
	 * @return The attempt as a decimal number.
	 */
	public int getPlayerTrackAttempt(String player, String track, int mode) {
		
		int attempt;
		String whereClause = TblPlayer_Track.COL_PLAYERNAME + "=? AND " + TblPlayer_Track.COL_TRACKNAME + "=? AND " + TblPlayer_Track.COL_MODE + "=?";
		
		Cursor cursor = mDB.query(TblPlayer_Track.NAME, new String[]{TblPlayer_Track.COL_ATTEMPT}, whereClause, new String[]{player, track, ""+mode}, null, null, null);
		
		cursor.moveToFirst();
		
		attempt = cursor.getInt(0);		
		
		cursor.close();
		return attempt;
	}
	
	/**
	 * Gets the wins the player made on the combination of player-track-mode.
	 * @param player The player name.
	 * @param track The track name.
	 * @param mode The racemode(roundmode or timermode).
	 * @return The wins as a decimal number.
	 */
	public int getPlayerTrackWins(String player, String track, int mode) {
		
		int wins;
		String whereClause = TblPlayer_Track.COL_PLAYERNAME + "=? AND " + TblPlayer_Track.COL_TRACKNAME + "=? AND " + TblPlayer_Track.COL_MODE + "=?";
		
		Cursor cursor = mDB.query(TblPlayer_Track.NAME, new String[]{TblPlayer_Track.COL_WINS}, whereClause, new String[]{player, track, ""+mode}, null, null, null);
		
		cursor.moveToFirst();
		
		wins = cursor.getInt(0);		
		
		cursor.close();
		return wins;
	}
	
	/**
	 * Gets the whole meters the player has driven on the combination of player-track-mode.
	 * @param player The player name.
	 * @param track The track name.
	 * @param mode The racemode (roundmode or timermode)
	 * @return The whole driven meters in meters.
	 */
	public float getPlayerTrackWholeDrivenMeters(String player, String track, int mode){
		
		float wholedrivenmeters;
		
		String whereClause = TblPlayer_Track.COL_PLAYERNAME + "=? AND " + TblPlayer_Track.COL_TRACKNAME + "=? AND " + TblPlayer_Track.COL_MODE + "=?";
		
		Cursor cursor = mDB.query(TblPlayer_Track.NAME, new String[]{TblPlayer_Track.COL_WHOLEDRIVENMETERS}, whereClause, new String[]{player, track, ""+mode}, null, null, null);
		
		cursor.moveToFirst();
		
		wholedrivenmeters = cursor.getFloat(0);		
		
		cursor.close();
		return wholedrivenmeters;	
	}
	
	/**
	 * Gets the whole average speed the player did in all races in combination of player-track-mode.
	 * @param player The player name.
	 * @param track The track name.
	 * @param mode The racemode(roundmode or timermode).
	 * @return The whole average speed as a floating point number.
	 */
	public float getPlayerTrackWholeAverageSpeed(String player, String track, int mode){
		
		float wholeavgspeed;
		
		String whereClause = TblPlayer_Track.COL_PLAYERNAME + "=? AND " + TblPlayer_Track.COL_TRACKNAME + "=? AND " + TblPlayer_Track.COL_MODE + "=?";
		
		Cursor cursor = mDB.query(TblPlayer_Track.NAME, new String[]{TblPlayer_Track.COL_WHOLEAVERAGESPEED}, whereClause, new String[]{player, track, ""+mode}, null, null, null);
		
		cursor.moveToFirst();
		
		wholeavgspeed = cursor.getFloat(0);	
		
		cursor.close();
		return wholeavgspeed;	
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}	

}
