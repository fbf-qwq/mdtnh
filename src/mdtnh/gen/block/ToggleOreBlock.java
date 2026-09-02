package mdtnh.gen.blocks;

import arc.graphics.g2d.TextureRegion;

import mdtnh.gen.block.ModOreRender;
import mdtnh.gen.block.OreExplorationController;
import mindustry.Vars;
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

    private boolean shouldShow(Tile tile){
        if(
                Vars.state != null &&
                        Vars.state.isEditor()
        ){
            return true;
        }

        if(!ModOreRender.renderOres){
            return false;
        }
        return OreExplorationController
                .isDiscovered(tile);
    }
    @Override
    public void drawBase(Tile tile){

        if(!shouldShow(tile)){
            return;
        }

        super.drawBase(tile);
    }
    @Override
    public TextureRegion getDisplayIcon(Tile tile){

        if(shouldShow(tile)){
            return super.getDisplayIcon(tile);
        }
        return tile.floor().getDisplayIcon(tile);
    }
    @Override
    public String getDisplayName(Tile tile){

        if(shouldShow(tile)){
            return super.getDisplayName(tile);
        }
        return tile.floor().getDisplayName(tile);
    }
}
