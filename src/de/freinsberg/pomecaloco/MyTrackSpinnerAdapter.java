package de.freinsberg.pomecaloco;

import java.io.ByteArrayInputStream;
import java.util.List;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This class represents an adapter for a spinner. It is used to fill trackname according to their picture next to each other into a row of the spinner.
 * @author freinsberg
 *
 */
public class MyTrackSpinnerAdapter extends ArrayAdapter<Pair<String, byte[]>>{
	
	private Context mContext;		
	private List <Pair<String, byte[]>> mObjects;
	/**
	 * Constructor: Creates an adapter with the given params.
	 * @param context The context where the adapter is used.
	 * @param textViewResourceId The resourceid of the layout file that represents the row in the spinner.
	 * @param objects This is a list of Pairs, including String and byte[], representing the trackname and a picture.
	 */
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

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row=inflater.inflate(R.layout.choose_track_spinner, parent, false);
        
        TextView label=(TextView)row.findViewById(R.id.choose_track_spinner_text);
        label.setText(mObjects.get(position).getL());
        
        ImageView icon=(ImageView)row.findViewById(R.id.choose_track_spinner_icon);            
        ByteArrayInputStream stream = new ByteArrayInputStream(mObjects.get(position).getR());
        icon.setImageBitmap(BitmapFactory.decodeStream(stream));

        return row;
        }
    
    /**
     * This method gets the position of a given track.
     * @param preselectedTrack The track for which the position is needed.
     * @return The position as an interger representative or -1 if something goes wrong.
     */
	public int getPosition(String preselectedTrack) {
		int i = 0;
		Log.i("debug","PreselectedTrack: " + preselectedTrack);
		for(Pair<String, byte[]> p : mObjects) {
			if(p.getL().equals(preselectedTrack))
				return i;
			i++;
		}
		return -1;
	}
}
