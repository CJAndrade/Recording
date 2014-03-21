package com.cja.wearablerecorder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SelectedAdapter extends ArrayAdapter{
	private String Recording_Path = "wearableRecord";
	// used to keep selected position in ListView
	File file;
	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	private int selectedPos = -1;	// init value for not-selected
	private String SDCardfolder = Environment.getExternalStorageDirectory()
    		.getAbsolutePath() + File.separator +Recording_Path+File.separator;
	public SelectedAdapter(Context context, int textViewResourceId,
                       List objects) {
		super(context, textViewResourceId, objects);
	}

	public void setSelectedPosition(int pos){
		selectedPos = pos;
		// inform the view of this change
		notifyDataSetChanged();
	}

	public int getSelectedPosition(){
		return selectedPos;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    View v = convertView;

	    // only inflate the view if it's null
	    if (v == null) {
	        LayoutInflater vi
                        = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        v = vi.inflate(R.layout.selected_song, null);
	    }

	    // get text view
	    
        TextView label = (TextView)v.findViewById(R.id.txtExample);
        TextView songTime = (TextView)v.findViewById(R.id.txt_time);
        TextView songDate = (TextView)v.findViewById(R.id.txt_date);

        //LinearLayout listofSongs = (LinearLayout)v.findViewById(R.id.Lay_playerlist);
        // change the row color based on selected state
        if(selectedPos == position){
        	label.setBackgroundColor(Color.BLUE);
        	songTime.setBackgroundColor(Color.BLUE);
        	songDate.setBackgroundColor(Color.BLUE);
        }else{
        	label.setBackgroundColor(Color.TRANSPARENT);
        	songTime.setBackgroundColor(Color.TRANSPARENT);
        	songDate.setBackgroundColor(Color.TRANSPARENT);
        }

        label.setText(this.getItem(position).toString());//Recording Name
       
        File file =new File(SDCardfolder+this.getItem(position).toString());
        songDate.setText("Recorded on:"+sdf.format(file.lastModified()).toString());
        
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(file.toString());
     // convert duration to minute:seconds
        String duration =  metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
 
            long dur = Long.parseLong(duration);
            String seconds = String.valueOf((dur % 60000) / 1000);
            String minutes = String.valueOf(dur / 60000);
            if (seconds.length() == 1) {
            	songTime.setText("0" + minutes + ":0" + seconds);
            }else {
            	songTime.setText("0" + minutes + ":" + seconds);
            }
        return(v);
	}
}
