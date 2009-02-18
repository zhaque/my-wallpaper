package com.koonen.photostream.api;

import java.io.IOException;
import java.io.InputStream;

public interface ResponseHandler {
	/**
	 * Processes the responses sent by the HTTP server following a GET request.
	 * 
	 * @param in
	 *            The stream containing the server's response.
	 * 
	 * @throws IOException
	 */
	public void handleResponse(InputStream in) throws IOException;
}