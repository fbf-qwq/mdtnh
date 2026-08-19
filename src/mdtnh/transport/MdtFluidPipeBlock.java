package mdtnh.transport;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.type.Liquid;
import mindustry.world.blocks.liquid.LiquidBlock;
import mindustry.world.meta.BlockGroup;

/**
 * MDT directional GT-style fluid pipe with 4-layer connected rendering.
 */
public class MdtFluidPipeBlock extends LiquidBlock {

    /** MDT 流体管道相对 GTNH 表值的实际速度倍率。 */
    public static final float transportSpeedMultiplier = 3f;

    /** 应用倍率后的实际 L/s。 */
    public final float litersPerSecond;
    public final float mdtUnitsPerSecond;
    public final int maxTemperatureK;
    public final int maxConcurrentLiquids;
    public final float perLiquidBufferCapacity;

    public String fallbackRegion = "conduit";

    public String centerBottomRegionName;
    public String edgeBottomRegionName;
    public String centerTopRegionName;
    public String edgeTopRegionName;

    public TextureRegion centerBottomRegion;
    public TextureRegion edgeBottomRegion;
    public TextureRegion centerTopRegion;
    public TextureRegion edgeTopRegion;

    public float centerBottomRotationOffsetDeg = 0f;
    public float edgeBottomRotationOffsetDeg = 0f;
    public float centerTopRotationOffsetDeg = 0f;
    public float edgeTopRotationOffsetDeg = 0f;

    /**
     * 流体在管道内部的可见宽度（世界单位）。
     * 1 格 = Vars.tilesize，默认约为半格宽；具体管径由 MdtTransportBlocks 调整。
     */
    public float liquidInnerWidth = Vars.tilesize * 0.50f;

    public MdtFluidPipeBlock(
            String name,
            float litersPerSecond,
            int maxTemperatureK,
            int maxConcurrentLiquids
    ) {
        super(name);

        this.litersPerSecond =
                Math.max(0f, litersPerSecond) * transportSpeedMultiplier;
        this.mdtUnitsPerSecond =
                this.litersPerSecond / GtTransportData.LITERS_PER_MDT_FLUID_UNIT;
        this.maxTemperatureK = maxTemperatureK;
        this.maxConcurrentLiquids = Math.max(1, maxConcurrentLiquids);
        this.perLiquidBufferCapacity = Math.max(1f, this.mdtUnitsPerSecond * 2f);

        update = true;
        rotate = true;
        solid = false;
        underBullets = true;
        hasLiquids = true;
        outputsLiquid = true;
        liquidCapacity = perLiquidBufferCapacity * this.maxConcurrentLiquids;
        group = BlockGroup.liquids;

        buildType = MdtFluidPipeBuild::new;
    }

    @Override
    public void load() {
        super.load();

        TextureRegion fallback = Core.atlas.find(fallbackRegion);

        centerBottomRegion = find(name, centerBottomRegionName, "center-bottom");
        edgeBottomRegion = find(name, edgeBottomRegionName, "edge-bottom");
        centerTopRegion = find(name, centerTopRegionName, "center-top");
        edgeTopRegion = find(name, edgeTopRegionName, "edge-top");

        if (!centerBottomRegion.found()) centerBottomRegion = fallback;
        if (!centerTopRegion.found()) centerTopRegion = centerBottomRegion;

        region = centerTopRegion.found() ? centerTopRegion : centerBottomRegion;
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{region};
    }

    private static TextureRegion find(String contentName, String explicitName, String suffix) {
        return Core.atlas.find(
                explicitName == null ? contentName + "-" + suffix : explicitName
        );
    }

    public class MdtFluidPipeBuild extends LiquidBuild {

        private float[] transferCredits = new float[0];

        /**
         * 每 tick 的输入配额。
         *
         * Mindustry 的标准 transferLiquid() 会先调用 acceptLiquid()/handleLiquid()，
         * 再从来源一次性扣除 flow，因此必须在 handleLiquid() 内截断并把超额返还来源，
         * 才能真正限制“净输入速度”。
         */
        private float[] inputUsedThisTick = new float[0];
        private long inputBudgetTick = Long.MIN_VALUE;

        @Override
        public void draw() {
            // 底层管壁。
            Draw.z(Layer.blockUnder + 0.10f);
            drawEdges(edgeBottomRegion, edgeBottomRotationOffsetDeg);

            Draw.z(Layer.blockUnder + 0.14f);
            Draw.rect(centerBottomRegion, x, y, rotdeg() + centerBottomRotationOffsetDeg);

            // 内容流体：与原版 Conduit 一样位于 bottom 与 top 之间。
            Draw.z(Layer.blockUnder + 0.18f);
            drawLiquidContents();

            // 上层管壁。
            Draw.z(Layer.blockUnder + 0.22f);
            drawEdges(edgeTopRegion, edgeTopRotationOffsetDeg);

            Draw.z(Layer.blockUnder + 0.26f);
            Draw.rect(centerTopRegion, x, y, rotdeg() + centerTopRotationOffsetDeg);

            Draw.reset();
        }

        private void drawLiquidContents() {
            if (liquids == null || liquids.currentAmount() <= 0.000001f) return;

            Liquid liquid = liquids.current();
            if (liquid == null) return;

            TextureRegion fluidFrame =
                    Vars.renderer.fluidFrames[liquid.gas ? 1 : 0][liquid.getAnimationFrame()];

            // 四联/九联管按单通道容量计算显示强度。
            float fullness = Mathf.clamp(
                    liquids.get(liquid) / Math.max(0.000001f, perLiquidBufferCapacity)
            );
            if (fullness <= 1f / 255f) return;

            /*
             * 不直接 Drawf.liquid(fluidFrame, x, y, ...)，因为 renderer.fluidFrames
             * 是整格大小；那样会把液体铺满一个 tile。
             *
             * 这里用同一动态 fluidFrame 画“中心 + 已连接方向的液体臂”，
             * 把可见区域限制在 liquidInnerWidth 内。
             */
            float width = Mathf.clamp(
                    liquidInnerWidth,
                    0.5f,
                    Vars.tilesize - 0.5f
            );

            Draw.color(liquid.color, fullness * liquid.color.a);

            // 中心液体。
            Draw.rect(fluidFrame, x, y, width, width);

            if (tile != null) {
                float halfTile = Vars.tilesize / 2f;
                float halfWidth = width / 2f;

                // 从中心块边缘一直延伸到 tile 边缘，并略微重叠以消除接缝。
                float armLength = Math.max(
                        0.5f,
                        halfTile - halfWidth + 0.35f
                );
                float armOffset = halfWidth + armLength / 2f - 0.15f;

                for (int direction = 0; direction < 4; direction++) {
                    Building nearby = tile.nearbyBuild(direction);
                    if (!visuallyConnectsTo(nearby)) continue;

                    float ax = x + Geometry.d4x(direction) * armOffset;
                    float ay = y + Geometry.d4y(direction) * armOffset;

                    Draw.rect(
                            fluidFrame,
                            ax,
                            ay,
                            armLength,
                            width,
                            direction * 90f
                    );
                }
            }

            Draw.color();
        }

        private void drawEdges(TextureRegion region, float rotationOffset) {
            if (region == null || !region.found() || tile == null) return;

            for (int direction = 0; direction < 4; direction++) {
                Building nearby = tile.nearbyBuild(direction);
                if (visuallyConnectsTo(nearby)) {
                    Draw.rect(region, x, y, direction * 90f + rotationOffset);
                }
            }
        }

        private boolean visuallyConnectsTo(Building nearby) {
            if (nearby == null
                    || nearby.dead()
                    || nearby.team != team
                    || !nearby.block.hasLiquids) {
                return false;
            }

            // 与原版 Conduit 的朝向融合规则保持一致：
            // 1) 本管道正面确实输出到该建筑；或
            // 2) 邻居能够把流体输出到本管道。
            //
            // 因此两条同向、并排的管道不会横向连接。
            boolean thisOutputsToNeighbor = front() == nearby;

            boolean neighborOutputsToThis =
                    nearby.block.outputsLiquid
                            && (!nearby.block.rotate || nearby.front() == this);

            return thisOutputsToNeighbor || neighborOutputsToThis;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            if (source != null && source.team != team) return false;
            if (liquid == null) return false;

            Building output = front();
            if (source != null && source == output) return false;

            float stored = liquids.get(liquid);
            if (stored >= perLiquidBufferCapacity - 0.000001f) return false;

            if (stored <= 0.000001f
                    && activeLiquidCount() >= maxConcurrentLiquids) {
                return false;
            }

            // 输入速度不能超过该管道的实际运输速度。
            return remainingInputAllowance(liquid) > 0.000001f;
        }

        @Override
        public void handleLiquid(Building source, Liquid liquid, float amount) {
            if (liquid == null || amount <= 0f) return;

            ensureInputBudgetWindow();

            int id = liquid.id;
            float stored = liquids.get(liquid);
            float room = Math.max(0f, perLiquidBufferCapacity - stored);
            float allowance = Math.max(
                    0f,
                    inputAllowanceThisTick() - inputUsedThisTick[id]
            );

            float accepted = Math.min(amount, Math.min(room, allowance));
            if (accepted > 0.000001f) {
                liquids.add(liquid, accepted);
                inputUsedThisTick[id] += accepted;
                noSleep();
            }

            /*
             * Vanilla transferLiquid()/moveLiquid() 会在 handleLiquid() 返回后
             * 从 source 扣除完整 flow。先把未接收部分返还给 source，
             * 随后的完整扣除就会得到正确的“净扣除 = accepted”。
             */
            float rejected = amount - accepted;
            if (rejected > 0.000001f
                    && source != null
                    && source != this
                    && source.liquids != null) {
                source.liquids.add(liquid, rejected);
            }
        }

        private float remainingInputAllowance(Liquid liquid) {
            ensureInputBudgetWindow();
            return Math.max(
                    0f,
                    inputAllowanceThisTick() - inputUsedThisTick[liquid.id]
            );
        }

        private float inputAllowanceThisTick() {
            return mdtUnitsPerSecond * Math.max(0f, delta()) / 60f;
        }

        private void ensureInputBudgetWindow() {
            ensureInputCreditArray();

            long currentTick = (long) Vars.state.tick;
            if (inputBudgetTick == currentTick) return;

            inputBudgetTick = currentTick;
            for (int i = 0; i < inputUsedThisTick.length; i++) {
                inputUsedThisTick[i] = 0f;
            }
        }

        @Override
        public void updateTile() {
            if (liquids == null || !hasAnyLiquid() || mdtUnitsPerSecond <= 0f) return;

            Building target = front();
            if (target == null || target.team != team || target.liquids == null) return;

            ensureCreditArray();

            for (Liquid liquid : Vars.content.liquids()) {
                float stored = liquids.get(liquid);
                if (stored <= 0.000001f) continue;

                int id = liquid.id;
                transferCredits[id] += mdtUnitsPerSecond * delta() / 60f;
                transferCredits[id] = Math.min(
                        transferCredits[id],
                        Math.max(perLiquidBufferCapacity, mdtUnitsPerSecond)
                );

                if (transferCredits[id] <= 0.000001f) continue;
                if (!target.acceptLiquid(this, liquid)) continue;

                float targetRoom = Math.max(
                        0f,
                        target.block.liquidCapacity - target.liquids.get(liquid)
                );
                float moved = Math.min(
                        stored,
                        Math.min(transferCredits[id], targetRoom)
                );

                if (moved <= 0.000001f) continue;

                target.handleLiquid(this, liquid, moved);
                liquids.remove(liquid, moved);
                transferCredits[id] -= moved;
            }
        }

        private boolean hasAnyLiquid() {
            for (Liquid liquid : Vars.content.liquids()) {
                if (liquids.get(liquid) > 0.000001f) return true;
            }
            return false;
        }

        private int activeLiquidCount() {
            int count = 0;
            for (Liquid liquid : Vars.content.liquids()) {
                if (liquids.get(liquid) > 0.000001f) {
                    count++;
                    if (count >= maxConcurrentLiquids) break;
                }
            }
            return count;
        }

        private void ensureCreditArray() {
            int needed = Vars.content.liquids().size;
            if (transferCredits.length >= needed) return;

            float[] next = new float[needed];
            System.arraycopy(transferCredits, 0, next, 0, transferCredits.length);
            transferCredits = next;
        }

        private void ensureInputCreditArray() {
            int needed = Vars.content.liquids().size;
            if (inputUsedThisTick.length >= needed) return;

            float[] next = new float[needed];
            System.arraycopy(
                    inputUsedThisTick,
                    0,
                    next,
                    0,
                    inputUsedThisTick.length
            );
            inputUsedThisTick = next;
        }
    }
}
