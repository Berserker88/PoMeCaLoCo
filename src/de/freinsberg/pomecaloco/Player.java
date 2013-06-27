package de.freinsberg.pomecaloco;

import org.opencv.core.Scalar;

import android.util.Log;

public class Player {
	
	public static final int ROUND_MODE = 1;
	public static final int TIMER_MODE = 2;
	public static final int LEFT_LANE = 1;
	public static final int RIGHT_LANE = 2;
	private int mLane;
	private int mMode;
	private Scalar mColor;
	
	public Player(int lane, int mode, Scalar color){
		mLane = lane;
		mMode = mode;
		mColor = color;
		Log.i("debug", "Spieler- Spur: "+mLane);
		Log.i("debug","Spieler- Spielmodus: "+mMode);
		Log.i("debug","Spieler- Spielerfarbe: "+mColor);
	}
	
	public int getLane(){
		
		if(mLane == LEFT_LANE)		
				return LEFT_LANE;
		else
			return RIGHT_LANE;
	}
	
	public int getMode(){
		
		if(mMode == ROUND_MODE)		
			return ROUND_MODE;
		else
			return TIMER_MODE;		
	}
	
	public Scalar getColor(){		
		return mColor;
	}
	
	

}
