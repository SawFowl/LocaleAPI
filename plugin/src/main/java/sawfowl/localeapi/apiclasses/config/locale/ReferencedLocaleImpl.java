package sawfowl.localeapi.apiclasses.config.locale;

import java.nio.file.Path;
import java.util.Locale;

import org.spongepowered.configurate.ConfigurationNode;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.Translation;
import sawfowl.localeapi.api.config.locale.ReferencedLocale;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.apiclasses.config.ReferencedConfigImpl;

public class ReferencedLocaleImpl<T extends Translation> extends ReferencedConfigImpl<T, ConfigurationNode> implements ReferencedLocale<T> {

	public static final <T extends Translation> ReferencedLocaleImpl<T> create(Path configDir, ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, Class<T> clazz, Locale locale) {
		return new ReferencedLocaleImpl<T>(configDir, configType, itemStackSerializerType, clazz, locale);
	}

	public static final <T extends Translation> ReferencedLocaleImpl<T> create(Path configDir, ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, T object, Locale locale) {
		return new ReferencedLocaleImpl<T>(configDir, configType, itemStackSerializerType, object, locale);
	}

	private final Locale locale;
	private ReferencedLocaleImpl(Path configDir, ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, Class<T> clazz, Locale locale) {
		super(configDir, locale.toLanguageTag(), configType, itemStackSerializerType, null, clazz);
		this.locale = locale;
	}

	private ReferencedLocaleImpl(Path configDir, ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, T object, Locale locale) {
		super(configDir, locale.toLanguageTag(), configType, itemStackSerializerType, null, object);
		this.locale = locale;
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public <L extends Translation, O extends ReferencedLocale<L>> O toReferenceTranslation(L config) {
		return (O) this;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public <L extends Translation, O extends ReferencedLocale<L>> O toReferenceTranslation(Class<L> config) {
		return (O) this;
	}

}
