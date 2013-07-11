package de.freinsberg.pomecaloco;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper{
	
	private static final String DB_NAME = "pomecaloco.db";
	private static final int DB_VERSION = 1;

	private SQLiteDatabase mDB;
	
	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		
	}
	//following section declares the database layout
	
	private class TblTracks {
		
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
		private static final String COL_ATTEMPT = "attempt";
		private static final String COL_FASTESTROUND = "fastestround";
		private static final String COL_LASTDRIVENMETERS = "lastdrivenmeters";
		private static final String COL_WHOLEDRIVENMETERS = "wholedrivenmeters";
		private static final String COL_LASTAVERAGESPEED = "lastaveragespeed";
		private static final String COL_WHOLEAVERAGESPEED = "wholeaveragespeed";		
	}
	private class TblPlayer {
		
		private static final String NAME = "player";
		private static final String COL_NAME = "name";
		private static final String COL_MODE = "mode";
		private static final String COL_LANE = "lane";
		private static final String COL_COLOR = "color";
		private static final String COL_INTCOLOR = "intcolor";	
	}
	
	private class TblGhost {
		
		private static final String NAME = "ghost";
		private static final String COL_NAME = "track_name";
		private static final String COL_MODE = "mode";
		private static final String COL_LANE = "lane";
		private static final String COL_ROUNDS = "rounds";
		private static final String COL_TIMES = "times";
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i("debug", "DBHelper: onCreate()");
		
		//Creating the Tracks Table
		db.execSQL(
					"CREATE TABLE" + TblTracks.NAME + 
					"("+ TblTracks.COL_NAME + " VARCHAR(30) PRIMARY KEY,"
					+ TblTracks.COL_LENGTH + " INTEGER,"
					+ TblTracks.COL_ISCROSSED + " BOOLEAN,"
					+ TblTracks.COL_IMAGE + " BLOB)"				
				);
		
		//Creating the Player Table
		db.execSQL(
					"CREATE TABLE" + TblPlayer.NAME + 
					"("+ TblPlayer.COL_NAME + " VARCHAR(30) PRIMARY KEY,"
					+ TblPlayer.COL_MODE + " INTEGER,"
					+TblPlayer.COL_LANE + " INTEGER,"
					+TblPlayer.COL_COLOR + " VARCHAR(16),"
					+TblPlayer.COL_INTCOLOR + " INTEGER)"			
				);
		
		//Creating the Ghost Table
		db.execSQL(
				"CREATE TABLE" + TblGhost.NAME + 
				"(" + TblGhost.COL_NAME + " VARCHAR(30) PRIMARY KEY REFERENCES " + TblTracks.NAME + "(" + TblTracks.COL_NAME + "), "
				+ TblGhost.COL_MODE + " INTEGER,"
				+TblGhost.COL_LANE + " INTEGER,"
				+TblGhost.COL_ROUNDS + " INTEGER,"
				+TblGhost.COL_TIMES + " VARCHAR(10000))"
				);
		
		//Creating the Player-Track Table
		db.execSQL(
				"CREATE TABLE" + TblPlayer_Track.NAME +
				"(" + TblPlayer_Track.COL_PLAYERNAME + " VARCHAR(30) PRIMARY KEY REFERENCES " +TblTracks.NAME + "(" + TblTracks.COL_NAME + "), "
				+ TblPlayer_Track.COL_TRACKNAME + "VARCHAR(30) PRIMARY KEY REFERENCES " + TblPlayer.NAME + "(" + TblPlayer.COL_NAME + "), "
				+ TblPlayer_Track.COL_ATTEMPT + " INTEGER,"
				+ TblPlayer_Track.COL_FASTESTROUND + " VARCHAR(9),"
				+ TblPlayer_Track.COL_LASTDRIVENMETERS + " FLOAT(5),"
				+ TblPlayer_Track.COL_WHOLEDRIVENMETERS + " FLOAT(5),"
				+ TblPlayer_Track.COL_LASTAVERAGESPEED + " FLOAT(5),"
				+ TblPlayer_Track.COL_WHOLEAVERAGESPEED + " FLOAT(5),"
				);		
	}

	public void openDB(){
		
		mDB = getWritableDatabase();
	}
	private void initDatabase(){
		
		
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Automatisch generierter Methodenstub
		
	}
	
	

}
