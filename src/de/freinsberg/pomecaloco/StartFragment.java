package de.freinsberg.pomecaloco;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.Toast;

public class StartFragment extends Fragment implements CvCameraViewListener2 {

	public static boolean mScanningComplete = false;
	public static boolean mFirstInitialization = true;
	public static final int ROUND_MODE = 1;
	public static final int TIMER_MODE = 2;
	public static final int LEFT_LANE = 1;
	public static final int RIGHT_LANE = 2;
	final public static int PREPARE_RACE = 0;
	final public static int RACE = 1;
	final public static int END_RACE = 2;
	private Race race;
	private Context c;
	public static CameraBridgeViewBase mOpenCvCameraView;
	private Track bridge, crossed;
	private List<String> tracks = new ArrayList<String>();
	private Mat bridge_image, crossed_image;
	ObjectDetector mFrame_to_process;
	private Spinner mTracks;
	private ArrayAdapter<String> mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		c = RaceFragmentActivity.mContext;
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
		if(!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, c,
				mLoaderCallback))
						Log.i("debug", "OpenCV wurde not initialized properly gerade.");
		
		
		
		
		mTracks = (Spinner) v.findViewById(R.id.choose_track);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		mAdapter = new ArrayAdapter<String>(c,
				android.R.layout.simple_spinner_item, tracks);
		// Specify the layout to use when the list of choices appears
		mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner		
		mTracks.setAdapter(mAdapter);
		
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

		

		// Adding Layout Buttons to this onCreate to interact with them
		// (fetching and setting data)
		final Button lap_mode = (Button) v.findViewById(R.id.lap_mode);
		final EditText lap_count = (EditText) v.findViewById(R.id.lap_count);
		final Button min_mode = (Button) v.findViewById(R.id.min_mode);
		final EditText min_count = (EditText) v.findViewById(R.id.min_count);
		// Making these Button disables so user can't interact with them before
		// scanning is complete.
		lap_mode.setEnabled(false);
		lap_count.setEnabled(false);
		min_mode.setEnabled(false);
		min_count.setEnabled(false);

		final Button results = (Button) v.findViewById(R.id.results);
		final Button scanner = (Button) v.findViewById(R.id.scanner);

		// Set Scanner- Button Text for first Start
		scanner.setText(R.string.scan_track);
		final Button rescan = (Button) v.findViewById(R.id.rescan);
		rescan.setVisibility(View.GONE);


		
		scanner.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (scanner.getText() == getString(R.string.scan_track)) {
					Log.i("debug", "Track scanned!");
					scanner.setText(R.string.scan_cars);
				} else if (scanner.getText() == getString(R.string.scan_cars)) {
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
				Intent myIntent = new Intent(c, SettingsFragmentActivity.class);
				getActivity().finish();
				getActivity().startActivity(myIntent);
			}
		});

		lap_mode.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				String count = lap_count.getText().toString();
				if(count.equals("")){					
					Toast.makeText(v.getContext(), "Bitte Rundenanzahl angeben!", Toast.LENGTH_SHORT).show();
					return;
				}
				Player p = new Player(LEFT_LANE, ROUND_MODE, new Scalar(255, 0,	0, 255));
				race = new Race(Integer.parseInt(count), ROUND_MODE);	
				Bundle data = new Bundle();
				data.putString("Anzahl", count);
				Fragment racefragment = new RaceFragment();
				racefragment.setArguments(data);
			
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
				InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				
				String count = min_count.getText().toString();
				if(count.equals("")){					
					Toast.makeText(v.getContext(), "Bitte Minutenanzahl angeben!", Toast.LENGTH_SHORT).show();
					return;
				}
				Player p = new Player(LEFT_LANE, TIMER_MODE, new Scalar(255, 0,	0, 255));
				race = new Race(Integer.parseInt(count), TIMER_MODE);	
				Bundle data = new Bundle();
				data.putString("Anzahl", count);
				Fragment racefragment = new RaceFragment();
				racefragment.setArguments(data);
			
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

		mFrame_to_process = new ObjectDetector(inputFrame);

		return mFrame_to_process.draw_colorrange_on_frame(
				((RaceFragmentActivity) getActivity()).getDisplay(),
				new Scalar(0, 0, 0, 100), new Scalar(100, 100, 100, 255));
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i("debug", "onResume (StartFragament) called");
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, c, mLoaderCallback);

	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(c) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i("debug", "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
				if(mFirstInitialization){
				try { 	
				    bridge_image = Utils.loadResource(c,R.drawable.bridge_image); 
				    } catch (IOException e) { 
				     e.printStackTrace(); 
				    } 
				try { 		
				    crossed_image = Utils.loadResource(c,R.drawable.crossed_image); 
				    } catch (IOException e) { 
				     e.printStackTrace(); 
				    } 
				bridge = new Track("Brückenbahn", false, 500, bridge_image);
				crossed = new Track("Kreuzungsbahn", true, 700, crossed_image);
				
				tracks.add(bridge.getName());
				tracks.add(crossed.getName());
				mAdapter.notifyDataSetChanged();				
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
