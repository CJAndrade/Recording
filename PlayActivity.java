package com.cja.wearablerecorder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import android.widget.Toast;




public class PlayActivity extends Activity{
    private int currentID = 0;
    private AdView mAdView;
    private ImageButton playButton;
    private ListView myListView;
	private TextView startText;
	private TextView endText;
	private MediaPlayer mp = null;
	private int duration;
	Toast toast;
	private String messagePass;
	private ProgressBar progressBar= null;
	RefreshSongTimeAsyncTask RefreshSongTime =null;
	private SelectedAdapter selectedAdapter; 
	private ArrayList list;
	private ArrayList listDate;
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
        //Addding Admobs
        mAdView = (AdView) findViewById(R.id.adView);
        mAdView.setAdListener(new ToastAdListener(this));
        mAdView.loadAd(new AdRequest.Builder().build());
     // Creating a  simple a list
     		list = new ArrayList();

           File file = new File(SDCardfolder);
           File[] files = file.listFiles(); 
           Arrays.sort(files, new Comparator<File>(){ //Sorting files desc
        	    public int compare(File f1, File f2)
        	    {
        	        return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
        	    } });
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
    		        	 }
    		        	 playSong(); 
    		         }
    			}
            });
      //CJADel to mark the button as play does not work.
            if (mp != null){
            mp.setOnCompletionListener(new OnCompletionListener(){
            public void onCompletion(MediaPlayer mp) {
            	playButton.setImageDrawable(getResources().getDrawable(R.drawable.btn_play));
            }
        });
            }
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

					RefreshSongTimeAsyncTask RefreshSongTime= new RefreshSongTimeAsyncTask();
					RefreshSongTime.execute();
				}
				return true;
			}
		});
    
    }
   
    

    private void playSong() {
        Log.v("CJAPlayer", "PlayActivity playSong. currentID: "
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
			Log.v("CJAPlayer", "mp.prepare();");
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        duration = mp.getDuration();
        Log.v("CJAPlayer", "duration");
        progressBar.setMax(duration);
        Log.v("CJAPlayer", "progressBar");
		}//end of if songstatus
        mp.start();
        songStatus = SongStatus.PLAYING;
        playButton.setImageDrawable(this.getResources().getDrawable(R.drawable.btn_pause));
        RefreshSongTime = new RefreshSongTimeAsyncTask();
        RefreshSongTime.execute();
        //Log.v("CJAPlayer", "PlayActivity Status async execute: "+ RefreshSongTime.getStatus().toString());
       /* if(RefreshSongTime.getStatus() == AsyncTask.Status.FINISHED){
        	Log.v("CJAPlayer", "PlayActivity Status async execute: "
                    + RefreshSongTime.getStatus().toString());
        }*/ 

   
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
				playButton.setImageDrawable(getResources().getDrawable(R.drawable.btn_play));
			}else
			{
				playSong();
			}
			
        }
    }
    
   
	// Refresh progress bar and text about times and percentages of download of the song
	public class RefreshSongTimeAsyncTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPostExecute(Void result) {
			Log.v("CJAPlayer", "PlayActivity onPostExecute");
			playButton.setImageDrawable(getResources().getDrawable(R.drawable.btn_play));
		}

		@Override
		protected void onPreExecute() {
			if (!isCancelled()){
			if(mp!=null){
				Log.v("CJAPlayer", "PlayActivity onPreExecute if");
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
			}else {
				Log.v("CJAPlayer", "PlayActivity onPreExecute else");
				
			}
		}

	    @Override
	    protected void onCancelled() {
	    	Log.v("CJAPlayer", "PlayActivity onCancelled");
	        if ((mp != null)) {
	        	if(mp.isPlaying()){
	        	       	 mp.stop();
	        	       	 mp.release();
	        	       	 mp=null;
	        	       //progressBar=null;
	        	       songStatus = SongStatus.INIT;
	        	}
	        	
	        }
	    }
	    
		@Override
		protected Void doInBackground(Void... params) {
			
			Log.v("CJAPlayer", "PlayActivity doInBackground");
			while(songStatus == SongStatus.PLAYING){
				if(isCancelled()){ //cja
                    break;
                }
        		int millis = mp.getCurrentPosition();
                if (!mp.isPlaying()) {
                  break; //to complete doInBackground and move to onPostExecute 
              }
        		progressBar.setProgress(millis);
        		//publishProgress(millis);
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

	   @Override
	    public boolean onKeyDown(int keyCode, KeyEvent event) {
	        if (keyCode == KeyEvent.KEYCODE_BACK) {
	        	Log.v("CJAPlayer", "PlayActivity KEYCODE_BACK" );
	        	startActivity(new Intent(PlayActivity.this, MainActivity.class));
	        }
	        return super.onKeyDown(keyCode, event);
	    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
		Bundle bundle = new Bundle();
        switch (item.getItemId()) {
        case R.id.action_edit_player:
        	Intent shareORdel = new Intent(PlayActivity.this, ShareActivity.class);
        	bundle.putString("key_action", "Edit");
        	shareORdel.putExtras(bundle);
        	startActivity(shareORdel);
        	
            return true;

          case R.id.action_share_player:
        	//  Intent shareORdel2 = new Intent(PlayActivity.this, ShareActivity.class);
        	//  bundle.putString("key_action", "Share");
        	  //shareORdel2.putExtras(bundle);
        		Intent  shareIntent = new Intent() ;
            	shareIntent.setAction(Intent.ACTION_SEND);
            	shareIntent.setType("audio/*");
            	shareIntent.putExtra(Intent.EXTRA_STREAM,Uri.parse("file://"+SDCardfolder+myListView.getItemAtPosition(currentID).toString()));
            	startActivity(Intent.createChooser(shareIntent, "Share: "+myListView.getItemAtPosition(currentID).toString()));
          	 // startActivity(shareORdel2);
          	
          	return true;
          default:
              return super.onOptionsItemSelected(item);
        }
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		 MenuInflater inflater = getMenuInflater();
	        inflater.inflate(R.menu.activity_player_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}



	@Override
	protected void onStop() {
       	if(RefreshSongTime != null){
        	Log.v("CJAPlayer", "PlayActivity onStop()" + RefreshSongTime.getStatus().toString());
        	//RefreshSongTime.onCancelled();
        	RefreshSongTime.cancel(true);
        	}
		super.onStop();
	}



	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub	

		super.onRestart();
	}



	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		mAdView.destroy();
		super.onDestroy();
	}



	@Override
	protected void onPause() {
		mAdView.pause();
		// TODO Auto-generated method stub
		super.onPause();
	}



	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mAdView.resume();
	}

	
	


}
