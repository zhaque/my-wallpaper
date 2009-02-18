package com.koonen.photostream.api;

/**
 * 
 * @author dryganets
 * 
 */
public class UserNotFoundException extends ServiceException {

	private static final long serialVersionUID = -2452895792705200301L;

	private static final String MESSAGE_FORMAT = "User with name {0} not found";

	public UserNotFoundException(String userName) {
		super(String.format(MESSAGE_FORMAT, userName));
	}

	public UserNotFoundException(String userName, Throwable throwable) {
		super(String.format(MESSAGE_FORMAT, userName), throwable);
	}

}
