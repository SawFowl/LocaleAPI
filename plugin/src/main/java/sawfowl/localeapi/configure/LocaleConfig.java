package sawfowl.localeapi.configure;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import sawfowl.localeapi.api.Translation;
import sawfowl.localeapi.configure.localization.ConfigComments;
import sawfowl.localeapi.configure.localization.LoggerMessages;

@ConfigSerializable
public class LocaleConfig implements Translation {

	public static LocaleConfig createRu() {
		LocaleConfig config = new LocaleConfig();
		config.comments = ConfigComments.createRu();
		config.loggerMessages = LoggerMessages.createRu();
		return config;
	}

	public LocaleConfig(){}

	@Setting("ConfigComments")
	private ConfigComments comments = new ConfigComments();
	@Setting("LoggerMessages")
	private LoggerMessages loggerMessages = new LoggerMessages();

	public ConfigComments getComments() {
		return comments;
	}

	public LoggerMessages getLoggerMessages() {
		return loggerMessages;
	}

}
