package com.koonen.photostream.api;

/**
 * 
 * @author dryganets
 * 
 */
public class ServiceException extends Exception {

	private static final long serialVersionUID = 8847349449119513892L;

	protected ServiceException(String detailMessage) {
		super(detailMessage);
	}

	protected ServiceException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}
}
