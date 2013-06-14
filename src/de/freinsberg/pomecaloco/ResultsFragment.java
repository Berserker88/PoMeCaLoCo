package de.freinsberg.pomecaloco;
import com.example.pomecaloco.R;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
 
public class ResultsFragment  extends Fragment {
    Context c;
 
        public ResultsFragment(){
         
    }

 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	c = RaceFragmentActivity.mContext;
        View v = inflater.inflate(R.layout.results, null);
        return       v;
    }
}