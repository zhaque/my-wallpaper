package com.koonen.photostream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.util.Log;

/**
 * Background task to set the cropped image as the wallpaper. The task simply
 * open the temporary file and sets it as the new wallpaper. The task ends by
 * deleting the temporary file and display a message to the user.
 */
public class SetWallpaperTask extends UserTask<Void, Void, Boolean> {

	private static final String TAG = SetWallpaperTask.class.getCanonicalName();

	private Context context;
	private WallPaperExecutor wallPaperExecutor;

	SetWallpaperTask(Context context, WallPaperExecutor wallPaperExecutor) {
		super();
		this.context = context;
		this.wallPaperExecutor = wallPaperExecutor;
	}

	public Boolean doInBackground(Void... params) {
		boolean success = false;
		InputStream in = null;
		try {
			File file = context
					.getFileStreamPath(CropWallpaperTask.WALLPAPER_FILE_NAME);
			if (file.exists() && file.canRead()) {
				in = context
						.openFileInput(CropWallpaperTask.WALLPAPER_FILE_NAME);
				context.setWallpaper(in);
				success = true;
			}
		} catch (Exception e) {
			Log.e(TAG, "Couldn't set wallpaper", e);
			success = false;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					success = false;
				}
			}
		}
		return success;
	}

	@Override
	public void onPostExecute(Boolean result) {
		wallPaperExecutor.onCleanWallPaper();

		if (result) {
			wallPaperExecutor.onShowWallpaperSuccess();
		}

		wallPaperExecutor.onFinish();
	}
}