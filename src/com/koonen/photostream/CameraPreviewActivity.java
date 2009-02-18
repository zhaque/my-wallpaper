package com.koonen.photostream;

import java.io.FileOutputStream;
import java.util.concurrent.Semaphore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;

// ----------------------------------------------------------------------

/**
 * 
 * @author Glick
 * 
 */
public class CameraPreviewActivity extends Activity {
	private Preview mPreview;

	private static final String TAG = CameraPreviewActivity.class.getName();

	public static final String PHOTO_FROM_CAMERA = "camera.jpg";
	public static final int REQUEST_SHOW_FILE_SYSTEM_PHOTO = 44;
	public static final int RESPONSE_FILE_SYSTEM_PHOTO = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Create our Preview view and set it as the content of our activity.
		mPreview = new Preview(this);
		setContentView(mPreview);
	}

	private static final int MENU_ITEM_TAKE_PICTURE = 1;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_ITEM_TAKE_PICTURE, 0, R.string.camera_take_picture);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_TAKE_PICTURE:
			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					mPreview.setTakePicture(true);
					mPreview.takePicture();
					try {
						mPreview.getSemaphore().acquire();
					} catch (InterruptedException e) {

					}
					Bitmap bitmap = mPreview.getPicture();
					try {
						FileOutputStream fos = CameraPreviewActivity.this
								.openFileOutput(PHOTO_FROM_CAMERA,
										MODE_WORLD_READABLE);

						bitmap.compress(CompressFormat.JPEG, 100, fos);

						fos.flush();
						fos.close();
					} catch (Exception e) {
						Log.e(TAG, e.toString());
					}
					setResult(RESPONSE_FILE_SYSTEM_PHOTO);
					finish();
				}

			});
			thread.start();

			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	static void show(Activity context) {
		Intent intent = new Intent(context, CameraPreviewActivity.class);
		context.startActivityForResult(intent, REQUEST_SHOW_FILE_SYSTEM_PHOTO);
	}

}

// ----------------------------------------------------------------------

class Preview extends SurfaceView implements SurfaceHolder.Callback {
	SurfaceHolder mHolder;
	Camera mCamera;
	Bitmap mBitmap;
	private boolean takePicture;
	private final Semaphore semaphore = new Semaphore(0);

	Preview(Context context) {
		super(context);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		takePicture = false;
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		mCamera = Camera.open();
		mCamera.setPreviewDisplay(holder);
	}

	private PictureCallback jpegCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			if (isTakePicture()) {
				mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
						null);
				semaphore.release();
			}
		}

	};

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(w, h);
		mCamera.setParameters(parameters);
		mCamera.startPreview();
	}

	private synchronized boolean isTakePicture() {
		return takePicture;
	}

	public synchronized void setTakePicture(boolean takePicture) {
		this.takePicture = takePicture;
	}

	public void takePicture() {
		mCamera.takePicture(null, null, jpegCallback);
	}

	public Bitmap getPicture() {
		return mBitmap;
	}

	public Semaphore getSemaphore() {
		return semaphore;
	}
}