package com.example.pomecaloco;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
 
public class Results  extends Fragment {
    Context c;
 
        public Results(){
         
    }
    public Results(Context c) {
        this.c = c;
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.results, null);
        return       v;
    }
}