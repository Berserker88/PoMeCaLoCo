package de.freinsberg.pomecaloco;

import android.app.Activity;
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

public class FinishActivity extends Activity {	
	
	public static int PREPARE_RACE = 0;
	public static int RACE = 1;
	public static int END_RACE = 2;
	private Context mContext;
	private Button go_to_results = null;
	private Button new_race = null;	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.finish);
		//Making Views and Buttons from XML-View accessible via Java Code
		go_to_results = (Button) findViewById(R.id.go_to_results);		
        go_to_results.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent intent = new Intent(v.getContext(), SettingsFragmentActivity.class);     
            	startActivity(intent);            	
            }
        });	
        
        new_race = (Button) findViewById(R.id.new_race);
		new_race.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.i("debug", "start a new race");
				Intent intent = new Intent().setClass(v.getContext(), StartActivity.class);
				startActivity(intent);     
			}
		});	
	}

}
