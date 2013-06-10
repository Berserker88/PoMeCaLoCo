package com.example.pomecaloco;

import java.io.IOException;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.example.pomecaloco.MyDetector;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    
    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        mCamera.setFaceDetectionListener(new MyDetector());
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            
    }

    public void startFaceDetection(){
        // Try starting Face Detection
        Camera.Parameters params = mCamera.getParameters();

        // start face detection only *after* preview has started
        if (params.getMaxNumDetectedFaces() > 0){
            // camera supports face detection, so can start it:
            mCamera.startFaceDetection();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
    	Camera.Parameters params = mCamera.getParameters();      	
    	Size previewsize = params.getSupportedPreviewSizes().get(0);
    	Size videosize = params.getPreferredPreviewSizeForVideo();
    	    	
    	mCamera.setDisplayOrientation(90);
    	Log.i("debug","Videosize: "+videosize);
    	Log.i("debug","Fokusmodus: "+params.getFocusMode());
    	Log.i("debug","Vorschaugröße: "+previewsize.width+previewsize.height);   	        	
    	  	
    	
    	mCamera.setParameters(params);
        try {

            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            startFaceDetection(); // start face detection feature
        } catch (IOException e) {
            Log.d(VIEW_LOG_TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    	mCamera.release();
        // empty. Take care of releasing the Camera preview in your activity.
    }

    
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    	
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);                   
            mCamera.startPreview();
            startFaceDetection(); // re-start face detection feature
        } catch (Exception e){
            Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
        }            
    }        
}