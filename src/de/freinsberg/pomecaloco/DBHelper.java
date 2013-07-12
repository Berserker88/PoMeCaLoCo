package de.freinsberg.pomecaloco;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import org.opencv.core.Mat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper{
	
	private static final String DB_NAME = "pomecaloco.db";
	private static final int DB_VERSION = 1;
	private Context mContext;	
	private SQLiteDatabase mDB;
	
	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		mContext = context;
		
	}
	//following section declares the database layout
	
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
		private static final String COL_FASTESTROUND = "fastestround";
		private static final String COL_LASTDRIVENMETERS = "lastdrivenmeters";		
		private static final String COL_LASTAVERAGESPEED = "lastaveragespeed";
		private static final String COL_WHOLEAVERAGESPEED = "wholeaveragespeed";		
	}
	private class TblPlayer {
		
		private static final String NAME = "player";
		private static final String COL_NAME = "name";
		private static final String COL_COLOR = "color";
		private static final String COL_INTCOLOR = "intcolor";
		private static final String COL_WHOLEDRIVENMETERS ="wholedrivenmeters";
	}
	
	private class TblRoundGhost {
		
		private static final String NAME = "roundghost";
		private static final String COL_NAME = "track_name";		
		private static final String COL_ROUNDS = "rounds";		
		private static final String COL_TIMES = "times";
	}
	
	private class TblTimeGhost {
		
		private static final String NAME = "timeghost";
		private static final String COL_NAME = "track_name";
		private static final String COL_TIME = "time";
		private static final String COL_TIMES = "times";
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
				+TblPlayer.COL_COLOR + " VARCHAR(16),"
				+TblPlayer.COL_INTCOLOR + " INTEGER, "
				+ TblPlayer.COL_WHOLEDRIVENMETERS + " FLOAT(5))"
			);
		
		//Creating the RoundGhost Table
		db.execSQL(
				"CREATE TABLE " + TblRoundGhost.NAME + 
				"(" + TblRoundGhost.COL_NAME + " VARCHAR(30), "							
				+TblRoundGhost.COL_ROUNDS + " INTEGER, "				
				+TblRoundGhost.COL_TIMES + " VARCHAR(10000), "
				+ "PRIMARY KEY (" + TblRoundGhost.COL_NAME + ", " + TblRoundGhost.COL_ROUNDS + "), "
				+ "FOREIGN KEY (" + TblRoundGhost.COL_NAME+ ") REFERENCES " + TblTrack.NAME + "(" + TblTrack.COL_NAME + "))"
			);
		
		//Creating the TimeGhost Table
		db.execSQL(
				"CREATE TABLE " + TblTimeGhost.NAME + 
				"(" + TblTimeGhost.COL_NAME + " VARCHAR(30), "	
				+TblTimeGhost.COL_TIME + " INTEGER, "
				+TblTimeGhost.COL_TIMES + " VARCHAR(10000), "
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
				+ TblPlayer_Track.COL_FASTESTROUND + " VARCHAR(9),"
				+ TblPlayer_Track.COL_LASTDRIVENMETERS + " FLOAT(5),"				
				+ TblPlayer_Track.COL_LASTAVERAGESPEED + " FLOAT(5),"
				+ TblPlayer_Track.COL_WHOLEAVERAGESPEED + " FLOAT(5),"
				+ "PRIMARY KEY (" + TblPlayer_Track.COL_PLAYERNAME + ", " + TblPlayer_Track.COL_TRACKNAME + ", " + TblPlayer_Track.COL_MODE + "), "
				+ "FOREIGN KEY (" + TblPlayer_Track.COL_PLAYERNAME + ")  REFERENCES " + TblTrack.NAME + "(" + TblTrack.COL_NAME + "), "
				+ "FOREIGN KEY (" + TblPlayer_Track.COL_TRACKNAME + ")  REFERENCES " + TblPlayer.NAME + "(" + TblPlayer.COL_NAME + "))"
			);
			mDB = db;
			initTracks();
	}

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
	
	public void createPlayer(String name, String color, int int_color, int wholedrivenmeters){
		mDB.execSQL(
				"INSERT INTO "+ TblPlayer.NAME + "(" 
				+ TblPlayer.COL_NAME + ", " 				
				+ TblPlayer.COL_COLOR + ", " 
				+ TblPlayer.COL_INTCOLOR + ", "
				+ TblPlayer.COL_WHOLEDRIVENMETERS + ")"
				+ "VALUES('" + name + "', " + color + "', " + int_color + ", " + wholedrivenmeters +")"
			);
	}
		
	private void createTrack(String name, boolean iscrossed, float length, byte[] image){
		Log.i("debug", "Byte[] before insertion into tracks: " + image);
		Log.i("debug", "mDb: " + mDB);
		ContentValues vals = new ContentValues();
		vals.put(TblTrack.COL_NAME, name);
		vals.put(TblTrack.COL_ISCROSSED, iscrossed);
		vals.put(TblTrack.COL_LENGTH, length);
		vals.put(TblTrack.COL_IMAGE, image);
		mDB.insert(TblTrack.NAME, null, vals);
//		mDB.execSQL(
//				"INSERT INTO " + TblTrack.NAME + "(" 
//				+ TblTrack.COL_NAME + ", " 
//				+ TblTrack.COL_ISCROSSED + ", " 
//				+ TblTrack.COL_LENGTH + ", " 
//				+ TblTrack.COL_IMAGE + ")"
//				+ "VALUES('" + name + "', " + iscrossed + ", '" + length + "', " + image + ")"
//			);
	}
	
	public void createRoundGhost(String name, int rounds, String times) {
		
		mDB.execSQL(
				"INSERT INTO " + TblRoundGhost.NAME + "("
				+ TblRoundGhost.COL_NAME + ", " + TblRoundGhost.COL_ROUNDS + ", " +  TblRoundGhost.COL_TIMES + ")"
				+ "VALUES('" + name + "', " + rounds + ", '" + times + "')"				
			);
	}
	
	public void createTimeGhost(String name, int time, String times) {
		
		mDB.execSQL(
				"INSERT INTO " + TblTimeGhost.NAME + "("
				+ TblTimeGhost.COL_NAME + ", " + TblTimeGhost.COL_TIME + ", " + TblTimeGhost.COL_TIMES + ")"
				+ "VALUES('" + name + "', " + time + "', '" + times + "')"				
			);
	}
	
	public void createPlayerTrack(String playername, String trackname, int mode, int attempt, String fastestround, float lastaveragespeed, float wholeaveragespeed, float lastdrivenmeters){
		
		mDB.execSQL(
				"INSERT INTO "+ TblPlayer_Track.NAME + "("
				+ TblPlayer_Track.COL_PLAYERNAME + ", "
				+ TblPlayer_Track.COL_TRACKNAME + ", "
				+ TblPlayer_Track.COL_MODE + ", "
				+ TblPlayer_Track.COL_ATTEMPT + ", "
				+ TblPlayer_Track.COL_FASTESTROUND + ", "
				+ TblPlayer_Track.COL_LASTAVERAGESPEED + ", "
				+ TblPlayer_Track.COL_WHOLEAVERAGESPEED + ", "
				+ TblPlayer_Track.COL_LASTDRIVENMETERS + ", "				
				+ "VALUES('" + playername + "', '" + trackname + "', " + mode + ", " + attempt + ", '" + fastestround + "', '" + lastaveragespeed + "','" + wholeaveragespeed + "','" + lastdrivenmeters + ")"
			);
	}
	
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
	
	public float getTrackLength(String name){
		Log.i("debug", "Get Track Length for: "+ name);
		float length;
		
		Cursor cursor = mDB.query(TblTrack.NAME, new String[]{TblTrack.COL_LENGTH}, TblTrack.COL_NAME + "=?", new String[]{name}, null, null, null);
		
		cursor.moveToFirst();
		
		length = cursor.getFloat(0);
		
		return length;
		
	}
	
	public ArrayList<String> getAllPlayernames(){
		ArrayList<String> names = new ArrayList<String>();
		Cursor cursor = mDB.query(TblPlayer.NAME, new String[]{TblPlayer.COL_NAME}, null, null, null, null, TblPlayer.COL_NAME);
		
		cursor.moveToFirst();
		
		while(!cursor.isAfterLast()){
			
			names.add(cursor.getString(0));
			cursor.moveToNext();			
		}
		return names;
	}
	
	public String getPlayerColor(String name){
		String color;
		
		Cursor cursor = mDB.query(TblPlayer.NAME, new String[]{TblPlayer.COL_COLOR}, TblPlayer.COL_NAME + "=?", new String[]{name}, null, null, null);
		
		cursor.moveToFirst();
		
		color = cursor.getString(0);
		
		return color;		
	}
	
	public int getPlayerIntColor(String name){
		int intcolor;
		
		Cursor cursor = mDB.query(TblPlayer.NAME, new String[]{TblPlayer.COL_INTCOLOR}, TblPlayer.COL_NAME + "=?", new String[]{name}, null, null, null);
		
		cursor.moveToFirst();
		
		intcolor = cursor.getInt(0);
		
		return intcolor;		
	}
	
	public float getPlayerWholeDrivenMeters(String name) {
		float meters;
		
		Cursor cursor = mDB.query(TblPlayer.NAME, new String[]{TblPlayer.COL_WHOLEDRIVENMETERS}, TblPlayer.COL_NAME + "=?", new String[]{name}, null, null, null);
		
		cursor.moveToFirst();
		
		meters = cursor.getFloat(0);
		
		return meters;	
		
	}
	
	public String getRoundGhost(String track, int rounds) {
		String times;
		
		String whereClause = TblRoundGhost.COL_ROUNDS+"=? AND "+ TblRoundGhost.COL_NAME +"=?";
		
		Cursor cursor = mDB.query(TblRoundGhost.NAME, new String[]{TblRoundGhost.COL_TIMES}, whereClause, new String[]{""+rounds, track}, null, null, null);		
		cursor.moveToFirst();
		
		times = cursor.getString(0);
		
		return times;		
	}
	
	public String getTimeGhost(String track, int time) {
		String times;
		
		String whereClause = TblTimeGhost.COL_TIME+"='?' AND "+ TblTimeGhost.COL_NAME +"=?";
		
		Cursor cursor = mDB.query(TblTimeGhost.NAME, new String[]{TblTimeGhost.COL_TIMES}, whereClause, new String[]{""+time, track}, null, null, null);		
		cursor.moveToFirst();
		
		times = cursor.getString(0);
		
		return times;		
	}

	
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Automatisch generierter Methodenstub
		
	}
	
	

}
