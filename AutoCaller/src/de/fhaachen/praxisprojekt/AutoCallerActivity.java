package de.fhaachen.praxisprojekt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class AutoCallerActivity extends Activity {
	/** Called when the activity is first created. */
	public static final String TAG = "AutoCaller";
	public static final int DIALOG_RESET = 0;
	private static boolean callstarted = false;
	private ArrayList<String> numbersList;
	private int progress = 0;
	private Button callbutton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		callbutton = (Button) findViewById(R.id.callbutton);
		callbutton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if (getNumbers() && (progress < numbersList.size())) {
					callbutton.setEnabled(true);
					call();
				} else {
					callbutton.setEnabled(false);
					showDialog(DIALOG_RESET);
				}
			}
		});
				
        EndCallListener callListener = new EndCallListener();
        TelephonyManager mTM = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        mTM.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);
       
        SharedPreferences settings = getPreferences(MODE_PRIVATE); 
        progress = settings.getInt("progress", 0);
	}
	
	private boolean getNumbers() {
		if ((numbersList == null) || (numbersList.size() == 0)) {  
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				File numbersFile = new File (path, "numbers.txt");
				if (numbersFile.exists()) {
					try {
						BufferedReader buf = new BufferedReader(
								new InputStreamReader(new FileInputStream(
										numbersFile)));
						String line;
						numbersList = new ArrayList<String>();
						while ((line = buf.readLine()) != null) {
							numbersList.add(line);
						}
					} catch (FileNotFoundException e) {
						Log.e(TAG, "Couldn't open numbers file. FNFE");
						e.printStackTrace();
						return false;
					} catch (IOException e) {
						Log.e(TAG, "Couldn't open numbers file. IOE");
						e.printStackTrace();
						return false;
					}
				} else {
					Log.e(TAG, "Couldn't open numbers file.");
					Log.d(TAG, "Searched for " + numbersFile.getAbsolutePath());
					return false;
				}
			} else {
				Log.e(TAG, "No external Storage");
				return false;
			}
		}
		return true;
	}
	
	private void call() {
		if (progress < numbersList.size()) {
			String number = numbersList.get(progress);
			progress++;
			Intent call = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+number));
			startActivity(call);
			finish();
		}
	}
	
	@Override
	public void finish() {
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("progress", progress);
		editor.commit();
		super.finish();
	}
	
	protected Dialog onCreateDialog(int id) {
		Dialog alert = null;
		
		if (id == DIALOG_RESET) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Finished all calls from the List")
			       .setCancelable(false)
			       .setPositiveButton("Start Over", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   AutoCallerActivity.this.progress = 0;
			        	   AutoCallerActivity.this.callbutton.setEnabled(true);
			           }
			       })
			       .setNegativeButton("End application", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   AutoCallerActivity.this.finish();
			           }
			       });
			alert = builder.create();
		}
		
		return alert;
	}

	private class EndCallListener extends PhoneStateListener {	
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
	        if(TelephonyManager.CALL_STATE_OFFHOOK == state) {
	        	callstarted = true;
	            Log.i(TAG, "OFFHOOK, state: " + callstarted);
	        }
	        if(TelephonyManager.CALL_STATE_IDLE == state) {
	        	Log.i(TAG, "IDLE, state " + callstarted);
	        	if (callstarted == true) {
	                Intent t = new Intent(AutoCallerActivity.this, AutoCallerActivity.class);
	                t.setAction(Intent.ACTION_MAIN);
	                Log.i(TAG, "Going Back, state: " + callstarted);
	                callstarted = false;
	                startActivity(t);
	                finish();
	        	}
	        }
		}
	}
}