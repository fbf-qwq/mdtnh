package mdtnh.combat.impl.abilities;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import arc.scene.ui.layout.Table;
import mdtnh.combat.api.resource.ChargeResource;
import mdtnh.combat.api.resource.HeatResource;
import mdtnh.combat.api.visual.ArmorStateProvider;
import mdtnh.combat.api.visual.MdtBodyOverlayProvider;
import mdtnh.combat.api.visual.DeployStateProvider;
import mdtnh.combat.api.visual.PhaseStateProvider;
import mdtnh.combat.impl.MdtCombatFx;
import mdtnh.combat.impl.MdtCombatStatuses;
import mdtnh.combat.impl.MdtCombatUtil;
import mdtnh.combat.impl.MdtDamageBus;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import mindustry.world.Tile;

/** Concrete B01-B15 unit mechanics. */
public final class MechanicAbilities {

    // B01
    public static class DeployMechanic extends Ability implements DeployStateProvider {
        public float deployTime = 90f;
        public float undeployTime = 24f;
        public float stationarySpeed = 0.10f;
        public float armorBonus = 4f;

        private float progress;

        @Override
        public void update(Unit unit) {
            boolean stationary = unit.vel.len() <= stationarySpeed;
            if (stationary) progress = Mathf.clamp(progress + Time.delta / deployTime);
            else progress = Mathf.clamp(progress - Time.delta / undeployTime);

            if (progress >= 0.999f) {
                unit.apply(MdtCombatStatuses.deployedBoost, 2f);
            }
        }

        @Override
        public float deployProgress(Unit unit) {
            return progress;
        }
    }

    // B02
    public static class HeatMechanic extends Ability implements HeatResource {
    public float heatPerShot = 0.045f;
    public float coolPerTick = 0.0035f;
    public float warmThreshold = 0.55f;
    public float recoverThreshold = 0.48f;

    private float heat;
    private boolean overheated;
    private int lastShots = -1;

    @Override
    public void update(Unit unit) {
        int shots = shotCount(unit);
        if (lastShots < 0) lastShots = shots;

        int fired = Math.max(0, shots - lastShots);
        lastShots = shots;

        if (fired > 0) heat += fired * heatPerShot;
        else heat -= coolPerTick * Time.delta;

        heat = Mathf.clamp(heat);

        if (!overheated && heat >= 0.999f) {
            overheated = true;
            Fx.bigShockwave.at(unit.x, unit.y);
        }

        if (overheated && heat <= recoverThreshold) {
            overheated = false;
            MdtCombatFx.softPulse.at(unit.x, unit.y, 0f, Pal.heal);
        }

        if (heat >= warmThreshold && !overheated) {
            unit.apply(StatusEffects.overclock, 2f);
        }

        if (overheated) {
            unit.apply(StatusEffects.disarmed, 2f);
        }
    }

    private int shotCount(Unit unit) {
        int total = 0;
        for (WeaponMount mount : unit.mounts) total += mount.totalShots;
        return total;
    }

    @Override
    public void displayBars(Unit unit, Table bars) {
        bars.add(new Bar("bar.mdt-heat", Color.valueOf("ff8c63"), () -> heatProgress(unit)));
        bars.row();
    }

    @Override
    public float heatProgress(Unit unit) {
        return heat;
    }

    @Override
    public void addHeat(Unit unit, float amount) {
        heat = Mathf.clamp(heat + amount);
    }
}

    // B03
    public static class FacingArmor extends Ability implements MdtBodyOverlayProvider {
    public float frontMultiplier = 0.65f;
    public float sideMultiplier = 1.00f;
    public float rearMultiplier = 1.30f;
    public float frontArc = 60f;
    public float rearArc = 60f;

    private float lastHealth = -1f;
    private float impactTime;
    private float impactRadius;
    private float impactAngle;
    private int impactZone = -1;

    private UnitType loadedType;
    private TextureRegion front, left, right, rear;

    private void load(Unit unit) {
        if (loadedType == unit.type) return;
        loadedType = unit.type;
        front = Core.atlas.find(unit.type.name + "-armor-front");
        left = Core.atlas.find(unit.type.name + "-armor-left");
        right = Core.atlas.find(unit.type.name + "-armor-right");
        rear = Core.atlas.find(unit.type.name + "-armor-rear");
    }

    @Override
    public void update(Unit unit) {
        load(unit);
        impactTime = Math.max(0f, impactTime - Time.delta);

        if (lastHealth < 0f) {
            lastHealth = unit.health;
            return;
        }

        float loss = lastHealth - unit.health;

        if (loss > 0.001f && !Vars.net.client()) {
            MdtDamageBus.HitInfo hit = MdtDamageBus.recent(unit, 3f);

            if (hit != null) {
                float relative = (hit.incomingFromAngle - unit.rotation) % 360f;
                if (relative < 0f) relative += 360f;

                float multiplier;
                if (relative <= frontArc || relative >= 360f - frontArc) {
                    multiplier = frontMultiplier;
                    impactZone = 0;
                } else if (relative >= 180f - rearArc && relative <= 180f + rearArc) {
                    multiplier = rearMultiplier;
                    impactZone = 3;
                } else if (relative < 180f) {
                    multiplier = sideMultiplier;
                    impactZone = 1;
                } else {
                    multiplier = sideMultiplier;
                    impactZone = 2;
                }

                if (multiplier < 1f) unit.heal(loss * (1f - multiplier));
                else if (multiplier > 1f) unit.damagePierce(loss * (multiplier - 1f));

                impactAngle = hit.incomingFromAngle;
                impactRadius = Mathf.clamp(
                    5f + loss / Math.max(1f, unit.maxHealth) * unit.hitSize * 7f,
                    5f,
                    unit.hitSize * 0.72f
                );
                impactTime = 14f;

                float ix = unit.x + Angles.trnsx(impactAngle, unit.hitSize * 0.46f);
                float iy = unit.y + Angles.trnsy(impactAngle, unit.hitSize * 0.46f);
                Fx.pointShockwave.at(ix, iy, impactRadius, Color.valueOf("7d8991"));
            }
        }

        lastHealth = unit.health;
    }

    private TextureRegion zoneRegion(int zone) {
        return switch (zone) {
            case 0 -> front;
            case 1 -> left;
            case 2 -> right;
            case 3 -> rear;
            default -> null;
        };
    }

    private void drawRegion(TextureRegion region, Unit unit) {
        if (region == null || !region.found()) return;
        unit.type.applyColor(unit);
        Draw.rect(region, unit.x, unit.y, unit.rotation - 90f);
        Draw.reset();
    }

    @Override
    public void drawBodyOverlay(Unit unit) {
        load(unit);

        // Independent armor plates are actual sprite overlays, drawn before weapons.
        drawRegion(front, unit);
        drawRegion(left, unit);
        drawRegion(right, unit);
        drawRegion(rear, unit);

        if (impactTime <= 0f) return;

        TextureRegion hit = zoneRegion(impactZone);
        if (hit != null && hit.found()) {
            float alpha = Mathf.clamp(impactTime / 14f);
            Draw.mixcol(Color.black, 0.30f * alpha);
            Draw.alpha(0.32f * alpha);
            Draw.rect(hit, unit.x, unit.y, unit.rotation - 90f);
            Draw.reset();
        }
    }
}

    // B04
    public static class RecoilAnchor extends Ability {
    public float velocityRetain = 0.36f;
    public float lockTime = 12f;

    private int lastShots = -1;
    private float lock;

    @Override
    public void update(Unit unit) {
        int shots = 0;
        for (WeaponMount mount : unit.mounts) shots += mount.totalShots;
        if (lastShots < 0) lastShots = shots;

        if (shots > lastShots) {
            unit.vel.scl(velocityRetain);
            lock = lockTime;
        }
        lastShots = shots;

        if (lock > 0f) {
            lock -= Time.delta;
            unit.vel.scl(0.92f);
        }
    }
}


    // B05 is implemented by ModeWeapon + WeaponModeAbility in Phase 2.


    // B06
    public static class DamageGate extends Ability {
    public float thresholdFraction = 0.12f;
    public float excessMultiplier = 0.45f;

    private float lastHealth = -1f;

    @Override
    public void update(Unit unit) {
        if (lastHealth < 0f) {
            lastHealth = unit.health;
            return;
        }

        float loss = lastHealth - unit.health;
        float threshold = unit.maxHealth * thresholdFraction;

        if (loss > threshold && !Vars.net.client()) {
            float excess = loss - threshold;
            float refund = excess * (1f - excessMultiplier);
            unit.heal(refund);

            Fx.unitShieldBreak.at(unit.x, unit.y, 0f, Color.valueOf("bfe6ff"), unit);
            Fx.bigShockwave.at(unit.x, unit.y);
        }

        lastHealth = unit.health;
    }
}

    // B07
    public static class AblativeArmor extends Ability implements ArmorStateProvider {
    public int maxPlates = 6;
    public float triggerDamageFraction = 0.07f;
    public float absorbedFraction = 0.62f;

    private int plates = -1;
    private float lastHealth = -1f;

    @Override
    public void update(Unit unit) {
        if (plates < 0) plates = maxPlates;

        if (lastHealth < 0f) {
            lastHealth = unit.health;
            return;
        }

        float loss = lastHealth - unit.health;

        if (plates > 0 && loss >= unit.maxHealth * triggerDamageFraction && !Vars.net.client()) {
            plates--;
            unit.heal(loss * absorbedFraction);

            float size = Mathf.clamp(
                7f + loss / Math.max(1f, unit.maxHealth) * unit.hitSize * 6f,
                7f,
                unit.hitSize * 0.65f
            );
            Fx.pointShockwave.at(unit.x, unit.y, size, Color.valueOf("858f96"));
        }

        lastHealth = unit.health;
    }

    @Override
    public void displayBars(Unit unit, Table bars) {
        bars.add(new Bar(
            () -> Core.bundle.format("bar.mdt-ablative-count", armorPlatesRemaining(unit), maxPlates),
            () -> Color.valueOf("aab2b8"),
            () -> maxPlates <= 0 ? 0f : armorPlatesRemaining(unit) / (float)maxPlates
        ));
        bars.row();
    }

    @Override
    public int armorPlatesRemaining(Unit unit) {
        return Math.max(0, plates);
    }

    @Override
    public int armorPlatesMax(Unit unit) {
        return maxPlates;
    }
}

    // B08
    public static class AdaptiveArmor extends Ability {
    public int maxStacks = 3;
    public float reductionPerStack = 0.08f;
    public float memoryTime = 150f;

    private float lastHealth = -1f;
    private int lastBulletType = -2;
    private int stacks;
    private float memory;
    private float flash;

    @Override
    public void update(Unit unit) {
        flash = Math.max(0f, flash - Time.delta);

        if (lastHealth < 0f) {
            lastHealth = unit.health;
            return;
        }

        memory -= Time.delta;
        if (memory <= 0f) {
            stacks = 0;
            lastBulletType = -2;
        }

        float loss = lastHealth - unit.health;

        if (loss > 0.001f && !Vars.net.client()) {
            MdtDamageBus.HitInfo hit = MdtDamageBus.recent(unit, 3f);

            if (hit != null) {
                int previousStacks = stacks;

                if (hit.bulletTypeId == lastBulletType) {
                    stacks = Math.min(maxStacks, stacks + 1);
                } else {
                    lastBulletType = hit.bulletTypeId;
                    stacks = 1;
                }

                memory = memoryTime;
                float reduction = Mathf.clamp(stacks * reductionPerStack, 0f, 0.45f);
                unit.heal(loss * reduction);

                if (stacks != previousStacks) {
                    flash = 24f;
                    Fx.overdriveWave.at(
                        unit.x, unit.y,
                        unit.hitSize * (1.2f + stacks * 0.25f),
                        Color.valueOf("8ecfe6")
                    );
                    MdtCombatFx.supportPulse.at(
                        unit.x, unit.y,
                        unit.hitSize * (1.1f + stacks * 0.22f),
                        Color.valueOf("8ecfe6")
                    );
                }
            }
        }

        lastHealth = unit.health;
    }

    @Override
    public void draw(Unit unit) {
        if (stacks <= 0) return;

        Draw.color(Color.valueOf("8ecfe6"));
        Draw.alpha(0.28f + (flash > 0f ? 0.45f : 0f));
        Lines.stroke(1.2f + stacks * 0.55f);

        for (int i = 0; i < stacks; i++) {
            Lines.poly(
                unit.x, unit.y,
                6,
                unit.hitSize * (0.58f + i * 0.12f),
                Time.time * (0.15f + i * 0.04f)
            );
        }

        Draw.reset();
    }
}

    // B09
    public static class Capacitor extends Ability implements ChargeResource {
    public float chargePerShot = 0.08f;
    public float chargePerDamageFraction = 0.65f;
    public float shieldGain = 420f;
    public float shieldCap = 1000f;

    private float charge;
    private float lastHealth = -1f;
    private int lastShots = -1;

    @Override
    public void update(Unit unit) {
        if (lastHealth < 0f) lastHealth = unit.health;

        int shots = 0;
        for (WeaponMount mount : unit.mounts) shots += mount.totalShots;
        if (lastShots < 0) lastShots = shots;

        int fired = Math.max(0, shots - lastShots);
        if (fired > 0) charge += fired * chargePerShot;
        lastShots = shots;

        float loss = Math.max(0f, lastHealth - unit.health);
        if (loss > 0f) {
            charge += (loss / Math.max(1f, unit.maxHealth)) * chargePerDamageFraction;
        }
        lastHealth = unit.health;

        if (charge >= 1f) {
            charge -= 1f;
            unit.shield = Math.min(shieldCap, unit.shield + shieldGain);
            MdtCombatFx.softPulse.at(unit.x, unit.y, 0f, Color.valueOf("8fe9ff"));
        }

        charge = Mathf.clamp(charge);
    }

    @Override
    public void displayBars(Unit unit, Table bars) {
        bars.add(new Bar("bar.mdt-energy", Color.valueOf("8fe9ff"), () -> chargeProgress(unit)));
        bars.row();
    }

    @Override
    public float chargeProgress(Unit unit) {
        return charge;
    }

    @Override
    public void addCharge(Unit unit, float amount) {
        charge = Mathf.clamp(charge + amount);
    }
}

    // B10
    public static class Momentum extends Ability {
    public float threshold = 0.92f;

    @Override
    public void update(Unit unit) {
        float ratio = Mathf.clamp(unit.vel.len() / Math.max(0.001f, unit.speed()));
        if (ratio >= threshold) unit.apply(StatusEffects.fast, 2f);
    }

    @Override
    public void draw(Unit unit) {
        float ratio = Mathf.clamp(unit.vel.len() / Math.max(0.001f, unit.speed()));
        if (ratio < threshold) return;

        float power = Mathf.clamp((ratio - threshold) / Math.max(0.01f, 1f - threshold));
        float back = Math.max(unit.type.engineOffset, unit.hitSize * 0.38f);
        float x = unit.x + Angles.trnsx(unit.rotation + 180f, back);
        float y = unit.y + Angles.trnsy(unit.rotation + 180f, back);

        Color outer = unit.type.engineColor == null ? unit.team.color : unit.type.engineColor;
        Color inner = unit.type.engineColorInner;

        Draw.color(outer);
        Draw.alpha(0.68f + 0.25f * power);
        Drawf.tri(
            x, y,
            Math.max(3.5f, unit.type.engineSize * (1.05f + 0.35f * power)),
            13f + 19f * power,
            unit.rotation + 180f
        );

        Draw.color(inner);
        Draw.alpha(0.82f);
        Drawf.tri(
            x, y,
            Math.max(2.1f, unit.type.engineSize * 0.62f),
            8f + 12f * power,
            unit.rotation + 180f
        );

        // Short fading flame tail, no energy-line "light bulb".
        for (int i = 1; i <= 3; i++) {
            float dst = 10f + i * (6f + 3f * power);
            Draw.color(outer);
            Draw.alpha((0.24f - i * 0.05f) * (0.6f + power));
            Fill.circle(
                x + Angles.trnsx(unit.rotation + 180f, dst),
                y + Angles.trnsy(unit.rotation + 180f, dst),
                Math.max(0.8f, 2.4f - i * 0.45f)
            );
        }

        Draw.reset();
    }
}

    // B11
    public static class PhaseBlink extends Ability implements PhaseStateProvider {
        public float healthThreshold = 0.45f;
        public float rearmThreshold = 0.70f;
        public float threatRange = 150f;
        public float distance = 70f;
        public float cooldown = 180f;

        private boolean armed = true;
        private float timer;
        private float visualTime;

        @Override
        public void update(Unit unit) {
            timer = Math.max(0f, timer - Time.delta);
            visualTime = Math.max(0f, visualTime - Time.delta);

            if (!armed && unit.healthf() >= rearmThreshold) armed = true;
            if (!armed || timer > 0f || unit.healthf() > healthThreshold) return;

            Teamc threat = Units.closestTarget(unit.team, unit.x, unit.y, threatRange);
            if (threat == null) return;

            float away = threat.angleTo(unit);
            float tx = unit.x + Angles.trnsx(away, distance);
            float ty = unit.y + Angles.trnsy(away, distance);

            if (!unit.isFlying()) {
                Tile tile = Vars.world.tileWorld(tx, ty);
                if (tile == null || tile.solid()) return;
            }

            if (!Vars.net.client()) {
                MdtCombatFx.phase.at(unit.x, unit.y, unit.rotation, Color.valueOf("b9b6ff"));
                unit.set(tx, ty);
                MdtCombatFx.phase.at(unit.x, unit.y, unit.rotation, Color.valueOf("b9b6ff"));
            }

            armed = false;
            timer = cooldown;
            visualTime = 18f;
        }

        @Override
        public boolean phased(Unit unit) {
            return visualTime > 0f;
        }

        @Override
        public float phaseStrength(Unit unit) {
            return Mathf.clamp(visualTime / 18f);
        }
    }



    // B13
    public static class LastStand extends Ability {
    public float threshold = 0.25f;

    @Override
    public void update(Unit unit) {
        if (unit.healthf() <= threshold) {
            unit.apply(StatusEffects.overclock, 2f);
        }
    }

    @Override
    public void draw(Unit unit) {
        if (unit.healthf() > threshold) return;

        Draw.color(Color.valueOf("a91628"));
        Draw.alpha(0.46f + Mathf.absin(Time.time, 7f, 0.12f));

        for (WeaponMount mount : unit.mounts) {
            var weapon = mount.weapon;

            float rotation = unit.rotation - 90f;
            float realRecoil = Mathf.pow(mount.recoil, weapon.recoilPow) * weapon.recoil;
            float weaponRotation = rotation + (weapon.rotate ? mount.rotation : weapon.baseRotation);

            float wx = unit.x
                + Angles.trnsx(rotation, weapon.x, weapon.y)
                + Angles.trnsx(weaponRotation, 0f, -realRecoil);
            float wy = unit.y
                + Angles.trnsy(rotation, weapon.x, weapon.y)
                + Angles.trnsy(weaponRotation, 0f, -realRecoil);

            float tail = Math.max(5f, Math.abs(weapon.shootY) * 0.55f + 4f);
            float cx = wx + Angles.trnsx(weaponRotation, 0f, -tail);
            float cy = wy + Angles.trnsy(weaponRotation, 0f, -tail);
            float radius = 5f + Math.min(7f, unit.hitSize * 0.14f);

            Lines.stroke(1.15f);
            Lines.circle(cx, cy, radius);
            Lines.poly(cx, cy, 6, radius * 0.74f, Time.time * 0.55f);
            Lines.poly(cx, cy, 3, radius * 0.48f, -Time.time * 0.72f);

            for (int i = 0; i < 3; i++) {
                float a = weaponRotation + 120f * i + Time.time * 0.25f;
                Lines.lineAngle(
                    cx + Angles.trnsx(a, radius * 0.78f),
                    cy + Angles.trnsy(a, radius * 0.78f),
                    a,
                    3f
                );
            }
        }

        Draw.reset();
    }
}

    // B14
    public static class CounterBattery extends Ability {
        public float memoryTime = 180f;

        private Teamc attacker;
        private float timer;

        @Override
        public void update(Unit unit) {
            MdtDamageBus.HitInfo hit = MdtDamageBus.recent(unit, 3f);
            if (hit != null && hit.attacker != null && hit.attacker.team() != unit.team) {
                attacker = hit.attacker;
                timer = memoryTime;
            }

            timer -= Time.delta;
            if (timer <= 0f || attacker == null) {
                attacker = null;
                return;
            }

            if (!unit.isPlayer()) {
                for (WeaponMount mount : unit.mounts) {
                    if (!mount.weapon.autoTarget) continue;
                    mount.target = attacker;
                    mount.aimX = attacker.x();
                    mount.aimY = attacker.y();
                    mount.shoot = true;
                    mount.rotate = true;
                }
            }
        }

        @Override
        public void draw(Unit unit) {
            if (attacker == null || timer <= 0f) return;
            Draw.color(Color.valueOf("ffcf77"));
            Draw.alpha(0.55f);
            Lines.stroke(0.8f);
            Lines.line(unit.x, unit.y, attacker.x(), attacker.y());
            Lines.circle(attacker.x(), attacker.y(), 8f + Mathf.absin(Time.time, 4f, 2f));
            Draw.reset();
        }
    }

    // B15
    public static class BurstDrive extends Ability {
        public float fullSpeedThreshold = 0.95f;
        public float impulse = 4.5f;
        public float reload = 120f;

        private float timer;

        @Override
        public void update(Unit unit) {
            timer = Math.max(0f, timer - Time.delta);
            if (timer > 0f || unit.vel.isZero()) return;

            float ratio = unit.vel.len() / Math.max(0.001f, unit.speed());
            if (ratio < fullSpeedThreshold) return;

            unit.vel.add(Tmp.v1.set(unit.vel).nor().scl(impulse));
            MdtCombatFx.phase.at(unit.x, unit.y, unit.vel.angle(), Color.valueOf("7fd6ff"));
            timer = reload;
        }
    }

    private MechanicAbilities() {}
}
