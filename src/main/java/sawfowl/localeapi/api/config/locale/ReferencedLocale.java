package sawfowl.localeapi.api.config.locale;

import sawfowl.localeapi.api.Translation;
import sawfowl.localeapi.api.config.ReferencedConfig;

public interface ReferencedLocale<T extends Translation> extends PluginLocale, ReferencedConfig<T> {

}
