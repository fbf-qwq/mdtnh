package mdtnh.gen.block;

import mindustry.type.Item;
import mdtnh.gen.blocks.ToggleOreBlock;
public class ModOre {
    public static ToggleOreBlock testOre;
    public static Item testOreItem;
    public static void load(){
        testOreItem=new Item("test-ore"){{
            hardness=1;
        }};
        testOre=new ToggleOreBlock(testOreItem){{
            variants = 1;
        }};
    }
}
