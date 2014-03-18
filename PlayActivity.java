package com.cja.wearablerecorder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;




public class PlayActivity extends Activity{
    private int currentID = 0;
    private ImageButton playButton;
    private ListView myListView;
	private TextView startText;
	private TextView endText;
	private MediaPlayer mp;
	private int duration;
	private ProgressBar progressBar;
	private SelectedAdapter selectedAdapter; 
	private ArrayList list;
	public enum SongStatus {
		INIT,PLAYING,PAUSE,STOPED;
	}
	private SongStatus songStatus;
	private String Recording_Path = "wearableRecord";
	   //private String Recording_Path= "MyMusic";
    private String SDCardfolder = Environment.getExternalStorageDirectory()
    		.getAbsolutePath() + File.separator +Recording_Path+File.separator;
    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) { //CJA change from bundle
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_player);
       //Setting the action bar
        ActionBar actionBar = getActionBar();
        //actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setIcon(R.drawable.ic_action_mic);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle("Player");
        actionBar.setDisplayHomeAsUpEnabled(true);

        songStatus = SongStatus.INIT;
     // Creating a  simple a list
     		list = new ArrayList();
           File file = new File(SDCardfolder);
           File[] files = file.listFiles(); 
           if (null != files) {
               for (int i = 0; i < files.length; i++) {
                   if (files[i].getName().endsWith("mp3")) {
                	   list.add(files[i].getName());
                   }
                   }
                   }
    		selectedAdapter = new SelectedAdapter(this,0,list);
    		selectedAdapter.setNotifyOnChange(true);

            myListView = (ListView) findViewById(R.id.listViewSongs);
            myListView.setAdapter(selectedAdapter);

            myListView.setOnItemClickListener(new OnItemClickListener() {
    			@Override
    			public void onItemClick(AdapterView arg0, View view,
                                               int position, long id) {
    				// user clicked a list item, make it "selected"
    				selectedAdapter.setSelectedPosition(position);
    				  Log.v("CJAPlayer",
    		                    "PlayActivity onItemClick: position" + position
    		                            + "id: " + id);
    		    
    		            currentID = position;    		           
    		            if((songStatus==SongStatus.INIT) || (songStatus==SongStatus.PAUSE))
    		            {
    		            	playSong();
    		            }else //there is a previous song that is playing
    		            {
    		        	 if ((mp != null) && (mp.isPlaying())) {
    		        	 mp.stop();
    		        	// mp.release(); 
    		        	 }
    		        	 playSong(); 
    		         }
    			}
            });

        processViews();//Initialize other buttons 

        //playSong(); //this will irritate the user  
		progressBar = (ProgressBar) findViewById(R.id.songProgress);
		progressBar.setProgress(0);		
		progressBar.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if((songStatus==SongStatus.PLAYING) || (songStatus==SongStatus.PAUSE)){
					progressBar.setProgress((int)(((double)arg1.getX()/(double)arg0.getWidth())*((int)((ProgressBar)arg0).getMax())));
					mp.start();
					mp.seekTo(progressBar.getProgress());

					new RefreshSongTimeAsyncTask().execute();
				}
				return true;
			}
		});
    }

    private void playSong() {
        Log.v("CJAPlayer", "PlayActivity --> playSong. currentID: "
                + currentID);
		if(!(songStatus==SongStatus.PAUSE)){
        mp = new MediaPlayer();
        try {
			mp.setDataSource(SDCardfolder+myListView.getItemAtPosition(currentID).toString());
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			mp.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        duration = mp.getDuration();
        progressBar.setMax(duration);
		}//end of if songstatus
        mp.start();
        songStatus = SongStatus.PLAYING;
        playButton.setImageDrawable(this.getResources().getDrawable(R.drawable.pause));
        new RefreshSongTimeAsyncTask().execute();

    }
    
  private void processViews() {
        playButton = (ImageButton) findViewById(R.id.btn_play);
        playButton.setOnClickListener(new PlayBtnListener());

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.songProgress);
    	progressBar.setProgress(0);
    	
		startText = (TextView) findViewById(R.id.startTimeTextView);
		endText = (TextView) findViewById(R.id.endTimeTextView);
    }
    
    class PlayBtnListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Log.v("CJAPlayer", "Play Button is Pressed");

            //stoping the song 
			if ((mp != null) && (mp.isPlaying())) {
				mp.pause();
				songStatus = SongStatus.PAUSE;
				playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_play));
			}else
			{
				playSong();
			}
			
        }
    }
    
   
	// Refresh progress bar and text about times and percentages of download of the song
	public class RefreshSongTimeAsyncTask extends AsyncTask<Void, Integer, Void> {
		Integer totalBytes;
		int countSum;
		
		@Override
		protected void onPostExecute(Void result) {

		}

		@Override
		protected void onPreExecute() {
			//String fileName = SDCardfolder+myListView.getItemAtPosition(currentID);
			
			if(mp!=null){
				int currentMillis = mp.getCurrentPosition();
				int totalMillis = mp.getDuration();
				int seconds = (int) (TimeUnit.MILLISECONDS.toSeconds(currentMillis) % 60);
				if(seconds>9){
					startText.setText(TimeUnit.MILLISECONDS.toMinutes(currentMillis) + ":" + seconds);
				} else{
					startText.setText(TimeUnit.MILLISECONDS.toMinutes(currentMillis) + ":" + "0" + seconds);
				}
				seconds = (int) (TimeUnit.MILLISECONDS.toSeconds(totalMillis) % 60);
				if(seconds>9){
					endText.setText(TimeUnit.MILLISECONDS.toMinutes(totalMillis) + ":" + seconds);
				} else{
					endText.setText(TimeUnit.MILLISECONDS.toMinutes(totalMillis) + ":" + "0" + seconds);
				}
				progressBar.setProgress(currentMillis);
				progressBar.setMax(totalMillis);
			} 
		}

		@Override
		protected Void doInBackground(Void... params) {
			while(songStatus == SongStatus.PLAYING){
        		int millis = mp.getCurrentPosition();
        		publishProgress(millis);
        		SystemClock.sleep(1000);
        	}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			switch (songStatus) {
			case PLAYING:
				int seconds = (int) (TimeUnit.MILLISECONDS.toSeconds(values[0]) % 60);
				if(seconds>9){
					startText.setText(TimeUnit.MILLISECONDS.toMinutes(values[0]) + ":" + seconds);
				} else{
					startText.setText(TimeUnit.MILLISECONDS.toMinutes(values[0]) + ":" + "0" + seconds);
				}					
				progressBar.setProgress(values[0]);	
				break;
			default:
				break;
			}	
		}
	}

}
