package de.freinsberg.pomecaloco;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

/**
 * This class is used to create the commonly known camera-flash on a view in the ui.
 * @author freinsberg
 *
 */
public class MyShotTask {
	int mResid;
	Activity mActivity;
	Timer mShotTimer = new Timer();
	Handler mHandler = new Handler();
	private TimerTask mShotTask;
	float mAlpha;
	int mAlphaCounter = 100;
	
	/**
	 * Constructor: Creates an object for the camera-flash task that operates on an ui view object.
	 * @param a The Activity where the task is placed.
	 * @param resid The resourceid of the view object where the camera-flash is performed.
	 */
	public MyShotTask(Activity a, int resid){
		Log.i("debug", "New ShotTaskCreated");
		mResid = resid;
		mActivity = a;
		createShotTask();
	}
	
	private void createShotTask(){	

		final ImageView image = (ImageView) mActivity.findViewById(mResid);
		
		mShotTask = new TimerTask() {	
		
			@Override
			public void run() {
			
			mHandler.post(new Runnable() {								
					@Override
					public void run() {
						if(mAlphaCounter>=1)
						{										
							mAlpha = (float) (mAlphaCounter/100.0);
//							Log.i("debug","Alpha: "+mAlpha);
							image.setAlpha(mAlpha);											
						
						mAlphaCounter--;
						}								
							mShotTimer.cancel();
							mShotTimer.purge();
							
						}														
					});							
			}
		};		
	}
	
	/**
	 * Performs the shot task.
	 */
	public void perform(){				
		mShotTimer.schedule(mShotTask, 5,5);		
		mShotTimer = new Timer();
	}
}
