package mdtnh;

import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.OreBlock;

/** GTNH 小采矿机：LV/MV/HV 分别使用 17/33/49 见方的扫描区域。 */
public class GtMinerBlock extends GtAutoRecipeCrafter {
    public final VoltageTier tier;
    public final int diameter;

    public GtMinerBlock(String name, VoltageTier tier, int diameter, float craftTicks) {
        super(name);
        this.tier = tier;
        this.diameter = diameter;
        size = 2;
        itemCapacity = 80;
        liquidCapacity = 0f;
        hasLiquids = false;

        energySpec.voltageV = tier.maxVoltageV;
        energySpec.minInputVoltageV = tier.minVoltageV;
        energySpec.maxInputVoltageV = tier.maxVoltageV;
        energySpec.capacityJ = Math.max(tier.capacityJ, tier.maxVoltageV * 8f);
        energySpec.maxInputA = 1;
        energySpec.maxOutputA = 0;

        float seconds = craftTicks / 60f;
        float energy = tier.maxVoltageV * seconds;
        groups = new RecipeGroup[]{
                new RecipeGroup("mining", new Recipe[]{
                        new Recipe(null, null, null, null, craftTicks, energy)
                })
        };
        buildType = GtMinerBuild::new;
    }

    public class GtMinerBuild extends GtAutoBuild {
        @Override
        protected void craft(Recipe recipe) {
            Tile target = findOreTile();
            if (target == null) return;

            OreBlock ore = target.overlay() instanceof OreBlock ? (OreBlock) target.overlay() : null;
            if (ore == null || ore.itemDrop == null) return;

            Item output = GtMaterials.rawOreForDrop(ore.itemDrop);
            if (output == null) output = ore.itemDrop;

            offload(output);
            target.setOverlay(Blocks.air);
        }

        private Tile findOreTile() {
            if (tile == null) return null;
            int radius = Math.max(1, diameter / 2);
            for (int ring = 0; ring <= radius; ring++) {
                for (int dx = -ring; dx <= ring; dx++) {
                    Tile top = checkOre(tile.x + dx, tile.y + ring);
                    if (top != null) return top;
                    Tile bottom = checkOre(tile.x + dx, tile.y - ring);
                    if (bottom != null) return bottom;
                }
                for (int dy = -ring + 1; dy <= ring - 1; dy++) {
                    Tile right = checkOre(tile.x + ring, tile.y + dy);
                    if (right != null) return right;
                    Tile left = checkOre(tile.x - ring, tile.y + dy);
                    if (left != null) return left;
                }
            }
            return null;
        }

        private Tile checkOre(int x, int y) {
            Tile t = Vars.world.tile(x, y);
            if (t == null) return null;
            return t.overlay() instanceof OreBlock ? t : null;
        }
    }
}
