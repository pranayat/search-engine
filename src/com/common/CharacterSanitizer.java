package com.common;

public class CharacterSanitizer {

	public static String sanitize(String inputString) {
		return inputString.replaceAll("[^A-Za-z0-9äüö ]+", "");
	}
}
