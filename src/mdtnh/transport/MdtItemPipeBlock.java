package mdtnh.transport;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Teamc;
import mindustry.graphics.Layer;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;

import static mindustry.Vars.itemSize;
import static mindustry.Vars.tilesize;

/**
 * MDT directional item pipe with visible moving items and 4-layer pipe sprites.
 *
 * <p>The pipe art is split into 4 optional atlas regions:</p>
 * <ul>
 *     <li>center-bottom</li>
 *     <li>edge-bottom</li>
 *     <li>center-top</li>
 *     <li>edge-top</li>
 * </ul>
 *
 * <p>Rendering order:</p>
 * <ol>
 *     <li>edge-bottom connections</li>
 *     <li>center-bottom body</li>
 *     <li>moving items inside the pipe</li>
 *     <li>edge-top connections</li>
 *     <li>center-top body</li>
 * </ol>
 */
public class MdtItemPipeBlock extends Block {

    public final float throughputItemsPerSecond;
    public final boolean packageMode;
    public final int packageSize;

    public String fallbackRegion;

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

    /** Dynamic item size control relative to Vars.itemSize. */
    public float movingItemScale = 0.70f;
    public float packageItemScale = 0.85f;
    public float maxRenderedItemSize = -1f;
    public float maxRenderedPackageSize = -1f;

    public float normalVisualSpacing = 0.22f;
    public float packageVisualSpacing = 0.45f;
    public float minVisualTravelTicks = 6f;
    public float maxVisualTravelTicks = 120f;

    public MdtItemPipeBlock(String name, float throughputItemsPerSecond) {
        super(name);

        this.throughputItemsPerSecond = Math.max(0f, throughputItemsPerSecond);
        this.packageMode = this.throughputItemsPerSecond > 32f;
        this.packageSize = packageMode
                ? Math.max(1, Math.round(this.throughputItemsPerSecond / 4f))
                : 1;
        this.fallbackRegion = packageMode ? "plastanium-conveyor" : "titanium-conveyor";

        update = true;
        rotate = true;
        solid = false;
        underBullets = true;
        hasItems = true;
        itemCapacity = packageMode ? Math.max(16, packageSize * 2) : 16;
        unloadable = false;
        conveyorPlacement = true;
        group = BlockGroup.transportation;

        buildType = MdtItemPipeBuild::new;
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

    public static final class TransitItem {
        public final Item item;
        public int amount;
        public int entryDirection;
        public float progress;

        public TransitItem(Item item, int amount, int entryDirection, float progress) {
            this.item = item;
            this.amount = amount;
            this.entryDirection = entryDirection;
            this.progress = progress;
        }
    }

    public class MdtItemPipeBuild extends Building {

        public float transferCredit;
        /** 输入端令牌桶，保证进入管道的平均速度不超过额定运输速度。 */
        public float inputCredit;
        public float packageTimer;
        public final Seq<TransitItem> transitItems = new Seq<>();

        @Override
        public void draw() {
            Draw.z(Layer.blockUnder + 0.10f);
            drawEdges(edgeBottomRegion, edgeBottomRotationOffsetDeg);

            Draw.z(Layer.blockUnder + 0.14f);
            Draw.rect(centerBottomRegion, x, y, rotdeg() + centerBottomRotationOffsetDeg);

            drawTransitItems(Layer.blockUnder + 0.18f);

            Draw.z(Layer.blockUnder + 0.22f);
            drawEdges(edgeTopRegion, edgeTopRotationOffsetDeg);

            Draw.z(Layer.blockUnder + 0.26f);
            Draw.rect(centerTopRegion, x, y, rotdeg() + centerTopRotationOffsetDeg);

            Draw.reset();
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

        private void drawTransitItems(float z) {
            if (transitItems.isEmpty()) return;

            Draw.z(z);

            for (TransitItem packet : transitItems) {
                if (packet.progress < 0f) continue;

                float t = Mathf.clamp(packet.progress);

                Tmp.v1.set(
                        Geometry.d4x(packet.entryDirection) * tilesize / 2f,
                        Geometry.d4y(packet.entryDirection) * tilesize / 2f
                ).lerp(
                        Geometry.d4x(rotation) * tilesize / 2f,
                        Geometry.d4y(rotation) * tilesize / 2f,
                        t
                );

                float drawSize = renderedItemSize(packet.amount > 1);
                Draw.rect(
                        packet.item.fullIcon,
                        x + Tmp.v1.x,
                        y + Tmp.v1.y,
                        drawSize,
                        drawSize
                );
            }
        }

        private float renderedItemSize(boolean packaged) {
            float base = itemSize * (packaged ? packageItemScale : movingItemScale);
            float hardCap = packaged ? maxRenderedPackageSize : maxRenderedItemSize;
            return hardCap > 0f ? Math.min(base, hardCap) : base;
        }

        private boolean visuallyConnectsTo(Building nearby) {
            if (nearby == null
                    || nearby.dead()
                    || nearby.team != team
                    || !nearby.block.hasItems) {
                return false;
            }

            // 与原版 Conveyor 的朝向融合逻辑一致：
            // 本管道正面输出到邻居，或者邻居能够输出到本管道。
            // 两条同向并排的管道因此不会横向连接。
            boolean thisOutputsToNeighbor = front() == nearby;

            boolean neighborOutputsToThis =
                    nearby.block.outputsItems()
                            && (!nearby.block.rotate || nearby.front() == this);

            return thisOutputsToNeighbor || neighborOutputsToThis;
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            if (source != null && source.team != team) return false;
            if (items.total() >= itemCapacity) return false;

            Building output = front();
            if (source != null && source == output) return false;

            return inputCredit >= 1f;
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source) {
            if (amount <= 0) return 0;
            if (source != null && source.team() != team) return 0;

            int room = Math.max(0, itemCapacity - items.total());
            int rateAllowance = Math.max(0, (int) inputCredit);
            return Math.min(amount, Math.min(room, rateAllowance));
        }

        @Override
        public void handleItem(Building source, Item item) {
            if (inputCredit < 1f || items.total() >= itemCapacity) return;

            inputCredit -= 1f;
            items.add(item, 1);
            enqueueTransit(item, 1, source);
            noSleep();
        }

        @Override
        public void handleStack(Item item, int amount, Teamc source) {
            if (amount <= 0) return;

            int accepted = Math.min(
                    amount,
                    Math.min(
                            Math.max(0, itemCapacity - items.total()),
                            Math.max(0, (int) inputCredit)
                    )
            );
            if (accepted <= 0) return;

            inputCredit -= accepted;
            items.add(item, accepted);
            Building sourceBuild = source instanceof Building ? (Building) source : null;
            enqueueTransit(item, accepted, sourceBuild);
            noSleep();
        }

        @Override
        public int removeStack(Item item, int amount) {
            int removed = super.removeStack(item, amount);
            trimTransitFromBack(item, removed);
            return removed;
        }

        @Override
        public void updateTile() {
            // 输入和输出使用相同的额定 items/s。
            inputCredit += throughputItemsPerSecond * delta() / 60f;
            inputCredit = Math.min(
                    inputCredit,
                    Math.max(1f, throughputItemsPerSecond)
            );

            ensureTransitMirror();
            advanceTransitAnimation();

            if (!items.any() || transitItems.isEmpty() || throughputItemsPerSecond <= 0f) {
                return;
            }

            Building target = front();
            if (target == null || target.team != team) return;

            if (packageMode) {
                updatePackageMode(target);
            } else {
                updateNormalMode(target);
            }
        }

        private float visualTravelTicks() {
            if (packageMode) return 15f;
            if (throughputItemsPerSecond <= 0.000001f) return maxVisualTravelTicks;

            return Mathf.clamp(
                    60f / throughputItemsPerSecond,
                    minVisualTravelTicks,
                    maxVisualTravelTicks
            );
        }

        private void advanceTransitAnimation() {
            if (transitItems.isEmpty()) return;

            float advance = delta() / Math.max(0.0001f, visualTravelTicks());
            float spacing = packageMode ? packageVisualSpacing : normalVisualSpacing;
            float maximum = 1f;

            for (int i = 0; i < transitItems.size; i++) {
                TransitItem packet = transitItems.get(i);
                packet.progress = Math.min(packet.progress + advance, maximum);
                maximum = packet.progress - spacing;
            }
        }

        private void updateNormalMode(Building target) {
            transferCredit += throughputItemsPerSecond * delta() / 60f;
            transferCredit = Math.min(transferCredit, Math.max(1f, throughputItemsPerSecond));

            while (transferCredit >= 1f && !transitItems.isEmpty()) {
                TransitItem packet = transitItems.first();
                if (packet.progress < 1f) break;
                if (!target.acceptItem(this, packet.item)) break;

                target.handleItem(this, packet.item);
                items.remove(packet.item, 1);
                consumeFrontPacket(1);
                transferCredit -= 1f;
            }
        }

        private void updatePackageMode(Building target) {
            packageTimer += delta();

            while (packageTimer >= 15f && !transitItems.isEmpty()) {
                TransitItem packet = transitItems.first();
                if (packet.progress < 1f) break;

                int requested = Math.min(packageSize, packet.amount);
                int moved = movePackageToTarget(target, packet.item, requested);
                if (moved <= 0) break;

                items.remove(packet.item, moved);
                consumeFrontPacket(moved);
                packageTimer -= 15f;
            }

            packageTimer = Math.min(packageTimer, 60f);
        }

        /**
         * 优先整包输出；如果目标不支持整包，剩余部分自动逐个输出。
         */
        private int movePackageToTarget(Building target, Item item, int requested) {
            if (target == null || item == null || requested <= 0) return 0;

            int moved = 0;

            int acceptedStack = Math.max(
                    0,
                    target.acceptStack(item, requested, this)
            );

            if (acceptedStack > 0) {
                int stackMoved = Math.min(requested, acceptedStack);
                target.handleStack(item, stackMoved, this);
                moved += stackMoved;
            }

            while (moved < requested && target.acceptItem(this, item)) {
                target.handleItem(this, item);
                moved++;
            }

            return moved;
        }

        private void consumeFrontPacket(int amount) {
            if (amount <= 0 || transitItems.isEmpty()) return;

            TransitItem packet = transitItems.first();
            packet.amount -= amount;
            if (packet.amount <= 0) transitItems.remove(0);
        }

        private int entryDirection(Building source) {
            if (source == null || source.tile == null || tile == null) {
                return Mathf.mod(rotation + 2, 4);
            }

            int direction = relativeToEdge(source.tile);
            return direction < 0 ? Mathf.mod(rotation + 2, 4) : direction;
        }

        private void enqueueTransit(Item item, int amount, Building source) {
            if (item == null || amount <= 0) return;

            int entry = entryDirection(source);

            if (!packageMode) {
                for (int i = 0; i < amount; i++) addPacket(item, 1, entry);
                return;
            }

            int remaining = amount;

            if (!transitItems.isEmpty()) {
                TransitItem last = transitItems.peek();

                if (last.item == item
                        && last.entryDirection == entry
                        && last.progress <= 0.25f
                        && last.amount < packageSize) {

                    int merged = Math.min(remaining, packageSize - last.amount);
                    last.amount += merged;
                    remaining -= merged;
                }
            }

            while (remaining > 0) {
                int chunk = Math.min(packageSize, remaining);
                addPacket(item, chunk, entry);
                remaining -= chunk;
            }
        }

        private void addPacket(Item item, int amount, int entry) {
            float spacing = packageMode ? packageVisualSpacing : normalVisualSpacing;
            float initialProgress = 0f;

            if (!transitItems.isEmpty()) {
                TransitItem last = transitItems.peek();
                initialProgress = Math.min(0f, last.progress - spacing);
            }

            transitItems.add(new TransitItem(item, amount, entry, initialProgress));
        }

        private void ensureTransitMirror() {
            int queuedTotal = queuedTotal();
            int storedTotal = items.total();
            if (queuedTotal == storedTotal) return;

            if (queuedTotal < storedTotal) {
                for (Item item : Vars.content.items()) {
                    int missing = items.get(item) - queuedAmount(item);
                    if (missing > 0) enqueueTransit(item, missing, null);
                }
            } else {
                for (Item item : Vars.content.items()) {
                    int excess = queuedAmount(item) - items.get(item);
                    if (excess > 0) trimTransitFromBack(item, excess);
                }
            }
        }

        private int queuedTotal() {
            int total = 0;
            for (TransitItem packet : transitItems) total += packet.amount;
            return total;
        }

        private int queuedAmount(Item item) {
            int total = 0;
            for (TransitItem packet : transitItems) {
                if (packet.item == item) total += packet.amount;
            }
            return total;
        }

        private void trimTransitFromBack(Item item, int amount) {
            int remaining = amount;

            for (int i = transitItems.size - 1; i >= 0 && remaining > 0; i--) {
                TransitItem packet = transitItems.get(i);
                if (packet.item != item) continue;

                int taken = Math.min(packet.amount, remaining);
                packet.amount -= taken;
                remaining -= taken;

                if (packet.amount <= 0) transitItems.remove(i);
            }
        }
    }
}
