package com.koonen.photostream.api;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Response parser used with {@link Flickr#parseResponse(java.io.InputStream,
 * com.axhive.photostream.Flickr.ResponseParser)}. When Flickr returns a valid
 * response, this parser is invoked to process the XML response.
 */
public interface ResponseParser {
	/**
	 * Processes the XML response sent by the Flickr web service after a
	 * successful request.
	 * 
	 * @param parser
	 *            The parser containing the XML responses.
	 * 
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public void parseResponse(XmlPullParser parser)
			throws XmlPullParserException, IOException;
}