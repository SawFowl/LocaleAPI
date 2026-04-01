package sawfowl.localeapi.apiclasses.config.loaders;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlFormat;

import sawfowl.localeapi.utils.LinkedHashMapCollector;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CollectionValueConverter {
	
	private CollectionValueConverter() {}

	public static Object toNightConfigCompatible(Object value) {
		if(value == null) return null;
		value = PrimitiveValueConverter.toNightConfigCompatible(value);
		if(value instanceof Map<?, ?> map) {
			Config config = TomlFormat.instance().createConfig();
			for(Map.Entry<?, ?> entry : map.entrySet()) config.set(entry.getKey().toString(), toNightConfigCompatible(entry.getValue()));
			return config;
		}
		if(value instanceof List<?> list) return list.stream().map(CollectionValueConverter::toNightConfigCompatible).collect(Collectors.toList());
		if(value instanceof Object[] array) return Stream.of(array).map(CollectionValueConverter::toNightConfigCompatible).toArray();
		return AdventureValueConverter.toPrimitive(value);
	}

	public static Object fromNightConfigCompatible(Object value) {
		if(value == null) return null;
		if(value instanceof Config config) return config.entrySet().stream().collect(LinkedHashMapCollector.toLinkedHashMap(entry -> entry.getKey(), entry -> fromNightConfigCompatible(entry.getValue())));
		if(value instanceof List<?> list) return list.stream().map(CollectionValueConverter::fromNightConfigCompatible).collect(Collectors.toList());
		if(value instanceof Object[] array) return Stream.of(array).map(CollectionValueConverter::fromNightConfigCompatible).toArray();
		return PrimitiveValueConverter.fromNightConfigCompatible(AdventureValueConverter.fromPrimitive(value));
	}

	public static boolean requiresConversion(Object value) {
		return value instanceof Map
				|| value instanceof List
				|| value instanceof Set
				|| value instanceof Object[]
				|| value instanceof Config
				|| PrimitiveValueConverter.isPrimitiveOrWrapper(value)
				|| PrimitiveValueConverter.isPrimitiveArray(value)
				|| AdventureValueConverter.requiresConversion(value);
	}

	public static boolean isCollectionOrArray(Object value) {
		return value instanceof Collection
				|| value instanceof Map
				|| value instanceof Object[]
				|| value instanceof Config
				|| PrimitiveValueConverter.isPrimitiveArray(value);
	}

}