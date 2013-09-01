package de.freinsberg.pomecaloco;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import java.text.DecimalFormat;

/**
 *  The Android chronometer widget revised so as to count milliseconds
 * @author antoniom and modified by freinsberg
 *
 */
public class MillisecondChronometer extends TextView {
    @SuppressWarnings("unused")
	private static final String TAG = "Chronometer";

    public interface OnChronometerTickListener {

        void onChronometerTick(MillisecondChronometer chronometer);
    }

    private long mBase;
    private boolean mVisible;
    private boolean mStarted;
    private boolean mRunning;    
    private OnChronometerTickListener mOnChronometerTickListener;

    private static final int TICK_WHAT = 2;

    private long mTimeElapsed;
    private String mTimeElapsedString;
    
    public MillisecondChronometer(Context context) {    	
        this (context, null, 0);
        Log.i("debug", "Konstruktor: Milliseconds Chronometer(context)");
    }

    public MillisecondChronometer(Context context, AttributeSet attrs) {
        this (context, attrs, 0);
        Log.i("debug", "Konstruktor: Milliseconds Chronometer(context, attrs)");
    }

    public MillisecondChronometer(Context context, AttributeSet attrs, int defStyle) {
        super (context, attrs, defStyle);
        init();
        
        Log.i("debug", "Konstruktor: Milliseconds Chronometer(context,attrs, defstyle)");
    }

    private void init() {
        mBase = SystemClock.elapsedRealtime();
        updateText(mBase);
    }

    public void setBase(long base) {
        mBase = base;
        dispatchChronometerTick();
        updateText(SystemClock.elapsedRealtime());
    }

    public long getBase() {
        return mBase;
    }

    public void setOnChronometerTickListener(
            OnChronometerTickListener listener) {
        mOnChronometerTickListener = listener;
    }

    public OnChronometerTickListener getOnChronometerTickListener() {
        return mOnChronometerTickListener;
    }

    public void start() {
        mBase = SystemClock.elapsedRealtime();
        mStarted = true;
        updateRunning();
    }

    public void stop() {
        mStarted = false;
        updateRunning();
    }


    public void setStarted(boolean started) {
        mStarted = started;
        updateRunning();
    }

    @Override
    protected void onDetachedFromWindow() {
        super .onDetachedFromWindow();
        mVisible = false;
        updateRunning();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super .onWindowVisibilityChanged(visibility);
        mVisible = visibility == VISIBLE;
        updateRunning();
    }

    private synchronized void updateText(long now) {
        mTimeElapsed = now - mBase;
        
        DecimalFormat df = new DecimalFormat("00");
        
        int hours = (int)(mTimeElapsed / (3600 * 1000));
        int remaining = (int)(mTimeElapsed % (3600 * 1000));
        
        int minutes = (int)(remaining / (60 * 1000));
        remaining = (int)(remaining % (60 * 1000));
        
        int seconds = (int)(remaining / 1000);
        remaining = (int)(remaining % (1000));
        
        int milliseconds = (int)(((int)mTimeElapsed % 1000) / 10);
        
        String text = "";
        
        if (hours > 0) {
        	text += df.format(hours) + ":";
        }
        
       	text += df.format(minutes) + ":";
       	text += df.format(seconds) + ":";
       	text += df.format(milliseconds);
       	if(Race.getInstance().mGhostMode && Race.getInstance().hasRaceBeenStarted())
       		Race.getInstance().checkGhostTime(text);
       	mTimeElapsedString = text;
        setText(text);
    }

    private void updateRunning() {
        boolean running = mVisible && mStarted;
        if (running != mRunning) {
            if (running) {
                updateText(SystemClock.elapsedRealtime());
                dispatchChronometerTick();
                mHandler.sendMessageDelayed(Message.obtain(mHandler,
                        TICK_WHAT), 100);
            } else {
                mHandler.removeMessages(TICK_WHAT);
            }
            mRunning = running;
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message m) {
            if (mRunning) {
                updateText(SystemClock.elapsedRealtime());
                dispatchChronometerTick();
                sendMessageDelayed(Message.obtain(this , TICK_WHAT),
                        100);
            }
        }
    };

    void dispatchChronometerTick() {
        if (mOnChronometerTickListener != null) {
            mOnChronometerTickListener.onChronometerTick(this);
        }
    }

	public long getTimeElapsed() {
		return mTimeElapsed;
	}

	/**
	 * Return the elapsed time of the chronometer as a String representative.
	 * @return The time string.
	 */
	public String getTimeElapsedString() {
		return mTimeElapsedString;
	}
    
}