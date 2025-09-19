package sawfowl.localeapi.api.config.locale;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.spongepowered.api.event.lifecycle.RegisterBuilderEvent;
import org.spongepowered.configurate.serialize.SerializationException;

import net.kyori.adventure.text.Component;
import sawfowl.localeapi.api.Text;
import sawfowl.localeapi.api.config.Config;

public interface PluginLocale extends Config {

	/**
	 * See {@link Locale}
	 */
	Locale getLocale();

	/**
	 * Getting a deserialized list of {@link Component} classes from the locale configuration node. 
	 * 
	 * @param path- Path in the config file.
	 */
	default List<Component> getComponents(Object... path) {
		try {
			return getRootNode().node(path).getList(Component.class);
		} catch (SerializationException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	/**
	 * Getting formatted text from the current localization configuration.
	 * 
	 * @param path - Path in the config file.
	 */
	default Component getComponent(Object... path) {
		try {
			return getRootNode().node(path).get(Component.class);
		} catch (SerializationException e) {
			e.printStackTrace();
			return Component.empty();
		}
	}

	/**
	 * Getting deserialized text in the constructor for its further modification.<br>
	 * The operation is possible only after the constructor is registered in the {@link RegisterBuilderEvent} event.
	 */
	default Text getText(Object... path) {
		return Text.of(getComponent(path));
	}

	/**
	 * Getting deserialized text in the constructor for its further modification.<br>
	 * The operation is possible only after the constructor is registered in the {@link RegisterBuilderEvent} event.
	 */
	default List<Text> getTexts(Object... path) {
		return getComponents(path).stream().map(Text::of).toList();
	}
}
