package de.freinsberg.pomecaloco;

import java.io.File;
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
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
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

public class StartActivity extends Activity implements CvCameraViewListener2 {

	public static boolean mScanningComplete = false;
	public static boolean mFirstInitialization = true;

	final public static int LEFT_LANE = 1;
	final public static int RIGHT_LANE = 2;


	private Handler mHandler = new Handler();
	private Timer mShotTimer = new Timer();
	private TimerTask mShotTask;
	private int mAlphacounter;
	private float mAlpha;
	private Race race;
	private Context mContext;
	public static CameraBridgeViewBase mOpenCvCameraView;
	private Mat mInputFrame;
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
	private ImageView lane_overlay = null;
	private ImageView left_car_color;
	private ImageView right_car_color;
	private Bitmap[] mCarColorBitmaps;
	private int mCount;
	private ArrayAdapter<String> mTracksAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {	
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start);
		mContext = getApplicationContext();
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
		mTracksAdapter = new ArrayAdapter<String>(this,	android.R.layout.simple_spinner_item, tracks);
		
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
					left_car_color.setImageBitmap(mCarColorBitmaps[0]);					
					right_car_color.setImageBitmap(mCarColorBitmaps[1]);					
					left_car_color.setVisibility(View.VISIBLE);								
					right_car_color.setVisibility(View.VISIBLE);	
					
					switch(mFrameToProcess.car_status()){
						case ObjectDetector.NO_CAR:
							Toast.makeText(v.getContext(), "Kein Fahrzeug gefunden!", Toast.LENGTH_LONG).show();
							return;
						case ObjectDetector.RIGHT_CAR:							
							Toast.makeText(v.getContext(), "Links kein Fahrzeug gefunden!", Toast.LENGTH_LONG).show();
							break;
						case ObjectDetector.LEFT_CAR:							
							Toast.makeText(v.getContext(), "Rechts kein Fahrzeug gefunden!", Toast.LENGTH_LONG).show();
							break;
						case ObjectDetector.BOTH_CAR:
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
				lap_mode.setEnabled(false);
				lap_count.setEnabled(false);
				min_mode.setEnabled(false);
				min_count.setEnabled(false);
			
				scanner.setText(R.string.scan_track);
			}
		});
		
		results.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent().setClass(v.getContext(), ResultsFragment.class);
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
				race = Race.getInstance();
				race.newRace(mCount, Race.ROUND_MODE);
				race.createPlayer(mFrameToProcess.car_status(),Race.ROUND_MODE, mCount);	
				
				Intent intent = new Intent().setClass(v.getContext(), RaceActivity.class);
				startActivity(intent);			
			
				Log.i("debug", "Moved to Race-Activity!");
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
				race = Race.getInstance();
				race.newRace(mCount, Race.TIMER_MODE);
				race.createPlayer(mFrameToProcess.car_status(),Race.TIMER_MODE, mCount);
				
				Intent intent = new Intent().setClass(v.getContext(), RaceActivity.class);
				startActivity(intent);			
			
				Log.i("debug", "Moved to Race-Activity!");
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
		// TODO Automatisch generierter Methodenstub
	}

	@Override
	public void onCameraViewStopped() {
		// TODO Automatisch generierter Methodenstub
	}
	int i = 0;
	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		
		if(!inputFrame.rgba().empty()) {
			if(mInputFrame != null)
				mInputFrame.release();
			
			mInputFrame = inputFrame.rgba();
		}
			
//			mFrameToProcess = ObjectDetector.getInstance(inputFrame);
		
//		Mat a = inputFrame.gray();
//		Mat b = new Mat(a.width(),a.height(), a.type());
//		//Imgproc.cvtColor(a, a, Imgproc.COLOR_BGRA2BGR);
//		if(i == 200){
//			Log.i("debug", "a ->  size: "+a.size()+", type:"+a.type()+", channels:"+a.channels()+", depth:"+a.depth()+", dump:"+a.dump());
//			
//			Highgui.imwrite(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "a.png").toString(), a);		
//		}
//		
//		a = a.t();

//		Imgproc.cvtColor(a, a, Imgproc.COLOR_RGBA2BGRA);
		//Imgproc.cvtColor(a, a, Imgproc.COLOR_BGR2BGRA);
//		Core.flip(a.t(), b, 1);
//		Point img_center = new Point(a.cols()/2,a.rows()/2);
//		Mat rot_mat = Imgproc.getRotationMatrix2D(img_center, 270, 1.0);
//		Imgproc.warpAffine(a, b, rot_mat, b.size());
//		Imgproc.cvtColor(b, b, Imgproc.COLOR_BGR2RGBA);
//		if(i == 200)
//			Log.i("debug", "b ->  size: "+b.size()+", type:"+b.type());
//			Highgui.imwrite(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "ROTATED.png").toString(), b);
//		b.copyTo(a);
//		i++;
////		a.release();
//		rot_mat.release();

		//mFrameToProcess = ObjectDetector.getInstance(inputFrame);
//		i++;
//		if(i == 200){
//		Log.i("debug", "b.t() ->  size: "+b.size()+", type:"+b.type()+", channels:"+b.channels()+", depth:"+b.depth()+", dump:"+b.dump());
//		Highgui.imwrite(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "a.t().png").toString(), b);	
//	}
		return inputFrame.rgba();
	
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i("debug", "onResume (StartFragament) called");
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, this, mLoaderCallback);
	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i("debug", "OpenCV loaded successfully");					
				mOpenCvCameraView.enableView();
				//Static adding of tracks
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
				}

				mTracksAdapter.notifyDataSetChanged();				
				mFirstInitialization = false;
				
			
			
				break;
			}
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

}
