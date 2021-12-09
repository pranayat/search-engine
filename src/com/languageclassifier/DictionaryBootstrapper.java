package com.languageclassifier;

import java.io.IOException;
import java.sql.SQLException;

public class DictionaryBootstrapper implements Runnable {
	
	private String language;
	
	public DictionaryBootstrapper(String language) {
		this.language = language;
	}
	
	public void run () {
		try {
			LanguageClassifier.bootstrapClassifierForLanguage(this.language);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
