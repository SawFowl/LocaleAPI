# LocaleAPI [![jitpack](https://jitpack.io/v/SawFowl/LocaleAPI.svg)](https://jitpack.io/#SawFowl/LocaleAPI)

API for working with plugin localizations on SpongeAPI7+. \
Test plugin -> <https://github.com/SawFowl/LocaleTestPlugin/tree/API11> \
javadoc -> <https://sawfowl.github.io/LocaleAPI>

```JAVA
@Plugin("pluginid")
public class Main {
	private Main instance;
	private Logger logger;
	private LocaleService localeService;
	private PluginContainer pluginContainer;
	private LocalesList<Translation> locales;
	public LocaleService getLocaleService() {
		return localeService;
	}

	@Inject
	public Main(PluginContainer pluginContainer, @ConfigDir(sharedRoot = false) Path configDirectory) {
		this.pluginContainer = pluginContainer;
		instance = this;
		logger = LogManager.getLogger("PluginName");
		// Get API.
		localeService = LocaleService.getInstance();
		locales = localeService.createLocales(pluginContainer);
		testLocales();
	}

	public void testLocales() {
		if(!localeService.localesExist(pluginContainer)) {
			locales.createSimpleTranslation(ConfigTypes.HOCON, Locales.DEFAULT);
			locales.createSimpleTranslation(ConfigTypes.HOCON, Locales.DEFAULT);
			locales.getSimple(Locales.DEFAULT).addIfNotExist("Your string for localization.", "Optional comment", "Path");
		}
		// Get message from locale. You can get and use the player's localization 'player.locale();'.
		// The boolean parameter defines what type of string serializer will be used.
		logger.info(locales.getSimple(Locales.DEFAULT).getComponent("Path"));
	}
}
```

## Gradle

```gradle
repositories {
	...
	maven { 
		name = "AspectMaven"
		url 'https://maven.aspect-realms.ru/repository/maven-public/' 
	}
}
dependencies {
	...
	implementation 'com.github.SawFowl:LocaleAPI:6.0'
}
```
