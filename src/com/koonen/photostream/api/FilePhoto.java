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
	
	public FilePhoto(String path) {
		this.path = path;
	}
	
	private FilePhoto(Parcel in) {
		path = in.readString();
	}

	@Override
	public String getUrl(PhotoSize photoSize) {
		return path;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(path);
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
