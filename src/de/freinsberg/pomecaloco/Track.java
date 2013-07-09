package de.freinsberg.pomecaloco;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import android.graphics.Bitmap;

/**
 * This class represents a Track.
 * @author freinsberg
 *
 */
public class Track {
	private String mName;
	private boolean mIsCrossed;
	private double mLength;
	private Mat mImage;
	
	/**
	 * Constructor: Creates a Track with a given name, logical value of crossed, length and an image.
	 * @param name The name of the track.
	 * @param isCrossed Do the lane Cross each other every Round.
	 * @param length The lenght of the track in meter.
	 * @param image An Image of the track.
	 */
	public Track (String name, boolean isCrossed, double length, Mat image){
		this.mName = name;
		this.mIsCrossed = isCrossed;
		this.mLength = length;
		this.mImage = image;		
	}
	/**
	 * This Method gets the track name
	 * @return The track name
	 */
	public String getName(){
		return mName;
	}
	
	/**
	 * This Method gets logical value of the crossed status for the track.
	 * @return TRUE if track is crossed, FALSE if track is NOT crossed.
	 */
	public boolean isCrossed(){
		if(mIsCrossed)
			return true;
		else
			return false; 
	}
	
	/**
	 * This Method gets the length of the track in meter.
	 * @return The length in meter.
	 */
	public double getLength(){
		return mLength;
	}
	
	/**
	 * This Method gets the image of the track
	 * @return The Bitmap containing an image of the track.
	 */
	public Bitmap getImage(){
		Bitmap image = Bitmap.createBitmap(mImage.cols(), mImage.rows(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(mImage, image);		
		return image;
	}
}
