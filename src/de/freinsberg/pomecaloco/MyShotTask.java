package de.freinsberg.pomecaloco;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

public class MyShotTask {
	int mResid;
	Activity mActivity;
	Timer mShotTimer = new Timer();
	Handler mHandler = new Handler();
	private TimerTask mShotTask;
	float mAlpha;
	int mAlphaCounter = 100;
	
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
							Log.i("debug","Alpha: "+mAlpha);
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
	
	public void perform(){				
		mShotTimer.schedule(mShotTask, 5,5);		
		mShotTimer = new Timer();

	}
	
	

}
