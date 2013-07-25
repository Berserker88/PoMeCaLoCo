package de.freinsberg.pomecaloco;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyTrackSpinnerAdapter extends ArrayAdapter<Pair<String, byte[]>>{
	
	private Context mContext;		
	private List <Pair<String, byte[]>> mObjects;
	//private static ArrayList <Pair<String, Bitmap>> mTracks;
 	

	public MyTrackSpinnerAdapter(Context context, int textViewResourceId, List<Pair<String, byte[]>> objects) {
		super(context, textViewResourceId, objects);
		this.mObjects = objects;
		this.mContext = context;
		
		
		
	}
	@Override
    public View getDropDownView(int position, View convertView,ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }
	
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row=inflater.inflate(R.layout.choose_track_spinner, parent, false);
        
        TextView label=(TextView)row.findViewById(R.id.choose_track_spinner_text);
        label.setText(mObjects.get(position).getL());
        
        ImageView icon=(ImageView)row.findViewById(R.id.choose_track_spinner_icon);            
        ByteArrayInputStream stream = new ByteArrayInputStream(mObjects.get(position).getR());
        icon.setImageBitmap(BitmapFactory.decodeStream(stream));

        return row;
        }
    
    

}
