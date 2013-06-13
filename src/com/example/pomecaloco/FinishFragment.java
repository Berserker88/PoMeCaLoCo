package com.example.pomecaloco;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class FinishFragment extends Fragment {
	
	Context c;
	String value = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		c = RaceFragmentActivity.mContext;
		View v = inflater.inflate(R.layout.finish, null);
		
		Button go_to_results = (Button) v.findViewById(R.id.go_to_results);
		
        go_to_results.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent myIntent = new Intent(c, SettingsFragmentActivity.class);     
            	getActivity().finish();
            	getActivity().startActivity(myIntent);
            	
            	
            }
        });
		
		
		return v;
	}

}
