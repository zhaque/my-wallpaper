package com.koonen.photostream.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 
 * @author Glick
 * 
 */
public class CategoryDAO {

	public CategoryDAO() {
	}

	private void checkCategoryFields(Category category) {
		if (category.getName() == null || category.getName() == "") {
			throw new IllegalStateException(
					"category_name field can't be null or empty");
		}
		if (category.getTags() == null) {
			throw new IllegalStateException("tags field can't be null");
		}
	}

	private ContentValues createContentValues(Category category) {
		ContentValues values = new ContentValues();
		values.put(CategoryList.Columns.CATEGORY_NAME, category.getName());
		values.put(CategoryList.Columns.TAGS, category.getTags());
		return values;
	}

	private Category getCategory(Cursor cursor) {
		Category category = Category.createCategory();
		category.setId(cursor.getInt(0));
		category.setName(cursor.getString(1));
		category.setTags(cursor.getString(2));
		return category;
	}

	void insert(SQLiteDatabase database, Category category) {
		checkCategoryFields(category);

		ContentValues values = createContentValues(category);

		database.insert(CategoryList.TABLE_NAME,
				CategoryList.Columns.CATEGORY_NAME, values);
	}

	public void insert(Category category) {
		SQLiteDatabase database = PhotoUrlListProvider.getReadableDatabase();

		insert(database, category);
	}

	public int getTotalCount() {
		String sql = String.format("SELECT COUNT(*) FROM %s",
				CategoryList.TABLE_NAME);

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

	public List<Category> selectAll() {
		SQLiteDatabase database = PhotoUrlListProvider.getReadableDatabase();

		String sql = String.format("SELECT %s,%s,%s FROM %s",
				CategoryList.Columns._ID, CategoryList.Columns.CATEGORY_NAME,
				CategoryList.Columns.TAGS, CategoryList.TABLE_NAME);
		Cursor cursor = database.rawQuery(sql, null);

		List<Category> dataList = new ArrayList<Category>();
		boolean move = cursor.moveToFirst();
		while (move) {
			dataList.add(getCategory(cursor));
			move = cursor.moveToNext();
		}
		cursor.close();
		return dataList;
	}

	public void update(Category category) {
		SQLiteDatabase database = PhotoUrlListProvider.getReadableDatabase();

		checkCategoryFields(category);

		ContentValues values = createContentValues(category);

		database.update(CategoryList.TABLE_NAME, values,
				CategoryList.Columns._ID + "=" + category.getId(), null);
	}

	public void delete(long id) {
		SQLiteDatabase database = PhotoUrlListProvider.getReadableDatabase();
		database.delete(CategoryList.TABLE_NAME, CategoryList.Columns._ID + "="
				+ id, null);
	}

	public boolean isUnmodifiable(Category category) {
		return Category.RECENT_CATEGORY.equals(category.getName());
	}

	public boolean isExistCategory(Category category) {
		return selectByCategoryName(category.getName()) != null;
	}

	// private Category selectById(int id) {
	// SQLiteDatabase database = PhotoUrlListProvider.getReadableDatabase();
	//
	// String sql = String.format("SELECT %s,%s,%s FROM %s WHERE (%s=?)",
	// CategoryList.Columns._ID, CategoryList.Columns.CATEGORY_NAME,
	// CategoryList.Columns.TAGS, CategoryList.TABLE_NAME,
	// CategoryList.Columns._ID);
	// Cursor cursor = database.rawQuery(sql, new String[] { id + "" });
	//
	// boolean move = cursor.moveToFirst();
	// Category category = null;
	// if (move) {
	// category = getCategory(cursor);
	// move = cursor.moveToNext();
	// }
	// cursor.close();
	// return category;
	// }

	public Category selectByCategoryName(String categoryName) {
		SQLiteDatabase database = PhotoUrlListProvider.getReadableDatabase();

		String sql = String.format("SELECT %s,%s,%s FROM %s WHERE (%s=?)",
				CategoryList.Columns._ID, CategoryList.Columns.CATEGORY_NAME,
				CategoryList.Columns.TAGS, CategoryList.TABLE_NAME,
				CategoryList.Columns.CATEGORY_NAME);
		Cursor cursor = database.rawQuery(sql, new String[] { categoryName });

		boolean move = cursor.moveToFirst();
		Category category = null;
		if (move) {
			category = getCategory(cursor);
			move = cursor.moveToNext();
		}
		cursor.close();
		return category;
	}
}
