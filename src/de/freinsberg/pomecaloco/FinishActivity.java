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
	private Button go_to_results;
	private Button new_race;	
	private TextView finish_track;
	private TextView finish_mode;
	private TextView finish_heading;
	
	private TextView finish_attempt;
	private TextView finish_meters;
	private TextView finish_fastest;
	private TextView finish_avg_speed;
	private TextView finish_timemode_driven_rounds;
	private TextView finish_roundmode_driven_time;
	
	private TextView finish_right_attempt;
	private TextView finish_right_meters;
	private TextView finish_right_fastest;
	private TextView finish_right_avg_speed;
	private TextView finish_right_timemode_driven_rounds;
	private TextView finish_right_roundmode_driven_time;
	
	private TextView finish_left_attempt;
	private TextView finish_left_meters;
	private TextView finish_left_fastest;
	private TextView finish_left_avg_speed;
	private TextView finish_left_timemode_driven_rounds;
	private TextView finish_left_roundmode_driven_time;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		int lane;
		if(ObjectDetector.getInstance().car_status() != ObjectDetector.BOTH_CAR)
		{		
			
			if(ObjectDetector.getInstance().car_status() == ObjectDetector.LEFT_CAR)
				lane = Race.LEFT_LANE;
			else
				lane = Race.RIGHT_LANE;
			setContentView(R.layout.finish_oneplayer);
	        //Setting Views and doing stuff for One-Player-Game	        
	        finish_heading = (TextView) findViewById(R.id.finish_heading);
	        finish_attempt = (TextView) findViewById(R.id.finish_attempt_view);
	        finish_meters = (TextView) findViewById(R.id.finish_meters_view);
	        finish_fastest = (TextView) findViewById(R.id.finish_fastest_view);
	        finish_avg_speed = (TextView) findViewById(R.id.finish_avg_speed_view);
	        finish_timemode_driven_rounds = (TextView) findViewById(R.id.finish_timemode_driven_rounds_view);
	        finish_roundmode_driven_time = (TextView) findViewById(R.id.finish_roundmode_driven_time_view);
	        finish_heading.setText("Einzelspieler");
	        if(Race.getInstance().getGameMode() == Race.ROUND_MODE){        	
	        	finish_mode.setText("Rundenrennen");
	        	finish_roundmode_driven_time.setVisibility(View.VISIBLE);
	        	finish_roundmode_driven_time.setText(Race.getInstance().getFinishedTime());
	        }
	        else{
	        	finish_mode.setText("Zeitfahren");
	        	finish_timemode_driven_rounds.setVisibility(View.VISIBLE);
	        	finish_timemode_driven_rounds.setText(Race.getInstance().getCurrentRound(lane));
	        }        
	        
	        finish_attempt.setText(Race.getInstance().getAttempt(lane));
	        finish_meters.setText(Race.getInstance().getDrivenMeters(lane));
	        finish_fastest.setText(Race.getInstance().getFastestRound(lane));
	        finish_avg_speed.setText(Race.getInstance().getAvgSpeed(lane));
		}
		else
		{
			setContentView(R.layout.finish_twoplayer);
			//Setting Views  and doing stuff for Two-Player-Game
			finish_heading = (TextView) findViewById(R.id.finish_heading);
	        //Views for the Left Lane
	        finish_left_attempt = (TextView) findViewById(R.id.finish_left_attempt_view);
	        finish_left_meters = (TextView) findViewById(R.id.finish_left_meters_view);
	        finish_left_fastest = (TextView) findViewById(R.id.finish_left_fastest_view);
	        finish_left_avg_speed = (TextView) findViewById(R.id.finish_left_avg_speed_view);
	        finish_left_timemode_driven_rounds = (TextView) findViewById(R.id.finish_left_timemode_driven_rounds_view);
	        finish_left_roundmode_driven_time = (TextView) findViewById(R.id.finish_left_roundmode_driven_time_view);	        
	        //Views for the Right Lane
	        finish_right_attempt = (TextView) findViewById(R.id.finish_right_attempt_view);
	        finish_right_meters = (TextView) findViewById(R.id.finish_right_meters_view);
	        finish_right_fastest = (TextView) findViewById(R.id.finish_right_fastest_view);
	        finish_right_avg_speed = (TextView) findViewById(R.id.finish_right_avg_speed_view);
	        finish_right_timemode_driven_rounds = (TextView) findViewById(R.id.finish_right_timemode_driven_rounds_view);
	        finish_right_roundmode_driven_time = (TextView) findViewById(R.id.finish_right_roundmode_driven_time_view);
	        
	        finish_heading.setText("Mehrspieler");
	        
	        if(Race.getInstance().getGameMode() == Race.ROUND_MODE){        	
	        	finish_mode.setText("Rundenrennen");
	        	finish_right_roundmode_driven_time.setVisibility(View.VISIBLE);
	        	finish_right_roundmode_driven_time.setText(Race.getInstance().getFinishedTime());
	        	finish_left_roundmode_driven_time.setVisibility(View.VISIBLE);
	        	finish_left_roundmode_driven_time.setText(Race.getInstance().getFinishedTime());
	        }
	        else{
	        	finish_mode.setText("Zeitfahren");
	        	finish_left_timemode_driven_rounds.setVisibility(View.VISIBLE);
	        	finish_left_timemode_driven_rounds.setText(Race.getInstance().getCurrentRound(Race.LEFT_LANE));
	        	finish_right_timemode_driven_rounds.setVisibility(View.VISIBLE);
	        	finish_right_timemode_driven_rounds.setText(Race.getInstance().getCurrentRound(Race.RIGHT_LANE));
	        }
	        finish_left_attempt.setText(Race.getInstance().getAttempt(Race.LEFT_LANE));
	        finish_left_meters.setText(Race.getInstance().getDrivenMeters(Race.LEFT_LANE));
	        finish_left_fastest.setText(Race.getInstance().getFastestRound(Race.LEFT_LANE));
	        finish_left_avg_speed.setText(Race.getInstance().getAvgSpeed(Race.LEFT_LANE));
	        
	        finish_right_attempt.setText(Race.getInstance().getAttempt(Race.RIGHT_LANE));
	        finish_right_meters.setText(Race.getInstance().getDrivenMeters(Race.RIGHT_LANE));
	        finish_right_fastest.setText(Race.getInstance().getFastestRound(Race.RIGHT_LANE));
	        finish_right_avg_speed.setText(Race.getInstance().getAvgSpeed(Race.RIGHT_LANE));
	        
		}		
		//Setting Views that are Inflated wether one or Two-Player Game
		go_to_results = (Button) findViewById(R.id.go_to_results);		
        new_race = (Button) findViewById(R.id.new_race);
        finish_track = (TextView) findViewById(R.id.finish_track_view);
        finish_mode = (TextView) findViewById(R.id.finish_mode_view);    
        
        finish_track.setText(Race.getInstance().getTrackName());        
        
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
