package sawfowl.localeapi.configure.localization;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class ItemSerialization {

	public static ItemSerialization createRu() {
		ItemSerialization serialization = new ItemSerialization();
		serialization.type = "Это никогда не применяется к виртуальным конфигурациям.\n"
				+ "Доступные варианты:\n"
				+ "SIMPLE - Все компоненты будут записаны в 1 строку. Этот вариант является наиболее надежным, но значительно усложняет ручное редактирование компонентов в конфигурации. Совместим с другими вариантами.\n"
				+ "JSON - Расширенная запись. Проще вносить изменения в конфигурацию вручную. Если у вас возникли проблемы с этим типом сериализации, вам следует сообщить об ошибках разработчику плагина LocaleAPI. Совместим с другими вариантами.\n"
				+ "SPONGE - Используется сериализатор Sponge. Некоторые данные будут записаны в 1 строку. Если у вас возникнут проблемы с этим типом сериализации, вам следует сообщить об ошибках разработчикам Sponge. Не совместим с другими вариантами и не может корректно загрузить данные сохраненные с помощью другого типа сериализации.";
		serialization.forceUse = "Принудительное использование указанного тут типа сериализации предметов.";
		return serialization;
	}

	public ItemSerialization(){}

	@Setting("Type")
	private String type = "This never applies to virtual configurations\n"
			+ "Acceptable variants:\n"
			+ "SIMPLE - All components will be written in 1 line. This option is the most reliable, but it makes manual editing of components in the configuration much more difficult. Compatible with other variants.\n"
			+ "JSON - Advanced recording. Easier to make manual changes to the config. If you have problems with this type of serialization, you should report errors to the LocaleAPI plugin developer. Compatible with other variants.\n"
			+ "SPONGE - Using Sponge serializer. Some data will be written in 1 line. If you encounter problems with this type of serialization, you should report bugs to the Sponge developers. It is not compatible with other variants and cannot correctly load data saved using a different type of serialization.";
	@Setting("ForceUse")
	private String forceUse = "Forced use of the item serialization type specified here.";

	public String getType() {
		return type;
	}

	public String getForceUse() {
		return forceUse;
	}

}
