package com.example.pomecaloco;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
 
public class SetupFragment  extends Fragment {
    Context c;
 
        public SetupFragment(){
         
    }

 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	c = RaceFragmentActivity.mContext;
        View v = inflater.inflate(R.layout.setup, null);
        return       v;
    }
}