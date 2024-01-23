package sawfowl.localeapi.api.serializetools.itemstack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.plugin.PluginContainer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.kyori.adventure.key.Key;

import sawfowl.localeapi.api.ClassUtils;
import sawfowl.localeapi.api.serializetools.SerializeOptions;

@ConfigSerializable
public class SerializedItemStackPlainNBT implements CompoundTag {

	SerializedItemStackPlainNBT(){}

	public SerializedItemStackPlainNBT(ItemStack itemStack) {
		serialize(itemStack);
	}

	public SerializedItemStackPlainNBT(String type, int quantity, String nbt) {
		itemType = type;
		itemQuantity = quantity;
		this.nbt = nbt;
	}

	@Setting("ItemType")
	private String itemType;
	@Setting("Quantity")
	private Integer itemQuantity;
	@Setting("NBT")
	private String nbt;
	private ItemStack itemStack;
	private TagUtil compoundNBT;

	public String getItemTypeAsString() {
		return itemType;
	}

	/**
	 * Getting {@link ItemStack} volume.
	 */
	public Integer getQuantity() {
		return itemQuantity;
	}

	/**
	 * Get all tags as a string.
	 */
	public String getNBT() {
		return nbt != null ? nbt : "";
	}

	/**
	 * Getting {@link ItemStack}
	 */
	public ItemStack getItemStack() {
		if(itemStack != null) return itemStack.copy();
		if(getItemType().isPresent()) {
			itemStack = ItemStack.of(getItemType().get());
			itemStack.setQuantity(itemQuantity);
			if(nbt != null && !nbt.equals("")) {
				try {
					itemStack = ItemStack.builder().fromContainer(itemStack.toContainer().set(DataQuery.of("UnsafeData"), DataFormats.JSON.get().read(nbt))).build();
				} catch (InvalidDataException | IOException e) {
					e.printStackTrace();
				}
			}
		} else itemStack = ItemStack.empty();
		return itemStack.copy();
	}

	/**
	 * Getting {@link ItemType}
	 */
	public Optional<ItemType> getItemType() {
		return Sponge.game().registry(RegistryTypes.ITEM_TYPE).findValue(ResourceKey.resolve(itemType));
	}

	/**
	 * The resulting value can be used to display the item in chat.
	 */
	public Key getItemKey() {
		return getItemType().isPresent() ? Key.key(itemType) : Key.key("air");
	}

	/**
	 * Getting a tag collection is similar to what's in Minecraft code.
	 */
	public TagUtil getOrCreateTag() {
		return compoundNBT != null ? compoundNBT : (compoundNBT = /*isForgeItem() ? new ForgeNBT() :*/ new EditNBT());
	}

	/**
	 * Changing {@link ItemStack} volume.
	 */
	public void setQuantity(int quantity) {
		itemQuantity = quantity;
	}

	public SerializedItemStackJsonNbt toSerializedItemStackJsonNbt() {
		return new SerializedItemStackJsonNbt(itemType, 0, nbt == null ? null : JsonParser.parseString(nbt).getAsJsonObject());
	}

	@Override
	public int hashCode() {
		return Objects.hash(itemQuantity, itemStack, itemType, nbt);
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof SerializedItemStackPlainNBT)) return false;
		SerializedItemStackPlainNBT other = (SerializedItemStackPlainNBT) obj;
		return Objects.equals(itemQuantity, other.itemQuantity) && Objects.equals(itemType, other.itemType) && Objects.equals(nbt, other.nbt);
	}

	public boolean equalsWhithoutQuantity(SerializedItemStackPlainNBT itemStack) {
		return this == itemStack || (Objects.equals(itemType, itemStack.itemType) && Objects.equals(nbt, itemStack.nbt));
	}

	public boolean equalsWhithoutNBT(SerializedItemStackPlainNBT itemStack) {
		return this == itemStack || (Objects.equals(itemType, itemStack.itemType) && Objects.equals(itemQuantity, itemStack.itemQuantity));
	}

	public boolean equalsToItemStack(ItemStack itemStack) {
		return equals(new SerializedItemStackPlainNBT(itemStack));
	}

	@Override
	public String toString() {
		return  "ItemType: " + itemType +
				", Quantity: " + itemQuantity + 
				", Nbt: " + getNBT();
	}

	private void serialize(ItemStack itemStack) {
		itemType = RegistryTypes.ITEM_TYPE.get().valueKey(itemStack.type()).asString();
		itemQuantity = itemStack.quantity();
		if(itemStack.toContainer().get(DataQuery.of("UnsafeData")).isPresent()) {
			try {
				nbt = DataFormats.JSON.get().write((DataView) itemStack.toContainer().get(DataQuery.of("UnsafeData")).get());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.itemStack = itemStack;
	}

	private <T extends CompoundTag> String createStringFromCustomTag(T tag) {
		StringWriter sink = new StringWriter();
		GsonConfigurationLoader loader = createWriter(sink);
		ConfigurationNode node = loader.createNode();
		try {
			if(tag.toJsonObject() == null) {
				node.set(tag.getClass(), tag);
				//if(!node.node("__class__").virtual()) node.removeChild("__class__");
			} else node.set(JsonObject.class, tag.toJsonObject());
			loader.save(node);
		} catch (ConfigurateException e) {
			e.printStackTrace();
		}
		node = null;
		loader = null;
		return sink.toString();
	}

	private <T extends CompoundTag> Optional<T> createTagFromString(String string, Class<T> clazz) {
		try {
			return tagFromNode(serializeNodeFromString(string), clazz);
		} catch (ConfigurateException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	private <T extends CompoundTag> Optional<T> tagFromNode(ConfigurationNode node, Class<T> clazz) throws SerializationException {
		return node.virtual() || node.empty() ? Optional.empty() : Optional.ofNullable(node.get(clazz));
	}

	private GsonConfigurationLoader createWriter(StringWriter sink) {
		return GsonConfigurationLoader.builder().defaultOptions(SerializeOptions.selectOptions(1)).sink(() -> new BufferedWriter(sink)).build();
	}

	private GsonConfigurationLoader createLoader(StringReader source) {
		return GsonConfigurationLoader.builder().defaultOptions(SerializeOptions.selectOptions(1)).source(() -> new BufferedReader(source)).build();
	}

	private ConfigurationNode serializeNodeFromString(String string) {
		try {
			return createLoader(new StringReader(string)).load();
		} catch (ConfigurateException e) {
			e.printStackTrace();
		}
		return BasicConfigurationNode.root();
	}

	private String getPluginId(PluginContainer container) {
		return container.metadata().id();
	}

	private void recreateStack() {
		itemStack = null;
		getItemStack();
	}

	@Override
	public JsonObject toJsonObject() {
		JsonObject object = new JsonObject();
		object.addProperty("ItemType", itemType);
		object.addProperty("Quantity", itemQuantity);
		if(nbt != null) object.addProperty("NBT", nbt);
		return object;
	}

	class EditNBT implements TagUtil {
		private StringWriter writer;
		private StringReader reader;
		private GsonConfigurationLoader loader;
		ConfigurationNode node;

		EditNBT() {
			updateNbt();
		}

		private void updateNbt() {
			if(writer != null && loader != null && node != null) {
				try {
					loader.save(node);
				} catch (IOException e) {
					e.printStackTrace();
				}
				SerializedItemStackPlainNBT.this.nbt = node.empty() || writer.toString().isEmpty() ? nbt : writer.toString();
				recreateStack();
			}
			try {
				reader = new StringReader(nbt != null ? nbt : "");
				writer = new StringWriter();
				loader = GsonConfigurationLoader.builder().defaultOptions(SerializeOptions.selectOptions(1)).source(() -> new BufferedReader(reader)).sink(() -> new BufferedWriter(writer)).build();
				node = nbt != null ? loader.load() : BasicConfigurationNode.root(o -> o.options().serializers(SerializeOptions.selectSerializersCollection(1)));
			} catch (IOException | RuntimeException e) {
				node = BasicConfigurationNode.root(o -> o.options().serializers(SerializeOptions.SERIALIZER_COLLECTION_VARIANT_1));
			}
		}

		@Override
		public <T> void putObject(PluginContainer container, String key, T object) {
			if(!ClassUtils.isPrimitiveOrBasicDataClass(object)) {
				try {
					throw new RuntimeException("This method accepts only primitives, or Java base data classes: '" + ClassUtils.getValuesToString() + "'.");
				} catch (Exception e) {
				}
				return;
			} else try {
				node.node("PluginTags", getPluginId(container), key).set(object.getClass(), object);
				updateNbt();
			} catch (SerializationException e) {
				e.printStackTrace();
			}
		}

		@Override
		public <T> void putObjects(PluginContainer container, String key, List<T> objects) {
			if(objects.isEmpty()) return;
			objects = objects.stream().filter(object -> ClassUtils.isPrimitiveOrBasicDataClass(object)).toList();
			if(objects.isEmpty()) return;
			try {
				node.node("PluginTags", getPluginId(container), key).setList(TypeTokens.createToken(objects.get(0)), objects);
			} catch (SerializationException e) {
				e.printStackTrace();
			}
			updateNbt();
		}

		@Override
		public <K, V> void putObjects(Class<K> mapKey, Class<V> mapValue, PluginContainer container, String key, Map<K, V> objects) {
			if(objects.isEmpty()) return;
			objects = objects.entrySet().stream().filter(entry -> ClassUtils.isPrimitiveOrBasicDataClass(entry.getKey()) && ClassUtils.isPrimitiveOrBasicDataClass(entry.getValue())).collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
			if(objects.isEmpty()) return;
			objects.forEach((k,v) -> {
				try {
					node.node("PluginTags", getPluginId(container), key, k).set(v);
				} catch (SerializationException e) {
					e.printStackTrace();
				}
			});
			updateNbt();
		}

		@Override
		public <T extends CompoundTag> void putCompoundTag(PluginContainer container, String key, T object) {
			putObject(container, key, createStringFromCustomTag(object));
		}

		@Override
		public void removeTag(PluginContainer container, String key) {
			if(!node.node("PluginTags", getPluginId(container), key).virtual()) node.node("PluginTags", getPluginId(container)).removeChild(key);
			if(node.node("PluginTags", getPluginId(container)).empty()) node.node("PluginTags").removeChild(getPluginId(container));
			if(node.node("PluginTags").empty()) node.removeChild("PluginTags");
			updateNbt();
		}

		@Override
		public boolean containsTag(PluginContainer container, String key) {
			return !node.node("PluginTags", getPluginId(container), key).virtual();
		}

		@Override
		public <T> T getObject(Class<T> clazz, PluginContainer container, String key, T def) {
			if(!ClassUtils.isPrimitiveOrBasicDataClass(clazz)) {
				try {
					throw new RuntimeException("This method accepts only primitives, or Java base data classes: '" + ClassUtils.getValuesToString() + "'.");
				} catch (Exception e) {
				}
				return def;
			} else if(nbt != null && node != null && !node.node("PluginTags", getPluginId(container), key).virtual()) {
				try {
					return node.node("PluginTags", getPluginId(container), key).get(clazz, def);
				} catch (SerializationException e) {
					e.printStackTrace();
				}
			}
			return def;
		}

		@Override
		public <T> List<T> getObjectsList(Class<T> clazz, PluginContainer container, String key, List<T> def) {
			if(!ClassUtils.isPrimitiveOrBasicDataClass(clazz)) {
				try {
					throw new RuntimeException("This method accepts only primitives, or Java base data classes: '" + ClassUtils.getValuesToString() + "'.");
				} catch (RuntimeException e) {
				}
				return def;
			} else if(nbt != null && node != null && !node.node("PluginTags", getPluginId(container), key).virtual()) {
				try {
					return node.node("PluginTags", getPluginId(container), key).getList(clazz, def);
				} catch (SerializationException e) {
					e.printStackTrace();
				}
			}
			return def;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <K, V> Map<K, V> getObjectsMap(Class<K> mapKey, Class<V> mapValue, PluginContainer container, String key, Map<K, V> objects) {
			if(containsTag(container, key)) {
				return node.node("PluginTags", getPluginId(container), key).childrenMap().entrySet().stream().collect(Collectors.toMap(entry -> (K) entry.getKey(), entry -> {
					try {
						return entry.getValue().get(mapValue);
					} catch (SerializationException e) {
						e.printStackTrace();
					}
					return null;
				}));
			}
			return objects;
		}

		@Override
		public <T extends CompoundTag> Optional<T> getCompoundTag(Class<T> clazz, PluginContainer container, String key) {
			return containsTag(container, key) ? createTagFromString(getString(container, key), clazz) : Optional.empty();
		}

		@Override
		public Set<String> getAllKeys(PluginContainer container) {
			return nbt != null && node != null ? new HashSet<>() : node.node("PluginTags", getPluginId(container)).childrenMap().keySet().stream().map(object -> object.toString()).collect(Collectors.toUnmodifiableSet());
		}

		@Override
		public int size(PluginContainer container) {
			return nbt != null && node != null ? 0 : node.node("PluginTags", getPluginId(container)).childrenMap().size();
		}
		
	}

}