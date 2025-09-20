# LocaleAPI [![jitpack](https://jitpack.io/v/SawFowl/LocaleAPI.svg)](https://jitpack.io/#SawFowl/LocaleAPI)

API for working with plugin localizations on SpongeAPI7+. \
Test plugin -> <https://github.com/SawFowl/LocaleTestPlugin/tree/API11> \
javadoc -> <https://sawfowl.github.io/LocaleAPI>

```JAVA
@Plugin("pluginid")
public class Main {
    private Main instance;
    private Logger logger;
    private static LocaleService localeService;
    private PluginContainer pluginContainer;
	private LocalesList locales;
    public LocaleService getLocaleService() {
        return localeService;
    }

    @Inject
    public Main(PluginContainer pluginContainer, @ConfigDir(sharedRoot = false) Path configDirectory) {
        this.pluginContainer = pluginContainer;
    }

    // Get API. Variant 1. This happens in event `ConstructPluginEvent`.  It's recommended if LocaleAPI is mandatory.
    @Listener
    public void onLocaleServisePostEvent(LocaleServiseEvent.Construct event) {
        instance = this;
        logger = LogManager.getLogger("PluginName");
        localeService = event.getLocaleService();
		locales = localeService.createLocales(pluginContainer);
        testLocales();
    }

    // Get API. Variant 2. This happens in event `StartedEngineEvent<Server>`. It's recommended if LocaleAPI is optional.
    // In this case, you can specify another class as the event listener.
    @Listener
    public void onLocaleServisePostEvent(LocaleServiseEvent.Started event) {
        localeService = event.getLocaleService();
		locales = localeService.createLocales(pluginContainer);
        testLocales();
    }

    public void testLocales() {
        if(!localeService.localesExist(pluginContainer)) {
            locales.createSimpleTranslation(ConfigTypes.HOCON, Locales.DEFAULT);
            locales.createSimpleTranslation(ConfigTypes.HOCON, Locales.DEFAULT);
            locales.getLocale(Locales.DEFAULT).addIfNotExist("Your string for localization.", "Optional comment", "Path");
        }
        // Get message from locale. You can get and use the player's localization 'player.locale();'.
        // The boolean parameter defines what type of string serializer will be used.
        logger.info(locales.getLocale(Locales.DEFAULT).getComponent("Path"));
    }
}
```

## Gradle

```gradle
repositories {
    ...
    maven { 
        name = "JitPack"
        url 'https://jitpack.io' 
    }
}
dependencies {
    ...
    implementation 'com.github.SawFowl:LocaleAPI:6.0'
}
```
