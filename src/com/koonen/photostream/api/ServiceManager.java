package com.koonen.photostream.api;

import com.koonen.photostream.api.flickr.FlickrService;
import com.koonen.photostream.dao.PhotoDAO;
import com.koonen.photostream.settings.Network;
import com.koonen.photostream.settings.UserPreferences;

import android.content.Context;

/**
 * 
 * @author dryganets
 * 
 */
public class ServiceManager {

	private Context context;

	private UserPreferences userPreferences;

	private FlickrService flickrService;
	
	private PhotoDAO photoDAO;

	private static ServiceManager instance;

	public static void init(Context context) {
		if (instance == null) {
			instance = new ServiceManager(context);
		}
	}

	public static ServiceManager get() {
		return instance;
	}

	private ServiceManager(Context context) {
		this.context = context;
		userPreferences = new UserPreferences(this.context);
		photoDAO = new PhotoDAO(this.context);
	}

	public IPhotoService getService() {
		IPhotoService service = null;
		Network network = userPreferences.getNetwork();
		if (Network.FLICKR == network) {
			service = getFlickrService();
		}

		return service;
	}

	private FlickrService getFlickrService() {
		if (flickrService == null) {
			flickrService = new FlickrService(userPreferences, photoDAO);
		}
		return flickrService;
	}

}
