package com.koonen.photostream.api;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of {@link com.koonen.photostream.Flickr.Photo photos}. A list
 * represents a series of photo on a page from the user's photostream, a list is
 * therefore associated with a page index and a page count. The page index and
 * the page count both depend on the number of photos per page.
 */
public class PhotoList {
	private List<Photo> photos;
	private int page;
	private int pageCount;

	public PhotoList() {
		photos = new ArrayList<Photo>();
	}

	public void add(Photo photo) {
		photos.add(photo);
	}

	/**
	 * Returns the photo at the specified index in the current set. An
	 * {@link ArrayIndexOutOfBoundsException} can be thrown if the index is less
	 * than 0 or greater then or equals to {@link #getCount()}.
	 * 
	 * @param index
	 *            The index of the photo to retrieve from the list.
	 * 
	 * @return A valid {@link com.koonen.photostream.Flickr.Photo}.
	 */
	public Photo get(int index) {
		return photos.get(index);
	}

	/**
	 * Returns the number of photos in the list.
	 * 
	 * @return A positive integer, or 0 if the list is empty.
	 */
	public int getCount() {
		return photos != null ? photos.size() : 0;
	}

	/**
	 * Returns the page index of the photos from this list.
	 * 
	 * @return The index of the Flickr page that contains the photos of this
	 *         list.
	 */
	public int getPage() {
		return page;
	}

	/**
	 * Returns the total number of photo pages.
	 * 
	 * @return A positive integer, or 0 if the photostream is empty.
	 */
	public int getPageCount() {
		return pageCount;
	}

	public List<Photo> getPhotos() {
		return photos;
	}

	public void setPhotos(List<Photo> photos) {
		this.photos = photos;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}
}