package sawfowl.localeapi.api;

import java.util.stream.Stream;

public enum ConfigTypes {

	HOCON(".conf") {
		@Override
		public String toString() {
			return ".conf";
		}
		@Override
		public String getExtension() {
			return "conf";
		}
		@Override
		public String getTypeName() {
			return "Hocon";
		}
	},
	JSON(".json") {
		@Override
		public String toString() {
			return ".json";
		}
		@Override
		public String getExtension() {
			return "json";
		}
		@Override
		public String getTypeName() {
			return "Json";
		}
	},
	JACKSON(".jackson") {
		@Override
		public String toString() {
			return ".jackson";
		}
		@Override
		public String getExtension() {
			return "jackson";
		}
		@Override
		public String getTypeName() {
			return "Jackson";
		}
	},
	XML(".xml") {
		@Override
		public String toString() {
			return ".xml";
		}
		@Override
		public String getExtension() {
			return "xml";
		}
		@Override
		public String getTypeName() {
			return "XML";
		}
	},
	GEYSER_YAML(".yml") {
		@Override
		public String toString() {
			return ".yml";
		}
		@Override
		public String getExtension() {
			return "yml";
		}
		@Override
		public String getTypeName() {
			return "GeyserYaml";
		}
		@Override
		public boolean comparableType(ConfigTypes other) {
			return other == this || other == YAML;
		}
	},
	YAML(".yml") {
		@Override
		public String toString() {
			return ".yml";
		}
		@Override
		public String getExtension() {
			return "yml";
		}
		@Override
		public String getTypeName() {
			return "Yaml";
		}
		@Override
		public boolean comparableType(ConfigTypes other) {
			return other == this || other == GEYSER_YAML;
		}
	},
	/**PROPERTIES(".properties") {
		@Override
		public String toString() {
			return ".properties";
		}
		@Override
		public String getExtension() {
			return "properties";
		}
	}*/
	UNKNOWN(""){
		@Override
		public String getTypeName() {
			return "UNKNOWN";
		}

	};

	ConfigTypes(String string) {}

	public String getExtension() {
		return "";
	}

	public abstract String getTypeName();

	public boolean comparableType(ConfigTypes other) {
		return other == this;
	}

	public static ConfigTypes find(String type) {
		return Stream.of(ConfigTypes.values()).filter(value -> value.getTypeName().equalsIgnoreCase(type) || value.toString().equals(type)).findFirst().orElse(UNKNOWN);
	}

	public static boolean isValidExtension(String extension) {
		return !extension.isEmpty() && Stream.of(ConfigTypes.values()).filter(v -> v.getExtension().equals(extension)).findFirst().isPresent();
	}

	public static ConfigTypes getTypeByExtension(String extension) {
		return Stream.of(ConfigTypes.values()).filter(v -> v.toString().equals(extension) || v.getExtension().equals(extension)).findFirst().orElse(HOCON);
	}

}
