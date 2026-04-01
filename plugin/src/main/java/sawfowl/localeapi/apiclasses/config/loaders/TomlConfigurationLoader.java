package sawfowl.localeapi.apiclasses.config.loaders;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlWriter;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.CommentHandler;
import org.spongepowered.configurate.loader.CommentHandlers;
import org.spongepowered.configurate.loader.ParsingException;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
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
		int lineNumber = 0;
		try {
			Path tempFile = Files.createTempFile(".temp-toml-config", ".toml");
			tempFile.toFile().deleteOnExit();
			try(BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
				String line;
				while((line = reader.readLine()) != null) {
					writer.write(line);
					writer.newLine();
					lineNumber++;
				}
			}
			CommentedFileConfig nightConfig = CommentedFileConfig.builder(tempFile)
					.autosave()
					.preserveInsertionOrder()
					.build();
			nightConfig.load();
			convertToConfigurate(nightConfig, node);
			nightConfig.close();
		} catch(IOException e) {
			throw new ParsingException(node, lineNumber, 0, null, "Error parsing node " + node.key() == null ? "nulled" : node.key().toString(), e);
		}
	}

	@Override
	protected void saveInternal(ConfigurationNode node, Writer writer) throws ConfigurateException {
		try {
			CommentedConfig nightConfig = TomlFormat.instance().createConfig();
			convertToNightConfig(node, nightConfig);
			TomlWriter tomlWriter = new TomlWriter();
			StringWriter stringWriter = new StringWriter();
			tomlWriter.write(nightConfig, stringWriter);
			writer.write(stringWriter.toString());
			writer.flush();
		} catch(Exception e) {
			throw new ConfigurateException(e);
		}
	}

	@Override
	public CommentedConfigurationNode createNode(ConfigurationOptions options) {
		return CommentedConfigurationNode.root(options);
	}

	private void convertValueToConfigurate(Object value, ConfigurationNode target) {
		if(value == null) return;
		try {
			target.set(CollectionValueConverter.fromNightConfigCompatible(value));
		} catch(SerializationException e) {
			e.printStackTrace();
		}
	}

	private void convertToConfigurate(Config source, ConfigurationNode target) {
		for(Config.Entry entry : source.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if(value instanceof Config config) {
				convertToConfigurate(config, target.node(key));
			} else if(value instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) value;
				for(Map.Entry<String, Object> mapEntry : map.entrySet()) {
					convertValueToConfigurate(mapEntry.getValue(), target.node(key, mapEntry.getKey()));
				}
			} else convertValueToConfigurate(value, target.node(key));
		}
		if(source instanceof CommentedConfig commentedSource) {
			for(Config.Entry entry : source.entrySet()) {
				String key = entry.getKey();
				String comment = commentedSource.getComment(key);
				if(comment == null || comment.isEmpty()) continue;
				if(comment.contains("\n")) {
					comment = String.join("\n", Stream.of(comment.split("\n")).map(str -> str.trim()).toArray(String[]::new));
				} else comment = comment.trim();
				if(target.node(key) instanceof CommentedConfigurationNode commentedNode) commentedNode.comment(comment);
			}
		}
	}

	private void convertToNightConfig(ConfigurationNode source, Config target) {
		if(source.isMap()) {
			for(Map.Entry<Object, ? extends ConfigurationNode> entry : source.childrenMap().entrySet()) {
				if(entry.getKey() == null) continue;
				String key = entry.getKey().toString();
				ConfigurationNode child = entry.getValue();
				Object value;
				if(child.isMap()) {
					Config subConfig = TomlFormat.instance().createConfig();
					convertToNightConfig(child, subConfig);
					value = subConfig;
				} else if(child.isList()) {
					Object raw = child.raw();
					if(raw instanceof Object[] array) {
						value = Stream.of(array).map(CollectionValueConverter::toNightConfigCompatible).toArray();
					} else value = CollectionValueConverter.toNightConfigCompatible(raw);
				} else value = CollectionValueConverter.toNightConfigCompatible(child.raw());
				target.set(key, value);
				if(target instanceof CommentedConfig commentedConfig && child instanceof CommentedConfigurationNode commentedNode && commentedNode.comment() != null && !commentedNode.comment().isEmpty()) {
					String comment = commentedNode.comment();
					if(comment.contains("\n")) {
						comment = String.join("\n", Stream.of(comment.split("\n")).map(str -> " " + str).toArray(String[]::new));
					} else if(!comment.startsWith(" ")) comment = " " + comment;
					commentedConfig.setComment(key, comment);
				}
			}
		}
	}

	public static final class Builder extends AbstractConfigurationLoader.Builder<Builder, TomlConfigurationLoader> {

		private Builder(){}

		@Override
		public TomlConfigurationLoader build() {
			return new TomlConfigurationLoader(this);
		}
	}
}