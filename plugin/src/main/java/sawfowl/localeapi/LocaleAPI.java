/*
 * LocaleAPI - Locale API for Sponge plugins.
 * Copyright (C) 2019 - 2023 SawFowl
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package sawfowl.localeapi;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.lifecycle.RegisterBuilderEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.network.ServerConnectionState;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.util.Nameable;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;

import sawfowl.localeapi.ImplementAPI.API;
import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.LocalesList;
import sawfowl.localeapi.api.Logger;
import sawfowl.localeapi.api.Text;
import sawfowl.localeapi.api.TextUtils;
import sawfowl.localeapi.api.config.ReferencedConfig;
import sawfowl.localeapi.api.config.locale.ReferencedLocale;
import sawfowl.localeapi.api.placeholders.Placeholder;
import sawfowl.localeapi.api.placeholders.Placeholders;
import sawfowl.localeapi.api.placeholders.Placeholders.DefaultPlaceholderKeys;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.api.services.ConfigurationService;
import sawfowl.localeapi.api.services.LocaleService;
import sawfowl.localeapi.api.services.LoggerService;
import sawfowl.localeapi.apiclasses.TextImpl;
import sawfowl.localeapi.apiclasses.services.ConfigurationServiceImplement;
import sawfowl.localeapi.apiclasses.services.LoggerServiceImplement;
import sawfowl.localeapi.configure.Config;
import sawfowl.localeapi.configure.LocaleConfig;
import sawfowl.localeapi.utils.WatchRunner;

@Plugin("localeapi")
public class LocaleAPI {

	private static PluginContainer container;
	private Logger logger;
	private LocaleService localeService;
	private LoggerService loggerService;
	private ConfigurationService configurationService;
	private boolean isPresentRegistry = false;
	private static String localeAPIConfigDir;
	private static String mainConfigDir;
	private static ReferencedConfig<Config> config;
	private static LocalesList<LocaleConfig> locales;

	@Inject
	public LocaleAPI(PluginContainer pluginContainer, @ConfigDir(sharedRoot = false) Path configDirectory) {
		container = pluginContainer;
		loggerService = new LoggerServiceImplement();
		configurationService = new ConfigurationServiceImplement();
		new InjectorAPI().createInjector();
		logger = Logger.createApacheLogger("LocaleAPI");
		localeService = new ImplementAPI().create(logger, configDirectory);
		if(!configDirectory.toFile().exists()) configDirectory.toFile().mkdir();
		registerDefaultPlaceholders();
		localeAPIConfigDir = configDirectory.toFile().getAbsolutePath();
		mainConfigDir = configDirectory.getParent().toFile().getAbsolutePath();
		File mainConfig = null;
		for(File file : configDirectory.toFile().listFiles()) {
			if(!file.isDirectory() && file.getName().contains("Config")) {
				mainConfig = file;
				break;
			}
		}
		if(mainConfig != null) {
			ConfigTypes type = ConfigTypes.getTypeByExtension(getExtension(mainConfig.getName()));
			config = ConfigurationService.getInstance()
				.createReferencedConfig(Config.class)
				.setPath(configDirectory)
				.setName("Config")
				.setType(type)
				.setItemStackSerializerType(ItemStackSerializerType.JSON)
				.build();
			if(!type.comparableType(getConfig().getConfigSettings().getType())) {
				ConfigurationNode node = config.getRootNode();
				config = ConfigurationService.getInstance()
					.createReferencedConfig(getConfig())
					.setPath(configDirectory)
					.setName("Config")
					.setType(getConfig().getConfigSettings().getType())
					.setItemStackSerializerType(ItemStackSerializerType.JSON)
					.build();
				try {
					config.getLoader().save(node);
				} catch (ConfigurateException e) {
					e.printStackTrace();
				}
				mainConfig.delete();
				node = null;
			}
		}
		mainConfig = null;
		locales = localeService.createLocales(pluginContainer, LocaleConfig.class);
		if(!locales.contains(Locales.DEFAULT)) locales.createReferencedTranslation(ConfigTypes.HOCON, Locales.DEFAULT, LocaleConfig.class);
		if(!locales.contains(Locales.RU_RU)) locales.createReferencedTranslation(ConfigTypes.HOCON, Locales.RU_RU, LocaleConfig.createRu());
		((API) localeService).startWatch();
		if(config == null) config = ConfigurationService.getInstance()
				.createReferencedConfig(Config.class)
				.setPath(configDirectory)
				.setName("Config")
				.setType(ConfigTypes.HOCON)
				.setItemStackSerializerType(ItemStackSerializerType.JSON)
				.build();
		if(getConfig().getLocalesSettings().isForcedUse()) {
			@SuppressWarnings("unchecked")
			List<ReferencedLocale<LocaleConfig>> copy = locales.stream().map(locale -> (ReferencedLocale<LocaleConfig>) locale).toList();
			WatchRunner.pause();
			copy.forEach(localeConfig -> {
				if(!localeConfig.getType().comparableType(getConfig().getLocalesSettings().getType())) {
					locales.remove(localeConfig.getLocale());
					localeConfig.getPath().toFile().delete();
					try {
						locales.createReferencedTranslation(getConfig().getLocalesSettings().getType(), localeConfig.getLocale(), localeConfig.get()).getLoader().save(localeConfig.getRootNode());
					} catch (ConfigurateException e) {
						e.printStackTrace();
					}
				}
			});
			WatchRunner.pause();
			copy = null;
		}
	}

	@Listener
	public void onStarted(StartedEngineEvent<Server> event) {
		isPresentRegistry  = RegistryTypes.CURRENCY.find().isPresent();
	}

	@Listener(order = Order.FIRST)
	public void registerBuilders(RegisterBuilderEvent event) {
		event.register(Text.Builder.class, () -> new TextImpl().builder());
	}

	@Listener
	public void onStop(StoppingEngineEvent<Server> event) {
		ImplementAPI.stopWatch();
	}

	public static PluginContainer getPluginContainer() {
		return container;
	}

	public static String getLocaleAPIConfigDir() {
		return localeAPIConfigDir;
	}

	public static String getMainConfigDir() {
		return mainConfigDir;
	}

	public static Config getConfig() {
		return config == null ? null : config.get();
	}

	public static LocalesList<LocaleConfig> getLocales() {
		return locales;
	}

	private void registerDefaultPlaceholders() {
		Placeholders.register(Nameable.class, DefaultPlaceholderKeys.NAMEABLE, (text, namable, def) -> (text.replace(DefaultPlaceholderKeys.NAMEABLE, namable.name())));
		Placeholders.register(Entity.class, DefaultPlaceholderKeys.ENTITY_DISPLAY_NAME, (text, entity, def) -> (text.replace(DefaultPlaceholderKeys.ENTITY_DISPLAY_NAME, entity.getValue(Keys.CUSTOM_NAME).map(value -> value.get()).orElse(entity instanceof Nameable ? Component.text(((Nameable) entity).name()) : (def == null ? Component.text("n/a") : def)))));
		Placeholders.register(Entity.class, DefaultPlaceholderKeys.ENTITY_UUID, (text, entity, def) -> (text.replace(DefaultPlaceholderKeys.ENTITY_UUID, entity.uniqueId())));
		Placeholders.register(ServerWorld.class, DefaultPlaceholderKeys.WORLD, (text, world, def) -> (text.replace(DefaultPlaceholderKeys.WORLD, world.key().asString())));
		Placeholders.register(ServerWorld.class, DefaultPlaceholderKeys.WORLD_TIME, (original, world, def) -> original.replace(DefaultPlaceholderKeys.WORLD_TIME, world.properties().dayTime().hour() + ":" + world.properties().dayTime().minute()));
		Placeholders.register(ServerLocation.class, DefaultPlaceholderKeys.LOCATION, (text, location, def) -> (text.replace(DefaultPlaceholderKeys.LOCATION, "<" + location.world().key().asString() + ">" + location.blockPosition().toString())));
		Placeholders.register(Vector3d.class, DefaultPlaceholderKeys.POSITION, (text, vector3d, def) -> (text.replace(DefaultPlaceholderKeys.POSITION, vector3d.toString())));
		Placeholders.register(Vector3i.class, DefaultPlaceholderKeys.BLOCK_POSITION, (text, vector3i, def) -> (text.replace(DefaultPlaceholderKeys.BLOCK_POSITION, vector3i.toString())));
		Placeholders.register(ServerPlayer.class, DefaultPlaceholderKeys.PLAYER_PING, (text, player, def) -> (text.replace(DefaultPlaceholderKeys.PLAYER_PING, player.connection().state().map(state -> state instanceof ServerConnectionState.Game game ? game.latency() : -1).orElse(-1))));
		Placeholders.register(ServerPlayer.class, DefaultPlaceholderKeys.PLAYER_PREFIX, (text, player, def) -> (text.replace(DefaultPlaceholderKeys.PLAYER_PREFIX, player.option("prefix").orElse(""))));
		Placeholders.register(ServerPlayer.class, DefaultPlaceholderKeys.PLAYER_SUFFIX, (text, player, def) -> (text.replace(DefaultPlaceholderKeys.PLAYER_SUFFIX, player.option("suffix").orElse(""))));
		Placeholders.register(ServerPlayer.class, DefaultPlaceholderKeys.PLAYER_RANK, (text, player, def) -> (text.replace(DefaultPlaceholderKeys.PLAYER_RANK, player.option("rank").orElse(""))));
		Placeholders.register(ServerPlayer.class, DefaultPlaceholderKeys.PLAYER_LEVEL, (original, player, def) -> original.replace(DefaultPlaceholderKeys.PLAYER_LEVEL, player.experienceLevel().get()));
		Placeholders.register(ServerPlayer.class, DefaultPlaceholderKeys.PLAYER_WORLD_TIME, (original, player, def) -> original.replace(DefaultPlaceholderKeys.PLAYER_WORLD_TIME, player.world().properties().dayTime().hour() + ":" + player.world().properties().dayTime().minute()));
		Placeholders.register(ServerPlayer.class, DefaultPlaceholderKeys.PLAYER_LOCATION, (original, player, def) -> original.replace(DefaultPlaceholderKeys.PLAYER_LOCATION, "<" + player.world().key().asString() + ">" + player.blockPosition().toString()));
		Placeholders.register(ServerPlayer.class, DefaultPlaceholderKeys.PLAYER_BLOCK_POSITION, (original, player, def) -> original.replace(DefaultPlaceholderKeys.PLAYER_BLOCK_POSITION, player.blockPosition().toString()));
		Placeholders.register(null, DefaultPlaceholderKeys.ONLINE_PLAYERS, (original, nulled, def) -> original.replace(DefaultPlaceholderKeys.ONLINE_PLAYERS, Sponge.server().onlinePlayers().size()));
		Placeholders.register(ItemStack.class, DefaultPlaceholderKeys.ITEM, (text, item, def) -> (text.replace(DefaultPlaceholderKeys.ITEM, item.asComponent().hoverEvent(HoverEvent.showItem(Key.key(ItemTypes.registry().findValueKey(item.type()).map(key -> key.asString()).orElse("air")), item.quantity())))));
		Placeholders.register(BlockState.class, DefaultPlaceholderKeys.BLOCK, (text, block, def) -> (text.replace(DefaultPlaceholderKeys.BLOCK, Component.text("[").append(block.type().item().map(item -> item.asComponent().hoverEvent(HoverEvent.showItem(Key.key(ItemTypes.registry().findValueKey(item).map(key -> key.asString()).orElse("air")), 1))).orElse(block.type().asComponent())).append(Component.text("]")))));
		Placeholders.register(null, DefaultPlaceholderKeys.SERVER_TPS, (original, nulled, def) -> original.replace(DefaultPlaceholderKeys.SERVER_TPS, BigDecimal.valueOf(Sponge.server().ticksPerSecond()).setScale(2, RoundingMode.HALF_UP).doubleValue()));
		Placeholders.register(null, DefaultPlaceholderKeys.SERVER_TICKS, (original, nulled, def) -> original.replace(DefaultPlaceholderKeys.SERVER_TICKS, BigDecimal.valueOf(Sponge.server().averageTickTime()).setScale(2, RoundingMode.HALF_UP).doubleValue()));
		Placeholders.register(ServerPlayer.class, "PlayerBalance", new Placeholder<ServerPlayer>() {
			@Override
			public Text apply(Text original, ServerPlayer player, Component def) {
				if(economyIsPresent()) {
					String plain = original.toPlain();
					if(plain.contains("%currency:")) {
						String currencyKey = getCurrencyKey(plain);
						Currency currency = getCurrency(currencyKey);
						original.replace("%currency:" + currencyKey + "%", getCurrencySymbol(currency)).replace(DefaultPlaceholderKeys.PLAYER_BALANCE, getBalance(player, getDefaultCurrency()));
						currencyKey = null;
						currency = null;
					} else original.replace(DefaultPlaceholderKeys.PLAYER_BALANCE, getBalance(player, getDefaultCurrency()));
					plain = null;
				}
				return original;
			}
		});
		Placeholders.register(ServerPlayer.class, DefaultPlaceholderKeys.PLAYER_STATISTIC, new Placeholder<ServerPlayer>() {
			@Override
			public Text apply(Text original, ServerPlayer player, Component def) {
				String plain = original.toPlain();
				if(!plain.contains("%statistic:")) return original;
				String statisticKey = getStatisticKey(plain);
				Optional<Statistic> stat = getStatistic(player, statisticKey);
				statisticKey = "%statistic:" + statisticKey + "%";
				if(stat.isPresent()) {
					original.replace(statisticKey, String.valueOf(player.get(Keys.STATISTICS).get().get(stat.get())));
				} else original.replace(statisticKey, def == null ? 0 : def);
				plain = null;
				statisticKey = null;
				stat = null;
				return original;
			}
		});
	}

	private boolean economyIsPresent() {
		return Sponge.server().serviceProvider().economyService().isPresent();
	}

	private String getStatisticKey(String string) {
		return string.split("%statistic:")[1].split("%")[0];
	}

	private String getCurrencySymbol(Currency currency) {
		return TextUtils.clearDecorations(currency.symbol());
	}

	private String getBalance(ServerPlayer player, Currency currency) {
		try {
			Optional<UniqueAccount> uOpt = Sponge.server().serviceProvider().economyService().get().findOrCreateAccount(player.uniqueId());
			if (uOpt.isPresent()) {
				return uOpt.get().balance(currency).setScale(2).toPlainString();
			}
		} catch (Exception ignored) {
		}
		return "";
	}

	private Currency getCurrency(String string) {
		if(string == null) return getDefaultCurrency();
		Optional<Currency> optCurrency = getCurrencies().stream().filter(currency -> ((isPresentRegistry && RegistryTypes.CURRENCY.find().get().findValue(ResourceKey.resolve(string)).isPresent()) || TextUtils.clearDecorations(currency.displayName()).equalsIgnoreCase(string) || TextUtils.clearDecorations(currency.symbol()).equalsIgnoreCase(string))).findFirst();
		return optCurrency.isPresent() ? optCurrency.get() : getDefaultCurrency();
	}

	private Currency getDefaultCurrency() {
		return Sponge.server().serviceProvider().economyService().get().defaultCurrency();
	}

	private List<Currency> getCurrencies() {
		List<Currency> currencies = new ArrayList<Currency>();
		Sponge.game().findRegistry(RegistryTypes.CURRENCY).ifPresent(registry -> {
			if(registry.stream().count() > 0) currencies.addAll(registry.stream().collect(Collectors.toList()));
		});
		return !currencies.isEmpty() ? currencies : Arrays.asList(getDefaultCurrency());
	}

	private String getCurrencyKey(String string) {
		return string.split("%currency:")[1].split("%")[0];
	}

	private Optional<Statistic> getStatistic(ServerPlayer player, String key) {
		return player.statistics().keySet().stream().filter(statistic -> (statistic.toString().contains(key.replace(':', '.')))).findFirst();
	}

	private String getExtension(String fileName) {
		char ch;
		int len;
		if(fileName==null || (len = fileName.length())==0 || (ch = fileName.charAt(len-1))=='/' || ch=='\\' || ch=='.' ) return "";
		int dotInd = fileName.lastIndexOf('.'),
			sepInd = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
		if(dotInd <= sepInd) return "";
		else return fileName.substring(dotInd+1).toLowerCase();
	}

	final class InjectorAPI extends AbstractModule {

		Injector createInjector() {
			return Guice.createInjector(this);
		}

		@Override
		protected void configure() {
			bind(LoggerService.class).toInstance(loggerService);
			this.requestStaticInjection(LoggerService.class);
			bind(ConfigurationService.class).toInstance(configurationService);
			this.requestStaticInjection(ConfigurationService.class);
		}

	}

}