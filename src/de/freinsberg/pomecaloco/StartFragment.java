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
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class StartFragment extends Fragment implements CvCameraViewListener2 {

	public static boolean mScanningComplete = false;
	public static boolean mFirstInitialization = true;
	final public static int ROUND_MODE = 1;
	final public static int TIMER_MODE = 2;
	final public static int LEFT_LANE = 1;
	final public static int RIGHT_LANE = 2;
	final public static int PREPARE_RACE = 0;
	final public static int RACE = 1;
	final public static int END_RACE = 2;
	private Handler mHandler = new Handler();
	private Timer mShotTimer = new Timer();
	private TimerTask mShotTask;
	private int mAlphacounter;
	private float mAlpha;
	private Race race;
	private Context mContext;
	public static CameraBridgeViewBase mOpenCvCameraView;
	private InputMethodManager inputManager = null;
	private Bundle mRaceBundle = new Bundle();
	private Track bridge;
	private Track crossed;
	private List<String> tracks = new ArrayList<String>();
	private Mat bridge_image = null; 
	private Mat crossed_image = null;
	ObjectDetector mFrameToProcess;
	private Spinner mTracks;
	private Button min_mode = null;
	private Button lap_mode = null;
	private Button results = null;
	private Button scanner = null;
	private Button rescan = null;
	private EditText min_count = null;
	private EditText lap_count = null;
	private ImageView alpha_overlay = null;
	private View frame_border_top = null;
	private View frame_border_bottom = null;
	private View frame_border_left = null;
	private View frame_border_right = null;
	private ImageView frame_track_overlay;
	private ImageView upper_lane_overlay = null;
	private ImageView lower_lane_overlay = null;
	private int mCount;
	private ArrayAdapter<String> mTracksAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {	
		
		mContext = RaceFragmentActivity.mContext;
		View v = inflater.inflate(R.layout.start, null);
		mOpenCvCameraView = (CameraBridgeViewBase) v
				.findViewById(R.id.camera_stream_prepare);
		if ((RaceFragment.mOpenCvCameraView == null)
				|| (RaceFragment.mOpenCvCameraView.getVisibility() != SurfaceView.VISIBLE))
			mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		else
			mOpenCvCameraView.setVisibility(SurfaceView.INVISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
		Log.i("debug", "setCVCameraViewListener properly");
		if(!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, mContext,
			mLoaderCallback))
				Log.i("debug", "OpenCV wurde not initialized properly gerade.");		
		
		mTracks = (Spinner) v.findViewById(R.id.choose_track);
		
		// Create an ArrayAdapter using the string array and a default spinner layout
		mTracksAdapter = new ArrayAdapter<String>(mContext,	android.R.layout.simple_spinner_item, tracks);
		
		// Specify the layout to use when the list of choices appears
		mTracksAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		// Apply the adapter to the spinner		
		mTracks.setAdapter(mTracksAdapter);		
		mTracks.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int pos, long id) {
				Log.i("debug", "Spinner Position "+pos+" gewählt.");				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				Log.i("debug", "Spinner keine Position ausgewählt.");				
			}			
		});	

		// Adding Layout Buttons to this onCreate to interact with them (fetching and setting data)
		lap_mode = (Button) v.findViewById(R.id.lap_mode);
		lap_count = (EditText) v.findViewById(R.id.lap_count);
		min_mode = (Button) v.findViewById(R.id.min_mode);
		min_count = (EditText) v.findViewById(R.id.min_count);
				
		// Making these Button disables so user can't interact with them before scanning is complete		
		lap_mode.setEnabled(false);
		lap_count.setEnabled(false);
		min_mode.setEnabled(false);
		min_count.setEnabled(false);

		results = (Button) v.findViewById(R.id.results);
		scanner = (Button) v.findViewById(R.id.scanner);
		rescan = (Button) v.findViewById(R.id.rescan);
		
		//the Views for interactive frame- border
		frame_border_top = (View) v.findViewById(R.id.frame_border_top);
		frame_border_bottom = (View) v.findViewById(R.id.frame_border_bottom);
		frame_border_left = (View) v.findViewById(R.id.frame_border_left);
		frame_border_right = (View) v.findViewById(R.id.frame_border_right);
		alpha_overlay = (ImageView) v.findViewById(R.id.alpha_overlay);
		frame_track_overlay = (ImageView) v.findViewById(R.id.frame_track_overlay);
		upper_lane_overlay = (ImageView) v.findViewById(R.id.upper_lane_overlay);
		lower_lane_overlay = (ImageView) v.findViewById(R.id.lower_lane_overlay);
		mAlphacounter = 0;
		
		// Set Scanner- Button Text for first Start
		scanner.setText(R.string.scan_track);		
		
		//disable this button at the beginning
		rescan.setVisibility(View.GONE);
		
		scanner.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (scanner.getText() == getString(R.string.scan_track)) {
					mAlphacounter = 100;					;		
					
					if(frame_track_overlay == null)
						Log.i("debug","kein Overlay :-(");
					frame_track_overlay.setImageBitmap(mFrameToProcess.generate_track_overlay());
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
					float scale = getActivity().getResources().getDisplayMetrics().density;
					
					RelativeLayout.LayoutParams lp_upper = new RelativeLayout.LayoutParams(upper_lane_overlay.getLayoutParams());
					
					lp_upper.setMargins((int) (70*scale),(int) (mFrameToProcess.getCenterOfLanes()[0]/scale), 0, 0);
					upper_lane_overlay.setLayoutParams(lp_upper);
					
					RelativeLayout.LayoutParams lp_lower = new RelativeLayout.LayoutParams(lower_lane_overlay.getLayoutParams());
					lp_lower.setMargins((int) (70*scale), (int) (mFrameToProcess.getCenterOfLanes()[1]/scale), 0, 0);
					lower_lane_overlay.setLayoutParams(lp_lower);
					
					upper_lane_overlay.setAlpha((float) 0.4);
					lower_lane_overlay.setAlpha((float) 0.4);
			
				} else if (scanner.getText() == getString(R.string.scan_cars)) {
					
					mFrameToProcess.get_cars_position_and_colors();
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
				scanner.setEnabled(true);
				mScanningComplete = false;
				lap_mode.setEnabled(false);
				lap_count.setEnabled(false);
				min_mode.setEnabled(false);
				min_count.setEnabled(false);
				scanner.setText(R.string.scan_track);
			}
		});
		
		results.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(mContext, SettingsFragmentActivity.class);
				getActivity().finish();
				getActivity().startActivity(myIntent);
			}
		});	

		lap_mode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				if(lap_count.getText().toString().equals("")){					
					Toast.makeText(v.getContext(), "Bitte Rundenanzahl angeben!", Toast.LENGTH_SHORT).show();
					return;
				}
				mCount = Integer.parseInt(lap_count.getText().toString());

				Player p = new Player(LEFT_LANE, ROUND_MODE, new Scalar(255, 0,	0, 255));
				race = new Race(mCount, ROUND_MODE);	
				mRaceBundle.putLong("Anzahl", mCount);
				Fragment racefragment = new RaceFragment();
				racefragment.setArguments(mRaceBundle);			
				((RaceFragmentActivity) getActivity()).getViewPager().setCurrentItem(RACE);				
				((RaceFragment) ((RaceFragmentActivity) getActivity())
						.getSupportFragmentManager().findFragmentByTag(
								"android:switcher:" + R.id.pager + ":1"))
						.startCountdown();				
				Log.i("debug", "Moved to Race-Tab!");
			}
		});
		
		min_mode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				if(min_count.getText().toString().equals("")){					
					Toast.makeText(v.getContext(), "Bitte Minutenanzahl angeben!", Toast.LENGTH_SHORT).show();
					return;
				}
				mCount = Integer.parseInt(min_count.getText().toString());
				Player p = new Player(LEFT_LANE, TIMER_MODE, new Scalar(255, 0,	0, 255));
				race = new Race(mCount, TIMER_MODE);	
				
				mRaceBundle.putLong("Anzahl", mCount);
				Fragment racefragment = new RaceFragment();
				racefragment.setArguments(mRaceBundle);			
				((RaceFragmentActivity) getActivity()).getViewPager().setCurrentItem(RACE);				
				((RaceFragment) ((RaceFragmentActivity) getActivity())
						.getSupportFragmentManager().findFragmentByTag(
								"android:switcher:" + R.id.pager + ":1"))
						.startCountdown();				
				Log.i("debug", "Moved to Race-Tab!");
			}
		});

		return v;
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
		// TODO Automatisch generierter Methodenstub
	}

	@Override
	public void onCameraViewStopped() {
		// TODO Automatisch generierter Methodenstub
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		
		mFrameToProcess = ObjectDetector.getInstance(inputFrame);
		
		return inputFrame.rgba();
	
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i("debug", "onResume (StartFragament) called");
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, mContext, mLoaderCallback);
	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(mContext) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i("debug", "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
				if(mFirstInitialization){
				try { 	
				    bridge_image = Utils.loadResource(mContext,R.drawable.bridge_image); 
				    } catch (IOException e) { 
				     e.printStackTrace(); 
				    } 
				try { 		
				    crossed_image = Utils.loadResource(mContext,R.drawable.crossed_image); 
				    } catch (IOException e) { 
				     e.printStackTrace(); 
				    }
				
				bridge = new Track("Brückenbahn", false, 500, bridge_image);
				crossed = new Track("Kreuzungsbahn", true, 700, crossed_image);		
				tracks.add(bridge.getName());
				tracks.add(crossed.getName());

				mTracksAdapter.notifyDataSetChanged();				
				mFirstInitialization = false;
				}
			}
			
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

}
