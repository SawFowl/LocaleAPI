package sawfowl.localeapi.configure;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import sawfowl.localeapi.api.Translation;
import sawfowl.localeapi.configure.localization.ConfigComments;

@ConfigSerializable
public class LocaleConfig implements Translation {

	public static LocaleConfig createRu() {
		LocaleConfig config = new LocaleConfig();
		config.comments = ConfigComments.createRu();
		return config;
	}

	public LocaleConfig(){}

	@Setting("ConfigComments")
	private ConfigComments comments = new ConfigComments();

	public ConfigComments getComments() {
		return comments;
	}

}
