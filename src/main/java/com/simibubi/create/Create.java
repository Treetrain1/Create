package com.simibubi.create;

import java.util.Random;

import com.simibubi.create.content.logistics.trains.BogeySizes;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.simibubi.create.api.behaviour.BlockSpoutingBehaviour;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.trinkets.Trinkets;
import com.simibubi.create.content.CreateItemGroup;
import com.simibubi.create.content.contraptions.TorquePropagator;
import com.simibubi.create.content.contraptions.fluids.tank.BoilerHeaters;
import com.simibubi.create.content.curiosities.weapons.BuiltinPotatoProjectileTypes;
import com.simibubi.create.content.logistics.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.logistics.block.display.AllDisplayBehaviours;
import com.simibubi.create.content.logistics.block.mechanicalArm.AllArmInteractionPointTypes;
import com.simibubi.create.content.logistics.trains.GlobalRailwayManager;
import com.simibubi.create.content.palettes.AllPaletteBlocks;
import com.simibubi.create.content.schematics.ServerSchematicLoader;
import com.simibubi.create.content.schematics.filtering.SchematicInstances;
import com.simibubi.create.events.CommonEvents;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.block.CopperRegistries;
import com.simibubi.create.foundation.command.ServerLagger;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.ContraptionMovementSetting;
import com.simibubi.create.foundation.data.AllLangPartials;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.LangMerger;
import com.simibubi.create.foundation.data.TagGen;
import com.simibubi.create.foundation.data.recipe.MechanicalCraftingRecipeGen;
import com.simibubi.create.foundation.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.foundation.data.recipe.SequencedAssemblyRecipeGen;
import com.simibubi.create.foundation.data.recipe.StandardRecipeGen;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipHelper.Palette;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.AttachedRegistry;
import com.simibubi.create.foundation.worldgen.AllFeatures;
import com.simibubi.create.foundation.worldgen.AllOreFeatureConfigEntries;
import com.simibubi.create.foundation.worldgen.AllPlacementModifiers;
import com.simibubi.create.foundation.worldgen.BuiltinRegistration;

import io.github.tropheusj.milk.Milk;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.data.ExistingFileHelper;

public class Create implements ModInitializer {

	public static final String ID = "create";
	public static final String NAME = "Create";
	public static final String VERSION = "0.5.1a";

	public static final Logger LOGGER = LogUtils.getLogger();

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting()
		.disableHtmlEscaping()
		.create();

	/** Use the {@link Random} of a local {@link Level} or {@link Entity} or create one */
	@Deprecated
	public static final Random RANDOM = new Random();

	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ID);

	static {
		REGISTRATE.setTooltipModifierFactory(item -> {
			return new ItemDescription.Modifier(item, Palette.STANDARD_CREATE)
				.andThen(TooltipModifier.mapNull(KineticStats.create(item)));
		});
	}

	public static final ServerSchematicLoader SCHEMATIC_RECEIVER = new ServerSchematicLoader();
	public static final RedstoneLinkNetworkHandler REDSTONE_LINK_NETWORK_HANDLER = new RedstoneLinkNetworkHandler();
	public static final TorquePropagator TORQUE_PROPAGATOR = new TorquePropagator();
	public static final GlobalRailwayManager RAILWAYS = new GlobalRailwayManager();
	public static final ServerLagger LAGGER = new ServerLagger();

	@Override
	public void onInitialize() { // onCtor
		AllSoundEvents.prepare();
		AllTags.init();
		AllCreativeModeTabs.init();
		AllBlocks.register();
		AllItems.register();
		AllFluids.register();
		AllPaletteBlocks.register();
		AllMenuTypes.register();
		AllEntityTypes.register();
		AllBlockEntityTypes.register();
		AllEnchantments.register();
		AllRecipeTypes.register();

		// fabric exclusive, squeeze this in here to register before stuff is used
		REGISTRATE.register();

		AllParticleTypes.register();
		AllStructureProcessorTypes.register();
		AllEntityDataSerializers.register();
		AllOreFeatureConfigEntries.init();
		AllFeatures.register();
		AllPlacementModifiers.register();
		BuiltinRegistration.register();
		BogeySizes.init();
		AllBogeyStyles.register();

		AllConfigs.register();

		AllMovementBehaviours.registerDefaults();
		AllInteractionBehaviours.registerDefaults();
		AllDisplayBehaviours.registerDefaults();
		ContraptionMovementSetting.registerDefaults();
		AllArmInteractionPointTypes.register();
		BlockSpoutingBehaviour.registerDefaults();
		ComputerCraftProxy.register();

		Milk.enableMilkFluid();
		CopperRegistries.inject();

		Create.init();
//		modEventBus.addListener(EventPriority.LOWEST, Create::gatherData); // CreateData entrypoint
		AllSoundEvents.register();

//		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CreateClient.onCtorClient(modEventBus, forgeEventBus)); // CreateClient entrypoint

		// causes class loading issues or something
		// noinspection Convert2MethodRef
		Mods.TRINKETS.executeIfInstalled(() -> () -> Trinkets.init());

		// fabric exclusive
		CommonEvents.register();
		AllPackets.channel.initServerListener();
	}

	public static void init() {
		AllPackets.registerPackets();
		SchematicInstances.register();
		BuiltinPotatoProjectileTypes.register();

//		event.enqueueWork(() -> {
			AttachedRegistry.unwrapAll();
			AllAdvancements.register();
			AllTriggers.register();
			BoilerHeaters.registerDefaults();
//		});
	}

	public static void gatherData(FabricDataGenerator gen, ExistingFileHelper helper) {
		TagGen.datagen();
		gen.addProvider(new LangMerger(gen, ID, NAME, AllLangPartials.values()));
		gen.addProvider(AllSoundEvents.provider(gen));
		gen.addProvider(new AllAdvancements(gen));
		gen.addProvider(new StandardRecipeGen(gen));
		gen.addProvider(new MechanicalCraftingRecipeGen(gen));
		gen.addProvider(new SequencedAssemblyRecipeGen(gen));
		ProcessingRecipeGen.registerAll(gen);
//		AllOreFeatureConfigEntries.gatherData(gen);
	}

	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(ID, path);
	}

}
