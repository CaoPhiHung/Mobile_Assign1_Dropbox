package com.example.assigment1_dropbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.example.assigment1_dropbox.MainActivity.ListFiles;
import com.example.assigment1_dropbox.MainActivity.UploadFiles;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class GridView extends Activity {

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		Intent menuIntent;
		switch (item.getItemId()) {
		case R.id.ViewDropBox:
			Log.i("Menu", "View");
			
			setContentView(R.layout.gridview_row);
			new ListFiles().execute();
			return true;
		case R.id.Upload:
			Log.i("Menu", "Upload");

			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("*/*");
			intent.addCategory(Intent.CATEGORY_OPENABLE);

			startActivityForResult(
					Intent.createChooser(intent, "Select a File to Upload"),
					MainActivity.FILE_SELECT_CODE);
			Log.i("Upload", "file select code " + MainActivity.FILE_SELECT_CODE);

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
		if (MainActivity.mDBApi.getSession().authenticationSuccessful()) {
			try {
				// Required to complete auth, sets the access token on the
				// session
				MainActivity.mDBApi.getSession().finishAuthentication();
				AccessTokenPair tokens = MainActivity.mDBApi.getSession()
						.getAccessTokenPair();

				new ListFiles().execute();

			} catch (IllegalStateException e) {
			}
		}
	}

	public void ListFileMain(ArrayList<String> pathHistory) {
		try {

			Entry myEntry = MainActivity.mDBApi.metadata(
					pathHistory.get(pathHistory.size() - 1), -1, null, true,
					null);
			MainActivity.files = myEntry.contents;
			MainActivity.arrayFolder.clear();
			for (Entry entry : MainActivity.files) {
				MainActivity.arrayFolder.add(entry.fileName());
				String string = entry.fileName();
			}

		} catch (DropboxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {
		if (MainActivity.pathHistory.get(MainActivity.pathHistory.size() - 1).equalsIgnoreCase("/")) {
			AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
			myAlertDialog.setTitle("--- Warning ---");

			myAlertDialog.setMessage("Do you want to exit?");
			myAlertDialog.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface arg0, int arg1) {
							// do something when
							// the OK button is
							// clicked
							System.exit(1);
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
			MainActivity.pathHistory.remove(MainActivity.pathHistory.size() - 1);
			new ListFiles().execute();
			Log.i("Back", "Back ne");
		}

	}

	public void showFile() {
		Model.LoadModel(MainActivity.arrayFolder);

		MainActivity.lv = (ListView) findViewById(R.id.listView1);

		String[] ids = new String[Model.Items.size()];

		for (int i = 0; i < ids.length; i++) {

			ids[i] = Integer.toString(i + 1);
			Log.i("Test", "ids: " + ids[i] + " File name: "
					+ MainActivity.files.get(i).fileName());
		}

		ItemAdapter adapter = new ItemAdapter(this, R.layout.gridview_row, ids);
		MainActivity.lv.setAdapter(adapter);
		adapter.notifyDataSetChanged();

	}

	public class ListFiles extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... urls) {
			ListFileMain(MainActivity.pathHistory);
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
		MainActivity.uploadVar = path;
		File objFile = new File(path);
		MainActivity.uploadName = objFile.getName();

		new UploadFiles().execute();

		super.onActivityResult(requestCode, resultCode, data);
	}

	public class UploadFiles extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			File file = new File(MainActivity.uploadVar);
			FileInputStream inputStream = null;
			try {
			inputStream = new FileInputStream(file);
			} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
			try {
			Entry response = MainActivity.mDBApi.putFile(MainActivity.pathHistory.get(MainActivity.pathHistory.size() - 1)+ "/" + MainActivity.uploadName, inputStream, file.length(), null, new ProgressListener(){

			public void onProgress(long bytes, long total) {
			// TODO Auto-generated method stub
			MainActivity.progressBar.setMax((int)total);
			MainActivity.progressBar.setProgress((int)bytes);
			}
			});
			} catch (DropboxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPreExecute(){
//			MainActivity.progressBar = new ProgressDialog(this);
//			MainActivity.progressBar.setCancelable(true);
//			MainActivity.progressBar.setMessage("File uploading ...");
//			MainActivity.progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//			MainActivity.progressBar.setProgress(0);
//			MainActivity.progressBar.setMax(100);
//			MainActivity.progressBar.show();
		}

		@Override
		protected void onPostExecute(Void result) {
			MainActivity.progressBar.dismiss();
			new ListFiles().execute();
		}

	}
		
}
