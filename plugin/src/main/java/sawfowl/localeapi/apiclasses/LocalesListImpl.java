package sawfowl.localeapi.apiclasses;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.plugin.PluginContainer;

import sawfowl.localeapi.LocaleAPI;
import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.EnumLocales;
import sawfowl.localeapi.api.LocalesList;
import sawfowl.localeapi.api.Translation;
import sawfowl.localeapi.api.config.locale.PluginLocale;
import sawfowl.localeapi.api.config.locale.ReferencedLocale;
import sawfowl.localeapi.api.services.LocaleService;
import sawfowl.localeapi.apiclasses.config.locale.PluginLocaleImpl;
import sawfowl.localeapi.apiclasses.config.locale.ReferencedLocaleImpl;
import sawfowl.localeapi.configure.Config;
import sawfowl.localeapi.utils.WatchRunner;

public class LocalesListImpl<T extends Translation> implements LocalesList<T> {

	public static LocalesList<? extends Translation> create(PluginContainer container, Path localesDir, LocaleService localeService) {
		return new LocalesListImpl<>(container, localesDir, localeService);
	}

	private static Config getConfig() {
		return LocaleAPI.getConfig();
	}

	private Map<Locale, PluginLocale> locales = new HashMap<>();
	private Path path;
	private PluginContainer container;
	private Class<T> reference;
	private LocaleService localeService;
	private static final String DOT = ".";
	private LocalesListImpl(PluginContainer container, Path localesDir, LocaleService localeService) {
		this.container = container;
		path = localesDir.resolve(container.metadata().id());
		this.localeService = localeService;
		if(path.toFile().exists() && path.toFile().isDirectory()) for(File file : path.toFile().listFiles()) {
			if(file.getName().startsWith(DOT) || !file.getName().contains(DOT) || file.getName().endsWith(DOT)) continue;
			String[] nameAndExtension = split(file.getName(), '.'); // For some reason, String.split(".") returns an empty array.
			if(EnumLocales.isValisTag(nameAndExtension[0]) && ConfigTypes.isValidExtension(nameAndExtension[1])) {
				if(localeService.getDefaultReference(container) == null) {
					createSimpleTranslation(ConfigTypes.getTypeByExtension(nameAndExtension[1]), EnumLocales.find(nameAndExtension[0]));
				} else createReferencedTranslation(ConfigTypes.getTypeByExtension(nameAndExtension[1]), EnumLocales.find(nameAndExtension[0]), localeService.getDefaultReference(container));
			}
			nameAndExtension = null;
		}
		saveAssetLocales();
	}

	@Override
	public PluginLocale createSimpleTranslation(ConfigTypes configType, Locale locale) {
		Objects.requireNonNull(locale);
		if(configType == null) configType = getConfig() == null ? ConfigTypes.HOCON : getConfig().getLocalesSettings().getType();
		if(getConfig() != null && getConfig().getConfigSettings().isForcedUse() && configType != getConfig().getConfigSettings().getType()) {
			PluginLocale updated = PluginLocaleImpl.create(container, path, getConfig().getConfigSettings().getType(), localeService.getItemStackSerializer(container), locale, this);
			if(path.resolve(locale.toLanguageTag() + configType.toString()).toFile().exists()) {
				PluginLocale old = PluginLocaleImpl.create(container, path, configType, localeService.getItemStackSerializer(container), locale, this);
				try {
					updated.getLoader().save(old.getRootNode());
				} catch (ConfigurateException e) {
					e.printStackTrace();
				}
				old.getPath().toFile().delete();
				old = null;
			}
			if(locales.containsKey(locale)) locales.remove(locale);
			locales.put(locale, updated);
		} else locales.put(locale, PluginLocaleImpl.create(container, path, configType, localeService.getItemStackSerializer(container), locale, this));
		return locales.get(locale);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <O extends T> ReferencedLocale<O> createReferencedTranslation(ConfigTypes configType, Locale locale, Class<O> clazz) {
		Objects.requireNonNull(locale);
		if(configType == null) configType = getConfig() == null ? ConfigTypes.HOCON : getConfig().getLocalesSettings().getType();
		if(getConfig() != null && getConfig().getConfigSettings().isForcedUse() && configType != getConfig().getConfigSettings().getType()) {
			ReferencedLocaleImpl<O> updated = ReferencedLocaleImpl.create(container, path, getConfig().getConfigSettings().getType(), localeService.getItemStackSerializer(container), clazz, locale);
			if(path.resolve(locale.toLanguageTag() + configType.toString()).toFile().exists()) {
				ReferencedLocale<O> old = ReferencedLocaleImpl.create(container, path, configType, localeService.getItemStackSerializer(container), clazz, locale);
				updated.save(old.get());
				old.getPath().toFile().delete();
				old = null;
			}
			if(locales.containsKey(locale)) locales.remove(locale);
			locales.put(locale, updated);
		} else locales.put(locale, ReferencedLocaleImpl.create(container, path, configType, localeService.getItemStackSerializer(container), clazz, locale));
		if(reference == null) reference = (Class<T>) clazz;
		return (ReferencedLocale<O>) locales.get(locale);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <O extends T> ReferencedLocale<O> createReferencedTranslation(ConfigTypes configType, Locale locale, O object) {
		Objects.requireNonNull(locale);
		if(configType == null) configType = getConfig() == null ? ConfigTypes.HOCON : getConfig().getLocalesSettings().getType();
		if(getConfig() != null && getConfig().getConfigSettings().isForcedUse() && configType != getConfig().getConfigSettings().getType()) {
			ReferencedLocaleImpl<O> updated = ReferencedLocaleImpl.create(container, path, getConfig().getConfigSettings().getType(), localeService.getItemStackSerializer(container), object, locale);
			if(path.resolve(locale.toLanguageTag() + configType.toString()).toFile().exists()) {
				ReferencedLocale<O> old = ReferencedLocaleImpl.create(container, path, configType, localeService.getItemStackSerializer(container), object, locale);
				updated.save(old.get());
				old.getPath().toFile().delete();
				old = null;
			}
			if(locales.containsKey(locale)) locales.remove(locale);
			locales.put(locale, updated);
		} else locales.put(locale, ReferencedLocaleImpl.create(container, path, configType, localeService.getItemStackSerializer(container), object, locale));
		if(reference == null) reference = (Class<T>) object.getClass();
		return (ReferencedLocale<O>) locales.get(locale);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <L extends PluginLocale> L getSimple(Locale locale) throws ClassCastException {
		return (L) (locales.containsKey(locale) ? locales.get(locale) : locales.get(Locales.DEFAULT));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <L extends PluginLocale> L remove(Locale locale) throws ClassCastException {
		return (L) locales.remove(locale);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <L extends PluginLocale> Stream<L> stream() {
		return (Stream<L>) locales.values().stream();
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
						} else createReferencedTranslation(configType, locale, reference);
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

	private String[] split(String string, char ch) {
		int off = 0;
		int next;
		ArrayList<String> list = new ArrayList<>();
		while ((next = string.indexOf(ch, off)) != -1) {
			list.add(string.substring(off, next));
			off = next + 1;
		}
		// If no match was found, return this
		if (off == 0) return new String[] {string};

		// Add remaining segment
		list.add(string.substring(off, string.length()));

		// Construct result
		int resultSize = list.size();
		while (resultSize > 0 && list.get(resultSize - 1).isEmpty()) {
			resultSize--;
		}
		return list.subList(0, resultSize).toArray(new String[resultSize]);
	}


}
