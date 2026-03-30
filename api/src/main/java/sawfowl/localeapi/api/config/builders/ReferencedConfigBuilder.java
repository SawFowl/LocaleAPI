package sawfowl.localeapi.api.config.builders;

import java.nio.file.Path;

import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.config.ReferencedConfig;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;

public interface ReferencedConfigBuilder<T> {

	ReferencedConfigBuilder<T> setPath(Path configDir);

	ReferencedConfigBuilder<T> setName(String name);

	ReferencedConfigBuilder<T> setType(ConfigTypes type);

	ReferencedConfigBuilder<T> setItemStackSerializerType(ItemStackSerializerType type);

	ReferencedConfigBuilder<T> addSerializers(TypeSerializerCollection collection);

	ReferencedConfig<T>  build();

}
