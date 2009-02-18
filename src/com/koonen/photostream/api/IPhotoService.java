package com.koonen.photostream.api;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 
 * @author dryganets
 * 
 */
public interface IPhotoService {

	public void downloadPhoto(Photo photo, PhotoSize size,
			OutputStream destination) throws IOException;

	public PhotoList execute(ServiceContext context)
			throws ServiceNetworkException, UserNotFoundException;
}
