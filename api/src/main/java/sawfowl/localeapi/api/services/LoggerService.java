package sawfowl.localeapi.api.services;

import com.google.inject.Inject;

import sawfowl.localeapi.api.Logger;

public abstract class LoggerService {

	@Inject
	private static LoggerService INSTANCE;

	/**
	 * Getting the API.<br>
	 * You can use this method in your plugin's constructor if your plugin is loaded after LocaleAPI.
	 */
	public static LoggerService getInstance() {
		return INSTANCE;
	}

	public abstract Logger createApacheLogger(String name);

	public abstract Logger createJavaLogger(String name);

}
