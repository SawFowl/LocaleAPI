package sawfowl.localeapi.apiclasses.config.builders;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.plugin.PluginContainer;

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
	private PluginContainer container;
	public SimpleBuilderImpl(PluginContainer container) {
		if(LocaleAPI.getConfig() != null) this.type = LocaleAPI.getConfig().getConfigSettings(container).getType();
		this.container = container;
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
		if(LocaleAPI.getConfig() != null) {
			if(LocaleAPI.getConfig().getConfigSettings(container).getSerialization().isForceUse()) {
				itemStackSerializerType = LocaleAPI.getConfig().getConfigSettings(container).getSerialization().getType();
			}
			if(LocaleAPI.getConfig().getConfigSettings(container).isForcedUse()) {
				Config updated = ConfigImpl.create(configDir, name, LocaleAPI.getConfig().getConfigSettings(container).getType(), itemStackSerializerType, collection);
				Config old = null;
				updated = ConfigImpl.create(configDir, name, LocaleAPI.getConfig().getConfigSettings(container).getType(), itemStackSerializerType, collection);
				for(File file : configDir.toFile().listFiles()) {
					if(!file.getName().contains(name)) continue;
					type = ConfigTypes.getTypeByExtension(ConfigTypes.getExtension(file.getName()));
					if(type  == ConfigTypes.UNKNOWN || type.comparableType(LocaleAPI.getConfig().getConfigSettings(container).getType())) continue;
					if(old == null) {
						old = ConfigImpl.create(configDir, name, type, itemStackSerializerType, collection);;
					} else file.delete();
				}
				if(old != null) {
					try {
						updated.getLoader().save(old.getRootNode());
					} catch (ConfigurateException e) {
						e.printStackTrace();
					}
					old.getPath().toFile().delete();
				}
				return updated;
			}
		}
		Objects.requireNonNull(type);
		return ConfigImpl.create(configDir, name, type, itemStackSerializerType, collection);
	}

}
