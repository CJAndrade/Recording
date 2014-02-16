package com.cja.wearablerecorder;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.UUID;
//import java.util.logging.Handler;



import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.cja.wearablerecorder.ToastAdListener;

//import com.google.android.gms.samples.ads;
//import com.google.android.gms.samples.ads.ToastAdListener;



import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemClock;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
//import for MOGA controller
import com.bda.controller.Controller;
import com.bda.controller.ControllerListener;
import com.bda.controller.KeyEvent;
import com.bda.controller.MotionEvent;
import com.bda.controller.StateEvent;

import android.widget.TextView;

public class MainActivity extends Activity implements OnCompletionListener, ControllerListener {
    private static final String TAG = "CJAAudioRecorder";
    static Date d = new Date();
    //static CharSequence currentDateTime  = DateFormat.format("EEEE, MMMM d, yyyy ", d.getTime());
   private  String currentDateTime ;//= DateFormat.format("yyyy-MM-dd'T'hh:mm:ss", d).toString();
    private String OUT_FILE_NAME;// = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Rec"+currentDateTime+".m4a";
    private ImageButton recordButton;
    private  String fromPebble =null;
   // private ImageButton ;

    private ImageButton playButton;

    private ImageButton deleteButton;

    private MediaRecorder mediaRecorder = null;

     MediaPlayer mediaPlayer = null;
     //For CJA MOGA
     Controller mController = null;
//For CJANFC
     NfcAdapter adapter;
     PendingIntent pendingIntent;
     IntentFilter writeTagFilters[];
     boolean writeMode;
     Tag mytag;
     Context ctx;
     Toast toast;
     String checkRecorderState = "N";
    
   //CJAWtest   private Button pushButton;
    private File files;
    private String extStorageDirectory;
   //CJAWtest private EditText trackNameeditText; //CJARM for testing
  //CJAWtest	private TextView fromPebbletextView; //CJARM for testing
	private TextView fileNametextView;
	private Chronometer timechronometer;
   //for Admobs
	private AdView mAdView;

    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);
 
        return super.onCreateOptionsMenu(menu);
	   }
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
        //case R.id.action_Record: //removed, throwing handle to play music
            // search action
            //return true;
        case R.id.action_play:
        	     String query = "";
        		Intent intent = new Intent();
        		intent.setAction(MediaStore.INTENT_ACTION_MEDIA_SEARCH);
        		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		query = "Wearable Recorder";
        		intent.putExtra(MediaStore.EXTRA_MEDIA_ALBUM, "wearableRecord"); //CJATODO correct spelling
        		intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS,
        		MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE);
        		intent.putExtra(SearchManager.QUERY, query);
        		startActivity(intent);
            return true;
          case R.id.action_installpebble:
        	  //intall pbw on pebble
        	MainActivity.this.installWatchApp();
        	return true;
          case R.id.action_share:
          	  //sharing recording
            	Intent  shareIntent = new Intent() ;
            	shareIntent.setAction(Intent.ACTION_SEND);
            	shareIntent.setType("audio/*");
            	shareIntent.putExtra(Intent.EXTRA_STREAM,Uri.parse("file://"+extStorageDirectory+"/"+OUT_FILE_NAME));
            	startActivity(Intent.createChooser(shareIntent, "Share Recording"));
          	return true;
          case R.id.action_pebble:
        	  //Open pebble app on pebble  
        	  UUID AppId = UUID.fromString("8bb49bab-77fe-4028-bd5e-4fbf35e134e1"); //CJATODO define UUID as global variable
        	  if(PebbleKit.isWatchConnected(getApplicationContext())){
        		  PebbleKit.startAppOnPebble(getApplicationContext(), AppId);
        	  }else {
        		  
          	    toast = Toast.makeText(getApplicationContext(), "To open the recorder app on pebble, ensure that the Phone is connected to the Watch", Toast.LENGTH_LONG);
          		toast.setGravity(Gravity.TOP, 0, 0);
          		toast.show();		  
        	  }
        	  return true;
          case R.id.action_writeNFC: 
        	  try {
        		  ctx = this;
					if(mytag==null){
						Toast.makeText(ctx, ctx.getString(R.string.error_detected), Toast.LENGTH_LONG ).show();
					}else{
						
						write("WRecord",mytag);
						Toast.makeText(ctx, ctx.getString(R.string.oktowrite), Toast.LENGTH_LONG ).show();
					}
				} catch (IOException e) {
					Toast.makeText(ctx, ctx.getString(R.string.errortowrite), Toast.LENGTH_LONG ).show();
					e.printStackTrace();
				} catch (FormatException e) {
					Toast.makeText(ctx, ctx.getString(R.string.errortowrite) , Toast.LENGTH_LONG ).show();
					e.printStackTrace();
				}
        	  return true;
          case R.id.action_useMoga:
        	  //opening new view to show how to moga
        	  startActivity(new Intent(MainActivity.this, Using_Moga.class));
        	return true;
          case R.id.action_tellaFriend:
          	  //sharing recording
            	Intent  shareIntentFriend = new Intent() ;
            	shareIntentFriend.setAction(Intent.ACTION_SEND);
            	shareIntentFriend.setType("text/plain");
            	shareIntentFriend.putExtra(Intent.EXTRA_TEXT,"Try this new Audio Recorder that I am using,with great features. Install it from https://play.google.com/store/apps/details?id=com.cja.wearablerecorder");
            	startActivity(Intent.createChooser(shareIntentFriend, "Share the App with your Friends"));
          	return true;
          default:
            return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        super.setContentView(R.layout.activity_main);
        //Changing action bar
        ActionBar actionBar = getActionBar();
         //actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setTitle("Recorder");
        
        //for CJANFC with out intent filter in Manifest
		adapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
		writeTagFilters = new IntentFilter[] { tagDetected };
		
        
        this.fileNametextView = (TextView)super.findViewById(R.id.fileNametextView);
        this.timechronometer = (Chronometer)super.findViewById(R.id.timechronometer);
        this.recordButton = (ImageButton)super.findViewById(R.id.recordButton);
        //this.stopButton = (ImageButton)super.findViewById(R.id.stopButton);
        this.playButton = (ImageButton)super.findViewById(R.id.playButton);
        this.deleteButton = (ImageButton)super.findViewById(R.id.deleteButton);
       //CJAWtest this.trackNameeditText = (EditText)super.findViewById(R.id.trackNameeditText);//CJA
      //CJAWtest  this.pushButton = (Button)super.findViewById(R.id.pushButton); //CJA
      //CJAWtest  this.fromPebbletextView = (TextView)super.findViewById(R.id.fromPebbletextView);
        mAdView = (AdView) findViewById(R.id.adView);
        mAdView.setAdListener(new ToastAdListener(this));
        mAdView.loadAd(new AdRequest.Builder().build());
        //mAdView.setVisibility(View.GONE);//CJAWtest to take screenshots
        //Creating a new folder for storing the recordings
        files = new File(Environment.getExternalStorageDirectory().getAbsolutePath().toString()+"/wearableRecord");//CJATODO correct spelling
        files.mkdirs();
        extStorageDirectory = files.toString();
        
       // trackNameeditText.setText(currentDateTime);
        this.setButtonsEnabled(true, false, false);
        //for MOGA controller
        mController = Controller.getInstance(this);
        mController.init();
        Log.d("CJAMOGA",mController.toString());
        
        mController.setListener(this, new Handler());
		//reciving data from pebble 
        //CJATODO need to implement a broadcast reciver
        //from http://developer.getpebble.com/2/mobile-app-guide/android-guide.htm
        UUID AppId = UUID.fromString("8bb49bab-77fe-4028-bd5e-4fbf35e134e1");
		PebbleKit.registerReceivedDataHandler(this, new PebbleKit.PebbleDataReceiver(AppId) {
			@Override
			public void receiveData(Context context, int transactionId,
					PebbleDictionary data) {
				String val = data.getInteger(1).toString();
				// TODO Auto-generated method stub
				Log.d("CJAPebbleRecordBRval", "Request value : " + val);
				//CJAWtest fromPebbletextView.setText(val);
				//11 - Start recording Up button from pebble
				//22 - stop recording down button from pebble
				//CJACHG to switch case later
				if(val.contentEquals("11")){ 
						record(null);
			     }
				
				if(val.contentEquals("22")){ 
					fromPebble="peb";
					stop(null);
				}
			}
		});
		/*//CJAWtest 
        //CJARM testing using push button
        pushButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d("TAGCJA", "On button push click");
				
				//sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.parse(this.files.getAbsolutePath())));
				//sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,uri.fromFile(Environment.getExternalStorageDirectory())));
				//Log.d("TAGCreateNewfile", "before sendBroadcast on push button ");
	            //Log.d("TAGCreateNewfile", Uri.parse(this.files.getAbsolutePath()).toString());
				//Starting the peeble app on the pebble
				UUID AppId = UUID.fromString("8bb49bab-77fe-4028-bd5e-4fbf35e134e1");
				PebbleKit.startAppOnPebble(getApplicationContext(), AppId);
				  // Closing my app
				  //PebbleKit.closeAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);
				
				// TODO Auto-generated method stub
				final String recordName = trackNameeditText.getText().toString();
			    //CJATODO no IO on the UI thread
				//new Thread(new Runnable(){

					//@Override
					//public void run() {
						// TODO Auto-generated method stub
				//CJA - Exception handling if pebble is not connected to the phone.
				//https://developer.getpebble.com/2/mobile-app-guide/android-guide.html		
				Log.d("TAGCJA", "before pusing");
						
						PebbleDictionary data = new PebbleDictionary();
						data.addString(123, recordName);
						data.addString(456, "CJA247923749");
						data.addString(789, "CJA32746283");
						Log.d("TAGCJA", "just before pushing");
						PebbleKit.sendDataToPebble(MainActivity.this, AppId, data);
						Log.d("TAGCJA", "After pusing to pebble");
					//}
					
				//});
			}
		});//CJAWtest */
    }

    private void setButtonsEnabled(boolean record, boolean play, boolean delete) {
        this.recordButton.setEnabled(record);
       // this.stopButton.setEnabled(stop);
        this.playButton.setEnabled(play);
        this.deleteButton.setEnabled(delete);
    }

    public void record(View v) {
    	if (this.mediaRecorder != null){
    		stop(null);
    	}else{
    	Log.d(TAG, "recordNewfile");
        //Setting variable to check if mediarecorder is recoding 
        checkRecorderState ="Y";
        //changing the image of the mic button
        recordButton.setImageDrawable(this.getResources().getDrawable(R.drawable.mic_orange));
        //stopButton.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_media_stop_red));
        playButton.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_media_play));
        
        //Starting timer
        timechronometer.setBase(SystemClock.elapsedRealtime());
        timechronometer.start();
      //Creating a new file every time
        d = new Date();
        currentDateTime = DateFormat.format("yyyy-MM-dd'T'hh:mm:ss", d).toString();
        OUT_FILE_NAME= "Rec"+currentDateTime+".mp3"; //CJA m4a
        //CJAWtest trackNameeditText.setText(this.files.getAbsolutePath().toString());
        this.files = new File(extStorageDirectory,OUT_FILE_NAME);
        
        try {
			files.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			checkRecorderState ="N";
			Log.d("TAGCreateNewfile", e1.toString());
		}
        
   
        this.mediaRecorder = new MediaRecorder();
        this.mediaRecorder.setAudioChannels(1);
        this.mediaRecorder.setAudioSamplingRate(44100);
        this.mediaRecorder.setAudioEncodingBitRate(64000);
        this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        this.mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        Log.d("TAGCreateNewfile", this.files.getAbsolutePath());
        this.mediaRecorder.setOutputFile(this.files.getAbsolutePath());//CJA changed from this.file.getAbsolutePath()
        this.mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
       
        try {
            this.mediaRecorder.prepare();
            this.mediaRecorder.start();

            // update the buttons
            this.setButtonsEnabled(true, false, false);
            fileNametextView.setText(OUT_FILE_NAME);
            
            //Pushing recorded file name to pebble
			UUID AppId = UUID.fromString("8bb49bab-77fe-4028-bd5e-4fbf35e134e1");
			PebbleKit.startAppOnPebble(getApplicationContext(), AppId);
			 
		   
			//CJA - Exception handling if pebble is not connected to the phone.
			//https://developer.getpebble.com/2/mobile-app-guide/android-guide.html		
			        Log.d("TAGCJAPebblePush", "before pusing");
					PebbleDictionary data = new PebbleDictionary();
					data.addString(123,OUT_FILE_NAME);
					data.addString(456, "Stop");
					data.addString(789, " ");
					Log.d("TAGCJAPebblePush", "On record just before pushing");
					PebbleKit.sendDataToPebble(MainActivity.this, AppId, data);
					Log.d("TAGCJAPebblePush", "On Record After pusing to pebble");


        } catch (IOException e) {
            Log.e(TAG, "Failed to record()", e);
        }catch (IllegalStateException e){
        	Log.e(TAG, "illegal - Failed to record()", e);
        	timechronometer.stop();
        	recordButton.setImageDrawable(this.getResources().getDrawable(R.drawable.mic_red));
        	this.mediaRecorder = null;
        	Toast.makeText(this, "Recoding errored, Ensure that the Mic is not used by another App.If this problem continues Close and Open the app", Toast.LENGTH_LONG).show();
        	
        }
    	}
    }

    public void play(View v) {
    	if (this.mediaPlayer != null){
    		stop(null);
    	}
    	else{
        Log.d(TAG, "play()");
        File filepaly = new File(extStorageDirectory,fileNametextView.getText().toString());
        if (filepaly.exists()) {
        	recordButton.setImageDrawable(this.getResources().getDrawable(R.drawable.mic_green));
        	 //stopButton.setImageDrawable(this.getResources().getDrawable(R.drawable.pause_red));
        	 playButton.setImageDrawable(this.getResources().getDrawable(R.drawable.pause_red));
        	timechronometer.setBase(SystemClock.elapsedRealtime());
        	timechronometer.start();
            this.mediaPlayer = new MediaPlayer();
            try {
            	Log.d(TAG,new File(extStorageDirectory,fileNametextView.getText().toString()).getAbsolutePath().toString());
            	Log.d(TAG,this.files.getAbsolutePath().toString());
                this.mediaPlayer.setDataSource(new File(extStorageDirectory,fileNametextView.getText().toString()).getAbsolutePath());//(this.files.getAbsolutePath());
                this.mediaPlayer.prepare();
                this.mediaPlayer.setOnCompletionListener(this);
                this.mediaPlayer.start();
        
                // update the buttons
                this.setButtonsEnabled(false, true, false);
            } catch (IOException e) {
                Log.e(TAG, "Failed to play()", e);
            }
        } else {
            this.playButton.setEnabled(false);
        }
    	}
    }

    public void stop(View v) {
        Log.d(TAG, "stop()");
        if (this.mediaPlayer != null) {
        	checkRecorderState="N";
        	timechronometer.stop();
        	recordButton.setImageDrawable(this.getResources().getDrawable(R.drawable.mic_red));
        	playButton.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_media_play_greenpng));
        	// stop/release the media player
            this.mediaPlayer.stop();
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        } else if (this.mediaRecorder != null) {
        	checkRecorderState="N";
        	//Stopping the timer
            timechronometer.stop();
            //setting image color
            recordButton.setImageDrawable(this.getResources().getDrawable(R.drawable.mic_red));
            playButton.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_media_play_greenpng));
            // stop/release the media recorder check state model
            this.mediaRecorder.stop();
            this.mediaRecorder.release();
            this.mediaRecorder = null;
			 //CJATODO renaming the file to what the user wants
		       AlertDialog.Builder alert = new AlertDialog.Builder(this);
		       alert.setTitle("Rename File");
		       alert.setMessage("Rename recorded file to");
            
         // Set an EditText view to get user input
		      // string = string.substring(0, string.length()-1);
		       OUT_FILE_NAME = OUT_FILE_NAME.substring(0, OUT_FILE_NAME.length()-4);
		       final EditText input = new EditText(this);
		       input.setText(OUT_FILE_NAME);
		       alert.setView(input);
		       alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		       public void onClick(DialogInterface dialog, int whichButton) {
		        if(OUT_FILE_NAME == input.getText().toString()){
		        	//OUT_FILE_NAME =OUT_FILE_NAME+".mp3";
		        	Log.d("TAGCreateNewfile", "1"+OUT_FILE_NAME);
		        }
		        else{  
		    	   if (input.getText().toString().isEmpty()){
		    		   Toast.makeText(getApplicationContext(), "Re-named file name can not be blank.File name set as: "+ OUT_FILE_NAME, Toast.LENGTH_LONG).show();
		    		   input.setText(OUT_FILE_NAME);
		    	   }
		    	   Log.d("TAGCreateNewfile", "0"+input.getText().toString());
		    	   fileNametextView.setText(input.getText().toString()+".mp3");
		           File directory = new File(extStorageDirectory+"/");
		           File from      = new File(directory, OUT_FILE_NAME+".mp3");
		           Log.d("TAGCreateNewfile", "2"+from.toString());
		           File to        = new File(directory,input.getText().toString()+".mp3");
		           Log.d("TAGCreateNewfile", "3"+to.toString());
		           try { 
			        	   if(from.renameTo(to)){ //returns true if renaming of file is sucessfull
			        		   Log.d("TAGCreateNewfile", "true"); 
			        	   }else
			        	   {
			        		   Log.d("TAGCreateNewfile", "failed to rename file"); 
			        		   Toast.makeText(getApplicationContext(), "Failed to re-name recorded file, please send the developer an email", Toast.LENGTH_LONG).show();
			        	   }
			               } 
			           catch (Exception e) 
			           { 
			        	   Toast.makeText(getApplicationContext(), ""+e, Toast.LENGTH_LONG).show();
			           }
		           
		           OUT_FILE_NAME = fileNametextView.getText().toString();
		           Log.d("TAGCreateNewfile", "4"+OUT_FILE_NAME.toString());
		        }
			       
		    	   sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+extStorageDirectory+"/"+OUT_FILE_NAME)));
		           Log.d("TAGCreateNewfile", "After OK sendBroadcast ");
		         }
		       });

		       alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		         public void onClick(DialogInterface dialog, int whichButton) {
		        	 sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+extStorageDirectory+"/"+OUT_FILE_NAME)));
		        	  Log.d("TAGCreateNewfile", "After Cancel sendBroadcast ");
		         }
		       });

		       alert.show();
        }
        // update the buttons
        this.setButtonsEnabled(true, true, new File(extStorageDirectory,fileNametextView.getText().toString()).exists());
      //Pushing recorded file name to pebble
		UUID AppId = UUID.fromString("8bb49bab-77fe-4028-bd5e-4fbf35e134e1");
		PebbleKit.startAppOnPebble(getApplicationContext(), AppId);
		
		//CJA - Exception handling if pebble is not connected to the phone.
		//https://developer.getpebble.com/2/mobile-app-guide/android-guide.html		
		        Log.d("TAGCJAPebblePush", "before pusing");
				PebbleDictionary data = new PebbleDictionary();
				data.addString(123, " ");
				data.addString(456, "Stopped Rec");
				data.addString(789, "Start");
				Log.d("TAGCJAPebblePush", "On record just before pushing");
				PebbleKit.sendDataToPebble(MainActivity.this, AppId, data);
				Log.d("TAGCJAPebblePush", "On Record After pusing to pebble");
				
    }

    public void delete(View v) {
        Log.d("TAGCJAPebble", "delete() file");
        //reseting the timer and the name of the file
        timechronometer.setBase(SystemClock.elapsedRealtime()); 
       playButton.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_media_play));
       // update the buttons
       
      //reminder alert for the user for deleting file
	       AlertDialog.Builder alert = new AlertDialog.Builder(this);
	       alert.setTitle("Deleteing recorded file");
	       alert.setMessage("Are you sure you want to delete the file");
  
	       alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog, int whichButton) {
			new File(extStorageDirectory,fileNametextView.getText().toString()).delete();
			 OUT_FILE_NAME = fileNametextView.getText().toString();
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+extStorageDirectory)));
			fileNametextView.setText("Recorded file deleted");
			setButtonsEnabled(true, false, false);
	         }
	       });

	       alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
	         public void onClick(DialogInterface dialog, int whichButton) {
	          setButtonsEnabled(true, true, true); 
	         }
	       });

	       alert.show();

	       Log.d("TAGCreateNewfile", this.files.toString());
         //this.files.delete();

      
      
       
    }

    @Override
    public void onPause() {
    	mAdView.pause();
        super.onPause();
        //this.stop(null); //CJATODO need to add in notification tray
        WriteModeOff(); //NFC function
        if(mController != null) {
        	mController.onPause(); }
    }

	@Override
	public void onResume(){
		super.onResume();
		mAdView.resume();
		 PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		 adapter.enableForegroundDispatch(this, pendingIntent, null, null);

		WriteModeOn();//NFC function
		if(mController != null) {
			mController.onResume(); }
	}
    // called when the playback is done
    public void onCompletion(MediaPlayer mp) {
        this.stop(null);
        //resetting play timer
        timechronometer.stop();
        //stopButton.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_media_stop));
        
    }
    
    @Override
    protected void onDestroy() {//CJATODO nullify all the variable
    	this.stop(null); //CJATODO stop recording if running which will in turn update media store
    	if(mController != null) {
    mController.exit(); }
    	mAdView.destroy();
    	
            super.onDestroy();
    }
//to install the watch app pbw called from the Action bar    
    private void installWatchApp() {

		Log.d("TAGCJAPebbleInstall", "copy_watchapp_from_assets()");
	    InputStream input = null;
	    OutputStream output = null;
	    try {
	        input = getApplicationContext().getAssets().open("pebbleRecorder.pbw");
	        File dest = new File(getApplicationContext().getExternalCacheDir().getAbsolutePath() + "/" + "pebbleRecorder.pbw");
	        // delete existing file
	        dest.delete();
	        output = new FileOutputStream(dest);
	        Log.d("TAGCJAPebbleInstall", "storing pbw: " + dest);
	        
	        // copy asset to file
	        byte[] buffer = new byte[2056];
	        int length;
	        while ((length = input.read(buffer))>0){
	            output.write(buffer, 0, length);
	        }
	        output.flush();
	        
	        // launch pebble update activity
	        Intent intent = new Intent(Intent.ACTION_VIEW);
	        intent.setData(Uri.fromFile(dest));
	        intent.setClassName("com.getpebble.android", "com.getpebble.android.ui.UpdateActivity");
	        Log.d("TAGCJAPebbleInstall", "launch pebble to load pbw");
	        try {
	            startActivity(intent);
	        } catch (ActivityNotFoundException e) {
	        	Log.e("TAGCJAPebbleInstall", "Pebble app not installed");
	        	toast = Toast.makeText(getApplicationContext(), "Pebble App is not installed, get the app from the Google Play Store", Toast.LENGTH_LONG);
	        	//toast.setGravity(Gravity.TOP, 0, 0);
	        	toast.show();
	        }
	    } catch (IOException e) {
	    	Log.e("TAGCJAPebbleInstall", e.getLocalizedMessage(), e);
	    } finally {
	        if (input != null) {
	            try {
	                input.close();
	            } catch (IOException e) {
	            	Log.e("TAGCJAPebbleInstall", e.getLocalizedMessage(), e);
	            }
	        }
	        if (output != null) {
	            try {
	                output.close();
	            } catch (IOException e) {
	            	Log.e("TAGCJAPebbleInstall", e.getLocalizedMessage(), e);
	            }
	        }
	    }
    }

    private void write(String text, Tag tag) throws IOException, FormatException {

		NdefRecord[] records = { createRecord(text) };
		NdefMessage  message = new NdefMessage(records);
		// Get an instance of Ndef for the tag.
		Ndef ndef = Ndef.get(tag);
		// Enable I/O
		ndef.connect();
		// Write the message
		ndef.writeNdefMessage(message);
		// Close the connection
		ndef.close();
	}



	private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
		String lang       = "en";
		byte[] textBytes  = text.getBytes();
		byte[] langBytes  = lang.getBytes("US-ASCII");
		int    langLength = langBytes.length;
		int    textLength = textBytes.length;
		byte[] payload    = new byte[1 + langLength + textLength];

		// set status byte (see NDEF spec for actual bits)
		payload[0] = (byte) langLength;

		// copy langbytes and textbytes into payload
		System.arraycopy(langBytes, 0, payload, 1,              langLength);
		System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

		NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);

		return recordNFC;
	}


	@Override
	protected void onNewIntent(Intent intent){
		if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
			mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);   
			Log.d("TAGCJANFCTag","0"+mytag.toString());
			//Toast.makeText(this, this.getString(R.string.oktodetection) + mytag.toString(), Toast.LENGTH_LONG ).show();
		//For read
			//protected void onNewIntent(Intent intent) {
				 // String action = intent.getAction();
				 // if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)){
				    Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
				    NdefMessage[] messages;
				    if (rawMsgs != null) {
				      messages = new NdefMessage[rawMsgs.length];
				      for (int i = 0; i < rawMsgs.length; i++) {
				        messages[i] = (NdefMessage) rawMsgs[i];     
				        // To get a NdefRecord and its different properties from a NdefMesssage   
				     NdefRecord record = messages[i].getRecords()[i];
				     byte[] id = record.getId();
				     short tnf = record.getTnf();
				     byte[] type = record.getType();
				     String message = getTextData(record.getPayload());
				     if (message.contentEquals("WRecord")){ 
				    	 if(checkRecorderState.contentEquals("N")){
				    		 record(null); 
				    	 }
				    	  else{
				    		 stop(null);
				    	 }
				    	 	 
				     }else{ 
				    	 Log.d("TAGCJANFCTag","NFCtagfirst"+message);
				    	 Toast.makeText(this, "Write  the NFC tag first.To prepare the tag for recording Write the Tag from the Options Menu", Toast.LENGTH_LONG).show();          		
				    	 Toast.makeText(this, "Data on NFC tag::"+message, Toast.LENGTH_LONG).show();
				          }
				      }
				    }
				    
				 // }
			//	}
		}
	}
	// Going through the payload containing text
	private String getTextData(byte[] payload) {
	  if(payload == null) 
	    return null;
	  try {
	    String encoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
	    int langageCodeLength = payload[0] & 0077;
	    return new String(payload, langageCodeLength + 1, payload.length - langageCodeLength - 1, encoding);     
	  } catch(Exception e) {
	    e.printStackTrace();
	    Log.d("TAGCJANFCtag","2"+"Exception to reading through NFC message");
	  }
	  return null;
	}
	//For NFC
	private void WriteModeOn(){
		writeMode = true;
		adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
	}
 //for NFC
	private void WriteModeOff(){
		writeMode = false;
		adapter.disableForegroundDispatch(this);
	}
	
	//For MOGA
	@Override
	public void onMotionEvent(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
   //For Moga	
public void onStateEvent(StateEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	//For Moga
	@Override
	public void onKeyEvent(KeyEvent event) { //A - Record X- Stop Y- Play 
		// TODO Auto-generated method stub
		switch(event.getKeyCode())
		{
		case KeyEvent. KEYCODE_BUTTON_A:  
		if(event.getAction() == KeyEvent.ACTION_DOWN) {
		// button A has been pressed
			Log.d("CJAMOGA","a button A is pressed");
			record(null); 
			}
		else
		{
		// button A has been released
			Log.d("CJAMOGA","a button A is Released");
		} break;
		case KeyEvent. KEYCODE_BUTTON_X:
		if(event.getAction() == KeyEvent.ACTION_DOWN) {
		// button A has been pressed
			Log.d("CJAMOGA","a button X is pressed");
			stop(null);
			}
		else
		{
		// button A has been released
			Log.d("CJAMOGA","a button X is Released");
		} break;
		case KeyEvent. KEYCODE_BUTTON_Y:
		if(event.getAction() == KeyEvent.ACTION_DOWN) {
		// button A has been pressed
			Log.d("CJAMOGA","a button Y is pressed");
			play(null);
			}
		else
		{
		// button A has been released
			Log.d("CJAMOGA","a button Y is Released");
		} break;
		case KeyEvent. KEYCODE_BUTTON_B:
			if(event.getAction() == KeyEvent.ACTION_DOWN) {
			// button A has been pressed
				Log.d("CJAMOGA","a button B is pressed");
				delete(null);
				}
			else
			{
			// button A has been released
				Log.d("CJAMOGA","a button B is Released");
			} break;
		}
	}

    
	
}


