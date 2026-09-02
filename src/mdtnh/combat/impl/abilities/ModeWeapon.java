package mdtnh.combat.impl.abilities;

import arc.Core;
import arc.func.Func2;
import arc.graphics.g2d.TextureRegion;
import mindustry.Vars;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.Mover;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Unit;
import mindustry.type.Weapon;

/**
 * One mount, many mutually-exclusive bullet modes.
 *
 * Default sprite convention:
 *
 *   <weapon-name>-mode-0
 *   <weapon-name>-mode-1
 *   ...
 *
 * Example PNG filenames in a Java mod:
 *
 *   mechanic-test-energy-weapon-mode-0.png
 *   mechanic-test-energy-weapon-mode-1.png
 *   mechanic-test-energy-weapon-mode-2.png
 *
 * Weapon.name should already be transformed using Vars.content.transformName(...).
 */
public class ModeWeapon extends Weapon {
    public BulletType[] modes = {};
    public String[] modeRegionNames = {};
    public Func2<Unit, WeaponMount, Integer> aiSelector;

    private TextureRegion[] modeRegions = {};

    public ModeWeapon() {
        super();
    }

    public ModeWeapon(String name) {
        super(name);
    }

    public ModeWeapon modes(BulletType... modes) {
        this.modes = modes == null ? new BulletType[0] : modes;
        if (this.modes.length > 0) bullet = this.modes[0];
        return this;
    }

    /** Optional explicit atlas-region names. */
    public ModeWeapon modeRegions(String... names) {
        modeRegionNames = names == null ? new String[0] : names;
        return this;
    }

    @Override
    public void load() {
        super.load();

        int count = Math.max(modes.length, modeRegionNames.length);
        modeRegions = new TextureRegion[count];

        for (int i = 0; i < count; i++) {
            String regionName =
                i < modeRegionNames.length
                    && modeRegionNames[i] != null
                    && !modeRegionNames[i].isEmpty()
                    ? modeRegionNames[i]
                    : name + "-mode-" + i;

            TextureRegion found = Core.atlas.find(regionName);
            modeRegions[i] = found != null && found.found() ? found : region;
        }
    }

    public int resolveMode(Unit unit, WeaponMount mount) {
        WeaponModeAbility state = WeaponModeAbility.find(unit);
        int max = Math.max(0, modes.length - 1);

        if (!Vars.headless
            && Vars.player != null
            && Vars.player.unit() == unit
            && state != null) {
            return Math.max(0, Math.min(max, state.mode()));
        }

        if (aiSelector != null) {
            Integer chosen = aiSelector.get(unit, mount);
            if (chosen != null) {
                int resolved = Math.max(0, Math.min(max, chosen));
                if (state != null) state.mode(unit, resolved, false);
                return resolved;
            }
        }

        return state == null
            ? 0
            : Math.max(0, Math.min(max, state.mode()));
    }

    public BulletType modeBullet(Unit unit, WeaponMount mount) {
        if (modes.length == 0) return bullet;
        return modes[resolveMode(unit, mount)];
    }

    public TextureRegion modeRegion(Unit unit, WeaponMount mount) {
        int index = resolveMode(unit, mount);

        if (index >= 0 && index < modeRegions.length) {
            TextureRegion selected = modeRegions[index];
            if (selected != null && selected.found()) return selected;
        }

        return region;
    }

    @Override
    public void update(Unit unit, WeaponMount mount) {
        BulletType previous = bullet;
        bullet = modeBullet(unit, mount);
        super.update(unit, mount);
        bullet = previous;
    }

    @Override
    protected void bullet(
        Unit unit,
        WeaponMount mount,
        float xOffset,
        float yOffset,
        float angleOffset,
        Mover mover
    ) {
        BulletType previous = bullet;
        bullet = modeBullet(unit, mount);
        super.bullet(unit, mount, xOffset, yOffset, angleOffset, mover);
        bullet = previous;
    }

    @Override
    public void draw(Unit unit, WeaponMount mount) {
        TextureRegion previous = region;
        region = modeRegion(unit, mount);
        super.draw(unit, mount);
        region = previous;
    }
}
