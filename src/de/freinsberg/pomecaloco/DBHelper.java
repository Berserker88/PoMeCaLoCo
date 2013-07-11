package de.freinsberg.pomecaloco;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{
	
	private static final String DB_NAME = "pomecaloco.db";
	private static final int DB_VERSION = 1;

	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		
	}
	//following section declares the database layout
	
	private class TblTracks {
		
		private static final String NAME = "tracks";
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
		private static final String COL_MODE = "mode";
		private static final String COL_LANE = "lane";
		private static final String COL_COLOR = "color";
		private static final String COL_INTCOLOR = "intcolor";	
	}
	
	private class TblGhost {
		
		private static final String NAME = "track_name";
		private static final String COL_MODE = "mode";
		private static final String COL_LANE = "lane";
		private static final String COL_ROUNDS = "rounds";
		private static final String COL_TIMES = "times";
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Automatisch generierter Methodenstub
		
	}

}
