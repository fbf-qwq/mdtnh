package mdtnh.combat.impl.projectile;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Bullet;
import mindustry.graphics.Drawf;

/**
 * Compact isosceles-triangle projectile used for split fragments.
 * No parent-child guide line is drawn.
 */
public class TriangleBulletType extends BasicBulletType {
    public float triangleWidth = 5f;
    public float triangleLength = 11f;
    public float tailLength = 5f;
    public Color triangleColor = Color.white;
    public Color tailColor = Color.lightGray;

    public TriangleBulletType(float speed, float damage, Color color) {
        super(speed, damage);
        triangleColor = color;
        frontColor = color;
        backColor = color;
        width = height = 0f;
        trailLength = 0;
    }

    @Override
    public void draw(Bullet b) {
        float angle = b.rotation();

        Draw.color(tailColor);
        Draw.alpha(0.28f);
        Drawf.tri(
            b.x + Angles.trnsx(angle + 180f, tailLength * 0.45f),
            b.y + Angles.trnsy(angle + 180f, tailLength * 0.45f),
            triangleWidth * 0.56f,
            tailLength,
            angle + 180f
        );

        Draw.color(triangleColor);
        Draw.alpha(0.95f);
        Drawf.tri(b.x, b.y, triangleWidth, triangleLength, angle);

        Draw.reset();
    }
}
