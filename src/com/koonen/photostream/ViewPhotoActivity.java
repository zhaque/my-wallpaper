/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.koonen.photostream;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.koonen.photostream.CropWallpaperTask.CropWallpaperExecutor;
import com.koonen.photostream.api.FilePhoto;
import com.koonen.photostream.api.Photo;
import com.koonen.photostream.api.PhotoList;
import com.koonen.photostream.api.PhotoSize;
import com.koonen.photostream.api.ServiceContext;
import com.koonen.photostream.api.ServiceManager;
import com.koonen.photostream.api.ServiceNetworkException;
import com.koonen.photostream.api.UserInfo;
import com.koonen.photostream.api.UserNotFoundException;
import com.koonen.photostream.dao.PhotoDAO;
import com.koonen.photostream.settings.UserPreferences;
import com.koonen.photostream.settings.UserSettingsActivity;
import com.koonen.utils.ConfigurationReader;
import com.koonen.utils.StreamUtils;

/**
 * Activity that displays a photo along with its title and the date at which it
 * was taken. This activity also lets the user set the photo as the wallpaper.
 */
public class ViewPhotoActivity extends Activity implements
		View.OnClickListener, ViewTreeObserver.OnGlobalLayoutListener,
		Animation.AnimationListener {

	// private static final String TAG = "ViewPhotoActivity";

	static final String ACTION = "com.google.android.photostream.FLICKR_PHOTO";
	static final String EXTRA_SEARCH_TAGS = "com.koonen.photostream.SearchTags";

	static final int SIMILAR_RESPONSE_ID = 1;

	static final int VIEW_PHOTO_REQUEST_ID = 1;
	static final int PREVIEW_PHOTO_REQUEST_ID = 2;

	static final int VIEW_PHOTO_SHARE_ID = 2;

	private static final String EXTRA_PHOTO = "com.koonen.photostream.photo";

	private static final String EXTRA_SERVICE_CONTEXT = "com.koonen.photostream.service_context";

	// private static final int DIALOG_SHARE = 1;
	// private static final int DIALOG_SHARE_ERROR = 2;

	private static final String STATE_SHOW_REFRESH = "show_refresh";
	private static final String STATE_PHOTO = "photo";

	public static final int REQUEST_SENT_EMAIL = 43;

	private static final String TAG = "ViewPhotoActivity";

	private Photo mPhoto;
	private ServiceContext serviceContext;
	private boolean isFirstOpen;
	private Bitmap mBitmap;
	private boolean restoreBitmap;
	private boolean loading = false;

	private ViewAnimator mSwitcher;
	private ImageView mPhotoView;
	private ViewGroup mContainer;
	private Handler handler;
	private ImageView refreshImage;

	private View mMenuMore;
	private View mMenuNext;
	private View mMenuPrev;
	private View mMenuSeparator;

	private UserTask<?, ?, ?> mTask;
	private TextView mPhotoTags;
	private TextView mPhotoLocation;
	private TextView mPhotoDate;

	private LayoutAnimationController mNextAnimation;
	private LayoutAnimationController mBackAnimation;

	private PhotoDAO photoDAO;
	private UserPreferences userPreferences;

	private boolean photoExist = false;
	private static final String STATE_CONTEXT = "requeset_context";

	private int currentOrientation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		photoDAO = new PhotoDAO(this);
		userPreferences = new UserPreferences(this);
		handler = new Handler();

		mPhoto = getPhoto();
		serviceContext = getServiceContext();
		isFirstOpen = true;

		// mBitmap = null;
		restoreBitmap = false;

		Configuration config = new ConfigurationReader(this).getConfiguration();
		currentOrientation = config.orientation;
		initActivity();
	}

	private void initActivity() {
		if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
			setContentView(R.layout.screen_photo);
		} else {
			setContentView(R.layout.screen_photo_landscape);
		}
		setupViews();
	}

	/**
	 * Starts the ViewPhotoActivity for the specified photo.
	 * 
	 * @param context
	 *            The application's environment.
	 * @param photo
	 *            The photo to display and optionally set as a wallpaper.
	 */
	static void show(Activity context, Photo photo,
			ServiceContext serviceContext, int photoPageNumber) {
		ServiceContext singleServiceContext = ServiceContext
				.createSingleContext(serviceContext, photoPageNumber);
		final Intent intent = new Intent(context, ViewPhotoActivity.class);

		intent.putExtra(EXTRA_PHOTO, photo);
		intent.putExtra(EXTRA_SERVICE_CONTEXT, singleServiceContext);
		context.startActivityForResult(intent, VIEW_PHOTO_REQUEST_ID);
	}

	static void show(Activity context, FilePhoto photo) {
		ServiceContext cameraServiceContext = ServiceContext
				.createCameraServiceContext();
		final Intent intent = new Intent(context, ViewPhotoActivity.class);

		intent.putExtra(EXTRA_PHOTO, photo);
		intent.putExtra(EXTRA_SERVICE_CONTEXT, cameraServiceContext);
		context.startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mTask != null && mTask.getStatus() != UserTask.Status.RUNNING) {
			mTask.cancel(true);
		}
	}

	private void setupViews() {
		mNextAnimation = AnimationUtils.loadLayoutAnimation(this,
				R.anim.layout_slide_next);
		mBackAnimation = AnimationUtils.loadLayoutAnimation(this,
				R.anim.layout_slide_back);

		mContainer = (ViewGroup) findViewById(R.id.container_photo);
		mSwitcher = (ViewAnimator) findViewById(R.id.switcher_menu);
		mPhotoView = (ImageView) findViewById(R.id.image_photo);

		mPhotoTags = (TextView) findViewById(R.id.caption_tag);
		mPhotoLocation = (TextView) findViewById(R.id.caption_location);
		mPhotoDate = (TextView) findViewById(R.id.caption_date);

		mMenuNext = findViewById(R.id.menu_next);
		mMenuPrev = findViewById(R.id.menu_prev);
		mMenuSeparator = findViewById(R.id.menu_separator);
		mMenuMore = findViewById(R.id.menu_more);

		mMenuNext.setOnClickListener(this);
		mMenuPrev.setOnClickListener(this);
		mMenuMore.setOnClickListener(this);

		mContainer.setVisibility(View.INVISIBLE);

		refreshImage = (ImageView) findViewById(R.id.refresh_image);
		refreshImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				refreshImage.setVisibility(View.INVISIBLE);
				animateAndLoadPhotos(mNextAnimation);
			}

		});

		// Sets up a view tree observer. The photo will be scaled using the size
		// of one of our views so we must wait for the first layout pass to be
		// done to make sure we have the correct size.
		mContainer.getViewTreeObserver().addOnGlobalLayoutListener(this);

		if (loading) {
			this.showProgress();
		}
	}

	/**
	 * Loads the photo after the first layout. The photo is scaled using the
	 * dimension of the ImageView that will ultimately contain the photo's
	 * bitmap. We make sure that the ImageView is laid out at least once to get
	 * its correct size.
	 */
	public void onGlobalLayout() {
		mContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
		loadPhoto();
	}

	private void prepareMenu() {
		boolean backVisible = !serviceContext.isRecent()
				&& serviceContext.isPagable() && serviceContext.isPrev();
		boolean nextVisible = !serviceContext.isRecent()
				&& serviceContext.isPagable() && serviceContext.isNext();
		boolean moreVisible = serviceContext.isRecent()
				&& serviceContext.isPagable();

		if (refreshImage.getVisibility() == View.VISIBLE) {
			backVisible = false;
			nextVisible = false;
			moreVisible = false;
		}

		mMenuPrev.setVisibility(backVisible ? View.VISIBLE : View.GONE);
		mMenuNext.setVisibility(nextVisible ? View.VISIBLE : View.GONE);
		mMenuSeparator.setVisibility(backVisible && nextVisible ? View.VISIBLE
				: View.GONE);
		mMenuMore.setVisibility(moreVisible ? View.VISIBLE : View.GONE);
	}

	/**
	 * Loads the photo either from the last known instance or from the network.
	 * Loading it from the last known instance allows for fast display rotation
	 * without having to download the photo from the network again.
	 * 
	 * @param width
	 *            The desired maximum width of the photo.
	 * @param height
	 *            The desired maximum height of the photo.
	 */
	private void loadPhoto() {
		final Object data = getLastNonConfigurationInstance();
		if (data == null && !restoreBitmap && !isLoading()) {
			Log.i(TAG, "geting data from service");
			int width = mPhotoView.getMeasuredWidth();
			int height = mPhotoView.getMeasuredHeight();
			mTask = new LoadPhotoTask().execute(mPhoto, width, height);
		} else {
			if (restoreBitmap) {
				setPhoto(mBitmap);
				restoreBitmap = false;

				prepareMenu();
				mTask = null;

				mSwitcher.showNext();
				mContainer.setVisibility(View.VISIBLE);
			} else {
				// LoadedBitmap loadedBitmap = (LoadedBitmap) data;
				// mPhotoView.setImageBitmap(loadedBitmap.getBitmap());

				mPhotoView.setImageBitmap((Bitmap) data);
				mSwitcher.showNext();
				prepareMenu();
			}
		}
	}

	/**
	 * Loads the {@link com.koonen.photostream.Flickr.Photo} to display from the
	 * intent used to start the activity.
	 * 
	 * @return The photo to display, or null if the photo cannot be found.
	 */
	public Photo getPhoto() {
		final Intent intent = getIntent();
		final Bundle extras = intent.getExtras();

		Photo photo = null;
		if (extras != null) {
			photo = extras.getParcelable(EXTRA_PHOTO);
		}

		return photo;
	}

	public ServiceContext getServiceContext() {
		final Intent intent = getIntent();
		final Bundle extras = intent.getExtras();

		ServiceContext serviceContext = null;
		if (extras != null) {
			serviceContext = extras.getParcelable(EXTRA_SERVICE_CONTEXT);
		}

		return serviceContext;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.view_photo, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean notFilePhoto = !(mPhoto instanceof FilePhoto);
		if (!notFilePhoto) {
			FilePhoto filePhoto = (FilePhoto) mPhoto;
			notFilePhoto = filePhoto.isCached();
		}
		MenuItem item = menu.findItem(R.id.menu_item_save_as_faivorites);
		if (notFilePhoto) {
			photoExist = photoDAO.hasExist(mPhoto);
			if (photoExist) {
				item.setTitle(R.string.menu_item_delete_from_faivorites);
			} else {
				item.setTitle(R.string.menu_item_add_to_faivorites);
			}
			item.setEnabled(true);
		} else {
			item.setTitle(R.string.menu_item_add_to_faivorites);
			item.setEnabled(false);
		}
		menu.findItem(R.id.menu_item_share).setEnabled(notFilePhoto);
		String tags = mPhoto.getTags();
		menu.findItem(R.id.menu_item_similar).setEnabled(
				notFilePhoto && tags != null && !"".equals(tags));
		return super.onPrepareOptionsMenu(menu);

	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_save_as_faivorites:
			onFavorite();
			break;
		/*
		 * case R.id.menu_item_radar: onShowRadar(); break;
		 */
		case R.id.menu_item_share:
			onShare();
			break;

		case R.id.menu_item_set:
			onSet();
			break;

		case R.id.menu_item_similar:
			onSimilar();
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void onShare() {
		// Create a new Intent to send messages
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		// Add attributes to the intent
		// sendIntent.putExtra(Intent.EXTRA_EMAIL, mailto);
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(
				R.string.mail_subject));
		String body = String
				.format(getResources().getString(R.string.mail_body), mPhoto
						.getUrl(PhotoSize.MEDIUM));
		sendIntent.putExtra(Intent.EXTRA_TEXT, body);
		sendIntent.setType("message/rfc822");
		sendIntent.setType("text/plain");
		startActivityForResult(Intent.createChooser(sendIntent, getResources()
				.getString(R.string.dialog_share)), REQUEST_SENT_EMAIL);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.menu_back:
			onBack();
			break;

		case R.id.menu_next:
			onNext();
			break;

		case R.id.menu_prev:
			onPrev();
			break;

		case R.id.menu_more:
			onMore();
			break;
		/*
		 * case R.id.menu_set: onSet(); break;
		 */
		}
	}

	private void onSimilar() {
		Intent intent = new Intent();
		intent.putExtra(EXTRA_SEARCH_TAGS, mPhoto.getTags());
		setResult(SIMILAR_RESPONSE_ID, intent);
		finish();
	}

	private void onSet() {
		mTask = new CropWallpaperTask(getApplicationContext(),
				new CropWallpaperExecutorViewPhotoActivity()).execute(mPhoto);
	}

	private void onNext() {
		serviceContext.next();
		animateAndLoadPhotos(mNextAnimation);
	}

	private void onMore() {
		animateAndLoadPhotos(mNextAnimation);
	}

	private void onPrev() {
		serviceContext.prev();
		animateAndLoadPhotos(mBackAnimation);
	}

	private void onBack() {
		finish();
	}

	private void onFavorite() {
		if (photoExist) {
			photoDAO.delete(mPhoto);
			deleteFile(mPhoto.getId() + ".jpg");
			showMessage(R.string.photo_favorite_delete_successfully);
		} else {
			photoDAO.insert(mPhoto);
			showMessage(R.string.photo_favorite_save_successfully);

			Photo photo = photoDAO.selectByPhotoId(mPhoto.getPhotoId());
			if (photo != null) {
				StreamUtils
						.saveBitmap(this, ((BitmapDrawable) mPhotoView
								.getDrawable()).getBitmap(), photo.getId()
								+ ".jpg");
			}
		}
	}

	// /**
	// * If we successfully loaded a photo, send it to our future self to allow
	// * for fast display rotation. By doing so, we avoid reloading the photo
	// from
	// * the network when the activity is taken down and recreated upon display
	// * rotation.
	// *
	// * @return The Bitmap displayed in the ImageView, or null if the photo
	// * wasn't loaded.
	// */
	// @Override
	// public Object onRetainNonConfigurationInstance() {
	// return ((BitmapDrawable) mPhotoView.getDrawable()).getBitmap();
	// }

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		serviceContext = savedInstanceState.getParcelable(STATE_CONTEXT);
		mPhoto = savedInstanceState.getParcelable(STATE_PHOTO);
		refreshImage.setVisibility(savedInstanceState
				.getInt(STATE_SHOW_REFRESH));
	}

	@Override
	protected void onResume() {
		super.onResume();
		prepareMenu();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(STATE_CONTEXT, serviceContext);
		outState.putParcelable(STATE_PHOTO, mPhoto);
		outState.putInt(STATE_SHOW_REFRESH, refreshImage.getVisibility());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Spawns a new task to set the wallpaper in a background thread when/if
		// we receive a successful result from the image cropper.
		if (requestCode == CropWallpaperTask.REQUEST_CROP_IMAGE) {
			if (resultCode == RESULT_OK) {
				mTask = new SetWallpaperTask(this, new SetWallpaperExecutor())
						.execute();
			} else {
				cleanupWallpaper();
				showWallpaperError();
			}
		} else if (requestCode == REQUEST_SENT_EMAIL) {
			if (resultCode == RESULT_OK) {
				showMessage(R.string.share_successfully);

			} else if (resultCode != RESULT_CANCELED) {
				showMessage(R.string.share_fail);
			}
		}
	}

	private void showWallpaperError() {
		showMessage(R.string.error_cannot_save_file);
	}

	private void showWallpaperSuccess() {
		showMessage(R.string.success_wallpaper_set);
	}

	private void cleanupWallpaper() {
		deleteFile(CropWallpaperTask.WALLPAPER_FILE_NAME);
		mSwitcher.showNext();
	}

	private void showMessage(final int resId) {
		showMessage(resId, Toast.LENGTH_SHORT);
	}

	private void showMessage(final String message, final int duration) {
		handler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(ViewPhotoActivity.this, message, duration)
						.show();
			}

		});
	}

	private void showMessage(final int resId, final int duration) {
		handler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(ViewPhotoActivity.this, resId, duration).show();
			}

		});

	}

	private void showError() {
		handler.post(new Runnable() {

			@Override
			public void run() {
				refreshImage.setVisibility(View.VISIBLE);

				showMessage(R.string.notification_photos_not_loaded);
			}

		});
	}

	private void onSettings() {
		Intent intent = new Intent();
		intent.setClass(this, UserSettingsActivity.class);
		startActivity(intent);
	}

	/**
	 * Background task to load the photo from Flickr. The task loads the bitmap,
	 * then scale it to the appropriate dimension. The task ends by readjusting
	 * the activity's layout so that everything aligns correctly.
	 */
	private class LoadPhotoTask extends UserTask<Object, Void, Bitmap> {

		@Override
		public void onPreExecute() {
			super.onPreExecute();
			setLoading(true);
			handler.post(new Runnable() {

				@Override
				public void run() {
					setTitle(serviceContext.getScreenName());
					if (refreshImage.getVisibility() != View.INVISIBLE) {
						refreshImage.setVisibility(View.INVISIBLE);
					}
				}

			});
		}

		public Bitmap doInBackground(Object... params) {
			Bitmap bitmap = null;
			Photo photo = null;

			if (isFirstOpen) {
				photo = ((Photo) params[0]);
				isFirstOpen = false;
			} else {
				PhotoList photoList = null;
				try {
					photoList = ServiceManager.get().getService().execute(
							serviceContext);
				} catch (ServiceNetworkException e) {
					photoList = new PhotoList();
					showError();
				} catch (UserNotFoundException e) {
					photoList = new PhotoList();
					onSettings();
					showMessage(String.format(getResources().getString(
							R.string.unknown_username), userPreferences
							.getUserName()), Toast.LENGTH_LONG);
				}
				if (photoList != null && photoList.getCount() != 0) {
					photo = photoList.get(0);
					mPhoto = photo;
				}
			}

			if (photo != null) {
				bitmap = ServiceManager.get().getService().loadPhotoBitmap(
						photo, PhotoSize.MEDIUM);
			}
			if (bitmap == null) {
				bitmap = BitmapFactory.decodeResource(getResources(),
						R.drawable.not_found);
				showMessage(R.string.notification_photo_not_loaded);
			}

			final int width = (Integer) params[1];
			final int height = (Integer) params[2];

			final Bitmap framed = ImageUtilities.scaleAndFrame(bitmap, width,
					height);
			bitmap.recycle();

			return framed;
		}

		@Override
		public void onPostExecute(Bitmap result) {
			setPhoto(result);

			prepareMenu();
			mTask = null;

			mSwitcher.showNext();

			mContainer.startAnimation(AnimationUtils.loadAnimation(
					ViewPhotoActivity.this, R.anim.fade_in));
			mContainer.setVisibility(View.VISIBLE);
			setLoading(false);
		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (currentOrientation != newConfig.orientation) {
			currentOrientation = newConfig.orientation;
			if (mPhotoView.getDrawable() != null) {
				mBitmap = ((BitmapDrawable) mPhotoView.getDrawable())
						.getBitmap();
			}
			restoreBitmap = mBitmap != null;
			initActivity();
		}
	}

	private void setPhoto(Bitmap result) {
		mPhotoView.setImageBitmap(result);

		if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
			// Find by how many pixels the title and date must be shifted on the
			// horizontal axis to be left aligned with the photo
			int offsetX = (mPhotoView.getMeasuredWidth() - result.getWidth()) / 2;
			setPhotoData(result, offsetX);
		} else {
			setPhotoData(result, 0);
		}
	}

	private void setPhotoData(Bitmap result, int offsetX) {
		// Forces the ImageView to have the same size as its embedded bitmap
		// This will remove the empty space between the title/date pair and
		// the photo itself
		LinearLayout.LayoutParams params;
		params = (LinearLayout.LayoutParams) mPhotoView.getLayoutParams();
		params.height = result.getHeight();
		params.weight = 0.0f;
		mPhotoView.setLayoutParams(params);

		handleTextViewPortrait(mPhoto.getDate(), mPhotoDate, offsetX,
				R.string.view_photo_date_taken_format);

		handleTextViewPortrait(mPhoto.getTags(), mPhotoTags, offsetX,
				R.string.view_photo_tags_format);

		UserInfo userInfo = mPhoto.getUserInfo();
		String location = userInfo != null ? userInfo.getLocation() : null;
		handleTextViewPortrait(location, mPhotoLocation, offsetX,
				R.string.view_photo_owner_location_format);
	}

	private void handleTextViewPortrait(String viewText, TextView textView,
			int offsetX, int resId) {
		if (viewText != null && !"".equals(viewText)) {
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textView
					.getLayoutParams();
			params.leftMargin = offsetX;
			textView.setLayoutParams(params);
			String format = ViewPhotoActivity.this.getResources().getString(
					resId);
			textView.setText(String.format(format, viewText));
			textView.setVisibility(View.VISIBLE);
		} else {
			textView.setVisibility(View.GONE);
		}
	}

	// private void handleTextViewLanscape(String viewText, TextView textView,
	// int resId) {
	// if (viewText != null && !"".equals(viewText)) {
	// LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textView
	// .getLayoutParams();
	// textView.setLayoutParams(params);
	// String format = ViewPhotoActivity.this.getResources()
	// .getString(resId);
	// textView.setText(String.format(format, viewText));
	// textView.setVisibility(View.VISIBLE);
	// } else {
	// textView.setVisibility(View.GONE);
	// }
	// }

	private class CropWallpaperExecutorViewPhotoActivity implements
			CropWallpaperExecutor {

		@Override
		public void onPreExecute() {
			mSwitcher.showNext();
		}

		@Override
		public void onPostExecuteError() {
			cleanupWallpaper();
			showWallpaperError();
		}

		@Override
		public void onPostExecuteFinish() {
			mTask = null;
		}

		@Override
		public void onPostExecuteSuccess(final Intent intent) {
			ViewPhotoActivity.this.mTask = new SetWallpaperTask(
					ViewPhotoActivity.this, new SetWallpaperExecutor())
					.execute();
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
			showWallpaperError();
		}

		public void onShowWallpaperSuccess() {
			showWallpaperSuccess();
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		mContainer.setLayoutAnimationListener(null);
		mContainer.setLayoutAnimation(null);
		mContainer.removeAllViews();
		loadPhoto();
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	@Override
	public void onAnimationStart(Animation animation) {
	}

	private void animateAndLoadPhotos(LayoutAnimationController animation) {
		mSwitcher.showNext();
		// mContainer.setLayoutAnimationListener(this);
		// mContainer.setLayoutAnimation(animation);
		mContainer.invalidate();
		loadPhoto();
	}

	private synchronized void setLoading(boolean value) {
		loading = value;
	}

	private synchronized boolean isLoading() {
		return loading;
	}

	private void showProgress() {
		mSwitcher.setDisplayedChild(1);
	}
}
