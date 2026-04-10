package sawfowl.localeapi.apiclasses.config.builders;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.plugin.PluginContainer;

import sawfowl.localeapi.LocaleAPI;
import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.config.ReferencedConfig;
import sawfowl.localeapi.api.config.builders.ReferencedConfigBuilder;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.apiclasses.config.ReferencedConfigImpl;

public class ReferencedBuilderImpl<T> implements ReferencedConfigBuilder<T> {

	private final Class<T> clazz;
	private final T value;
	private Path configDir;
	private String name;
	private ConfigTypes type;
	private ItemStackSerializerType itemStackSerializerType;
	private TypeSerializerCollection collection;
	boolean fromFile = false;
	private PluginContainer container;
	public ReferencedBuilderImpl(PluginContainer container, Class<T> type) {
		Objects.requireNonNull(type);
		this.container = container;
		this.clazz = type;
		this.value = null;
		if(LocaleAPI.getConfig() != null) this.type = LocaleAPI.getConfig().getConfigSettings(container).getType();
	}

	@SuppressWarnings("unchecked")
	public ReferencedBuilderImpl(PluginContainer container, T value) {
		Objects.requireNonNull(value);
		this.container = container;
		this.value = value;
		this.clazz = (Class<T>) value.getClass();
		if(LocaleAPI.getConfig() != null) this.type = LocaleAPI.getConfig().getConfigSettings(container).getType();
	}

	@Override
	public ReferencedConfigBuilder<T> fromFile(File file) {
		if(file.exists() && file.toPath().getParent() != null) {
			setPath(file.toPath().getParent());
			ConfigTypes type = ConfigTypes.getTypeByExtension(ConfigTypes.getExtension(file.getName()));
			if(type != null && type != ConfigTypes.UNKNOWN) {
				setType(type);
				setName(file.getName().replace(type.toString(), ""));
				fromFile = true;
			}
		}
		return this;
	}

	@Override
	public ReferencedConfigBuilder<T> setPath(Path configDir) {
		this.configDir = configDir;
		return this;
	}

	@Override
	public ReferencedConfigBuilder<T> setName(String name) {
		if(!fromFile) this.name = name;
		return this;
	}

	@Override
	public ReferencedConfigBuilder<T> setType(ConfigTypes type) {
		if(type != null && !fromFile) this.type = type;
		return this;
	}

	@Override
	public ReferencedConfigBuilder<T> setItemStackSerializerType(ItemStackSerializerType type) {
		this.itemStackSerializerType = type;
		return this;
	}

	@Override
	public ReferencedConfigBuilder<T> addSerializers(TypeSerializerCollection collection) {
		this.collection = collection;
		return this;
	}

	@Override
	public ReferencedConfig<T> build() {
		Objects.requireNonNull(configDir);
		Objects.requireNonNull(name);
		if(LocaleAPI.getConfig() != null) {
			if(LocaleAPI.getConfig().getConfigSettings(container).getSerialization().isForceUse()) {
				itemStackSerializerType = LocaleAPI.getConfig().getConfigSettings(container).getSerialization().getType();
			}
			if(LocaleAPI.getConfig().getConfigSettings(container).isForcedUse()) {
				ReferencedConfig<T> updated;
				ReferencedConfig<T> old = null;
				if(value == null) {
					updated = ReferencedConfigImpl.create(configDir, name, LocaleAPI.getConfig().getConfigSettings(container).getType(), itemStackSerializerType, collection, clazz);
					for(File file : configDir.toFile().listFiles()) {
						if(!file.getName().contains(name)) continue;
						type = ConfigTypes.getTypeByExtension(ConfigTypes.getExtension(file.getName()));
						if(type  == ConfigTypes.UNKNOWN || type.comparableType(LocaleAPI.getConfig().getConfigSettings(container).getType())) continue;
						if(old == null) {
							old = ReferencedConfigImpl.create(configDir, name, type, itemStackSerializerType, collection, clazz);
						} else file.delete();
					}
				} else {
					updated = ReferencedConfigImpl.create(configDir, name, LocaleAPI.getConfig().getConfigSettings(container).getType(), itemStackSerializerType, collection, value);
					for(File file : configDir.toFile().listFiles()) {
						if(!file.getName().contains(name)) continue;
						type = ConfigTypes.getTypeByExtension(ConfigTypes.getExtension(file.getName()));
						if(type  == ConfigTypes.UNKNOWN || type == LocaleAPI.getConfig().getConfigSettings(container).getType()) continue;
						if(old == null) {
							old = ReferencedConfigImpl.create(configDir, name, type, itemStackSerializerType, collection, clazz);
						} else file.delete();
					}
				}
				if(old != null) {
					updated.save(old.get());
					try {
						updated.getLoader().save(old.getRootNode());
					} catch (ConfigurateException e) {
						e.printStackTrace();
					}
					old.getPath().toFile().delete();
				}
				updated.load();
				return updated;
			}
		}
		Objects.requireNonNull(type);
		return value == null
			?
			ReferencedConfigImpl.create(configDir, name, type, itemStackSerializerType, collection, clazz)
			:
			ReferencedConfigImpl.create(configDir, name, type, itemStackSerializerType, collection, value);
	}

}
