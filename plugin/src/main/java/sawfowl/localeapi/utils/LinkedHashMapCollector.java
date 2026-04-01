package sawfowl.localeapi.utils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class LinkedHashMapCollector {

	public static <T, K, V> Collector<T, ?, LinkedHashMap<K, V>> 
			toLinkedHashMap(Function<? super T, ? extends K> keyMapper,
						   Function<? super T, ? extends V> valueMapper) {
		
		return toLinkedHashMap(keyMapper, valueMapper, throwingMerger(), LinkedHashMap::new);
	}

	public static <T, K, V> Collector<T, ?, LinkedHashMap<K, V>> 
			toLinkedHashMap(Function<? super T, ? extends K> keyMapper,
						   Function<? super T, ? extends V> valueMapper,
						   BinaryOperator<V> mergeFunction) {
		
		return toLinkedHashMap(keyMapper, valueMapper, mergeFunction, LinkedHashMap::new);
	}

	public static <T, K, V, M extends Map<K, V>> Collector<T, ?, M> 
			toLinkedHashMap(Function<? super T, ? extends K> keyMapper,
						   Function<? super T, ? extends V> valueMapper,
						   BinaryOperator<V> mergeFunction,
						   Supplier<M> mapSupplier) {
		
		return new CollectorImpl<>(
			mapSupplier,
			(map, element) -> map.merge(
				keyMapper.apply(element),
				valueMapper.apply(element),
				mergeFunction
			),
			(left, right) -> {
				right.forEach((k, v) -> left.merge(k, v, mergeFunction));
				return left;
			},
			new HashSet<>(Arrays.asList(Collector.Characteristics.IDENTITY_FINISH))
		);
	}

	private static <T> BinaryOperator<T> throwingMerger() {
		return (u, v) -> {
			throw new IllegalStateException(String.format("Duplicate key %s", u));
		};
	}

	private static class CollectorImpl<T, A, R> implements Collector<T, A, R> {
		private final Supplier<A> supplier;
		private final BiConsumer<A, T> accumulator;
		private final BinaryOperator<A> combiner;
		private final Function<A, R> finisher;
		private final Set<Characteristics> characteristics;

		@SuppressWarnings("unchecked")
		CollectorImpl(Supplier<A> supplier,
					  BiConsumer<A, T> accumulator,
					  BinaryOperator<A> combiner,
					  Set<Characteristics> characteristics) {
			this(supplier, accumulator, combiner, (Function<A, R>) Function.identity(), characteristics);
		}

		CollectorImpl(Supplier<A> supplier,
					  BiConsumer<A, T> accumulator,
					  BinaryOperator<A> combiner,
					  Function<A, R> finisher,
					  Set<Characteristics> characteristics) {
			this.supplier = supplier;
			this.accumulator = accumulator;
			this.combiner = combiner;
			this.finisher = finisher;
			this.characteristics = characteristics;
		}

		@Override
		public Supplier<A> supplier() {
			return supplier; 
		}

		@Override
		public BiConsumer<A, T> accumulator() {
			return accumulator; 
		}
		
		@Override
		public BinaryOperator<A> combiner() {
			return combiner; 
		}
		
		@Override
		public Function<A, R> finisher() {
			return finisher; 
		}
		
		@Override
		public Set<Characteristics> characteristics() {
			return characteristics; 
		}
	}

}
