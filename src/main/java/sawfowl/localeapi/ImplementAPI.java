package sawfowl.localeapi;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppedGameEvent;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.plugin.PluginContainer;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.EnumLocales;
import sawfowl.localeapi.api.Translation;
import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.api.Logger;
import sawfowl.localeapi.api.PluginLocale;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.api.serializetools.SerializeOptions;
import sawfowl.localeapi.apiclasses.AbstractLocale;
import sawfowl.localeapi.apiclasses.HoconLocale;
import sawfowl.localeapi.apiclasses.JsonLocale;
import sawfowl.localeapi.apiclasses.LegacyLocale;
import sawfowl.localeapi.apiclasses.YamlLocale;
import sawfowl.localeapi.utils.WatchRunner;

public class ImplementAPI {

	private static LocaleService service;

	public static LocaleService getLocaleService() {
		return service;
	}

	API create(Logger logger, Path path) {
		return new API(logger, path);
	}

	static void stopWatch() {
		((API) service).watchThread.stopWatch();
	}

	class API implements LocaleService {

		private Map<String, Map<Locale, PluginLocale>> pluginLocales;
		private Map<String, ItemStackSerializerType> stackSerializers;
		private Map<String, Class<? extends Translation>> defaultReferences;
		private List<Locale> locales;
		private WatchRunner watchThread;
		private final Path configDirectory;
		private final Logger logger;
		private Locale system = Locale.getDefault();
		private boolean allowSystem = false;
		API(Logger logger, Path path) {
			setInstaice();
			this.logger = logger;
			configDirectory = path;
			pluginLocales = new HashMap<String, Map<Locale, PluginLocale>>();
			stackSerializers = new HashMap<String, ItemStackSerializerType>();
			defaultReferences = new HashMap<String, Class<? extends Translation>>();
			locales = EnumLocales.getLocales();
			WatchRunner.createInstance(this, logger, path);
			watchThread = WatchRunner.getInstance();
			allowSystem = locales.contains(system) || locales.stream().filter(locale -> (locale.toLanguageTag().equals(system.toLanguageTag()))).findFirst().isPresent();
			Sponge.eventManager().registerListeners(LocaleAPI.getPluginContainer(), this, MethodHandles.publicLookup());
		}

		private void setInstaice() {
			service = this;
		}

		private void updateWatch(PluginContainer pluginID) {
			WatchRunner.initPlugin(pluginID);
		}

		private String getPluginID(PluginContainer plugin) {
			return plugin.metadata().id();
		}

		private void saveAssets(String pluginID, Locale locale) {
			if(pluginID == null || pluginID.isEmpty()) {
				logger.error("Plugin can not be null or noname(\"\")");
				return;
			}
			Optional<PluginContainer> optPluginContainer = Sponge.pluginManager().plugin(pluginID);
			if(optPluginContainer.isPresent()) {
				PluginContainer pluginContainer = optPluginContainer.get();
				for(ConfigTypes configType : ConfigTypes.values()) {
					String configTypeName = configType.toString();
					pluginContainer.openResource(File.separator + "assets" + File.separator + pluginID + File.separator + "lang" + File.separator + locale.toLanguageTag() + configTypeName).ifPresent(inputStream -> {
						File localeFile = configDirectory.resolve(pluginID + File.separator + locale.toLanguageTag() + configTypeName).toFile();
						if(!localeFile.exists()) {
							try {
								Files.copy(inputStream, localeFile.toPath());
								logger.info("Locale config " + locale.toLanguageTag() + configTypeName + " for plugin \"" + pluginID + "\" has been saved");
							} catch (IOException e) {
								logger.error(e.getLocalizedMessage());
							}
						}
					});
				}
			} else {
				logger.error("Could not find PluginContainer for plugin " + pluginID);
			}
		}

		private void addPluginLocale(String pluginID, Locale locale, AbstractLocale localeUtil) {
			if(!pluginLocales.get(pluginID).containsKey(locale)) pluginLocales.get(pluginID).put(locale, localeUtil);
		}

		public Locale getSystemOrDefaultLocale() {
			return allowSystem ? system : getDefaultLocale();
		}

		public List<Locale> getLocalesList() {
			return locales;
		}

		public Locale getDefaultLocale() {
			return Locales.DEFAULT;
		}

		public Map<Locale, PluginLocale> getPluginLocales(PluginContainer plugin) {
			return getPluginLocales(getPluginID(plugin));
		}

		public Map<Locale, PluginLocale> getPluginLocales(String pluginID) {
			if(pluginID == null || pluginID.isEmpty()) {
				logger.error("Plugin can not be null or noname(\"\")");
				return null;
			}
			return pluginLocales.containsKey(pluginID) ? pluginLocales.get(pluginID) : new HashMap<Locale, PluginLocale>();
		}

		public PluginLocale getOrDefaultLocale(PluginContainer plugin, Locale locale) {
			return getOrDefaultLocale(getPluginID(plugin), locale);
		}

		public PluginLocale getOrDefaultLocale(String pluginID, Locale locale) {
			if(pluginID == null || pluginID.isEmpty()) {
				logger.error("Plugin can not be null or noname(\"\")");
				return null;
			}
			return getPluginLocales(pluginID).containsKey(locale) ? getPluginLocales(pluginID).get(locale) : getPluginLocales(pluginID).get(Locales.DEFAULT);
		}

		public void saveAssetLocales(PluginContainer plugin) {
			String pluginID = getPluginID(plugin);
			saveAssetLocales(pluginID);
			updateWatch(plugin);
		}

		public void saveAssetLocales(String pluginID) {
			if(pluginID == null || pluginID.isEmpty()) {
				logger.error("Plugin can not be null or noname(\"\")");
				return;
			}
			File localePath = new File(this.configDirectory + File.separator + pluginID);
			if(!localePath.exists()) localePath.mkdir();
			if(!pluginLocales.containsKey(pluginID)) pluginLocales.put(pluginID, new HashMap<Locale, PluginLocale>());
			for(Locale locale : this.locales) saveAssets(pluginID, locale);
			localesExist(pluginID);
			Sponge.pluginManager().plugin(pluginID).ifPresent(plugin -> updateWatch(plugin));
		}

		public PluginLocale createPluginLocale(PluginContainer plugin, ConfigTypes configType, Locale locale) {
			return createPluginLocale(getPluginID(plugin), configType, locale);
		}

		public PluginLocale createPluginLocale(String pluginID, ConfigTypes configType, Locale locale) {
			if(pluginID == null || pluginID.isEmpty()) {
				this.logger.error("Plugin can not be null or noname(\"\")");
				return null;
			}
			if(!configDirectory.resolve(pluginID).toFile().exists()) configDirectory.resolve(pluginID).toFile().mkdir();
			if(!pluginLocales.containsKey(pluginID)) pluginLocales.put(pluginID, new HashMap<Locale, PluginLocale>());
			if(getPluginLocales(pluginID).containsKey(locale)) return getPluginLocales(pluginID).get(locale);
			if(configType.equals(ConfigTypes.HOCON)) {
				addPluginLocale(pluginID, locale, new HoconLocale(this, logger, configDirectory, pluginID, locale.toLanguageTag()));
			} else if(configType.equals(ConfigTypes.JSON)) {
				addPluginLocale(pluginID, locale, new JsonLocale(this, logger, configDirectory, pluginID, locale.toLanguageTag()));
			} else if(configType.equals(ConfigTypes.YAML)) {
				addPluginLocale(pluginID, locale, new YamlLocale(this, logger, configDirectory, pluginID, locale.toLanguageTag()));
			} else if(configType.equals(ConfigTypes.PROPERTIES)) {
				addPluginLocale(pluginID, locale, new LegacyLocale(this, logger, configDirectory, pluginID, locale.toLanguageTag()));
			}
			Sponge.pluginManager().plugin(pluginID).ifPresent(plugin -> updateWatch(plugin));
			return getPluginLocales(pluginID).get(locale);
		}

		public boolean localesExist(PluginContainer plugin) {
			return localesExist(getPluginID(plugin));
		}

		public boolean localesExist(String pluginID) {
			if(pluginID == null || pluginID.isEmpty()) {
				this.logger.error("Plugin can not be null or noname(\"\")");
				return false;
			}
			for(Locale locale : locales) {
				if(configDirectory.resolve(pluginID + File.separator + locale.toLanguageTag() + ".conf").toFile().exists() && SerializeOptions.createHoconConfigurationLoader(getItemStackSerializer(pluginID)).path(configDirectory.resolve(pluginID + File.separator + locale.toLanguageTag() + ".conf")).build().canLoad()) {
					createPluginLocale(pluginID, ConfigTypes.HOCON, locale);
				} else if(configDirectory.resolve(pluginID + File.separator + locale.toLanguageTag() + ".json").toFile().exists() && 
						SerializeOptions.createJsonConfigurationLoader(
								getItemStackSerializer(pluginID))
						.path(configDirectory.resolve(pluginID + File.separator + locale.toLanguageTag() + ".json")).build().canLoad()) {
					createPluginLocale(pluginID, ConfigTypes.JSON, locale);
				} else if(configDirectory.resolve(pluginID + File.separator + locale.toLanguageTag() + ".yml").toFile().exists() && SerializeOptions.createYamlConfigurationLoader(getItemStackSerializer(pluginID)).path(configDirectory.resolve(pluginID + File.separator + locale.toLanguageTag() + ".yml")).build().canLoad()) {
					createPluginLocale(pluginID, ConfigTypes.YAML, locale);
				} else if(configDirectory.resolve(pluginID + File.separator + locale.toLanguageTag() + ".properties").toFile().exists()) {
					createPluginLocale(pluginID, ConfigTypes.PROPERTIES, locale);
				}
			}
			return pluginLocales.containsKey(pluginID) && pluginLocales.get(pluginID).containsKey(Locales.DEFAULT);
		}

		@Listener(order = Order.LAST)
		public void onCompleteLoad(StartedEngineEvent<Server> event) {
			watchThread.enable();
		}

		@Listener
		public void stopWatch(StoppedGameEvent event) {
			if(event == null) return;
			watchThread.stopWatch();
		}

		@Override
		public void setItemStackSerializerVariant(PluginContainer container, ItemStackSerializerType variant) throws Exception {
			Objects.requireNonNull(container);
			Objects.requireNonNull(variant);
			if(stackSerializers.containsKey(container.metadata().id())) stackSerializers.remove(container.metadata().id());
			stackSerializers.put(container.metadata().id(), variant);
		}

		@Override
		public ItemStackSerializerType getItemStackSerializer(PluginContainer container) {
			Objects.requireNonNull(container);
			return getItemStackSerializer(container.metadata().id());
		}

		public ItemStackSerializerType getItemStackSerializer(String plugin) {
			return stackSerializers.getOrDefault(plugin, ItemStackSerializerType.SPONGE);
		}

		@Override
		public <T extends Translation> void setDefaultReference(PluginContainer container, Class<T> defaultReference) {
			if(defaultReferences.containsKey(container.metadata().id())) defaultReferences.remove(container.metadata().id());
			defaultReferences.put(container.metadata().id(), defaultReference);
		}

		@Override
		public Class<? extends Translation> getDefaultReference(PluginContainer container) {
			return getDefaultReference(container.metadata().id());
		}

		@Override
		public Class<? extends Translation> getDefaultReference(String pluginID) {
			return defaultReferences.containsKey(pluginID) ? defaultReferences.get(pluginID) : null;
		}

		void startWatch() {
			watchThread.run();
		}

	}

}
