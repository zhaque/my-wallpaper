package com.koonen.photostream.api.flickr;

/**
 * 
 * @author dryganets
 * 
 */
public interface FlickrConstants {

	String API_KEY = "0600a74e52c683df704b27cb8ac547cb";

	String API_REST_HOST = "api.flickr.com";
	String API_REST_URL = "/services/rest/";
	String API_FEED_URL = "/services/feeds/photos_public.gne";

	String API_PEOPLE_FIND_BY_USERNAME = "flickr.people.findByUsername";
	String API_PEOPLE_GET_INFO = "flickr.people.getInfo";
	String API_PEOPLE_GET_PUBLIC_PHOTOS = "flickr.people.getPublicPhotos";
	String API_PEOPLE_GET_LOCATION = "flickr.photos.geo.getLocation";
	String API_PHOTOS_GET_RECENT = "flickr.photos.getRecent";
	String API_PHOTOS_SEARCH = "flickr.photos.search";
	String API_PHOTOS_GET_INFO = "flickr.photos.getInfo";
	String API_AUTHS_GET_FROB = "flickr.auth.getFrob";
	String API_AUTHS_GET_TOKEN = "flickr.auth.getToken";
	String API_AUTHS_CHECK_TOKEN = "flickr.auth.checkToken";

	String PARAM_API_KEY = "api_key";
	String PARAM_METHOD = "method";
	String PARAM_USERNAME = "username";
	String PARAM_USERID = "user_id";
	String PARAM_PER_PAGE = "per_page";
	String PARAM_PAGE = "page";
	String PARAM_EXTRAS = "extras";
	String PARAM_TEXT = "text";
	String PARAM_TAGS = "tags";
	String PARAM_GROUP_ID = "group_id";
	String PARAM_PHOTO_ID = "photo_id";
	String PARAM_FEED_ID = "id";
	String PARAM_FEED_FORMAT = "format";
	String PARAM_AUTH_FROB = "frob";
	String PARAM_AUTH_TOKEN = "auth_token";
	String PARAM_AUTH_MINI_TOKEN = "mini_token";

	String VALUE_DEFAULT_EXTRAS = "date_taken,  tags";

	String VALUE_DEFAULT_FORMAT = "atom";

	String RESPONSE_TAG_RSP = "rsp";
	String RESPONSE_ATTR_STAT = "stat";
	String RESPONSE_STATUS_OK = "ok";

	String RESPONSE_TAG_USER = "user";
	String RESPONSE_ATTR_NSID = "nsid";

	String RESPONSE_TAG_FROB = "frob";

	String RESPONSE_TAG_TOKEN = "token";
	String RESPONSE_TAG_PERMS = "perms";

	String RESPONSE_TAG_PHOTOS = "photos";
	String RESPONSE_ATTR_PAGE = "page";
	String RESPONSE_ATTR_PAGES = "pages";

	String RESPONSE_TAG_PHOTO = "photo";
	String RESPONSE_ATTR_ID = "id";
	String RESPONSE_ATTR_SECRET = "secret";
	String RESPONSE_ATTR_SERVER = "server";
	String RESPONSE_ATTR_FARM = "farm";
	String RESPONSE_ATTR_TITLE = "title";
	String RESPONSE_ATTR_DATE_TAKEN = "datetaken";
	String RESPONSE_ATTR_TAGS = "tags";
	String RESPONSE_ATTR_OWNER_ID = "owner";
	String RESPONSE_ATTR_OWNER_LOCATION = "location";

	String RESPONSE_TAG_TAG = "tag";

	String RESPONSE_TAG_OWNER = "owner";
	// String RESPONSE_ATTR_NSID = "nsid";
	// String RESPONSE_ATTR_OWNER_LOCATION = "location";

	String RESPONSE_TAG_DATES = "dates";
	String RESPONSE_ATTR_TAKEN = "taken";

	String RESPONSE_TAG_TITLE = "title";

	String RESPONSE_TAG_PERSON = "person";
	String RESPONSE_ATTR_ISPRO = "ispro";
	String RESPONSE_ATTR_ICONSERVER = "iconserver";
	String RESPONSE_ATTR_ICONFARM = "iconfarm";
	String RESPONSE_TAG_USERNAME = "username";
	String RESPONSE_TAG_REALNAME = "realname";
	String RESPONSE_TAG_LOCATION = "location";
	String RESPONSE_ATTR_LATITUDE = "latitude";
	String RESPONSE_ATTR_LONGITUDE = "longitude";
	String RESPONSE_TAG_PHOTOSURL = "photosurl";
	String RESPONSE_TAG_PROFILEURL = "profileurl";
	String RESPONSE_TAG_MOBILEURL = "mobileurl";

	String RESPONSE_TAG_FEED = "feed";
	String RESPONSE_TAG_UPDATED = "updated";

	String PHOTO_IMAGE_URL_PART_WITHOUT_POSTFIX = "http://farm%s.static.flickr.com/%s/%s_%s";
	String PHOTO_IMAGE_URL_PART_POSTFIX = "%s.jpg";

	String BUDDY_ICON_URL = "http://farm%s..flickr.com/%s/buddyicons/%s.jpg";
	String DEFAULT_BUDDY_ICON_URL = "http://www.flickr.com/images/buddyicon.jpg";

	boolean FLAG_DECODE_PHOTO_STREAM_WITH_SKIA = false;
	int IO_BUFFER_SIZE = 4 * 1024;

	String WALLPAPERS_GROUP_ID = "94834891@N00";

}
