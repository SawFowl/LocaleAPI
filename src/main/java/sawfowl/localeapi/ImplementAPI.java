package sawfowl.localeapi;

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppedGameEvent;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.plugin.PluginContainer;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import sawfowl.localeapi.api.EnumLocales;
import sawfowl.localeapi.api.Translation;
import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.api.LocalesList;
import sawfowl.localeapi.api.Logger;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.apiclasses.LocalesListImpl;
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

	class API extends LocaleService {

		private Map<String, LocalesList<? extends Translation>> pluginLocales = new HashMap<>();;
		private Map<String, ItemStackSerializerType> stackSerializers;
		private Map<String, Class<? extends Translation>> defaultReferences;
		private List<Locale> locales;
		private WatchRunner watchThread;
		private final Path configDirectory;
		private Locale system = Locale.getDefault();
		private boolean allowSystem = false;
		API(Logger logger, Path path) {
			service = this;
			configDirectory = path;
			stackSerializers = new HashMap<String, ItemStackSerializerType>();
			defaultReferences = new HashMap<String, Class<? extends Translation>>();
			locales = EnumLocales.getLocales();
			WatchRunner.createInstance(this, logger, path);
			watchThread = WatchRunner.getInstance();
			allowSystem = locales.contains(system) || locales.stream().filter(locale -> (locale.toLanguageTag().equals(system.toLanguageTag()))).findFirst().isPresent();
			Sponge.eventManager().registerListeners(LocaleAPI.getPluginContainer(), this, MethodHandles.lookup());
			new InjectorAPI().createInjector();
		}

		@Override
		public Locale getSystemOrDefaultLocale() {
			return allowSystem ? system : getDefaultLocale();
		}

		@Override
		public List<Locale> getLocalesList() {
			return locales;
		}

		@Override
		public Locale getDefaultLocale() {
			return Locales.DEFAULT;
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
			Objects.requireNonNull(defaultReference);
			if(defaultReferences.containsKey(container.metadata().id())) defaultReferences.remove(container.metadata().id());
			defaultReferences.put(container.metadata().id(), defaultReference);
		}

		@Override
		public <T extends Translation> Class<T> getDefaultReference(PluginContainer container) {
			return getDefaultReference(container.metadata().id());
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends Translation> Class<T> getDefaultReference(String pluginID) {
			return defaultReferences.containsKey(pluginID) ? (Class<T>) defaultReferences.get(pluginID) : null;
		}

		@Override
		public <T extends Translation> LocalesList<T> createLocales(PluginContainer container) {
			if(pluginLocales.containsKey(container.metadata().id())) return getLocales(container);
			pluginLocales.put(container.metadata().id(), LocalesListImpl.create(container, configDirectory, this));
			WatchRunner.initPlugin(container);
			return getLocales(container);
		}

		@Override
		public <T extends Translation> LocalesList<T> createLocales(PluginContainer container, Class<? extends T> translationReference) {
			if(pluginLocales.containsKey(container.metadata().id())) return getLocales(container);
			setDefaultReference(container, translationReference);
			return createLocales(container);
		}

		@Override
		public <T extends Translation> LocalesList<T> getLocales(PluginContainer container) {
			return getLocales(container.metadata().id());
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends Translation> LocalesList<T> getLocales(String plugin) {
			return (LocalesList<T>) pluginLocales.get(plugin);
		}

		@Override
		public boolean localesExist(PluginContainer container) {
			return localesExist(container.metadata().id());
		}

		@Override
		public boolean localesExist(String plugin) {
			return pluginLocales.containsKey(plugin);
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

		void startWatch() {
			watchThread.run();
		}

	}

	final class InjectorAPI extends AbstractModule {

		Injector createInjector() {
			return Guice.createInjector(this);
		}

		@Override
		protected void configure() {
			bind(LocaleService.class).toInstance(service);
			this.requestStaticInjection(LocaleService.class);
		}

	}

}
