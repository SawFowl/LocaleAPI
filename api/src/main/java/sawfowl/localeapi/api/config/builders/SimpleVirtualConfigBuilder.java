package sawfowl.localeapi.api.config.builders;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.config.VirtualConfig;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;

public interface SimpleVirtualConfigBuilder {

	/**
	 * With this method, you can specify which configuration data to load from the raw string.<br>
	 * If you do not specify anything, the configuration will be empty and can be used to write any other data and then retrieve it as a raw string.
	 */
	SimpleVirtualConfigBuilder setData(String rawData);

	/**
	 * The type of the configuration.
	 */
	SimpleVirtualConfigBuilder setType(@NotNull ConfigTypes type);

	/**
	 * A variant of writing serialized data for an object with the ItemStack type.
	 */
	SimpleVirtualConfigBuilder setItemStackSerializerType(ItemStackSerializerType type);

	/**
	 * Additional serializers for your data.
	 */
	SimpleVirtualConfigBuilder addSerializers(TypeSerializerCollection collection);

	/**
	 * Creating configurations.
	 */
	VirtualConfig build();

}
