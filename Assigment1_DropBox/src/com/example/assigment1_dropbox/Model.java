package com.example.assigment1_dropbox;

import java.util.ArrayList;

import com.dropbox.client2.DropboxAPI.ThumbFormat;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.text.style.LeadingMarginSpan;
import android.util.Log;

public class Model {
	public static ArrayList<Item> Items;
	public static int i = 1;
	private final static String TAG = "Assignment1";


	public static void LoadModel(ArrayList<String> arrayFolder) {

		Items = new ArrayList<Item>();
		i=1;
		for (String string : arrayFolder) {

			if (string.length() >= 5) {

				if (string.substring(string.length() - 4, string.length())
						.equalsIgnoreCase(".txt")) {
					Items.add(new Item(i, "txt.png", string));
				} else if (string.substring(string.length() - 4,
						string.length()).equalsIgnoreCase(".pdf")) {
					Items.add(new Item(i, "pdf.png", string));
				} else if (string.substring(string.length() - 4,
						string.length()).equalsIgnoreCase(".zip")) {
					Items.add(new Item(i, "zip.jpg", string));
				} else if (string.substring(string.length() - 4,
						string.length()).equalsIgnoreCase(".rar")) {
					Items.add(new Item(i, "rar.jpg", string));
					
				} else if((string.substring(string.length() - 4,
						string.length()).equalsIgnoreCase(".png"))||(string.substring(string.length() - 4,
								string.length()).equalsIgnoreCase(".jpg"))){
					
					Items.add(new Item(i, "", string));
					//Items.add(new Item(i, get, string));
				} else {
					Items.add(new Item(i, "folder_image.png", string));
				}

			} else {
				Items.add(new Item(i, "folder_image.png", string));
			}
			Log.i("Test","Item size: "+Items.size());

			i++;

		}

	}
	

	public static Item GetbyId(int id) {

		for (Item item : Items) {
			if (item.Id == id) {
				return item;
			}
		}
		return null;
	}

}