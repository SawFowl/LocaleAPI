package sawfowl.localeapi.api.config.builders;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.config.VirtualConfig;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;

public interface SimpleVirtualConfigBuilder {

	SimpleVirtualConfigBuilder setData(String rawData);

	SimpleVirtualConfigBuilder setType(@NotNull ConfigTypes type);

	SimpleVirtualConfigBuilder setItemStackSerializerType(ItemStackSerializerType type);

	SimpleVirtualConfigBuilder addSerializers(TypeSerializerCollection collection);

	VirtualConfig build();

}
