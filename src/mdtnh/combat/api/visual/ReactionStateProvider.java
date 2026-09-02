package mdtnh.combat.api.visual;

import mindustry.gen.Unit;

public interface ReactionStateProvider {
    int reactionStacks(Unit unit);
    int reactionStackMax(Unit unit);
}
