package sawfowl.localeapi.apiclasses.serializers.itemstack;

import java.io.IOException;
import java.lang.reflect.Type;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.api.serializetools.itemstack.ComponentUtil;
import sawfowl.localeapi.api.serializetools.itemstack.SerializedItemStack;
import sawfowl.localeapi.api.services.ConfigurationService;

public final class ItemStackSerializer implements TypeSerializer<ItemStack> {

	public static final ItemStackSerializer INSTANCE = new ItemStackSerializer();

	private ItemStackSerializer(){}

	@Override
	public ItemStack deserialize(Type type, ConfigurationNode node) throws SerializationException {
		if(!node.node("Components").virtual() && !node.node("Components").isMap()) return node.get(SerializedItemStack.class).getItemStack();
		if((!node.node("Data").virtual() && !node.node("Data").empty() && node.node("Data").isMap())) return ConfigurationService.getInstance().selectSerializersCollection(ItemStackSerializerType.SPONGE).get(ItemStack.class).deserialize(type, node);
		return createStack(node);
	}

	@Override
	public void serialize(Type type, @org.checkerframework.checker.nullness.qual.Nullable ItemStack itemStack, ConfigurationNode node) throws SerializationException {
		node.node("ItemType").set(RegistryTypes.ITEM_TYPE.get().valueKey(itemStack.type()).asString());
		node.node("Quantity").set(itemStack.quantity());
		if(itemStack.toContainer().get(ComponentUtil.COMPONENTS_PATH).isPresent()) try {
			node.node("ComponentsMap").set(JsonElement.class, JsonParser.parseString(DataFormats.JSON.get().write((DataView) itemStack.toContainer().get(ComponentUtil.COMPONENTS_PATH).get())));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ItemStack createStack(ConfigurationNode node) throws SerializationException {
		return setComponents(node, ItemStack.of(ItemTypes.registry().findValue(ResourceKey.resolve(node.node("ItemType").getString("minecraft:air"))).orElse(ItemTypes.AIR.get()), node.node("Quantity").getInt(1)));
	}

	private ItemStack setComponents(ConfigurationNode node, ItemStack itemStack) throws SerializationException {
		if(!node.node("ComponentsMap").virtual() && !node.node("ComponentsMap").childrenMap().isEmpty()) {
			if(node.node("ComponentsMap").isMap()) {
				try {
					itemStack = ItemStack.builder().fromContainer(itemStack.toContainer().set(ComponentUtil.COMPONENTS_PATH, DataFormats.JSON.get().read(node.node("ComponentsMap").get(JsonElement.class).toString()))).build();
				} catch (InvalidDataException | IOException e) {
					e.printStackTrace();
				}
			} else itemStack = node.get(SerializedItemStack.class).getItemStack();
		}
		return itemStack;
	}

}
