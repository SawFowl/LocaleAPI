package sawfowl.localeapi.apiclasses.config.loaders;

public final class PrimitiveValueConverter {

	private PrimitiveValueConverter() {}

	public static Object toNightConfigCompatible(Object value) {
		if(value == null) return null;
		if(value instanceof Character character) return character.toString();
		return value;
	}

	public static Object fromNightConfigCompatible(Object value) {
		if(value == null) return null;
		if(value instanceof String string && string.length() == 1) return string.charAt(0);
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
				|| value instanceof String;
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