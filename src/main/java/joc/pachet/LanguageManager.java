package joc.pachet;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {
	private static ResourceBundle bundle;
	
	public static void setLanguage(String languageCode) {
		Locale selectie = Locale.of(languageCode);
		bundle = ResourceBundle.getBundle("languages.language", selectie);
	}
	
	public static String get(String key) {
		return bundle.getString(key);
	}
}