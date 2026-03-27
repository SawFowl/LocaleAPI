package sawfowl.localeapi.api.config;

import java.nio.file.Path;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.ValueReference;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.plugin.PluginContainer;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.api.services.ConfigurationService;

public interface ReferencedConfig<T> extends Config {

	/**
	 * Creating a configuration for your plugin.
	 * @deprecated use {@link ConfigurationService}
	 * 
	 * @param plugin - The PluginContainer of your plugin.
	 * @param configDir - The configuration directory of your plugin.
	 * @param name - The name of your configuration file. You don't need to specify the type here.
	 * @param configType - The type of your configuration file.
	 * @param itemStackSerializerType - A variant of writing serialized data for an object with the ItemStack type.
	 * @param clazz - The serializable class of your configuration.
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	static <T> ReferencedConfig<T> create(PluginContainer plugin, Path configDir, String name, ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, @Nullable TypeSerializerCollection serializers, Class<T> clazz) {
		return (ReferencedConfig<T>) ConfigurationService.getInstance().createReferencedConfig(plugin).setPath(configDir).setName(name).setType(configType).setItemStackSerializerType(itemStackSerializerType).addSerializers(serializers).buildWithType(clazz);
	}

	/**
	 * Creating a configuration for your plugin.
	 * 
	 * @param plugin - The PluginContainer of your plugin.
	 * @param configDir - The configuration directory of your plugin.
	 * @param name - The name of your configuration file. You don't need to specify the type here.
	 * @param configType - The type of your configuration file.
	 * @param itemStackSerializerType - A variant of writing serialized data for an object with the ItemStack type.
	 * @param object - An object of the serializable class of your configuration.
	 */
	@SuppressWarnings("unchecked")
	static <T> ReferencedConfig<T> create(PluginContainer plugin, Path configDir, String name, ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, @Nullable TypeSerializerCollection serializers, T object) {
		return (ReferencedConfig<T>) ConfigurationService.getInstance().createReferencedConfig(plugin).setPath(configDir).setName(name).setType(configType).setItemStackSerializerType(itemStackSerializerType).addSerializers(serializers).build(object);
	}

	/**
	 * Getting the configuration loader.
	 */
	<N extends ConfigurationNode> ConfigurationReference<N> getReference();

	/**
	 * See {@link ValueReference}
	 */
	<N extends ConfigurationNode> ValueReference<T, N> getValueReference();

	/**
	 * Saving an object of the serializable class to the current configuration.
	 */
	<E extends T> void save(E object);

	/**
	 * Retrieving an object of the serializable class from the current configuration.
	 */
	default T get() {
		return getValueReference().get();
	}

}
