package sawfowl.localeapi.apiclasses.config.builders;

import java.util.Objects;

import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import sawfowl.localeapi.LocaleAPI;
import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.config.VirtualConfig;
import sawfowl.localeapi.api.config.builders.SimpleVirtualConfigBuilder;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.apiclasses.config.VirtualConfigImpl;

public class SimpleVirtualBuilderImpl implements SimpleVirtualConfigBuilder {

	private String rawData = "";
	private ConfigTypes type;
	private ItemStackSerializerType itemStackSerializerType;
	private TypeSerializerCollection collection;
	public SimpleVirtualBuilderImpl() {
		if(LocaleAPI.getConfig() != null) this.type = LocaleAPI.getConfig().getConfigSettings().getType();
	}

	@Override
	public SimpleVirtualConfigBuilder setData(String rawData) {
		Objects.requireNonNull(rawData);
		this.rawData = rawData;
		return this;
	}

	@Override
	public SimpleVirtualConfigBuilder setType(ConfigTypes type) {
		Objects.requireNonNull(type);
		this.type = type;
		return this;
	}

	@Override
	public SimpleVirtualConfigBuilder setItemStackSerializerType(ItemStackSerializerType type) {
		this.itemStackSerializerType = type;
		return this;
	}

	@Override
	public SimpleVirtualConfigBuilder addSerializers(TypeSerializerCollection collection) {
		this.collection = collection;
		return this;
	}

	@Override
	public VirtualConfig build() {
		Objects.requireNonNull(type);
		return VirtualConfigImpl.create(rawData, type, itemStackSerializerType, collection);
	}

}
