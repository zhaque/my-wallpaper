package com.koonen.photostream.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.koonen.photostream.api.FilePhoto;
import com.koonen.photostream.api.Photo;

/**
 * 
 * @author glick
 * 
 */
public class ImageDAO {

	private Context context;

	public ImageDAO(Context context) {
		this.context = context;
	}

	public int getTotalCount(Uri uri) {
		Cursor cursor = context.getContentResolver().query(uri, null, null,
				null, null);
		return cursor.getCount();
	}

	public List<Photo> select(Uri uri, int start, int perPage) {
		Cursor cursor = context.getContentResolver().query(uri, null, null,
				null, null);
		List<Photo> photos = new ArrayList<Photo>(perPage);
		boolean move = cursor.moveToFirst();
		int i = 0;
		while (move && i < start + perPage) {
			if (i >= start) {
				Photo photo = createFilePhoto(cursor);
				photos.add(photo);
			}
			move = cursor.moveToNext();
			i++;
		}
		cursor.close();
		return photos;
	}

	private FilePhoto createFilePhoto(Cursor cursor) {
		FilePhoto photo = new FilePhoto("file://" +cursor.getString(1));

		// photo.setId(cursor.getInt(0));
		// photo.setPath(cursor.getString(1));

		return photo;
	}
}
