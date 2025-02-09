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
	},
	PROPERTIES(".properties") {
		@Override
		public String toString() {
			return ".properties";
		}
		@Override
		public String getExtension() {
			return "properties";
		}
	},
	UNKNOWN(""){};

	ConfigTypes(String string) {}

	public String getExtension() {
		return "";
	}

	public static ConfigTypes find(String type) {
		return Stream.of(ConfigTypes.values()).filter(value -> value.toString().equals(type)).findFirst().orElse(UNKNOWN);
	}

	public static boolean isValidExtension(String extension) {
		return Stream.of(ConfigTypes.values()).filter(v -> v.getExtension().equals(extension)).findFirst().isPresent();
	}

	public static ConfigTypes getTypeByExtension(String extension) {
		return Stream.of(ConfigTypes.values()).filter(v -> v.toString().equals(extension) || v.getExtension().equals(extension)).findFirst().orElse(HOCON);
	}

}
