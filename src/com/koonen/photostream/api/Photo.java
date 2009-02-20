package com.koonen.photostream.api;

import android.os.Parcel;
import android.os.Parcelable;

import com.koonen.photostream.api.flickr.FlickrConstants;

/**
 * A photo is represented by a title, the date at which it was taken and a URL.
 * The URL depends on the desired
 * {@link com.koonen.photostream.Flickr.PhotoSize}.
 */
public class Photo implements Parcelable, FlickrConstants {
//	private static final String TAG = "Photo";

	private int id;
	private String photoId;
	private String secret;
	private String server;
	private String farm;
	private String title;
	private String date;

	private String tags;
	private UserInfo userInfo;

	private String urlPattern;
	private SourceType sourceType;

	public Photo() {
	}

	protected Photo(Parcel in) {
		id = in.readInt();
		photoId = in.readString();
		secret = in.readString();
		server = in.readString();
		farm = in.readString();
		title = in.readString();
		date = in.readString();
		tags = in.readString();
		urlPattern = in.readString();
		sourceType = SourceType.valueOf(in.readString());
		userInfo = new UserInfo(in);
	}

	/**
	 * Returns the title of the photo, if specified.
	 * 
	 * @return The title of the photo. The returned value can be empty or null.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Returns the date at which the photo was taken, formatted in the current
	 * locale with the following pattern: MMMM d, yyyy.
	 * 
	 * @return The title of the photo. The returned value can be empty or null.
	 */
	public String getDate() {
		return date;
	}

	/**
	 * Returns the URL to the photo for the specified size.
	 * 
	 * @param photoSize
	 *            The required size of the photo.
	 * 
	 * @return A URL to the photo for the specified size.
	 * 
	 * @see com.koonen.photostream.Flickr.PhotoSize
	 */
	public String getUrl(PhotoSize photoSize) {
		return String.format(urlPattern, photoSize.size());
	}

	@Override
	public String toString() {
		return title + ", " + date + " @" + photoId;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(photoId);
		dest.writeString(secret);
		dest.writeString(server);
		dest.writeString(farm);
		dest.writeString(title);
		dest.writeString(date);
		dest.writeString(tags);
		dest.writeString(urlPattern);
		dest.writeString(sourceType.getValue());
		userInfo.writeToParcel(dest, flags);
	}

	public static final Parcelable.Creator<Photo> CREATOR = new Parcelable.Creator<Photo>() {
		public Photo createFromParcel(Parcel in) {
			return new Photo(in);
		}

		public Photo[] newArray(int size) {
			return new Photo[size];
		}
	};

	public String getPhotoId() {
		return photoId;
	}

	public void setPhotoId(String photoId) {
		this.photoId = photoId;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getFarm() {
		return farm;
	}

	public void setFarm(String farm) {
		this.farm = farm;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getUrlPattern() {
		return urlPattern;
	}

	public void setUrlPattern(String urlPattern) {
		this.urlPattern = urlPattern;
	}

	public SourceType getSourceType() {
		return sourceType;
	}

	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}
}