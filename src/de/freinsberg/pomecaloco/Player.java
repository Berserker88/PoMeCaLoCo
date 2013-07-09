package de.freinsberg.pomecaloco;

import org.opencv.core.Scalar;

import android.graphics.Color;
import android.util.Log;

public class Player {
	
	public static final int ROUND_MODE = 1;
	public static final int TIMER_MODE = 2;
	public static final int LEFT_LANE = 1;
	public static final int RIGHT_LANE = 2;
	private String mName;
	private int mLane;
	private int mMode;
	private double[] mRGB;
	private Scalar mColor;
	private Color mRGBColor;
	private int mRGBIntColor;
	
	
	public Player(int lane, int mode, Scalar color, String name){
		mName = name;
		mLane = lane;
		mMode = mode;
		mColor = color;
		mRGB = color.val;		
		mRGBColor = new Color();
		mRGBIntColor = mRGBColor.rgb((int) mRGB[0], (int) mRGB[1], (int) mRGB[2]);
		Log.i("debug", "Spieler- Name: "+mName);
		Log.i("debug", "Spieler- Spur: "+mLane);
		Log.i("debug","Spieler- Spielmodus: "+mMode);
		Log.i("debug","Spieler- Spielerfarbe: "+mColor);
	}
	
	
	public String getName(){
		return mName;
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
	
	public int getRGBColor(){
		return mRGBIntColor;
	}
	
	
	
	

}
