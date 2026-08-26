package mdtnh;

import arc.graphics.Color;
import mindustry.type.Liquid;

/**
 * GT 常用化工流体。
 */
public final class GtLiquids {
    private GtLiquids() {}

    public static Liquid distilledWater;
    public static Liquid hydrogen;
    public static Liquid oxygen;
    public static Liquid chlorine;
    public static Liquid methane;
    public static Liquid ethylene;
    public static Liquid polyethylene;
    public static Liquid diesel;
    public static Liquid lightFuel;
    public static Liquid sulfuricAcid;
    public static Liquid hydrochloricAcid;
    public static Liquid sodiumPersulfate;
    public static Liquid mercury;
    public static Liquid biomass;
    public static Liquid lubricant;
    public static Liquid moltenRubber;

    public static void load() {
        if (distilledWater != null) return;

        distilledWater = liquid("gt-distilled-water", "A7D8FF");
        hydrogen = liquid("gt-hydrogen", "E8F7FF");
        oxygen = liquid("gt-oxygen", "9BC7FF");
        chlorine = liquid("gt-chlorine", "D8FF72");
        methane = liquid("gt-methane", "DCEBFF");
        ethylene = liquid("gt-ethylene", "E7F3FF");
        polyethylene = liquid("gt-polyethylene", "F2F2F2");
        diesel = liquid("gt-diesel", "D8B43D");
        lightFuel = liquid("gt-light-fuel", "E3C95F");
        sulfuricAcid = liquid("gt-sulfuric-acid", "C8C86A");
        hydrochloricAcid = liquid("gt-hydrochloric-acid", "DDEB9B");
        sodiumPersulfate = liquid("gt-sodium-persulfate", "DDE7FF");
        mercury = liquid("gt-mercury", "B8BEC8");
        biomass = liquid("gt-biomass", "7BAA46");
        lubricant = liquid("gt-lubricant", "C7B64A");
        moltenRubber = liquid("gt-molten-rubber", "2C2C2C");
    }

    private static Liquid liquid(String name, String color) {
        return new Liquid(name, Color.valueOf(color));
    }
}
