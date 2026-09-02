package mdtnh.combat.api.projectile;

/**
 * Per-bullet behavior storage.
 * One slot is reserved per behavior instance.
 *
 * payload preserves whatever data was originally supplied to BulletType.create(..., data).
 */
public final class MdtBulletRuntime {
    public final Object payload;
    public final Object[] states;

    public MdtBulletRuntime(Object payload, int behaviorCount) {
        this.payload = payload;
        this.states = new Object[behaviorCount];
    }

    @SuppressWarnings("unchecked")
    public <T> T state(int index) {
        return (T) states[index];
    }

    public void state(int index, Object value) {
        states[index] = value;
    }
}
