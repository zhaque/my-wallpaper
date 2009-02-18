package com.koonen.photostream.dao;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 
 * @author Glick
 *
 */
public class CategoryList {
	public final class Columns implements BaseColumns {
		public static final String CATEGORY_NAME = "category_name";
		public static final String TAGS = "tags";
	}

	public static final String TABLE_NAME = "category_list";

	public static final String AUTHORITY = "com.koonen.photostream.dao.CategoryList";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + TABLE_NAME);

	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.koonen.category_list";

	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.koonen.category_list";

}
