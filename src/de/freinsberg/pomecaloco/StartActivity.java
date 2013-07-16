package de.freinsberg.pomecaloco;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;

import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class StartActivity extends Activity implements CvCameraViewListener2 {

	public static final int DIALOG_RADIOBUTTON_LEFT_INPUT_ID = 1000;
	public static final int DIALOG_RADIOBUTTON_RIGHT_INPUT_ID = 1001;
	public static final int DIALOG_RADIOBUTTON_INPUT_ID = 1000;
	public static boolean mScanningComplete = false;	
	private DBHelper mDbHelper;
	private Handler mHandler = new Handler();
	private Timer mShotTimer = new Timer();
	private TimerTask mShotTask;
	private int mAlphacounter;
	private float mAlpha;
	private int mSpinnerPosition;
	private Race race;
	private Context mContext;
	public static CameraBridgeViewBase mOpenCvCameraView;
	private Mat mInputFrame;
	private InputMethodManager inputManager = null;
	private boolean isFirstInitialisation = true;
	private Dialog mDialogIsEmpty;
//	private Track bridge;
//	private Track crossed;
//	private List<Track> tracks = new ArrayList<Track>();
	private List<String> mPlayerNames = new ArrayList<String>();
	private MyTrackSpinnerAdapter mTracksAdapter;
	private Mat bridge_image = null; 
	private Mat crossed_image = null;
	ObjectDetector mFrameToProcess;
	private Spinner mTracks;
	private TextView racemode = null;
	private Button min_mode = null;
	private Button lap_mode = null;
	private Button results = null;
	private Button scanner = null;
	private Button rescan = null;
	private boolean mLeftRadioButtonClicked;
	private boolean mRightRadioButtonClicked;
	private boolean mRadioButtonClicked;
	private int mCheckedIdLeft = 1;
	private int mCheckedIdRight = 1;
	private int mCheckedId = 1;
	private RadioGroup mFirstRadioGroup;
	private RadioGroup mSecondRadioGroup;	
	private EditText min_count = null;
	private EditText lap_count = null;
	private ImageView alpha_overlay = null;
	private View frame_border_top = null;
	private View frame_border_bottom = null;
	private View frame_border_left = null;
	private View frame_border_right = null;
	private ImageView frame_track_overlay;	
	private ImageView lane_overlay = null;
	private ImageView left_car_color;
	private ImageView right_car_color;
	private Bitmap[] mCarColorBitmaps;
	private int mCount;
	private int mCarStatus;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {	
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start);
		//open connection to the database
		mContext = getApplicationContext();
		mDbHelper = new DBHelper(mContext);
		mDbHelper.openDB();
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_stream_prepare);
		mOpenCvCameraView.setCvCameraViewListener(this);
		Log.i("debug", "setCVCameraViewListener properly");
		if(!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, this,	mLoaderCallback))
			Log.i("debug", "OpenCV wurde not initialized properly gerade.");		

		//Initialize GUI-Elements
		mTracks = (Spinner) findViewById(R.id.choose_track);
		lap_mode = (Button) findViewById(R.id.lap_mode);
		lap_count = (EditText) findViewById(R.id.lap_count);
		min_mode = (Button) findViewById(R.id.min_mode);
		min_count = (EditText) findViewById(R.id.min_count);
		results = (Button) findViewById(R.id.results);
		scanner = (Button) findViewById(R.id.scanner);
		rescan = (Button) findViewById(R.id.rescan);		
		racemode = (TextView) findViewById(R.id.racemode);
		alpha_overlay = (ImageView) findViewById(R.id.alpha_overlay);
		frame_track_overlay = (ImageView) findViewById(R.id.frame_track_overlay);
		lane_overlay = (ImageView) findViewById(R.id.lane_overlay);
		left_car_color = (ImageView) findViewById(R.id.left_car_color);
		right_car_color = (ImageView) findViewById(R.id.right_car_color);
		
		//the Views for interactive frame- border
		frame_border_top = (View) findViewById(R.id.frame_border_top);
		frame_border_bottom = (View) findViewById(R.id.frame_border_bottom);
		frame_border_left = (View) findViewById(R.id.frame_border_left);
		frame_border_right = (View) findViewById(R.id.frame_border_right);
		
		// Create an ArrayAdapter using the string array and a default spinner layout
		//mTracksAdapter = new ArrayAdapter<Track>(this,	android.R.layout.simple_spinner_item, tracks);
		
		// Specify the layout to use when the list of choices appears
		//mTracksAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		mTracksAdapter = new MyTrackSpinnerAdapter(this,R.layout.choose_track_spinner,  R.layout.choose_track_spinner_item, mDbHelper.getAllTracksWithImages());
		// Apply the adapter to the spinner		
		mTracks.setAdapter(mTracksAdapter);		
		mTracks.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int pos, long id) {
				Log.i("debug", "Spinner Position "+pos+" gewählt.");
				mSpinnerPosition = pos;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				Log.i("debug", "Spinner keine Position ausgewählt.");
			}			
		});	
				
		// Making these Button disables so user can't interact with them before scanning is complete		
		lap_mode.setEnabled(false);
		lap_count.setEnabled(false);
		min_mode.setEnabled(false);
		min_count.setEnabled(false);	
		//disable rescan button at the beginning
		rescan.setVisibility(View.GONE);
		
		// Set the Alpha Overlay to transparent	
		mAlphacounter = 0;
		
		// Set Scanner- Button Text for first Start
		scanner.setText(R.string.scan_track);		
		
		scanner.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mFrameToProcess = ObjectDetector.getInstance(mInputFrame.clone());
				if (scanner.getText() == getString(R.string.scan_track)) {
					mAlphacounter = 100;		
					frame_track_overlay.setImageBitmap(mFrameToProcess.generate_track_overlay());
					frame_track_overlay.setVisibility(View.VISIBLE);	
					if(mFrameToProcess.getFoundLines() == false){
						Toast.makeText(v.getContext(), "Bitte das Smartphone mittig über der Bahn platzieren.", Toast.LENGTH_LONG).show();
						return;
					}					
					Log.i("debug", "Track scanned!");
					scanner.setText(R.string.scan_cars);
					
					mShotTask = new TimerTask() {						
						@Override
						public void run() {
						mHandler.post(new Runnable() {								
								@Override
								public void run() {
									if(mAlphacounter>=1){										
										mAlpha = (float) (mAlphacounter/100.0);
										//Log.i("debug","Alpha: "+mAlpha);
										alpha_overlay.setAlpha(mAlpha);											
									frame_border_top.setBackgroundResource(R.color.record_yellow);	
									frame_border_bottom.setBackgroundResource(R.color.record_yellow);	
									frame_border_left.setBackgroundResource(R.color.record_yellow);	
									frame_border_right.setBackgroundResource(R.color.record_yellow);	
									mAlphacounter--;
									}else{
										frame_border_top.setBackgroundResource(R.color.white);	
										frame_border_bottom.setBackgroundResource(R.color.white);	
										frame_border_left.setBackgroundResource(R.color.white);	
										frame_border_right.setBackgroundResource(R.color.white);
										
										mShotTimer.cancel();
										mShotTimer.purge();										
									}														
								}								
							});							
						}
					};	
					mShotTimer = new Timer();
					mShotTimer.schedule(mShotTask, 50,5);
					
					if(lane_overlay == null)
						Log.i("debug","kein Lane Overlay :-(");
//					lane_overlay.setAlpha((float) (0.4));		
					Bitmap b;
					if((b = mFrameToProcess.draw_car_recognizer())!= null){
						lane_overlay.setImageBitmap(b);								
						lane_overlay.setVisibility(View.VISIBLE);
					}
				} else if (scanner.getText() == getString(R.string.scan_cars)) {
					mAlphacounter = 100;					
					mCarColorBitmaps = mFrameToProcess.get_cars_colors();
					if(mCarColorBitmaps[0] != null){
						left_car_color.setVisibility(View.VISIBLE);	
						left_car_color.setImageBitmap(mCarColorBitmaps[0]);	
					}
					if(mCarColorBitmaps[1] != null){
						right_car_color.setVisibility(View.VISIBLE);	
						right_car_color.setImageBitmap(mCarColorBitmaps[1]);
					}
												
					
					mCarStatus = mFrameToProcess.car_status();
					switch(mCarStatus){
						case ObjectDetector.NO_CAR:
							Toast.makeText(v.getContext(), "Kein Fahrzeug gefunden!", Toast.LENGTH_LONG).show();
							return;
						case ObjectDetector.RIGHT_CAR:							
							racemode.setText("1 Spieler Modus");
							Toast.makeText(v.getContext(), "Links kein Fahrzeug gefunden!", Toast.LENGTH_LONG).show();
							break;
						case ObjectDetector.LEFT_CAR:	
							racemode.setText("1 Spieler Modus");
							Toast.makeText(v.getContext(), "Rechts kein Fahrzeug gefunden!", Toast.LENGTH_LONG).show();
							break;
						case ObjectDetector.BOTH_CAR:
							racemode.setText("2 Spieler Modus");
							Toast.makeText(v.getContext(), "Zwei Fahrzeuge erkannt!", Toast.LENGTH_LONG).show();
							break;	
						default:
							Toast.makeText(v.getContext(), "Fehler bei der Fahrzeugerkennung!", Toast.LENGTH_LONG).show();							
					}
					
					mShotTask = new TimerTask() {	
						
						@Override
						public void run() {
						mHandler.post(new Runnable() {								
								@Override
								public void run() {
									if(mAlphacounter>=1){										
										mAlpha = (float) (mAlphacounter/100.0);
										//Log.i("debug","Alpha: "+mAlpha);
										alpha_overlay.setAlpha(mAlpha);											
									frame_border_top.setBackgroundResource(R.color.record_yellow);	
									frame_border_bottom.setBackgroundResource(R.color.record_yellow);	
									frame_border_left.setBackgroundResource(R.color.record_yellow);	
									frame_border_right.setBackgroundResource(R.color.record_yellow);	
									mAlphacounter--;
									}else{
										frame_border_top.setBackgroundResource(R.color.white);	
										frame_border_bottom.setBackgroundResource(R.color.white);	
										frame_border_left.setBackgroundResource(R.color.white);	
										frame_border_right.setBackgroundResource(R.color.white);
										
										mShotTimer.cancel();
										mShotTimer.purge();
										
									}
														
									}								
								});							
						}
					};	
					Log.i("debug", "defined the runnable for camera shot");
					mShotTimer = new Timer();
					mShotTimer.schedule(mShotTask, 50,5);
					
					Log.i("debug", "Cars scanned!");
					scanner.setEnabled(false);
					scanner.setText(R.string.complete);
					mScanningComplete = true;
					Log.i("debug", "Scanning COMPLETED!");
					lap_mode.setEnabled(true);
					lap_count.setEnabled(true);
					min_mode.setEnabled(true);
					min_count.setEnabled(true);
					rescan.setVisibility(View.VISIBLE);
				}
			}
		});
		
		
		rescan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				rescan.setVisibility(View.GONE);
				frame_track_overlay.setVisibility(View.GONE);				
				lane_overlay.setVisibility(View.GONE);				
				left_car_color.setVisibility(View.GONE);				
				right_car_color.setVisibility(View.GONE);	
				scanner.setEnabled(true);
				mScanningComplete = false;
				racemode.setText(R.string.racemode);
				lap_mode.setEnabled(false);
				lap_count.setEnabled(false);
				min_mode.setEnabled(false);
				min_count.setEnabled(false);
			
				scanner.setText(R.string.scan_track);
			}
		});
		
		results.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent().setClass(v.getContext(), SettingsFragmentActivity.class);
				startActivity(intent);
			}
		});	

		lap_mode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				inputManager = (InputMethodManager) (getSystemService(Context.INPUT_METHOD_SERVICE));
				if(inputManager != null && getCurrentFocus() != null)
					inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				if(lap_count.getText().toString().equals("")){					
					Toast.makeText(v.getContext(), "Bitte Rundenanzahl angeben!", Toast.LENGTH_SHORT).show();
					return;
				}
				mCount = Integer.parseInt(lap_count.getText().toString());	
				setPlayerName(Race.ROUND_MODE);										
				
				
			}
		});
		
		min_mode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				if(inputManager != null && getCurrentFocus() != null)
					inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				if(min_count.getText().toString().equals("")){					
					Toast.makeText(v.getContext(), "Bitte Minutenanzahl angeben!", Toast.LENGTH_SHORT).show();
					return;
				}
				mCount = Integer.parseInt(min_count.getText().toString());				
				setPlayerName(Race.TIMER_MODE);				
				
				
			}
		});	
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		
	}

	@Override
	public void onCameraViewStopped() {
		
	}
	int i = 0;
	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		
		if(!inputFrame.rgba().empty()) {
			if(mInputFrame != null)
				mInputFrame.release();
			
			mInputFrame = inputFrame.rgba();
		}
		return inputFrame.rgba();
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i("debug", "onResume (StartFragament) called");
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, this, mLoaderCallback);
	}
	

	@SuppressWarnings("unchecked")
	private void setPlayerName(int mode){
		final int _mode = mode;		
		
		//final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final Dialog dialog = new Dialog(this);
		final ArrayList<String> playerlist = mDbHelper.getAllPlayernames();
		final LayoutInflater inflater = this.getLayoutInflater();
		
		if(ObjectDetector.getInstance().car_status() == ObjectDetector.BOTH_CAR){
					
			dialog.setContentView(R.layout.two_player_names);
			dialog.setTitle("Spielernamen erforderlich");
			final RadioGroup left_radioGroup = inflateRadioGroup((RadioGroup) dialog.findViewById(R.id.dialog_select_playername_left), playerlist, DIALOG_RADIOBUTTON_LEFT_INPUT_ID);				
			final RadioGroup right_radioGroup = inflateRadioGroup((RadioGroup) dialog.findViewById(R.id.dialog_select_playername_right), playerlist, DIALOG_RADIOBUTTON_RIGHT_INPUT_ID);				
			
			if(playerlist.size() != 0){
				mCheckedIdLeft = 0;
				mCheckedIdRight = 0;
				TextView left_player = (TextView) dialog.findViewById(R.id.dialog_left_player);
				left_player.setVisibility(View.GONE);
				TextView right_player = (TextView) dialog.findViewById(R.id.dialog_right_player);
				right_player.setVisibility(View.GONE);
				left_radioGroup.invalidate();
				right_radioGroup.invalidate();
				
				left_radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {					
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						
						EditText left_playername = (EditText) dialog.findViewById(R.id.dialog_left_player_name);		
						if(checkedId == DIALOG_RADIOBUTTON_LEFT_INPUT_ID)												
							left_playername.setVisibility(View.VISIBLE);	
						else{
							mLeftRadioButtonClicked = true;
							left_playername.setVisibility(View.INVISIBLE);	
							if(left_radioGroup.getChildAt(mCheckedIdLeft-1) != null){
							if(right_radioGroup.getChildAt(mCheckedIdLeft-1).isEnabled() == false)
								right_radioGroup.getChildAt(mCheckedIdLeft-1).setEnabled(true);
							}
							mCheckedIdLeft = checkedId;
							right_radioGroup.getChildAt(checkedId-1).setEnabled(false);
						}						
					}
				});
				
				right_radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {					
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {						
						EditText right_playername = (EditText) dialog.findViewById(R.id.dialog_right_player_name);								
						if(checkedId == DIALOG_RADIOBUTTON_RIGHT_INPUT_ID)												
							right_playername.setVisibility(View.VISIBLE);	
						else{
							mRightRadioButtonClicked = true;
							right_playername.setVisibility(View.INVISIBLE);	
							if(left_radioGroup.getChildAt(mCheckedIdRight-1) != null){
								if(left_radioGroup.getChildAt(mCheckedIdRight-1).isEnabled() == false)
									left_radioGroup.getChildAt(mCheckedIdRight-1).setEnabled(true);
							}
							mCheckedIdRight = checkedId;
							left_radioGroup.getChildAt(checkedId-1).setEnabled(false);
						}							
					}
				});				
			}		
			
			Button okButton = (Button) dialog.findViewById(R.id.dialog_ok);
			Button cancelButton = (Button) dialog.findViewById(R.id.dialog_cancel);
			
			okButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mPlayerNames.clear();
					EditText left_playername = (EditText) dialog.findViewById(R.id.dialog_left_player_name);
					EditText right_playername = (EditText) dialog.findViewById(R.id.dialog_right_player_name);
					if(checkDialogInputTwoPlayer(left_playername, right_playername, left_radioGroup, right_radioGroup)){

						start(_mode, false);
						Log.i("debug", "Moved to Race-Activity!");
					}
									
				}
			});			
			cancelButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					dialog.dismiss();					
				}
			});
		}
		else
		{
			
			dialog.setContentView(R.layout.one_player_name);
			dialog.setTitle("Spielername erforderlich");
			final RadioGroup radioGroup = inflateRadioGroup((RadioGroup) dialog.findViewById(R.id.dialog_select_playername), playerlist, DIALOG_RADIOBUTTON_INPUT_ID);
			if(playerlist.size() != 0){
				mCheckedId = 0;					
				radioGroup.invalidate();
				radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {					
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						
						EditText player_name = (EditText) dialog.findViewById(R.id.dialog_player_name);		
						if(checkedId == DIALOG_RADIOBUTTON_INPUT_ID)												
							player_name.setVisibility(View.VISIBLE);	
						else{
							mRadioButtonClicked = true;
							mCheckedId = checkedId;
							player_name.setVisibility(View.INVISIBLE);								
						}						
					}
				});				
			}			
			
			Button okButton = (Button) dialog.findViewById(R.id.dialog_ok);			
			Button cancelButton = (Button) dialog.findViewById(R.id.dialog_cancel);
			okButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {		
					EditText playername = (EditText) dialog.findViewById(R.id.dialog_player_name);
					if(checkDialogInputOnePlayer(playername, radioGroup)){
						start(_mode, false);					
						Log.i("debug", "Moved to Race-Activity!");	
						dialog.dismiss();
					}
				}
			});	
			
			Button ghostButton = (Button) dialog.findViewById(R.id.dialog_ghost);
			if(_mode == Race.ROUND_MODE){
				if(mDbHelper.isRoundGhostPresent(((Pair<String,byte[]>) mTracks.getSelectedItem()).getL(), mCount)){	
					Log.i("debug", "Ghost is available, settings are, Trackname:" + ((Pair<String,byte[]>) mTracks.getSelectedItem()).getL() + ", Rounds: " + mCount + ", Times: " + mDbHelper.getRoundGhost(((Pair<String,byte[]>) mTracks.getSelectedItem()).getL(), mCount));
					ghostButton.setEnabled(true);		
				}
			}
			else{
					if(mDbHelper.isTimeGhostPresent(((Pair<String,byte[]>) mTracks.getSelectedItem()).getL(), mCount)){
						Log.i("debug", "Ghost is available, settings are, Trackname:" + ((Pair<String,byte[]>) mTracks.getSelectedItem()).getL() + ", Rounds: " + mCount + ", Times: " + mDbHelper.getTimeGhost(((Pair<String,byte[]>) mTracks.getSelectedItem()).getL(), mCount));
						ghostButton.setEnabled(true);	
					}
			}
				
				ghostButton.setOnClickListener(new OnClickListener() {					
					@Override
					public void onClick(View v) {
						EditText playername = (EditText) dialog.findViewById(R.id.dialog_player_name);
						if(checkDialogInputOnePlayer(playername, radioGroup)){
							start(_mode, true);					
							Log.i("debug", "Moved to Race-Activity!");	
							dialog.dismiss();
						}
					}
				});
			
				
			cancelButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					dialog.dismiss();					
				}
			});
		}		
		dialog.show();
	}
	
	private boolean checkDialogInputOnePlayer(EditText playername, RadioGroup radioGroup) {
		mPlayerNames.clear();
		
		Log.i("debug","Spielername: |"+playername.getText().toString()+"|");
		if(playername.getVisibility() != View.INVISIBLE){		
			if(playername.getText().toString().isEmpty()){						
				Log.i("debug","Spielername is empty");					
				playername.setHintTextColor(getResources().getColor(R.color.loosing_red));
				playername.setHint(R.string.dialog_name_warning);					
			}else if(mDbHelper.isPlayerPresent(playername.getText().toString()))
			{
				playername.setHintTextColor(getResources().getColor(R.color.loosing_red));
				playername.setHint(R.string.dialog_name_exists);		
			}
			else
			{
				mPlayerNames.add(playername.getText().toString());
				mDbHelper.createPlayer(playername.getText().toString());
			}
		}
		else
		{
			if(mCheckedId != 0)							
				mPlayerNames.add(((RadioButton) radioGroup.getChildAt(mCheckedId-1)).getText().toString());
		}
		if(!mPlayerNames.isEmpty())		
			return true;
		else
			return false;	
	}
	
	private boolean checkDialogInputTwoPlayer(EditText left_playername, EditText right_playername, RadioGroup left_radioGroup, RadioGroup right_radioGroup){
		boolean all_ok = true;					
		if(left_playername.getVisibility() != View.INVISIBLE){					
		
			if(left_playername.getText().toString().isEmpty()){	
				all_ok = false;
				left_playername.setHintTextColor(getResources().getColor(R.color.loosing_red));
				left_playername.setHint(R.string.dialog_name_warning);												
			}
			else if(mDbHelper.isPlayerPresent(left_playername.getText().toString()))
			{
				all_ok = false;
				left_playername.setHintTextColor(getResources().getColor(R.color.loosing_red));
				left_playername.setHint(R.string.dialog_name_exists);		
			}				
		}
		
		
		if(right_playername.getVisibility() != View.INVISIBLE){	
			if(right_playername.getText().toString().isEmpty())
			{	
				all_ok = false;
				right_playername.setHintTextColor(getResources().getColor(R.color.loosing_red));
				right_playername.setHint(R.string.dialog_name_warning);							
			}
			else if(mDbHelper.isPlayerPresent(right_playername.getText().toString()))
			{
				all_ok = false;
				right_playername.setHintTextColor(getResources().getColor(R.color.loosing_red));
				right_playername.setHint(R.string.dialog_name_exists);		
			}			
		}
		if(all_ok)
		{
			if(left_playername.getVisibility() != View.INVISIBLE){
				mPlayerNames.add(left_playername.getText().toString());	
			mDbHelper.createPlayer(left_playername.getText().toString());
			}							
			else
			{
				mPlayerNames.add(((RadioButton) left_radioGroup.getChildAt(mCheckedIdLeft-1)).getText().toString());
			}
			if(right_playername.getVisibility() != View.INVISIBLE)
			{
				mPlayerNames.add(right_playername.getText().toString());
				mDbHelper.createPlayer(right_playername.getText().toString());
			}
				
			else{
				mPlayerNames.add(((RadioButton) right_radioGroup.getChildAt(mCheckedIdRight-1)).getText().toString());
			}
			return true;
		}else
			return false;
		
	}
	
	private RadioGroup inflateRadioGroup(RadioGroup group, ArrayList<String> playerlist, int id){	
		
		int i = 1;
		for(String s : playerlist){					
			Log.i("debug", "BaseContext: "+ getBaseContext() + ",  Context: " + mContext);
			RadioButton b = new RadioButton(this);
			b.setText(s);
			b.setId(i);
			group.addView(b, new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT));
			i++;
		}
		RadioButton input = new RadioButton(this);
		input.setText(R.string.dialog_new_player);
		input.setId(id);
		group.addView(input, new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT));	
		
		return group;		
	}
	
	private void start(int mode, boolean ghostMode){
		int _mode = mode;	
		Race.getInstance().newRace(mContext, mCount, _mode,mTracks.getSelectedItem(), mCarStatus, mPlayerNames, ghostMode);					
		Intent intent = new Intent().setClass(mContext, RaceActivity.class);
		startActivity(intent);
		finish();
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


	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i("debug", "OpenCV loaded successfully");					
				mOpenCvCameraView.enableView();
				//Static adding of tracks		
//				if(isFirstInitialisation){
//					try { 	
//					    bridge_image = Utils.loadResource(mContext,R.drawable.bridge_image); 
//					    } catch (IOException e) { 
//					     e.printStackTrace(); 
//					    } 
//					try { 		
//					    crossed_image = Utils.loadResource(mContext,R.drawable.crossed_image); 
//					    } catch (IOException e) { 
//					     e.printStackTrace(); 
//					    }
//					bridge = new Track("Brückenbahn", false, 5, bridge_image);
//					crossed = new Track("Kreuzungsbahn", true, 7, crossed_image);		
//					tracks.add(bridge);
//					tracks.add(crossed);		
//					mTracksAdapter.notifyDataSetChanged();
//					isFirstInitialisation = false;
//					break;
//				}
				
			}
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};	

}
