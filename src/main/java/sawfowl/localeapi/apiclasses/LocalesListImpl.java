package sawfowl.localeapi.apiclasses;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.spongepowered.plugin.PluginContainer;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.EnumLocales;
import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.api.LocalesList;
import sawfowl.localeapi.api.Translation;
import sawfowl.localeapi.api.config.locale.PluginLocale;
import sawfowl.localeapi.api.config.locale.ReferencedLocale;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.apiclasses.config.locale.PluginLocaleImpl;
import sawfowl.localeapi.apiclasses.config.locale.ReferencedLocaleImpl;
import sawfowl.localeapi.utils.WatchRunner;

public class LocalesListImpl implements LocalesList {

	public static LocalesList create(PluginContainer container, Path localesDir, LocaleService localeService) {
		return new LocalesListImpl(container, localesDir, localeService);
	}

	private Map<Locale, PluginLocale> locales = new HashMap<Locale, PluginLocale>();
	private Path path;
	private PluginContainer container;
	private Class<? extends Translation> reference;
	private LocaleService localeService;
	private LocalesListImpl(PluginContainer container, Path localesDir, LocaleService localeService) {
		this.container = container;
		path = localesDir.resolve(container.metadata().id());
		this.localeService = localeService;
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
		if(reference == null) reference = clazz;
		return (ReferencedLocale<T>) locales.get(locale);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Translation> ReferencedLocale<T> createReferenceTranslation(ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, Locale locale, T object) {
		locales.put(locale, ReferencedLocaleImpl.create(container, path, configType, itemStackSerializerType, object, locale));
		if(reference == null) reference = object.getClass();
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
	public boolean contains(Locale locale) {
		return locales.containsKey(locale);
	}

	@Override
	public int size() {
		return locales.size();
	}

	public void saveAssetLocales() {
		File localePath = new File(this.path + File.separator + getPluginID());
		if(!localePath.exists()) localePath.mkdir();
		for(Locale locale : EnumLocales.getLocales()) saveAssets(locale);
		updateWatch();
	}


	private void saveAssets(Locale locale) {
		for(ConfigTypes configType : ConfigTypes.values()) {
			String configTypeName = configType.toString();
			container.openResource(File.separator + "assets" + File.separator + getPluginID() + File.separator + "lang" + File.separator + locale.toLanguageTag() + configTypeName).ifPresent(inputStream -> {
				File localeFile = path.resolve(getPluginID() + File.separator + locale.toLanguageTag() + configTypeName).toFile();
				if(!localeFile.exists() && !contains(locale)) {
					try {
						Files.copy(inputStream, localeFile.toPath());
						container.logger().info("Locale config " + locale.toLanguageTag() + configTypeName + " for plugin \"" + getPluginID() + "\" has been saved");
						if(reference == null) {
							createSimpleTranslation(configType, localeService.getItemStackSerializer(container), locale);
						} else createReferenceTranslation(configType, localeService.getItemStackSerializer(container), locale, reference);
					} catch (IOException e) {
						container.logger().error(e.getLocalizedMessage());
					}
				}
			});
		}
	}


	private void updateWatch() {
		WatchRunner.initPlugin(container);
	}

	private String getPluginID() {
		return container.metadata().id();
	}


}
