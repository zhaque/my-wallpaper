package com.koonen.photostream.api;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * @author Glick
 * 
 */
public class FilePhoto extends Photo {

	private String path;
	private boolean cached;

	public FilePhoto(String path) {
		this.path = path;
		cached = false;
	}

	public FilePhoto(Photo photo, String path) {
		super(photo);
		this.path = path;
		this.cached = true;
	}

	private FilePhoto(Parcel in) {
		super(in);
		path = in.readString();
		cached = Boolean.parseBoolean(in.readString());
	}

	@Override
	public String getUrl(PhotoSize photoSize) {
		return path;
	}

	@Override
	public boolean isScaled() {
		return true;
	}

	public boolean isCached() {
		return cached;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(path);
		dest.writeString(Boolean.toString(cached));
	}

	public static final Parcelable.Creator<FilePhoto> CREATOR = new Parcelable.Creator<FilePhoto>() {
		public FilePhoto createFromParcel(Parcel in) {
			return new FilePhoto(in);
		}

		public FilePhoto[] newArray(int size) {
			return new FilePhoto[size];
		}
	};
}
