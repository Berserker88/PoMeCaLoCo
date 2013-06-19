package de.freinsberg.pomecaloco;

import java.util.List;

import android.os.CountDownTimer;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MyTimer extends CountDownTimer{
	
	public int mCountdown;
	public TextView mTv;
	public List<String> mCountdownValues;

	private MyTimer(long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
		// TODO Automatisch generierter Konstruktorstub
	}
	public MyTimer(long millisInFuture, long countDownInterval, List<String> countdownvalues, TextView tv) {
		super(millisInFuture, countDownInterval);
		mCountdownValues = countdownvalues;		
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
			mTv.setText("%H:%M:%S");
		}		
	}	
}
