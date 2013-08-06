package de.freinsberg.pomecaloco;


import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
 
public class ResultsFragment  extends Fragment{
	
    private Context mContext;
    private DBHelper mDbHelper;
    private Spinner results_track;
    private Spinner results_name;
    private RadioGroup results_racemode;
    private boolean mRoundMode;
    private boolean mTimerMode;
    private String mSelectedTrack = "";
    private String mSelectedName = "";
    private ArrayAdapter<String> mResultAdapter;
    private ArrayList<String> mResultSet = new ArrayList<String>();
    
    private GridView results_grid; 
    public ResultsFragment(){} 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	mContext = SettingsFragmentActivity.mContext;
        View v = inflater.inflate(R.layout.results, null);
        mRoundMode = false;
        mTimerMode = false;        
        mDbHelper = new DBHelper(mContext);
        mDbHelper.openDB();
        //Creating the LayoutViews
		results_track = (Spinner) v.findViewById(R.id.results_track);
		results_name = (Spinner) v.findViewById(R.id.results_name);
		results_racemode = (RadioGroup) v.findViewById(R.id.results_racemode);
		results_grid = (GridView) v.findViewById(R.id.results_grid);
		
		mResultAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, mResultSet);
		results_grid.setAdapter(mResultAdapter);
		ArrayAdapter<String> nameAdapter = new ArrayAdapter<String>(mContext, R.layout.choose_name_spinner , mDbHelper.getAllPlayernames());
		nameAdapter.setDropDownViewResource(R.layout.choose_name_spinner);
		results_name.setAdapter(nameAdapter);		
		
		String preselectedPlayername = Race.getInstance().getPlayerName(0);
		if(preselectedPlayername != null){
			
			int spinnerPos = nameAdapter.getPosition(preselectedPlayername);
			Log.i("debug","Name Spinner Preselection to: " + spinnerPos);
			results_name.setSelection(spinnerPos);
		}
		
		results_name.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				mSelectedName = arg0.getItemAtPosition(arg2).toString();
				getResultSet();
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				Log.i("debug", "No new Name!");
				
			}
		});
		
		
		MyTrackSpinnerAdapter trackAdapter = new MyTrackSpinnerAdapter(mContext,  R.layout.choose_track_spinner, mDbHelper.getAllTracksWithImages());
		results_track.setAdapter(trackAdapter);
		
		String preselectedTrack = Race.getInstance().getTrackName();
		if(preselectedTrack != null){
			
			int spinnerPos = trackAdapter.getPosition(preselectedTrack);
			Log.i("debug","Track Spinner Preselection to: " + spinnerPos);
			results_name.setSelection(spinnerPos);
		}
		
		results_track.setOnItemSelectedListener(new OnItemSelectedListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				mSelectedTrack = ((Pair<String,byte[]>)arg0.getItemAtPosition(arg2)).getL();
				getResultSet();
				
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				Log.i("debug", "No new Track!");
				
			}
		});		
				
		//String[] resultSet = mDbHelper.getResultSet((String) results_name.getSelectedItem() ,(String) results_track.getSelectedItem());
		//React to the Different Combinations to View the Content in GridView
		//ArrayAdapter<String> resultsAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, resultSet);
		
		//results_grid.
		

		
			
		return v;
    }
	private void getResultSet() {
		Log.i("debug", "Selected Item in Name Spinner: " + mSelectedName);
		Log.i("debug", "Selected Item in Track Spinner: " + mSelectedTrack);
		Log.i("debug", "Selected Item in RadioGroup, Roundmode: " + mRoundMode + ",  Timermode: " + mTimerMode);
		int mode = 0;
		if(mRoundMode)
			mode = Race.ROUND_MODE;
		else if(mTimerMode)
			mode = Race.TIMER_MODE;			
		ArrayList<String> tempResults = mDbHelper.getResultSet(mSelectedName, mSelectedTrack, mode);
		mResultSet.clear();
		if(tempResults != null){
			
			for(String s : tempResults){
				mResultSet.add(s);
			}			
		}
		mResultAdapter.notifyDataSetChanged();
			
		//results_grid.setAdapter(mResultAdapter);
		
	}
	public void setRoundModeChecked() {		
		mRoundMode = true;
		mTimerMode = false;
		getResultSet();
		
		
	}
	public void setTimerModeChecked() {
		mTimerMode = true;
		mRoundMode = false;
		getResultSet();
		
		
	}
    

}