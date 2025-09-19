package sawfowl.localeapi.api.config;

import java.nio.file.Path;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.ValueReference;
import org.spongepowered.plugin.PluginContainer;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.apiclasses.config.ReferencedConfigImpl;

public interface ReferencedConfig<T> extends Config {

	@SuppressWarnings("unchecked")
	static <T> ReferencedConfig<T> create(PluginContainer plugin, Path configDir, String name, ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, Class<T> clazz) {
		return (ReferencedConfig<T>) ReferencedConfigImpl.create(plugin, configDir, name, configType, itemStackSerializerType);
	}

	static <T> ReferencedConfig<T> create(PluginContainer plugin, Path configDir, String name, ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, T object) {
		return (ReferencedConfig<T>) ReferencedConfigImpl.create(plugin, configDir, name, configType, itemStackSerializerType, object);
	}

	<N extends ConfigurationNode> ConfigurationReference<N> getReference();

	<N extends ConfigurationNode> ValueReference<T, N> getValueReference();

	<E extends T> void save(E object);

	default T get() {
		return getValueReference().get();
	}

}
