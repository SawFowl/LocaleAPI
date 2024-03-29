package sawfowl.localeapi.apiclasses;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;

import net.kyori.adventure.text.Component;

import sawfowl.localeapi.LocaleAPI;
import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.LocaleReference;
import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.api.PluginLocale;
import sawfowl.localeapi.api.Text;
import sawfowl.localeapi.api.TextUtils;

public abstract class AbstractLocale implements PluginLocale {


	protected final LocaleService localeService;
	protected final Logger logger;
	protected final String pluginID;
	protected final boolean thisIsDefault;
	protected final Path path;
	protected final String locale;
	public AbstractLocale(LocaleService localeService, Logger logger, Path path, String pluginID, String locale) {
		this.localeService = localeService;
		this.logger = logger;
		this.path = path.resolve(pluginID + File.separator + locale + getType().toString());
		this.pluginID = pluginID;
		this.locale = locale;
		thisIsDefault = locale.equals(Locales.DEFAULT.toLanguageTag());
		setDefaultReference();
	}

	abstract ConfigTypes getType();

	abstract void setComment(String comment, Object... path);

	@Override
	public Component getComponent(Object... path) {
		if(thisIsDefault && getLocaleNode(path).virtual()) return TextUtils.deserializeLegacy("&cPath " + getPathName(path) + " not exist!");
		try {
			return getLocaleNode(path).virtual() && !thisIsDefault ? getDefaultLocale().getComponent(path) : (getLocaleNode(path).childrenList().isEmpty() && getLocaleNode(path).childrenMap().isEmpty() ? TextUtils.deserialize(getString(path)) : getLocaleNode(path).get(Component.class));
		} catch (SerializationException e) {
			return TextUtils.deserialize(getString(path));
		}
	}

	@Override
	public Text getText(Object... path) {
		return Text.of(getComponent(path));
	}

	@Override
	public List<Text> getTexts(Object... path) {
		return getListComponents(path).stream().map(Text::of).toList();
	}

	@Override
	public List<Component> getListComponents(Object... path) {
		if(thisIsDefault && getLocaleNode(path).virtual()) return Arrays.asList(TextUtils.deserializeLegacy("&cPath " + getPathName(path) + " not exist!"));
		try {
			return getLocaleNode(path).virtual() && !thisIsDefault ? getDefaultLocale().getListComponents(path) : (getLocaleNode(path).childrenList().isEmpty() && getLocaleNode(path).childrenMap().isEmpty() ? getListStrings(path).stream().map(TextUtils::deserialize).toList() : getLocaleNode(path).getList(Component.class));
		} catch (SerializationException e) {
			return getListStrings(path).stream().map(TextUtils::deserialize).toList();
		}
	}

	@Override
	public String getString(Object... path) {
		if(thisIsDefault && getLocaleNode(path).virtual()) return "Path " + getPathName(path) + " not exist!";
		return getLocaleNode(path).virtual() && !thisIsDefault ? getDefaultLocale().getString(path) : getLocaleNode(path).getString();
	}

	@Override
	public List<String> getListStrings(Object... path) {
		if(thisIsDefault && getLocaleNode(path).virtual()) return Arrays.asList("Path " + getPathName(path) + " not exist!");
		try {
			return getLocaleNode(path).virtual() && !thisIsDefault ? getDefaultLocale().getListStrings(path) : getLocaleNode(path).getList(String.class);
		} catch (SerializationException e) {
			logger.error(e.getLocalizedMessage());
		}
		return Arrays.asList("Error getting list of Strings " + getPathName(path));
	}

	@Override
	public boolean checkComponent(boolean json, Component component, String comment, Object... path) {
		if(getLocaleNode(path).empty() || !fileExists()) {
			try {
				if(json) {
					getLocaleNode(path).set(Component.class, component);
				} else getLocaleNode(path).set(TextUtils.serializeLegacy(component));
				if(comment != null) setComment(comment, path);
				return true;
			} catch (SerializationException e) {
				logger.error(e.getLocalizedMessage());
			}
		}
		return false;
	}

	@Override
	public boolean checkListComponents(boolean json, List<Component> components, String comment, Object... path) {
		if(getLocaleNode(path).empty() || !fileExists()) {
			try {
				if(json) {
					getLocaleNode(path).setList(Component.class, components);
				} else getLocaleNode(path).setList(String.class, components.stream().map(TextUtils::serializeLegacy).toList());
				if(comment != null) setComment(comment, path);
				return true;
			} catch (SerializationException e) {
				logger.error(e.getLocalizedMessage());
			}
		}
		return false;
	}

	@Override
	public boolean checkString(String string, String comment, Object... path) {
		if(getLocaleNode(path).empty() || !fileExists()) {
			try {
				getLocaleNode(path).set(string);
				if(comment != null) setComment(comment, path);
				return true;
			} catch (SerializationException e) {
				logger.error(e.getLocalizedMessage());
			}
		}
		return false;
	}

	@Override
	public boolean checkListStrings(List<String> strings, String comment, Object... path) {
		if(getLocaleNode(path).empty() || !fileExists()) {
			try {
				getLocaleNode(path).setList(String.class, strings);
				if(comment != null) setComment(comment, path);
				return true;
			} catch (SerializationException e) {
				logger.error(e.getLocalizedMessage());
			}
		}
		return !false;
	}

	@Override
	public boolean fileExists() {
		return path.toFile().exists();
	}

	protected void freezeWatcher() {
		getUpdated().put(pluginID + locale, TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
		Sponge.asyncScheduler().submit(Task.builder().plugin(LocaleAPI.getPluginContainer()).delay(3, TimeUnit.SECONDS).execute(() -> {
			if(getUpdated().containsKey(pluginID + locale)) getUpdated().remove(pluginID + locale);
		}).build());
	}

	protected Map<String, Long> getUpdated() {
		return ((LocaleAPI) LocaleAPI.getPluginContainer().instance()).getUpdated();
	}

	protected PluginLocale getDefaultLocale() {
		return localeService.getPluginLocales(pluginID).get(Locales.DEFAULT);
	}

	protected String getPathName(Object... path) {
		return "[" + String.join(", ", Stream.of(path).map(Object::toString).toArray(String[]::new)) + "]";
	}

	protected void setDefaultReference() {
		if(getType() == ConfigTypes.PROPERTIES) return;
		Class<? extends LocaleReference> defaultReference = localeService.getDefaultReference(pluginID);
		if(defaultReference == null || asReference(defaultReference) != null) return;
		try {
			setLocaleReference(defaultReference);
			if(!path.toFile().exists() || !getLocaleRootNode().empty()) saveLocaleNode();
			defaultReference = null;
		} catch (ConfigurateException e) {
			e.printStackTrace();
		}
	}

}