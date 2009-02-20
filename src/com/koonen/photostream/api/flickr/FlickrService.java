package com.koonen.photostream.api.flickr;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.util.Xml;
import android.view.InflateException;

import com.koonen.photostream.api.IPhotoService;
import com.koonen.photostream.api.Photo;
import com.koonen.photostream.api.PhotoList;
import com.koonen.photostream.api.PhotoSize;
import com.koonen.photostream.api.ResponseHandler;
import com.koonen.photostream.api.ResponseParser;
import com.koonen.photostream.api.ServiceContext;
import com.koonen.photostream.api.ServiceNetworkException;
import com.koonen.photostream.api.SourceType;
import com.koonen.photostream.api.User;
import com.koonen.photostream.api.UserInfo;
import com.koonen.photostream.api.UserNotFoundException;
import com.koonen.photostream.dao.ImageDAO;
import com.koonen.photostream.dao.PhotoDAO;
import com.koonen.photostream.settings.UserPreferences;
import com.koonen.utils.StreamUtils;

/**
 * 
 * @author dryganets
 * 
 */
public class FlickrService implements IPhotoService, FlickrConstants {

	static final String LOG_TAG = "Photostream";

	private static final int RECONNECT_COUNT = 3;

	private String userId;

	private UserPreferences userPreferences;
	private PhotoDAO photoDAO;
	private ImageDAO imageDAO;

	private HttpClient client;

	public FlickrService(UserPreferences userPreferences, PhotoDAO photoDAO,
			ImageDAO imageDAO) {
		this.userPreferences = userPreferences;
		this.photoDAO = photoDAO;
		this.imageDAO = imageDAO;
		userPreferences
				.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {

					@Override
					public void onSharedPreferenceChanged(
							SharedPreferences sharedPreferences, String key) {
						if (key.equals(UserPreferences.NETWORK_USER_NAME_KEY)) {
							userId = null;
						}

					}

				});
	}

	private HttpClient getHttpClient() {
		if (client == null) {
			final HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, "UTF-8");

			final SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));

			final ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(
					params, registry);

			client = new DefaultHttpClient(manager, params);
		}
		return client;
	}

	private String getUserId() throws UserNotFoundException {
		if (userId == null) {
			User user = findByUserName(userPreferences.getUserName());
			if (user != null) {
				userId = user.getId();
			} else {
				throw new UserNotFoundException(userPreferences.getUserName());
			}

		}
		return userId;
	}

	/**
	 * Builds an HTTP GET request for the specified Flickr API method. The
	 * returned request contains the web service path, the query parameter for
	 * the API KEY and the query parameter for the specified method.
	 * 
	 * @param method
	 *            The Flickr API method to invoke.
	 * 
	 * @return A Uri.Builder containing the GET path, the API key and the method
	 *         already encoded.
	 */
	private static Uri.Builder buildGetMethod(String method) {
		final Uri.Builder builder = new Uri.Builder();
		builder.path(API_REST_URL).appendQueryParameter(PARAM_API_KEY, API_KEY);
		builder.appendQueryParameter(PARAM_METHOD, method);
		return builder;
	}

	User findByUserName(String userName) {
		final Uri.Builder uri = buildGetMethod(API_PEOPLE_FIND_BY_USERNAME);
		uri.appendQueryParameter(PARAM_USERNAME, userName);

		final HttpGet get = new HttpGet(uri.build().toString());
		final String[] userId = new String[1];

		try {
			executeRequest(get, new ResponseHandler() {
				public void handleResponse(InputStream in) throws IOException {
					parseResponse(in, new ResponseParser() {
						public void parseResponse(XmlPullParser parser)
								throws XmlPullParserException, IOException {
							parseUser(parser, userId);
						}
					});
				}
			});

			if (userId[0] != null) {
				return new User(userId[0]);
			}
		} catch (IOException e) {
			android.util.Log.e(LOG_TAG, "Could not find the user with name: "
					+ userName);
		}

		return null;
	}

	private String getFrob() {
		Uri.Builder uri = buildGetMethod(API_AUTHS_GET_FROB);
		HttpGet get = new HttpGet(uri.build().toString());
		final String[] frob = new String[1];

		try {
			executeRequest(get, new ResponseHandler() {
				public void handleResponse(InputStream in) throws IOException {
					parseResponse(in, new ResponseParser() {
						public void parseResponse(XmlPullParser parser)
								throws XmlPullParserException, IOException {
							parseFrob(parser, frob);
						}
					});
				}
			});

			if (frob[0] != null) {
				return frob[0];
			}
		} catch (IOException e) {
			android.util.Log.e(LOG_TAG, "Could not return frob ");
		}

		return null;
	}

	private Auth getToken(String frob) {
		Uri.Builder uri = buildGetMethod(API_AUTHS_GET_TOKEN);
		uri.appendQueryParameter(PARAM_AUTH_FROB, frob);
		HttpGet get = new HttpGet(uri.build().toString());
		final Auth auth = new Auth();

		try {
			executeRequest(get, new ResponseHandler() {
				public void handleResponse(InputStream in) throws IOException {
					parseResponse(in, new ResponseParser() {
						public void parseResponse(XmlPullParser parser)
								throws XmlPullParserException, IOException {
							parseToken(parser, auth);
						}
					});
				}
			});

			return auth;
		} catch (IOException e) {
			android.util.Log.e(LOG_TAG, "Could not load token ");
		}

		return null;
	}

	private Auth checkToken(String token) {
		Uri.Builder uri = buildGetMethod(API_AUTHS_CHECK_TOKEN);
		uri.appendQueryParameter(PARAM_AUTH_TOKEN, token);
		HttpGet get = new HttpGet(uri.build().toString());
		final Auth auth = new Auth();

		try {
			executeRequest(get, new ResponseHandler() {
				public void handleResponse(InputStream in) throws IOException {
					parseResponse(in, new ResponseParser() {
						public void parseResponse(XmlPullParser parser)
								throws XmlPullParserException, IOException {
							parseToken(parser, auth);
						}
					});
				}
			});

			return auth;
		} catch (IOException e) {
			android.util.Log.e(LOG_TAG, "Could not check token ");
		}

		return null;
	}

	private Auth getFullToken(String miniToken) {
		Uri.Builder uri = buildGetMethod(API_AUTHS_CHECK_TOKEN);
		uri.appendQueryParameter(PARAM_AUTH_MINI_TOKEN, miniToken);
		HttpGet get = new HttpGet(uri.build().toString());
		final Auth auth = new Auth();

		try {
			executeRequest(get, new ResponseHandler() {
				public void handleResponse(InputStream in) throws IOException {
					parseResponse(in, new ResponseParser() {
						public void parseResponse(XmlPullParser parser)
								throws XmlPullParserException, IOException {
							parseToken(parser, auth);
						}
					});
				}
			});

			return auth;
		} catch (IOException e) {
			android.util.Log.e(LOG_TAG, "Could not return frob ");
		}

		return null;
	}

	private void parseUserInfo(XmlPullParser parser, UserInfo info)
			throws XmlPullParserException, IOException {
		int type;
		String name;
		final int depth = parser.getDepth();

		while (((type = parser.next()) != XmlPullParser.END_TAG || parser
				.getDepth() > depth)
				&& type != XmlPullParser.END_DOCUMENT) {
			if (type != XmlPullParser.START_TAG) {
				continue;
			}

			name = parser.getName();
			if (RESPONSE_TAG_LOCATION.equals(name)) {
				if (parser.next() == XmlPullParser.TEXT) {
					info.setLocation(parser.getText());
				}
			}
		}
	}

	/**
	 * Retrieves a public set of information about the specified user. The user
	 * can either be
	 * {@link com.google.android.photostream.Flickr.User#fromId(String) created
	 * manually} or {@link #findByUserName(String) obtained from a user name}.
	 * 
	 * @param user
	 *            The user, whose NSID is valid, to retrive public information
	 *            for.
	 * 
	 * @return An instance of
	 *         {@link com.google.android.photostream.Flickr.UserInfo} or null if
	 *         the user could not be found.
	 * 
	 * @see com.google.android.photostream.Flickr.UserInfo
	 * @see com.google.android.photostream.Flickr.User
	 * @see #findByUserName(String)
	 */
	public UserInfo getUserInfo(User user) {
		final String nsid = user.getId();
		final Uri.Builder uri = buildGetMethod(API_PEOPLE_GET_INFO);
		uri.appendQueryParameter(PARAM_USERID, nsid);

		final HttpGet get = new HttpGet(uri.build().toString());

		try {
			final UserInfo info = new UserInfo(user);

			executeRequest(get, new ResponseHandler() {
				public void handleResponse(InputStream in) throws IOException {
					parseResponse(in, new ResponseParser() {
						public void parseResponse(XmlPullParser parser)
								throws XmlPullParserException, IOException {
							parseUserInfo(parser, info);
						}
					});
				}
			});

			return info;
		} catch (IOException e) {
			android.util.Log.e(LOG_TAG, "Could not find the user with id: "
					+ nsid);
		}

		return null;
	}

	/**
	 * Parses a valid Flickr XML response from the specified input stream. When
	 * the Flickr response contains the OK tag, the response is sent to the
	 * specified response parser.
	 * 
	 * @param in
	 *            The input stream containing the response sent by Flickr.
	 * @param responseParser
	 *            The parser to use when the response is valid.
	 * 
	 * @throws IOException
	 */
	private void parseResponse(InputStream in, ResponseParser responseParser)
			throws IOException {
		final XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setInput(new InputStreamReader(in));

			int type;
			while ((type = parser.next()) != XmlPullParser.START_TAG
					&& type != XmlPullParser.END_DOCUMENT) {
				// Empty
			}

			if (type != XmlPullParser.START_TAG) {
				throw new InflateException(parser.getPositionDescription()
						+ ": No start tag found!");
			}

			String name = parser.getName();
			if (RESPONSE_TAG_RSP.equals(name)) {
				final String value = parser.getAttributeValue(null,
						RESPONSE_ATTR_STAT);
				if (!RESPONSE_STATUS_OK.equals(value)) {
					throw new IOException("Wrong status: " + value);
				}
			}

			responseParser.parseResponse(parser);

		} catch (XmlPullParserException e) {
			final IOException ioe = new IOException(
					"Could not parser the response");
			ioe.initCause(e);
			throw ioe;
		}
	}

	private void parseFrob(XmlPullParser parser, String[] frob)
			throws XmlPullParserException, IOException {
		int type;
		String name = null;
		final int depth = parser.getDepth();
		boolean tagHanled = false;

		while (((type = parser.next()) != XmlPullParser.END_TAG || parser
				.getDepth() > depth)
				&& type != XmlPullParser.END_DOCUMENT) {
			if (type == XmlPullParser.START_TAG) {
				name = parser.getName();
				if (RESPONSE_TAG_FROB.equals(name)) {
					tagHanled = true;
				}
			} else if (type == XmlPullParser.TEXT) {
				if (tagHanled) {
					if (RESPONSE_TAG_FROB.equals(name)) {
						frob[0] = parser.getText();
					}
					tagHanled = false;
				}
			}
		}
	}

	private void parseToken(XmlPullParser parser, Auth auth)
			throws XmlPullParserException, IOException {
		int type;
		String name = null;
		final int depth = parser.getDepth();
		boolean tagHanled = false;

		while (((type = parser.next()) != XmlPullParser.END_TAG || parser
				.getDepth() > depth)
				&& type != XmlPullParser.END_DOCUMENT) {
			if (type == XmlPullParser.START_TAG) {
				name = parser.getName();
				if (RESPONSE_TAG_TOKEN.equals(name)
						|| RESPONSE_TAG_PERMS.equals(name)) {
					tagHanled = true;
				} else if (RESPONSE_TAG_USER.equals(name)) {
					auth.setUser(new User(parser.getAttributeValue(null,
							RESPONSE_ATTR_NSID)));
				}
			} else if (type == XmlPullParser.TEXT) {
				if (tagHanled) {
					if (RESPONSE_TAG_TOKEN.equals(name)) {
						auth.setToken(parser.getText());
					} else if (RESPONSE_TAG_PERMS.equals(name)) {
						auth.setPerms(Perms.valueOf(parser.getText()));
					}
					tagHanled = false;
				}
			}
		}
	}

	private void parseUser(XmlPullParser parser, String[] userId)
			throws XmlPullParserException, IOException {
		int type;
		String name;
		final int depth = parser.getDepth();

		while (((type = parser.next()) != XmlPullParser.END_TAG || parser
				.getDepth() > depth)
				&& type != XmlPullParser.END_DOCUMENT) {
			if (type != XmlPullParser.START_TAG) {
				continue;
			}

			name = parser.getName();
			if (RESPONSE_TAG_USER.equals(name)) {
				userId[0] = parser.getAttributeValue(null, RESPONSE_ATTR_NSID);
			}
		}
	}

	private void parsePhotos(XmlPullParser parser, PhotoList photos)
			throws XmlPullParserException, IOException {
		int type;
		String name;
		SimpleDateFormat parseFormat = null;
		SimpleDateFormat outputFormat = null;

		final int depth = parser.getDepth();

		while (((type = parser.next()) != XmlPullParser.END_TAG || parser
				.getDepth() > depth)
				&& type != XmlPullParser.END_DOCUMENT) {
			if (type != XmlPullParser.START_TAG) {
				continue;
			}

			name = parser.getName();
			Photo photo = null;
			if (RESPONSE_TAG_PHOTOS.equals(name)) {
				photos.setPage(Integer.parseInt(parser.getAttributeValue(null,
						RESPONSE_ATTR_PAGE)));
				photos.setPageCount(Integer.parseInt(parser.getAttributeValue(
						null, RESPONSE_ATTR_PAGES)));
				photos.setPhotos(new ArrayList<Photo>());
			} else if (RESPONSE_TAG_PHOTO.equals(name)) {
				if (parseFormat == null) {
					parseFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					outputFormat = new SimpleDateFormat("MMMM d, yyyy");
				}
				photo = new Photo();

				parsePhoto(parser, photo);

				photo.setTitle(parser.getAttributeValue(null,
						RESPONSE_ATTR_TITLE));
				photo.setDate(parser.getAttributeValue(null,
						RESPONSE_ATTR_DATE_TAKEN));
				setFormatedDate(photo, parseFormat, outputFormat);

				photo.setTags(parser
						.getAttributeValue(null, RESPONSE_ATTR_TAGS));
				String ownerId = parser.getAttributeValue(null,
						RESPONSE_ATTR_OWNER_ID);
				UserInfo userInfo = new UserInfo(new User(ownerId));
				userInfo.setLocation(parser.getAttributeValue(null,
						RESPONSE_ATTR_OWNER_LOCATION));
				photo.setUserInfo(userInfo);

				photos.add(photo);
			}
		}
	}

	private void parsePhotoOnly(XmlPullParser parser, final Photo photo)
			throws XmlPullParserException, IOException {
		int type;
		String name = null;
		final int depth = parser.getDepth();
		boolean tagHanled = false;

		StringBuilder tags = new StringBuilder();
		while (((type = parser.next()) != XmlPullParser.END_TAG || parser
				.getDepth() > depth)
				&& type != XmlPullParser.END_DOCUMENT) {
			if (type == XmlPullParser.START_TAG) {
				name = parser.getName();
				if (RESPONSE_TAG_PHOTO.equals(name)) {
					parsePhoto(parser, photo);
				} else if (RESPONSE_TAG_OWNER.equals(name)) {
					String ownerId = parser.getAttributeValue(null,
							RESPONSE_ATTR_NSID);
					UserInfo userInfo = new UserInfo(new User(ownerId));
					userInfo.setLocation(parser.getAttributeValue(null,
							RESPONSE_ATTR_OWNER_LOCATION));
					photo.setUserInfo(userInfo);
				} else if (RESPONSE_TAG_TAG.equals(name)
						|| RESPONSE_TAG_TITLE.equals(name)) {
					tagHanled = true;
				} else if (RESPONSE_TAG_DATES.equals(name)) {
					photo.setDate(parser.getAttributeValue(null,
							RESPONSE_ATTR_TAKEN));
					SimpleDateFormat parseDateFormat = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					SimpleDateFormat outputDateFormat = new SimpleDateFormat(
							"MMMM d, yyyy");

					setFormatedDate(photo, parseDateFormat, outputDateFormat);
				} else {
					name = null;
				}
			} else if (type == XmlPullParser.TEXT) {
				if (tagHanled) {
					if (RESPONSE_TAG_TAG.equals(name)) {
						String tag = parser.getText();
						if (tag != null) {
							tags.append(tag + ",");
						}
					} else if (RESPONSE_TAG_TITLE.equals(name)) {
						photo.setTitle(parser.getText());
					}
					tagHanled = false;
				}
			}
		}
		photo.setTags(tags.toString());
	}

	private void setFormatedDate(Photo photo, SimpleDateFormat parseDateFormat,
			SimpleDateFormat outputDateFormat) {
		try {
			photo.setDate(outputDateFormat.format(parseDateFormat.parse(photo
					.getDate())));
		} catch (ParseException e) {
			android.util.Log.w(LOG_TAG, "Could not parse photo date", e);
		}
	}

	private Photo parsePhoto(XmlPullParser parser, final Photo photo)
			throws XmlPullParserException, IOException {
		photo.setPhotoId(parser.getAttributeValue(null, RESPONSE_ATTR_ID));
		photo.setSecret(parser.getAttributeValue(null, RESPONSE_ATTR_SECRET));
		photo.setServer(parser.getAttributeValue(null, RESPONSE_ATTR_SERVER));
		photo.setFarm(parser.getAttributeValue(null, RESPONSE_ATTR_FARM));

		photo.setSourceType(SourceType.FLICKR);
		photo.setUrlPattern(formatPhotoUrlPattern(photo));

		return photo;
	}

	private String formatPhotoUrlPattern(Photo photo) {
		return String.format(PHOTO_IMAGE_URL_PART_WITHOUT_POSTFIX, photo
				.getFarm(), photo.getServer(), photo.getPhotoId(), photo
				.getSecret())
				+ PHOTO_IMAGE_URL_PART_POSTFIX;
	}

	/**
	 * Executes an HTTP request on Flickr's web service. If the response is ok,
	 * the content is sent to the specified response handler.
	 * 
	 * @param get
	 *            The GET request to executed.
	 * @param handler
	 *            The handler which will parse the response.
	 * 
	 * @throws IOException
	 */
	private void executeRequest(HttpGet get, ResponseHandler handler)
			throws IOException {
		HttpEntity entity = null;
		HttpHost host = new HttpHost(API_REST_HOST, 80, "http");
		for (int i = 0; i < RECONNECT_COUNT; i++) {
			try {
				HttpResponse response = getHttpClient().execute(host, get);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					entity = response.getEntity();
					final InputStream in = entity.getContent();
					handler.handleResponse(in);
				}
				break;
			} catch (IOException exception) {
				Log.e(LOG_TAG, i + " trial is failed - "
						+ exception.getMessage());
				if (i + 1 == RECONNECT_COUNT) {
					throw exception;
				}
			} finally {
				if (entity != null) {
					entity.consumeContent();
				}
			}
		}
	}

	private PhotoList search(String query, int page, int perPage)
			throws ServiceNetworkException {
		final Uri.Builder uri = buildGetMethod(API_PHOTOS_SEARCH);
		uri.appendQueryParameter(PARAM_PER_PAGE, String.valueOf(perPage));
		uri.appendQueryParameter(PARAM_PAGE, String.valueOf(page));
		uri.appendQueryParameter(PARAM_TAGS, query);
		uri.appendQueryParameter(PARAM_EXTRAS, VALUE_DEFAULT_EXTRAS);

		String group = userPreferences.getGroup();
		if (group != "") {
			uri.appendQueryParameter(PARAM_GROUP_ID, group);
		}

		final HttpGet get = new HttpGet(uri.build().toString());
		final PhotoList photos = new PhotoList();

		try {
			executeRequest(get, new ResponseHandler() {
				public void handleResponse(InputStream in) throws IOException {
					parseResponse(in, new ResponseParser() {
						public void parseResponse(XmlPullParser parser)
								throws XmlPullParserException, IOException {
							parsePhotos(parser, photos);
						}
					});
				}
			});
		} catch (IOException e) {
			android.util.Log.e(LOG_TAG, "Could not find personal photos ");
			throw new ServiceNetworkException(e.getMessage(), e);
		}

		return photos;
	}

	private PhotoList getPersonalPhotos(int perPage, int page)
			throws ServiceNetworkException, UserNotFoundException {
		final Uri.Builder uri = buildGetMethod(API_PEOPLE_GET_PUBLIC_PHOTOS);

		uri.appendQueryParameter(PARAM_USERID, getUserId());
		uri.appendQueryParameter(PARAM_PER_PAGE, String.valueOf(perPage));
		uri.appendQueryParameter(PARAM_PAGE, String.valueOf(page));
		uri.appendQueryParameter(PARAM_EXTRAS, VALUE_DEFAULT_EXTRAS);

		final HttpGet get = new HttpGet(uri.build().toString());
		final PhotoList photos = new PhotoList();

		try {
			executeRequest(get, new ResponseHandler() {
				public void handleResponse(InputStream in) throws IOException {
					parseResponse(in, new ResponseParser() {
						public void parseResponse(XmlPullParser parser)
								throws XmlPullParserException, IOException {
							parsePhotos(parser, photos);
						}
					});
				}
			});
		} catch (IOException e) {
			android.util.Log.e(LOG_TAG, "Could not find personal photos ");
			throw new ServiceNetworkException(e.getMessage(), e);
		}

		return photos;
	}

	public PhotoList getRecentPhotos(int perPage, int page)
			throws ServiceNetworkException {
		final Uri.Builder uri = buildGetMethod(API_PHOTOS_GET_RECENT);
		uri.appendQueryParameter(PARAM_PER_PAGE, String.valueOf(perPage));
		uri.appendQueryParameter(PARAM_PAGE, String.valueOf(page));
		uri.appendQueryParameter(PARAM_EXTRAS, VALUE_DEFAULT_EXTRAS);

		final HttpGet get = new HttpGet(uri.build().toString());
		final PhotoList photos = new PhotoList();

		try {
			executeRequest(get, new ResponseHandler() {
				public void handleResponse(InputStream in) throws IOException {
					parseResponse(in, new ResponseParser() {
						public void parseResponse(XmlPullParser parser)
								throws XmlPullParserException, IOException {
							parsePhotos(parser, photos);
						}
					});
				}
			});
		} catch (IOException e) {
			android.util.Log.e(LOG_TAG, "Could not find personal photos ");
			throw new ServiceNetworkException(e.getMessage(), e);
		}

		return photos;
	}

	private PhotoList getFavoritePhotos(int perPage, int page) {
		PhotoList photoList = new PhotoList();

		photoList.setPage(page);
		photoList.setPageCount(perPage);
		List<Photo> photos = null;
		try {
			photos = photoDAO.select(getStart(perPage, page), perPage);
			for (Photo photo : photos) {
				loadPhotoInfo(photo);
			}
			int totalCount = photoDAO.getTotalCount();
			photoList.setPageCount(calculatePageCount(totalCount, perPage));
		} catch (Exception e) {
			android.util.Log.e(LOG_TAG, "Could not load favorite photos ");
		}
		photoList.setPhotos(photos);

		return photoList;
	}

	private int getStart(int perPage, int page) {
		return (page - 1) * perPage;
	}

	private int calculatePageCount(int totalCount, int perPage) {
		return (totalCount / perPage) + (totalCount % perPage == 0 ? 0 : 1);
	}

	private PhotoList getFileSystemPhotos(Uri uri, int perPage, int page) {
		PhotoList photoList = new PhotoList();

		photoList.setPage(page);
		photoList.setPageCount(perPage);
		List<Photo> photos = null;
		try {
			photos = imageDAO.select(uri, getStart(perPage, page), perPage);
			int removedItems = 0;
			for (Iterator<Photo> i = photos.iterator(); i.hasNext();) {
				Photo photo = i.next();
				// TODO: url == null is bad
				URL url = new URL(photo.getUrl(null));
				File file = new File(url.getFile());
				if (!file.canRead()) {
					i.remove();
					removedItems++;
				}
			}
			// for (Photo photo : photos) {
			// loadPhotoInfo(photo);
			// }
			int totalCount = imageDAO.getTotalCount(uri) - removedItems;
			photoList.setPageCount(calculatePageCount(totalCount, perPage));
		} catch (Exception e) {
			android.util.Log.e(LOG_TAG, "Could not load favorite photos ");
		}
		photoList.setPhotos(photos);

		return photoList;
	}

	private void loadPhotoInfo(final Photo photo) {
		final Uri.Builder uri = buildGetMethod(API_PHOTOS_GET_INFO);
		uri.appendQueryParameter(PARAM_PHOTO_ID, photo.getPhotoId());
		final HttpGet get = new HttpGet(uri.build().toString());
		try {
			executeRequest(get, new ResponseHandler() {
				public void handleResponse(InputStream in) throws IOException {
					parseResponse(in, new ResponseParser() {
						public void parseResponse(XmlPullParser parser)
								throws XmlPullParserException, IOException {
							parsePhotoOnly(parser, photo);
						}
					});
				}
			});
		} catch (IOException e) {
			android.util.Log.e(LOG_TAG, "Could not find the photo with id: "
					+ photo.getPhotoId());
		}
	}

	/**
	 * Loads a Bitmap representing the photo for the specified size. The Bitmap
	 * is loaded from the URL returned by
	 * {@link #getUrl(com.koonen.photostream.Flickr.PhotoSize)}.
	 * 
	 * @param size
	 *            The size of the photo to load.
	 * 
	 * @return A Bitmap whose longest size is the same as the longest side of
	 *         the specified {@link com.koonen.photostream.Flickr.PhotoSize}, or
	 *         null if the photo could not be loaded.
	 */
	public Bitmap loadPhotoBitmap(Photo photo, PhotoSize size) {
		Bitmap bitmap = null;
		InputStream in = null;
		BufferedOutputStream out = null;

		try {
			URL url = new URL(photo.getUrl(size));
			if (url.getHost() == null) {
				if (url.getFile() != null) {
					File file = new File(url.getFile());
					if (file.canRead()) {
						in = new BufferedInputStream(new FileInputStream(file));
					}

				}
			} else {
				in = new BufferedInputStream(url.openStream(), IO_BUFFER_SIZE);
			}

			if (in != null) {
				if (FLAG_DECODE_PHOTO_STREAM_WITH_SKIA) {
					bitmap = BitmapFactory.decodeStream(in);
				} else {
					final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
					out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
					StreamUtils.copy(in, out);
					out.flush();

					final byte[] data = dataStream.toByteArray();
					bitmap = BitmapFactory
							.decodeByteArray(data, 0, data.length);
				}
			} else {
				Log.w(LOG_TAG, "Can't load photo - no file and no url");
			}

		} catch (IOException e) {
			Log.e(LOG_TAG, "Could not load photo: " + this, e);
		} finally {
			StreamUtils.closeStream(in);
			StreamUtils.closeStream(out);
		}

		return bitmap;
	}

	// /**
	// * Downloads the specified photo at the specified size in the specified
	// * destination.
	// *
	// * @param photo
	// * The photo to download.
	// * @param size
	// * The size of the photo to download.
	// * @param destination
	// * The output stream in which to write the downloaded photo.
	// *
	// * @throws IOException
	// * If any network exception occurs during the download.
	// */
	// public void downloadPhoto(Photo photo, PhotoSize size,
	// OutputStream destination) throws IOException {
	// final BufferedOutputStream out = new BufferedOutputStream(destination,
	// IO_BUFFER_SIZE);
	// final String url = photo.getUrl(size);
	//
	// final HttpGet get = new HttpGet(url);
	//
	// HttpEntity entity = null;
	// try {
	// final HttpResponse response = client.execute(get);
	// if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	// entity = response.getEntity();
	// entity.writeTo(out);
	// out.flush();
	// }
	// } finally {
	// if (entity != null) {
	// entity.consumeContent();
	// }
	// }
	// }

	public PhotoList execute(ServiceContext context)
			throws ServiceNetworkException, UserNotFoundException {
		PhotoList result = null;
		int perPage = context.getPageSize();
		int page = context.getCurrentPage();

		switch (context.getType()) {
		case PERSONAL:
			result = getPersonalPhotos(perPage, page);
			break;

		case SEARCH:
			result = search(context.getQuery(), page, perPage);
			break;

		case RECENT:
			result = getRecentPhotos(perPage, page);
			break;

		case FAVORITES:
			result = getFavoritePhotos(perPage, page);
			break;

		case FILE_SYSTEM_INT:
			result = getFileSystemPhotos(Media.INTERNAL_CONTENT_URI, perPage,
					page);
			break;
		case FILE_SYSTEM_EXT:
			result = getFileSystemPhotos(Media.EXTERNAL_CONTENT_URI, perPage,
					page);
			break;
		default:
			break;
		}

		if (result != null) {
			context.setCountPages(result.getPageCount());
		}

		return result;
	}
}
