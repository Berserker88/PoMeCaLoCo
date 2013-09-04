package de.freinsberg.pomecaloco;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
 
/**
 * This class will show the leaderboard.
 * @author freinsberg
 *
 */
public class LeaderboardFragment  extends Fragment { 
        public LeaderboardFragment(){         
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	//mContext = ResultsFragmentActivity.mContext;
        View v = inflater.inflate(R.layout.leaderboard, null);
        return v;
    }
}