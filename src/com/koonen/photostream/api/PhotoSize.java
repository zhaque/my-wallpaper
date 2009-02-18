package com.koonen.photostream.api;

/**
 * Defines the size of the image to download from Flickr.
 * 
 * @see com.koonen.photostream.Flickr.Photo
 */
public enum PhotoSize {
	/**
	 * Small square image (75x75 px).
	 */
	SMALL_SQUARE("_s", 75),
	/**
	 * Thumbnail image (the longest side measures 100 px).
	 */
	THUMBNAIL("_t", 100),
	/**
	 * Small image (the longest side measures 240 px).
	 */
	SMALL("_m", 240),
	/**
	 * Medium image (the longest side measures 500 px).
	 */
	MEDIUM("", 500),
	/**
	 * Large image (the longest side measures 1024 px).
	 */
	LARGE("_b", 1024),

	ORIGINAL("_o", 0);

	private final String mSize;
	private final int mLongSide;

	private PhotoSize(String size, int longSide) {
		mSize = size;
		mLongSide = longSide;
	}

	/**
	 * Returns the size in pixels of the longest side of the image.
	 * 
	 * @return THe dimension in pixels of the longest side.
	 */
	int longSide() {
		return mLongSide;
	}

	/**
	 * Returns the name of the size, as defined by Flickr. For instance, the
	 * LARGE size is defined by the String "_b".
	 * 
	 * @return
	 */
	String size() {
		return mSize;
	}

	@Override
	public String toString() {
		return name() + ", longSide=" + mLongSide;
	}
}