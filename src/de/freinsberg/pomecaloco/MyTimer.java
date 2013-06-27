package de.freinsberg.pomecaloco;

import java.text.DecimalFormat;
import java.util.List;

import android.os.CountDownTimer;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MyTimer extends CountDownTimer{
	
	public int mCountdown = 0;
	public double mMsCountdown;
	public boolean isRaceCountdown = false;
	public boolean isTimerRace = false;
	public DecimalFormat mFormat = new DecimalFormat("00.00");
	public TextView mTv;
	public List<String> mCountdownValues;

	private MyTimer(long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
		// TODO Automatisch generierter Konstruktorstub
	}
	public MyTimer(long millisInFuture, long countDownInterval, List<String> countdownvalues, TextView tv) {		
		super(millisInFuture, countDownInterval);
		isRaceCountdown = true;
		Log.i("debug", "Im Timer für den Countdown zum Rennen");
		mCountdownValues = countdownvalues;		
		mTv = tv;
	}
	public MyTimer(long millisInFuture, long countDownInterval, TextView tv) {		
		super(millisInFuture, countDownInterval);
		isTimerRace = true;
		Log.i("debug", "Im Timer fürs Rennen!!!");
		mMsCountdown = millisInFuture;
		mTv = tv;
	}
	@Override
	public void onFinish() {
		if(isRaceCountdown){
			mTv.setText(" ");
			isRaceCountdown = false;
			if(Race.mRaceTimer != null)
				Race.mRaceTimer.start();
		}
		else if(isTimerRace){
			mTv.setText("00.00.00");
			isTimerRace = false;
			Race.processResults();
			
		}
		
	}

	@Override
	public void onTick(long millisUntilFinished) {
		if(mCountdownValues != null){			
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
			mTv.setTextColor(mTv.getResources().getColor(R.color.white));
			
			mTv.setText(mFormat.format(mMsCountdown));
			mMsCountdown = millisUntilFinished;
			
		}		
	}	
}
