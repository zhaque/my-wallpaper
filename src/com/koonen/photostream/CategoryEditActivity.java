package com.koonen.photostream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.koonen.photostream.dao.Category;

/**
 * 
 * @author Glick
 * 
 */
public class CategoryEditActivity extends Activity {

	static final String EXTRA_CATEGORY = "com.koonen.photostream.category";
	static final String EXTRA_EDIT_CATEGORY = "com.koonen.photostream.edit_category";

	private EditText categoryName;
	private EditText categoryTags;

	private Category category;
	private boolean isEdit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		category = getCategory();
		isEdit = getCategory() != null;

		setContentView(R.layout.category_edit);
		setupViews();
	}

	private Category getCategory() {
		final Intent intent = getIntent();
		final Bundle extras = intent.getExtras();

		Category category = null;
		if (extras != null) {
			category = extras.getParcelable(EXTRA_CATEGORY);
		}

		return category;
	}

	private void setupViews() {
		categoryName = (EditText) findViewById(R.id.category_name);
		categoryTags = (EditText) findViewById(R.id.category_tags);

		if (category != null) {
			categoryName.setText(category.getName());
			categoryTags.setText(category.getTags());
		}

		Button okButton = (Button) findViewById(R.id.ok_button);
		okButton.setOnClickListener(new OnClickListener() {

			private void showMessage(int resId) {
				Toast.makeText(CategoryEditActivity.this, resId,
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onClick(View v) {
				if (category == null) {
					category = Category.createCategory();
				}
				String field = categoryName.getText().toString();
				if (field == null || "".equals(field.trim())) {
					showMessage(R.string.category_name_not_empty);
					return;
				}
				category.setName(field);

				field = categoryTags.getText().toString();
				if (field == null || "".equals(field.trim())) {
					showMessage(R.string.tags_not_empty);
					return;
				}
				category.setTags(field);

				Intent intent = new Intent();
				intent.putExtra(EXTRA_CATEGORY, category);
				intent.putExtra(EXTRA_EDIT_CATEGORY, isEdit);
				setResult(RESULT_OK, intent);
				CategoryEditActivity.this.finish();
			}
		});

		Button cancelButton = (Button) findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CategoryEditActivity.this.setResult(RESULT_CANCELED);
				CategoryEditActivity.this.finish();
			}

		});
	}

	static void show(Activity context) {
		show(context, null);
	}

	static void show(Activity context, Category category) {
		Intent intent = new Intent(context, CategoryEditActivity.class);
		if (category != null) {
			intent.putExtra(EXTRA_CATEGORY, category);
		}
		context.startActivityForResult(intent,
				ActivityConstants.REQUEST_ID_CATEGORY_EDIT);
	}
}
