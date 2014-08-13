package com.example.assigment1_dropbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.ListView;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.dropbox.client2.DropboxAPI;

import com.dropbox.client2.DropboxAPI.Entry;

import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

public class MainActivity extends Activity {

	final static private String APP_KEY = "3zhyr9wta6jqypn";
	final static private String APP_SECRET = "1gw6268utivaock";
	final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;
	public static DropboxAPI<AndroidAuthSession> mDBApi;
	public static String uploadVar;
	public static String uploadName;
	static final int FILE_SELECT_CODE = 0;
	static ProgressDialog progressBar;

	private final static String TAG = "Assignment1";
	static final int REQUEST_LINK_TO_DBX = 0;
	public static Drawable pd_retrieved;
	public static ListView lv;
	public static GridView gv;
	public static List<Entry> files;
	public static ArrayList<String> pathHistory = new ArrayList<String>();
	public static String test;
	static ProgressBar circle;
	public static boolean showListView = true;

	public static ArrayList<String> arrayFolder = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// And later in some initialization function:
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session = new AndroidAuthSession(appKeys,
				ACCESS_TYPE);
		mDBApi = new DropboxAPI<AndroidAuthSession>(session);
		// MyActivity below should be your activity class name
		mDBApi.getSession().startAuthentication(MainActivity.this);
		if (showListView == true) {
			setContentView(R.layout.activity_main);
		} else if (showListView == false) {
			setContentView(R.layout.activity_grid_view);
		}

		Log.i(TAG, "on Create....................");
		pathHistory.add("/");
		Log.i("History", "Array size: " + pathHistory.size() + " Root Path: "
				+ pathHistory.get(pathHistory.size() - 1));
		circle = (ProgressBar) findViewById(R.id.circle);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		Log.i(TAG, "on Create Option....................");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		// Intent menuIntent;
		switch (item.getItemId()) {
		case R.id.ViewDropBox:
			Log.i("Menu", "View");
			if(showListView == true){
			showListView = false;
			}else{
			showListView = true;
			}
			
			if (showListView == true) {
				setContentView(R.layout.activity_main);
			} else if (showListView == false) {
				setContentView(R.layout.activity_grid_view);
			}
			 new ListFiles().execute();

			return true;
		case R.id.Upload:
			Log.i("Menu", "Upload");

			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("*/*");
			intent.addCategory(Intent.CATEGORY_OPENABLE);

			startActivityForResult(
					Intent.createChooser(intent, "Select a File to Upload"),
					FILE_SELECT_CODE);
			Log.i("Upload", "file select code " + FILE_SELECT_CODE);

			return true;
		case R.id.Undo:
			Log.i("Menu", "Undo");
			return true;
		case R.id.AddNew:
			Log.i("Menu", "AddNew");
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@SuppressLint("NewApi")
	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "on Resume....................");
		if (mDBApi.getSession().authenticationSuccessful()) {
			try {
				// Required to complete auth, sets the access token on the
				// session
				mDBApi.getSession().finishAuthentication();
				Log.i(TAG,
						"on Resume- Authentication  Successful....................");
				AccessTokenPair tokens = mDBApi.getSession()
						.getAccessTokenPair();

				new ListFiles().execute();

			} catch (IllegalStateException e) {
				Log.i(TAG,
						"on Resume- Autgentication  Fail....................");
			}
		}
	}

	public void ListFileMain(ArrayList<String> pathHistory) {
		try {

			Entry myEntry = mDBApi.metadata(
					pathHistory.get(pathHistory.size() - 1), -1, null, true,
					null);
			files = myEntry.contents;
			arrayFolder.clear();
			for (Entry entry : files) {
				Log.i(TAG, "file = " + entry.fileName());
				arrayFolder.add(entry.fileName());
				String string = entry.fileName();
			}

		} catch (DropboxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {
		if (pathHistory.get(pathHistory.size() - 1).equalsIgnoreCase("/")) {
			AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
			myAlertDialog.setTitle("--- Warning ---");

			myAlertDialog.setMessage("Do you want to exit?");
			myAlertDialog.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface arg0, int arg1) {
							// do something when
							// the OK button is
							// clicked
							finish();
						}
					});
			myAlertDialog.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface arg0, int arg1) {
							// do something when
							// the Cancel button
							// is clicked
						}
					});
			myAlertDialog.show();
		} else {
			pathHistory.remove(pathHistory.size() - 1);
			new ListFiles().execute();
			Log.i("Back", "Back ne");
		}

	}

	public void showFile() {
		Log.i(TAG, "Array size: " + arrayFolder.size());
		Model.LoadModel(arrayFolder);

		lv = (ListView) findViewById(R.id.listView1);
		gv = (GridView) findViewById(R.id.gridView1);

		String[] ids = new String[Model.Items.size()];

		for (int i = 0; i < ids.length; i++) {

			ids[i] = Integer.toString(i + 1);
			Log.i("Test", "ids: " + ids[i] + " File name: "
					+ files.get(i).fileName());
		}
		ItemAdapter adapter = null;
		if (showListView == true) {
			adapter = new ItemAdapter(this, R.layout.row, ids);
			lv.setAdapter(adapter);
		} else if (showListView == false) {
			adapter = new ItemAdapter(this, R.layout.gridview_row, ids);
			gv.setAdapter(adapter);
		}

		adapter.notifyDataSetChanged();

	}

	public class ListFiles extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... urls) {
			ListFileMain(pathHistory);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			showFile();
		}
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Audio.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		String path = getPath(data.getData());
		uploadVar = path;
		File objFile = new File(path);
		uploadName = objFile.getName();

		new UploadFiles().execute();

		super.onActivityResult(requestCode, resultCode, data);
	}

	public class UploadFiles extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			File file = new File(uploadVar);
			FileInputStream inputStream = null;
			try {
				inputStream = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				Entry response = mDBApi.putFile(
						pathHistory.get(pathHistory.size() - 1) + "/"
								+ uploadName, inputStream, file.length(), null,
						new ProgressListener() {

							public void onProgress(long bytes, long total) {
								// TODO Auto-generated method stub
								progressBar.setMax((int) total);
								progressBar.setProgress((int) bytes);
							}
						});
			} catch (DropboxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			progressBar = new ProgressDialog(MainActivity.this);
			progressBar.setCancelable(true);
			progressBar.setMessage("File uploading ...");
			progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressBar.setProgress(0);
			progressBar.setMax(100);
			progressBar.show();
		}

		@Override
		protected void onPostExecute(Void result) {
			progressBar.dismiss();
			new ListFiles().execute();
		}

	}

	
	

	
	
}
