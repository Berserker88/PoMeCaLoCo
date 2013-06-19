package de.freinsberg.pomecaloco;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FinishFragment extends Fragment {	
	
	public static int PREPARE_RACE = 0;
	public static int RACE = 1;
	public static int END_RACE = 2;
	private Context mContext;
	private Button go_to_results = null;
	private Button new_race = null;	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContext = RaceFragmentActivity.mContext;
		View v = inflater.inflate(R.layout.finish, null);		
		
		//Making Views and Buttons from XML-View accessible via Java Code
		go_to_results = (Button) v.findViewById(R.id.go_to_results);		
        go_to_results.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent myIntent = new Intent(mContext, SettingsFragmentActivity.class);     
            	getActivity().finish();
            	getActivity().startActivity(myIntent);            	
            }
        });	
        
        new_race = (Button) v.findViewById(R.id.new_race);
		new_race.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.i("debug", "start a new race");
				((RaceFragmentActivity) getActivity()).getViewPager().setCurrentItem(PREPARE_RACE);
			}
		});
		
		return v;
	}

}
