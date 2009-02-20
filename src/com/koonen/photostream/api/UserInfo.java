package com.koonen.photostream.api;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * A set of information for a given user. The information exposed include: - The
 * user's id - The user's location
 */
public class UserInfo implements Parcelable {
	private String mUserId;
	private String mLocation;

	public UserInfo(User user) {
		mUserId = user.getId();
	}
	
	public UserInfo(UserInfo userInfo) {
		mUserId = userInfo.mUserId;
		mLocation = userInfo.mLocation;
	}
	
	public UserInfo(Parcel parcel) {
		mUserId = parcel.readString();
		mLocation = parcel.readString();
	}
	
	public String getUserId() {
		return mUserId;
	}

	public String getLocation() {
		return mLocation;
	}

	public void setLocation(String location) {
		mLocation = location;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mUserId);
		dest.writeString(mLocation);
	}
	
	public static final Parcelable.Creator<UserInfo> CREATOR = new Parcelable.Creator<UserInfo>() {
		public UserInfo createFromParcel(Parcel in) {
			return new UserInfo(in);
		}

		public UserInfo[] newArray(int size) {
			return new UserInfo[size];
		}
	};
}
