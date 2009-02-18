package com.koonen.photostream;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class FileBrowserActivity extends ListActivity {

	static final String EXTRA_FILE_PATH = "com.koonen.photostream.FilePath";
	public static final int REQUEST_SHOW_FILE_SYSTEM_PHOTO = 45;

	private enum DISPLAYMODE {
		ABSOLUTE, RELATIVE;
	}

	private final DISPLAYMODE displayMode = DISPLAYMODE.RELATIVE;
	private List<String> directoryEntries = new ArrayList<String>();
	private File currentDirectory = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setDefaultCurrentPath();

		browseTo(currentDirectory);
	}

	private void setDefaultCurrentPath() {
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			currentDirectory = Environment.getExternalStorageDirectory();
		} else {
			Toast.makeText(FileBrowserActivity.this,
					R.string.can_not_access_to_sdcard, Toast.LENGTH_LONG)
					.show();
			currentDirectory = new File("/");
		}
	}

	/**
	 * This function browses up one level according to the field:
	 * currentDirectory
	 */
	private void upOneLevel() {
		if (this.currentDirectory.getParent() != null)
			this.browseTo(this.currentDirectory.getParentFile());
	}

	private void browseTo(final File aDirectory) {
		if (aDirectory.isDirectory()) {
			this.currentDirectory = aDirectory;
			fill(aDirectory.listFiles());
		} else {
			clickToFile(aDirectory);
			// OnClickListener okButtonListener = new OnClickListener() {
			//
			// @Override
			// public void onClick(View v) {
			// // Lets start an intent to View the file, that was
			// // clicked...
			// clickToFile(aDirectory);
			// }
			// };
			// OnClickListener cancelButtonListener = new OnClickListener() {
			//
			// @Override
			// public void onClick(View v) {
			// }
			//
			// };

			// LinearLayout layout = new LinearLayout(this);
			// layout.setOrientation(LinearLayout.VERTICAL);
			//
			// LinearLayout fieldLayout = new LinearLayout(this);
			// fieldLayout.setOrientation(LinearLayout.VERTICAL);
			//
			// TextView view = new TextView(this);
			// view.setText("Do you want to open that file?\n");
			// view.setPadding(5, 0, 0, 0);
			// fieldLayout.addView(view);
			//
			// LinearLayout buttonLayout = new LinearLayout(this);
			// buttonLayout.setGravity(Gravity.RIGHT);
			// buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
			//
			// Button okButton = new Button(this);
			// okButton.setText("ok");
			// okButton.setOnClickListener(okButtonListener);
			// layout.addView(okButton);
			// Button cancelButton = new Button(this);
			// cancelButton.setText("cancel");
			//
			// layout.addView(fieldLayout);
			// layout.addView(buttonLayout);

			// new AlertDialog.Builder(FileBrowserActivity.this).setTitle(
			// "Question").setView(layout).create().show();
		}
	}

	private void clickToFile(File aFile) {
		Intent intent = new Intent();
		intent.putExtra(EXTRA_FILE_PATH, Uri.parse(
				"file://" + aFile.getAbsolutePath()).toString());
		setResult(ActivityConstants.SIMILAR_RESPONSE_ID, intent);
		finish();
	}

	private void fill(File[] files) {
		this.directoryEntries.clear();

		// Add the "." == "current directory"
		// and the ".." == 'Up one level'
		this.directoryEntries.add(getString(R.string.current_dir));
		if (this.currentDirectory.getParent() != null)
			this.directoryEntries.add(getString(R.string.up_one_level));

		switch (this.displayMode) {
		case ABSOLUTE:
			for (File file : files) {
				String fileName = file.getName();

				if (file.isDirectory()
						|| checkEndsWithInStringArray(fileName, getResources()
								.getStringArray(R.array.fileEndingImage))) {
					this.directoryEntries.add(file.getPath());
				}
			}
			break;
		case RELATIVE: // On relative Mode, we have to add the current-path to
			// the beginning
			int currentPathStringLenght = this.currentDirectory
					.getAbsolutePath().length();
			for (File file : files) {
				String fileName = file.getName();

				if (file.isDirectory()
						|| checkEndsWithInStringArray(fileName, getResources()
								.getStringArray(R.array.fileEndingImage))) {
					this.directoryEntries.add(file.getAbsolutePath().substring(
							currentPathStringLenght));
				}
			}
			break;
		}

		ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this,
				R.layout.file_row, this.directoryEntries);

		this.setListAdapter(directoryList);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		// int selectionRowID = (int) this.getSelectionRowID();
		// int selectionRowID = (int) this.getSelectedItemId();
		int selectionRowID = position;

		String selectedFileString = this.directoryEntries.get(selectionRowID);
		if (selectedFileString.equals(getString(R.string.current_dir))) {
			// Refresh
			this.browseTo(this.currentDirectory);
		} else if (selectedFileString.equals(getString(R.string.up_one_level))) {
			this.upOneLevel();
		} else {
			File clickedFile = null;
			switch (this.displayMode) {
			case RELATIVE:
				clickedFile = new File(this.currentDirectory.getAbsolutePath()
						+ this.directoryEntries.get(selectionRowID));
				break;
			case ABSOLUTE:
				clickedFile = new File(this.directoryEntries
						.get(selectionRowID));
				break;
			}
			if (clickedFile != null)
				this.browseTo(clickedFile);
		}
	}

	static void show(Activity context) {
		Intent intent = new Intent(context, FileBrowserActivity.class);

		context.startActivityForResult(intent, REQUEST_SHOW_FILE_SYSTEM_PHOTO);
	}

	/**
	 * Checks whether checkItsEnd ends with one of the Strings from fileEndings
	 */
	private boolean checkEndsWithInStringArray(String checkItsEnd,
			String[] fileEndings) {
		for (String aEnd : fileEndings) {
			if (checkItsEnd.endsWith(aEnd))
				return true;
		}
		return false;
	}
}