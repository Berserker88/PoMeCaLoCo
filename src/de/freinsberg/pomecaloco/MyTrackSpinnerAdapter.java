package de.freinsberg.pomecaloco;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyTrackSpinnerAdapter extends ArrayAdapter<Track>{
	Context context;
	List<Track> objects;

	public MyTrackSpinnerAdapter(Context context, int resource, int textViewResourceId,
			List<Track> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		this.objects = objects;
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

        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row=inflater.inflate(R.layout.choose_track_spinner, parent, false);
        TextView label=(TextView)row.findViewById(R.id.choose_track_spinner_text);
        label.setText(objects.get(position).getName());

        ImageView icon=(ImageView)row.findViewById(R.id.choose_track_spinner_icon);
        icon.setImageBitmap(objects.get(position).getImage());

        return row;
        }

}
