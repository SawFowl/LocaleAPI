package sawfowl.localeapi.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.plugin.PluginContainer;

import sawfowl.localeapi.LocaleAPI;
import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.api.PluginLocale;
import sawfowl.localeapi.api.event.LocaleEvent;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class WatchLocales {

	private LocaleService localeService;
	private Logger logger;
	private Path configDirectory;
	private WatchService watchService;
	private Cause cause;
	private PluginContainer pluginContainer;
	public WatchLocales(LocaleService localeService, Logger logger, Path path) {
		this.localeService = localeService;
		this.logger = logger;
		pluginContainer = LocaleAPI.getPluginContainer();
		cause = Cause.of(EventContext.builder().add(EventContextKeys.PLUGIN, pluginContainer).build(), pluginContainer);
		configDirectory = path;
		try {
			watchService = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
		};
	}

	public void addPluginData(String pluginID) {
		if(getUpdated().containsKey(pluginID)) return;
		if(!configDirectory.resolve(pluginID).toFile().exists()) configDirectory.resolve(pluginID).toFile().mkdir();
		getUpdated().put(pluginID, TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
		try {
			configDirectory.resolve(pluginID).register(watchService, ENTRY_CREATE, ENTRY_MODIFY);
			logger.info("[FileWatcher] Added tracking of localization files for plugin: " + pluginID);
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
		}
	}

	void startWatch() {
		WatchKey key;
		try {
			Thread.sleep(5000);
			while(watchService != null && (key = watchService.take()) != null) {
				for(WatchEvent<?> event : key.pollEvents()) {
					String pluginID = key.watchable().toString().replace(configDirectory.toString() + File.separator, "");
					String fileName = event.context().toString();
					if(event.kind() == ENTRY_CREATE) {
						checkOnCreate(pluginID, fileName);
					} else if(event.kind() == ENTRY_MODIFY) {
						checkOnModify(pluginID, fileName);
					}
				}
				if(!key.reset()) {
					break;
				}
			}
		} catch (InterruptedException e) {
			if(watchService != null) logger.error(e.getLocalizedMessage());
		}
	}

	void stopWatch() {
		watchService = null;
	}

	private void checkOnCreate(String pluginID, String fileName) {
		if(fileName.contains("tmp")) {
			return;
		}
		long oldTime = System.currentTimeMillis();
		for(Locale locale : localeService.getLocalesList()) {
			if(fileName.contains(locale.toLanguageTag())) {
				if((getUpdated().containsKey(pluginID) && getUpdated().get(pluginID) + 5 > oldTime) || localeService.getPluginLocales(pluginID).containsKey(locale)) return;
				for(ConfigTypes configType : ConfigTypes.values()) {
					String configTypeName = configType.toString();
					if(fileName.contains(configTypeName)) {
						if(configType == ConfigTypes.PROPERTIES && localeService.getDefaultReference(pluginID) != null) {
							logger.warn("[FileWatcher] The \"" + fileName + "\" localization for the \"" + pluginID + "\" plugin cannot be loaded, because the plugin uses \"Reference\" localization classes.");
							return;
						}
						int localesSizeBefore = localeService.getPluginLocales(pluginID).size();
						logger.info("[FileWatcher] New locale file found -> \"" + fileName + "\" for plugin \"" + pluginID + "\"! Loading...");
						PluginLocale localeconfig = localeService.createPluginLocale(pluginID, configType, locale);
						class CreateLocaleEvent extends AbstractEvent implements LocaleEvent.Create {

							@Override
							public String plugin() {
								return pluginID;
							}

							@Override
							public Locale getLocale() {
								return locale;
							}

							@Override
							public Cause cause() {
								return cause;
							}

							@Override
							public ConfigTypes configType() {
								return configType;
							}

							@Override
							public PluginLocale getLocaleConfig() {
								return localeconfig;
							}
							
						}
						postEvent(new CreateLocaleEvent());
						if(localesSizeBefore < localeService.getPluginLocales(pluginID).size()) {
							logger.info("[FileWatcher] Done. " + (System.currentTimeMillis() - oldTime) + "ms");
						} else {
							logger.error("[FileWatcher] Error loading file \"" + fileName + "\" for plugin \"" + pluginID + "\"!");
						}
					}
				}
				getUpdated().put(pluginID + locale.toLanguageTag(), TimeUnit.MILLISECONDS.toSeconds(oldTime));
				remove(pluginID + locale.toLanguageTag());
				break;
			}
		}
	}

	private void checkOnModify(String pluginID, String fileName) {
		if(fileName.contains("tmp")) {
			return;
		}
		long oldTime = System.currentTimeMillis();
		for(Locale locale : localeService.getLocalesList()) {
			if(getUpdated().containsKey(pluginID + locale.toLanguageTag())) return;
			if(fileName.contains(locale.toLanguageTag()) && (fileName.contains("conf") || fileName.contains("yml") || fileName.contains("json") || fileName.contains("properties"))) {
				if(getUpdated().containsKey(fileName)) return;
				logger.info("[FileWatcher] Locale file \"" + fileName + "\" for plugin \"" + pluginID + "\" has been changed! Reloading...");
				PluginLocale localeconfig = localeService.getOrDefaultLocale(pluginID, locale);
				localeconfig.reload();
				getUpdated().put(pluginID + locale.toLanguageTag(), TimeUnit.MILLISECONDS.toSeconds(oldTime));
				class ReloadLocaleEvent extends AbstractEvent implements LocaleEvent.Reload {

					@Override
					public String plugin() {
						return pluginID;
					}

					@Override
					public Locale getLocale() {
						return locale;
					}

					@Override
					public Cause cause() {
						return cause;
					}

					@Override
					public PluginLocale getLocaleConfig() {
						return localeconfig;
					}
					
				}
				postEvent(new ReloadLocaleEvent());
				remove(pluginID + locale.toLanguageTag());
				break;
			}
		}
	}

	private void remove(String key) {
		Sponge.asyncScheduler().submit(Task.builder().plugin(pluginContainer).delay(3, TimeUnit.SECONDS).execute(() -> {
			if(getUpdated().containsKey(key)) getUpdated().remove(key);
		}).build());
	}

	private void postEvent(LocaleEvent localeEvent) {
		Sponge.asyncScheduler().submit(Task.builder().plugin(pluginContainer).delay(3, TimeUnit.SECONDS).execute(() -> {
			Sponge.eventManager().post(localeEvent);
		}).build());
	}

	private Map<String, Long> getUpdated() {
		return ((LocaleAPI) pluginContainer.instance()).getUpdated();
	}

}
