package com.koonen.photostream;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.koonen.photostream.CropWallpaperTask.CropWallpaperExecutor;
import com.koonen.photostream.api.Photo;
import com.koonen.photostream.api.PhotoList;
import com.koonen.photostream.api.ServiceContext;
import com.koonen.photostream.api.ServiceException;
import com.koonen.photostream.api.ServiceManager;
import com.koonen.photostream.settings.BackgroundSource;
import com.koonen.photostream.settings.UserPreferences;

/**
 * 
 * @author Glick
 * 
 */
public class RotationService extends Service {

	private static final String LOG_TAG = RotationService.class
			.getCanonicalName();
	private static final int INTERVAL = 60000;

	private UserTask<?, ?, ?> mTask;

	private Long prevElapsedRealtime = null;

	private UserPreferences userPreferences = null;
	private ServiceContext serviceContext = null;

	private Timer timer;
	private boolean started = false;

	private ServiceManager manager;

	@Override
	public void onCreate() {
		super.onCreate();
		ServiceManager.init(this);
		manager = ServiceManager.get();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return stub;
	}

	private void cleanupWallpaper() {
		deleteFile(CropWallpaperTask.WALLPAPER_FILE_NAME);
	}

	private void showMessage(int codeMessage) {
		if (userPreferences.isRotationNotificationEnabled()) {
			Toast.makeText(RotationService.this, codeMessage,
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		android.util.Log.e(LOG_TAG, "Rotation service Start "
				+ new Date().toString());

		if (!started) {
			started = true;
			runService(intent, startId);
		}

	}

	private void runService(Intent intent, int startId) {
		super.onStart(intent, startId);

		if (ServiceManager.get() == null) {
			ServiceManager.init(this);
		}

		if (userPreferences == null) {
			userPreferences = new UserPreferences(this);
			userPreferences
					.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {

						@Override
						public void onSharedPreferenceChanged(
								SharedPreferences sharedPreferences, String key) {
							if (key.equals(UserPreferences.ROTATION_SOURCE_KEY)) {
								serviceContext = null;
							}
						}

					});
		}

		timer = new Timer();
		final TimerTask timerTask = new TimerTask() {

			@Override
			public void run() {
				android.util.Log.e(LOG_TAG,
						"Rotation service timer task start "
								+ new Date().toString());

				if (!userPreferences.isRotationEnabed()) {
					return;
				}

				long interval = userPreferences.getRotationSchedule();
				long elapsedRealtime = SystemClock.elapsedRealtime();
				if (prevElapsedRealtime == null) {
					prevElapsedRealtime = new Long(elapsedRealtime);
				}

				if (elapsedRealtime - prevElapsedRealtime >= interval) {
					prevElapsedRealtime = new Long(elapsedRealtime);

					if (serviceContext == null) {
						BackgroundSource source = userPreferences
								.getRotationBackgroundSource();
						if (source == BackgroundSource.RANDOM) {
							serviceContext = ServiceContext
									.createRecentContext(1);
						} else if (source == BackgroundSource.FAVORITES) {
							serviceContext = ServiceContext
									.createFavoritesServiceContext(1);
						} else if (source == BackgroundSource.PERSONAL) {
							serviceContext = ServiceContext
									.createPersonalContext(1);
						}
					} else {
						if (serviceContext.isNext()) {
							serviceContext.next();
						} else {
							serviceContext.setCurrentPage(1);
						}
					}
					try {
						PhotoList photoList = manager.getService().execute(
								serviceContext);

						if (photoList != null && photoList.getCount() > 0) {
							Photo photo = photoList.get(0);
							mTask = new CropWallpaperTask(RotationService.this,
									new CropWallpaperExecutorRotationService())
									.execute(photo);
						}
					} catch (ServiceException e) {
						Log.e(LOG_TAG, e.getMessage());
					}
				}

			}

		};

		timer.schedule(timerTask, INTERVAL, INTERVAL);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mTask != null && mTask.getStatus() == UserTask.Status.RUNNING) {
			mTask.cancel(true);
		}
		timer.cancel();
	}

	private class CropWallpaperExecutorRotationService implements
			CropWallpaperExecutor {

		@Override
		public void onPostExecuteError() {
			cleanupWallpaper();
		}

		@Override
		public void onPostExecuteFinish() {
		}

		@Override
		public void onPostExecuteSuccess(Intent intent) {
			RotationService.this.mTask = new SetWallpaperTask(
					RotationService.this, new SetWallpaperExecutor()).execute();
		}

		@Override
		public void onPreExecute() {
		}
	}

	private class SetWallpaperExecutor implements WallPaperExecutor {

		@Override
		public void onCleanWallPaper() {
			cleanupWallpaper();
		}

		@Override
		public void onFinish() {
			mTask = null;
		}

		public void onShowWallpaperError() {
			showMessage(R.string.error_cannot_save_file);
		}

		public void onShowWallpaperSuccess() {
			showMessage(R.string.success_wallpaper_set);
		}
	}

	private final IRotationService.Stub stub = new IRotationService.Stub() {

		@Override
		public boolean isRun() throws RemoteException {
			return started;
		}

		@Override
		public void run() throws RemoteException {
			final Intent intent = new Intent(RotationService.this,
					RotationService.class);
			RotationService.this.startService(intent);
		}

	};
}
