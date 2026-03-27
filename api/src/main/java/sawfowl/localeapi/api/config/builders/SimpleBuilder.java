package sawfowl.localeapi.api.config.builders;

import java.nio.file.Path;

import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.config.Config;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;

public interface SimpleBuilder<C extends Config> {

	SimpleBuilder<C> setPath(Path configDir);

	SimpleBuilder<C> setName(String name);

	SimpleBuilder<C> setType(ConfigTypes type);

	SimpleBuilder<C> setItemStackSerializerType(ItemStackSerializerType type);

	SimpleBuilder<C> addSerializers(TypeSerializerCollection collection);

	C build();

}
