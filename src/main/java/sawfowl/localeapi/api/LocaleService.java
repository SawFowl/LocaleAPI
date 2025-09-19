package sawfowl.localeapi.api;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.spongepowered.plugin.PluginContainer;

import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;

public interface LocaleService {

	/*
	 * Getting the system locale.<br>
	 * If Sponge does not support your system locale, the default locale for Sponge will be selected.
	 */
	Locale getSystemOrDefaultLocale();

	/**
	 * List of all localizations of the game.
	 * 
	 */
	List<Locale> getLocalesList();

	/**
	 * The default locale. Used in a localization map.
	 */
	Locale getDefaultLocale();

	/**
	 * Selecting serialization variant for items.<br>
	 * <b>1</b> - All NBT tags will be written in 1 line. This option is the most reliable, but significantly complicates manual editing of NBT tags in config.<br>
	 * <b>2</b> - Advanced recording. Easier to make manual changes to the config. If you have problems with this type of serialization, you should report errors to the LocaleAPI plugin developer.<br>
	 * <b>3</b> - Using Sponge serializer. Some data will be written in 1 line. If you encounter problems with this type of serialization, you should report bugs to the Sponge developers.<br>
	 */
	void setItemStackSerializerVariant(PluginContainer container, ItemStackSerializerType serializerType) throws Exception ;

	/**
	 * Getting the type number of the serialization type of an items.
	 */
	ItemStackSerializerType getItemStackSerializer(PluginContainer container);

	/**
	 * Getting the type number of the serialization type of an items.
	 */
	ItemStackSerializerType getItemStackSerializer(String pluginID);

	/**
	 * 
	 * Set the default serializable class object for all plugin localizations.<br>
	 * If no data has been previously written to this localization, it will be applied from the specified class.<br>
	 * This class will be applied automatically to all localizations loaded after its addition.<br>
	 * Automatic application of this class does not make any changes to the localization data.
	 * 
	 * @param defaultReference - The serializable class extends {@link Translation}
	 * @param container - {@link PluginContainer}
	 */
	<T extends Translation> void setDefaultReference(PluginContainer container, Class<T> defaultReference);

	/**
	 * Get the default serialization class for plugin localizations.<br>
	 * No type conversion is performed.
	 * 
	 * @param container - {@link PluginContainer}
	 * @return Serializable class, or null if no class assignment was previously made.
	 */
	Class<? extends Translation> getDefaultReference(PluginContainer container);

	/**
	 * Same as {@linkplain #getDefaultReference(PluginContainer)}
	 */
	Class<? extends Translation> getDefaultReference(String pluginID);

	LocalesList createLocales(PluginContainer container);

	LocalesList getLocales(PluginContainer container);

}
