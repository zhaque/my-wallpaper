package com.koonen.photostream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.koonen.photostream.api.Photo;
import com.koonen.photostream.api.PhotoSize;
import com.koonen.photostream.api.ServiceManager;

/**
 * Background task to crop a large version of the image. The cropped result
 * will be set as a wallpaper. The tasks starts by showing the progress bar,
 * then downloads the large version of the photo into a temporary file and
 * ends by sending an intent to the Camera application to crop the image.
 */
public class CropWallpaperTask extends UserTask<Photo, Void, Boolean> {
	
	public interface CropWallpaperExecutor {
		public void onPreExecute();
		
		public void onPostExecuteSuccess(final Intent intent);
		
		public void onPostExecuteError();
		
		public void onPostExecuteFinish();
	}
	
	private static final String TAG = "CropWallpaperTask";
	
	public static final String WALLPAPER_FILE_NAME = "wallpaper";
	
	public static final int REQUEST_CROP_IMAGE = 42;

	private File mFile;
	private Context context;
	private CropWallpaperExecutor cropWallpaperExecutor;

	CropWallpaperTask(Context context,
			CropWallpaperExecutor cropWallpaperExecutor) {
		super();
		this.context = context;
		this.cropWallpaperExecutor = cropWallpaperExecutor;
	}

	@Override
	public void onPreExecute() {
		mFile = context.getFileStreamPath(WALLPAPER_FILE_NAME);
		cropWallpaperExecutor.onPreExecute();
	}

	public Boolean doInBackground(Photo... params) {
		boolean success = false;

		OutputStream out = null;
		try {
			out = context.openFileOutput(mFile.getName(), Context.MODE_WORLD_READABLE
					| Context.MODE_WORLD_WRITEABLE);
			ServiceManager.get().getService().downloadPhoto(params[0],
					PhotoSize.MEDIUM, out);
			success = true;
		} catch (FileNotFoundException e) {
			android.util.Log.e(TAG, "Could not download photo", e);
			success = false;
		} catch (IOException e) {
			android.util.Log.e(TAG, "Could not download photo", e);
			success = false;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					success = false;
				}
			}
		}

		return success;
	}

	@Override
	public void onPostExecute(Boolean result) {
		if (!result) {
			cropWallpaperExecutor.onPostExecuteError();
		} else {
			final int width = context.getWallpaperDesiredMinimumWidth();
			final int height = context.getWallpaperDesiredMinimumHeight();

			final Intent intent = new Intent(
					"com.android.camera.action.CROP");
			intent.setClassName("com.android.camera",
					"com.android.camera.CropImage");
			intent.setData(Uri.fromFile(mFile));
			intent.putExtra("outputX", width);
			intent.putExtra("outputY", height);
			intent.putExtra("aspectX", width);
			intent.putExtra("aspectY", height);
			intent.putExtra("scale", true);
			intent.putExtra("noFaceDetection", true);
			intent.putExtra("output", Uri.parse("file:/"
					+ mFile.getAbsolutePath()));

			cropWallpaperExecutor.onPostExecuteSuccess(intent);
		}

		cropWallpaperExecutor.onPostExecuteFinish();
	}
}