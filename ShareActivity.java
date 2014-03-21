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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
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
			actionBar.setTitle("Delete Recordings");
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
        MenuItem itemShare  = menu.findItem(R.id.action_share_player);
        itemShare.setVisible(true);
		}else{
			MenuItem itemDel  = menu.findItem(R.id.action_delete_player);
			itemDel.setVisible(true);
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
