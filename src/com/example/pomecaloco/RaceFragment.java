package com.example.pomecaloco;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Mat;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

public class RaceFragment extends Fragment implements CvCameraViewListener2{
	
	
		
		private Context c;
		public static CameraBridgeViewBase mOpenCvCameraView;
		
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			c = RaceFragmentActivity.mContext;
			View v = inflater.inflate(R.layout.race, null);
			

			mOpenCvCameraView = (CameraBridgeViewBase) v
					.findViewById(R.id.camera_stream_race);		
			
				mOpenCvCameraView.setVisibility(SurfaceView.INVISIBLE);		
			mOpenCvCameraView.setCvCameraViewListener(this);
			Log.i("debug", "setCVCameraViewListener for Race properly");	
			
				
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
			
			
			
			return inputFrame.rgba();
		}
		@Override
		public void onResume() {
			super.onResume();
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, c,
					mLoaderCallback);
			
		}

		private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(c) {
			@Override
			public void onManagerConnected(int status) {
				switch (status) {
				case LoaderCallbackInterface.SUCCESS: {
					Log.i("debug", "OpenCV loaded successfully");
					mOpenCvCameraView.enableView();
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
	

