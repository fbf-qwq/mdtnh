package mdtnh;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Log;
import arc.util.Timer;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mdtnh.draw.DrawerManager;
import mdtnh.gen.block.GtEarlyOreBlocks;
import mdtnh.energy.EnergySpec;
import mdtnh.energy.EnergyState;
import mdtnh.energy.MdtEnergyNode;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.content.Blocks;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.draw.DrawBlock;
import mindustry.world.meta.BuildVisibility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.Math.max;

public class ModDrill extends Block {

    private DrawerManager drawerManager = new DrawerManager();
    private TextureRegion region;

    public int top = 1, button = 1, left = 1, right = 1;
    public float drillTime = 60f; // 每次产出的 tick 数

    /** 是否接入 MDT 离散电网；默认 false 以保持旧钻头行为。 */
    public boolean usesMdtEnergy = false;
    /** 每完成一次采矿周期消耗的总能量。 */
    public float energyPerMineJ = 0f;
    /** true 时每周期只采一个矿格；GT 矿机使用该模式。 */
    public boolean mineOneOrePerCycle = false;
    /** true 时成功采矿后移除矿石 overlay，模拟 GT 矿机的有限矿脉开采。 */
    public boolean consumeOreOverlay = false;
    /** 旧钻头按扫描到的总硬度降低速度；GT 矿机应关闭。 */
    public boolean useHardnessSpeedPenalty = true;

    /** 该钻头接入 MDT 电网时使用的能源规格。 */
    public final EnergySpec energySpec = new EnergySpec();

    public ModDrill(String name) {
        super(name);
        update = true;
        solid = true;
        hasItems = true;
        buildVisibility = BuildVisibility.shown;
        requirements(Category.crafting, ItemStack.with(Items.copper, 50));
        itemCapacity = 20;

        energySpec.role = EnergySpec.Role.consumer;
        energySpec.voltageV = 8f;
        energySpec.minInputVoltageV = 0f;
        energySpec.maxInputVoltageV = 8f;
        energySpec.capacityJ = 64f;
        energySpec.maxInputA = 1;
        energySpec.maxOutputA = 0;

        buildType = ModDrillBuilding::new;
    }

    @Override
    public void load() {
        super.load();
        region = Core.atlas.find(name);
    }

    public void setDrawer(DrawBlock drawer) {
        drawerManager.setDrawer(drawer);
    }

    public DrawBlock getDrawer() {
        return drawerManager.getDrawer();
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        Seq<BuildPlan> plans = new Seq<>();
        list.each(plans::add);
        drawerManager.drawPlan(this, plan, plans);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);

        float tile = Vars.tilesize;
        float size = this.size * tile;
        float cx = x * tile + size / 2f;
        float cy = y * tile + size / 2f;

        float leftX   = cx - size/2f - left * tile-tile/2f;
        float rightX  = cx + size/2f + right * tile-tile/2f;
        float bottomY = cy - size/2f - button * tile-tile/2f;
        float topY    = cy + size/2f + top * tile-tile/2f;

        float rectWidth  = rightX - leftX;
        float rectHeight = topY - bottomY;

        if (rectWidth <= 0 || rectHeight <= 0) return;

        Draw.z(Layer.plans + 1f);
        Draw.color(Color.yellow, 0.2f);
        Fill.rect((leftX + rightX) / 2f, (bottomY + topY) / 2f, rectWidth, rectHeight);
        Draw.color(Color.yellow);
        Drawf.dashRect(Color.yellow, leftX, bottomY, rectWidth, rectHeight);
        Draw.reset();
    }
    @Override
    public void setBars(){
        super.setBars();
        addBar("process", (ModDrillBuilding build) -> new Bar(
                () -> Core.bundle.get("mdt.process.bar"),
                () -> Color.valueOf("ffd37f"),
                () -> build.progress / drillTime
        ));

        if (usesMdtEnergy) {
            addBar("mdt-energy", (ModDrillBuilding build) -> new Bar(
                    () -> Core.bundle.format("mdt.bar.energy",
                            Math.round(build.nodeState.energyJ),
                            Math.round(energySpec.capacityJ)),
                    () -> Color.valueOf("84f491"),
                    () -> energySpec.capacityJ <= 0f
                            ? 0f
                            : Math.min(1f, build.nodeState.energyJ / energySpec.capacityJ)
            ));
        }
    }

    // ----- 内部建筑类 -----
    public class ModDrillBuilding extends Building implements MdtEnergyNode {

        public final EnergyState nodeState = new EnergyState();

        /** 存储每个矿石格子的信息：坐标和对应的物品 */
        private static class OreSlot {
            final int x, y;
            final Item item;
            OreSlot(int x, int y, Item item) {
                this.x = x;
                this.y = y;
                this.item = item;
            }
        }

        /** 当前扫描到的所有矿石格子列表 */
        private List<OreSlot> oreSlots = new ArrayList<>();

        /** 生产进度（tick 累计） */
        private float progress = 0f;

        private int sumHardness = 0;

        /**
         * 重新扫描范围内的矿石格子
         */
        private void rescanOres() {
            oreSlots.clear();
            sumHardness=0;
            for (int dx = -left; dx <= right; dx++) {
                for (int dy = -button; dy <= top; dy++) {
                    Tile checkTile = Vars.world.tile(tile.x + dx, tile.y + dy);
                    if (checkTile == null) continue;

                    Block overlay =
                            checkTile.overlay();

                    Item drop =
                            GtEarlyOreBlocks.drillDrop(
                                    overlay
                            );

                    if (drop != null) {
                        oreSlots.add(
                                new OreSlot(
                                        checkTile.x,
                                        checkTile.y,
                                        drop
                                )
                        );

                        sumHardness +=
                                drop.hardness;
                    }
                }
            }

            Log.info(
                    Core.bundle.format(
                            "mdtnh.log.drill-rescan",
                            tile.x + "," + tile.y,
                            oreSlots.size()
                    )
            );
        }

        @Override
        public void placed() {
            super.placed();
            rescanOres();
        }

        @Override
        public void updateTile() {

            for (var i : oreSlots) {
                if (items.has(i.item)) dump(i.item);
            }

            if (oreSlots.isEmpty()) {
                progress = 0f;
                return;
            }

            float speedPenalty = useHardnessSpeedPenalty ? max(1, sumHardness) : 1f;
            float progressTicks = delta() / speedPenalty;

            if (usesMdtEnergy && energyPerMineJ > 0f) {
                float requiredJ = energyPerMineJ * progressTicks / Math.max(1f, drillTime);
                if (!nodeState.consume(requiredJ)) {
                    return;
                }
            }

            progress += progressTicks;

            if (progress >= drillTime) {
                progress -= drillTime;

                if (mineOneOrePerCycle) {
                    mineOneOre();
                } else {
                    mineAllOresOnce();
                }
            }
        }

        private void mineOneOre() {
            if (oreSlots.isEmpty()) return;

            for (int i = 0; i < oreSlots.size(); i++) {
                OreSlot slot = oreSlots.get(i);
                Tile t =
                        Vars.world.tile(
                                slot.x,
                                slot.y
                        );

                if (t == null) continue;

                Item item =
                        GtEarlyOreBlocks.drillDrop(
                                t.overlay()
                        );

                if (item == null ||
                        items.get(item) >=
                                itemCapacity) {
                    continue;
                }

                items.add(item, 1);
                if (consumeOreOverlay) {
                    t.setOverlay(Blocks.air);
                    oreSlots.remove(i);
                    recalculateHardness();
                }
                return;
            }
        }

        private void mineAllOresOnce() {
            for (OreSlot slot : new ArrayList<>(oreSlots)) {
                Tile t =
                        Vars.world.tile(
                                slot.x,
                                slot.y
                        );

                if (t == null) continue;

                Item item =
                        GtEarlyOreBlocks.drillDrop(
                                t.overlay()
                        );

                if (item == null ||
                        items.get(item) >=
                                itemCapacity) {
                    continue;
                }

                items.add(item, 1);
                if (consumeOreOverlay) {
                    t.setOverlay(Blocks.air);
                }
            }

            if (consumeOreOverlay) {
                rescanOres();
            }
        }

        private void recalculateHardness() {
            sumHardness = 0;
            for (OreSlot slot : oreSlots) {
                if (slot.item != null) sumHardness += slot.item.hardness;
            }
        }

        @Override
        public Building energyBuilding() {
            return this;
        }

        @Override
        public EnergySpec energySpec() {
            return ModDrill.this.energySpec;
        }

        @Override
        public EnergyState energyState() {
            return nodeState;
        }

        @Override
        public boolean canConnectToElectricGrid() {
            return usesMdtEnergy;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(progress);
            if (usesMdtEnergy) nodeState.write(write);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            if (revision >= 1) {
                progress = read.f();
                if (usesMdtEnergy) nodeState.read(read, energySpec);
            }
        }

        @Override
        public byte version() {
            return 1;
        }

        /**
         * 当周围方块发生变化时（如矿石被挖），重新扫描
         */
        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            // 延迟一帧重新扫描，避免频繁更新
            Timer.schedule(this::rescanOres, 0.5f);
        }

        @Override
        public void draw() {
            drawerManager.drawBuilding(this);
        }

        @Override
        public void drawLight() {
            drawerManager.drawLight(this);
        }
    }

    @Override
    public void drawBase(Tile tile) {
        Draw.rect(region, tile.worldx(), tile.worldy());
    }
}