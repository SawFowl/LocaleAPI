package sawfowl.localeapi.api.config.builders;

import java.nio.file.Path;

import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.config.Config;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;

public interface SimpleConfigBuilder {

	/**
	 * @return The path to the configuration file.
	 */
	SimpleConfigBuilder setPath(Path configDir);

	/**
	 * The name of your configuration file. You don't need to specify the type here.
	 */
	SimpleConfigBuilder setName(String name);

	/**
	 * The type of the configuration file.
	 */
	SimpleConfigBuilder setType(ConfigTypes type);

	/**
	 * A variant of writing serialized data for an object with the ItemStack type.
	 */
	SimpleConfigBuilder setItemStackSerializerType(ItemStackSerializerType type);

	/**
	 * Additional serializers for your data.
	 */
	SimpleConfigBuilder addSerializers(TypeSerializerCollection collection);

	/**
	 * Creating configurations.
	 */
	Config build();

}
