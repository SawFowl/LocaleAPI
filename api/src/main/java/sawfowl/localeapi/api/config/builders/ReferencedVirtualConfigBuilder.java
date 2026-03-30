package sawfowl.localeapi.api.config.builders;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.config.ReferencedVirtualConfig;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;

public interface ReferencedVirtualConfigBuilder<T> {

	ReferencedVirtualConfigBuilder<T> setType(@NotNull ConfigTypes type);

	ReferencedVirtualConfigBuilder<T> setItemStackSerializerType(ItemStackSerializerType type);

	ReferencedVirtualConfigBuilder<T> addSerializers(TypeSerializerCollection collection);

	ReferencedVirtualConfig<T>  build();

}
