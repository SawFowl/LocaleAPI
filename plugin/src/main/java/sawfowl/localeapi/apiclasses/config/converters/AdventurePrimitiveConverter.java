package sawfowl.localeapi.apiclasses.config.converters;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.util.*;

/**
 * Converts Adventure objects to primitive types and back
 */
public final class AdventurePrimitiveConverter {

	private AdventurePrimitiveConverter() {}

	/**
	 * Checks if an object is an Adventure type
	 */
	public static boolean isAdventure(Object object) {
		return object instanceof Component 
			|| object instanceof NamedTextColor 
			|| object instanceof TextColor 
			|| object instanceof TextDecoration 
			|| object instanceof Key;
	}

	/**
	 * Converts Adventure object to a primitive type
	 */
	public static Object convertAdventureToPrimitive(Object value) {
		if(value == null) return null;

		if(value instanceof Component component) {
			return GsonComponentSerializer.gson().serialize(component);
		}

		if(value instanceof NamedTextColor color) {
			return color.toString();
		}

		if(value instanceof TextColor color) {
			return color.asHexString();
		}

		if(value instanceof TextDecoration decoration) {
			return decoration.toString();
		}

		if(value instanceof Key key) {
			return key.asString();
		}

		return value;
	}

	/**
	 * Recursively converts all Adventure objects in a Map to primitive types
	 */
	public static Map<String, Object> convertMapValuesToPrimitive(Map<String, Object> map) {
		Map<String, Object> result = new LinkedHashMap<>();
		for(Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			result.put(key, convertValueToPrimitive(value));
		}
		return result;
	}

	/**
	 * Converts a value to a primitive type (recursive)
	 */
	@SuppressWarnings("unchecked")
	public static Object convertValueToPrimitive(Object value) {
		if(value == null) return null;

		if(isAdventure(value)) {
			value = convertAdventureToPrimitive(value);
		}

		if(value instanceof Character character) {
			return character.toString();
		}

		if(value instanceof UUID uuid) {
			return uuid.toString();
		}

		if(value instanceof Map) {
			return convertMapValuesToPrimitive((Map<String, Object>) value);
		}

		if(value instanceof List) {
			List<Object> result = new ArrayList<>();
			for(Object item : (List<?>) value) {
				result.add(convertValueToPrimitive(item));
			}
			return result;
		}

		if(value instanceof Object[]) {
			Object[] array = (Object[]) value;
			Object[] result = new Object[array.length];
			for(int i = 0; i < array.length; i++) {
				result[i] = convertValueToPrimitive(array[i]);
			}
			return result;
		}

		return value;
	}

}