package de.freinsberg.pomecaloco;

import android.os.Handler;

public class GUIUpdater {

        private Handler mHandler = new Handler();
        private Runnable mCountdown;
        private int UPDATE_INTERVAL = 1000;

        public GUIUpdater(final Runnable uiUpdater){
            mCountdown = new Runnable() {
                @Override
                public void run() {
                    // Run the passed runnable
                    uiUpdater.run();
                    // Re-run it after the update interval
                    mHandler.postDelayed(this, UPDATE_INTERVAL);
                }
            };
        }

        public GUIUpdater(Runnable uiUpdater, int interval){
        	this(uiUpdater);
            UPDATE_INTERVAL = interval;
            
        }

        public void startUpdates(){
            mCountdown.run();
        }

        public void stopUpdates(){
            mHandler.removeCallbacks(mCountdown);
        }
}