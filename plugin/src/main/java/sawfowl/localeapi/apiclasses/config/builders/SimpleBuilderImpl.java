package sawfowl.localeapi.apiclasses.config.builders;

import java.nio.file.Path;
import java.util.Objects;

import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import sawfowl.localeapi.LocaleAPI;
import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.config.Config;
import sawfowl.localeapi.api.config.builders.SimpleConfigBuilder;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.apiclasses.config.ConfigImpl;

public class SimpleBuilderImpl implements SimpleConfigBuilder {

	private Path configDir;
	private String name;
	private ConfigTypes type;
	private ItemStackSerializerType itemStackSerializerType;
	private TypeSerializerCollection collection;
	public SimpleBuilderImpl() {
		if(LocaleAPI.getConfig() != null) this.type = LocaleAPI.getConfig().getConfigSettings().getType();
	}

	@Override
	public SimpleConfigBuilder setPath(Path configDir) {
		this.configDir = configDir;
		return this;
	}

	@Override
	public SimpleConfigBuilder setName(String name) {
		this.name = name;
		return this;
	}

	@Override
	public SimpleConfigBuilder setType(ConfigTypes type) {
		if(type != null) this.type = type;
		return this;
	}

	@Override
	public SimpleConfigBuilder setItemStackSerializerType(ItemStackSerializerType type) {
		this.itemStackSerializerType = type;
		return this;
	}

	@Override
	public SimpleConfigBuilder addSerializers(TypeSerializerCollection collection) {
		this.collection = collection;
		return this;
	}

	@Override
	public Config build() {
		Objects.requireNonNull(configDir);
		Objects.requireNonNull(name);
		if(LocaleAPI.getConfig() != null && LocaleAPI.getConfig().getConfigSettings().isForcedUse() && type != null && !type.comparableType(LocaleAPI.getConfig().getConfigSettings().getType())) {
			Config updated = ConfigImpl.create(configDir, name, LocaleAPI.getConfig().getConfigSettings().getType(), itemStackSerializerType, collection);
			if(configDir.resolve(name + type.toString()).toFile().exists()) {
				Config old = ConfigImpl.create(configDir, name, type, itemStackSerializerType, collection);
				try {
					updated.getLoader().save(old.getRootNode());
				} catch (ConfigurateException e) {
					e.printStackTrace();
				}
				old.getPath().toFile().delete();
				old = null;
			}
			return updated;
		} else Objects.requireNonNull(type);
		return ConfigImpl.create(configDir, name, type, itemStackSerializerType, collection);
	}

}
