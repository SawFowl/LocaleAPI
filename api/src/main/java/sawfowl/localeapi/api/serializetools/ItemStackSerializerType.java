package sawfowl.localeapi.api.serializetools;

import java.util.Objects;

public enum ItemStackSerializerType {

	/**
	 * Using Sponge serializer. Some data will be written in 1 line. If you encounter problems with this type of serialization, you should report bugs to the Sponge developers.
	 */
	SPONGE {
		@Override
		public String toString() {
			return "Sponge";
		}
		@Override
		String legacyType(String string) {
			return "3";
		}
	},
	/**
	 * Advanced recording. Easier to make manual changes to the config. If you have problems with this type of serialization, you should report errors to the LocaleAPI plugin developer.
	 */
	JSON {
		@Override
		public String toString() {
			return "Json";
		}
		@Override
		String legacyType(String string) {
			return "2";
		}
	},
	/**
	 * All components will be written in 1 line. This option is the most reliable, but significantly complicates manual editing of components in config.
	 */
	SIMPLE {
		@Override
		public String toString() {
			return "Simple";
		}
		@Override
		String legacyType(String string) {
			return "1";
		}
	};

	/**
	 * Deprecated item data type identifier.<br>
	 * Numeric type identification is used for compatibility with earlier versions of the plugin.<br>
	 * This method is not public, as it is better to use a new, more understandable identifier instead.<br>
	 * This method may be removed in the future.
	 */
	abstract String legacyType(String string);

	/**
	 * Search for a specific type by its string designation.
	 */
	public static ItemStackSerializerType fromString(String string) {
		Objects.requireNonNull(string);
		for(ItemStackSerializerType type : ItemStackSerializerType.values()) if(type.toString().equalsIgnoreCase(string) || type.legacyType(string).equals(string)) return type;
		throw new IllegalArgumentException("Unexpected value: " + string);
	}

}
