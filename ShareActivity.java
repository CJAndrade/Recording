package com.cja.wearablerecorder;

import java.io.File;
import java.util.ArrayList;


import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ShareActivity extends Activity {
	private int currentID = 0;
	private ArrayList list;
	private ListView myListView;
	private String Recording_Path = "wearableRecord";
	private SelectedAdapter selectedAdapter; 
	private File file;
	private String messageRecv;
	//private String Recording_Path= "MyMusic";
    private String SDCardfolder = Environment.getExternalStorageDirectory()
 		.getAbsolutePath() + File.separator +Recording_Path+File.separator;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);
		myListView = (ListView) findViewById(R.id.listViewSongs);
		
		//getting data bundle from PlayActivity
		Bundle bundle = getIntent().getExtras();
		messageRecv =  bundle.getString("key_action");
		Log.v("CJAshare", "messageRecv:"+messageRecv);

		
        ActionBar actionBar = getActionBar();
        //actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setIcon(R.drawable.ic_media_play);
       actionBar.setDisplayShowHomeEnabled(true);
		if(messageRecv.contentEquals("Share")){
	      actionBar.setTitle("Share");	
	      
		}
		else
		{
			actionBar.setTitle("Edit Recordings");
		}
       actionBar.setDisplayHomeAsUpEnabled(true);
       
 // Creating a  simple a list
       list = new ArrayList();
      file = new File(SDCardfolder);
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

       
       myListView.setAdapter(selectedAdapter);
       myListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView arg0, View view,
                                          int position, long id) {
				// user clicked a list item, make it "selected"
				selectedAdapter.setSelectedPosition(position);
				  Log.v("CJAShare","PlayActivity onItemClick: position" + position+ "id: " + id);
		           currentID = position;    		                
			}
       });

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_share_actions, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		if(messageRecv.contentEquals("Share")){
		MenuItem itemDel  = menu.findItem(R.id.action_delete_player);
		itemDel.setVisible(false);
		MenuItem itemRename  = menu.findItem(R.id.action_rename_player);
		itemRename.setVisible(false);
        MenuItem itemShare  = menu.findItem(R.id.action_share_player);
        itemShare.setVisible(true);
		}else{
			MenuItem itemDel  = menu.findItem(R.id.action_delete_player);
			itemDel.setVisible(true);
			MenuItem itemRename  = menu.findItem(R.id.action_rename_player);
			itemRename.setVisible(true);
	        MenuItem itemShare  = menu.findItem(R.id.action_share_player);
	        itemShare.setVisible(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
        case R.id.action_delete_player:
        
        	Log.v("CJAshare", "action_delete_player:");
        	 AlertDialog.Builder alert = new AlertDialog.Builder(this);
           	       alert.setTitle("Delete Recording");
           	       alert.setMessage("Are you sure you want to delete the Recording :"+ myListView.getItemAtPosition(currentID).toString());
          	       alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
          	       public void onClick(DialogInterface dialog, int whichButton) {
          	    	 //Log.v("CJAShare", "inside yes delete:");
          		     new File(SDCardfolder+myListView.getItemAtPosition(currentID).toString()).delete();
          			//sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+extStorageDirectory)));//CJAKitkat gives an security exception
          			//got to Player
          		   startActivity(new Intent(ShareActivity.this, PlayActivity.class));
          	         }
          	       });
          	       alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
          	         public void onClick(DialogInterface dialog, int whichButton) {
          	         }
          	       });
          	     alert.show();
            return true;
        case R.id.action_rename_player:
        	AlertDialog.Builder alertRename = new AlertDialog.Builder(this);
        	alertRename.setTitle("Rename File");
        	alertRename.setMessage("Rename recorded file to");
        	final EditText input = new EditText(this);
		       int maxLength = 24;    
		       input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
		       input.setText(myListView.getItemAtPosition(currentID).toString());
		       alertRename.setView(input);
		       alertRename.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		       public void onClick(DialogInterface dialog, int whichButton) {
		        if(myListView.getItemAtPosition(currentID).toString() == input.getText().toString()){
		        	//OUT_FILE_NAME =OUT_FILE_NAME+".mp3";
		        	Log.d("TAGCreateNewfile", "1"+myListView.getItemAtPosition(currentID).toString());
		        }
		        else{  
		    	   if (input.getText().toString().isEmpty()){
		    		   Toast.makeText(getApplicationContext(), "Re-named file name can not be blank.File name set as: "+ myListView.getItemAtPosition(currentID).toString(), Toast.LENGTH_LONG).show();
		    		   input.setText(myListView.getItemAtPosition(currentID).toString());
		    	   }
		    	   Log.d("TAGCreateNewfile", "0"+input.getText().toString());
		           File directory = new File(SDCardfolder);
		           File from      = new File(directory, myListView.getItemAtPosition(currentID).toString());//+".mp3"
		           Log.d("TAGCreateNewfile", "2"+from.toString());
		           File to        = new File(directory,input.getText().toString());//+".mp3"
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

		        }
			       
		    	   sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(SDCardfolder+myListView.getItemAtPosition(currentID).toString())));
		           Log.d("TAGCreateNewfile", "After OK sendBroadcast ");
		           startActivity(new Intent(ShareActivity.this, PlayActivity.class));
		         }
		       });

		       alertRename.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		         public void onClick(DialogInterface dialog, int whichButton) {
		        	 sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(SDCardfolder+myListView.getItemAtPosition(currentID).toString())));
		        	  Log.d("TAGCreateNewfile", "After Cancel sendBroadcast ");
		         }
		       });

		       alertRename.show();
        	
        	Log.v("CJAshare", "action_rename_player:");
            return true;
          case R.id.action_share_player:
          	  //sharing recording
     Log.v("CJAShare", "action_share_player"+ "file://"+SDCardfolder+myListView.getItemAtPosition(currentID).toString());
            	Intent  shareIntent = new Intent() ;
            	shareIntent.setAction(Intent.ACTION_SEND);
            	shareIntent.setType("audio/*");
            	shareIntent.putExtra(Intent.EXTRA_STREAM,Uri.parse("file://"+SDCardfolder+myListView.getItemAtPosition(currentID).toString()));
            	startActivity(Intent.createChooser(shareIntent, "Share: "+myListView.getItemAtPosition(currentID).toString()));
          	return true;
          default:
              return super.onOptionsItemSelected(item);
        }
	}
	
	
}
