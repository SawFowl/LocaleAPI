package sawfowl.localeapi.api;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.spongepowered.api.util.locale.LocaleSource;
import org.spongepowered.api.util.locale.Locales;

import sawfowl.localeapi.api.config.locale.PluginLocale;
import sawfowl.localeapi.api.config.locale.ReferencedLocale;

public interface LocalesList {

	PluginLocale createSimpleTranslation(ConfigTypes configType, Locale locale);

	<T extends Translation> ReferencedLocale<T> createReferenceTranslation(ConfigTypes configType, Locale locale, Class<T> clazz);

	<T extends Translation> ReferencedLocale<T> createReferenceTranslation(ConfigTypes configType, Locale locale, T object);

	<T extends PluginLocale> T getLocale(Locale locale) throws ClassCastException;

	<T extends PluginLocale> T remove(Locale locale) throws ClassCastException ;

	<T extends PluginLocale> Stream<T> stream();

	void forEach(Consumer<? super PluginLocale> action);

	boolean contains(Locale locale);

	int size();

	boolean isEmpy();

	/**
	 * Save plugin locales from assets.
	 */
	void saveAssetLocales();

	@SuppressWarnings("unchecked")
	default <T extends Translation> T getAsReference(Locale locale) {
		return (T) getLocale(locale).toReference().get();
	}

	default <T extends PluginLocale> T getLocale(LocaleSource localeSource) {
		return getLocale(localeSource.locale());
	}

	default <T extends Translation> T getAsReference(LocaleSource localeSource) {
		return getAsReference(localeSource.locale());
	}

	default <T extends PluginLocale> T getDefaultLocale() {
		return getLocale(Locales.DEFAULT);
	}

	default <T extends Translation> T getDefaultAsReference() {
		return getAsReference(Locales.DEFAULT);
	}

	default <T extends PluginLocale> T getSystemLocale() {
		return getLocale(Locale.getDefault());
	}

	default <T extends Translation> T getSystemAsReference() {
		return getAsReference(Locale.getDefault());
	}

}
