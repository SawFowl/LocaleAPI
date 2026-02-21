package sawfowl.localeapi.apiclasses.serializers;

import java.lang.reflect.Type;

import org.checkerframework.checker.nullness.qual.Nullable;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;

public final class LAEnumItemStackTypeSerializer implements TypeSerializer<ItemStackSerializerType> {

	public static final LAEnumItemStackTypeSerializer INSTANCE = new LAEnumItemStackTypeSerializer();

	private LAEnumItemStackTypeSerializer(){}

	@Override
	public ItemStackSerializerType deserialize(Type type, ConfigurationNode node) throws SerializationException {
		return ItemStackSerializerType.fromString(node.getString());
	}

	@Override
	public void serialize(Type type, @Nullable ItemStackSerializerType itemStackSerializerType, ConfigurationNode node) throws SerializationException {
		node.set(String.class, itemStackSerializerType.toString());
	}

}
