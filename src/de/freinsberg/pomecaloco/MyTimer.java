package de.freinsberg.pomecaloco;

import java.util.List;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;

/**
 * 
 * @author freinsberg
 *
 */
public class MyTimer extends CountDownTimer{
	
	private int mCountdown = 0;
	private long mMsCountdown;	
	private boolean mIsFinished = false;
	private boolean isRaceCountdown = false;
	@SuppressWarnings("unused")
	private boolean isTimerRace = false;
	
	public TextView mTv;
	public List<String> mCountdownValues;

	private MyTimer(long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);		
	}
	public MyTimer(long millisInFuture, long countDownInterval, List<String> countDownValues, TextView tv) {		
		super(millisInFuture, countDownInterval);
		isRaceCountdown = true;
		mIsFinished = false;
		Log.i("debug", "Im Timer für den Countdown zum Rennen");
		mCountdownValues = countDownValues;		
		mTv = tv;
	}
	public MyTimer(long millisInFuture, long countDownInterval, TextView tv) {		
		super(millisInFuture, countDownInterval);
		Log.i("debug", "Im Timer fürs Rennen!!!");
		mMsCountdown = millisInFuture;
		mTv = tv;
	}
	@Override
	public void onFinish() 
	{
		if(isRaceCountdown)
		{
			mTv.setText(" ");	
			isTimerRace = true;
			Race.getInstance().go();
		}			
		else 
		{
			Log.i("debug", "Timer is finished in >Timer!");
			mTv.setText("00:00:00");
			mIsFinished = true;
		}	
	}

	@Override
	public void onTick(long millisUntilFinished) {
			//Log.i("debug", "timer tick tick");
			if(mCountdownValues != null)
			{			
				Log.i("debug", "Size: "+mCountdownValues.size()+", Counter: "+mCountdown);
				if(mCountdown >= mCountdownValues.size()) {
					cancel();
					return;
				}			
				mTv.setTextColor(mTv.getResources().getColor(R.color.record_yellow));
				mTv.setText(mCountdownValues.get(mCountdown));						
				mCountdown++;
			}
			else
			{
				String msUntilFinished = (parse(millisUntilFinished));
				if(Race.getInstance().mGhostMode)
					Race.getInstance().checkGhostTime(msUntilFinished);
				mTv.setTextColor(mTv.getResources().getColor(R.color.white));				
				mTv.setText(msUntilFinished);				
				mMsCountdown = millisUntilFinished;
				
			}		
		
	}	
	
	public String getCurrentTime() {
		return (parse(mMsCountdown));
	}
	
	public void stop() {
		cancel();	
	}
	
	public boolean isFinished(){
		return mIsFinished;
	}
	
	private String parse(long l) {
		String parsed;
		String[] splits;
		parsed = "00:00:00";
		splits = parsed.split(":");
		if(splits.length < 3){
			Log.i("debug", "Interner Parsingfehler");
			return null;
		}
		//l comes as Milliseconds, we want to use hundreds per second so divide by 10.
		
		l = l /10;		
		int minutes=0;
		int seconds=0;
		int hundreds=0;
		if(l >= 6000){
			minutes = (int) (l/6000);			
			if(minutes < 10){				
				splits[0] = ("0"+minutes);
			}else
				splits[0] = Integer.toString(minutes);				
		}
		if(l >= 100) {
			seconds = (int) (l/100);				
			seconds = seconds - (minutes*60);			
			if(seconds < 10){								
				splits[1] = ("0"+seconds);	
			}else
				splits[1] = Integer.toString(seconds);
		}
		if(l >= 1) {
			hundreds = (int) l;			
			hundreds = hundreds - (seconds*100);
			hundreds = hundreds -(minutes*6000);			
			if(hundreds < 10){				
				splits[2] = ("0"+hundreds);	
			}else
				splits[2] = Integer.toString(hundreds);			
		}
		parsed = splits[0]+":"+splits[1]+":"+splits[2];
		
		return parsed;		
	}
}
