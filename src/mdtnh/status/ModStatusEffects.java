package mdtnh.status;

import mindustry.type.StatusEffect;

/**
 * Hidden gameplay-only statuses used by MDTNH custom abilities.
 *
 * These are deliberately hidden from the content database. Player-facing text belongs
 * to the Ability bundle keys, not to hard-coded Java strings.
 */
public final class ModStatusEffects {

    public static StatusEffect
            deployed,
            heated,
            overheated,
            marked,
            gravitySlow,
            formation2,
            formation3,
            formation4,
            lock1,
            lock2,
            execution,
            tacticalLinked,
            braced,
            overdriveAura,
            suppressed,
            momentum,
            lastStand;

    public static void load() {
        deployed = hidden("mdt-deployed");
        deployed.speedMultiplier = 0.35f;
        deployed.reloadMultiplier = 1.25f;
        deployed.damageMultiplier = 1.10f;

        heated = hidden("mdt-heated");
        heated.reloadMultiplier = 1.15f;
        heated.damageMultiplier = 1.08f;

        overheated = hidden("mdt-overheated");
        overheated.disarm = true;
        overheated.speedMultiplier = 0.85f;

        marked = hidden("mdt-marked");

        gravitySlow = hidden("mdt-gravity-slow");
        gravitySlow.speedMultiplier = 0.65f;

        formation2 = hidden("mdt-formation-2");
        formation2.reloadMultiplier = 1.05f;

        formation3 = hidden("mdt-formation-3");
        formation3.reloadMultiplier = 1.05f;
        formation3.speedMultiplier = 1.05f;

        formation4 = hidden("mdt-formation-4");
        formation4.reloadMultiplier = 1.05f;
        formation4.speedMultiplier = 1.05f;
        formation4.damageMultiplier = 1.08f;

        lock1 = hidden("mdt-lock-1");
        lock1.damageMultiplier = 1.05f;

        lock2 = hidden("mdt-lock-2");
        lock2.damageMultiplier = 1.12f;
        lock2.reloadMultiplier = 1.06f;

        execution = hidden("mdt-execution");
        execution.damageMultiplier = 1.25f;

        tacticalLinked = hidden("mdt-tactical-linked");

        braced = hidden("mdt-braced");
        braced.speedMultiplier = 0.72f;
        braced.reloadMultiplier = 1.18f;
        braced.damageMultiplier = 1.08f;

        overdriveAura = hidden("mdt-overdrive-aura");
        overdriveAura.speedMultiplier = 1.07f;
        overdriveAura.reloadMultiplier = 1.12f;

        suppressed = hidden("mdt-suppressed");
        suppressed.speedMultiplier = 0.82f;
        suppressed.reloadMultiplier = 0.82f;
        suppressed.damageMultiplier = 0.90f;

        momentum = hidden("mdt-momentum");
        momentum.speedMultiplier = 1.05f;
        momentum.damageMultiplier = 1.07f;

        lastStand = hidden("mdt-last-stand");
        lastStand.speedMultiplier = 0.94f;
        lastStand.reloadMultiplier = 1.18f;
        lastStand.damageMultiplier = 1.14f;
    }

    private static StatusEffect hidden(String name) {
        StatusEffect effect = new StatusEffect(name);
        effect.show = false;
        effect.outline = false;
        return effect;
    }

    private ModStatusEffects() {
    }
}
