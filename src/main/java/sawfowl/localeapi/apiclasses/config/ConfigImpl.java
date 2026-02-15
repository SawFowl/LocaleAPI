package sawfowl.localeapi.apiclasses.config;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.plugin.PluginContainer;

import io.leangen.geantyref.TypeToken;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.config.Config;
import sawfowl.localeapi.api.config.ReferencedConfig;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.api.serializetools.SerializeOptions;

public class ConfigImpl implements Config {

	public static final ConfigImpl create(PluginContainer plugin, Path configDir, String name, ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, TypeSerializerCollection serializers) {
		return new ConfigImpl(plugin, configDir, name, configType, itemStackSerializerType, serializers);
	}

	private final ConfigTypes type;
	private final ItemStackSerializerType itemStackSerializerType;
	private ConfigurationNode node;
	private ConfigurationLoader<? extends ConfigurationNode> loader;
	private ReferencedConfig<?> referenced;
	private final PluginContainer container;
	private Path path;
	private String name;
	private TypeSerializerCollection serializers;
	protected ConfigImpl(PluginContainer plugin, Path configDir, String name, ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, TypeSerializerCollection serializers) {
		this.container = plugin;
		this.path = configDir.resolve(name + configType.toString());
		this.type = configType;
		this.name = name;
		this.itemStackSerializerType = itemStackSerializerType;
		this.serializers = serializers;
		if(!(this instanceof ReferencedConfigImpl)) load();
	}

	@Override
	public Path getPath() {
		return path;
	}

	@Override
	public ConfigTypes getType() {
		return type;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <N extends ConfigurationNode> N getRootNode() {
		return (N) node;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <N extends ConfigurationNode, L extends ConfigurationLoader<N>> L getLoader() {
		return (L) loader;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, O extends ReferencedConfig<T>> O toReference(T config) {
		Objects.requireNonNull(config);
		return (O) (referenced == null ? (referenced = ReferencedConfigImpl.create(getContainer(), path, getName(), type, getItemStackSerializerType(), serializers, config)) : referenced);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, O extends ReferencedConfig<T>> O toReference(Class<T> config) {
		Objects.requireNonNull(config);
		return (O) (referenced == null ? (referenced = ReferencedConfigImpl.create(getContainer(), path, getName(), type, getItemStackSerializerType(), serializers, config)) : referenced);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, O extends ReferencedConfig<T>> O toReference() {
		return (O) referenced;
	}

	@Override
	public <T> boolean addIfNotExist(T object, @Nullable String comment, TypeToken<T> token, Object... path) {
		Objects.requireNonNull(object);
		Objects.requireNonNull(token);
		Objects.requireNonNull(path);
		if(getRootNode().node(path).virtual()) {
			try {
				getRootNode().node(path).set(token, object);
				if(comment != null && getRootNode() instanceof CommentedConfigurationNode commented) commented.node(path).comment(comment);
				return true;
			} catch (SerializationException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public <T> boolean addIfNotExist(T object, @Nullable String comment, Object... path) {
		Objects.requireNonNull(object);
		Objects.requireNonNull(path);
		if(getRootNode().node(path).virtual()) {
			try {
				getRootNode().node(path).set(object.getClass(), object);
				if(comment != null && getRootNode() instanceof CommentedConfigurationNode commented) commented.node(path).comment(comment);
				return true;
			} catch (SerializationException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public <T> boolean addIfNotExist(List<T> object, @Nullable String comment, TypeToken<T> token, Object... path) {
		Objects.requireNonNull(object);
		Objects.requireNonNull(token);
		Objects.requireNonNull(path);
		if(getRootNode().node(path).virtual()) {
			try {
				getRootNode().node(path).setList(token, object);
				if(comment != null && getRootNode() instanceof CommentedConfigurationNode commented) commented.node(path).comment(comment);
				return true;
			} catch (SerializationException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public <T> boolean addIfNotExist(Class<T> clazz, List<T> object, @Nullable String comment, Object... path) {
		Objects.requireNonNull(object);
		Objects.requireNonNull(clazz);
		Objects.requireNonNull(path);
		if(getRootNode().node(path).virtual()) {
			try {
				getRootNode().node(path).setList(clazz, object);
				if(comment != null && getRootNode() instanceof CommentedConfigurationNode commented) commented.node(path).comment(comment);
				return true;
			} catch (SerializationException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public void addSerializers(TypeSerializerCollection collection) {
		try {
			loader = selectBuilder(type).path(path).defaultOptions(options -> options.serializers(serializers -> serializers.registerAll(loader.defaultOptions().serializers()).registerAll(collection))).build();
			node = loader.load();
		} catch (ConfigurateException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean fileExist() {
		return path.toFile().exists();
	}

	@Override
	public boolean hasReferenced() {
		return referenced != null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Config> C load() {
		try {
			if(loader == null) loader = selectBuilder(type).path(path).build();
			node = loader.load();
		} catch (ConfigurateException e) {
			e.printStackTrace();
		}
		return (C) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Config> C save() {
		try {
			loader.save(node);
		} catch (ConfigurateException e) {
			e.printStackTrace();
		}
		return (C) this;
	}

	@SuppressWarnings("unchecked")
	<B extends AbstractConfigurationLoader.Builder<B, ?>> B selectBuilder(ConfigTypes loaderType) {
		switch (loaderType) {
			case YAML: return (B) SerializeOptions.createYamlConfigurationLoader(itemStackSerializerType, serializers).path(path);
			//case XML: return (B) XmlConfigurationLoader.builder().defaultOptions(ConfigOptions.OPTIONS).writesExplicitType(true);
			case JSON: return (B) SerializeOptions.createJsonConfigurationLoader(itemStackSerializerType, serializers).path(path);
			//case JACKSON: return (B) JacksonConfigurationLoader.builder().defaultOptions(ConfigOptions.OPTIONS).fieldValueSeparatorStyle(FieldValueSeparatorStyle.SPACE_BOTH_SIDES);
			default: return (B) SerializeOptions.createHoconConfigurationLoader(itemStackSerializerType, serializers).path(path);
		}
	}

	protected PluginContainer getContainer() {
		return container;
	}

	protected String getName() {
		return name;
	}

	protected ItemStackSerializerType getItemStackSerializerType() {
		return itemStackSerializerType;
	}

}
