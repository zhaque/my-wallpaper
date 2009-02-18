package com.koonen.photostream.dao;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 
 * @author Glick
 *
 */
public final class PhotoUrlList {
	public final class Columns implements BaseColumns {
		public static final String URL = "url";
		public static final String SOURCE_TYPE = "source_type";
		public static final String PHOTO_ID = "photo_id";
	}

	public static final String TABLE_NAME = "photo_url_list";

	public static final String AUTHORITY = "com.koonen.photostream.dao.PhotoUrlList";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + TABLE_NAME);

	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.koonen.photo_url_list";

	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.koonen.photo_url_list";
}
