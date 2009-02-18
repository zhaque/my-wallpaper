package com.koonen.photostream.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.koonen.photostream.api.Photo;
import com.koonen.photostream.api.SourceType;

/**
 * 
 * @author Glick
 * 
 */
public class PhotoDAO {

	private static final String[] SELECT_PROJECTION = new String[] {
			PhotoUrlList.Columns._ID, // 0
			PhotoUrlList.Columns.URL, // 1
			PhotoUrlList.Columns.SOURCE_TYPE, // 2
			PhotoUrlList.Columns.PHOTO_ID };

	private Context context;

	public PhotoDAO(Context context) {
		this.context = context;
	}

	public void insert(Photo photo) {
		ContentValues values = new ContentValues();
		values.put(PhotoUrlList.Columns.URL, photo.getUrlPattern());
		values.put(PhotoUrlList.Columns.SOURCE_TYPE, photo.getSourceType()
				.getValue());
		values.put(PhotoUrlList.Columns.PHOTO_ID, photo.getPhotoId());
		context.getContentResolver().insert(PhotoUrlList.CONTENT_URI, values);
	}

	public boolean hasExist(Photo photo) {
		return selectByPhotoId(photo.getPhotoId()) != null;
	}

	private Photo selectByPhotoId(String photoId) {
		Photo photo = null;
		Cursor cursor = context.getContentResolver().query(
				PhotoUrlList.CONTENT_URI, SELECT_PROJECTION, "(photo_id = ?)",
				new String[] { photoId }, null);

		boolean move = cursor.moveToFirst();
		if (move) {
			photo = new Photo();
			photo.setId(cursor.getInt(0));
			photo.setUrlPattern(cursor.getString(1));
			photo.setSourceType(SourceType.valueOf(cursor.getString(2)));
			photo.setPhotoId(cursor.getString(3));
			move = cursor.moveToNext();
		}
		cursor.close();
		return photo;
	}

	public int getTotalCount() {
		String sql = String.format("SELECT COUNT(*) FROM %s",
				PhotoUrlList.TABLE_NAME);

		SQLiteDatabase database = PhotoUrlListProvider.getReadableDatabase();
		Cursor cursor = database.rawQuery(sql, null);
		boolean move = cursor.moveToFirst();
		int totalCount = 0;
		if (move) {
			totalCount = cursor.getInt(0);
			move = cursor.moveToNext();
		}
		cursor.close();
		return totalCount;
	}

	public List<Photo> select(int start, int perPage) {
		String sql = String.format("SELECT %s,%s,%s,%s FROM %s LIMIT %s,%s",
				PhotoUrlList.Columns._ID, PhotoUrlList.Columns.URL,
				PhotoUrlList.Columns.SOURCE_TYPE,
				PhotoUrlList.Columns.PHOTO_ID, PhotoUrlList.TABLE_NAME, start,
				perPage);

		SQLiteDatabase database = PhotoUrlListProvider.getReadableDatabase();
		Cursor cursor = database.rawQuery(sql, null);

		List<Photo> dataList = new ArrayList<Photo>();
		boolean move = cursor.moveToFirst();
		while (move) {
			Photo photo = new Photo();
			photo.setId(cursor.getInt(0));
			photo.setUrlPattern(cursor.getString(1));
			photo.setSourceType(SourceType.valueOf(cursor.getString(2)));
			photo.setPhotoId(cursor.getString(3));
			dataList.add(photo);
			move = cursor.moveToNext();
		}
		cursor.close();
		return dataList;
	}

	public List<Photo> selectAll() {
		Cursor cursor = context.getContentResolver().query(
				PhotoUrlList.CONTENT_URI, SELECT_PROJECTION, null, null, null);

		List<Photo> dataList = new ArrayList<Photo>();
		boolean move = cursor.moveToFirst();
		while (move) {
			Photo photo = new Photo();
			photo.setId(cursor.getInt(0));
			photo.setUrlPattern(cursor.getString(1));
			photo.setSourceType(SourceType.valueOf(cursor.getString(2)));
			photo.setPhotoId(cursor.getString(3));
			dataList.add(photo);
			move = cursor.moveToNext();
		}
		cursor.close();
		return dataList;
	}

	public void delete(Photo photo) {
		SQLiteDatabase database = PhotoUrlListProvider.getReadableDatabase();
		database.delete(PhotoUrlList.TABLE_NAME, PhotoUrlList.Columns.PHOTO_ID
				+ "=" + photo.getPhotoId(), null);
	}

//	public void delete(long id) {
//		context.getContentResolver().delete(PhotoUrlList.CONTENT_URI,
//				PhotoUrlList.Columns._ID + "=?", new String[] { id + "" });
//	}
}
