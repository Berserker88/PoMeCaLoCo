package de.freinsberg.pomecaloco;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class FinishActivity extends Activity {	
	
	public static int PREPARE_RACE = 0;
	public static int RACE = 1;
	public static int END_RACE = 2;
	private Context mContext;
	private Button go_to_results = null;
	private Button new_race = null;	
	private TextView finish_track;
	private TextView finish_mode;
	private TextView finish_attempt;
	private TextView finish_meters;
	private TextView finish_fastest;
	private TextView finish_avg_speed;
	private TextView finish_timemode_driven_rounds;
	private TextView finish_roundmode_driven_time;
	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.finish);
		//Making Views and Buttons from XML-View accessible via Java Code
		go_to_results = (Button) findViewById(R.id.go_to_results);		
        new_race = (Button) findViewById(R.id.new_race);
        finish_track = (Button) findViewById(R.id.finish_track_view);
        finish_mode = (Button) findViewById(R.id.finish_mode_view);
        finish_attempt = (Button) findViewById(R.id.finish_attempt_view);
        finish_meters = (Button) findViewById(R.id.finish_meters_view);
        finish_fastest = (Button) findViewById(R.id.finish_fastest_view);
        finish_avg_speed = (Button) findViewById(R.id.finish_avg_speed_view);
        finish_timemode_driven_rounds = (Button) findViewById(R.id.finish_timemode_driven_rounds_view);
        finish_roundmode_driven_time = (Button) findViewById(R.id.finish_roundmode_driven_time_view);
        
        finish_track.setText(Race.getInstance().getTrackName());
        if(Race.getInstance().getGameMode() == Race.ROUND_MODE){
        	finish_mode.setText("Rundenrennen");
        	finish_roundmode_driven_time.setText(Race.getInstance().getFinishedTime());
        }
        else{
        	finish_mode.setText("Zeitfahren");
        finish_timemode_driven_rounds.setText(Race.getInstance().getCurrentRound(Race.LEFT_LANE));
        }        
        
        //finish_attempt.setText(Race.getInstance().getAttempt(Race.LEFT_LANE));
        //finish_meters.setText(Race.getInstance().getDrivenMeters(Race.LEFT_LANE));
        //finish_fastest.setText(Race.getInstance().getFastestRound(Race.LEFT_LANE));
        //finish_avg_speed.setText(Race.getInstance().getAvgSpeed(Race.LEFT_LANE));      
        
        go_to_results.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent intent = new Intent(v.getContext(), SettingsFragmentActivity.class);     
            	startActivity(intent);  
            	finish();
            }
        });	        

		new_race.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.i("debug", "start a new race");
				Intent intent = new Intent().setClass(v.getContext(), StartActivity.class);
				startActivity(intent);     
				finish();
			}
		});	
	}
	
	@Override
    public void onBackPressed() {
		new AlertDialog.Builder(this)
			.setTitle("Beenden?")
			.setMessage("Wollen Sie die App beenden?")
        	.setCancelable(false)
        	.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int id) {
        			finish();
        		}
        	})
        	.setNegativeButton("Abbrechen", null)
        	.show();
    }


}
