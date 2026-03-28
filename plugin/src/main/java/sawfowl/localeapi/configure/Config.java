package sawfowl.localeapi.configure;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import sawfowl.localeapi.configure.config.ConfigSettings;
import sawfowl.localeapi.configure.config.LocalesSettings;

@ConfigSerializable
public class Config {

	public Config(){}

	@Setting("ConfigSettings")
	private ConfigSettings configSettings = new ConfigSettings();
	@Setting("LocalesSettings")
	private LocalesSettings localesSettings = new LocalesSettings();

	public ConfigSettings getConfigSettings() {
		return configSettings;
	}

	public LocalesSettings getLocalesSettings() {
		return localesSettings;
	}

}
