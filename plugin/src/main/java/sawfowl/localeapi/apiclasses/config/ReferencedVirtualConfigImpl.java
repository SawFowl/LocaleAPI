package sawfowl.localeapi.apiclasses.config;

import java.util.Objects;

import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.ValueReference;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.config.ReferencedVirtualConfig;
import sawfowl.localeapi.api.config.VirtualConfig;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;

public class ReferencedVirtualConfigImpl<T, N extends ConfigurationNode> extends VirtualConfigImpl implements ReferencedVirtualConfig<T>{

	public static final <T> ReferencedVirtualConfigImpl<T, ConfigurationNode> create(ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, TypeSerializerCollection serializers, Class<T> clazz) {
		return new ReferencedVirtualConfigImpl<T, ConfigurationNode>(configType, itemStackSerializerType, serializers, clazz);
	}

	public static final <T> ReferencedVirtualConfigImpl<T, ConfigurationNode> create(ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, TypeSerializerCollection serializers, T object) {
		return new ReferencedVirtualConfigImpl<T, ConfigurationNode>(configType, itemStackSerializerType, serializers, object);
	}

	private ConfigurationReference<N> configurationReference;
	private ValueReference<T, N> valueReference;
	private Class<T> clazz;
	protected ReferencedVirtualConfigImpl(ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, TypeSerializerCollection serializers, Class<T> clazz) {
		super("", configType, itemStackSerializerType, serializers);
		Objects.requireNonNull(clazz);
		this.clazz = clazz;
		load();
		save();
	}

	@SuppressWarnings("unchecked")
	protected ReferencedVirtualConfigImpl(ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, TypeSerializerCollection serializers, T object) {
		super("", configType, itemStackSerializerType, serializers);
		Objects.requireNonNull(object);
		this.clazz = (Class<T>) object.getClass();
		load();
		save(object);
	}

	@Override
	public void loadFromRaw(String rawData) {
		Objects.requireNonNull(rawData);
		super.rawData = rawData;
		updateBuffers();
		load();
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
	public <C extends VirtualConfig> C load() {
		updateBuffers();
		try {
			configurationReference = (ConfigurationReference<N>) super.selectLoader().loadToReference();
			// configurationReference.load();
			valueReference = configurationReference.referenceTo(clazz);
		} catch (ConfigurateException e) {
			e.printStackTrace();
		}
		return (C) this;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public <C extends VirtualConfig> C save() {
		valueReference.setAndSave(get());
		updateRawData();
		load();
		return (C) this;
	}

	@Override
	public <E extends T> void save(E object) {
		valueReference.setAndSave(object);
		updateRawData();
		load();
	}

	@Override
	public void addSerializers(TypeSerializerCollection collection) {
		if(serializers == null) {
			serializers = collection;
		} else serializers = serializers.childBuilder().registerAll(collection).build();
		load();
	}

	@SuppressWarnings({ "unchecked", "hiding" })
	@Override
	public <T, O extends ReferencedVirtualConfig<T>> O toReference(T config) {
		return (O) this;
	}

	@SuppressWarnings({ "unchecked", "hiding" })
	@Override
	public <T, O extends ReferencedVirtualConfig<T>> O toReference(Class<T> config) {
		return (O) this;
	}

	@SuppressWarnings({ "unchecked", "hiding" })
	@Override
	public <T, O extends ReferencedVirtualConfig<T>> O toReference() {
		return (O) this;
	}

	@Override
	public boolean hasReferenced() {
		return true;
	}

}
