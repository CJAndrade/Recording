package com.cja.wearablerecorder;

import com.cja.wearablerecorder.R.drawable;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.view.Menu;

public class Using_Moga extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_using__moga);
        ActionBar actionBar = getActionBar();
        //actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setIcon(R.drawable.ic_action_mic);
       actionBar.setDisplayShowHomeEnabled(true);
       actionBar.setTitle("How to use Moga");
       actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.using__moga, menu);
		return true;
	}

}
