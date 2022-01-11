package com.crawler;

import java.util.Arrays;

// Implemented as per MDN spec - https://developer.mozilla.org/en-US/docs/Learn/Common_questions/What_is_a_URL
public class Url {
	private String protocol;
	private String domain;
	private String path;

	public Url(String inputUrlString, Url parentUrl) throws Exception {
		String[] urlSegments = {};

		// www.wikipedia.com/content
		if (inputUrlString.length() >= 4 && inputUrlString.substring(0, 4).equals("www.")) {
			urlSegments = inputUrlString.split("/");
			if (parentUrl != null && parentUrl.getProtocol().length() > 0) {
				this.protocol = parentUrl.getProtocol();
			} else {
				this.protocol = "https://";
			}
			this.domain = urlSegments[0];
			this.path = String.join("/", Arrays.copyOfRange(urlSegments, 1, urlSegments.length));
		}

		// https://wikipedia.com/content
		else if (inputUrlString.length() >= 8 && inputUrlString.substring(0, 8).equals("https://")) {
			urlSegments = inputUrlString.substring(8, inputUrlString.length()).split("/");
			this.protocol = "https://";
			this.domain = urlSegments[0];
			this.path = String.join("/", Arrays.copyOfRange(urlSegments, 1, urlSegments.length));
		}

		// http://wikipedia.com/content
		else if (inputUrlString.length() >= 7 && inputUrlString.substring(0, 7).equals("http://")) {
			urlSegments = inputUrlString.substring(7, inputUrlString.length()).split("/");
			this.protocol = "https://"; // normalize all to https
			this.domain = urlSegments[0];
			this.path = String.join("/", Arrays.copyOfRange(urlSegments, 1, urlSegments.length));
		}

		// Implicit protocol
		// Eg. //developer.mozilla.org/en-US/docs/Learn
		else if (inputUrlString.length() >= 2 && inputUrlString.substring(0, 2).equals("//")) {
			urlSegments = inputUrlString.substring(2, inputUrlString.length()).split("/");

			if (parentUrl != null && parentUrl.getProtocol().length() > 0) {
				// should ideally not land here for implicit protocol urls
				this.protocol = parentUrl.getProtocol();
			} else {
				this.protocol = "https://";
			}

			this.domain = urlSegments[0];
			this.path = String.join("/", Arrays.copyOfRange(urlSegments, 1, urlSegments.length));
		}

		else if (inputUrlString.equals("/")) {
			if (parentUrl != null && parentUrl.getProtocol().length() > 0) {
				// should ideally not land here for implicit protocol urls
				this.protocol = parentUrl.getProtocol();
			} else {
				this.protocol = "https://";
			}

			if (parentUrl.getDomain().length() > 0) {
				this.domain = parentUrl.getDomain();
			} else {
				this.domain = "wikipedia.com"; // something has gone wrong
			}

			this.path = "";
		}
		// Implicit domain and protocol
		// Eg. /en-US/docs/Learn
		else if (inputUrlString.length() >= 1 && inputUrlString.substring(0, 1).equals("/")) {
			urlSegments = inputUrlString.split("/");

			if (parentUrl != null && parentUrl.getProtocol().length() > 0) {
				// should ideally not land here for implicit protocol urls
				this.protocol = parentUrl.getProtocol();
			} else {
				this.protocol = "https://";
			}

			if (parentUrl.getDomain().length() > 0) {
				this.domain = parentUrl.getDomain();
			} else {
				this.domain = "wikipedia.com"; // something has gone wrong
			}

			this.path = String.join("/", Arrays.copyOfRange(urlSegments, 1, urlSegments.length));
		} 
		// directory style relative paths ../../custom/kaiserslautern/img/logo.png
		else if (inputUrlString.length() > 2 && inputUrlString.startsWith("../")) {
			urlSegments = inputUrlString.split("/");
			String[] parentUrlSegments = parentUrl.getPath().split("/");
			this.domain = parentUrl.getDomain();
			this.protocol = parentUrl.getProtocol();

			String[] absUrlSegments = {};

			int goUpLevel = 0;
			for (String seg : urlSegments) {
				if (seg.equals("..")) {
					goUpLevel += 1;
				}
			}

			this.path = String.join("/",
					Arrays.copyOfRange(parentUrlSegments, 0, parentUrlSegments.length - goUpLevel - 1)) + "/"
					+ String.join("/", Arrays.copyOfRange(urlSegments, goUpLevel, urlSegments.length));
		} else {
			throw new Exception("Invalid URL string");
		}
	}

	public String getUrlString() {
		if (!(this.path.length() > 0)) {
			return this.protocol + this.domain;
		}

		if (domain.endsWith("/")) {
			domain = domain.substring(0, domain.length() - 1);
		}

		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}

		if (domain.startsWith("/")) {
			domain = domain.substring(1, domain.length());
		}

		if (path.startsWith("/")) {
			path = path.substring(1, path.length());
		}

		return this.protocol + this.domain + "/" + this.path;
	}

	public String getProtocol() {
		return this.protocol;
	}

	public String getDomain() {
		return this.domain;
	}

	public String getPath() {
		return this.path;
	}
}
