package mdtnh.combat.api.visual;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.type.UnitType;

/**
 * Generic visual adapter. It does not own gameplay state.
 * Instead it reads state from sibling abilities implementing the provider interfaces.
 *
 * Sprite convention:
 *   <unit-id>-deployed.png  : complete deployed body
 *   <unit-id>-heat.png      : transparent heat-emissive mask
 *
 * If a region is missing, that visual is simply skipped.
 */
public class StateVisualAbility extends Ability implements MdtUnitVisualProvider {
    public String deployedSuffix = "-deployed";
    public String heatSuffix = "-heat";

    public float deploySwapThreshold = 0.92f;
    public float heatMaskMin = 0.08f;
    public float phaseGhostDistance = 6f;

    public Color heatCold = Color.valueOf("ff7a45");
    public Color heatHot = Color.valueOf("fff1d0");
    public Color phaseColor = Color.valueOf("aabfff");
    public Color chargeColor = Color.valueOf("8fe9ff");

    private TextureRegion deployedRegion;
    private TextureRegion heatMask;
    private UnitType loadedType;

    public StateVisualAbility() {
        display = false;
    }

    @Override
    public TextureRegion bodyRegion(Unit unit) {
        ensureLoaded(unit);

        float deploy = readDeploy(unit);
        if (deploy >= deploySwapThreshold && deployedRegion != null && deployedRegion.found()) {
            return deployedRegion;
        }

        return null;
    }

    @Override
    public float bodyPriority(Unit unit) {
        return 100f;
    }

    @Override
    public void draw(Unit unit) {
        ensureLoaded(unit);

        float heat = Mathf.clamp(readHeat(unit));
        if (heat > heatMaskMin && heatMask != null && heatMask.found()) {
            Color c = Tmp.c1.set(heatCold).lerp(heatHot, heat);
            Draw.color(c);
            Draw.alpha(Mathf.clamp((heat - heatMaskMin) / (1f - heatMaskMin)));
            Draw.rect(heatMask, unit.x, unit.y, unit.rotation - 90f);
            Draw.reset();
        }

        float charge = Mathf.clamp(readCharge(unit));
        if (charge > 0.02f) {
            Draw.color(chargeColor);

            int particles = 2 + (int)(charge * 8f);
            float baseRadius = unit.hitSize() * 0.30f;
            float travel = unit.hitSize() * (0.65f + charge * 0.55f);

            for (int i = 0; i < particles; i++) {
                float phase = (Time.time * (0.55f + charge * 0.55f) + i * 17f) % 30f / 30f;
                float angle = i * (360f / particles) + Time.time * 0.22f;
                float radius = baseRadius + travel * phase;

                Draw.alpha((0.10f + 0.38f * charge) * (1f - phase));
                Fill.square(
                    unit.x + Angles.trnsx(angle, radius),
                    unit.y + Angles.trnsy(angle, radius),
                    0.8f + 1.1f * charge,
                    45f + angle
                );
            }

            Draw.reset();
        }

        float phase = Mathf.clamp(readPhase(unit));
        if (phase > 0.02f) {
            float pulse = 0.5f + Mathf.absin(Time.time, 4f, 0.5f);
            float dx = Angles.trnsx(unit.rotation + 180f, phaseGhostDistance * pulse);
            float dy = Angles.trnsy(unit.rotation + 180f, phaseGhostDistance * pulse);

            Draw.color(phaseColor);
            Draw.alpha(0.10f + 0.18f * phase);
            Draw.rect(unit.type.region, unit.x + dx, unit.y + dy, unit.rotation - 90f);
            Draw.rect(unit.type.region, unit.x - dx * 0.55f, unit.y - dy * 0.55f, unit.rotation - 90f);
            Draw.reset();
        }
    }

    private void ensureLoaded(Unit unit) {
        if (loadedType == unit.type) return;

        loadedType = unit.type;
        deployedRegion = Core.atlas.find(unit.type.name + deployedSuffix);
        heatMask = Core.atlas.find(unit.type.name + heatSuffix);
    }

    private float readDeploy(Unit unit) {
        float out = 0f;
        for (Ability ability : unit.abilities) {
            if (ability instanceof DeployStateProvider provider) {
                out = Math.max(out, provider.deployProgress(unit));
            }
        }
        return out;
    }

    private float readHeat(Unit unit) {
        float out = 0f;
        for (Ability ability : unit.abilities) {
            if (ability instanceof HeatStateProvider provider) {
                out = Math.max(out, provider.heatProgress(unit));
            }
        }
        return out;
    }

    private float readCharge(Unit unit) {
        float out = 0f;
        for (Ability ability : unit.abilities) {
            if (ability instanceof ChargeStateProvider provider) {
                out = Math.max(out, provider.chargeProgress(unit));
            }
        }
        return out;
    }

    private float readPhase(Unit unit) {
        float out = 0f;
        for (Ability ability : unit.abilities) {
            if (ability instanceof PhaseStateProvider provider) {
                out = Math.max(out, provider.phaseStrength(unit));
            }
        }
        return out;
    }
}
