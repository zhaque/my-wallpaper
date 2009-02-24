package com.koonen.photostream.dao;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.koonen.photostream.R;

/**
 * 
 * @author Glick
 * 
 */
public class PhotoUrlListProvider extends ContentProvider {

	private static final int PHOTO_URL_LIST = 1;

	private static final int PHOTO_URL_LIST_ID = 2;

	private static final String TAG = PhotoUrlListProvider.class
			.getCanonicalName();

	private static final String DATABASE_NAME = "PhotostreamDB";

	private static final int DATABASE_VERSION = 7;

	private static DatabaseHelper openHelper;

	private static UriMatcher photoUriMatcher;
	private static HashMap<String, String> photoUrlListProjectionMap;

	static {
		photoUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		photoUriMatcher.addURI(PhotoUrlList.AUTHORITY, PhotoUrlList.TABLE_NAME,
				PHOTO_URL_LIST);
		photoUriMatcher.addURI(PhotoUrlList.AUTHORITY, PhotoUrlList.TABLE_NAME
				+ "/#", PHOTO_URL_LIST_ID);

		photoUrlListProjectionMap = new HashMap<String, String>();
		photoUrlListProjectionMap.put(PhotoUrlList.Columns._ID,
				PhotoUrlList.Columns._ID);
		photoUrlListProjectionMap.put(PhotoUrlList.Columns.URL,
				PhotoUrlList.Columns.URL);
		photoUrlListProjectionMap.put(PhotoUrlList.Columns.SOURCE_TYPE,
				PhotoUrlList.Columns.SOURCE_TYPE);
		photoUrlListProjectionMap.put(PhotoUrlList.Columns.PHOTO_ID,
				PhotoUrlList.Columns.PHOTO_ID);
	}

	public static class DatabaseHelper extends SQLiteOpenHelper {
		private Context context;

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			this.context = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + PhotoUrlList.TABLE_NAME + " ("
					+ PhotoUrlList.Columns._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ PhotoUrlList.Columns.URL + " TEXT NOT NULL,"
					+ PhotoUrlList.Columns.SOURCE_TYPE + " TEXT NOT NULL,"
					+ PhotoUrlList.Columns.PHOTO_ID + " TEXT NOT NULL" + ");");

			db.execSQL("CREATE TABLE " + CategoryList.TABLE_NAME + " ("
					+ CategoryList.Columns._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ CategoryList.Columns.CATEGORY_NAME + " TEXT NOT NULL,"
					+ CategoryList.Columns.TAGS + " TEXT NOT NULL" + ");");

			Category category = Category.createRecentCategory();

			CategoryDAO categoryDAO = new CategoryDAO(db);
			categoryDAO.insert(db, category);

			String[] categoryNames = context.getResources().getStringArray(
					R.array.init_categories_names_v7);
			String[] categoryValues = context.getResources().getStringArray(
					R.array.init_categories_values_v7);

			for (int i = 0; i < categoryNames.length; i++) {
				category = Category.createCategory();
				category.setName(categoryNames[i]);
				category.setTags(categoryValues[i]);
				categoryDAO.insert(db, category);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion);

			CategoryDAO categoryDAO = new CategoryDAO(db);
			List<Category> oldCategories = categoryDAO.selectAll();

			switch (oldVersion) {
			case 6:
				// create filter set
				String[] categoriesV6 = context.getResources().getStringArray(
						R.array.init_categories_v6);
				Set<String> filtersSet = new HashSet<String>(Arrays
						.asList(categoriesV6));
				// filter exists categories
				for (Iterator<Category> iterator = oldCategories.iterator(); iterator
						.hasNext();) {
					Category category = (Category) iterator.next();
					if (category.getName().equalsIgnoreCase(category.getTags())
							&& filtersSet.contains(category.getName())) {
						iterator.remove();
					}
				}
				break;
			}

			db.execSQL("DROP TABLE IF EXISTS " + PhotoUrlList.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + CategoryList.TABLE_NAME);

			onCreate(db);

			for (Category category : oldCategories) {
				if (!category.isRecent()) {
					categoryDAO.insert(db, category);
				}
			}
		}

	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		int count;
		switch (photoUriMatcher.match(uri)) {
		case PHOTO_URL_LIST:
			count = db.delete(PhotoUrlList.TABLE_NAME, where, whereArgs);
			break;

		case PHOTO_URL_LIST_ID:
			String id = uri.getPathSegments().get(1);
			count = db.delete(PhotoUrlList.TABLE_NAME,
					PhotoUrlList.Columns._ID
							+ "="
							+ id
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		Intent intent = new Intent();
		intent.setData(uri);

		getContext().sendBroadcast(intent);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (photoUriMatcher.match(uri)) {
		case PHOTO_URL_LIST:
			return PhotoUrlList.CONTENT_TYPE;

		case PHOTO_URL_LIST_ID:
			return PhotoUrlList.CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		// Validate the requested uri
		if (photoUriMatcher.match(uri) != PHOTO_URL_LIST) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		if (values.containsKey(PhotoUrlList.Columns.URL) == false) {
			throw new IllegalStateException("url field can't be null");
		}

		if (values.containsKey(PhotoUrlList.Columns.SOURCE_TYPE) == false) {
			throw new IllegalStateException("source_type field can't be null");
		}

		if (values.containsKey(PhotoUrlList.Columns.PHOTO_ID) == false) {
			throw new IllegalStateException("photo_id field can't be null");
		}

		SQLiteDatabase db = openHelper.getWritableDatabase();
		long rowId = db.insert(PhotoUrlList.TABLE_NAME,
				PhotoUrlList.Columns.URL, values);

		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(PhotoUrlList.CONTENT_URI,
					rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		openHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (photoUriMatcher.match(uri)) {
		case PHOTO_URL_LIST:
			qb.setTables(PhotoUrlList.TABLE_NAME);
			qb.setProjectionMap(photoUrlListProjectionMap);
			break;

		case PHOTO_URL_LIST_ID:
			qb.setTables(PhotoUrlList.TABLE_NAME);
			qb.setProjectionMap(photoUrlListProjectionMap);
			qb.appendWhere(PhotoUrlList.Columns._ID + "="
					+ uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Get the database and run the query
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, null);

		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		int count;
		switch (photoUriMatcher.match(uri)) {
		case PHOTO_URL_LIST:
			count = db
					.update(PhotoUrlList.TABLE_NAME, values, where, whereArgs);
			break;

		case PHOTO_URL_LIST_ID:
			String noteId = uri.getPathSegments().get(1);
			count = db.update(PhotoUrlList.TABLE_NAME, values,
					PhotoUrlList.Columns._ID
							+ "="
							+ noteId
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	public static SQLiteDatabase getReadableDatabase() {
		return openHelper.getReadableDatabase();
	}
}
