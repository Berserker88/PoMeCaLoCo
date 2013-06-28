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
	public long mMsCountdown;
	public boolean isRaceCountdown = false;
	public boolean isTimerRace = false;
	
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
			mTv.setText("00:00:00");
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
			
			mTv.setText((parse(mMsCountdown)));
			mMsCountdown = millisUntilFinished;
			
		}		
	}	
	public String parse(long l) {
		String parsed;
		String[] splits;
		parsed = "00:00:00";
		splits = parsed.split(":");
		if(splits.length < 3){
			Log.i("debug", "Interner Parsingfehler");
			return null;
		}
		//l comes as Milliseconds, we want to use hundreds per second so divide by 10.
		Log.i("debug","ms to parse:"+l);
		l = l /10;		
		int minutes=0;
		int seconds=0;
		int hundreds=0;
		if(l >= 6000){
			minutes = (int) (l/6000);			
			if(minutes < 10)
				Log.i("debug","minuten < 10");
				splits[0] = "0"+Integer.toString(minutes);
			splits[0] = Integer.toString(minutes);				
		}
		if(l >= 100) {
			seconds = (int) (l/100);	
			Log.i("debug", "Minutes while calculating Seconds: "+minutes);
			seconds = seconds - (minutes*60);
			Log.i("debug", "Seconds: "+seconds);
			if(seconds < 10)				
				Log.i("debug","sekunden < 10");
				splits[1] = "0"+Integer.toString(seconds);	
			splits[1] = Integer.toString(seconds);
		}
		if(l >= 1) {
			hundreds = (int) l;			
			hundreds = hundreds - (seconds*100);
			hundreds = hundreds -(minutes*6000);
			Log.i("debug", "Hundreds: "+hundreds);
			if(hundreds < 10)
				Log.i("debug","hundertstel < 10");
				splits[2] = "0"+Integer.toString(hundreds);	
			splits[2] = Integer.toString(hundreds);			
		}
		parsed = splits[0]+":"+splits[1]+":"+splits[2];
		Log.i("debug", "parsed String: "+parsed);
		return parsed;		
	}
}
