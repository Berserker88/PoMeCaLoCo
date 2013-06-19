package de.freinsberg.pomecaloco;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
 
public class SetupFragment  extends Fragment {
	
    private Context mContext;
 
        public SetupFragment(){
         
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	mContext = RaceFragmentActivity.mContext;
        View v = inflater.inflate(R.layout.setup, null);
        return v;
    }
}