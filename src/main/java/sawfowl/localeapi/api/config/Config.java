package sawfowl.localeapi.api.config;

import java.nio.file.Path;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.plugin.PluginContainer;

import io.leangen.geantyref.TypeToken;
import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.apiclasses.config.ConfigImpl;

public interface Config {

	static Config create(PluginContainer plugin, Path configDir, String name, ConfigTypes configType, ItemStackSerializerType itemStackSerializerType) {
		return ConfigImpl.create(plugin, configDir, name, configType, itemStackSerializerType);
	}

	Path getPath();

	ConfigTypes getType();

	<N extends ConfigurationNode> N getRootNode();

	<N extends ConfigurationNode, L extends ConfigurationLoader<N>> L getLoader();

	<T, O extends ReferencedConfig<T>> O toReference(T config);

	<T, O extends ReferencedConfig<T>> O toReference(Class<T> config);

	@Nullable <T, O extends ReferencedConfig<T>> O toReference();

	<T> boolean addIfNotExist(T object, @Nullable String comment, TypeToken<T> token, Object... path);

	void addSerializers(TypeSerializerCollection collection);

	boolean fileExist();

	boolean hasReferenced();

	void load();

	void save();

	default boolean contains(Object... path) {
		return !getRootNode().node(path).virtual();
	}

	default String getString(Object... path) {
		return getRootNode().node(path).getString();
	}

	default int getInt(Object... path) {
		return getRootNode().node(path).getInt();
	}

	default long getLong(Object... path) {
		return getRootNode().node(path).getLong();
	}

	default double getDouble(Object... path) {
		return getRootNode().node(path).getDouble();
	}

	default float getFloat(Object... path) {
		return getRootNode().node(path).getFloat();
	}

	default boolean getBoolean(Object... path) {
		return getRootNode().node(path).getBoolean();
	}

}
