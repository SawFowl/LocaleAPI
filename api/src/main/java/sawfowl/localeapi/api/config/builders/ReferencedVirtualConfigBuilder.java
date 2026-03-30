package sawfowl.localeapi.api.config.builders;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.config.ReferencedVirtualConfig;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;

public interface ReferencedVirtualConfigBuilder<T> {

	/**
	 * The type of the configuration.
	 */
	ReferencedVirtualConfigBuilder<T> setType(@NotNull ConfigTypes type);

	/**
	 * A variant of writing serialized data for an object with the ItemStack type.
	 */
	ReferencedVirtualConfigBuilder<T> setItemStackSerializerType(ItemStackSerializerType type);

	/**
	 * Additional serializers for your data.
	 */
	ReferencedVirtualConfigBuilder<T> addSerializers(TypeSerializerCollection collection);

	/**
	 * Creating configurations.
	 */
	ReferencedVirtualConfig<T>  build();

}
