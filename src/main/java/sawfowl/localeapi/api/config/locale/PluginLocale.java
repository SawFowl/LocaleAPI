package sawfowl.localeapi.api.config.locale;

import java.util.Locale;

import org.spongepowered.configurate.serialize.SerializationException;

import net.kyori.adventure.text.Component;
import sawfowl.localeapi.api.config.Config;

public interface PluginLocale extends Config {

	Locale getLocale();

	default Component getComponent(Object... path) {
		try {
			return getRootNode().node(path).get(Component.class);
		} catch (SerializationException e) {
			e.printStackTrace();
			return Component.empty();
		}
	}
}
