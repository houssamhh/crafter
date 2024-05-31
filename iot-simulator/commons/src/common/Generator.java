package common;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Generator {

	private static final String DEFAULT_MESSAGE = "{ \"timestamp\": ${TIMESTAMP}, \"padding\": \"${PADDING}\" }";
	
	public static String generatePassword(final int length) {

		String capitalCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
		String specialCharacters = "!@#$";
		String numbers = "1234567890";
		String combinedChars = capitalCaseLetters + lowerCaseLetters + specialCharacters + numbers;
		Random random = new Random();
		List<Character> password = new ArrayList<Character>();

		password.add(lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length())));
		password.add(capitalCaseLetters.charAt(random.nextInt(capitalCaseLetters.length())));
		password.add(specialCharacters.charAt(random.nextInt(specialCharacters.length())));
		password.add(numbers.charAt(random.nextInt(numbers.length())));

		for (int i = 4; i < length; i++) {
			password.add(combinedChars.charAt(random.nextInt(combinedChars.length())));
		}
		Collections.shuffle(password);
		return password.toString();
	}
	
	/*
	 * Generate message with specific size in bytes
	 */
	public static String generateMessage(int size) {
		String message = DEFAULT_MESSAGE.replace("${TIMESTAMP}", String.valueOf(System.currentTimeMillis()));
		int remainingSize = size - (message.length() - "${PADDING}".length());
		message = message.replace("${PADDING}", "x".repeat(Math.max(0, remainingSize)));
		return message;
	}
}