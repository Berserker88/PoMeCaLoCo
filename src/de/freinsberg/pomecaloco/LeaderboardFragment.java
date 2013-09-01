package de.freinsberg.pomecaloco;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
 
public class LeaderboardFragment  extends Fragment {
	
    private Context mContext;
 
        public LeaderboardFragment(){
         
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	mContext = SettingsFragmentActivity.mContext;
        View v = inflater.inflate(R.layout.setup, null);
        return v;
    }
}