package org.mcjug.aameetingmanager.util;

import java.util.regex.Pattern;

public class ValidationUtil {

	//Copied from android.util.Patterns.EMAIL_ADDRESS, since API 7 does not have it
	public static final Pattern EMAIL_ADDRESS 
		= Pattern.compile(
				"[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
				"\\@" +
				"[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
				"(" +
				"\\." +
				"[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
				")+"
		);
	
	public static boolean isEmailValid(CharSequence email) {
		return EMAIL_ADDRESS.matcher(email).matches();
	}
}
