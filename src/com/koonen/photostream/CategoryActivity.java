package com.koonen.photostream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.koonen.photostream.dao.Category;

/**
 * 
 * @author Glick
 * 
 */
public class CategoryActivity extends ListActivity {

	private static final String TAG = CategoryActivity.class.getCanonicalName();

	private static final int MENU_ITEM_CATEGORY_ADD = 1;
	private static final int MENU_ITEM_CATEGORY_UPDATE = 2;
	private static final int MENU_ITEM_CATEGORY_DELETE = 3;

	private CategoryAdapter adapter;

	private static final String PREFERENCES_CATEGORY = "CATEGORY";
	private static final String PREFERENCE_CATEGORY_IS_FIRST = "eula.isFirst";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new CategoryAdapter(this);
		this.setListAdapter(adapter);

		final SharedPreferences preferences = getSharedPreferences(
				PREFERENCES_CATEGORY, Activity.MODE_PRIVATE);
		if (!preferences.getBoolean(PREFERENCE_CATEGORY_IS_FIRST, false)) {
			LinearLayout layout = new LinearLayout(this);
			LayoutParams layoutParams = new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layout.setLayoutParams(layoutParams);
			layout.setPadding(5, 0, 0, 0);
			TextView view = new TextView(this);
			view.setText(R.string.category_first_open);
			view.setLayoutParams(layoutParams);
			layout.addView(view);

			final AlertDialog dialog = new AlertDialog.Builder(this).setTitle(
					R.string.dialog_name_info).setView(layout).create();

			dialog.setButton(
					getResources().getString(R.string.ok_button_label),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							commitCategoryIsFirst(preferences);
							dialog.dismiss();
						}

					});
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

				@Override
				public void onCancel(DialogInterface arg0) {
					commitCategoryIsFirst(preferences);
				}
			});

			dialog.getWindow().setGravity(Gravity.CENTER);
			dialog.show();
		}
	}

	private void commitCategoryIsFirst(SharedPreferences preferences) {
		preferences.edit().putBoolean(PREFERENCE_CATEGORY_IS_FIRST, true)
				.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_ITEM_CATEGORY_ADD, 0, R.string.menu_category_add)
				.setIcon(android.R.drawable.ic_menu_edit);

		menu
				.add(0, MENU_ITEM_CATEGORY_UPDATE, 1,
						R.string.menu_category_update).setIcon(
						android.R.drawable.ic_menu_edit);

		menu
				.add(0, MENU_ITEM_CATEGORY_DELETE, 2,
						R.string.menu_category_delete).setIcon(
						android.R.drawable.ic_menu_delete);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		int position = getSelectedItemPosition();
		if (position != AdapterView.INVALID_POSITION) {
			Category category = (Category) adapter.getItem(position);
			boolean canModify = adapter.isExistCategory(category)
					&& !adapter.isUnmodifiable(category);
			menu.findItem(MENU_ITEM_CATEGORY_UPDATE).setEnabled(canModify);
			menu.findItem(MENU_ITEM_CATEGORY_DELETE).setEnabled(canModify);
		} else {
			menu.findItem(MENU_ITEM_CATEGORY_UPDATE).setEnabled(false);
			menu.findItem(MENU_ITEM_CATEGORY_DELETE).setEnabled(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_CATEGORY_ADD:
			CategoryEditActivity.show(this);
			return true;
		case MENU_ITEM_CATEGORY_UPDATE: {
			int position = getSelectedItemPosition();
			if (position != AdapterView.INVALID_POSITION) {
				Category category = (Category) adapter.getItem(position);
				CategoryEditActivity.show(this, category);
			}
			return true;
		}
		case MENU_ITEM_CATEGORY_DELETE: {
			int position = getSelectedItemPosition();
			if (position != AdapterView.INVALID_POSITION) {
				Category category = (Category) adapter.getItem(position);
				adapter.remove(category);
			}
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ActivityConstants.REQUEST_ID_CATEGORY_EDIT) {
			if (resultCode == RESULT_OK) {
				final Bundle extras = data.getExtras();

				Category category = null;
				boolean isEdit = false;
				boolean success = true;
				if (extras != null) {
					category = extras
							.getParcelable(CategoryEditActivity.EXTRA_CATEGORY);
					isEdit = extras
							.getBoolean(CategoryEditActivity.EXTRA_EDIT_CATEGORY);
				}
				if (category == null) {
					success = false;
				} else {
					try {
						Category category2 = adapter.getCategoryByName(category
								.getName());
						if (isEdit) {
							if (category2 == null
									|| category2.getId() == category.getId()) {
								adapter.update(category);
								showMessage(R.string.category_save_successfully);
							} else {
								showMessage(R.string.category_exist);
							}
						} else {
							if (category2 == null) {
								adapter.add(category);
								showMessage(R.string.category_save_successfully);
							} else {
								showMessage(R.string.category_exist);
							}
						}
					} catch (Exception e) {
						Log.e(TAG, "Couldn't save category", e);
						success = false;
					}
				}
				if (!success) {
					showMessage(R.string.category_save_fail);
				}
			}
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (position != AdapterView.INVALID_POSITION) {
			Category category = (Category) adapter.getItem(position);
			Intent intent = new Intent();
			intent.putExtra(CategoryEditActivity.EXTRA_CATEGORY, category);
			setResult(ActivityConstants.RESULT_CATEGORY_SEARCH, intent);
			finish();
		}
	}

	private void showMessage(int resId) {
		Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
	}

	static void show(Activity context) {
		Intent intent = new Intent(context, CategoryActivity.class);
		context.startActivityForResult(intent,
				ActivityConstants.REQUEST_ID_CATEGORY_SEARCH);
	}
}
