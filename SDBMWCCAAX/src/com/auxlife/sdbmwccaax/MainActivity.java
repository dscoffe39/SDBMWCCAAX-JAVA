package com.auxlife.sdbmwccaax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	private final static String URL_LOGIN = "http://sdbmwcca.com/ANDroid.endpOInt/login.actIOn";
	private final static String URL_LOGOUT = "http://sdbmwcca.com/ANDroid.endpOInt/logout.actIOn";
	//private final static String URL_GET_MEMB = "http://sdbmwcca.com/ANDroid.endpOInt/login.actIOn";
	//private final static String URL_PUT_MEMB = "http://sdbmwcca.com/ANDroid.endpOInt/login.actIOn";
	private View MainView;
	private TextView title;
	private TextView logoff;
	private TextView login;
	private Button newmember;
	private Button viewstaff;
	private Button viewinst;
	private ProgressDialog pDialog;
	private ArrayList<String> user = new ArrayList<String>(Arrays.asList(getUID(), "idle", "Name", "Position"));
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Locale.setDefault(new Locale("US"));
		setContentView(R.layout.activity_main);
		MainView = (View) findViewById(R.id.RelativeLayout);
		title = (TextView) findViewById(R.id.TVtitle);
		logoff = (TextView) findViewById(R.id.TVlogoff);
		login = (TextView) findViewById(R.id.TVlogin);
		newmember = (Button) findViewById(R.id.Bmember);
		viewstaff = (Button) findViewById(R.id.Bviewstaff);
		viewinst = (Button) findViewById(R.id.Bviewinst);

		MainView.setVisibility(View.INVISIBLE);
		CheckNetworkState(this);
	}
	
	/** Called when the user clicks the logoff text */
	public void Click_logoff(View view) {
	    new Logout().execute();
	}
	
	/** Called when the user clicks the login text */
	public void Click_login(View view) {
		Intent login = new Intent(this, DisplayLoginActivity.class);
		login.putStringArrayListExtra("user", user);
		startActivityForResult(login,1);
	}
	
	/** Called when the login activity has finished */
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
	  super.onActivityResult(requestCode, resultCode, data); 
	  switch(requestCode) { 
	    case (1) : { 
	      if (resultCode == Activity.RESULT_OK) { 
		      user = data.getExtras().getStringArrayList("user");
		      Log.d("Received Data:", "> " + user.toString());
		      ToggleView();
	      } 
	      break; 
	    } 
	  }
	}
	
	/** must check if we have a valid network 
	 * connection, and if server is reachable
	 * before restarting activity
	 */
	@Override
	public void onRestart(){
		super.onRestart();
	    CheckNetworkState(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/** Switches between login/logout text/buttons */
	public void ToggleView()
	{
		MainView.setVisibility(View.VISIBLE);
	//	if(pDialog.isShowing())
		//	pDialog.dismiss();
		//Log.d("bl:",String.valueOf(user.get(1).toLowerCase(Locale.getDefault()).indexOf("active")));
		if(user.get(1).toLowerCase(Locale.getDefault()).indexOf("active") != -1){
		//if(user.get(1) == "active") {
			title.setText("Welcome back " + user.get(2) + "!");
			login.setVisibility(View.INVISIBLE);
			logoff.setVisibility(View.VISIBLE);
			newmember.setVisibility(View.VISIBLE);
			viewstaff.setVisibility(View.VISIBLE);
			viewinst.setVisibility(View.VISIBLE);
		}
		else {
			title.setText("This device is not logged in.");
			logoff.setVisibility(View.INVISIBLE);
			newmember.setVisibility(View.INVISIBLE);
			viewstaff.setVisibility(View.INVISIBLE);
			viewinst.setVisibility(View.INVISIBLE);
			login.setVisibility(View.VISIBLE);
		}
	}
	
	/** creates an alert and returns to 
	 * home once user acknowledges 
	 */
	private void ShowFatalAlert(Context context, String title, String msg){
		AlertDialog.Builder alert = new AlertDialog.Builder(context);
		final AlertDialog ad = alert.create();
		alert.setTitle(title);
		alert.setMessage(msg);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				MainActivity.this.moveTaskToBack(true);
				//android.os.Process.killProcess(android.os.Process.myPid());
			}
		});

		alert.show();
		if(ad.isShowing())
			ad.dismiss();
	}
	
	/** check for network connection before loading app */
	private void CheckNetworkState(Context context)
	{
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean error = true;
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		
		if(activeNetwork != null)
			if(activeNetwork.isConnected()&&activeNetwork.isAvailable())
				error = false;
		
		if(error)
			ShowFatalAlert(context,"Internet Connection","This application requires an active internet connection to work!");
		else
			if(user.get(1)== "idle")
				new CheckLogin().execute();
		ToggleView();
	}
	
	/** Return Pseudo Unique ID */
	public static String getUID()
	{
	    // IF all else fails, if the user does is lower than API 9(lower 
	    // than Gingerbread), has reset their phone or 'Secure.ANDROID_ID'
	    // returns 'null', then simply the ID returned will be soley based
	    // off their Android device information. This is where the collisions 
	    // can happen.
	    // Thanks http://www.pocketmagic.net/?p=1662!
	    // Try not to use DISPLAY, HOST or ID - these items could change
	    // If there are collisions, there will be overlapping data
	    String m_szDevIDShort = "35" + (Build.BOARD.length() % 10) + (Build.BRAND.length() % 10) + (Build.CPU_ABI.length() % 10) + (Build.DEVICE.length() % 10) + (Build.MANUFACTURER.length() % 10) + (Build.MODEL.length() % 10) + (Build.PRODUCT.length() % 10);
	
	    // Thanks to @Roman SL!
	    // http://stackoverflow.com/a/4789483/950427
	    // Only devices with API >= 9 have android.os.Build.SERIAL
	    // http://developer.android.com/reference/android/os/Build.html#SERIAL
	    // If a user upgrades software or roots their phone, there will be a duplicate entry
	    String serial = null; 
	    try 
	    {
	        serial = android.os.Build.class.getField("SERIAL").toString();
	
	        // go ahead and return the serial for api => 9
	        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
	    } 
	    catch (Exception e) 
	    { 
	        // String needs to be initialized
	        serial = "serial"; // some value
	    }
	
	    // Thanks @Joe!
	    // http://stackoverflow.com/a/2853253/950427
	    // Finally, combine the values we have found by using the UUID class to create a unique identifier
	    return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
	}
	
	/** sends a post call to the php page to check if this UID has an active login */
	private class CheckLogin extends AsyncTask<Void, Void, Void> {
		private int success = 10;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Checking your account...");
			pDialog.setCancelable(false);
			pDialog.show();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			JsonHandler sh = new JsonHandler();
		
			List<NameValuePair> param = new ArrayList<NameValuePair>();
			param.add(new BasicNameValuePair("usrID", user.get(0)));
			Log.d("Post Data: (login)", "> " + param.toString());
			String jsonStr = sh.makeJsonCall(URL_LOGIN, JsonHandler.POST, param);
			Log.d("Response: ", "> " + jsonStr);
			
			if (jsonStr != null) {
				try {
					JSONObject jsonObj = new JSONObject(jsonStr);
					success = jsonObj.getInt("ActiveUser");
					if(success == 1) {
						user.set(1, "active");
						user.set(2, jsonObj.getString("Name"));
						user.set(3, jsonObj.getString("Position"));
						Log.d("Saved Data:", "> " + user.toString());
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			return null;
		}
		@Override
		protected void onPostExecute(Void result){
			super.onPostExecute(result);
			if(pDialog.isShowing())
				pDialog.dismiss();
			if(success==10)
				ShowFatalAlert(MainActivity.this,"Internet Connection","The server is not reachable!");
			ToggleView();
		}
		
	}
	
	/** sends a post call to the php page to remove this UID from the active logins */
	private class Logout extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Logging you out...");
			pDialog.setCancelable(false);
			pDialog.show();
		}
		@Override
		protected Void doInBackground(Void... params) {
			JsonHandler sh = new JsonHandler();
			int success = 0;
			
			List<NameValuePair> param = new ArrayList<NameValuePair>();
			param.add(new BasicNameValuePair("usrID", user.get(0)));
			Log.d("Post Data (logout): ", "> " + param.toString());
			String jsonStr = sh.makeJsonCall(URL_LOGOUT, JsonHandler.POST, param);
			Log.d("Response: ", "> " + jsonStr);
			
			if (jsonStr != null) {
				try {
					JSONObject jsonObj = new JSONObject(jsonStr);
					success = jsonObj.getInt("ActiveUser");
					if(success == 0) {
						user.set(1, "idle");
						user.set(2, "Name");
						user.set(3, "Position");
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void result){
			super.onPostExecute(result);
			if(pDialog.isShowing())
				pDialog.dismiss();
			ToggleView();
		}
	}
}
