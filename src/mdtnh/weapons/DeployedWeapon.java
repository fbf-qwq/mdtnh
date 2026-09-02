package mdtnh.weapons;

import mdtnh.status.ModStatusEffects;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Unit;
import mindustry.type.Weapon;

/**
 * A weapon that can aim/reload normally but only creates bullets while the owner is deployed.
 * Used for deployable side batteries without giving every unit a custom controller.
 */
public class DeployedWeapon extends Weapon {

    public DeployedWeapon(String name) {
        super(name);
    }

    @Override
    protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation) {
        if (unit.hasEffect(ModStatusEffects.deployed)) {
            super.shoot(unit, mount, shootX, shootY, rotation);
        }
    }
}
