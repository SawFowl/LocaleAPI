package sawfowl.localeapi.apiclasses.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Path;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.jackson.FieldValueSeparatorStyle;
import org.spongepowered.configurate.jackson.JacksonConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.NodeResolver;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.xml.XmlConfigurationLoader;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.LocalisedComment;
import sawfowl.localeapi.api.config.builders.ReferencedConfigBuilder;
import sawfowl.localeapi.api.config.builders.ReferencedVirtualConfigBuilder;
import sawfowl.localeapi.api.config.builders.SimpleConfigBuilder;
import sawfowl.localeapi.api.config.builders.SimpleVirtualConfigBuilder;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.api.services.ConfigurationService;
import sawfowl.localeapi.apiclasses.LocalisedCommentFactory;
import sawfowl.localeapi.apiclasses.config.builders.ReferencedBuilderImpl;
import sawfowl.localeapi.apiclasses.config.builders.ReferencedVirtualBuilderImpl;
import sawfowl.localeapi.apiclasses.config.builders.SimpleBuilderImpl;
import sawfowl.localeapi.apiclasses.config.builders.SimpleVirtualBuilderImpl;
import sawfowl.localeapi.apiclasses.config.loaders.TomlConfigurationLoader;
import sawfowl.localeapi.apiclasses.serializers.ConfigTypeSerializer;
import sawfowl.localeapi.apiclasses.serializers.LAEnumItemStackTypeSerializer;
import sawfowl.localeapi.apiclasses.serializers.itemstack.ItemStackSerializer;
import sawfowl.localeapi.apiclasses.serializers.itemstack.PlainItemStackSerializer;
import sawfowl.localeapi.apiclasses.serializers.json.JsonArraySerializer;
import sawfowl.localeapi.apiclasses.serializers.json.JsonElementSerializer;
import sawfowl.localeapi.apiclasses.serializers.json.JsonObjectSerializer;
import sawfowl.localeapi.apiclasses.serializers.json.JsonPrimitiveSerializer;

public class ConfigurationServiceImplement extends ConfigurationService {

	private static ConfigurationServiceImplement instance;
	public ConfigurationServiceImplement() {
		instance = this;
	}

	public static ConfigurationServiceImplement getInstance() {
		return instance;
	}

	private final ObjectMapper.Factory FACTORY = ObjectMapper.factoryBuilder().addProcessor(LocalisedComment.class, LocalisedCommentFactory.INSTANCE).addNodeResolver(NodeResolver.onlyWithSetting()).build();
	private final TypeSerializerCollection JSON_SERIALIZERS = TypeSerializerCollection.defaults().childBuilder().registerAnnotatedObjects(FACTORY).register(ItemStackSerializerType.class, LAEnumItemStackTypeSerializer.INSTANCE).register(ConfigTypes.class, ConfigTypeSerializer.INSTANCE).register(JsonElement.class, JsonElementSerializer.INSTANCE).register(JsonObject.class, JsonObjectSerializer.INSTANCE).register(JsonArray.class, JsonArraySerializer.INSTANCE).register(JsonPrimitive.class, JsonPrimitiveSerializer.INSTANCE).build();
	private final TypeSerializerCollection SIMPLE_SERIALIZER_COLLECTION_VARIANT = TypeSerializerCollection.defaults().childBuilder().registerAnnotatedObjects(FACTORY).register(ItemStackSerializerType.class, LAEnumItemStackTypeSerializer.INSTANCE).register(ConfigTypes.class, ConfigTypeSerializer.INSTANCE).register(ItemStack.class, PlainItemStackSerializer.INSTANCE).register(BlockState.class, Sponge.game().configManager().serializers().get(BlockState.class)).registerAll(TypeSerializerCollection.defaults()).registerAll(ConfigurateComponentSerializer.configurate().serializers()).registerAll(JSON_SERIALIZERS).build();
	private final TypeSerializerCollection JSON_SERIALIZER_COLLECTION_VARIANT = TypeSerializerCollection.defaults().childBuilder().registerAnnotatedObjects(FACTORY).register(ItemStackSerializerType.class, LAEnumItemStackTypeSerializer.INSTANCE).register(ConfigTypes.class, ConfigTypeSerializer.INSTANCE).register(ItemStack.class, ItemStackSerializer.INSTANCE).register(BlockState.class, Sponge.game().configManager().serializers().get(BlockState.class)).registerAll(TypeSerializerCollection.defaults()).registerAll(ConfigurateComponentSerializer.configurate().serializers()).registerAll(JSON_SERIALIZERS).build();
	private final TypeSerializerCollection SPONGE_SERIALIZER_COLLECTION_VARIANT = TypeSerializerCollection.defaults().childBuilder().registerAnnotatedObjects(FACTORY).register(ItemStackSerializerType.class, LAEnumItemStackTypeSerializer.INSTANCE).register(ConfigTypes.class, ConfigTypeSerializer.INSTANCE).registerAll(Sponge.game().configManager().serializers()).registerAll(ConfigurateComponentSerializer.configurate().serializers()).registerAll(JSON_SERIALIZERS).build();
	private final TypeSerializerCollection DEFAULT = TypeSerializerCollection.defaults().childBuilder().registerAnnotatedObjects(FACTORY).build();
	private final ConfigurationOptions SIMPLE_OPTIONS_VARIANT = ConfigurationOptions.defaults().serializers(SIMPLE_SERIALIZER_COLLECTION_VARIANT);
	private final ConfigurationOptions JSON_OPTIONS_VARIANT = ConfigurationOptions.defaults().serializers(JSON_SERIALIZER_COLLECTION_VARIANT);
	private final ConfigurationOptions SPONGE_OPTIONS_VARIANT = ConfigurationOptions.defaults().serializers(SPONGE_SERIALIZER_COLLECTION_VARIANT);

	private geysermc.yaml.YamlConfigurationLoader.Builder createGeyserYamlConfigurationLoader(ItemStackSerializerType serializerType, @Nullable TypeSerializerCollection otherSerializers) {
		return geysermc.yaml.YamlConfigurationLoader.builder().defaultOptions(options -> options.serializers(serializerType == null ? otherSerializers == null ? DEFAULT : otherSerializers : ConfigurationService.mergeSerializers(selectSerializersCollection(serializerType), otherSerializers))).nodeStyle(geysermc.yaml.NodeStyle.BLOCK);
	}

	private YamlConfigurationLoader.Builder createYamlConfigurationLoader(ItemStackSerializerType serializerType, @Nullable TypeSerializerCollection otherSerializers) {
		return YamlConfigurationLoader.builder().defaultOptions(options -> options.serializers(serializerType == null ? otherSerializers == null ? DEFAULT : otherSerializers : ConfigurationService.mergeSerializers(selectSerializersCollection(serializerType), otherSerializers))).nodeStyle(NodeStyle.BLOCK);
	}

	private HoconConfigurationLoader.Builder createHoconConfigurationLoader(ItemStackSerializerType serializerType, @Nullable TypeSerializerCollection otherSerializers) {
		return HoconConfigurationLoader.builder().defaultOptions(options -> options.serializers(serializerType == null ? otherSerializers == null ? DEFAULT : otherSerializers : ConfigurationService.mergeSerializers(selectSerializersCollection(serializerType), otherSerializers)));
	}

	private GsonConfigurationLoader.Builder createJsonConfigurationLoader(ItemStackSerializerType serializerType, @Nullable TypeSerializerCollection otherSerializers) {
		return GsonConfigurationLoader.builder().defaultOptions(options -> options.serializers(serializerType == null ? otherSerializers == null ? DEFAULT : otherSerializers : ConfigurationService.mergeSerializers(selectSerializersCollection(serializerType), otherSerializers)));
	}

	private JacksonConfigurationLoader.Builder createJacksonConfigurationLoader(ItemStackSerializerType serializerType, @Nullable TypeSerializerCollection otherSerializers) {
		return JacksonConfigurationLoader.builder().fieldValueSeparatorStyle(FieldValueSeparatorStyle.SPACE_BOTH_SIDES).defaultOptions(options -> options.serializers(serializerType == null ? otherSerializers == null ? DEFAULT : otherSerializers : ConfigurationService.mergeSerializers(selectSerializersCollection(serializerType), otherSerializers)));
	}

	private XmlConfigurationLoader.Builder createXmlConfigurationLoader(ItemStackSerializerType serializerType, @Nullable TypeSerializerCollection otherSerializers) {
		return XmlConfigurationLoader.builder().writesExplicitType(true).defaultOptions(options -> options.serializers(serializerType == null ? otherSerializers == null ? DEFAULT : otherSerializers : ConfigurationService.mergeSerializers(selectSerializersCollection(serializerType), otherSerializers)));
	}

	private TomlConfigurationLoader.Builder createTomlConfigurationLoader(ItemStackSerializerType serializerType, @Nullable TypeSerializerCollection otherSerializers) {
		return TomlConfigurationLoader.builder().defaultOptions(options -> options.serializers(serializerType == null ? otherSerializers == null ? DEFAULT : otherSerializers : ConfigurationService.mergeSerializers(selectSerializersCollection(serializerType), otherSerializers)));
	}

	@Override
	public ConfigurationNode createVirtualNode(@Nullable ItemStackSerializerType serializerType) {
		return serializerType == null ? BasicConfigurationNode.root() : BasicConfigurationNode.root(o -> o.options().serializers(selectSerializersCollection(serializerType)));
	}

	@Override
	public SimpleConfigBuilder createSimpleConfig() {
		return new SimpleBuilderImpl();
	}

	@Override
	public <T> ReferencedConfigBuilder<T> createReferencedConfig(Class<T> type) {
		return new ReferencedBuilderImpl<>(type);
	}

	@Override
	public <T> ReferencedConfigBuilder<T> createReferencedConfig(T value) {
		return new ReferencedBuilderImpl<>(value);
	}

	@Override
	public SimpleVirtualConfigBuilder createVirtualConfig() {
		return new SimpleVirtualBuilderImpl();
	}

	@Override
	public <T> ReferencedVirtualConfigBuilder<T> createVirtualReferencedConfig(Class<T> type) {
		return new ReferencedVirtualBuilderImpl<>(type);
	}

	@Override
	public <T> ReferencedVirtualConfigBuilder<T> createVirtualReferencedConfig(T value) {
		return new ReferencedVirtualBuilderImpl<>(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends ConfigurationNode> ConfigurationLoader<C> createConfigLoader(Path path, ConfigTypes configType, ItemStackSerializerType serializerType, @Nullable TypeSerializerCollection otherSerializers) {
		switch (configType) {
			case HOCON: return (ConfigurationLoader<C>) createHoconConfigurationLoader(serializerType, otherSerializers).path(path).build();
			case YAML: return (ConfigurationLoader<C>) createYamlConfigurationLoader(serializerType, otherSerializers).path(path).build();
			case GEYSER_YAML: return (ConfigurationLoader<C>) createGeyserYamlConfigurationLoader(serializerType, otherSerializers).path(path).build();
			case JSON: return (ConfigurationLoader<C>) createJsonConfigurationLoader(serializerType, otherSerializers).path(path).build();
			case JACKSON: return (ConfigurationLoader<C>) createJacksonConfigurationLoader(serializerType, otherSerializers).path(path).build();
			case XML: return (ConfigurationLoader<C>) createXmlConfigurationLoader(serializerType, otherSerializers).path(path).build();
			case TOML: return (ConfigurationLoader<C>) createTomlConfigurationLoader(serializerType, otherSerializers).path(path).build();
			default: throw new IllegalArgumentException("Inappropriate value: " + configType);
		}
	}

	@SuppressWarnings("unchecked")
	public <T, C extends ConfigurationNode> ConfigurationLoader<C> createConfigLoader(BufferedWriter bufferedWriter, BufferedReader bufferedReader, ConfigTypes loaderType, ItemStackSerializerType serializerType, @Nullable TypeSerializerCollection otherSerializers) {
		switch (loaderType) {
			case HOCON: return (ConfigurationLoader<C>) createHoconConfigurationLoader(serializerType, otherSerializers).sink(() -> bufferedWriter).source(() -> bufferedReader).build();
			case YAML: return (ConfigurationLoader<C>) createYamlConfigurationLoader(serializerType, otherSerializers).sink(() -> bufferedWriter).source(() -> bufferedReader).build();
			case GEYSER_YAML: return (ConfigurationLoader<C>) createGeyserYamlConfigurationLoader(serializerType, otherSerializers).sink(() -> bufferedWriter).source(() -> bufferedReader).build();
			case JSON: return (ConfigurationLoader<C>) createJsonConfigurationLoader(serializerType, otherSerializers).sink(() -> bufferedWriter).source(() -> bufferedReader).build();
			case JACKSON: return (ConfigurationLoader<C>) createJacksonConfigurationLoader(serializerType, otherSerializers).sink(() -> bufferedWriter).source(() -> bufferedReader).build();
			case XML: return (ConfigurationLoader<C>) createXmlConfigurationLoader(serializerType, otherSerializers).sink(() -> bufferedWriter).source(() -> bufferedReader).build();
			case TOML: return (ConfigurationLoader<C>) createTomlConfigurationLoader(serializerType, otherSerializers).sink(() -> bufferedWriter).source(() -> bufferedReader).build();
			default: throw new IllegalArgumentException("Inappropriate value: " + loaderType);
		}
	}

	@Override
	public ConfigurationOptions selectOptions(ItemStackSerializerType serializerType) {
		switch(serializerType) {
			case SIMPLE: return SIMPLE_OPTIONS_VARIANT;
			case JSON: return JSON_OPTIONS_VARIANT;
			default: return SPONGE_OPTIONS_VARIANT;
		}
	}

	@Override
	public TypeSerializerCollection selectSerializersCollection(ItemStackSerializerType serializerType) {
		switch(serializerType) {
			case SIMPLE: return SIMPLE_SERIALIZER_COLLECTION_VARIANT;
			case JSON: return JSON_SERIALIZER_COLLECTION_VARIANT;
			default: return SPONGE_SERIALIZER_COLLECTION_VARIANT;
		}
	}

}
