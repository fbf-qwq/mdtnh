package mdtnh.gen.block;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import mdtnh.GtMaterials;
import mdtnh.GtOreCatalog;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.blocks.environment.OreBlock;
import  mdtnh.gen.blocks.ToggleOreBlock;

/**
 * Registers early/HV GTNH ore overlays and terrain-like loose deposits.
 */
public final class GtEarlyOreBlocks {

    public static final ObjectMap<String, Block> oreBlocks =
            new ObjectMap<>();

    public static final ObjectMap<String, Block> veinOres =
            new ObjectMap<>();

    public static final ObjectMap<String, GtLooseMineralBlock> looseDeposits =
            new ObjectMap<>();

    private static boolean loaded;

    private GtEarlyOreBlocks() {
    }

    public static synchronized void load() {
        if (loaded) return;
        loaded = true;

        for (GtOreCatalog.OreSpec spec :
                GtOreCatalog.ores()) {

            Item raw =
                    GtMaterials.require(
                            spec.id + "_raw-ore"
                    );

            ToggleOreBlock block =
                    new ToggleOreBlock(
                            "gt-ore-" + spec.id,
                            raw
                    );

            block.mapColor =
                    raw.color.cpy();

            oreBlocks.put(
                    spec.id,
                    block
            );

            if (spec.veinEligible) {
                veinOres.put(
                        spec.id,
                        block
                );
            }
        }

        for (GtOreCatalog.LooseDepositSpec spec :
                GtOreCatalog.looseDeposits()) {

            Item raw =
                    GtMaterials.require(
                            spec.materialId +
                                    "_raw-ore"
                    );

            GtLooseMineralBlock block =
                    new GtLooseMineralBlock(
                            "gt-deposit-" +
                                    spec.id,
                            spec.id,
                            spec.materialId,
                            raw
                    );

            block.mapColor =
                    spec.fallbackColor();

            looseDeposits.put(
                    spec.id,
                    block
            );
        }
    }

    public static Block ore(String id) {
        load();
        return oreBlocks.get(id);
    }

    public static Block veinOre(String id) {
        load();
        return veinOres.get(id);
    }

    public static GtLooseMineralBlock loose(
            String id) {

        load();
        return looseDeposits.get(id);
    }

    public static boolean isLooseDeposit(
            Block block) {

        return block instanceof
                GtLooseMineralBlock;
    }

    /**
     * Unified drill/miner output resolver.
     */
    public static Item drillDrop(
            Block overlay) {

        /*
         * Terrain-like deposits are deliberately not OreBlock instances.
         * This branch is what gives ModDrill/GtMinerBlock explicit support
         * for gravel/clay/sand-style mineral tiles.
         */
        if (overlay instanceof GtLooseMineralBlock) {
            return ((GtLooseMineralBlock) overlay)
                    .mineralDrop;
        }

        if (!(overlay instanceof OreBlock)) {
            return null;
        }

        Item source =
                ((OreBlock) overlay).itemDrop;

        if (source == null) {
            return null;
        }

        Item raw =
                GtMaterials.rawOreForDrop(
                        source
                );

        return raw != null
                ? raw
                : source;
    }

    public static Seq<Block> allVeinOres() {
        load();

        Seq<Block> result =
                new Seq<>();

        for (Block block :
                veinOres.values()) {

            result.add(block);
        }

        return result;
    }

    public static Seq<Block> allLooseDeposits() {
        load();

        Seq<Block> result =
                new Seq<>();

        for (GtLooseMineralBlock block :
                looseDeposits.values()) {

            result.add(block);
        }

        return result;
    }
}
