package sawfowl.localeapi.apiclasses.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import io.leangen.geantyref.TypeToken;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.config.ReferencedVirtualConfig;
import sawfowl.localeapi.api.config.VirtualConfig;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.apiclasses.services.ConfigurationServiceImplement;

public class VirtualConfigImpl implements VirtualConfig {

	public static VirtualConfigImpl create(String rawData, ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, TypeSerializerCollection serializers) {
		return new VirtualConfigImpl(rawData, configType, itemStackSerializerType, serializers);
	}

	private final ConfigTypes type;
	private final ItemStackSerializerType itemStackSerializerType;
	protected String rawData = "";
	protected TypeSerializerCollection serializers;
	private ConfigurationNode node;
	private ConfigurationLoader<? extends ConfigurationNode> loader;
	private BufferedWriter bufferedWriter;
	private StringWriter stringWriter;
	private BufferedReader bufferedReader;
	private StringReader stringReader;
	private ReferencedVirtualConfig<?> referenced;
	public VirtualConfigImpl(String rawData, ConfigTypes configType, ItemStackSerializerType itemStackSerializerType, TypeSerializerCollection serializers) {
		this.rawData = rawData;
		this.type = configType;
		this.itemStackSerializerType = itemStackSerializerType;
		this.serializers = serializers;
		if(!(this instanceof ReferencedVirtualConfigImpl)) load();
	}

	@Override
	public VirtualConfig loadFromRaw(String rawData) {
		Objects.requireNonNull(rawData);
		this.rawData = rawData;
		updateBuffers();
		load();
		return this;
	}

	@Override
	public String getRawData() {
		return rawData;
	}

	@Override
	public ConfigTypes getType() {
		return type;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <N extends ConfigurationNode> N getRootNode() {
		return (N) node;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <N extends ConfigurationNode, L extends ConfigurationLoader<N>> L getLoader() {
		return (L) loader;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, O extends ReferencedVirtualConfig<T>> O toReference(T config) {
		return (O) (referenced == null ? (referenced = ReferencedVirtualConfigImpl.create(type, itemStackSerializerType, serializers, config)) : referenced);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, O extends ReferencedVirtualConfig<T>> O toReference(Class<T> config) {
		return (O) (referenced == null ? (referenced = ReferencedVirtualConfigImpl.create(type, itemStackSerializerType, serializers, config)) : referenced);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, O extends ReferencedVirtualConfig<T>> @Nullable O toReference() {
		return (@Nullable O) referenced;
	}

	@Override
	public <T> boolean addIfNotExist(T object, @Nullable String comment, TypeToken<T> token, Object... path) {
		Objects.requireNonNull(object);
		Objects.requireNonNull(token);
		Objects.requireNonNull(path);
		if(getRootNode().node(path).virtual()) {
			try {
				getRootNode().node(path).set(token, object);
				if(comment != null && getRootNode() instanceof CommentedConfigurationNode commented) commented.node(path).comment(comment);
				return true;
			} catch (SerializationException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public <T> boolean addIfNotExist(T object, @Nullable String comment, Object... path) {
		Objects.requireNonNull(object);
		Objects.requireNonNull(path);
		if(getRootNode().node(path).virtual()) {
			try {
				getRootNode().node(path).set(object.getClass(), object);
				if(comment != null && getRootNode() instanceof CommentedConfigurationNode commented) commented.node(path).comment(comment);
				return true;
			} catch (SerializationException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public <T> boolean addIfNotExist(List<T> object, @Nullable String comment, TypeToken<T> token, Object... path) {
		Objects.requireNonNull(object);
		Objects.requireNonNull(token);
		Objects.requireNonNull(path);
		if(getRootNode().node(path).virtual()) {
			try {
				getRootNode().node(path).setList(token, object);
				if(comment != null && getRootNode() instanceof CommentedConfigurationNode commented) commented.node(path).comment(comment);
				return true;
			} catch (SerializationException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public <T> boolean addIfNotExist(Class<T> clazz, List<T> object, @Nullable String comment, Object... path) {
		Objects.requireNonNull(object);
		Objects.requireNonNull(clazz);
		Objects.requireNonNull(path);
		if(getRootNode().node(path).virtual()) {
			try {
				getRootNode().node(path).setList(clazz, object);
				if(comment != null && getRootNode() instanceof CommentedConfigurationNode commented) commented.node(path).comment(comment);
				return true;
			} catch (SerializationException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public void addSerializers(TypeSerializerCollection collection) {
		if(serializers == null) {
			serializers = collection;
		} else serializers = serializers.childBuilder().registerAll(collection).build();
		try {
			loader = selectLoader();
			node = loader.load();
		} catch (ConfigurateException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasReferenced() {
		return referenced != null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends VirtualConfig> C load() {
		updateBuffers();
		try {
			if(loader == null) loader = selectLoader();
			node = loader.load();
		} catch (ConfigurateException e) {
			e.printStackTrace();
		}
		return (C) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends VirtualConfig> C save() {
		try {
			loader.save(node);
			updateRawData();
		} catch (ConfigurateException e) {
			e.printStackTrace();
		}
		return (C) this;
	}

	<T, C extends ConfigurationNode> ConfigurationLoader<C> selectLoader() {
		return ConfigurationServiceImplement.getInstance().createConfigLoader(bufferedWriter, bufferedReader, type, itemStackSerializerType, serializers);
	}

	protected void updateRawData() {
		rawData = stringWriter.toString();
	}

	protected void updateBuffers() {
		stringWriter = new StringWriter();
		bufferedWriter = new BufferedWriter(stringWriter);
		stringReader = new StringReader(rawData);
		bufferedReader = new BufferedReader(stringReader);
	}

}
