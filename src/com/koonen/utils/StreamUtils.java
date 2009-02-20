package com.koonen.utils;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

/**
 * 
 * @author dryganets
 * 
 */
public class StreamUtils {
	private static final String TAG = "StreamUtils";

	private static final int IO_BUFFER_SIZE = 1024 * 8;

	/**
	 * Copy the content of the input stream into the output stream, using a
	 * temporary byte array buffer whose size is defined by
	 * {@link #IO_BUFFER_SIZE}.
	 * 
	 * @param in
	 *            The input stream to copy from.
	 * @param out
	 *            The output stream to copy to.
	 * 
	 * @throws IOException
	 *             If any error occurs during the copy.
	 */
	public static void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] b = new byte[IO_BUFFER_SIZE];
		int read;
		while ((read = in.read(b)) != -1) {
			out.write(b, 0, read);
		}
	}

	/**
	 * Closes the specified stream.
	 * 
	 * @param stream
	 *            The stream to close.
	 */
	public static void closeStream(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				android.util.Log.e(TAG, "Could not close stream", e);
			}
		}
	}

	// TODO: refactore
	public static boolean saveBitmap(Context context, Bitmap bitmap,
			String filePath) {
		boolean result = false;
		try {
			FileOutputStream fos = context.openFileOutput(filePath,
					Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);

			bitmap.compress(CompressFormat.JPEG, 100, fos);

			fos.flush();
			fos.close();
			result = true;
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		return result;
	}
}
