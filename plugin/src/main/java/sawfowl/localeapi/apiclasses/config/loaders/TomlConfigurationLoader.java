package sawfowl.localeapi.apiclasses.config.loaders;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.json.JsonParser;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.CommentHandler;
import org.spongepowered.configurate.loader.CommentHandlers;
import org.spongepowered.configurate.loader.ParsingException;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

public class TomlConfigurationLoader extends AbstractConfigurationLoader<CommentedConfigurationNode> {

	public static Builder builder() {
		return new Builder();
	}

	private TomlConfigurationLoader(Builder builder) {
		super(builder, new CommentHandler[]{CommentHandlers.HASH});
	}

	@Override
	protected void loadInternal(CommentedConfigurationNode node, BufferedReader reader) throws ParsingException {
		try {
			// Read entire TOML file into a string
			StringBuilder content = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line).append("\n");
			}
			
			// Load TOML with comments
			CommentedConfig tomlConfig = TomlFormat.instance().createConfig();
			tomlConfig = TomlFormat.instance().createParser().parse(content.toString());
			
			// Extract comments separately
			Map<String, String> comments = extractComments(tomlConfig, "");
			
			// Convert TOML to JSON string
			String jsonString = convertTomlToJson(tomlConfig);
			
			// Parse JSON into Config
			JsonParser jsonParser = new JsonParser();
			CommentedConfig nightConfig = TomlFormat.instance().createConfig();
			jsonParser.parse(jsonString, nightConfig, ParsingMode.REPLACE);
			
			// Convert to Map and set into node
			Map<String, Object> dataMap = convertToPlainMap(nightConfig);
			node.raw(dataMap);
			
			// Restore comments in the node
			restoreComments(node, comments, "");
			
		} catch (Exception e) {
			throw new ParsingException(node, 0, 0, null, "Error parsing TOML", e);
		}
	}

	@Override
	protected void saveInternal(ConfigurationNode node, Writer writer) throws ConfigurateException {
		try {
			// Get data as Map
			Object raw = node.raw();
			if(!(raw instanceof Map)) {
				throw new ConfigurateException("Root node is not a Map");
			}
			
			@SuppressWarnings("unchecked")
			Map<String, Object> dataMap = (Map<String, Object>) raw;
			
			// Extract comments from the node
			Map<String, String> comments = extractCommentsFromNode(node, "");
			
			// Convert all Adventure objects to strings before saving
			Map<String, Object> convertedMap = convertValuesToPrimitive(dataMap);
			
			// Convert Map to TOML Config
			CommentedConfig nightConfig = convertMapToToml(convertedMap);
			
			// Restore comments to TOML Config
			restoreCommentsToConfig(nightConfig, comments, "");
			
			// Save to TOML
			TomlWriter tomlWriter = new TomlWriter();
			StringWriter stringWriter = new StringWriter();
			tomlWriter.write(nightConfig, stringWriter);
			writer.write(stringWriter.toString());
			writer.flush();
		} catch (Exception e) {
			throw new ConfigurateException(e);
		}
	}

	@Override
	public CommentedConfigurationNode createNode(ConfigurationOptions options) {
		return CommentedConfigurationNode.root(options);
	}

	/**
	 * Extracts comments from TOML Config
	 */
	private Map<String, String> extractComments(CommentedConfig config, String path) {
		Map<String, String> result = new LinkedHashMap<>();
		for(Config.Entry entry : config.entrySet()) {
			String key = entry.getKey();
			String fullPath = path.isEmpty() ? key : path + "." + key;
			
			if(config instanceof CommentedConfig commented) {
				String comment = commented.getComment(key);
				if(comment != null && !comment.isEmpty()) {
					result.put(fullPath, comment);
				}
			}
			
			Object value = entry.getValue();
			if(value instanceof Config subConfig) {
				result.putAll(extractComments((CommentedConfig) subConfig, fullPath));
			}
		}
		return result;
	}

	/**
	 * Extracts comments from ConfigurationNode
	 */
	private Map<String, String> extractCommentsFromNode(ConfigurationNode node, String path) {
		Map<String, String> result = new LinkedHashMap<>();
		if(node.isMap()) {
			for(Map.Entry<Object, ? extends ConfigurationNode> entry : node.childrenMap().entrySet()) {
				String key = entry.getKey().toString();
				String fullPath = path.isEmpty() ? key : path + "." + key;
				ConfigurationNode child = entry.getValue();
				
				if(child instanceof CommentedConfigurationNode commented) {
					String comment = commented.comment();
					if(comment != null && !comment.isEmpty()) {
						result.put(fullPath, comment);
					}
				}
				
				if(child.isMap()) {
					result.putAll(extractCommentsFromNode(child, fullPath));
				}
			}
		}
		return result;
	}

	/**
	 * Restores comments in ConfigurationNode
	 */
	private void restoreComments(ConfigurationNode node, Map<String, String> comments, String currentPath) {
		if(node.isMap()) {
			for(Map.Entry<Object, ? extends ConfigurationNode> entry : node.childrenMap().entrySet()) {
				String key = entry.getKey().toString();
				String fullPath = currentPath.isEmpty() ? key : currentPath + "." + key;
				ConfigurationNode child = entry.getValue();
				
				String comment = comments.get(fullPath);
				if(comment != null && child instanceof CommentedConfigurationNode commented) {
					// Format comment
					if(comment.contains("\n")) {
						comment = String.join("\n", Arrays.stream(comment.split("\n"))
							.map(String::trim)
							.toArray(String[]::new));
					} else {
						comment = comment.trim();
					}
					commented.comment(comment);
				}
				
				if(child.isMap()) {
					restoreComments(child, comments, fullPath);
				}
			}
		}
	}

	/**
	 * Restores comments to TOML Config
	 */
	private void restoreCommentsToConfig(CommentedConfig config, Map<String, String> comments, String currentPath) {
		for(Config.Entry entry : config.entrySet()) {
			String key = entry.getKey();
			String fullPath = currentPath.isEmpty() ? key : currentPath + "." + key;
			
			String comment = comments.get(fullPath);
			if(comment != null && config instanceof CommentedConfig commented) {
				// Format comment for TOML
				if(comment.contains("\n")) {
					comment = String.join("\n", Stream.of(comment.split("\n")).map(line -> line.startsWith(" ") ? line : " " + line).toArray(String[]::new));
				} else if(!comment.startsWith(" ")) {
					comment = " " + comment;
				}
				commented.setComment(key, comment);
			}
			
			Object value = entry.getValue();
			if(value instanceof CommentedConfig subConfig) {
				restoreCommentsToConfig(subConfig, comments, fullPath);
			}
		}
	}

	/**
	 * Recursively converts all objects to primitive types
	 */
	private Map<String, Object> convertValuesToPrimitive(Map<String, Object> map) {
		Map<String, Object> result = new LinkedHashMap<>();
		for(Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			result.put(key, convertValueToPrimitive(value));
		}
		return result;
	}

	private boolean isAdventure(Object object) {
		return object instanceof Component || object instanceof NamedTextColor || object instanceof TextColor || object instanceof TextDecoration || object instanceof Key;
	}

	/**
	 * Converts Adventure object to a primitive type
	 */
	private Object convertAdventureToPrimitive(Object value) {
		if(value == null) return null;
		
		// Component -> JSON string
		if(value instanceof Component component) {
			return GsonComponentSerializer.gson().serialize(component);
		}
		
		// NamedTextColor -> color name
		if(value instanceof NamedTextColor color) {
			return color.toString();
		}
		
		// TextColor -> HEX string
		if(value instanceof TextColor color) {
			return color.asHexString();
		}
		
		// TextDecoration -> name
		if(value instanceof TextDecoration decoration) {
			return decoration.toString();
		}
		
		// Key -> string
		if(value instanceof Key key) {
			return key.asString();
		}
		
		return value;
	}

	/**
	 * Converts a value to a primitive type
	 */
	@SuppressWarnings("unchecked")
	private Object convertValueToPrimitive(Object value) {
		if(value == null) return null;

		if(isAdventure(value)) {
			value = convertAdventureToPrimitive(value);
		}
		
		// Character -> string
		if(value instanceof Character character) {
			return character.toString();
		}
		
		// Map -> recursive processing
		if(value instanceof Map) {
			return convertValuesToPrimitive((Map<String, Object>) value);
		}
		
		// List -> recursive processing
		if(value instanceof List) {
			List<Object> result = new ArrayList<>();
			for(Object item : (List<?>) value) {
				result.add(convertValueToPrimitive(item));
			}
			return result;
		}
		
		// Array -> recursive processing
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

	/**
	 * Recursively converts Config to plain Map
	 */
	private Map<String, Object> convertToPlainMap(Config config) {
		Map<String, Object> result = new LinkedHashMap<>();
		for(Config.Entry entry : config.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			
			if(value instanceof Config subConfig) {
				result.put(key, convertToPlainMap(subConfig));
			} else if(value instanceof List<?> list) {
				List<Object> convertedList = new ArrayList<>();
				for(Object item : list) {
					if(item instanceof Config itemConfig) {
						convertedList.add(convertToPlainMap(itemConfig));
					} else {
						convertedList.add(item);
					}
				}
				result.put(key, convertedList);
			} else {
				result.put(key, value);
			}
		}
		return result;
	}

	/**
	 * Converts TOML Config to JSON string
	 */
	private String convertTomlToJson(CommentedConfig tomlConfig) {
		JsonObject jsonObject = new JsonObject();
		copyConfigToJson(tomlConfig, jsonObject);
		return jsonObject.toString();
	}

	/**
	 * Recursively copies data from TOML Config to JsonObject
	 */
	private void copyConfigToJson(Config source, JsonObject target) {
		for(Config.Entry entry : source.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			
			if(value instanceof Config subConfig) {
				JsonObject subJson = new JsonObject();
				copyConfigToJson(subConfig, subJson);
				target.add(key, subJson);
			} else if(value instanceof List<?> list) {
				JsonArray jsonArray = new JsonArray();
				for(Object item : list) {
					if(item instanceof Config itemConfig) {
						JsonObject subJson = new JsonObject();
						copyConfigToJson(itemConfig, subJson);
						jsonArray.add(subJson);
					} else if(item instanceof String) {
						jsonArray.add(new JsonPrimitive((String) item));
					} else if(item instanceof Number) {
						jsonArray.add(new JsonPrimitive((Number) item));
					} else if(item instanceof Boolean) {
						jsonArray.add(new JsonPrimitive((Boolean) item));
					} else if(item != null) {
						jsonArray.add(new JsonPrimitive(item.toString()));
					}
				}
				target.add(key, jsonArray);
			} else if(value instanceof String) {
				target.addProperty(key, (String) value);
			} else if(value instanceof Number) {
				target.addProperty(key, (Number) value);
			} else if(value instanceof Boolean) {
				target.addProperty(key, (Boolean) value);
			} else if(value != null) {
				target.addProperty(key, value.toString());
			}
		}
	}

	/**
	 * Converts Map to TOML Config
	 */
	@SuppressWarnings("unchecked")
	private CommentedConfig convertMapToToml(Map<String, Object> map) {
		CommentedConfig result = TomlFormat.instance().createConfig();
		for(Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			
			if(value instanceof Map) {
				result.set(key, convertMapToToml((Map<String, Object>) value));
			} else if(value instanceof List<?> list) {
				List<Object> convertedList = new ArrayList<>();
				for(Object item : list) {
					if(item instanceof Map) {
						convertedList.add(convertMapToToml((Map<String, Object>) item));
					} else {
						convertedList.add(item);
					}
				}
				result.set(key, convertedList);
			} else {
				result.set(key, value);
			}
		}
		return result;
	}

	public static final class Builder extends AbstractConfigurationLoader.Builder<Builder, TomlConfigurationLoader> {

		private Builder() {}

		@Override
		public TomlConfigurationLoader build() {
			return new TomlConfigurationLoader(this);
		}
	}

}