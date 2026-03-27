package sawfowl.localeapi.api.config.builders;

import java.nio.file.Path;

import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.config.ReferencedConfig;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;

public interface ReferencedBuilder<T> {

	ReferencedBuilder<T> setPath(Path configDir);

	ReferencedBuilder<T> setName(String name);

	ReferencedBuilder<T> setType(ConfigTypes type);

	ReferencedBuilder<T> setItemStackSerializerType(ItemStackSerializerType type);

	ReferencedBuilder<T> addSerializers(TypeSerializerCollection collection);

	<C extends T> ReferencedConfig<T> buildWithType(Class<C> type);

	ReferencedConfig<T>  build(T object);

}
