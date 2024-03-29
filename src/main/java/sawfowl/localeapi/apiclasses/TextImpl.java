package sawfowl.localeapi.apiclasses;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.Queries;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import sawfowl.localeapi.api.Text;
import sawfowl.localeapi.api.TextUtils;

public class TextImpl implements Text {

	private Component component = Component.empty();

	public Builder builder() {
		return new Builder() {
			@Override
			public @NotNull Text build() {
				return TextImpl.this;
			}
			@Override
			public Text fromString(String string) {
				return fromComponent(TextUtils.deserialize(string == null ? "" : string));
			}
			@Override
			public Text fromComponent(Component component) {
				TextImpl.this.component = component == null ? Component.empty() : component;
				return build();
			}
		};
	}

	@Override
	public int contentVersion() {
		return 0;
	}

	@Override
	public DataContainer toContainer() {
		return DataContainer.createNew()
				.set(Queries.CONTENT_VERSION, contentVersion())
				.set(DataQuery.of("Component"), component);
	}

	@Override
	public Component get() {
		return component;
	}

	@Override
	public Text append(Component component) {
		this.component = this.component.append(component);
		return this;
	}

	@Override
	public Text append(Text text) {
		return append(text.get());
	}

	@Override
	public String toPlain() {
		return PlainTextComponentSerializer.plainText().serialize(component);
	}

	@Override
	public Text replace(String key, Component value) {
		component = component.replaceText(TextReplacementConfig.builder().match(key).replacement(value).build());
		return this;
	}

	@Override
	public Text replace(String key, Text value) {
		return replace(key, value.get());
	}

	@Override
	public Text replace(String key, String value) {
		return replace(key, TextUtils.deserialize(value));
	}

	@Override
	public Text replace(String key, Object value) {
		return replace(key, value.toString());
	}

	@Override
	public Text replace(String[] keys, String... values) {
		replace(replaceMap(keys, values));
		return this;
	}

	@Override
	public Text replace(String[] keys, Object... values) {
		replace(replaceMap(keys, values));
		return this;
	}

	@Override
	public Text replace(String[] keys, Component... values) {
		replaceComponents(replaceMapComponents(keys, values));
		return this;
	}

	@Override
	public Text replace(String[] keys, Text... values) {
		replaceComponents(replaceMapTexts(keys, values));
		return this;
	}

	@Override
	public Text createCallBack(Runnable runnable) {
		return createCallBack(cause -> {
			runnable.run();
		});
	}

	@Override
	public Text createCallBack(Consumer<CommandCause> callback) {
		component = component.clickEvent(SpongeComponents.executeCallback(callback));
		return this;
	}

	@Override
	public Text removeDecorations() {
		component = TextUtils.removeDecorations(component);
		return this;
	}

	private void replace(Map<String, String> map) {
		map.forEach((k, v) -> replace(k, v));
	}

	private void replaceComponents(Map<String, Component> map) {
		map.forEach((k, v) -> replace(k, v));
	}

	private Map<String, String> replaceMap(String[] keys, Object[] values) {
		return IntStream.range(0, keys.length).boxed().collect(Collectors.toMap(i -> keys[i], i -> values.length > i ? values[i].toString() : ""));
	}

	private Map<String, Component> replaceMapComponents(String[] keys, Component[] values) {
		return IntStream.range(0, keys.length).boxed().collect(Collectors.toMap(i -> keys[i], i -> values.length > i ? values[i] : Component.empty()));
	}

	private Map<String, Component> replaceMapTexts(String[] keys, Text[] values) {
		return IntStream.range(0, keys.length).boxed().collect(Collectors.toMap(i -> keys[i], i -> values.length > i ? values[i].get() : Component.empty()));
	}

}
