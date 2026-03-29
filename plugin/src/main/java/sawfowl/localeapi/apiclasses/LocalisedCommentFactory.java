package sawfowl.localeapi.apiclasses;

import java.lang.reflect.Type;

import org.spongepowered.configurate.CommentedConfigurationNodeIntermediary;
import org.spongepowered.configurate.objectmapping.meta.Processor;
import org.spongepowered.configurate.objectmapping.meta.Processor.Factory;

import com.google.inject.Inject;

import sawfowl.localeapi.api.LocalisedComment;
import sawfowl.localeapi.api.services.LocaleService;

public final class LocalisedCommentFactory implements Factory<LocalisedComment, Object> {

	@Inject
	private static LocaleService LOCALE_SERVICE;
	public static final LocalisedCommentFactory INSTANCE = new LocalisedCommentFactory();

	private LocalisedCommentFactory(){}

	@Override
	public Processor<Object> make(LocalisedComment data, Type type) {
		return (value, destination) -> {
			if (destination instanceof CommentedConfigurationNodeIntermediary<?> node) {
				if(node.comment() != null && !node.comment().isEmpty()) return;
				if(data.plugin() == null || data.path() == null || data.path().length == 0) {
					if(!data.def().isEmpty()) node.comment(data.def());
				} else if(LOCALE_SERVICE.localesExist(data.plugin()) && LOCALE_SERVICE.getLocales(data.plugin()).getSimple(LOCALE_SERVICE.getSystemOrDefaultLocale()).contains((Object[]) data.path())) {
					if(node.comment() == null || node.comment().isEmpty()) node.comment(LOCALE_SERVICE.getLocales(data.plugin()).getSimple(LOCALE_SERVICE.getSystemOrDefaultLocale()).getString((Object[]) data.path()));
				} else if(!data.def().isEmpty()) node.comment(data.def());
			}
		};
	}

}
