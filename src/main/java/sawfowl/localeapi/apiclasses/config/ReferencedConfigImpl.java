package sawfowl.localeapi.apiclasses.config;

import java.nio.file.Path;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.ValueReference;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.plugin.PluginContainer;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.config.Config;
import sawfowl.localeapi.api.config.ReferencedConfig;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;

public class ReferencedConfigImpl<T, N extends ConfigurationNode> extends ConfigImpl implements ReferencedConfig<T>{

	public static final <T> ReferencedConfigImpl<T, ConfigurationNode> create(PluginContainer plugin, Path configDir, String name, ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, Class<T> clazz) {
		return new ReferencedConfigImpl<T, ConfigurationNode>(plugin, configDir, name, configType, itemStackSerializerType, clazz);
	}

	public static final <T> ReferencedConfigImpl<T, ConfigurationNode> create(PluginContainer plugin, Path configDir, String name, ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, T object) {
		return new ReferencedConfigImpl<T, ConfigurationNode>(plugin, configDir, name, configType, itemStackSerializerType, object);
	}

	private ConfigurationReference<N> configurationReference;
	private ValueReference<T, N> valueReference;
	private Class<T> clazz;
	protected ReferencedConfigImpl(PluginContainer plugin, Path configDir, String name, ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, Class<T> clazz) {
		super(plugin, configDir, name, configType, itemStackSerializerType);
		this.clazz = clazz;
		load();
		if(!getPath().toFile().exists()) save();
	}

	@SuppressWarnings("unchecked")
	protected ReferencedConfigImpl(PluginContainer plugin, Path configDir, String name, ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, T object) {
		super(plugin, configDir, name, configType, itemStackSerializerType);
		this.clazz = (Class<T>) object.getClass();
		load();
		if(!getPath().toFile().exists()) save();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ConfigurationReference<N> getReference() {
		return configurationReference;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ValueReference<T, N> getValueReference() {
		return valueReference;
	}

	@SuppressWarnings({ "unchecked", "hiding" })
	@Override
	public <N extends ConfigurationNode, L extends ConfigurationLoader<N>> L getLoader() {
		return (L) getReference().loader();
	}

	@SuppressWarnings("unchecked")
	@Override
	public N getRootNode() {
		return getValueReference().node();
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public <C extends Config> C load() {
		try {
			configurationReference = (ConfigurationReference<N>) getLoader().loadToReference();
			configurationReference.load();
			valueReference = configurationReference.referenceTo(clazz);
		} catch (ConfigurateException e) {
			e.printStackTrace();
		}
		return (C) this;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public <C extends Config> C save() {
		valueReference.setAndSave(get());
		return (C) this;
	}

	@Override
	public <E extends T> void save(E object) {
		valueReference.setAndSave(object);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addSerializers(TypeSerializerCollection collection) {
		try {
			configurationReference = (ConfigurationReference<N>) selectBuilder(getType()).path(getPath()).defaultOptions(options -> options.serializers(serializers -> serializers.registerAll(configurationReference.loader().defaultOptions().serializers()).registerAll(collection))).build().loadToReference();
			configurationReference.load();
			valueReference = configurationReference.referenceTo(clazz);
		} catch (ConfigurateException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "unchecked", "hiding" })
	@Override
	public <T, O extends ReferencedConfig<T>> O toReference(T config) {
		return (O) this;
	}

	@SuppressWarnings({ "unchecked", "hiding" })
	@Override
	public <T, O extends ReferencedConfig<T>> O toReference(Class<T> config) {
		return (O) this;
	}

	@SuppressWarnings({ "unchecked", "hiding" })
	@Override
	public <T, O extends ReferencedConfig<T>> O toReference() {
		return (O) this;
	}

	@Override
	public boolean hasReferenced() {
		return true;
	}

}
