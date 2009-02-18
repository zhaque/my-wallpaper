package com.koonen.photostream.api;

/**
 * 
 * @author Glick
 *
 */
public class ServiceNetworkException extends ServiceException {

	private static final long serialVersionUID = 5679809989593207185L;
	
	private static final String MESSAGE_FORMAT = "Network problem. Message details:\n{0}";

	public ServiceNetworkException(String detailMessage) {
		super(String.format(MESSAGE_FORMAT, detailMessage));
	}

	public ServiceNetworkException(String detailMessage, Throwable throwable) {
		super(String.format(MESSAGE_FORMAT, detailMessage), throwable);
	}
}
