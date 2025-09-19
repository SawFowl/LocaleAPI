package sawfowl.localeapi.apiclasses;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.spongepowered.plugin.PluginContainer;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.LocalesList;
import sawfowl.localeapi.api.Translation;
import sawfowl.localeapi.api.config.locale.PluginLocale;
import sawfowl.localeapi.api.config.locale.ReferencedLocale;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.apiclasses.config.locale.PluginLocaleImpl;
import sawfowl.localeapi.apiclasses.config.locale.ReferencedLocaleImpl;

public class LocalesListImpl implements LocalesList {

	public static LocalesList create(PluginContainer container, Path localesDir) {
		return LocalesListImpl.create(container, localesDir);
	}

	private Map<Locale, PluginLocale> locales = new HashMap<Locale, PluginLocale>();
	private Path path;
	private PluginContainer container;
	private LocalesListImpl(PluginContainer container, Path localesDir) {
		this.container = container;
		path = localesDir.resolve(container.metadata().id());
	}

	@Override
	public PluginLocale createSimpleTranslation(ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, Locale locale) {
		locales.put(locale, PluginLocaleImpl.create(container, path, configType, itemStackSerializerType, locale));
		return locales.get(locale);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Translation> ReferencedLocale<T> createReferenceTranslation(ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, Locale locale, Class<T> clazz) {
		locales.put(locale, ReferencedLocaleImpl.create(container, path, configType, itemStackSerializerType, clazz, locale));
		return (ReferencedLocale<T>) locales.get(locale);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Translation> ReferencedLocale<T> createReferenceTranslation(ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, Locale locale, T object) {
		locales.put(locale, ReferencedLocaleImpl.create(container, path, configType, itemStackSerializerType, object, locale));
		return (ReferencedLocale<T>) locales.get(locale);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends PluginLocale> T getLocale(Locale locale) throws ClassCastException {
		return (T) locales.get(locale);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends PluginLocale> T remove(Locale locale) throws ClassCastException {
		return (T) locales.remove(locale);
	}

	@Override
	public int size() {
		return locales.size();
	}

}
