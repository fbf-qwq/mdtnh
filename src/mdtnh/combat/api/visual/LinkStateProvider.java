package mdtnh.combat.api.visual;

import arc.func.Cons;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;

public interface LinkStateProvider {
    int linkCount(Unit unit);

    /** Avoids allocating a temporary Seq every draw tick. */
    void eachLink(Unit unit, Cons<Teamc> consumer);
}
