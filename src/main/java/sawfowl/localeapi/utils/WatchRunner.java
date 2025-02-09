package sawfowl.localeapi.utils;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.spongepowered.plugin.PluginContainer;

import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.api.Logger;

public class WatchRunner {

	private static WatchRunner instance;
	private boolean work = false;
	private final Watcher watcher;
	private boolean started = false;
	private static Logger logger;
	private WatchRunner(LocaleService localeService, Logger logger, Path path) {
		WatchRunner.logger = logger;
		watcher = new Watcher(localeService, logger, path);
	}

	public static void createInstance(LocaleService localeService, Logger logger, Path path) {
		if(instance == null) instance = new WatchRunner(localeService, logger, path);
	}

	public static WatchRunner getInstance() {
		return instance;
	}

	public static void initPlugin(PluginContainer container) {
		getInstance().watcher.register(container);
	}

	public void enable() {
		work = true;
		watcher.enable();
	}

	public void run() {
		if(started) return;
		started = true;
		getLogger().info("[FileWatcher] File tracking has been launched.");
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
