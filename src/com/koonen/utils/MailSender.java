package com.koonen.utils;

import java.net.*;
import java.io.*;
import java.util.*;

public class MailSender {

	private Socket smtpHost;
	private BufferedReader In;
	private DataOutputStream Out;

	/**
	 * <P>
	 * Creates a new connection to the mail host on port 25
	 * <P>
	 * 
	 * @param _Host
	 *            The host name where the mail server resides.
	 * 
	 */
	public MailSender(String _Host) {
		try {
			smtpHost = new Socket(_Host, 25);
			Out = new DataOutputStream(smtpHost.getOutputStream());
			In = new BufferedReader(new InputStreamReader(smtpHost
					.getInputStream()));

			// - Read Welcome message from server
			String LineIn = In.readLine();
			if (LineIn.indexOf("220") == -1)
				throw new Exception("Bad Server");

			if (In.ready())
				LineIn = In.readLine();

			// - Introduce ourselves to the server
			Out.writeBytes("HELO n-ary.com\r\n");
			LineIn = In.readLine();
			if (LineIn.indexOf("250") == -1)
				throw new Exception("No response from Server");
		} catch (Exception E) {
			smtpHost = null;
		}
	}

	public boolean bOk() {
		if (smtpHost == null)
			return false;
		else
			return true;
	}

	/**
	 * <P>
	 * Sends a new email message to the specified addressee
	 * <P>
	 * 
	 * @param _to
	 *            The email address that will receive the email
	 * @param _from
	 *            The email address that will send the email
	 * @param _subject
	 *            The email subject
	 * @param _body
	 *            The email body
	 * @return boolean Returns a <code>true</code> if the mail was sent
	 *         successfully
	 * 
	 */
	public boolean send(String _to, String _from, String _subject, String _body) {
		// - Send One Email
		if (smtpHost == null)
			return false;

		try {
			String LineIn;

			// - Set the FROM
			Out.writeBytes("MAIL FROM:<" + _from + ">\r\n");
			LineIn = In.readLine();
			if (LineIn.indexOf("250") == -1)
				throw new Exception("Bad MAIL FROM:");

			// - Set the TO field
			Out.writeBytes("RCPT TO:<" + transform(_to) + ">\r\n");
			LineIn = In.readLine();
			if (LineIn.indexOf("250") == -1)
				throw new Exception("Bad RCPT TO:");

			// - Set the DATA field
			Out.writeBytes("DATA\r\n");
			LineIn = In.readLine();
			if (LineIn.indexOf("354") == -1)
				throw new Exception("Bad DATA");
			Out.writeBytes("From: " + _from + "\r\nSubject: " + _subject
					+ "\r\n");
			Out.writeBytes("To: " + _to + "\r\n");

			Out.writeBytes(_body);
			Out.writeBytes("\r\n.\r\n");

			LineIn = In.readLine();
			if (LineIn.indexOf("250") == -1)
				throw new Exception("Bad End of Data");

		} catch (Exception E) {
			return false;
		}

		return true;
	}

	public boolean sendStart(String _to, String _from, String _subject) {
		// - Send One Email
		if (smtpHost == null)
			return false;

		String LineIn = "";
		try {

			// - Set the FROM
			Out.writeBytes("MAIL FROM:<" + _from + ">\r\n");
			LineIn = In.readLine();
			if (LineIn.indexOf("250") == -1)
				throw new Exception("Bad MAIL FROM:");

			// - Set the TO field
			Out.writeBytes("RCPT TO:<" + transform(_to) + ">\r\n");
			LineIn = In.readLine();
			if (LineIn.indexOf("250") == -1)
				throw new Exception("Bad RCPT TO:");

			// - Set the DATA field
			Out.writeBytes("DATA\r\n");

			LineIn = In.readLine();
			if (LineIn.indexOf("354") == -1)
				throw new Exception("Bad DATA");
			Out.writeBytes("From: " + _from + "\r\nSubject: " + _subject
					+ "\r\n");
			Out.writeBytes("To: " + _to + "\r\n");

		} catch (Exception E) {
			return false;
		}

		return true;
	}

	public boolean sendStart(String _to[], String _from, String _subject) {
		// - Send One Email
		if (smtpHost == null || _to.length == 0)
			return false;

		String LineIn = "";
		try {

			// - Set the FROM
			Out.writeBytes("MAIL FROM:<" + _from + ">\r\n");
			LineIn = In.readLine();
			if (LineIn.indexOf("250") == -1)
				throw new Exception("Bad MAIL FROM:");

			// - Set the TO field
			for (int x = 0; x < _to.length; x++) {
				Out.writeBytes("RCPT TO:<" + transform(_to[x]) + ">\r\n");
				LineIn = In.readLine();
				if (LineIn.indexOf("250") == -1)
					throw new Exception("Bad RCPT TO:");
			}

			// - Set the DATA field
			Out.writeBytes("DATA\r\n");

			LineIn = In.readLine();
			if (LineIn.indexOf("354") == -1)
				throw new Exception("Bad DATA");
			Out.writeBytes("From: " + _from + "\r\nSubject: " + _subject
					+ "\r\n");
			Out.writeBytes("To: " + _to[0] + "\r\n");

		} catch (Exception E) {
			return false;
		}

		return true;
	}

	public boolean sendBodyLine(String _line) {
		try {
			Out.writeBytes(_line + "\n");
			return true;
		} catch (Exception E) {
		}
		return false;
	}

	public boolean sendEnd() {
		try {
			Out.writeBytes("\r\n.\r\n");
			String LineIn = In.readLine();
			if (LineIn.indexOf("250") == -1)
				throw new Exception("Bad End of Data");

			return true;
		} catch (Exception E) {
		}
		return false;
	}

	/**
	 * <P>
	 * Closes the connection to the mail server
	 * <P>
	 */
	public void close() {
		try {
			if (smtpHost != null)
				smtpHost.close();
		} catch (Exception E) {
		}
	}

	protected void finalize() throws Throwable {
		close();
	}

	public static String transform(String _to) {
		String newTo = "";

		StringTokenizer st = new StringTokenizer(_to, " ");
		while (st.hasMoreTokens()) {
			newTo = st.nextToken();
			if (newTo.indexOf("@") != -1) {
				return newTo.replace('<', ' ').replace('>', ' ').trim();
			}
		}

		return _to;
	}

	public boolean sendMail(String _to[], String _from, String _cc[],
			String _bcc[], String _subject, Vector<String> _vBody) {

		// - Send One Email
		if (smtpHost == null || _to.length == 0)
			return false;

		String LineIn = "";
		try {

			// - Set the FROM
			Out.writeBytes("MAIL FROM:<" + _from + ">\r\n");
			LineIn = In.readLine();
			if (LineIn.indexOf("250") == -1)
				throw new Exception("Bad MAIL FROM:");

			// - Set the TO field
			for (int x = 0; x < _to.length; x++) {
				Out.writeBytes("RCPT TO:<" + transform(_to[x]) + ">\r\n");
				LineIn = In.readLine();
			}

			// - Set the CC field
			for (int x = 0; x < _cc.length; x++) {
				Out.writeBytes("RCPT TO:<" + transform(_cc[x]) + ">\r\n");
				LineIn = In.readLine();
			}

			// - Set the BCC field
			for (int x = 0; x < _bcc.length; x++) {
				Out.writeBytes("RCPT TO:<" + transform(_bcc[x]) + ">\r\n");
				LineIn = In.readLine();
			}

			// - Set the DATA field
			Out.writeBytes("DATA\r\n");

			LineIn = In.readLine();
			if (LineIn.indexOf("354") == -1)
				throw new Exception("Bad DATA");
			Out.writeBytes("From: " + _from + "\r\nSubject: " + _subject
					+ "\r\n");

			// - Send the to field
			Out.writeBytes("To: ");
			for (int x = 0; x < _to.length; x++) {
				Out.writeBytes(transform(_to[x]));
				if (x < _to.length - 1)
					Out.writeBytes(",");
			}
			Out.writeBytes("\r\n");

			// - Send the to field
			Out.writeBytes("Cc: ");
			for (int x = 0; x < _cc.length; x++) {
				Out.writeBytes(transform(_cc[x]));
				if (x < _cc.length - 1)
					Out.writeBytes(",");
			}
			Out.writeBytes("\r\n");

			java.util.Enumeration<?> EE = _vBody.elements();
			while (EE.hasMoreElements())
				Out.writeBytes((String) EE.nextElement() + "\n");

			Out.writeBytes("\r\n.\r\n");
			LineIn = In.readLine();
			if (LineIn.indexOf("250") == -1)
				throw new Exception("Bad End of Data");

			return true;

		} catch (Exception E) {
		}

		return false;
	}

}