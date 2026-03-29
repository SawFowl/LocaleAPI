package sawfowl.localeapi.apiclasses.config.builders;

import java.nio.file.Path;
import java.util.Objects;

import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.plugin.PluginContainer;

import sawfowl.localeapi.LocaleAPI;
import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.config.ReferencedConfig;
import sawfowl.localeapi.api.config.builders.ReferencedBuilder;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.apiclasses.config.ReferencedConfigImpl;

public class ReferencedBuilderImpl<T> implements ReferencedBuilder<T> {

	private final PluginContainer container;
	private final Class<T> clazz;
	private final T value;
	private Path configDir;
	private String name;
	private ConfigTypes type;
	private ItemStackSerializerType itemStackSerializerType;
	private TypeSerializerCollection collection;
	public ReferencedBuilderImpl(PluginContainer container, Class<T> type) {
		Objects.requireNonNull(container);
		Objects.requireNonNull(type);
		this.container = container;
		this.clazz = type;
		this.value = null;
		if(LocaleAPI.getConfig() != null) this.type = LocaleAPI.getConfig().getConfigSettings().getType();
	}

	@SuppressWarnings("unchecked")
	public ReferencedBuilderImpl(PluginContainer container, T value) {
		Objects.requireNonNull(container);
		Objects.requireNonNull(value);
		this.container = container;
		this.value = value;
		this.clazz = (Class<T>) value.getClass();
		if(LocaleAPI.getConfig() != null) this.type = LocaleAPI.getConfig().getConfigSettings().getType();
	}

	@Override
	public ReferencedBuilder<T> setPath(Path configDir) {
		this.configDir = configDir;
		return this;
	}

	@Override
	public ReferencedBuilder<T> setName(String name) {
		this.name = name;
		return this;
	}

	@Override
	public ReferencedBuilder<T> setType(ConfigTypes type) {
		if(type != null) this.type = type;
		return this;
	}

	@Override
	public ReferencedBuilder<T> setItemStackSerializerType(ItemStackSerializerType type) {
		this.itemStackSerializerType = type;
		return this;
	}

	@Override
	public ReferencedBuilder<T> addSerializers(TypeSerializerCollection collection) {
		this.collection = collection;
		return this;
	}

	@Override
	public ReferencedConfig<T> build() {
		Objects.requireNonNull(configDir);
		Objects.requireNonNull(name);
		if(LocaleAPI.getConfig() != null && LocaleAPI.getConfig().getConfigSettings().isForcedUse() && type != null && !type.comparableType(LocaleAPI.getConfig().getConfigSettings().getType())) {
			if(value == null) {
				ReferencedConfig<T> updated = ReferencedConfigImpl.create(container, configDir, name, LocaleAPI.getConfig().getConfigSettings().getType(), itemStackSerializerType, collection, clazz);
				if(configDir.resolve(name + type.toString()).toFile().exists()) {
					ReferencedConfig<T> old = ReferencedConfigImpl.create(container, configDir, name, type, itemStackSerializerType, collection, clazz);
					updated.save(old.get());
					try {
						updated.getLoader().save(old.getRootNode());
					} catch (ConfigurateException e) {
						e.printStackTrace();
					}
					old.getPath().toFile().delete();
					old = null;
				}
				return updated;
			} else {
				ReferencedConfig<T> updated = ReferencedConfigImpl.create(container, configDir, name, LocaleAPI.getConfig().getConfigSettings().getType(), itemStackSerializerType, collection, value);
				if(configDir.resolve(name + type.toString()).toFile().exists()) {
					ReferencedConfig<T> old = ReferencedConfigImpl.create(container, configDir, name, type, itemStackSerializerType, collection, value);
					updated.save(old.get());
					try {
						updated.getLoader().save(old.getRootNode());
					} catch (ConfigurateException e) {
						e.printStackTrace();
					}
					old.getPath().toFile().delete();
					old = null;
				}
				return updated;
			}
		} else Objects.requireNonNull(type);
		return value == null
			?
			ReferencedConfigImpl.create(container, configDir, name, type, itemStackSerializerType, collection, clazz)
			:
			ReferencedConfigImpl.create(container, configDir, name, type, itemStackSerializerType, collection, value);
	}

}
