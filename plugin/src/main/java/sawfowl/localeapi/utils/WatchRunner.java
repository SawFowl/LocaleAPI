package sawfowl.localeapi.utils;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.spongepowered.plugin.PluginContainer;

import sawfowl.localeapi.LocaleAPI;
import sawfowl.localeapi.api.Logger;
import sawfowl.localeapi.api.services.LocaleService;

public class WatchRunner {

	private static WatchRunner instance;
	private boolean work = false;
	private final Watcher watcher;
	private boolean started = false;
	private static Logger logger;
	private WatchRunner(LocaleService localeService, Logger logger) {
		WatchRunner.logger = logger;
		watcher = new Watcher(localeService, logger);
	}

	public static void createInstance(LocaleService localeService, Logger logger) {
		if(instance == null) instance = new WatchRunner(localeService, logger);
	}

	public static WatchRunner getInstance() {
		return instance;
	}

	public static void initPlugin(PluginContainer container, Path localesPath) {
		getInstance().watcher.register(container, localesPath);
	}

	public void enable() {
		work = true;
		watcher.enable();
	}

	public void run() {
		if(started) return;
		started = true;
		getLogger().info("[FileWatcher] " + LocaleAPI.getLocales().getSystemAsReferenced().getLoggerMessages().getStartWatch());
		enable();
		CompletableFuture.runAsync(() -> {
			while(work) {
				watcher.startWatch();
			}
		});
	}

	public void stopWatch() {
		watcher.stopWatch();
		work = false;
		started = false;
	}

	private Logger getLogger() {
		return logger;
	}


}
