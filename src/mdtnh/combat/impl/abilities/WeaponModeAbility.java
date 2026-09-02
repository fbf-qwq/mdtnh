package mdtnh.combat.impl.abilities;

import arc.Core;
import arc.input.KeyCode;
import arc.util.Time;
import mindustry.Vars;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;

/**
 * Per-unit weapon-mode state.
 *
 * Direct player control:
 *   V cycles the mode.
 *
 * Visual feedback is handled by ModeWeapon by swapping the actual weapon sprite.
 * No colored ring or orbiting pips are drawn here.
 */
public class WeaponModeAbility extends Ability {
    public String[] modeBundleKeys = {};
    public KeyCode cycleKey = KeyCode.v;
    public boolean autoCycleAI = true;
    public float aiCycleTime = 180f;

    private int mode;
    private float aiTimer;

    public WeaponModeAbility(String... modeBundleKeys) {
        this.modeBundleKeys = modeBundleKeys == null ? new String[0] : modeBundleKeys;
        display = false;
    }

    public int modeCount() {
        return modeBundleKeys.length;
    }

    public int mode() {
        return Math.max(0, Math.min(mode, Math.max(0, modeCount() - 1)));
    }

    public void mode(Unit unit, int value, boolean announce) {
        if (modeCount() <= 0) {
            mode = 0;
            return;
        }

        int next = Math.floorMod(value, modeCount());
        if (next == mode) return;
        mode = next;

        if (announce && !Vars.headless && Vars.ui != null) {
            String key = modeBundleKeys[mode];
            String name = Core.bundle.get(key);
            Vars.ui.showInfoToast(
                Core.bundle.format("mdt.weaponmode.changed", name),
                2.0f
            );
        }
    }

    public void cycle(Unit unit, boolean announce) {
        mode(unit, mode() + 1, announce);
    }

    @Override
    public void update(Unit unit) {
        boolean localPlayer =
            !Vars.headless
            && Vars.player != null
            && Vars.player.unit() == unit;

        if (localPlayer) {
            if (Core.input.keyTap(cycleKey)) {
                cycle(unit, true);
            }
            aiTimer = 0f;
            return;
        }

        if (autoCycleAI && modeCount() > 1) {
            aiTimer += Time.delta;
            if (aiTimer >= aiCycleTime) {
                aiTimer = 0f;
                cycle(unit, false);
            }
        }
    }

    public static WeaponModeAbility find(Unit unit) {
        for (Ability ability : unit.abilities) {
            if (ability instanceof WeaponModeAbility modes) return modes;
        }
        return null;
    }

    @Override
    public Ability copy() {
        WeaponModeAbility out = (WeaponModeAbility)super.copy();
        out.mode = 0;
        out.aiTimer = 0f;
        return out;
    }
}
