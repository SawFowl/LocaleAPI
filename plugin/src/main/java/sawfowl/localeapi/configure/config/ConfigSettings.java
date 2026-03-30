package sawfowl.localeapi.configure.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.LocalisedComment;

@ConfigSerializable
public class ConfigSettings {

	public ConfigSettings(){}

	@Setting("ConfigType")
	@LocalisedComment(plugin = "localeapi", path = {"ConfigComments", "ConfigType"})
	private ConfigTypes type = ConfigTypes.HOCON;
	@Setting("ForcedUse")
	@LocalisedComment(plugin = "localeapi", path = {"ConfigComments", "ForcedUse"})
	private boolean forcedUse = false;

	public ConfigTypes getType() {
		return type;
	}

	public boolean isForcedUse() {
		return forcedUse;
	}

}
