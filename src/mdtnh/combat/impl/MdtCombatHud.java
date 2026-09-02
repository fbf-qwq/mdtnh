package mdtnh.combat.impl;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Table;
import mdtnh.combat.impl.abilities.DroneBayWeapon;
import mdtnh.combat.impl.abilities.MechanicAbilities.AblativeArmor;
import mdtnh.combat.impl.abilities.MechanicAbilities.Capacitor;
import mdtnh.combat.impl.abilities.MechanicAbilities.HeatMechanic;
import mindustry.Vars;
import mindustry.entities.abilities.Ability;
import mindustry.entities.units.WeaponMount;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.gen.Unit;
import mindustry.ui.Bar;

/**
 * Small top-left status panel shown only while the local player directly controls
 * a unit that owns MDTNH resource/counter mechanics.
 */
public final class MdtCombatHud {
    private static boolean installed;
    private static Table root;
    private static Table bars;
    private static int lastUnitId = -1;

    public static void install() {
        if (installed || Vars.headless) return;
        installed = true;

        Events.on(ClientLoadEvent.class, event -> Core.app.post(MdtCombatHud::build));
    }

    private static void build() {
        if (Vars.ui == null || Vars.ui.hudGroup == null) return;

        root = new Table();
        root.setFillParent(true);
        root.top().left();
        root.marginTop(82f);
        root.marginLeft(10f);
        root.touchable = Touchable.disabled;

        bars = new Table();
        bars.defaults().growX().height(18f).padBottom(3f);
        root.add(bars).width(215f).top().left();

        root.visible(() -> {
            Unit unit = Vars.player == null ? null : Vars.player.unit();
            return unit != null && hasStatusBars(unit);
        });

        root.update(() -> {
            Unit unit = Vars.player == null ? null : Vars.player.unit();
            int id = unit == null ? -1 : unit.id;

            if (id != lastUnitId) {
                lastUnitId = id;
                rebuild(unit);
            }
        });

        Vars.ui.hudGroup.addChild(root);
    }

    private static boolean hasStatusBars(Unit unit) {
        if (unit == null) return false;

        for (Ability ability : unit.abilities) {
            if (ability instanceof HeatMechanic
                || ability instanceof Capacitor
                || ability instanceof AblativeArmor) {
                return true;
            }
        }

        for (WeaponMount mount : unit.mounts) {
            if (mount.weapon instanceof DroneBayWeapon) return true;
        }

        return false;
    }

    private static void rebuild(Unit unit) {
        if (bars == null) return;
        bars.clearChildren();

        if (unit == null) return;

        for (Ability ability : unit.abilities) {
            if (ability instanceof HeatMechanic heat) {
                bars.add(new Bar("bar.mdt-heat", Color.valueOf("ff8c63"), () -> heat.heatProgress(unit)));
                bars.row();
            } else if (ability instanceof Capacitor capacitor) {
                bars.add(new Bar("bar.mdt-energy", Color.valueOf("8fe9ff"), () -> capacitor.chargeProgress(unit)));
                bars.row();
            } else if (ability instanceof AblativeArmor armor) {
                bars.add(new Bar(
                    () -> Core.bundle.format(
                        "bar.mdt-ablative-count",
                        armor.armorPlatesRemaining(unit),
                        armor.armorPlatesMax(unit)
                    ),
                    () -> Color.valueOf("aab2b8"),
                    () -> armor.armorPlatesMax(unit) <= 0
                        ? 0f
                        : armor.armorPlatesRemaining(unit) / (float)armor.armorPlatesMax(unit)
                ));
                bars.row();
            }
        }

        for (WeaponMount mount : unit.mounts) {
            if (mount.weapon instanceof DroneBayWeapon bay) {
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

    private MdtCombatHud() {
    }
}
