package de.freinsberg.pomecaloco;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.RadioGroup;
import android.widget.Spinner;
 
public class ResultsFragment  extends Fragment{
	
    private Context mContext;
    private DBHelper mDbHelper;
    private Spinner results_track;
    private Spinner results_name;
    private RadioGroup results_racemode;
    private GridView results_grid;    
 
    public ResultsFragment(){
         
    } 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	mContext = SettingsFragmentActivity.mContext;
        View v = inflater.inflate(R.layout.results, null);
        mDbHelper = new DBHelper(mContext);
        mDbHelper.openDB();
        //Creating the LayoutViews
		results_track = (Spinner) v.findViewById(R.id.results_track);
		results_name = (Spinner) v.findViewById(R.id.results_name);
		results_racemode = (RadioGroup) v.findViewById(R.id.results_racemode);
		results_grid = (GridView) v.findViewById(R.id.results_grid);	

		
		ArrayAdapter<String> nameAdapter = new ArrayAdapter<String>(mContext, R.layout.choose_name_spinner , mDbHelper.getAllPlayernames());
		nameAdapter.setDropDownViewResource(R.layout.choose_name_spinner);
		results_name.setAdapter(nameAdapter);
		
		
		MyTrackSpinnerAdapter trackAdapter = new MyTrackSpinnerAdapter(mContext,  R.layout.choose_track_spinner, mDbHelper.getAllTracksWithImages());
		results_track.setAdapter(trackAdapter);
		
		
		//React to the Different Combinations to View the Content in GridView
		
		
		//results_grid.
		

		
			
		return v;
    }
}