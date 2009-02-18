package com.koonen.photostream.dao;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * @author Glick
 * 
 */
public class Category implements Parcelable {

	public static final String RECENT_CATEGORY = "Recent";

//	private static final String[] CATEGORY_NAMES = { "Me", "Art", "Australia",
//			"Beach", "Birthday", "California", "Canada", "Cat", "China",
//			"Christmas", "City", "Concert", "Dog", "England", "Europe",
//			"Family", "Festival", "Flowers", "Food", "France", "Friends",
//			"Fun", "Germany", "Green", "Holiday", "Italy", "Japan", "London",
//			"Music", "Nature", "New", "Newyork", "Night", "Nikon", "Nyc",
//			"Paris", "Park", "Party", "People", "Portrait", "Red",
//			"San Francisco", "Sky", "Snow", "Spain", "Summer", "Sunset",
//			"Stockholm", "Travel", "Usa", "Vacation", "Water", "Wedding",
//			"Winter" };
//
//	public static final Set<String> CATEGORY_NAMES_SET;
//
//	static {
//		CATEGORY_NAMES_SET = new HashSet<String>(Arrays.asList(CATEGORY_NAMES));
//	}

	private int id;
	private String name;
	private String tags;

	private Category() {
	}

	private Category(Parcel in) {
		id = in.readInt();
		name = in.readString();
		tags = in.readString();
	}

	public void update(Category category) {
		name = category.name;
		tags = category.tags;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public boolean isRecent() {
		return RECENT_CATEGORY.equals(name);
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public String toString() {
		return id + " - " + name;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(name);
		dest.writeString(tags);
	}

	public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {
		public Category createFromParcel(Parcel in) {
			return new Category(in);
		}

		public Category[] newArray(int size) {
			return new Category[size];
		}
	};

	public static Category createRecentCategory() {
		Category category = new Category();
		category.name = RECENT_CATEGORY;
		category.tags = "";
		return category;
	}

	public static Category createCategory() {
		Category category = new Category();
		return category;
	}
}
