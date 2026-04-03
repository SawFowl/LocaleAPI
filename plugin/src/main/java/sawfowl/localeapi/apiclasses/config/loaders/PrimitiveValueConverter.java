package sawfowl.localeapi.apiclasses.config.loaders;

import java.util.UUID;
import java.util.regex.Pattern;

public final class PrimitiveValueConverter {

	private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

	private PrimitiveValueConverter() {}

	public static Object toNightConfigCompatible(Object value) {
		if(value == null) return null;
		if(value instanceof Character character) return character.toString();
		if(value instanceof UUID uuid) return uuid.toString();
		return value;
	}

	public static Object fromNightConfigCompatible(Object value) {
		if(value == null) return null;
		if(value instanceof String string) {
			if(UUID_PATTERN.matcher(string).matches()) {
				try {
					return UUID.fromString(string);
				} catch (Exception e) {
				}
			}
			if(string.length() == 1) return string.charAt(0);
		}
		return value;
	}

	public static boolean isPrimitiveOrWrapper(Object value) {
		return value instanceof Character
				|| value instanceof Boolean
				|| value instanceof Byte
				|| value instanceof Short
				|| value instanceof Integer
				|| value instanceof Long
				|| value instanceof Float
				|| value instanceof Double
				|| value instanceof String
				|| value instanceof UUID;
	}

	public static boolean isPrimitiveArray(Object value) {
		return value instanceof char[]
				|| value instanceof byte[]
				|| value instanceof short[]
				|| value instanceof int[]
				|| value instanceof long[]
				|| value instanceof float[]
				|| value instanceof double[]
				|| value instanceof boolean[];
	}

}