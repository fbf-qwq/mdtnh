package mdtnh;

import arc.Core;
import arc.Events;
import arc.audio.Sound;
import arc.struct.ObjectMap;
import arc.util.Log;

import mdtnh.ai.MdtAISystem;
import mdtnh.combat.impl.MdtCombatMechanics;
import mdtnh.energy.MdtEnergyBlocks;
import mdtnh.energy.MdtEnergySystem;
import mdtnh.gen.MineralVeins;
import mdtnh.gen.ModPlanet;
import mdtnh.gen.ModSectors;
import mdtnh.gen.block.ModOre;
import mdtnh.gen.block.ModOreRender;
import mdtnh.gen.block.OreExplorationController;
import mdtnh.gen.block.OreExplorationOverlay;
import mdtnh.graphics.MdtMaterialDraw;
import mdtnh.modui.buildui.MdtBuildMenuContent;
import mdtnh.modui.buildui.MdtBuildMenuFragment;
import mdtnh.modui.itemui.MdtCoreItemsQuickBar;
import mdtnh.modui.recipeui.RecipeQueryUI;
import mdtnh.status.ModStatusEffects;
import mdtnh.test.ModMechanicTestUnits;
import mdtnh.transport.MdtTransportBlocks;
import mdtnh.turret.MdtImplementedTurrets;
import mdtnh.units.MdtProjectileStyling;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.mod.Mod;

public class MainMod extends Mod {
    public static ObjectMap<Integer, Sound> IdToSound = new ObjectMap<>();
    private MdtBuildMenuFragment buildMenu;

    private MdtCoreItemsQuickBar itemQuickBar;

    public MainMod() {
        MdtAISystem.install();

        OreExplorationController.install();
        ModOreRender.install();
        OreExplorationOverlay.install();

        RecipeQueryUI.registerKeybind();
        RecipeQueryUI.install();
        Log.info("MDTNH recipe query UI installed.");

        Events.on(ClientLoadEvent.class, event -> Core.app.post(() -> {
            MdtMaterialDraw.load();

            if (buildMenu == null) {
                Log.info("Loading MDT build menu...");

                MdtBuildMenuContent.load();

                buildMenu = new MdtBuildMenuFragment(
                        MdtBuildMenuContent.registry
                );
                buildMenu.install();
                Log.info("MDT build menu installed.");
            }

            if (itemQuickBar == null) {
                Log.info("Loading MDT core item quick rod...");
                itemQuickBar = new MdtCoreItemsQuickBar();
                itemQuickBar.install();
                Log.info("MDT core item quick rod installed.");
            }
        }));
    }

    @Override
    public void loadContent() {
        Log.info("MainMod.loadContent() started");

        ModStatusEffects.load();
        ModUnits.load();
        MdtCombatMechanics.load();
        ModMechanicTestUnits.load();

        /*
         * IMPORTANT:
         * Run this after all regular units and their weapons exist.
         *
         * It:
         * - assigns projectile layers by semantics;
         * - recursively styles frag/interval/spawn bullets;
         * - scans MDTNH custom behavior-owned BulletType fields;
         * - applies tier-dependent VFX;
         * - upgrades every detected A24/gravity projectile;
         * - injects the new air-drop/call-in bombardment attacks.
         */
        MdtProjectileStyling.applyAll();

        ModItems.load();
        ModLiquids.load();
        GtLiquids.load();
        GtMaterials.load();
        ModOre.load();
        MineralVeins.load();
        Component.load();
        ModCrafters.load();
        GtProductionMachines.load();
        GtUtilityMachines.load();
        MdtImplementedTurrets.load();
        VoltageExampleMachines.load();
        MdtTransportBlocks.load();

        ModPlanet.load();
        ModSectors.load();

        MdtEnergyBlocks.load();
        MdtEnergySystem.install();

        Core.assets.load("sounds/steamOverFlow.ogg", Sound.class).loaded = a -> {
            IdToSound.put(1, a);
        };

        Log.info("All content loaded.");
    }
}
