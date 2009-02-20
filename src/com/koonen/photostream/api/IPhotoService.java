package com.koonen.photostream.api;

import android.graphics.Bitmap;

/**
 * 
 * @author dryganets
 * 
 */
public interface IPhotoService {

//	public void downloadPhoto(Photo photo, PhotoSize size,
//			OutputStream destination) throws IOException;
	
	public Bitmap loadPhotoBitmap(Photo photo, PhotoSize size);

	public PhotoList execute(ServiceContext context)
			throws ServiceNetworkException, UserNotFoundException;
}
