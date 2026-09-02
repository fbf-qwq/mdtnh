package mdtnh.combat.impl.abilities;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import mindustry.entities.abilities.Ability;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Unit;
import mindustry.ui.Bar;

/** UI-only counters for all DroneBayWeapon mounts. No orbiting dots are drawn. */
public class DroneBayStatusAbility extends Ability {
    public DroneBayStatusAbility() {
        display = false;
    }

    @Override
    public void displayBars(Unit unit, Table bars) {
        for (WeaponMount mount : unit.mounts) {
            if (!(mount.weapon instanceof DroneBayWeapon bay)) continue;

            bars.add(new Bar(
                () -> Core.bundle.format(
                    "bar.mdt-dronebay-count",
                    bay.activeDrones(mount),
                    bay.maxDrones
                ),
                () -> Color.valueOf("8fcbd1"),
                () -> bay.maxDrones <= 0 ? 0f : bay.activeDrones(mount) / (float)bay.maxDrones
            ));
            bars.row();
        }
    }
}
