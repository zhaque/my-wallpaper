package com.koonen.photostream.api;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents the geographical location of a photo.
 */
public class Location implements Parcelable {
	private float mLatitude;
	private float mLongitude;

	Location(float latitude, float longitude) {
		mLatitude = latitude;
		mLongitude = longitude;
	}
	
	public Location(Parcel in) {
		mLatitude = in.readFloat();
		mLongitude = in.readFloat();
	}

	float getLatitude() {
		return mLatitude;
	}

	float getLongitude() {
		return mLongitude;
	}

	public void setLatitude(float latitude) {
		mLatitude = latitude;
	}

	public void setLongitude(float longitude) {
		mLongitude = longitude;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeFloat(mLatitude);
		dest.writeFloat(mLongitude);
	}
	
	public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>() {
		public Location createFromParcel(Parcel in) {
			return new Location(in);
		}

		public Location[] newArray(int size) {
			return new Location[size];
		}
	};
}
