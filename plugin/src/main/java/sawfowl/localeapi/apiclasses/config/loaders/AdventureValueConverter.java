package sawfowl.localeapi.apiclasses.config.loaders;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.RGBLike;

import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class AdventureValueConverter {

	private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

	private AdventureValueConverter(){}

	public static Object toPrimitive(Object value) {
		if(value == null) return null;
		if(value instanceof NamedTextColor namedColor) return namedColor.asHexString();
		if(value instanceof TextColor textColor) return textColor.asHexString();
		if(value instanceof RGBLike rgbLike) return String.format("#%06X", rgbLike.red() << 16 | rgbLike.green() << 8 | rgbLike.blue());
		if(value instanceof TextDecoration decoration) return decoration.toString();
		if(value instanceof Key key) return key.asString();
		if(value instanceof Object[] array) return Stream.of(array).map(AdventureValueConverter::toPrimitive).toArray();
		return value;
	}

	public static Object fromPrimitive(Object value) {
		if(value == null) return null;
		if(value instanceof String string && HEX_COLOR_PATTERN.matcher(string).matches()) {
			try {
				return NamedTextColor.nearestTo(TextColor.fromHexString(string));
			} catch (Exception e) {
				return string;
			}
		}
		if(value instanceof Object[] array) return Stream.of(array).map(AdventureValueConverter::fromPrimitive).toArray();
		return value;
	}

	public static boolean requiresConversion(Object value) {
		return value instanceof NamedTextColor
				|| value instanceof TextColor
				|| value instanceof RGBLike
				|| value instanceof TextDecoration
				|| value instanceof Key;
	}

	public static boolean isHexColor(String str) {
		return str != null && HEX_COLOR_PATTERN.matcher(str).matches();
	}

}