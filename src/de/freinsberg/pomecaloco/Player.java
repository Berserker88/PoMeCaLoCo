package de.freinsberg.pomecaloco;

import org.opencv.core.Scalar;

import android.graphics.Color;
import android.util.Log;

/**
 * This class represents a Player
 * @author freinsberg
 *
 */
public class Player {
	
	private String mName;
	private int mLane;
	private int mMode;
	private double[] mRGB;
	private Scalar mColor;
	private Color mRGBColor;
	private int mRGBIntColor;
	
	/**
	 * Constructor: Creates a Player for a lane, mode, color and a name.
	 * Creating an integer-representative of players color. 
	 * @param name The name of the player.
	 * @param lane The lane on which the player drives.
	 * @param mode The game mode the Player plays.
	 * @param color The color of the player

	 */
	public Player(String name, int lane, int mode, Scalar color){
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
	
	/**
	 * This Method gets the player name.
	 * @return The player name.
	 */
	public String getName(){
		return mName;
	}
	
	/**
	 * This Methods gets the Lane of which the player drives.
	 * @return The lane.
	 */
	public int getLane(){
		
		return mLane;
	}
	
	/**
	 * This Method gets the game mode for this player.
	 * @return The game mode.
	 */
	public int getMode(){
		
		return mMode;	
	}
	
	/**
	 * This Method gets the color of this player as a Scalar.
	 * @return The Scalar color.
	 */
	public Scalar getColor(){		
		return mColor;
	}
	
	/**
	 * This Method gets the color of this Player as an Integer-representative.
	 * @return The integer-representative of the rgbcolor.
	 */
	public int getRGBColor(){
		return mRGBIntColor;
	}
	
	
	
	

}
