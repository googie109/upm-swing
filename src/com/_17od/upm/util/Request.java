package com._17od.upm.util;

import javax.xml.ws.http.HTTPException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by dkramer on 6/2/17.
 */

public class Request {
    private static String DOMAIN = "http://localhost:4040/test";

    /*
     * Default Timeout values
     */
    private static final int DEFAULT_READ_TIMEOUT       = 10000;
    private static final int DEFAULT_CONNECT_TIMEOUT    = 15000;

	/*
	 * HTTP Method Request Constants
	 */
	public static final String METHOD_GET       = "GET";
	public static final String METHOD_POST      = "POST";
	public static final String METHOD_PUT       = "PUT";
	public static final String METHOD_DELETE    = "DELETE";


    // set the global domain to be used by all requests
    public static void setGlobalDomain(String domain) {
        DOMAIN = domain;
    }


	// connection to the server
	private HttpURLConnection connection;

	// destination
	private String endpoint;

	// HTTP method
	private String method;

	// url urlParams
	private String urlParams;

	// data to send in request
	private String reqData;

	// response received back after this request has been sent
	private Response response;

    // has an error of some kind occurred?
    private boolean hasError;




	// use the static GET, POST, PUT, and DELETE methods
	private Request(String endpoint, String method) {
		this.endpoint = endpoint;
		this.method = method;
	}


	/* Convenience Methods for each of the available HTTP Methods */

	public static Request Get(String endpoint) {
		Request req = new Request(endpoint, METHOD_GET);
		return req;
	}

	public static Request Post(String endpoint) {
		Request req = new Request(endpoint, METHOD_POST);
		return req;
	}

	public static Request Put(String endpoint) {
		Request req = new Request(endpoint, METHOD_PUT);
		return req;
	}

	public static Request Delete(String endpoint) {
		Request req = new Request(endpoint, METHOD_DELETE);
		return req;
	}


	// set the url parameter needed for the endpoint
	public Request setURLParams(String params) {
		this.urlParams = params;
		return this;
	}

	// send out the request synchronously
	public Request send() throws IOException {
		// build connection to send
		createConnection();
		if (reqData != null) {
			writeDataToConnection();
		}
		connection.connect();

		// build a request object from whatever we receive back
		createResponse();
		return this;
	}

	// send the request on async background thread and provides a callback after
	public Request sendAsync(final OnResponseCallback cb) {
		final Request req = this;
        new Thread(() -> {
            try {
                req.send();
                Response res = req.getResponse();
                cb.execute(null, res);
            } catch (IOException e) {
                e.printStackTrace();
                // kind of a hack to throw the exception.. callback should check
                // to see if exception is null or not
                this.hasError = true;
                cb.execute(e, null);
            }
        }).start();
		return req;
	}

	// set data to send to server
	public Request setData(String data) {
		this.reqData = data;
		return this;
	}

	// get the response stream from the server
	private InputStream getResponseInputStream() throws IOException {
		if (connection == null) {
			throw new IllegalStateException("Cannot get response InputStream from null connection!");
		}
		InputStream stream = null;

		if (connection.getResponseCode() / 100 == 2) {
			// success
			stream = connection.getInputStream();
		} else {
			// something went wrong
			stream = connection.getErrorStream();
		}
		return stream;
	}

	// add a request property to the connection. Connection will be created if it
	// doesn't exist already
	public Request addRequestProperty(String key, String value) throws IOException {
		if (connection == null) {
			createConnection();
		}
		this.connection.addRequestProperty(key, value);
		return this;
	}

	// create the connection needed to connect to server
	private void createConnection() throws IOException {
		if (this.connection == null) {
			this.connection = (HttpURLConnection)getURL().openConnection();

			this.connection.setRequestMethod(this.method);
			this.connection.setReadTimeout(DEFAULT_READ_TIMEOUT);
			this.connection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
			this.connection.addRequestProperty("Content-Type", "application/json");
			this.connection.setDoOutput(true);
		}
	}

	// creates the correct url based on the data provided to the request
	private URL getURL() throws MalformedURLException {
		URL url = null;

		// replace with params if they exist, otherwise make it blank
		String replacement = (urlParams != null) ? urlParams : "";
		endpoint = endpoint.replaceFirst("/", replacement);

		url = new URL(DOMAIN + endpoint);
        return url;
	}

	// helper method that extracts server response into its own contained object
	private Response createResponse() throws IOException {
		Response response = new Response();
		response.message = this.connection.getResponseMessage();
		response.code = this.connection.getResponseCode();
		response.responseInputStream = getResponseInputStream();
		this.response = response;
		return response;
	}

	public Response getResponse() {
		return this.response;
	}

    public boolean hasError() {
        return hasError;
    }

	// write post data to the connection
	private void writeDataToConnection() throws IOException {
		DataOutputStream wr = new DataOutputStream(this.connection.getOutputStream());
		wr.writeBytes(this.reqData);
		wr.flush();
		wr.close();
	}

	public interface OnResponseCallback {
		void execute(Exception exception, Response response);
	}

	// class for HTTP Response as received from our Request
	public class Response {
		// server response message
		private String message;

		// server response stream
		private InputStream responseInputStream;

		// server response code
		private int code;

		// only construct within Request
		private Response() {}

		public InputStream getResponseInputStream() {
			return responseInputStream;
		}

		public String getMessage() {
			return this.message;
		}

		public int getCode() {
			return this.code;
		}

		// return the response data as a string
		public String getResponseString() throws IOException {
			String result = null;
			BufferedReader br = new BufferedReader(new InputStreamReader(getResponseInputStream()));

			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}

			if (br != null) {
				br.close();
			}
			result = sb.toString();
			return result;
		}
	}
}
