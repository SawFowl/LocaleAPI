package sawfowl.localeapi.api.serializetools;

public enum ItemStackSerializerType {

	/**
	 * Using Sponge serializer. Some data will be written in 1 line. If you encounter problems with this type of serialization, you should report bugs to the Sponge developers.
	 */
	SPONGE,
	/**
	 * Advanced recording. Easier to make manual changes to the config. If you have problems with this type of serialization, you should report errors to the LocaleAPI plugin developer.
	 */
	JSON,
	/**
	 * All NBT tags will be written in 1 line. This option is the most reliable, but significantly complicates manual editing of NBT tags in config.
	 */
	SIMPLE

}
