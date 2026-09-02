package mdtnh.combat.impl.support;

import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.struct.IntMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mdtnh.combat.api.MechanicId;
import mdtnh.combat.api.resource.ChargeResource;
import mdtnh.combat.api.resource.HeatResource;
import mdtnh.combat.api.support.MdtSupportAbility;
import mdtnh.combat.api.support.SupportMode;
import mdtnh.combat.api.visual.HeatStateProvider;
import mdtnh.combat.api.visual.LinkStateProvider;
import mdtnh.combat.impl.MdtCombatFx;
import mdtnh.combat.impl.MdtCombatModifiers;
import mdtnh.combat.impl.MdtCombatStatuses;
import mdtnh.combat.impl.MdtCombatUtil;
import mdtnh.combat.impl.MdtDamageBus;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.entities.units.BuildPlan;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.world.blocks.ConstructBlock.ConstructBuild;

/** Concrete S01-S20 support mechanics. */
public final class SupportAbilities {

    // S01
    public static class FireControlLink extends Ability implements MdtSupportAbility, LinkStateProvider {
    public float linkRange = 190f;
    public float targetRange = 290f;
    public float sharedTargetDamageBonus = 0.14f;

    private Teamc target;
    private Seq<Teamc> links = new Seq<>();

    @Override
    public void update(Unit source) {
        links.clear();
        target = Units.closestTarget(source.team, source.x, source.y, targetRange);

        if (target == null) return;

        MdtCombatModifiers.mark(target, 0.06f, 12f);

        Units.nearby(source.team, source.x, source.y, linkRange, ally -> {
            if (ally == source || ally.dead) return;

            links.add(ally);
            MdtCombatModifiers.link(ally, target, sharedTargetDamageBonus, 12f);

            // Keep the vanilla weapon mount pointed at the shared target where possible;
            // the registry bonus makes the mechanic useful even if another AI later retargets.
            if (!ally.isPlayer()) {
                for (WeaponMount mount : ally.mounts) {
                    mount.target = target;
                    mount.aimX = target.x();
                    mount.aimY = target.y();
                }
            }
        });
    }

    @Override
    public void draw(Unit source) {
        if (target == null) return;

        Draw.color(Pal.accent);
        Draw.alpha(0.68f);
        Lines.stroke(1.4f);

        for (Teamc ally : links) {
            Lines.line(source.x, source.y, ally.x(), ally.y());
            Lines.line(ally.x(), ally.y(), target.x(), target.y());
        }

        Lines.square(target.x(), target.y(), 12f + Mathf.absin(Time.time, 5f, 3f), 45f);
        Draw.reset();
    }

    @Override public MechanicId mechanicId() { return MechanicId.FIRE_CONTROL_LINK; }
    @Override public SupportMode supportMode() { return SupportMode.LINK; }
    @Override public float supportRange(Unit source) { return linkRange; }
    @Override public boolean canSupport(Unit source, Teamc target) { return target != null && target.team() == source.team; }
    @Override public void applySupport(Unit source, Teamc target, float delta) {}

    @Override public int linkCount(Unit unit) { return links.size; }
    @Override public void eachLink(Unit unit, Cons<Teamc> consumer) { links.each(consumer); }

    @Override
    public Ability copy() {
        FireControlLink out = (FireControlLink)super.copy();
        out.links = new Seq<>();
        out.target = null;
        return out;
    }
}

    // S02
    public static class CounterBatteryMark extends Ability implements MdtSupportAbility {
    public float range = 240f;
    public float markTime = 180f;
    public float allyDamageBonus = 0.22f;

    private Teamc marked;
    private float timer;

    @Override
    public void update(Unit source) {
        timer -= Time.delta;

        final Teamc[] found = {null};

        Units.nearby(source.team, source.x, source.y, range, ally -> {
            if (found[0] != null) return;

            MdtDamageBus.HitInfo hit = MdtDamageBus.recent(ally, 10f);
            if (hit != null && hit.attacker != null && hit.attacker.team() != source.team) {
                found[0] = hit.attacker;
            }
        });

        if (found[0] != null) {
            marked = found[0];
            timer = markTime;
            MdtCombatModifiers.mark(marked, allyDamageBonus, markTime);

            Fx.overdriveWave.at(marked.x(), marked.y(), 46f, Color.valueOf("ffb85f"));
        }

        if (timer <= 0f) marked = null;
    }

    @Override
    public void draw(Unit source) {
        if (marked == null || timer <= 0f) return;

        Draw.color(Color.valueOf("ffb85f"));
        Draw.alpha(0.82f);
        Lines.stroke(2f);
        Lines.square(marked.x(), marked.y(), 13f + Mathf.absin(Time.time, 4f, 4f), 45f);
        Lines.square(marked.x(), marked.y(), 20f + Mathf.absin(Time.time, 5f, 3f), 0f);
        Lines.line(source.x, source.y, marked.x(), marked.y());
        Draw.reset();
    }

    @Override public MechanicId mechanicId() { return MechanicId.COUNTER_BATTERY_MARK; }
    @Override public SupportMode supportMode() { return SupportMode.INFORMATION; }
    @Override public float supportRange(Unit source) { return range; }
    @Override public boolean canSupport(Unit source, Teamc target) { return target != null && target.team() == source.team; }
    @Override public void applySupport(Unit source, Teamc target, float delta) {}
}

    // S03
    public static class HeatTransfer extends Ability implements MdtSupportAbility, HeatResource {
    public float range = 155f;
    public float reload = 100f;
    public float coolAmount = 0.26f;
    public float selfHeatGain = 0.08f;
    public float buffDuration = 95f;

    private float timer;
    private float sinkHeat;

    @Override
    public void update(Unit source) {
        sinkHeat = Mathf.clamp(sinkHeat - 0.0028f * Time.delta);
        timer += Time.delta;

        if (timer < reload) return;
        timer = 0f;

        final int[] affected = {0};

        Units.nearby(source.team, source.x, source.y, range, ally -> {
            if (ally == source) return;

            HeatResource resource = MdtCombatUtil.heatResource(ally);
            if (resource == null || resource.heatProgress(ally) <= 0.08f) return;

            resource.addHeat(ally, -coolAmount);
            ally.apply(StatusEffects.overclock, buffDuration);

            // Dim compact feedback around both participants; no bright global pulse/chain.
            MdtCombatFx.softPulse.at(ally.x, ally.y, 0f, Color.valueOf("627e73"));
            affected[0]++;
        });

        if (affected[0] > 0) {
            sinkHeat = Mathf.clamp(sinkHeat + selfHeatGain * affected[0]);
            MdtCombatFx.softPulse.at(source.x, source.y, 0f, Color.valueOf("627e73"));
        }
    }

    @Override public float heatProgress(Unit unit) { return sinkHeat; }
    @Override public void addHeat(Unit unit, float amount) { sinkHeat = Mathf.clamp(sinkHeat + amount); }

    @Override public MechanicId mechanicId() { return MechanicId.HEAT_TRANSFER; }
    @Override public SupportMode supportMode() { return SupportMode.AREA; }
    @Override public float supportRange(Unit source) { return range; }
    @Override public boolean canSupport(Unit source, Teamc target) { return target instanceof Unit u && u.team == source.team; }
    @Override public void applySupport(Unit source, Teamc target, float delta) {}
}

    // S04
    public static class CapacitorTransfer extends Ability implements MdtSupportAbility {
    public float range = 155f;
    public float reload = 115f;
    public float chargePulse = 0.24f;
    public float buffDuration = 90f;

    private float timer;

    @Override
    public void update(Unit source) {
        timer += Time.delta;
        if (timer < reload) return;
        timer = 0f;

        final int[] affected = {0};

        Units.nearby(source.team, source.x, source.y, range, ally -> {
            if (ally == source) return;

            ChargeResource resource = MdtCombatUtil.chargeResource(ally);
            if (resource == null) return;

            resource.addCharge(ally, chargePulse);
            ally.apply(StatusEffects.overclock, buffDuration);
            MdtCombatFx.softPulse.at(ally.x, ally.y, 0f, Color.valueOf("55758b"));
            affected[0]++;
        });

        if (affected[0] > 0) {
            MdtCombatFx.softPulse.at(source.x, source.y, 0f, Color.valueOf("55758b"));
        }
    }

    @Override public MechanicId mechanicId() { return MechanicId.CAPACITOR_TRANSFER; }
    @Override public SupportMode supportMode() { return SupportMode.AREA; }
    @Override public float supportRange(Unit source) { return range; }
    @Override public boolean canSupport(Unit source, Teamc target) { return target instanceof Unit u && u.team == source.team; }
    @Override public void applySupport(Unit source, Teamc target, float delta) {}
}

    // S05
    public static class ReloadService extends Ability implements MdtSupportAbility {
    public float range = 145f;
    public float reload = 100f;
    public float directReloadFraction = 0.28f;
    public float buffDuration = 110f;

    private float timer;

    @Override
    public void update(Unit source) {
        timer += Time.delta;
        if (timer < reload) return;
        timer = 0f;

        final int[] affected = {0};

        Units.nearby(source.team, source.x, source.y, range, ally -> {
            if (ally == source || ally.mounts.length == 0) return;

            for (WeaponMount mount : ally.mounts) {
                mount.reload = Math.max(0f, mount.reload - mount.weapon.reload * directReloadFraction);
            }

            ally.apply(StatusEffects.overclock, buffDuration);
            MdtCombatFx.softPulse.at(ally.x, ally.y, 0f, Color.valueOf("67777d"));
            affected[0]++;
        });

        if (affected[0] > 0) {
            MdtCombatFx.softPulse.at(source.x, source.y, 0f, Color.valueOf("67777d"));
        }
    }

    @Override public MechanicId mechanicId() { return MechanicId.RELOAD_SERVICE; }
    @Override public SupportMode supportMode() { return SupportMode.AREA; }
    @Override public float supportRange(Unit source) { return range; }
    @Override public boolean canSupport(Unit source, Teamc target) { return target instanceof Unit u && u.team == source.team; }
    @Override public void applySupport(Unit source, Teamc target, float delta) {}
}

    // S06
    public static class StatusCleanse extends Ability implements MdtSupportAbility {
    public float range = 145f;
    public float reload = 125f;
    public float afterBuff = 65f;

    private float timer;

    @Override
    public void update(Unit source) {
        timer += Time.delta;
        if (timer < reload) return;
        timer = 0f;

        final int[] affected = {0};

        Units.nearby(source.team, source.x, source.y, range, ally -> {
            if (ally == source) return;

            boolean dirty =
                ally.hasEffect(StatusEffects.burning)
                || ally.hasEffect(StatusEffects.freezing)
                || ally.hasEffect(StatusEffects.slow)
                || ally.hasEffect(StatusEffects.wet)
                || ally.hasEffect(StatusEffects.muddy)
                || ally.hasEffect(StatusEffects.melting)
                || ally.hasEffect(StatusEffects.sapped)
                || ally.hasEffect(StatusEffects.tarred)
                || ally.hasEffect(StatusEffects.electrified)
                || ally.hasEffect(StatusEffects.sporeSlowed)
                || ally.hasEffect(StatusEffects.shocked)
                || ally.hasEffect(StatusEffects.blasted)
                || ally.hasEffect(StatusEffects.corroded)
                || ally.hasEffect(StatusEffects.disarmed);

            if (!dirty) return;

            ally.unapply(StatusEffects.burning);
            ally.unapply(StatusEffects.freezing);
            ally.unapply(StatusEffects.slow);
            ally.unapply(StatusEffects.wet);
            ally.unapply(StatusEffects.muddy);
            ally.unapply(StatusEffects.melting);
            ally.unapply(StatusEffects.sapped);
            ally.unapply(StatusEffects.tarred);
            ally.unapply(StatusEffects.electrified);
            ally.unapply(StatusEffects.sporeSlowed);
            ally.unapply(StatusEffects.shocked);
            ally.unapply(StatusEffects.blasted);
            ally.unapply(StatusEffects.corroded);
            ally.unapply(StatusEffects.disarmed);

            ally.apply(StatusEffects.fast, afterBuff);
            Fx.chainLightning.at(source.x, source.y, 0f, Pal.heal, ally);
            Fx.healWaveMend.at(ally.x, ally.y, ally.hitSize * 1.5f, Pal.heal);
            affected[0]++;
        });

        if (affected[0] > 0) {
            Fx.healWaveMend.at(source.x, source.y, range, Pal.heal);
            MdtCombatFx.supportPulse.at(source.x, source.y, range, Pal.heal);
        }
    }

    @Override public MechanicId mechanicId() { return MechanicId.STATUS_CLEANSE; }
    @Override public SupportMode supportMode() { return SupportMode.AREA; }
    @Override public float supportRange(Unit source) { return range; }
    @Override public boolean canSupport(Unit source, Teamc target) { return target instanceof Unit u && u.team == source.team; }
    @Override public void applySupport(Unit source, Teamc target, float delta) {}
}

    // S07
    public static class RescueTractor extends Ability implements MdtSupportAbility {
    public float range = 155f;
    public float triggerHealth = 0.35f;
    public float pull = 0.075f;
    /** Fraction of max health restored per simulation tick while being rescued. */
    public float healFractionPerTick = 0.00025f;

    private Unit rescued;

    @Override
    public void update(Unit source) {
        rescued = MdtCombatUtil.nearestDamagedAlly(source, range, triggerHealth);

        if (rescued != null) {
            MdtCombatUtil.pull(rescued, source.x, source.y, pull * Time.delta);
            rescued.heal(rescued.maxHealth * healFractionPerTick * Time.delta);
        }
    }

    @Override
    public void draw(Unit source) {
        if (rescued == null) return;

        Draw.color(Color.valueOf("7391a8"));
        Draw.alpha(0.48f);
        Lines.stroke(1.6f);
        Lines.line(source.x, source.y, rescued.x, rescued.y);
        Draw.reset();
    }

    @Override public MechanicId mechanicId() { return MechanicId.RESCUE_TRACTOR; }
    @Override public SupportMode supportMode() { return SupportMode.LINK; }
    @Override public float supportRange(Unit source) { return range; }
    @Override public boolean canSupport(Unit source, Teamc target) {
        return target instanceof Unit u && u.team == source.team && u.healthf() < triggerHealth;
    }
    @Override public void applySupport(Unit source, Teamc target, float delta) {}
}

    // S08
    public static class VectorAssist extends Ability implements MdtSupportAbility {
    public float range = 125f;
    private Seq<Unit> active = new Seq<>();

    @Override
    public void update(Unit source) {
        active.clear();

        Units.nearby(source.team, source.x, source.y, range, ally -> {
            if (ally == source || ally.vel.isZero()) return;

            ally.apply(StatusEffects.fast, 3f);
            active.add(ally);
        });
    }

    @Override
    public void draw(Unit source) {
        if (active.isEmpty()) return;

        Draw.color(Color.valueOf("6f9fb4"));
        Draw.alpha(0.28f);
        Lines.stroke(1f);
        Lines.circle(source.x, source.y, range);

        // Only units actually receiving the buff get a tiny tail.
        for (Unit ally : active) {
            float angle = ally.vel.isZero() ? ally.rotation + 180f : ally.vel.angle() + 180f;

            for (int i = 0; i < 2; i++) {
                float dst = ally.hitSize * 0.35f + 5f + i * 5f;
                Draw.alpha(0.26f - i * 0.08f);
                Fill.square(
                    ally.x + Angles.trnsx(angle, dst),
                    ally.y + Angles.trnsy(angle, dst),
                    1.3f - i * 0.2f,
                    45f
                );
            }
        }

        Draw.reset();
    }

    @Override
    public Ability copy() {
        VectorAssist out = (VectorAssist)super.copy();
        out.active = new Seq<>();
        return out;
    }

    @Override public MechanicId mechanicId() { return MechanicId.VECTOR_ASSIST; }
    @Override public SupportMode supportMode() { return SupportMode.AREA; }
    @Override public float supportRange(Unit source) { return range; }
    @Override public boolean canSupport(Unit source, Teamc target) { return target instanceof Unit u && u.team == source.team; }
    @Override public void applySupport(Unit source, Teamc target, float delta) {}
}

    // S09
    public static class ElectronicSuppression extends Ability implements MdtSupportAbility {
    public float range = 155f;
    public float chainInterval = 42f;
    public float statusDuration = 55f;

    private float chainTimer;
    private Seq<Unit> active = new Seq<>();

    @Override
    public void update(Unit source) {
        active.clear();

        Units.nearbyEnemies(source.team, source.x, source.y, range, enemy -> {
            enemy.apply(StatusEffects.electrified, statusDuration);
            active.add(enemy);
        });

        if (active.isEmpty()) {
            chainTimer = Math.min(chainTimer, chainInterval);
            return;
        }

        chainTimer += Time.delta;
        if (chainTimer < chainInterval) return;
        chainTimer = 0f;

        float brightness = Mathf.clamp(0.85f / active.size, 0.14f, 0.52f);

        for (Unit enemy : active) {
            Tmp.c1.set(Color.valueOf("876fa6")).mul(0.55f + brightness * 0.55f).a(brightness);
            Fx.chainEmp.at(source.x, source.y, 0f, Tmp.c1, enemy);
        }
    }

    @Override
    public void draw(Unit source) {
        if (active.isEmpty()) return;

        Draw.color(Color.valueOf("806b91"));
        Draw.alpha(0.18f);
        Lines.stroke(1f);
        Lines.circle(source.x, source.y, range);
        Draw.reset();
    }

    @Override
    public Ability copy() {
        ElectronicSuppression out = (ElectronicSuppression)super.copy();
        out.active = new Seq<>();
        out.chainTimer = 0f;
        return out;
    }

    @Override public MechanicId mechanicId() { return MechanicId.ELECTRONIC_SUPPRESSION; }
    @Override public SupportMode supportMode() { return SupportMode.AREA; }
    @Override public float supportRange(Unit source) { return range; }
    @Override public boolean canSupport(Unit source, Teamc target) { return target instanceof Unit u && u.team != source.team; }
    @Override public void applySupport(Unit source, Teamc target, float delta) {}
}

    // S10
    public static class DecoyChaff extends Ability implements MdtSupportAbility {
    public UnitType decoyType;
    public float reload = 180f;
    public int count = 3;
    public float spawnRadius = 18f;
    public float detectRange = 95f;

    private float timer;

    public DecoyChaff(UnitType decoyType) {
        this.decoyType = decoyType;
    }

    @Override
    public void update(Unit source) {
        timer -= Time.delta;
        if (timer > 0f || decoyType == null) return;

        float sumX = 0f, sumY = 0f;
        int incoming = 0;

        for (Bullet bullet : Groups.bullet) {
            if (bullet.team == source.team || !bullet.within(source, detectRange)) continue;
            sumX += bullet.x;
            sumY += bullet.y;
            incoming++;
        }

        if (incoming <= 0) return;

        // Spawn toward the side where the barrage is actually coming from.
        float barrageX = sumX / incoming;
        float barrageY = sumY / incoming;
        float direction = source.angleTo(barrageX, barrageY);

        if (!Vars.net.client()) {
            for (int i = 0; i < count; i++) {
                Unit decoy = decoyType.create(source.team);
                float angle = direction + Mathf.range(32f);

                decoy.set(
                    source.x + Angles.trnsx(angle, spawnRadius + Mathf.random(0f, 8f)),
                    source.y + Angles.trnsy(angle, spawnRadius + Mathf.random(0f, 8f))
                );
                decoy.rotation = angle;
                decoy.add();
            }
        }

        MdtCombatFx.softPulse.at(
            source.x + Angles.trnsx(direction, spawnRadius),
            source.y + Angles.trnsy(direction, spawnRadius),
            0f,
            Color.valueOf("9ca3aa")
        );

        timer = reload;
    }

    @Override public MechanicId mechanicId() { return MechanicId.DECOY_CHAFF; }
    @Override public SupportMode supportMode() { return SupportMode.INTERCEPT; }
    @Override public float supportRange(Unit source) { return detectRange; }
    @Override public boolean canSupport(Unit source, Teamc target) { return false; }
    @Override public void applySupport(Unit source, Teamc target, float delta) {}
}

    // S11
    public static class DeflectionWedge extends Ability implements MdtSupportAbility {
        public float range = 82f;
        public float halfAngle = 55f;
        public float maxBulletDamage = 180f;

        @Override
        public void update(Unit source) {
            for (Bullet bullet : Groups.bullet) {
                if (bullet.team == source.team || bullet.damage > maxBulletDamage || !bullet.within(source, range)) continue;

                float angleToBullet = source.angleTo(bullet);
                if (!Angles.within(source.rotation, angleToBullet, halfAngle)) continue;

                float outward = source.angleTo(bullet);
                bullet.rotation(outward);
                bullet.vel.scl(0.85f);
                MdtCombatFx.spark.at(bullet.x, bullet.y, outward, Color.valueOf("bce8ff"));
            }
        }

        @Override
        public void draw(Unit source) {
            Draw.color(Color.valueOf("bce8ff"));
            Draw.alpha(0.28f);
            Lines.stroke(1.1f);
            Lines.arc(source.x, source.y, range, halfAngle * 2f / 360f, source.rotation - halfAngle);
            Draw.reset();
        }

        @Override public MechanicId mechanicId() { return MechanicId.DEFLECTION_WEDGE; }
        @Override public SupportMode supportMode() { return SupportMode.INTERCEPT; }
        @Override public float supportRange(Unit source) { return range; }
        @Override public boolean canSupport(Unit source, Teamc target) { return false; }
        @Override public void applySupport(Unit source, Teamc target, float delta) {}
    }

    // S12
    public static class DamageRedirect extends Ability implements MdtSupportAbility {
    public float range = 122f;
    public float fraction = 0.25f;
    public float maxPerSecond = 320f;

    private IntMap<Float> lastHealth = new IntMap<>();
    private float budget;
    private float flash;

    @Override
    public void update(Unit source) {
        flash = Math.max(0f, flash - Time.delta);
        budget = Math.min(maxPerSecond, budget + maxPerSecond / 60f * Time.delta);

        Units.nearby(source.team, source.x, source.y, range, ally -> {
            if (ally == source || ally.dead) return;

            Float previous = lastHealth.get(ally.id);

            if (previous != null) {
                float lost = previous - ally.health;

                if (lost > 0f && budget > 0f && !Vars.net.client()) {
                    float redirected = Math.min(lost * fraction, budget);

                    ally.heal(redirected);
                    source.damagePierce(redirected);
                    budget -= redirected;
                    flash = 18f;

                    Fx.chainLightning.at(ally.x, ally.y, 0f, Color.valueOf("708ca0"), source);
                }
            }

            lastHealth.put(ally.id, ally.health);
        });
    }

    @Override
    public void draw(Unit source) {
        if (flash <= 0f) return;

        Draw.color(Color.valueOf("708ca0"));
        Draw.alpha(0.25f * Mathf.clamp(flash / 18f));
        Lines.stroke(1.2f);
        Lines.circle(source.x, source.y, range);
        Draw.reset();
    }

    @Override
    public Ability copy() {
        DamageRedirect out = (DamageRedirect)super.copy();
        out.lastHealth = new IntMap<>();
        out.budget = 0f;
        out.flash = 0f;
        return out;
    }

    @Override public MechanicId mechanicId() { return MechanicId.DAMAGE_REDIRECT; }
    @Override public SupportMode supportMode() { return SupportMode.AREA; }
    @Override public float supportRange(Unit source) { return range; }
    @Override public boolean canSupport(Unit source, Teamc target) { return target instanceof Unit u && u.team == source.team; }
    @Override public void applySupport(Unit source, Teamc target, float delta) {}
}

    // S13
    public static class StabilizationField extends Ability implements MdtSupportAbility {
    public float range = 120f;
    private int affected;

    @Override
    public void update(Unit source) {
        affected = 0;

        Units.nearby(source.team, source.x, source.y, range, ally -> {
            if (ally == source) return;
            ally.apply(StatusEffects.overclock, 3f);
            affected++;
        });
    }

    @Override
    public void draw(Unit source) {
        if (affected <= 0) return;

        Draw.color(Color.valueOf("87959d"));
        Draw.alpha(0.20f);
        Lines.stroke(1f);
        Lines.circle(source.x, source.y, range);
        Draw.reset();
    }

    @Override public MechanicId mechanicId() { return MechanicId.STABILIZATION_FIELD; }
    @Override public SupportMode supportMode() { return SupportMode.AREA; }
    @Override public float supportRange(Unit source) { return range; }
    @Override public boolean canSupport(Unit source, Teamc target) { return target instanceof Unit u && u.team == source.team; }
    @Override public void applySupport(Unit source, Teamc target, float delta) {}
}

    // S14
    public static class PhaseCorridor extends Ability implements MdtSupportAbility {
    public float length = 190f;
    public float halfWidth = 30f;
    private Seq<Unit> active = new Seq<>();

    @Override
    public void update(Unit source) {
        active.clear();

        float cos = Mathf.cosDeg(source.rotation);
        float sin = Mathf.sinDeg(source.rotation);

        Units.nearby(source.team, source.x, source.y, length, ally -> {
            if (ally == source) return;

            float dx = ally.x - source.x;
            float dy = ally.y - source.y;
            float forward = dx * cos + dy * sin;
            float side = -dx * sin + dy * cos;

            if (forward >= 0f && forward <= length && Math.abs(side) <= halfWidth) {
                ally.apply(StatusEffects.fast, 3f);
                active.add(ally);
            }
        });
    }

    @Override
    public void draw(Unit source) {
        if (active.isEmpty()) return;

        Draw.color(Color.valueOf("8b89b6"));
        Draw.alpha(0.22f);
        Lines.stroke(1f);
        Lines.circle(source.x, source.y, length);

        for (Unit ally : active) {
            float angle = ally.vel.isZero() ? ally.rotation + 180f : ally.vel.angle() + 180f;

            for (int i = 0; i < 2; i++) {
                float dst = ally.hitSize * 0.35f + 5f + i * 5f;
                Draw.alpha(0.24f - i * 0.07f);
                Fill.square(
                    ally.x + Angles.trnsx(angle, dst),
                    ally.y + Angles.trnsy(angle, dst),
                    1.3f - i * 0.2f,
                    45f
                );
            }
        }

        Draw.reset();
    }

    @Override
    public Ability copy() {
        PhaseCorridor out = (PhaseCorridor)super.copy();
        out.active = new Seq<>();
        return out;
    }

    @Override public MechanicId mechanicId() { return MechanicId.PHASE_CORRIDOR; }
    @Override public SupportMode supportMode() { return SupportMode.AREA; }
    @Override public float supportRange(Unit source) { return length; }
    @Override public boolean canSupport(Unit source, Teamc target) { return target instanceof Unit u && u.team == source.team; }
    @Override public void applySupport(Unit source, Teamc target, float delta) {}
}

    // S15
    public static class TargetDesignation extends Ability implements MdtSupportAbility {
    public float range = 250f;
    public float markDuration = 60f;
    public float allyDamageBonus = 0.10f;

    private Teamc target;

    @Override
    public void update(Unit source) {
        target = Units.closestTarget(source.team, source.x, source.y, range);

        if (target != null) {
            MdtCombatModifiers.mark(target, allyDamageBonus, markDuration);
        }
    }

    @Override
    public void draw(Unit source) {
        if (target == null) return;

        Draw.color(Color.valueOf("ffd37f"));
        Draw.alpha(0.88f);
        Lines.stroke(1.8f);

        float r = 10f + Mathf.absin(Time.time, 5f, 3f);
        Lines.square(target.x(), target.y(), r, 45f);
        Lines.square(target.x(), target.y(), r + 8f, 0f);
        Lines.line(source.x, source.y, target.x(), target.y());

        Draw.reset();
    }

    @Override public MechanicId mechanicId() { return MechanicId.TARGET_DESIGNATION; }
    @Override public SupportMode supportMode() { return SupportMode.INFORMATION; }
    @Override public float supportRange(Unit source) { return range; }
    @Override public boolean canSupport(Unit source, Teamc target) { return target != null && target.team() != source.team; }
    @Override public void applySupport(Unit source, Teamc target, float delta) {}
}

    // S16
    public static class DroneMaintenance extends Ability implements MdtSupportAbility {
        public UnitType droneType;
        public float range = 95f;
        public float healPerTick = 5f;
        public float reloadService = 1.2f;

        private Unit linked;

        public DroneMaintenance(UnitType droneType) {
            this.droneType = droneType;
        }

        @Override
        public void update(Unit source) {
            linked = Units.closest(source.team, source.x, source.y, range,
                u -> u != source && u.type == droneType && u.healthf() < 0.98f);

            if (linked != null) {
                linked.heal(healPerTick * Time.delta);
                for (WeaponMount mount : linked.mounts) {
                    mount.reload = Math.max(0f, mount.reload - reloadService * Time.delta);
                }
            }
        }

        @Override
        public void draw(Unit source) {
            if (linked == null) return;
            Draw.color(Color.valueOf("8fe7c8"));
            Draw.alpha(0.60f);
            Lines.stroke(1f);
            Lines.line(source.x, source.y, linked.x, linked.y);
            Draw.reset();
        }

        @Override public MechanicId mechanicId() { return MechanicId.DRONE_MAINTENANCE; }
        @Override public SupportMode supportMode() { return SupportMode.TARGETED; }
        @Override public float supportRange(Unit source) { return range; }
        @Override public boolean canSupport(Unit source, Teamc target) { return target instanceof Unit u && u.team == source.team && u.type == droneType; }
        @Override public void applySupport(Unit source, Teamc target, float delta) {}
    }

    // S17
    public static class BuildAssist extends Ability implements MdtSupportAbility {
    public float range = 135f;
    public float reload = 105f;
    /** Additional fraction of one normal builder tick applied as a pulse. */
    public float bonusBuildFraction = 0.55f;
    public float buffDuration = 90f;

    private float timer;

    @Override
    public void update(Unit source) {
        timer += Time.delta;
        if (timer < reload) return;
        timer = 0f;

        final int[] affected = {0};

        Units.nearby(source.team, source.x, source.y, range, ally -> {
            if (ally == source || !ally.activelyBuilding()) return;

            BuildPlan plan = ally.buildPlan();
            if (plan == null || plan.breaking) return;

            var tile = plan.tile();
            if (tile == null || !(tile.build instanceof ConstructBuild construct)) return;
            if (ally.team.core() == null && !Vars.state.rules.infiniteResources) return;

            // Pulse actual construction progress without inventing a duplicate build queue.
            float base = 1f / Math.max(0.0001f, construct.buildCost)
                * ally.type.buildSpeed
                * Vars.state.rules.buildSpeed(ally.team);

            if (!Vars.net.client()) {
                construct.construct(
                    ally,
                    ally.team.core(),
                    base * bonusBuildFraction,
                    plan.config
                );
            }

            // Re-use the vanilla overclock icon/tint as the short visible buff layer.
            ally.apply(StatusEffects.overclock, buffDuration);
            Fx.chainLightning.at(source.x, source.y, 0f, Pal.accent, ally);
            affected[0]++;
        });

        if (affected[0] > 0) {
            Fx.overdriveWave.at(source.x, source.y, range, Pal.accent);
            MdtCombatFx.supportPulse.at(source.x, source.y, range, Pal.accent);
        }
    }

    @Override public MechanicId mechanicId() { return MechanicId.BUILD_ASSIST; }
    @Override public SupportMode supportMode() { return SupportMode.BUILD_ASSIST; }
    @Override public float supportRange(Unit source) { return range; }
    @Override public boolean canSupport(Unit source, Teamc target) {
        return target instanceof Unit u && u.team == source.team && u.activelyBuilding();
    }
    @Override public void applySupport(Unit source, Teamc target, float delta) {}
}


    // S18 removed in Phase 2: overlaps vanilla logistics-unit gameplay.


    // S19
    public static class FormationCoordination extends Ability implements MdtSupportAbility, LinkStateProvider {
        public float range = 105f;
        public int minimum = 3;

        private Seq<Teamc> links = new Seq<>();

        @Override
        public void update(Unit source) {
            links.clear();
            Units.nearby(source.team, source.x, source.y, range, ally -> {
                if (ally != source && !ally.dead) links.add(ally);
            });

            if (links.size + 1 >= minimum) {
                source.apply(MdtCombatStatuses.formation, 3f);
                for (Teamc other : links) {
                    if (other instanceof Unit u) u.apply(MdtCombatStatuses.formation, 3f);
                }
            }
        }

        @Override public int linkCount(Unit unit) { return links.size; }
        @Override public void eachLink(Unit unit, Cons<Teamc> consumer) { links.each(consumer); }

        @Override
        public Ability copy() {
            FormationCoordination out = (FormationCoordination)super.copy();
            out.links = new Seq<>();
            return out;
        }

        @Override public MechanicId mechanicId() { return MechanicId.FORMATION_COORDINATION; }
        @Override public SupportMode supportMode() { return SupportMode.LINK; }
        @Override public float supportRange(Unit source) { return range; }
        @Override public boolean canSupport(Unit source, Teamc target) { return target instanceof Unit u && u.team == source.team; }
        @Override public void applySupport(Unit source, Teamc target, float delta) {}
    }

    // S20
    public static class ThreatWarning extends Ability implements MdtSupportAbility {
        public float range = 150f;
        public float predictionTicks = 24f;

        @Override
        public void draw(Unit source) {
            Draw.color(Color.valueOf("ff7c7c"));
            Draw.alpha(0.48f);
            Lines.stroke(0.9f);

            for (Bullet bullet : Groups.bullet) {
                if (bullet.team == source.team || !bullet.within(source, range)) continue;
                float px = bullet.x + bullet.vel.x * predictionTicks;
                float py = bullet.y + bullet.vel.y * predictionTicks;
                Lines.line(bullet.x, bullet.y, px, py);
                Lines.circle(px, py, 3.5f);
            }

            Draw.reset();
        }

        @Override public MechanicId mechanicId() { return MechanicId.THREAT_WARNING; }
        @Override public SupportMode supportMode() { return SupportMode.INFORMATION; }
        @Override public float supportRange(Unit source) { return range; }
        @Override public boolean canSupport(Unit source, Teamc target) { return false; }
        @Override public void applySupport(Unit source, Teamc target, float delta) {}
    }

    private SupportAbilities() {}
}
