package sawfowl.localeapi.api.config.builders;

import java.nio.file.Path;

import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.config.Config;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;

public interface SimpleBuilder {

	SimpleBuilder setPath(Path configDir);

	SimpleBuilder setName(String name);

	SimpleBuilder setType(ConfigTypes type);

	SimpleBuilder setItemStackSerializerType(ItemStackSerializerType type);

	SimpleBuilder addSerializers(TypeSerializerCollection collection);

	Config build();

}
