package mdtnh.gen.block;

import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.OreBlock;

public class ToggleOreBlock extends OreBlock{

    public ToggleOreBlock(Item ore){
        super(ore);
    }

    public ToggleOreBlock(String name, Item ore){
        super(name, ore);
    }

    @Override
    public void drawBase(Tile tile){

        // F8 全局关闭矿物渲染
        if(!ModOreRender.renderOres){
            return;
        }

        // 没有被地质勘探过
        if(!OreExplorationController.isDiscovered(tile)){
            return;
        }

        super.drawBase(tile);
    }
}
