package sawfowl.localeapi.apiclasses.services;

import sawfowl.localeapi.api.Logger;
import sawfowl.localeapi.api.services.LoggerService;
import sawfowl.localeapi.apiclasses.SimplifiedApacheLogger;
import sawfowl.localeapi.apiclasses.SimplifiedJavaLogger;

public class LoggerServiceImplement extends LoggerService {

	public LoggerServiceImplement(){}

	@Override
	public Logger createApacheLogger(String name) {
		return new SimplifiedApacheLogger(name);
	}

	@Override
	public Logger createJavaLogger(String name) {
		return new SimplifiedJavaLogger(name);
	}

}
