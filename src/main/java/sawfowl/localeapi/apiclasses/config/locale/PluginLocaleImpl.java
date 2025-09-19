package sawfowl.localeapi.apiclasses.config.locale;

import java.nio.file.Path;
import java.util.Locale;

import org.spongepowered.plugin.PluginContainer;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.config.locale.PluginLocale;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.apiclasses.config.ConfigImpl;

public class PluginLocaleImpl extends ConfigImpl implements PluginLocale {

	public static final PluginLocaleImpl create(PluginContainer plugin, Path configDir, ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, Locale locale) {
		return new PluginLocaleImpl(plugin, configDir, configType, itemStackSerializerType, locale);
	}

	private final Locale locale;
	private PluginLocaleImpl(PluginContainer plugin, Path configDir, ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, Locale locale) {
		super(plugin, configDir, locale.toLanguageTag(), configType, itemStackSerializerType);
		this.locale = locale;
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

}
