package mdtnh.gen.block;

import mindustry.type.UnitType;

/**
 * 带独立矿物勘探半径的单位类型。
 *
 * oreScanRadius 只用于 OreExplorationController，
 * 与 Mindustry 原版 fogRadius 无关。
 */
public class SurveyUnitType extends UnitType {

    /** 矿物勘探半径，单位 Tile。 */
    public int oreScanRadius = 18;

    public SurveyUnitType(String name) {
        super(name);
    }
}
