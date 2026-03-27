package sawfowl.localeapi.api.event;

import java.util.Locale;

import org.spongepowered.api.event.Event;

import sawfowl.localeapi.api.config.locale.PluginLocale;

public interface LocaleEvent extends Event {

	public interface Reload extends LocaleEvent {

		public PluginLocale getLocaleConfig();

	}

	public interface Create extends Reload {

		public String configType();

	}

	public interface Delete extends LocaleEvent {

		String getFileName();

	}

	public String plugin();

	public Locale getLocale();

}
