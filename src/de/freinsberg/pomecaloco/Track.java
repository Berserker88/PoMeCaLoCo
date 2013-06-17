package de.freinsberg.pomecaloco;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import android.graphics.Bitmap;

public class Track {
	private String mName;
	private boolean mIsCrossed;
	private double mLength;
	private Mat mImage;
	
	public Track (String name, boolean isCrossed, double length, Mat image){
		this.mName = name;
		this.mIsCrossed = isCrossed;
		this.mLength = length;
		this.mImage = image;		
	}
	
	public String getName(){
		return mName;
	}
	
	public boolean isCrossed(){
		if(mIsCrossed)
			return true;
		else
			return false; 
	}
	
	public double getLength(){
		return mLength;
	}
	
	public Bitmap getImage(){
		Bitmap image = Bitmap.createBitmap(mImage.cols(), mImage.rows(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(mImage, image);		
		return image;
	}
}
