package com.koonen.photostream;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.koonen.photostream.api.Photo;
import com.koonen.photostream.api.PhotoSize;
import com.koonen.photostream.api.ServiceManager;
import com.koonen.utils.StreamUtils;

/**
 * Background task to crop a large version of the image. The cropped result will
 * be set as a wallpaper. The tasks starts by showing the progress bar, then
 * downloads the large version of the photo into a temporary file and ends by
 * sending an intent to the Camera application to crop the image.
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
		try {
			Bitmap bitmap = ServiceManager.get().getService().loadPhotoBitmap(
					params[0], PhotoSize.MEDIUM);
			if (bitmap != null) {
				int width = context.getWallpaperDesiredMinimumWidth();
				int height = context.getWallpaperDesiredMinimumHeight();
				Bitmap scaledBitmap = bitmap;
				if (bitmap.getWidth() != width || bitmap.getHeight() != height) {
					scaledBitmap = ImageUtilities.scale(bitmap, width, height, true);
					bitmap.recycle();
				}
				success = StreamUtils.saveBitmap(context, scaledBitmap, mFile
						.getName());
			}
		} catch (Exception e) {
			Log.e(TAG, "Photo not loaded", e);
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

			final Intent intent = new Intent("com.android.camera.action.CROP");
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