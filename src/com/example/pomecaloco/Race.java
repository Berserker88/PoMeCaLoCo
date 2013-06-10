package com.example.pomecaloco;



import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import android.widget.Spinner;
 
public class Race extends Fragment implements CvCameraViewListener{
    Context c;
    private CameraBridgeViewBase mOpenCvCameraView;

    public Race(){
         
    }
    public Race(Context c) {
        this.c = c;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.race, null);   
        Log.i("debug","CreateRaceView");
        
        mOpenCvCameraView = (CameraBridgeViewBase) v.findViewById(R.id.camera_stream);
        
        Log.i("debug", Float.toString(mOpenCvCameraView.getRotation()));
        Log.i("debug","Bound Camera to XML");
	     mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
	     Log.i("debug","make CameraView visible");
	     mOpenCvCameraView.setCvCameraViewListener(this);
	     Log.i("debug","setCVCameraViewListener properly");
	    
        Spinner set_track = (Spinner) v.findViewById(R.id.choose_track);
     // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(c,
                R.array.tracks, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        set_track.setAdapter(adapter);       
        return v;

    }    
    
	 @Override
	 public void onPause()
	 {
	     super.onPause();
	     if (mOpenCvCameraView != null)
	         mOpenCvCameraView.disableView();
	 }

	 public void onDestroy() {
	     super.onDestroy();
	     if (mOpenCvCameraView != null)
	         mOpenCvCameraView.disableView();
	 }

	 public void onCameraViewStarted(int width, int height) {

	 }

	 public void onCameraViewStopped() {
	 }
		@Override
		public Mat onCameraFrame(Mat inputFrame) {
			
			return inputFrame;
		}
    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, c, mLoaderCallback);
    }

	
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(c) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("debug", "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
}

	