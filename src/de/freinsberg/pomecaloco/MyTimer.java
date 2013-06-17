package de.freinsberg.pomecaloco;

import java.util.List;

import android.os.CountDownTimer;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MyTimer extends CountDownTimer{
	
	public int mCountdowncounter;
	public TextView mTv;
	public List<String> mCountdownvalues;

	private MyTimer(long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
		// TODO Automatisch generierter Konstruktorstub
	}
	public MyTimer(long millisInFuture, long countDownInterval, List<String> countdownvalues, TextView tv) {
		super(millisInFuture, countDownInterval);
		mCountdownvalues = countdownvalues;		
		mTv = tv;
	}
	public MyTimer(long millisInFuture, long countDownInterval, TextView tv) {
		super(millisInFuture, countDownInterval);			
		mTv = tv;
	}
	@Override
	public void onFinish() {
		mTv.setText(" ");
	}

	@Override
	public void onTick(long millisUntilFinished) {
		if(mCountdownvalues != null){
			Log.i("debug", "Size: "+mCountdownvalues.size()+", Counter: "+mCountdowncounter);
			if(mCountdowncounter >= mCountdownvalues.size()) {
				cancel();
				return;
			}
			mTv.setTextColor(mTv.getResources().getColor(R.color.winning_green));
			mTv.setText(mCountdownvalues.get(mCountdowncounter));						
			mCountdowncounter++;
		}
		else
		{
			Time timer = new Time();
			timer.set(0, 0, 17, 0, 0, 0);
			mTv.setTextColor(mTv.getResources().getColor(R.color.white));
			mTv.setText(timer.format("%H:%M:%S"));
		}
		
	}
	
	
}
