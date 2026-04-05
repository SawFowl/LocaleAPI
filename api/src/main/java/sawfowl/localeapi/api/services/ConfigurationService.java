package sawfowl.localeapi.api.services;

import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.plugin.PluginContainer;

import com.google.gson.JsonObject;
import com.google.inject.Inject;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.config.builders.ReferencedConfigBuilder;
import sawfowl.localeapi.api.config.builders.ReferencedVirtualConfigBuilder;
import sawfowl.localeapi.api.config.builders.SimpleConfigBuilder;
import sawfowl.localeapi.api.config.builders.SimpleVirtualConfigBuilder;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;

public abstract class ConfigurationService {

	@Inject
	private static ConfigurationService INSTANCE;

	/**
	 * Getting the API.<br>
	 * You can use this method in your plugin's constructor if your plugin is loaded after LocaleAPI.
	 */
	public static ConfigurationService getInstance() {
		return INSTANCE;
	}

	/**
	 * Creating a simple configuration.
	 */
	public abstract SimpleConfigBuilder createSimpleConfig(PluginContainer container);

	/**
	 * Creating a reference configuration that accepts and returns a serializable object of the specified type.<br>
	 * See also {@link ConfigSerializable}
	 */
	public abstract <T> ReferencedConfigBuilder<T> createReferencedConfig(PluginContainer container, Class<T> type);

	/**
	 * Creating a reference configuration that accepts and returns a serializable object of the specified type.<br>
	 * See also {@link ConfigSerializable}
	 */
	public abstract <T> ReferencedConfigBuilder<T> createReferencedConfig(PluginContainer container, T value);

	/**
	 * Creating a simple virtual configuration.
	 */
	public abstract SimpleVirtualConfigBuilder createVirtualConfig();

	/**
	 * Creating a reference virtual configuration that accepts and returns a serializable object of the specified type.<br>
	 * See also {@link ConfigSerializable}
	 */
	public abstract <T> ReferencedVirtualConfigBuilder<T> createVirtualReferencedConfig(Class<T> type, String rawData);

	/**
	 * Creating a reference virtual configuration that accepts and returns a serializable object of the specified type.<br>
	 * See also {@link ConfigSerializable}
	 */
	public abstract <T> ReferencedVirtualConfigBuilder<T> createVirtualReferencedConfig(Class<T> type, JsonObject rawData);

	/**
	 * Creating a reference virtual configuration that accepts and returns a serializable object of the specified type.<br>
	 * See also {@link ConfigSerializable}
	 */
	public abstract <T> ReferencedVirtualConfigBuilder<T> createVirtualReferencedConfig(T value);

	/**
	 * Creating a configuration loader with a type.
	 * The server administrator will not be able to override the configuration type created using this method.
	 * 
	 * @param <C>  - Configuration node processing class. Note that `{@link CommentedConfigurationNode}` is not suitable for configurations in Json format.
	 * @param path - Path to the configuration file.
	 * @param configType - Configuration Type. To avoid errors, it must point to the same class loader as the `Class<T> loaderClass` parameter.
	 * @param serializerType - The type of item serialization used. See {@linkplain #selectSerializersCollection(ItemStackSerializerType)}
	 * @return
	 */
	public abstract <C extends ConfigurationNode> ConfigurationLoader<C> createConfigLoader(Path path, ConfigTypes configType, ItemStackSerializerType serializerType, @Nullable TypeSerializerCollection otherSerializers);

	/**
	 * Selecting serialization variant for items.<br>
	 * <b>SIMPLE</b> - All components will be written in 1 line. This option is the most reliable, but it makes manual editing of components in the configuration much more difficult.<br>
	 * <b>JSON</b> - Advanced recording. Easier to make manual changes to the config. If you have problems with this type of serialization, you should report errors to the LocaleAPI plugin developer.<br>
	 * <b>SPONGE</b> - Using Sponge serializer. Some data will be written in 1 line. If you encounter problems with this type of serialization, you should report bugs to the Sponge developers.<br>
	 */
	public abstract ConfigurationOptions selectOptions(ItemStackSerializerType serializerType);

	/**
	 * Selecting serialization variant for items.<br>
	 * <b>SIMPLE</b> - All components will be written in 1 line. This option is the most reliable, but it makes manual editing of components in the configuration much more difficult.<br>
	 * <b>JSON</b> - Advanced recording. Easier to make manual changes to the config. If you have problems with this type of serialization, you should report errors to the LocaleAPI plugin developer.<br>
	 * <b>SPONGE</b> - Using Sponge serializer. Some data will be written in 1 line. If you encounter problems with this type of serialization, you should report bugs to the Sponge developers.<br>
	 */
	public abstract TypeSerializerCollection selectSerializersCollection(ItemStackSerializerType serializerType);

	/**
	 * Creating a virtual configuration section.
	 */
	public abstract ConfigurationNode createVirtualNode(@Nullable ItemStackSerializerType serializerType);

	/**
	 * Combining serializer collections into a single collection.
	 */
	public abstract TypeSerializerCollection mergeSerializers(@NotNull TypeSerializerCollection first, @Nullable TypeSerializerCollection second);

}
