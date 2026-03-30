package sawfowl.localeapi.apiclasses.config.builders;

import java.util.Objects;

import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.config.ReferencedVirtualConfig;
import sawfowl.localeapi.api.config.builders.ReferencedVirtualConfigBuilder;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.apiclasses.config.ReferencedVirtualConfigImpl;

public class ReferencedVirtualBuilderImpl<T> implements ReferencedVirtualConfigBuilder<T> {

	private final Class<T> clazz;
	private final T value;
	private ConfigTypes type;
	private ItemStackSerializerType itemStackSerializerType;
	private TypeSerializerCollection collection;
	public ReferencedVirtualBuilderImpl(Class<T> type) {
		Objects.requireNonNull(type);
		this.clazz = type;
		this.value = null;
	}

	@SuppressWarnings("unchecked")
	public ReferencedVirtualBuilderImpl(T value) {
		Objects.requireNonNull(value);
		this.value = value;
		this.clazz = (Class<T>) value.getClass();
	}

	@Override
	public ReferencedVirtualConfigBuilder<T> setType(ConfigTypes type) {
		if(type != null) this.type = type;
		return this;
	}

	@Override
	public ReferencedVirtualConfigBuilder<T> setItemStackSerializerType(ItemStackSerializerType type) {
		this.itemStackSerializerType = type;
		return this;
	}

	@Override
	public ReferencedVirtualConfigBuilder<T> addSerializers(TypeSerializerCollection collection) {
		this.collection = collection;
		return this;
	}

	@Override
	public ReferencedVirtualConfig<T> build() {
		Objects.requireNonNull(type);
		Objects.requireNonNull(clazz);
		return value == null
			?
			ReferencedVirtualConfigImpl.create(type, itemStackSerializerType, collection, clazz)
			:
			ReferencedVirtualConfigImpl.create(type, itemStackSerializerType, collection, value);
	}

}
