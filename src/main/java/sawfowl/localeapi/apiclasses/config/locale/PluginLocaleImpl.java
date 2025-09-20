package sawfowl.localeapi.apiclasses.config.locale;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

import org.spongepowered.plugin.PluginContainer;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.LocalesList;
import sawfowl.localeapi.api.Translation;
import sawfowl.localeapi.api.config.locale.PluginLocale;
import sawfowl.localeapi.api.config.locale.ReferencedLocale;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.apiclasses.config.ConfigImpl;

public class PluginLocaleImpl extends ConfigImpl implements PluginLocale {

	public static final PluginLocaleImpl create(PluginContainer plugin, Path configDir, ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, Locale locale, LocalesList localesList) {
		return new PluginLocaleImpl(plugin, configDir, configType, itemStackSerializerType, locale, localesList);
	}

	private final Locale locale;
	private final LocalesList localesList;
	private PluginLocaleImpl(PluginContainer plugin, Path configDir, ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, Locale locale, LocalesList localesList) {
		super(plugin, configDir, locale.toLanguageTag(), configType, itemStackSerializerType);
		this.locale = locale;
		this.localesList = localesList;
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Translation, O extends ReferencedLocale<T>> O toReferenceTranslation(T config) {
		Objects.requireNonNull(config);
		localesList.remove(locale);
		return (O) localesList.createReferenceTranslation(getType(), locale, config);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Translation, O extends ReferencedLocale<T>> O toReferenceTranslation(Class<T> config) {
		localesList.remove(locale);
		return (O) localesList.createReferenceTranslation(getType(), locale, config);
	}

}
