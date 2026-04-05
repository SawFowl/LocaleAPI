package sawfowl.localeapi.configure.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import sawfowl.localeapi.api.LocalisedComment;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;

@ConfigSerializable
public class ItemSerialization {

	public ItemSerialization(){}

	@Setting("Type")
	@LocalisedComment(plugin = "localeapi", path = {"ConfigComments", "ItemSerialization", "Type"})
	private ItemStackSerializerType type = ItemStackSerializerType.JSON;
	@Setting("ForceUse")
	@LocalisedComment(plugin = "localeapi", path = {"ConfigComments", "ItemSerialization", "ForceUse"})
	private boolean forceUse = false;

	public ItemStackSerializerType getType() {
		return type;
	}

	public boolean isForceUse() {
		return forceUse;
	}

}
