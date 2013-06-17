package de.freinsberg.pomecaloco;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
 
public class ResultsFragment  extends Fragment {
    Context c;
 
        public ResultsFragment(){
         
    }

 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	c = RaceFragmentActivity.mContext;
        View v = inflater.inflate(R.layout.results, null);
        
		Spinner results_racemode = (Spinner) v.findViewById(R.id.results_racemode);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(c,
				R.array.racemodes, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		results_racemode.setAdapter(adapter);
		
		GridView results_grid = (GridView) v.findViewById(R.id.results_grid);
		
		return       v;
    }
}