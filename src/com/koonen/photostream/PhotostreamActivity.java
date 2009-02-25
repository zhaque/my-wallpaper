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

import java.io.File;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.koonen.photostream.api.FilePhoto;
import com.koonen.photostream.api.Photo;
import com.koonen.photostream.api.PhotoList;
import com.koonen.photostream.api.PhotoSize;
import com.koonen.photostream.api.ServiceContext;
import com.koonen.photostream.api.ServiceManager;
import com.koonen.photostream.api.ServiceNetworkException;
import com.koonen.photostream.api.Type;
import com.koonen.photostream.api.UserNotFoundException;
import com.koonen.photostream.dao.Category;
import com.koonen.photostream.effects.EffectsApplier;
import com.koonen.photostream.settings.UserPreferences;
import com.koonen.photostream.settings.UserSettingsActivity;
import com.koonen.utils.ConfigurationReader;

/**
 * Activity used to display a Flickr user's photostream. This activity shows a
 * fixed number of photos at a time. The activity is invoked either by
 * LoginActivity, when the application is launched normally, or by a Home
 * shortcut, or by an Intent with the view action and a flickr://photos/nsid
 * URI.
 */
public class PhotostreamActivity extends Activity implements
		View.OnClickListener, Animation.AnimationListener {

	// private static final String TAG = PhotostreamActivity.class
	// .getCanonicalName();

	static final String ACTION = "com.google.android.photostream.FLICKR_STREAM";

	static final String EXTRA_NOTIFICATION = "com.google.android.photostream.extra_notify_id";
	static final String EXTRA_NSID = "com.google.android.photostream.extra_nsid";
	static final String EXTRA_USER = "com.google.android.photostream.extra_user";

	private static final String STATE_CONTEXT = "requeset_context";

	private static final String STATE_SHOW_SPLASH = "show_splash";
	private static final String STATE_SHOW_REFRESH = "show_refresh";

	private static final int MENU_ITEM_SEARCH = 1;
	private static final int MENU_ITEM_CATEGORIES = 2;
	private static final int MENU_ITEM_MY_FAVORITES = 3;
	private static final int MENU_ITEM_TAKE_PICTURE = 4;
	private static final int MENU_ITEM_PERSONAL = 5;
	private static final int MENU_ITEM_SETTINGS = 6;

	private static final int DIALOG_SEARCH = 1;
	private static final int DIALOG_TAKE_PHOTO = 3;

	private ServiceContext serviceContext;

	private LayoutInflater mInflater;

	private ViewAnimator mSwitcher;
	private View mMenuNext;
	private View mMenuBack;
	private View mMenuSeparator;
	private View mMenuMore;
	private GridLayout mGrid;
	private ImageView refreshImage;

	private boolean showSplash = true;

	private LayoutAnimationController mNextAnimation;
	private LayoutAnimationController mBackAnimation;

	private UserTask<?, ?, ?> mTask;

	private UserPreferences userPreferences;

	private Handler handler;

	@Override
	protected void onResume() {
		super.onResume();
		Eula.showEula(this, new Runnable() {

			@Override
			public void run() {
				showSplash();
			}

		});
		prepareMenu();
	}

	private void showSplash() {
		if (showSplash) {
			showSplash = false;
			final ImageView splash = (ImageView) findViewById(R.id.splash_image);
			final GridLayout grid = (GridLayout) findViewById(R.id.grid_photos);
			final ViewAnimator switcher = (ViewAnimator) findViewById(R.id.switcher_menu);

			grid.setVisibility(View.GONE);
			switcher.setVisibility(View.GONE);

			Configuration config = new ConfigurationReader(this)
					.getConfiguration();

			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			Bitmap scaledBm = null;
			int resId;

			if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
				resId = R.drawable.splash_320x240;
			} else {
				resId = R.drawable.splash;
			}
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
			scaledBm = Bitmap.createScaledBitmap(bitmap, dm.widthPixels,
					dm.heightPixels, true);
			bitmap.recycle();
			splash.setImageBitmap(scaledBm);
			splash.setVisibility(View.VISIBLE);
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					splash.setVisibility(View.GONE);
					grid.setVisibility(View.VISIBLE);
					switcher.setVisibility(View.VISIBLE);
				}

			}, 2000);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ViewPhotoActivity.VIEW_PHOTO_REQUEST_ID) {
			if (resultCode == ViewPhotoActivity.SIMILAR_RESPONSE_ID) {
				String searchTags = data
						.getStringExtra(ViewPhotoActivity.EXTRA_SEARCH_TAGS);
				this.serviceContext = ServiceContext.createSearchContext(
						this.userPreferences.getImagesPerRequest(), searchTags);
				animateAndLoadPhotos(mNextAnimation);
			}
		} else if (requestCode == ActivityConstants.REQUEST_ID_CATEGORY_SEARCH) {
			if (resultCode == ActivityConstants.RESULT_CATEGORY_SEARCH) {
				Bundle extras = data.getExtras();
				if (extras != null) {
					Category category = extras
							.getParcelable(CategoryEditActivity.EXTRA_CATEGORY);
					this.serviceContext = ServiceContext.createCategoryContext(
							category, this.userPreferences
									.getImagesPerRequest());
					animateAndLoadPhotos(mNextAnimation);
				}
			}
		} else if (requestCode == CameraPreviewActivity.REQUEST_SHOW_FILE_SYSTEM_PHOTO) {
			if (resultCode == CameraPreviewActivity.RESPONSE_FILE_SYSTEM_PHOTO) {
				File file = getFileStreamPath(CameraPreviewActivity.PHOTO_FROM_CAMERA);

				FilePhoto photo = new FilePhoto(file.toURI().toString());
				ViewPhotoActivity.show(this, photo);
			}
		} else if (requestCode == FileBrowserActivity.REQUEST_SHOW_FILE_SYSTEM_PHOTO) {
			if (resultCode == ActivityConstants.SIMILAR_RESPONSE_ID) {
				String filePath = data
						.getStringExtra(FileBrowserActivity.EXTRA_FILE_PATH);
				FilePhoto photo = new FilePhoto(filePath);
				ViewPhotoActivity.show(this, photo);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new Handler();
		ServiceManager.init(this);
		userPreferences = new UserPreferences(this);

		if (serviceContext == null) {
			if (userPreferences.getGroup() == "") {
				serviceContext = ServiceContext
						.createRecentContext(userPreferences
								.getImagesPerRequest());
			} else {
				serviceContext = ServiceContext.createSearchContext(
						userPreferences.getImagesPerRequest(), "");
			}

		}

		initService();

		clearNotification();
		setContentView(R.layout.screen_photostream);
		setupViews();
		loadPhotos();
	}

	private void initService() {
		if (ServiceConnector.getInstance() == null) {
			ServiceConnector.init(this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_ITEM_MY_FAVORITES, 0, R.string.menu_my_favorites)
				.setIcon(R.drawable.ic_menu_favorite);
		menu.add(0, MENU_ITEM_CATEGORIES, 1, R.string.menu_categories).setIcon(
				R.drawable.ic_menu_my_tags);
		menu.add(0, MENU_ITEM_TAKE_PICTURE, 2, R.string.menu_take_picture)
				.setIcon(R.drawable.ic_menu_photo);

		menu.add(0, MENU_ITEM_SEARCH, 3, R.string.menu_search).setIcon(
				R.drawable.ic_menu_search);
		menu.add(0, MENU_ITEM_PERSONAL, 4, R.string.menu_personal).setIcon(
				R.drawable.ic_menu_my_network);
		menu.add(0, MENU_ITEM_SETTINGS, 5, R.string.menu_settings).setIcon(
				R.drawable.ic_menu_settings);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(MENU_ITEM_PERSONAL);
		item.setEnabled(!"".equals(userPreferences.getUserName()));
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_SEARCH:
			showDialog(DIALOG_SEARCH);
			return true;

		case MENU_ITEM_PERSONAL:
			serviceContext = ServiceContext
					.createPersonalContext(userPreferences
							.getImagesPerRequest());
			animateAndLoadPhotos(mNextAnimation);
			return true;

		case MENU_ITEM_MY_FAVORITES:
			serviceContext = ServiceContext
					.createFavoritesServiceContext(userPreferences
							.getImagesPerRequest());
			animateAndLoadPhotos(mNextAnimation);
			return true;

		case MENU_ITEM_TAKE_PICTURE:
			showDialog(DIALOG_TAKE_PHOTO);
			return true;

		case MENU_ITEM_CATEGORIES:
			CategoryActivity.show(this);
			return true;

		case MENU_ITEM_SETTINGS:
			onSettings();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void onSettings() {
		Intent intent = new Intent();
		intent.setClass(this, UserSettingsActivity.class);
		startActivity(intent);
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DIALOG_SEARCH: {
			LinearLayout layout = new LinearLayout(this);
			TextView view = new TextView(this);
			view.setText(" ");
			layout.addView(view);
			final EditText searchEditText = new EditText(this);
			searchEditText.setLayoutParams(new LayoutParams(210,
					LayoutParams.WRAP_CONTENT));
			searchEditText.setPadding(5, 0, 0, 0);
			searchEditText.setSingleLine();
			Button searchButton = new Button(this);
			searchButton.setText(R.string.search_button_label);
			layout.addView(searchEditText);
			layout.addView(searchButton);

			final AlertDialog dialog = new AlertDialog.Builder(
					PhotostreamActivity.this).setTitle(R.string.dialog_search)
					.setView(layout).create();
			dialog.getWindow().setGravity(Gravity.TOP);
			dialog.setIcon(R.drawable.ic_menu_search);

			searchButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String query = searchEditText.getText().toString();

					if (query == null || "".equals(query.trim())) {
						showMessage(R.string.search_query_not_empty);
						return;
					}
					serviceContext = ServiceContext.createSearchContext(
							userPreferences.getImagesPerRequest(), query);
					dialog.dismiss();
					animateAndLoadPhotos(mNextAnimation);
				}

			});

			return dialog;
		}

		case DIALOG_TAKE_PHOTO: {
			final UserPreferences preferences = new UserPreferences(this);

			return new AlertDialog.Builder(PhotostreamActivity.this).setTitle(
					R.string.dialog_take_photo).setItems(
					R.array.take_photo_menu_items,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case 0:
								onCameraPreview();
								break;
							case 1:
								serviceContext = ServiceContext
										.createInternalContext(preferences
												.getImagesPerRequest());
								animateAndLoadPhotos(mNextAnimation);
								// FileBrowserActivity
								// .show(PhotostreamActivity.this);
								break;
							case 2:
								serviceContext = ServiceContext
										.createExternalContext(preferences
												.getImagesPerRequest());
								animateAndLoadPhotos(mNextAnimation);
								// FileBrowserActivity
								// .show(PhotostreamActivity.this);
								break;
							default:
								dialog.dismiss();
							}
						}
					}).create();
		}

		}

		return null;
	}

	private void clearNotification() {
		final int notification = getIntent()
				.getIntExtra(EXTRA_NOTIFICATION, -1);
		if (notification != -1) {
			NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			manager.cancel(notification);
		}
	}

	private void setupViews() {
		mInflater = LayoutInflater.from(PhotostreamActivity.this);
		mNextAnimation = AnimationUtils.loadLayoutAnimation(this,
				R.anim.layout_slide_next);
		mBackAnimation = AnimationUtils.loadLayoutAnimation(this,
				R.anim.layout_slide_back);

		mSwitcher = (ViewAnimator) findViewById(R.id.switcher_menu);
		mMenuNext = findViewById(R.id.menu_next);
		mMenuBack = findViewById(R.id.menu_back);
		mMenuSeparator = findViewById(R.id.menu_separator);
		mMenuMore = findViewById(R.id.menu_more);
		mGrid = (GridLayout) findViewById(R.id.grid_photos);

		mMenuNext.setOnClickListener(this);
		mMenuBack.setOnClickListener(this);
		mMenuMore.setOnClickListener(this);

		mGrid.setClipToPadding(false);

		refreshImage = (ImageView) findViewById(R.id.refresh_image);
		refreshImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				refreshImage.setVisibility(View.INVISIBLE);
				animateAndLoadPhotos(mNextAnimation);
			}

		});
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(STATE_CONTEXT, serviceContext);
		outState.putBoolean(STATE_SHOW_SPLASH, false);
		outState.putInt(STATE_SHOW_REFRESH, refreshImage.getVisibility());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		serviceContext = savedInstanceState.getParcelable(STATE_CONTEXT);
		showSplash = savedInstanceState.getBoolean(STATE_SHOW_SPLASH);
		refreshImage.setVisibility(savedInstanceState
				.getInt(STATE_SHOW_REFRESH));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mTask != null && mTask.getStatus() == UserTask.Status.RUNNING) {
			mTask.cancel(true);
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.menu_next:
			onNext();
			break;
		case R.id.menu_back:
			onBack();
			break;
		case R.id.menu_more:
			onMore();
			break;
		default:
			int photoNumber = mGrid.indexOfChild(v);
			onShowPhoto((Photo) v.getTag(), photoNumber);
			break;
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		final GridLayout grid = mGrid;
		final int count = grid.getChildCount();
		final LoadedPhoto[] list = new LoadedPhoto[count];

		for (int i = 0; i < count; i++) {
			final ImageView v = (ImageView) grid.getChildAt(i);
			list[i] = new LoadedPhoto(((BitmapDrawable) v.getDrawable())
					.getBitmap(), (Photo) v.getTag());
		}

		return list;
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

		mMenuBack.setVisibility(backVisible ? View.VISIBLE : View.GONE);
		mMenuNext.setVisibility(nextVisible ? View.VISIBLE : View.GONE);
		mMenuSeparator.setVisibility(backVisible && nextVisible ? View.VISIBLE
				: View.GONE);
		mMenuMore.setVisibility(moreVisible ? View.VISIBLE : View.GONE);
	}

	private void loadPhotos() {
		final Object data = getLastNonConfigurationInstance();
		if (data == null) {
			mTask = new GetPhotoListTask().execute();
		} else {
			EffectsApplier effectsApplier = new EffectsApplier(mGrid,
					userPreferences);
			final LoadedPhoto[] photos = (LoadedPhoto[]) data;
			for (LoadedPhoto photo : photos) {
				addPhoto(effectsApplier, photo);
			}
			prepareMenu();
			mSwitcher.showNext();
		}
	}

	private void showPhotos(PhotoList photos) {
		mTask = new LoadPhotosTask().execute(photos);
	}

	private void onShowPhoto(Photo photo, int photoNumber) {
		ViewPhotoActivity.show(this, photo, serviceContext, photoNumber);
	}

	private void onCameraPreview() {
		CameraPreviewActivity.show(this);
	}

	private void onNext() {
		serviceContext.next();
		animateAndLoadPhotos(mNextAnimation);
	}

	private void onBack() {
		serviceContext.prev();
		animateAndLoadPhotos(mBackAnimation);
	}

	private void onMore() {
		animateAndLoadPhotos(mNextAnimation);
	}

	private void animateAndLoadPhotos(LayoutAnimationController animation) {
		mSwitcher.showNext();
		mGrid.setLayoutAnimationListener(this);
		mGrid.setLayoutAnimation(animation);
		mGrid.invalidate();
	}

	public void onAnimationEnd(Animation animation) {
		mGrid.setLayoutAnimationListener(null);
		mGrid.setLayoutAnimation(null);
		mGrid.removeAllViews();
		loadPhotos();
	}

	public void onAnimationStart(Animation animation) {
	}

	public void onAnimationRepeat(Animation animation) {
	}

	private void addPhoto(EffectsApplier applier, LoadedPhoto... value) {
		ImageView image = (ImageView) mInflater.inflate(
				R.layout.grid_item_photo, mGrid, false);
		applier.applyEffects(image, value[0].mBitmap);
		image.setTag(value[0].mPhoto);
		image.setOnClickListener(PhotostreamActivity.this);
		try {
			mGrid.addView(image);
		} catch (Exception exception) {
			// TODO: check this point
			exception.getMessage();
		}
	}

	/**
	 * Background task used to load each individual photo. The task loads each
	 * photo in order and publishes each loaded Bitmap as a progress unit. The
	 * tasks ends by hiding the progress bar and showing the menu.
	 */
	private class LoadPhotosTask extends
			UserTask<PhotoList, LoadedPhoto, PhotoList> {
		private final Random mRandom;

		private final EffectsApplier effectsApplier;

		private LoadPhotosTask() {
			mRandom = new Random();
			effectsApplier = new EffectsApplier(mGrid, userPreferences);
		}

		@Override
		public void onPreExecute() {
			mGrid.removeAllViews();
		}

		public PhotoList doInBackground(PhotoList... params) {
			final PhotoList list = params[0];
			final int count = list.getCount();

			for (int i = 0; i < count; i++) {
				if (isCancelled())
					break;

				final Photo photo = list.get(i);
				Bitmap bitmap = ServiceManager.get().getService()
						.loadPhotoBitmap(photo, PhotoSize.THUMBNAIL);
				if (!isCancelled()) {
					if (bitmap == null) {
						final boolean portrait = mRandom.nextFloat() >= 0.5f;
						bitmap = BitmapFactory.decodeResource(getResources(),
								portrait ? R.drawable.not_found_small_1
										: R.drawable.not_found_small_2);
					} else {
						if (photo.isScaled()) {
							// double k = 100.0 / bitmap.getWidth();
							// int height = (int) (k * bitmap.getHeight());
							// bitmap = Bitmap.createScaledBitmap(bitmap, 100,
							// height, true);
							final Bitmap framed = ImageUtilities.scaleAndFrame(
									bitmap, 100, 100);
							bitmap.recycle();
							bitmap = framed;
						}
					}
					publishProgress(new LoadedPhoto(ImageUtilities
							.rotateAndFrame(bitmap), photo));
					bitmap.recycle();
				}
			}

			return list;
		}

		/**
		 * Whenever a photo's Bitmap is loaded from the background thread, it is
		 * displayed in this method by adding a new ImageView in the photos
		 * grid. Each ImageView's tag contains the
		 * {@link com.koonen.photostream.Flickr.Photo} it was loaded from.
		 * 
		 * @param value
		 *            The photo and its bitmap.
		 */
		@Override
		public void onProgressUpdate(LoadedPhoto... value) {
			addPhoto(effectsApplier, value);
		}

		@Override
		public void onPostExecute(PhotoList result) {
			if (serviceContext.getType() == Type.FAVORITES
					&& result.getCount() == 0) {
				showMessage(R.string.no_favorites_photo);
			}
			prepareMenu();
			mSwitcher.showNext();
			mTask = null;
		}
	}

	private void showMessage(int resId) {
		Toast.makeText(PhotostreamActivity.this, resId, Toast.LENGTH_SHORT)
				.show();
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

	/**
	 * Background task used to load the list of photos. The tasks queries Flickr
	 * for the list of photos to display and ends by starting the
	 * LoadPhotosTask.
	 */
	private class GetPhotoListTask extends UserTask<Integer, Void, PhotoList> {

		@Override
		public void onPreExecute() {
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

		public PhotoList doInBackground(Integer... params) {
			PhotoList photos;
			try {
				photos = ServiceManager.get().getService().execute(
						serviceContext);
			} catch (ServiceNetworkException e) {
				photos = new PhotoList();
				showError();
			} catch (UserNotFoundException e) {
				photos = new PhotoList();
				handler.post(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(
								PhotostreamActivity.this,
								String.format(getResources().getString(
										R.string.unknown_username),
										userPreferences.getUserName()),
								Toast.LENGTH_LONG).show();
					}

				});

				onSettings();
			}

			return photos;
		}

		@Override
		public void onPostExecute(PhotoList photoList) {
			showPhotos(photoList);
			mTask = null;
		}
	}

	/**
	 * A LoadedPhoto contains the Flickr photo and the Bitmap loaded for that
	 * photo.
	 */
	private static class LoadedPhoto {
		Bitmap mBitmap;
		Photo mPhoto;

		LoadedPhoto(Bitmap bitmap, Photo photo) {
			mBitmap = bitmap;
			mPhoto = photo;
		}
	}

	// private static void showFiles(Activity context, ServiceContext
	// serviceContext) {
	// final Intent intent = new Intent(context, PhotostreamActivity.class);
	// intent.putExtra(STATE_CONTEXT, serviceContext);
	// intent.putExtra(STATE_SHOW_SPLASH, false);
	// context.startActivityForResult(intent, FILE_S);
	// }
	//	
	// private static void showInternalFiles(Activity context) {
	// showFiles(context, ServiceContext.createInternalContext(pageSize));
	// }
}
