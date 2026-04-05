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
import sawfowl.localeapi.apiclasses.config.ConfigImpl;
import sawfowl.localeapi.apiclasses.config.locale.PluginLocaleImpl;
import sawfowl.localeapi.apiclasses.config.locale.ReferencedLocaleImpl;
import sawfowl.localeapi.configure.Config;
import sawfowl.localeapi.utils.WatchRunner;

public class LocalesListImpl<T extends Translation> implements LocalesList<T> {

	public static LocalesListImpl<? extends Translation> create(PluginContainer container, Path localesDir, LocaleService localeService) {
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
	private LocalesListImpl(PluginContainer container, Path configDirectory, LocaleService localeService) {
		this.container = container;
		if(getConfig() != null) {
			if(getConfig().getLocalesSettings(container).getPath().contains("{LOCALEAPI_PATH}")) {
				if(getConfig().getLocalesSettings(container).getPath().contains("{PATH_SEPARATOR}")) {
					if(getConfig().getLocalesSettings(container).getPath().endsWith("{PATH_SEPARATOR}")) {
						path = Path.of(getConfig().getLocalesSettings(container).getPath().replace("{LOCALEAPI_PATH}", LocaleAPI.getLocaleAPIConfigDir()).replace("{PATH_SEPARATOR}", File.separator) + container.metadata().id());
					} else path = Path.of(getConfig().getLocalesSettings(container).getPath().replace("{LOCALEAPI_PATH}", LocaleAPI.getLocaleAPIConfigDir()).replace("{PATH_SEPARATOR}", File.separator) + File.separator + container.metadata().id());
				} else path = Path.of(getConfig().getLocalesSettings(container).getPath().replace("{LOCALEAPI_PATH}", LocaleAPI.getLocaleAPIConfigDir() + File.separator + container.metadata().id()));
			} else if(getConfig().getLocalesSettings(container).getPath().contains("{PLUGIN_CONFIG_PATH}")) {
				path = Path.of(getConfig().getLocalesSettings(container).getPath().replace("{PLUGIN_CONFIG_PATH}", LocaleAPI.getMainConfigDir() + File.separator + container.metadata().id()).replace("{PATH_SEPARATOR}", File.separator));
			} else path = configDirectory.resolve(container.metadata().id());
		} else path = configDirectory.resolve(container.metadata().id());
		createFolders(path, path.toFile());
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
		WatchRunner.pause();
		if(configType == null) configType = getConfig() == null ? ConfigTypes.HOCON : getConfig().getLocalesSettings(container).getType();
		if(getConfig() != null && getConfig().getLocalesSettings(container).isForcedUse() && !configType.comparableType(getConfig().getLocalesSettings(container).getType())) {
			if(path.resolve(locale.toLanguageTag() + getConfig().getLocalesSettings(container).getType().toString()).toFile().exists()) {
				locales.put(locale, PluginLocaleImpl.create(path, getConfig().getLocalesSettings(container).getType(), localeService.getItemStackSerializer(container), locale, this));
				return locales.get(locale); 
			}
			PluginLocale updated = PluginLocaleImpl.create(path, getConfig().getLocalesSettings(container).getType(), localeService.getItemStackSerializer(container), locale, this);
			PluginLocale old = null;
			for(File file : path.toFile().listFiles()) {
				if(!file.getName().contains(locale.toLanguageTag())) continue;
				ConfigTypes type = ConfigTypes.getTypeByExtension(ConfigTypes.getExtension(file.getName()));
				if(type  == ConfigTypes.UNKNOWN || type.comparableType(getConfig().getLocalesSettings(container).getType())) continue;
				if(old == null) {
					old = PluginLocaleImpl.create(path, configType, localeService.getItemStackSerializer(container), locale, this);
					try {
						updated.getLoader().save(old.getRootNode());
						old.getPath().toFile().delete();
						old = null;
					} catch (ConfigurateException e) {
						e.printStackTrace();
						updated.getPath().toFile().delete();
					}
				} else file.delete();
			}
			if(locales.containsKey(locale)) locales.remove(locale);
			locales.put(locale, updated);
		} else locales.put(locale, PluginLocaleImpl.create(path, configType, localeService.getItemStackSerializer(container), locale, this));
		WatchRunner.pause();
		return locales.get(locale);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <O extends T> ReferencedLocale<O> createReferencedTranslation(ConfigTypes configType, Locale locale, Class<O> clazz) {
		Objects.requireNonNull(locale);
		Objects.requireNonNull(clazz);
		WatchRunner.pause();
		if(reference == null) reference = (Class<T>) clazz;
		if(configType == null) configType = getConfig() == null ? ConfigTypes.HOCON : getConfig().getLocalesSettings(container).getType();
		if(getConfig() != null && getConfig().getLocalesSettings(container).isForcedUse()) {
			if(path.resolve(locale.toLanguageTag() + getConfig().getLocalesSettings(container).getType().toString()).toFile().exists()) {
				locales.put(locale, ReferencedLocaleImpl.create(path, getConfig().getLocalesSettings(container).getType(), localeService.getItemStackSerializer(container), clazz, locale));
				return (ReferencedLocale<O>) locales.get(locale); 
			}
			ReferencedLocaleImpl<O> updated = ReferencedLocaleImpl.create(path, getConfig().getLocalesSettings(container).getType(), localeService.getItemStackSerializer(container), clazz, locale);
			ConfigImpl old = null;
			for(File file : path.toFile().listFiles()) {
				if(!file.getName().contains(locale.toLanguageTag())) continue;
				ConfigTypes type = ConfigTypes.getTypeByExtension(ConfigTypes.getExtension(file.getName()));
				if(type  == ConfigTypes.UNKNOWN || type.comparableType(getConfig().getLocalesSettings(container).getType())) continue;
				if(old == null) {
					old = ConfigImpl.create(path, locale.toLanguageTag(), configType, localeService.getItemStackSerializer(container), updated.getSerializers());
					//old = ReferencedLocaleImpl.create(path, configType, localeService.getItemStackSerializer(container), clazz, locale);
					old.load();
					//updated.save(old.get());
					try {
						updated.getLoader().save(old.getRootNode());
						old.getPath().toFile().delete();
						old = null;
					} catch (ConfigurateException e) {
						e.printStackTrace();
						updated.getPath().toFile().delete();
					}
				} else file.delete();
			}
			if(locales.containsKey(locale)) locales.remove(locale);
			locales.put(locale, updated);
		} else locales.put(locale, ReferencedLocaleImpl.create(path, configType, localeService.getItemStackSerializer(container), clazz, locale));
		if(reference == null) reference = (Class<T>) clazz;
		WatchRunner.pause();
		return (ReferencedLocale<O>) locales.get(locale);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <O extends T> ReferencedLocale<O> createReferencedTranslation(ConfigTypes configType, Locale locale, O object) {
		Objects.requireNonNull(locale);
		Objects.requireNonNull(object);
		WatchRunner.pause();
		if(reference == null) reference = (Class<T>) object.getClass();
		if(configType == null) configType = getConfig() == null ? ConfigTypes.HOCON : getConfig().getLocalesSettings(container).getType();
		if(getConfig() != null && getConfig().getLocalesSettings(container).isForcedUse()) {
			if(path.resolve(locale.toLanguageTag() + getConfig().getLocalesSettings(container).getType().toString()).toFile().exists()) {
				locales.put(locale, ReferencedLocaleImpl.create(path, getConfig().getLocalesSettings(container).getType(), localeService.getItemStackSerializer(container), object, locale));
				return (ReferencedLocale<O>) locales.get(locale); 
			}
			ReferencedLocaleImpl<O> updated = ReferencedLocaleImpl.create(path, getConfig().getLocalesSettings(container).getType(), localeService.getItemStackSerializer(container), object, locale);
			ReferencedLocale<O> old = null;
			for(File file : path.toFile().listFiles()) {
				if(!file.getName().contains(locale.toLanguageTag())) continue;
				ConfigTypes type = ConfigTypes.getTypeByExtension(ConfigTypes.getExtension(file.getName()));
				if(type  == ConfigTypes.UNKNOWN || type.comparableType(getConfig().getLocalesSettings(container).getType())) continue;
				if(old == null) {
					old = ReferencedLocaleImpl.create(path, configType, localeService.getItemStackSerializer(container), object, locale);
					old.load();
					updated.save(old.get());
					try {
						updated.getLoader().save(old.getRootNode());
						old.getPath().toFile().delete();
						old = null;
					} catch (ConfigurateException e) {
						e.printStackTrace();
						updated.getPath().toFile().delete();
					}
				} else file.delete();
			}
			if(locales.containsKey(locale)) locales.remove(locale);
			locales.put(locale, updated);
		} else locales.put(locale, ReferencedLocaleImpl.create(path, configType, localeService.getItemStackSerializer(container), object, locale));
		if(reference == null) reference = (Class<T>) object.getClass();
		WatchRunner.pause();
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

	public Path getPath() {
		return path;
	}

	private void saveAssets(Locale locale) {
		for(ConfigTypes configType : ConfigTypes.values()) {
			String configTypeName = configType.toString();
			container.openResource(File.separator + "assets" + File.separator + getPluginID() + File.separator + "lang" + File.separator + locale.toLanguageTag() + configTypeName).ifPresent(inputStream -> {
				File localeFile = path.resolve(locale.toLanguageTag() + configTypeName).toFile();
				if(!localeFile.exists() && !contains(locale)) {
					try {
						Files.copy(inputStream, localeFile.toPath());
						container.logger().info(LocaleAPI.getLocales().getSystemAsReferenced().getLoggerMessages().getSaveAsset(locale, configType, getPluginID()));
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
		WatchRunner.initPlugin(container, path);
	}

	private String getPluginID() {
		return container.metadata().id();
	}

	private void createFolders(Path path, File file) {
		if(!file.exists() && path.getParent() != null) {
			createFolders(path.getParent(), path.toFile());
			file.mkdir();
		}
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
