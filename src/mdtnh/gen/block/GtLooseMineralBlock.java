package mdtnh.gen.block;

import mindustry.type.Item;
import mindustry.world.blocks.environment.OverlayFloor;

/**
 * Terrain-like mineable mineral overlay.
 *
 * <p>This is intentionally NOT an OreBlock. It is a map overlay/floor-like
 * deposit, kept outside normal MineralVein data. MDT drills recognize it
 * through GtEarlyOreBlocks.drillDrop(...).</p>
 */
public class GtLooseMineralBlock extends OverlayFloor {

    public final String depositId;
    public final String materialId;
    public final Item mineralDrop;

    public GtLooseMineralBlock(
            String name,
            String depositId,
            String materialId,
            Item drop) {

        super(name);

        this.depositId = depositId;
        this.materialId = materialId;
        this.mineralDrop = drop;

        /*
         * Floor already exposes itemDrop; setting it also makes the content
         * self-describing for map/editor tooling. The MDT drill still uses
         * mineralDrop explicitly, so it does not depend on OreBlock behavior.
         */
        this.itemDrop = drop;
        this.playerUnmineable = true;
    }
}
