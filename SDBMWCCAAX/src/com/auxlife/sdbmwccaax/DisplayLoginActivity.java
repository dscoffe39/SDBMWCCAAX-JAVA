package com.auxlife.sdbmwccaax;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Activity which displays a login screen to the user
 * and creates a json call to verify the credentials.
 */
@SuppressLint("DefaultLocale")
public class DisplayLoginActivity extends Activity {
	private final static String URL_LOGIN = "http://sdbmwcca.com/ANDroid.endpOInt/login.actIOn";
	private ProgressDialog pDialog;
	private ArrayList<String> user;

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mUn;
	private String mPassword;

	// UI references.
	private EditText mUnView;
	private EditText mPasswordView;
	private Button mSignInButton;
	private View mLoginFormView;
	private View mLoginStatusView;


	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Locale.setDefault(new Locale("US"));
		setContentView(R.layout.activity_display_login);
		
		// Set up the login form.
		//user.set(0,getIntent().getStringExtra("com.auxlife.sdbmwccaax.UID"));
		//user.set(0, getIntent().getExtras().getString("UID"));
		user = getIntent().getExtras().getStringArrayList("user");
		Log.d("Received Data:", "> " + user.toString());
		
		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mSignInButton = (Button) findViewById(R.id.sign_in_button);
		mUnView = (EditText) findViewById(R.id.uname);
		mPasswordView = (EditText) findViewById(R.id.password);
		mSignInButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.display_login, menu);
		return true;
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
				DisplayLoginActivity.this.moveTaskToBack(true);
				//android.os.Process.killProcess(android.os.Process.myPid());
			}
		});

		alert.show();
		if(ad.isShowing())
			ad.dismiss();
	}
	
	/** sends a post call to the php page to login user and matches it to the device UID */
	private class UserLoginTask extends AsyncTask<Void, Void, Void> {
		private int success = 10;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			pDialog = new ProgressDialog(DisplayLoginActivity.this);
			pDialog.setMessage("Logging you in...");
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			JsonHandler sh = new JsonHandler();
		
			List<NameValuePair> param = new ArrayList<NameValuePair>();
			param.add(new BasicNameValuePair("login","login"));
			param.add(new BasicNameValuePair("usrID", user.get(0)));
			param.add(new BasicNameValuePair("myusername", mUn));
			param.add(new BasicNameValuePair("mypassword", mPassword));
			Log.d("Post Data: (login)", "> " + param.toString());
			String jsonStr = sh.makeJsonCall(URL_LOGIN, JsonHandler.POST, param);
			Log.d("Response: ", "> " + jsonStr);
			if(jsonStr.toLowerCase().indexOf("error") != -1)
				success = 99;
			else if (jsonStr != null) {
				try {
					JSONObject jsonObj = new JSONObject(jsonStr);
					success = jsonObj.getInt("ActiveUser");
					if(success == 1) {
						user.set(1, "active");
						user.set(2, jsonObj.getString("Name"));
						user.set(3, jsonObj.getString("Position"));
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
			mAuthTask = null;
			
			//showProgress(false);
			switch(success) {
			case 1:
				Intent resultIntent = new Intent();
				resultIntent.putStringArrayListExtra("user", user);
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
				break;
			case 10:
				ShowFatalAlert(DisplayLoginActivity.this,"Internet Connection","The server is not reachable!");
				break;
			case 0:
				mPasswordView.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
				break;
			case 2:
				mUnView.setError(getString(R.string.error_incorrect_un));
				mUnView.requestFocus();
				break;
			default:
				ShowFatalAlert(DisplayLoginActivity.this,"Server Error","Something went wrong please contact the administrator!");
				break;
			}
		}
		
	}
	
	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mUnView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mUn = mUnView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		String Temp;

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 2) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid username.
		if (TextUtils.isEmpty(mUn)) {
			mUnView.setError(getString(R.string.error_field_required));
			focusView = mUnView;
			cancel = true;
		} else if (mUn.length() < 4) {
			mUnView.setError(getString(R.string.error_invalid_un));
			focusView = mUnView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			//mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			Temp = Hashing.sha1().hashString(mUn+">B;#7yD9", Charsets.UTF_8).toString();
			mUn = Hashing.md5().hashString(Temp+mUn, Charsets.UTF_8).toString();
			Temp = Hashing.sha1().hashString(mPassword+"-(j8@gAS", Charsets.UTF_8).toString();
			mPassword = Hashing.md5().hashString(Temp+mPassword, Charsets.UTF_8).toString();
			
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
}
