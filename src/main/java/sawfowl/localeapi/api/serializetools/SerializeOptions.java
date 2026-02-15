package sawfowl.localeapi.api.serializetools;

import java.nio.file.Path;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.NodeResolver;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.LocalisedComment;
import sawfowl.localeapi.apiclasses.LocalisedCommentFactory;
import sawfowl.localeapi.apiclasses.serializers.itemstack.ItemStackSerializer;
import sawfowl.localeapi.apiclasses.serializers.itemstack.PlainItemStackSerializer;
import sawfowl.localeapi.apiclasses.serializers.json.JsonArraySerializer;
import sawfowl.localeapi.apiclasses.serializers.json.JsonElementSerializer;
import sawfowl.localeapi.apiclasses.serializers.json.JsonObjectSerializer;
import sawfowl.localeapi.apiclasses.serializers.json.JsonPrimitiveSerializer;

/**
 * These options disable serialization of objects not marked by the <b>@Setting</b> annotation.
 */
public class SerializeOptions {

	private static final TypeSerializer<ItemStack> SIMPLE_ITEMSTACK_SERIALIZER = new PlainItemStackSerializer();
	private static final TypeSerializer<ItemStack> JSON_ITEMSTACK_SERIALIZER = new ItemStackSerializer();
	public static final ObjectMapper.Factory FACTORY = ObjectMapper.factoryBuilder().addProcessor(LocalisedComment.class, new LocalisedCommentFactory()).addNodeResolver(NodeResolver.onlyWithSetting()).build();
	public static final TypeSerializerCollection JSON_SERIALIZERS = TypeSerializerCollection.defaults().childBuilder().register(JsonElement.class, new JsonElementSerializer()).register(JsonObject.class, new JsonObjectSerializer()).register(JsonArray.class, new JsonArraySerializer()).register(JsonPrimitive.class, new JsonPrimitiveSerializer()).build();
	public static final TypeSerializerCollection SIMPLE_SERIALIZER_COLLECTION_VARIANT = TypeSerializerCollection.defaults().childBuilder().registerAnnotatedObjects(FACTORY).register(ItemStack.class, SIMPLE_ITEMSTACK_SERIALIZER).register(BlockState.class, Sponge.game().configManager().serializers().get(BlockState.class)).registerAll(TypeSerializerCollection.defaults()).registerAll(ConfigurateComponentSerializer.configurate().serializers()).registerAll(JSON_SERIALIZERS).build();
	public static final TypeSerializerCollection JSON_SERIALIZER_COLLECTION_VARIANT = TypeSerializerCollection.defaults().childBuilder().registerAnnotatedObjects(FACTORY).register(ItemStack.class, JSON_ITEMSTACK_SERIALIZER).register(BlockState.class, Sponge.game().configManager().serializers().get(BlockState.class)).registerAll(TypeSerializerCollection.defaults()).registerAll(ConfigurateComponentSerializer.configurate().serializers()).registerAll(JSON_SERIALIZERS).build();
	public static final TypeSerializerCollection SPONGE_SERIALIZER_COLLECTION_VARIANT = TypeSerializerCollection.defaults().childBuilder().registerAnnotatedObjects(FACTORY).registerAll(Sponge.game().configManager().serializers()).registerAll(ConfigurateComponentSerializer.configurate().serializers()).registerAll(JSON_SERIALIZERS).build();
	public static final ConfigurationOptions SIMPLE_OPTIONS_VARIANT = ConfigurationOptions.defaults().serializers(SIMPLE_SERIALIZER_COLLECTION_VARIANT);
	public static final ConfigurationOptions JSON_OPTIONS_VARIANT = ConfigurationOptions.defaults().serializers(JSON_SERIALIZER_COLLECTION_VARIANT);
	public static final ConfigurationOptions SPONGE_OPTIONS_VARIANT = ConfigurationOptions.defaults().serializers(SPONGE_SERIALIZER_COLLECTION_VARIANT);

	/**
	 * Creating a YAML config with serializers applied and standard options preserved.
	 */
	public static YamlConfigurationLoader.Builder createYamlConfigurationLoader(ItemStackSerializerType serializerType) {
		return YamlConfigurationLoader.builder().defaultOptions(options -> options.serializers(selectSerializersCollection(serializerType))).nodeStyle(NodeStyle.BLOCK);
	}

	/**
	 * Creating a HOCON config with serializers applied and standard options preserved.
	 */
	public static HoconConfigurationLoader.Builder createHoconConfigurationLoader(ItemStackSerializerType serializerType) {
		return HoconConfigurationLoader.builder().defaultOptions(options -> options.serializers(selectSerializersCollection(serializerType)));
	}

	/**
	 * Creating a JSON config with serializers applied and standard options preserved.
	 */
	public static GsonConfigurationLoader.Builder createJsonConfigurationLoader(ItemStackSerializerType serializerType) {
		return GsonConfigurationLoader.builder().defaultOptions(options -> options.serializers(selectSerializersCollection(serializerType)));
	}

	/**
	 * Creating a YAML config with serializers applied and standard options preserved.
	 */
	public static YamlConfigurationLoader.Builder createYamlConfigurationLoader(ItemStackSerializerType serializerType, TypeSerializerCollection otherSerializers) {
		return YamlConfigurationLoader.builder().defaultOptions(options -> options.serializers(merge(selectSerializersCollection(serializerType), otherSerializers))).nodeStyle(NodeStyle.BLOCK);
	}

	/**
	 * Creating a HOCON config with serializers applied and standard options preserved.
	 */
	public static HoconConfigurationLoader.Builder createHoconConfigurationLoader(ItemStackSerializerType serializerType, TypeSerializerCollection otherSerializers) {
		return HoconConfigurationLoader.builder().defaultOptions(options -> options.serializers(merge(selectSerializersCollection(serializerType), otherSerializers)));
	}

	/**
	 * Creating a JSON config with serializers applied and standard options preserved.
	 */
	public static GsonConfigurationLoader.Builder createJsonConfigurationLoader(ItemStackSerializerType serializerType, TypeSerializerCollection otherSerializers) {
		return GsonConfigurationLoader.builder().defaultOptions(options -> options.serializers(merge(selectSerializersCollection(serializerType), otherSerializers)));
	}

	public static ConfigurationNode createVirtualNode(ItemStackSerializerType serializerType) {
		return BasicConfigurationNode.root(o -> o.options().serializers(selectSerializersCollection(serializerType)));
	}

	/**
	 * Creating a configuration loader with a type.
	 * 
	 * @param <T> loaderClass - Configuration Loader Class.
	 * @param <C> nodeClass - Configuration node processing class. Note that `{@link CommentedConfigurationNode}` is not suitable for configurations in Json format.
	 * @param path - Path to the configuration file.
	 * @param configType - Configuration Type. To avoid errors, it must point to the same class loader as the `Class<T> loaderClass` parameter.
	 * @param serializerType - The type of item serialization used. See {@linkplain #selectSerializersCollection(ItemStackSerializerType)}
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T, C extends ConfigurationNode> ConfigurationLoader<C> createConfigLoader(Class<T> loaderClass, Class<C> nodeClass, Path path, ConfigTypes configType, ItemStackSerializerType serializerType, TypeSerializerCollection otherSerializers) {
		switch (configType) {
		case HOCON: return (ConfigurationLoader<C>) createHoconConfigurationLoader(serializerType, otherSerializers).path(path).build();
		case YAML: return (ConfigurationLoader<C>) createYamlConfigurationLoader(serializerType, otherSerializers).path(path).build();
		case JSON: return (ConfigurationLoader<C>) createJsonConfigurationLoader(serializerType, otherSerializers).path(path).build();
		default: throw new IllegalArgumentException("Inappropriate value: " + configType);
		}
	}

	/**
	 * Selecting serialization variant for items.<br>
	 * <b>SIMPLE</b> - All NBT tags will be written in 1 line. This option is the most reliable, but significantly complicates manual editing of NBT tags in config.<br>
	 * <b>JSON</b> - Advanced recording. Easier to make manual changes to the config. If you have problems with this type of serialization, you should report errors to the LocaleAPI plugin developer.<br>
	 * <b>SPONGE</b> - Using Sponge serializer. Some data will be written in 1 line. If you encounter problems with this type of serialization, you should report bugs to the Sponge developers.<br>
	 */
	public static ConfigurationOptions selectOptions(ItemStackSerializerType serializerType) {
		switch(serializerType) {
			case SIMPLE: return SIMPLE_OPTIONS_VARIANT;
			case JSON: return JSON_OPTIONS_VARIANT;
			default: return SPONGE_OPTIONS_VARIANT;
		}
	}

	/**
	 * Selecting serialization variant for items.<br>
	 * <b>SIMPLE</b> - All NBT tags will be written in 1 line. This option is the most reliable, but significantly complicates manual editing of NBT tags in config.<br>
	 * <b>JSON</b> - Advanced recording. Easier to make manual changes to the config. If you have problems with this type of serialization, you should report errors to the LocaleAPI plugin developer.<br>
	 * <b>SPONGE</b> - Using Sponge serializer. Some data will be written in 1 line. If you encounter problems with this type of serialization, you should report bugs to the Sponge developers.<br>
	 */
	public static TypeSerializerCollection selectSerializersCollection(ItemStackSerializerType serializerType) {
		switch(serializerType) {
			case SIMPLE: return SIMPLE_SERIALIZER_COLLECTION_VARIANT;
			case JSON: return JSON_SERIALIZER_COLLECTION_VARIANT;
			default: return SPONGE_SERIALIZER_COLLECTION_VARIANT;
		}
	}

	private static TypeSerializerCollection merge(TypeSerializerCollection laColection, TypeSerializerCollection otherCollection) {
		return otherCollection == null ? laColection : otherCollection.childBuilder().registerAll(laColection).build();
	}

}
