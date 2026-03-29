package sawfowl.localeapi.utils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.plugin.PluginContainer;

import sawfowl.localeapi.LocaleAPI;
import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.EnumLocales;
import sawfowl.localeapi.api.Logger;
import sawfowl.localeapi.api.config.locale.PluginLocale;
import sawfowl.localeapi.api.event.LocaleEvent;
import sawfowl.localeapi.api.services.LocaleService;
import sawfowl.localeapi.configure.localization.LoggerMessages;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

class Watcher {

	private WatchService watchService;
	private boolean freeze = true;
	private Set<String> registered = new HashSet<>();
	private Map<Path, PluginContainer> paths = new HashMap<>();
	private Set<UpdateInfo> updateInfo = new HashSet<>();
	private LocaleService localeService;
	private Logger logger;
	private Cause cause;
	private PluginContainer pluginContainer;
	Watcher(LocaleService localeService, Logger logger) {
		this.localeService = localeService;
		this.logger = logger;
		pluginContainer = LocaleAPI.getPluginContainer();
		cause = Cause.of(EventContext.builder().add(EventContextKeys.PLUGIN, pluginContainer).build(), pluginContainer);
		try {
			watchService = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
		};
	}

	void enable() {
		freeze = false;
	}

	void register(PluginContainer container, Path localesDir) {
		if(!registered.contains(container.metadata().id())) try {
			if(!localesDir.toFile().exists()) localesDir.toFile().mkdir();
			localesDir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
			registered.add(container.metadata().id());
			paths.put(localesDir, container);
		} catch (IOException e) {
			getLogger().error(e.getLocalizedMessage());
		}
	}

	void startWatch() {
		if(freeze) return;
		if(!updateInfo.isEmpty()) updateInfo.removeIf(info -> System.currentTimeMillis() - info.time > 1000);
		try {
			while(isResetKey(watchService.take()));
		} catch (InterruptedException e) {
			getLogger().error(e.getLocalizedMessage());
		}
	}

	private boolean isResetKey(WatchKey key) {
		if(freeze) return false;
		for(WatchEvent<?> event : key.pollEvents()) {
			if(paths.containsKey((Path) key.watchable())) {
				work(event, paths.get((Path) key.watchable()), (Path) key.watchable(), event.context().toString());
			} else if(((Path) key.watchable()).getParent() != null) {
				work(event, (Path) key.watchable(), (((Path) key.watchable()).getParent()).toFile().getName(), event.context().toString());
			}
		}
		return key.reset();
	}

	private void work(WatchEvent<?> event, Path path, String plugin, String file) {
		work(event, Sponge.pluginManager().plugin(plugin).orElse(null), path, file);
	}

	private void work(WatchEvent<?> event, PluginContainer container, Path path, String file) {
		if(container != null && !file.contains(".tmp") && file.contains(".")) work(event, container, path, file.split("\\."), file);
	}

	private void work(WatchEvent<?> event, PluginContainer container, Path path, String[] file, String fileName) {
		if(file.length == 2 && isValidFile(file[0], file[1])) work(event, container, path, EnumLocales.find(file[0]), ConfigTypes.getTypeByExtension(file[1]), fileName);
	}

	private void work(WatchEvent<?> event, PluginContainer container, Path path, Locale locale, ConfigTypes type, String fileName) {
		if(event.kind() == ENTRY_CREATE) {
			onCreate(container, locale, type);
		} else if(event.kind() == ENTRY_MODIFY) {
			onModify(container, locale, type);
		} if(event.kind() == ENTRY_DELETE && locale != Locales.DEFAULT) Sponge.asyncScheduler().submit(Task.builder().delay(200, TimeUnit.MILLISECONDS).plugin(LocaleAPI.getPluginContainer()).execute(() -> {
			if(path.resolve(fileName).toFile().exists()) return;
			localeService.getLocales(container).remove(locale);
			logger.info("[FileWatcher] " + getMessages().getRemove(locale, container));
			Sponge.eventManager().post(new LocaleEvent.Delete() {

				@Override
				public Cause cause() {
					return cause;
				}

				@Override
				public String plugin() {
					return container.metadata().id();
				}

				@Override
				public Locale getLocale() {
					return locale;
				}

				@Override
				public String getFileName() {
					return fileName;
				}

			});
		}).build());
	}

	private void onCreate(PluginContainer container, Locale locale, ConfigTypes type) {
		if(!localeService.getLocales(container).contains(locale)) create(container, locale, type, System.currentTimeMillis());
	}

	private void create(PluginContainer container, Locale locale, ConfigTypes type, long time) {
		logger.info("[FileWatcher] " + getMessages().getAdd(locale, type, container));
		PluginLocale pluginLocale = localeService.getDefaultReference(container) == null
			?
			localeService.getLocales(container).createSimpleTranslation(type, locale)
			:
			localeService.getLocales(container).createReferencedTranslation(type, locale, localeService.getDefaultReference(container));
		Sponge.eventManager().post(new LocaleEvent.Create() {

			@Override
			public Cause cause() {
				return cause;
			}

			@Override
			public String plugin() {
				return container.metadata().id();
			}

			@Override
			public PluginLocale getLocaleConfig() {
				return pluginLocale;
			}

			@Override
			public Locale getLocale() {
				return locale;
			}

			@Override
			public String configType() {
				return type.getExtension();
			}

		});
		this.updateInfo.add(new UpdateInfo(time, locale, container));
	}

	private void onModify(PluginContainer container, Locale locale, ConfigTypes type) {
		UpdateInfo updateInfo = this.updateInfo.stream().filter(info -> info.locale.equals(locale) && info.container.metadata().id().equals(container.metadata().id())).findFirst().orElse(null);
		if(updateInfo == null) {
			if(!localeService.getLocales(container).contains(locale) || localeService.getLocales(container).getSimple(locale).getType() != type) return;
			PluginLocale pluginLocale = localeService.getLocales(container).getSimple(locale);
			pluginLocale.load();
			this.updateInfo.add(new UpdateInfo(System.currentTimeMillis(), locale, container));
			logger.info("[FileWatcher] " + getMessages().getReload(locale, type, container));
			Sponge.eventManager().post(new LocaleEvent.Reload() {

				@Override
				public Cause cause() {
					return cause;
				}

				@Override
				public String plugin() {
					return container.metadata().id();
				}

				@Override
				public PluginLocale getLocaleConfig() {
					return pluginLocale;
				}

				@Override
				public Locale getLocale() {
					return locale;
				}

			});
		} else this.updateInfo.remove(updateInfo);
	}

	private boolean isValidFile(String fileName, String extension) {
		return existTag(fileName) && ConfigTypes.isValidExtension(extension);
	}

	void stopWatch() {
		freeze = true;
	}

	private Logger getLogger() {
		return logger;
	}

	private boolean existTag(String locale) {
		return Stream.of(EnumLocales.values()).filter(value -> value.getTag().equals(locale)).findFirst().isPresent();
	}

	private LoggerMessages getMessages() {
		return LocaleAPI.getLocales().getSystemAsReferenced().getLoggerMessages();
	}

	private class UpdateInfo {
		final PluginContainer container;
		final Locale locale;
		final long time;
		public UpdateInfo(long time, Locale locale, PluginContainer container) {
			this.container = container;
			this.locale = locale;
			this.time = time;
		}

		@Override
		public int hashCode() {
			return Objects.hash(container, locale, time);
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(obj == null || getClass() != obj.getClass()) return false;
			return Objects.equals(container.metadata().id(), ((UpdateInfo) obj).container.metadata().id()) && Objects.equals(locale, ((UpdateInfo) obj).locale) && time == ((UpdateInfo) obj).time;
		}

	}


}
