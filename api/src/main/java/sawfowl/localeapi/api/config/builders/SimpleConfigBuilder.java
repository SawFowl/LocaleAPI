package sawfowl.localeapi.api.config.builders;

import java.nio.file.Path;

import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.config.Config;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;

public interface SimpleConfigBuilder {

	SimpleConfigBuilder setPath(Path configDir);

	SimpleConfigBuilder setName(String name);

	SimpleConfigBuilder setType(ConfigTypes type);

	SimpleConfigBuilder setItemStackSerializerType(ItemStackSerializerType type);

	SimpleConfigBuilder addSerializers(TypeSerializerCollection collection);

	Config build();

}
