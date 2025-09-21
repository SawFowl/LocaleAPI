package sawfowl.localeapi.apiclasses;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.plugin.PluginContainer;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.EnumLocales;
import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.api.LocalesList;
import sawfowl.localeapi.api.Translation;
import sawfowl.localeapi.api.config.locale.PluginLocale;
import sawfowl.localeapi.api.config.locale.ReferencedLocale;
import sawfowl.localeapi.apiclasses.config.locale.PluginLocaleImpl;
import sawfowl.localeapi.apiclasses.config.locale.ReferencedLocaleImpl;
import sawfowl.localeapi.utils.WatchRunner;

public class LocalesListImpl implements LocalesList {

	public static LocalesList create(PluginContainer container, Path localesDir, LocaleService localeService) {
		return new LocalesListImpl(container, localesDir, localeService);
	}

	private Map<Locale, PluginLocale> locales = new HashMap<>();
	private Path path;
	private PluginContainer container;
	private Class<? extends Translation> reference;
	private LocaleService localeService;
	private static final String DOT = ".";
	private LocalesListImpl(PluginContainer container, Path localesDir, LocaleService localeService) {
		this.container = container;
		path = localesDir.resolve(container.metadata().id());
		this.localeService = localeService;
		if(path.toFile().exists() && path.toFile().isDirectory()) for(File file : path.toFile().listFiles()) {
			if(!file.getName().startsWith(DOT) || !file.getName().contains(DOT) || file.getName().endsWith(DOT)) continue;
			String[] nameAndExtension = file.getName().split(DOT);
			if(EnumLocales.isValisTag(nameAndExtension[0]) && ConfigTypes.isValidExtension(nameAndExtension[1])) {
				if(localeService.getDefaultReference(container) == null) {
					createSimpleTranslation(ConfigTypes.getTypeByExtension(nameAndExtension[1]), EnumLocales.find(nameAndExtension[0]));
				} else createReferenceTranslation(ConfigTypes.getTypeByExtension(nameAndExtension[1]), EnumLocales.find(nameAndExtension[0]), localeService.getDefaultReference(container));
			}
		}
	}

	@Override
	public PluginLocale createSimpleTranslation(ConfigTypes configType, Locale locale) {
		locales.put(locale, PluginLocaleImpl.create(container, path, configType, localeService.getItemStackSerializer(container), locale, this));
		return locales.get(locale);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Translation> ReferencedLocale<T> createReferenceTranslation(ConfigTypes configType, Locale locale, Class<T> clazz) {
		locales.put(locale, ReferencedLocaleImpl.create(container, path, configType, localeService.getItemStackSerializer(container), clazz, locale));
		if(reference == null) reference = clazz;
		return (ReferencedLocale<T>) locales.get(locale);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Translation> ReferencedLocale<T> createReferenceTranslation(ConfigTypes configType, Locale locale, T object) {
		locales.put(locale, ReferencedLocaleImpl.create(container, path, configType, localeService.getItemStackSerializer(container), object, locale));
		if(reference == null) reference = object.getClass();
		return (ReferencedLocale<T>) locales.get(locale);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends PluginLocale> T getLocale(Locale locale) throws ClassCastException {
		return (T) (locales.containsKey(locale) ? locales.get(locale) : locales.get(Locales.DEFAULT));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends PluginLocale> T remove(Locale locale) throws ClassCastException {
		return (T) locales.remove(locale);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends PluginLocale> Stream<T> stream() {
		return (Stream<T>) locales.values().stream();
	}

	@Override
	public void forEach(Consumer<? super PluginLocale> action) {
		locales.values().forEach(action);
	}

	@Override
	public boolean contains(Locale locale) {
		return locales.containsKey(locale);
	}

	@Override
	public int size() {
		return locales.size();
	}
	@Override
	public boolean isEmpy() {
		return locales.isEmpty();
	}

	@Override
	public String toString() {
		return "LocalesList[plugin=" + container.metadata().id() + ", path=" + path.toFile().getAbsolutePath() + ", locales=" + locales.keySet().stream().map(Locale::toLanguageTag).toList() + "]";
	}

	public void saveAssetLocales() {
		File localePath = this.path.toFile();
		if(!localePath.exists()) localePath.mkdir();
		for(Locale locale : EnumLocales.getLocales()) saveAssets(locale);
		updateWatch();
	}


	private void saveAssets(Locale locale) {
		for(ConfigTypes configType : ConfigTypes.values()) {
			String configTypeName = configType.toString();
			container.openResource(File.separator + "assets" + File.separator + getPluginID() + File.separator + "lang" + File.separator + locale.toLanguageTag() + configTypeName).ifPresent(inputStream -> {
				File localeFile = path.resolve(locale.toLanguageTag() + configTypeName).toFile();
				if(!localeFile.exists() && !contains(locale)) {
					try {
						Files.copy(inputStream, localeFile.toPath());
						container.logger().info("Locale config " + locale.toLanguageTag() + configTypeName + " for plugin \"" + getPluginID() + "\" has been saved");
						if(reference == null) {
							createSimpleTranslation(configType, locale);
						} else createReferenceTranslation(configType, locale, reference);
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
