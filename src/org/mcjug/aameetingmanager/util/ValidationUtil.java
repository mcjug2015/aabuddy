package org.mcjug.aameetingmanager.util;


public class ValidationUtil {

	public static boolean isEmailValid(CharSequence email) {
		return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}
}
