package com.example.assigment1_dropbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.DropboxAPI.ThumbFormat;
import com.dropbox.client2.DropboxAPI.ThumbSize;
import com.dropbox.client2.exception.DropboxException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import android.graphics.drawable.Drawable;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

public class ItemAdapter extends ArrayAdapter<String> {

	private final Context context;
	private final String[] Ids;
	private final int rowResourceId;
	private ArrayList<String> newArrayFolder = new ArrayList<String>();
	private int pos = 0;
	private String fileName;
	private String filePath;
	private String newPath;
	private Boolean txtfile=false;
	private ProgressDialog progressBar;
	public static StringBuilder text = new StringBuilder();
	public String textAfterEdit="";
	public String newMovePath;
	private int size=0;
	public static ArrayList<String> arrayFileName = new ArrayList<String>();
	
	

	public ItemAdapter(Context context, int textViewResourceId, String[] objects) {

		super(context, textViewResourceId, objects);

		this.context = context;
		this.Ids = objects;
		this.rowResourceId = textViewResourceId;

	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView = inflater.inflate(rowResourceId, parent, false);
		rowView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				pos = position;
				new ProcessShowFiles().execute();
			}
		});

		ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
		TextView textView = (TextView) rowView.findViewById(R.id.textView);

		rowView.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				pos = position;
				
								AlertDialog.Builder builder = new AlertDialog.Builder(context);
								builder.setTitle("Files Options");
				
								final String[] choiceList = { "Rename", "Move", "Download",
										"Delete" };
				
								builder.setItems(choiceList,
										new DialogInterface.OnClickListener() {
				
											@Override
											public void onClick(DialogInterface dialog,
													int which) {
												Toast.makeText(
														context,
														"Select " + choiceList[which]
																+ " int: " + which,
														Toast.LENGTH_SHORT).show();
												switch (which) {
												case 0:
													Log.i("Test", "Option Rename");
													new ProcessShowNameFiles().execute();
				
													break;
												case 1:
													Log.i("Test", "Option Move");
													new MoveFile().execute();
													break;
												case 2:
													Log.i("Test", "Option Download");
													new ProcessShowDownloadFiles().execute();
													// new DowloadFiles().execute();
													break;
												case 3:
													Log.i("Test", "Option Delete");
													AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(
															context);
													myAlertDialog.setTitle("--- Warning ---");
													// String
													// deleteFile=MainActivity.files.get(pos).path;
													myAlertDialog
															.setMessage("Do you want to delete this?");
													myAlertDialog
															.setPositiveButton(
																	"OK",
																	new DialogInterface.OnClickListener() {
				
																		public void onClick(
																				DialogInterface arg0,
																				int arg1) {
																			// do something when
																			// the OK button is
																			// clicked
																			new ProcessDeleteFiles()
																					.execute();
																			Log.i("History",
																					"Done Delete Then Show File -Path: "
																							+ MainActivity.pathHistory
																									.get(MainActivity.pathHistory
																											.size() - 1));
																			new ProcessShowFilesAffterDeleteFiles()
																					.execute();
																		}
																	});
													myAlertDialog
															.setNegativeButton(
																	"Cancel",
																	new DialogInterface.OnClickListener() {
				
																		public void onClick(
																				DialogInterface arg0,
																				int arg1) {
																			// do something when
																			// the Cancel button
																			// is clicked
																		}
																	});
													myAlertDialog.show();
				
													break;
												default:
													break;
												}
											}
										});
								AlertDialog alert = builder.create();
								alert.show();
				return false;
			}
		});

		int id = Integer.parseInt(Ids[position]);
		String imageFile = Model.GetbyId(id).IconFile;

		textView.setText(Model.GetbyId(id).Name);
		// get input stream
		InputStream ims = null;
		
		
		if(!imageFile.equalsIgnoreCase("")){
			try {
				ims = context.getAssets().open(imageFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			
				try {
					StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
					StrictMode.setThreadPolicy(policy); 
					ims = MainActivity.mDBApi.getThumbnailStream(
							MainActivity.pathHistory.get(MainActivity.pathHistory.size()-1) + Model.GetbyId(id).Name
							, ThumbSize.ICON_256x256, ThumbFormat.JPEG);
				} catch (DropboxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		}
			
		
		
		// load image as Drawable
		Drawable d = Drawable.createFromStream(ims, null);
		// set image to ImageView
		imageView.setImageDrawable(d);
		return rowView;

	}

	public class MoveFile extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			Entry oldEntry;
			try {
				oldEntry = MainActivity.mDBApi.metadata(
						MainActivity.pathHistory.get(MainActivity.pathHistory
								.size() - 1), -1, null, true, null);
				List<Entry> oldFiles = oldEntry.contents;
				arrayFileName.clear();
				fileName = oldFiles.get(pos).fileName();
				filePath = oldFiles.get(pos).path;
				newPath = oldFiles.get(pos).parentPath();
				Log.i("Test1","ParentPath: " + newPath);
				arrayFileName.add(newPath);
				for (Entry entry : oldFiles) {
					Log.i("Test", "move file = " + entry.fileName());
					if((entry.isDir)&&!(entry.fileName().equalsIgnoreCase(fileName))){
					arrayFileName.add(entry.path);
					}
				}
			} catch (DropboxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("Option Move:");
			// builder.setMessage("What is your name:");
			final String [] choiceList = arrayFileName.toArray(new String[arrayFileName.size()]);
			builder.setItems(choiceList,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							Toast.makeText(
									context,
									"Select " + choiceList[which]
											+ " int: " + which,
									Toast.LENGTH_SHORT).show();
							newMovePath=choiceList[which];
							
							new doMoveFile().execute();
							
												
						}
			});
//			
//			// Use an EditText view to get user input.
//			final EditText input = new EditText(context);
//			// input.setId(TEXT_ID);
//			input.setText(fileName);
//			builder.setView(input);

			builder.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
//							String value = input.getText().toString();
//							Log.i("Test", "FileName: " + value);
//
//							newPath = newPath + value;
//							new ReNameFiles().execute();
//							new ProcessShowFilesAffterDeleteFiles().execute();
							return;
						}
					});

			builder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							return;
						}
					});

			builder.show();
		}		
	}
	
	public class doMoveFile extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			try {
				Log.i("Test1","File path: "+ filePath + " length: " + filePath.length());
				Log.i("Test1","New path to move: "+ newMovePath +" length: " + newMovePath.length());
				String newPath="";
				if(filePath.length() < (newMovePath.length()+fileName.length())){
					Log.i("Test1","Move in ");
					newPath=newMovePath+"/"+fileName;
				}else{
					Log.i("Test1","Move out ");
					int size=filePath.length() - fileName.length() - 2 - MainActivity.pathHistory.get(MainActivity.pathHistory.size()-2).length();
					int t = fileName.length()+size+2;
						
					newPath=filePath.substring(0, filePath.length()-t)+fileName;
					Log.i("Test1","File name " + fileName);
					Log.i("Test1","New path to move out: "+ newPath);
				}
				
				
				Entry myEntry = MainActivity.mDBApi.move(filePath, newPath);
			} catch (DropboxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			new ProcessShowFilesAffterDeleteFiles().execute();
		}
		
	}
	
	public class ProcessShowDownloadFiles extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			Entry oldEntry;
			try {
				oldEntry = MainActivity.mDBApi.metadata(
						MainActivity.pathHistory.get(MainActivity.pathHistory
								.size() - 1), -1, null, true, null);
				List<Entry> oldFiles = oldEntry.contents;
				filePath = oldFiles.get(pos).path;

				fileName = oldFiles.get(pos).fileName();
				newPath = oldFiles.get(pos).parentPath();
			} catch (DropboxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPreExecute(){
			progressBar = new ProgressDialog(context);
			progressBar.setCancelable(true);
			progressBar.setMessage("File downloading ...");
			progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressBar.setProgress(0);
			progressBar.setMax(100);
			progressBar.show();
		}

		@Override
		protected void onPostExecute(Void result) {
			progressBar.dismiss();
			new DowloadFiles().execute();
		}

	}

	public class DowloadFiles extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {

			// TODO Auto-generated method stub
			Log.i("Download", "FileName: " + fileName);
			Log.i("Download", "FilePath: " + filePath);
			File file = new File("/mnt/sdcard/Download/" + fileName);
			FileOutputStream outputStream = null;
			try {
				Log.i("Download", "Before output");
				outputStream = new FileOutputStream(file);
				Log.i("Download", "After output");
				DropboxFileInfo info = MainActivity.mDBApi.getFile(filePath,
						null, outputStream, null);
			} catch (FileNotFoundException e) {
				Log.i("Download", "file not found cmnr");
				e.printStackTrace();
			}

			catch (DropboxException e) {
				// TODO Auto-generated catch block
				Log.i("Download", "info null cmnr");
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

		}
	}

	public class ReNameFiles extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				MainActivity.mDBApi.move(filePath, newPath);
			} catch (DropboxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

	}

	public class ProcessShowNameFiles extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			Entry oldEntry;
			try {
				oldEntry = MainActivity.mDBApi.metadata(
						MainActivity.pathHistory.get(MainActivity.pathHistory
								.size() - 1), -1, null, true, null);
				List<Entry> oldFiles = oldEntry.contents;
				filePath = oldFiles.get(pos).path;

				fileName = oldFiles.get(pos).fileName();
				newPath = oldFiles.get(pos).parentPath();
			} catch (DropboxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}
		


		@Override
		protected void onPostExecute(Void result) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("Option Rename:");
			// builder.setMessage("What is your name:");

			// Use an EditText view to get user input.
			final EditText input = new EditText(context);
			// input.setId(TEXT_ID);
			input.setText(fileName);
			builder.setView(input);

			builder.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							String value = input.getText().toString();
							Log.i("Test", "FileName: " + value);

							newPath = newPath + value;
							new ReNameFiles().execute();
							new ProcessShowFilesAffterDeleteFiles().execute();
							return;
						}
					});

			builder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							return;
						}
					});

			builder.show();
		}
	}

	public class ProcessShowFiles extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... urls) {
			try {
				Log.i("History",
						"Array size: "
								+ MainActivity.pathHistory.size()
								+ " Before-Show File-Path: "
								+ MainActivity.pathHistory
										.get(MainActivity.pathHistory.size() - 1));
				Entry oldEntry = MainActivity.mDBApi.metadata(
						MainActivity.pathHistory.get(MainActivity.pathHistory
								.size() - 1), -1, null, true, null);
				List<Entry> oldFiles = oldEntry.contents;
				 filePath = oldFiles.get(pos).path;
				 fileName = oldFiles.get(pos).fileName();
				Log.i("Test1", "FileName = " + fileName);
				
				
				//Choose which one is txt,image file or folder
				//then choose the way when use click on it
				//eg folder-> open folder
				// file.txt -> open+ can edit
				// image -> open
				//other : will implement later
				
				if(fileName .substring(fileName .length() - 4, fileName.length())
						.equalsIgnoreCase(".txt")){
						//
					txtfile= true;
						Log.i("Test","Vao day");
				}else if((fileName.substring(fileName.length() - 4,
						fileName.length()).equalsIgnoreCase(".png"))||(fileName.substring(fileName.length() - 4,
								fileName.length()).equalsIgnoreCase(".jpg"))){
					openImage();
					
				}else if(oldFiles.get(pos).isDir){			
				
				Entry newEntry = MainActivity.mDBApi.metadata(filePath, -1,
						null, true, null);
				List<Entry> newfiles = newEntry.contents;
				newArrayFolder.clear();
				for (Entry entry : newfiles) {
					newArrayFolder.add(entry.fileName());
					String string = entry.fileName();
					//Log.i("Test", "newFile = " + string);
				}

				//Log.i("Test", "Array size: " + newArrayFolder.size());
				MainActivity.pathHistory.add(filePath);
				
				}
			} catch (DropboxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}
		
		
		protected void onPreExecute(){
			
			MainActivity.circle.setVisibility(View.VISIBLE);
						
		}


		@Override
		protected void onPostExecute(Void result) {
			// showFile();
	
			MainActivity.circle.setVisibility(View.GONE);
			if(txtfile == true){
				new doEditFile().execute();
			}else{
			
			Model.LoadModel(newArrayFolder);
			// ListView lv = (ListView) findViewById(R.id.listView1);
			String[] ids = new String[Model.Items.size()];
			for (int i = 0; i < ids.length; i++) {
				ids[i] = Integer.toString(i + 1);
			}

			ItemAdapter adapter = null;
			if (MainActivity.showListView == true) {
				adapter = new ItemAdapter(context, R.layout.row, ids);
				MainActivity.lv.setAdapter(adapter);
			} else if (MainActivity.showListView == false) {
				adapter = new ItemAdapter(context, R.layout.gridview_row, ids);
				MainActivity.gv.setAdapter(adapter);
			}

			adapter.notifyDataSetChanged();
			}
		}

	}

	public class ProcessDeleteFiles extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... urls) {
			try {
				Log.i("History",
						"Array size: "
								+ MainActivity.pathHistory.size()
								+ " BeforeDelete-Path: "
								+ MainActivity.pathHistory
										.get(MainActivity.pathHistory.size() - 1));
				Entry newEntry = MainActivity.mDBApi.metadata(
						MainActivity.pathHistory.get(MainActivity.pathHistory
								.size() - 1), -1, null, true, null);
				List<Entry> newfiles = newEntry.contents;
				String deleteFile = newfiles.get(pos).path;
				Log.i("History", "Delete at path: " + deleteFile + "Position: "
						+ pos);
				MainActivity.mDBApi.delete(deleteFile);

			} catch (DropboxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Log.i("History",
					"Array size: "
							+ MainActivity.pathHistory.size()
							+ " AfterDelete-Path: "
							+ MainActivity.pathHistory
									.get(MainActivity.pathHistory.size() - 1));
		}

	}

	public class ProcessShowFilesAffterDeleteFiles extends
			AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... urls) {
			try {
				Entry oldEntry = MainActivity.mDBApi.metadata(
						MainActivity.pathHistory.get(MainActivity.pathHistory
								.size() - 1), -1, null, true, null);
				List<Entry> newFiles = oldEntry.contents;
				newArrayFolder.clear();
				for (Entry entry : newFiles) {
					newArrayFolder.add(entry.fileName());
					String string = entry.fileName();
					Log.i("Test", "newFile = " + string);
				}

			} catch (DropboxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Model.LoadModel(newArrayFolder);
			// ListView lv = (ListView) findViewById(R.id.listView1);
			String[] ids = new String[Model.Items.size()];
			for (int i = 0; i < ids.length; i++) {
				ids[i] = Integer.toString(i + 1);
			}

			ItemAdapter adapter = null;
			if (MainActivity.showListView == true) {
				adapter = new ItemAdapter(context, R.layout.row, ids);
				MainActivity.lv.setAdapter(adapter);
			} else if (MainActivity.showListView == false) {
				adapter = new ItemAdapter(context, R.layout.gridview_row, ids);
				MainActivity.gv.setAdapter(adapter);
			}

			adapter.notifyDataSetChanged();
		}

	}


	public class doEditFile extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			Log.i("Test","Vao do EditFile r");
			File file = new File("/mnt/sdcard/Download/" + fileName);
			FileOutputStream outputStream = null;
			try {
				outputStream = new FileOutputStream(file);
				DropboxFileInfo info = MainActivity.mDBApi.getFile(filePath, null, outputStream, new ProgressListener() {
					
					@Override
					public void onProgress(long bytes, long total) {
						// TODO Auto-generated method stub
						progressBar.setMax((int)total);
						progressBar.setProgress((int)bytes);
						
					}
				});
				
				BufferedReader br = new BufferedReader(new FileReader(file));
			    String line;

			    	text.delete(0,text.length());
					while ((line = br.readLine()) != null) {
					    text.append(line);
					    Log.i("Test","line: "+ line);
					    text.append('\n');
					}
				
			    Log.i("Test","test ne: "+ text.toString());
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				Log.i("Test","eo down dc file");
				e1.printStackTrace();
			

			} catch (DropboxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			
			} catch (IOException e) {
			// TODO Auto-generated catch block
				Log.i("Test","eo doc dc file");
				e.printStackTrace();
			}
			
			return null;
		}

		@Override
		protected void onPreExecute() {
			progressBar = new ProgressDialog(context);
			progressBar.setCancelable(true);
			progressBar.setMessage("File Downloading...");
			progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressBar.setProgress(0);
			progressBar.setMax(100);
			progressBar.show();
		}
		
		@Override
		protected void onPostExecute(Void result) {
//			Intent int1 = new Intent(context, com.assignment1.EditTextFile.class);
//			startActivity(int1);
			
			Log.i("Test","Hien ra cai bang");
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("Edit "+ fileName + ":");
			//builder.setMessage(fileName);
			final String a= text.toString();
			Log.i("Test","content: "+ a);
			// Use an EditText view to get user input.
			final EditText input = new EditText(context);
			// input.setId(TEXT_ID);
			input.setText(text.toString());
			//text.delete(0, text.length());
			builder.setView(input);

			builder.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
//							String value = input.getText().toString();
//							Log.i("Test", "FileName: " + value);
//
//							newPath = newPath + value;
//							new ReNameFiles().execute();
//							new ProcessShowFilesAffterDeleteFiles().execute();
							//text=input.getText().toString();
							textAfterEdit="";
							 textAfterEdit =input.getText().toString();
							new DeleteAndUpdateFileWhenEdit().execute();
							return;
						}
					});

			builder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							return;
						}
					});

			builder.show();
			
			
			
			progressBar.dismiss();
		}
	}

	public class DeleteAndUpdateFileWhenEdit extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			//Override content of file.txt on the new file in sd card
			
			Log.i("Test","String a: "+ textAfterEdit);
			try {
				File file = new File("/mnt/sdcard/Download/" + fileName);
	 
				// if file doesnt exists, then create it
				if (!file.exists()) {
					file.createNewFile();
				}
	 
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(textAfterEdit);
				textAfterEdit="";
				bw.close();
	 
			} catch (IOException e) {
				e.printStackTrace();
			}			
			//delete that file on dropbox
			try {
				MainActivity.mDBApi.delete(filePath);
			} catch (DropboxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			//upload the new file after delete
			new UploadNewFileAfterDelete().execute();
		}
		
	}
	
	public class UploadNewFileAfterDelete extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			
			File file = new File("/mnt/sdcard/Download/" + fileName);
			FileInputStream inputStream = null;
			
			try {
				inputStream = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				Entry response = MainActivity.mDBApi.putFile(MainActivity.pathHistory.get(MainActivity.pathHistory.size() - 1)+ "/" + fileName, inputStream, file.length(), null, new ProgressListener() {
					
					@Override
					public void onProgress(long bytes, long total) {
						// TODO Auto-generated method stub
						progressBar.setMax((int)total);
						progressBar.setProgress((int)bytes);
					}
				});
			} catch (DropboxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}
		
		protected void onPreExecute() {
			progressBar = new ProgressDialog(context);
			progressBar.setCancelable(true);
			progressBar.setMessage("File Uploading...");
			progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressBar.setProgress(0);
			progressBar.setMax(100);
			progressBar.show();
			//new DeleteContent().execute(null, null, null);
		}
		
		@Override
		protected void onPostExecute(Void result) {
			progressBar.dismiss();
			//finish();
		}
		
	}
	
	public void openImage(){	
	}
	
	}

