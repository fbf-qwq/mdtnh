package mdtnh.energy;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.scene.ui.CheckBox;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import arc.util.Log;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.ui.Bar;
import mindustry.world.Block;

public class MdtEnergyBlock extends Block {

    /** 方块在离散能源系统中的用途。 */
    public enum EnergyRole {
        generator,
        wire,
        consumer,
        battery
    }

    /*
     * 方块级默认参数。
     *
     * 普通能源方块直接使用这些值；开启 configurableGenerator 后，
     * 每个建筑实例会复制一份运行时 EnergySpec，并用其独立配置覆盖
     * 输出电压与最大输出电流。
     */
    public EnergyRole role = EnergyRole.battery;
    public float voltageV = 12f;
    public float capacityJ = 1000f;
    public float initialEnergyFraction = 0f;
    public int maxInputA = 1;
    public int maxOutputA = 1;
    public float generationJPerSecond = 0f;
    public float consumptionJPerSecond = 0f;
    /** 导线额定最高包电压；普通设备不使用。 */
    public float maxWireVoltageV = Float.MAX_VALUE;
    public int maxWireCurrentA = 1;
    public float wireLossV = 0f;
    public String fallbackRegion = "battery";

    /**
     * GT 导线/线缆的方向连接贴图。
     *
     * <p>导线只有两张贴图：</p>
     * <ul>
     *     <li>center：始终绘制；</li>
     *     <li>edge：默认朝右，仅在对应方向存在电网连接时旋转并绘制。</li>
     * </ul>
     *
     * <p>线缆先绘制“对应材料、对应粗细的导线”底层，再绘制一套仅按粗细区分、
     * 所有材料共用的 cable center/edge 覆盖层。</p>
     */
    public boolean connectedWireSprites = false;

    /** 导线底层中心贴图。 */
    public String wireCenterRegionName;
    /** 导线底层边缘贴图；原图默认朝右。 */
    public String wireEdgeRegionName;
    public TextureRegion wireCenterRegion;
    public TextureRegion wireEdgeRegion;

    /** 是否额外绘制线缆绝缘覆盖层。 */
    public boolean cableWireSprites = false;
    /** 同一粗细线缆共用的覆盖层中心贴图。 */
    public String cableCenterRegionName;
    /** 同一粗细线缆共用的覆盖层边缘贴图；原图默认朝右。 */
    public String cableEdgeRegionName;
    public TextureRegion cableCenterRegion;
    public TextureRegion cableEdgeRegion;

    public Color wireSpriteColor = Color.white.cpy();

    /**
     * edge 原图默认朝右，因此默认偏移为 0°。
     * 如果美术资源的基准方向不同，可单独调整这两个偏移。
     */
    public float wireEdgeRotationOffsetDeg = 0f;
    public float cableEdgeRotationOffsetDeg = 0f;

    /*
     * 可配置调试发电机。
     *
     * 开启后，发电由 MdtEnergyBuild.updateTile() 根据建筑实例的配置执行。
     * 为防止和旧 MdtEnergySystem 中读取 generationJPerSecond 的固定发电
     * 重复叠加，示例发电机应把 generationJPerSecond 设为 0，并通过
     * defaultConfiguredGenerationJPerSecond 指定默认增能速度。
     */
    public boolean configurableGenerator = false;
    public boolean defaultGeneratorEnabled = true;
    public float defaultConfiguredVoltageV = -1f;
    public float defaultConfiguredGenerationJPerSecond = -1f;
    public int defaultConfiguredMaxOutputA = -1;

    /** 配置输入的保护上限，防止误输入造成浮点溢出或超大循环。 */
    public float maxConfigVoltageV = 536_870_912f;
    public float maxConfigGenerationJPerSecond = 1_000_000_000f;
    public int maxConfigOutputA = 1_000_000;

    /** 方块级统一能源规格；可配置发电机会为每个建筑复制运行时版本。 */
    private EnergySpec spec;

    public MdtEnergyBlock(String name) {
        super(name);

        update = true;
        solid = true;
        destructible = true;
        canOverdrive = false;
        hasPower = false;
        outputsPower = false;
        consumesPower = false;
        conductivePower = false;
        connectedPower = false;
        sync = true;

        /*
         * 使用 String 作为网络配置载体，格式为：
         * enabled;voltageV;generationJPerSecond;maxOutputA
         *
         * Mindustry 的配置对象序列化支持 String，因此多人游戏中也会
         * 通过正常的 tileConfig 调用交给服务器处理。
         */
        config(String.class, (MdtEnergyBuild build, String value) ->
                build.applyGeneratorConfig(value));

        configClear((MdtEnergyBuild build) ->
                build.resetGeneratorConfig());

        buildType = MdtEnergyBuild::new;
    }

    @Override
    public void init() {
        if (isConfigurableGenerator()) {
            configurable = true;
            saveConfig = true;
            copyConfig = true;
            clearOnDoubleTap = false;
        }

        super.init();
    }

    /** 在内容加载时把方块级参数复制到统一 EnergySpec。 */
    @Override
    public void load() {
        super.load();
        region = Core.atlas.find(fallbackRegion);

        if (connectedWireSprites && role == EnergyRole.wire) {
            /*
             * 不直接使用生成器里写死的 "gt-..." 名称。
             * Java 模组内容的实际 content name 可能已经带有模组命名空间前缀，
             * 例如 mdtnh-gt-copper-wire-1。这里从最终的 name 推导 atlas 名，
             * 从而和 Mindustry 实际打包后的 region 名保持一致。
             */
            String wireVisualBase = cableWireSprites
                    ? name.replace("-cable-", "-wire-")
                    : name;

            wireCenterRegion = findWireRegion(
                    wireCenterRegionName,
                    wireVisualBase + "-center"
            );
            wireEdgeRegion = findWireRegion(
                    wireEdgeRegionName,
                    wireVisualBase + "-edge"
            );

            if (!wireCenterRegion.found()) {
                Log.err("Missing MDT wire center sprite: @", wireVisualBase + "-center");
            }
            if (!wireEdgeRegion.found()) {
                Log.err("Missing MDT wire edge sprite: @", wireVisualBase + "-edge");
            }

            if (cableWireSprites) {
                String cableOverlayBase = sharedCableOverlayBase(name);

                cableCenterRegion = findWireRegion(
                        cableCenterRegionName,
                        cableOverlayBase + "-center"
                );
                cableEdgeRegion = findWireRegion(
                        cableEdgeRegionName,
                        cableOverlayBase + "-edge"
                );

                if (!cableCenterRegion.found()) {
                    Log.err("Missing MDT cable center sprite: @", cableOverlayBase + "-center");
                }
                if (!cableEdgeRegion.found()) {
                    Log.err("Missing MDT cable edge sprite: @", cableOverlayBase + "-edge");
                }
            }

            /*
             * 不再把缺失的导线贴图替换成 power-node。
             * 否则一旦资源名不匹配，就会出现“导线仍被渲染成电力节点”的假象。
             */
            if (wireCenterRegion.found()) {
                region = wireCenterRegion;
            }
        }

        spec = new EnergySpec();
        spec.role = convertRole(role);
        spec.voltageV = voltageV;
        spec.capacityJ = capacityJ;
        spec.maxInputA = maxInputA;
        spec.maxOutputA = maxOutputA;
        spec.maxWireVoltageV = maxWireVoltageV;
        spec.maxWireCurrentA = maxWireCurrentA;
        spec.wireLossV = wireLossV;
    }

    /**
     * 查找导线相关 region。explicitName 允许调用方覆盖默认名称；
     * 默认名称应当已经从最终 content name 推导，因此天然保留模组命名空间。
     */
    private TextureRegion findWireRegion(String explicitName, String defaultName) {
        if (explicitName == null || explicitName.isEmpty()) {
            return Core.atlas.find(defaultName);
        }

        TextureRegion explicit = Core.atlas.find(explicitName);
        if (explicit.found()) return explicit;

        // 若显式名称是未加命名空间的 gt-*，尝试补上当前方块使用的前缀。
        int gtIndex = name.indexOf("gt-");
        if (gtIndex > 0 && explicitName.startsWith("gt-")) {
            TextureRegion namespaced = Core.atlas.find(
                    name.substring(0, gtIndex) + explicitName
            );
            if (namespaced.found()) return namespaced;
        }

        return explicit;
    }

    /**
     * 从实际线缆 content name 推导“同粗细共用”的覆盖贴图名称。
     *
     * 例如：
     * mdtnh-gt-copper-cable-4 -> mdtnh-gt-cable-4
     * gt-copper-cable-4       -> gt-cable-4
     */
    private static String sharedCableOverlayBase(String contentName) {
        int gtIndex = contentName.indexOf("gt-");
        int cableIndex = contentName.lastIndexOf("-cable-");

        if (gtIndex >= 0 && cableIndex > gtIndex) {
            String prefix = contentName.substring(0, gtIndex);
            String count = contentName.substring(cableIndex + "-cable-".length());
            return prefix + "gt-cable-" + count;
        }

        // 非标准名称时仍给出稳定后备规则。
        int lastDash = contentName.lastIndexOf('-');
        String count = lastDash >= 0 ? contentName.substring(lastDash + 1) : contentName;
        return "gt-cable-" + count;
    }

    public boolean isWire() {
        return spec != null ? spec.isWire() : role == EnergyRole.wire;
    }

    public boolean isConfigurableGenerator() {
        return configurableGenerator && role == EnergyRole.generator;
    }

    public EnergySpec energySpec() {
        return spec;
    }

    private static EnergySpec.Role convertRole(EnergyRole value) {
        switch (value) {
            case generator:
                return EnergySpec.Role.generator;
            case wire:
                return EnergySpec.Role.wire;
            case consumer:
                return EnergySpec.Role.consumer;
            default:
                return EnergySpec.Role.battery;
        }
    }

    private float defaultGeneratorVoltage() {
        return defaultConfiguredVoltageV >= 0f
                ? defaultConfiguredVoltageV
                : voltageV;
    }

    private float defaultGeneratorRate() {
        return defaultConfiguredGenerationJPerSecond >= 0f
                ? defaultConfiguredGenerationJPerSecond
                : generationJPerSecond;
    }

    private int defaultGeneratorOutputCurrent() {
        return defaultConfiguredMaxOutputA >= 0
                ? defaultConfiguredMaxOutputA
                : maxOutputA;
    }

    @Override
    public void setBars() {
        super.setBars();

        if (isWire()) {
            addBar("mdt-current", build -> {
                MdtEnergyBuild energy = (MdtEnergyBuild) build;
                int maximum = Math.max(0, energy.energySpec().maxWireCurrentA);

                return new Bar(
                        () -> "Current: " + energy.nodeState.currentA
                                + " / " + maximum + " A",
                        () -> Color.valueOf("ffd37f"),
                        () -> maximum <= 0
                                ? 0f
                                : Math.min(1f, energy.nodeState.currentA / (float) maximum)
                );
            });
        } else {
            addBar("mdt-energy", build -> {
                MdtEnergyBuild energy = (MdtEnergyBuild) build;
                float capacity = energy.energySpec().capacityJ;

                return new Bar(
                        () -> "Energy: " + Math.round(energy.nodeState.energyJ)
                                + " / " + Math.round(capacity) + " J",
                        () -> Color.valueOf("ffd37f"),
                        () -> capacity <= 0f
                                ? 0f
                                : Math.min(1f, energy.nodeState.energyJ / capacity)
                );
            });

            addBar("mdt-io", build -> {
                MdtEnergyBuild energy = (MdtEnergyBuild) build;
                EnergySpec runtime = energy.energySpec();
                int maximum = Math.max(1,
                        Math.max(runtime.maxInputA, energy.configuredMaxOutputA()));

                return new Bar(
                        () -> "I/O: " + energy.nodeState.inputA
                                + " A in, " + energy.nodeState.outputA + " A out",
                        () -> Color.valueOf("84f491"),
                        () -> Math.min(1f,
                                Math.max(energy.nodeState.inputA, energy.nodeState.outputA)
                                        / (float) maximum)
                );
            });

            if (isConfigurableGenerator()) {
                addBar("mdt-generator-config", build -> {
                    MdtEnergyBuild energy = (MdtEnergyBuild) build;

                    return new Bar(
                            () -> (energy.generatorEnabled ? "[green]ON[] " : "[red]OFF[] ")
                                    + Strings.fixed(energy.configuredVoltageV, 2) + " V, "
                                    + Strings.fixed(energy.configuredGenerationJPerSecond, 1)
                                    + " J/s, "
                                    + energy.configuredMaxOutputA + " A",
                            () -> energy.generatorEnabled
                                    ? Color.valueOf("84f491")
                                    : Color.valueOf("ff6655"),
                            () -> energy.generatorEnabled ? 1f : 0f
                    );
                });
            }
        }
    }

    public class MdtEnergyBuild extends Building implements MdtEnergyNode {

        /** 当前储能与上一模拟秒的电流测量值。 */
        public final EnergyState nodeState = new EnergyState();

        /** 可配置发电机的实例级参数。 */
        public boolean generatorEnabled;
        public float configuredVoltageV;
        public float configuredGenerationJPerSecond;
        public int configuredMaxOutputA;

        /** 仅供本建筑使用，避免修改同类方块的全局 EnergySpec。 */
        private final EnergySpec runtimeSpec = new EnergySpec();
        private boolean generatorConfigInitialized;

        @Override
        public Building energyBuilding() {
            return this;
        }

        @Override
        public EnergySpec energySpec() {
            if (!isConfigurableGenerator()) {
                return MdtEnergyBlock.this.energySpec();
            }

            ensureGeneratorConfigInitialized();
            refreshRuntimeSpec();
            return runtimeSpec;
        }

        @Override
        public EnergyState energyState() {
            return nodeState;
        }

        /** 兼容旧能源系统中取得方块类型的代码。 */
        public MdtEnergyBlock energyBlock() {
            return MdtEnergyBlock.this;
        }

        public boolean isWire() {
            return energySpec().isWire();
        }

        public float soc() {
            float capacity = energySpec().capacityJ;
            return capacity <= 0f ? 0f : nodeState.energyJ / capacity;
        }

        public int configuredMaxOutputA() {
            if (!isConfigurableGenerator()) {
                return energySpec().maxOutputA;
            }

            ensureGeneratorConfigInitialized();
            return configuredMaxOutputA;
        }

        private void ensureGeneratorConfigInitialized() {
            if (!generatorConfigInitialized) {
                resetGeneratorConfig();
            }
        }

        /**
         * 恢复该建筑的默认调试参数。
         * 配置被清除、旧存档读取或建筑刚创建时都会调用。
         */
        public void resetGeneratorConfig() {
            generatorEnabled = defaultGeneratorEnabled;
            configuredVoltageV = Mathf.clamp(
                    defaultGeneratorVoltage(),
                    0f,
                    Math.max(0f, maxConfigVoltageV)
            );
            configuredGenerationJPerSecond = Mathf.clamp(
                    defaultGeneratorRate(),
                    0f,
                    Math.max(0f, maxConfigGenerationJPerSecond)
            );
            configuredMaxOutputA = Mathf.clamp(
                    defaultGeneratorOutputCurrent(),
                    0,
                    Math.max(0, maxConfigOutputA)
            );
            generatorConfigInitialized = true;
            refreshRuntimeSpec();
        }

        /**
         * 应用来自配置界面或网络的参数。
         * 无效字段不会抛出异常，而是保留当前值并继续钳制到安全范围。
         */
        public void applyGeneratorConfig(String encoded) {
            if (!isConfigurableGenerator() || encoded == null) {
                return;
            }

            ensureGeneratorConfigInitialized();

            String[] parts = encoded.split(";", -1);
            if (parts.length != 4) {
                return;
            }

            boolean nextEnabled =
                    "1".equals(parts[0])
                            || "true".equalsIgnoreCase(parts[0]);

            float nextVoltage = parseFloat(parts[1], configuredVoltageV);
            float nextRate = parseFloat(
                    parts[2],
                    configuredGenerationJPerSecond
            );
            int nextCurrent = parseInt(parts[3], configuredMaxOutputA);

            generatorEnabled = nextEnabled;
            configuredVoltageV = Mathf.clamp(
                    nextVoltage,
                    0f,
                    Math.max(0f, maxConfigVoltageV)
            );
            configuredGenerationJPerSecond = Mathf.clamp(
                    nextRate,
                    0f,
                    Math.max(0f, maxConfigGenerationJPerSecond)
            );
            configuredMaxOutputA = Mathf.clamp(
                    nextCurrent,
                    0,
                    Math.max(0, maxConfigOutputA)
            );

            refreshRuntimeSpec();
        }

        private float parseFloat(String text, float fallback) {
            try {
                float value = Float.parseFloat(text.trim());
                return Float.isFinite(value) ? value : fallback;
            } catch (RuntimeException ignored) {
                return fallback;
            }
        }

        private int parseInt(String text, int fallback) {
            try {
                return Integer.parseInt(text.trim());
            } catch (RuntimeException ignored) {
                return fallback;
            }
        }

        private String encodeGeneratorConfig(
                boolean enabledValue,
                float voltageValue,
                float rateValue,
                int currentValue
        ) {
            return (enabledValue ? "1" : "0")
                    + ";" + voltageValue
                    + ";" + rateValue
                    + ";" + currentValue;
        }

        /**
         * 从方块级规格复制不变部分，再覆盖该建筑的输出参数。
         *
         * 关闭发电机时把运行时 maxOutputA 设为 0，既停止继续增能，
         * 也阻止其把内部残留能量继续送入网络。
         */
        private void refreshRuntimeSpec() {
            EnergySpec base = MdtEnergyBlock.this.energySpec();
            if (base == null) {
                return;
            }

            runtimeSpec.role = base.role;
            runtimeSpec.voltageV = configuredVoltageV;
            runtimeSpec.capacityJ = base.capacityJ;
            runtimeSpec.maxInputA = base.maxInputA;
            runtimeSpec.maxOutputA = generatorEnabled
                    ? configuredMaxOutputA
                    : 0;
            runtimeSpec.maxWireVoltageV = base.maxWireVoltageV;
            runtimeSpec.maxWireCurrentA = base.maxWireCurrentA;
            runtimeSpec.wireLossV = base.wireLossV;
        }

        @Override
        public void created() {
            super.created();

            if (isConfigurableGenerator()) {
                resetGeneratorConfig();
            }

            nodeState.energyJ = isWire()
                    ? 0f
                    : energySpec().capacityJ * initialEnergyFraction;
        }

        @Override
        public void updateTile() {
            super.updateTile();

            if (!isConfigurableGenerator()) {
                return;
            }

            ensureGeneratorConfigInitialized();

            /*
             * 只由服务器或单人游戏执行权威增能。
             * Time.delta 的基准是 1 tick，因此除以 60 得到每秒速率。
             */
            if (!Vars.net.client()
                    && generatorEnabled
                    && configuredGenerationJPerSecond > 0f) {
                float generatedJ =
                        configuredGenerationJPerSecond * Time.delta / 60f;
                nodeState.add(generatedJ, energySpec());
            }
        }

        @Override
        public void buildConfiguration(Table table) {
            if (!isConfigurableGenerator()) {
                return;
            }

            ensureGeneratorConfigInitialized();

            table.defaults().pad(4f).left();

            CheckBox enabledBox = new CheckBox("启用发电与输出");
            enabledBox.setChecked(generatorEnabled);
            table.add(enabledBox).colspan(2).left();
            table.row();

            table.add("输出电压 (V)");
            TextField voltageField =
                    new TextField(Strings.fixed(configuredVoltageV, 3));
            table.add(voltageField).width(150f);
            table.row();

            table.add("增能速度 (J/s)");
            TextField rateField =
                    new TextField(Strings.fixed(
                            configuredGenerationJPerSecond,
                            3
                    ));
            table.add(rateField).width(150f);
            table.row();

            table.add("最大输出电流 (A)");
            TextField currentField =
                    new TextField(Integer.toString(configuredMaxOutputA));
            table.add(currentField).width(150f);
            table.row();

            table.button("应用", () -> {
                float nextVoltage =
                        parseFloat(voltageField.getText(), configuredVoltageV);
                float nextRate =
                        parseFloat(
                                rateField.getText(),
                                configuredGenerationJPerSecond
                        );
                int nextCurrent =
                        parseInt(currentField.getText(), configuredMaxOutputA);

                configure(encodeGeneratorConfig(
                        enabledBox.isChecked(),
                        nextVoltage,
                        nextRate,
                        nextCurrent
                ));
            }).width(105f);

            table.button("恢复默认", () -> configure(null))
                    .width(105f);
        }

        /**
         * 返回当前配置，使蓝图、复制配置和重建计划保留调试参数。
         */
        @Override
        public Object config() {
            if (!isConfigurableGenerator()) {
                return null;
            }

            ensureGeneratorConfigInitialized();
            return encodeGeneratorConfig(
                    generatorEnabled,
                    configuredVoltageV,
                    configuredGenerationJPerSecond,
                    configuredMaxOutputA
            );
        }

        @Override
        public void draw() {
            if (!isWire()) {
                super.draw();
                return;
            }

            if (connectedWireSprites
                    && wireCenterRegion != null
                    && wireCenterRegion.found()) {
                drawConnectedWireSprites();
                return;
            }

            // Legacy/debug fallback when no connected-sprite mode is requested.
            float fraction = energySpec().maxWireCurrentA <= 0
                    ? 0f
                    : Math.min(
                    1f,
                    nodeState.currentA
                            / (float) energySpec().maxWireCurrentA
            );

            Draw.color(Color.valueOf("ffd37f"));
            Lines.stroke(1.2f + 1.8f * fraction);

            if (tile != null) {
                for (int direction = 0; direction < 4; direction++) {
                    Building nearby = tile.nearbyBuild(direction);
                    if (nearby instanceof MdtEnergyNode
                            && MdtEnergySystem.canConnect(
                            this,
                            (MdtEnergyNode) nearby
                    )) {
                        Lines.line(x, y, nearby.x, nearby.y);
                    }
                }
            }

            Draw.reset();
            super.draw();
        }

        private void drawConnectedWireSprites() {
            Draw.color(wireSpriteColor);

            // 底层：始终绘制导线中心；有连接的方向再绘制导线边缘。
            Draw.z(Layer.blockUnder + 0.10f);
            Draw.rect(wireCenterRegion, x, y);

            Draw.z(Layer.blockUnder + 0.12f);
            drawConnectedWireEdges(wireEdgeRegion, wireEdgeRotationOffsetDeg);

            // 上层：仅线缆启用。所有同粗细线缆共用同一套覆盖贴图。
            if (cableWireSprites) {
                if (cableCenterRegion != null && cableCenterRegion.found()) {
                    Draw.z(Layer.blockUnder + 0.20f);
                    Draw.rect(cableCenterRegion, x, y);
                }

                Draw.z(Layer.blockUnder + 0.22f);
                drawConnectedWireEdges(cableEdgeRegion, cableEdgeRotationOffsetDeg);
            }

            Draw.reset();
        }

        /**
         * 绘制四周连接贴图。
         *
         * <p>Mindustry 的方向 0/1/2/3 依次对应右/上/左/下，因此当 edge 原图默认朝右时，
         * 直接使用 direction * 90° 即可得到四个方向。</p>
         */
        private void drawConnectedWireEdges(
                TextureRegion region,
                float rotationOffset
        ) {
            if (region == null || !region.found() || tile == null) return;

            for (int direction = 0; direction < 4; direction++) {
                Building nearby = tile.nearbyBuild(direction);
                if (!(nearby instanceof MdtEnergyNode)
                        || !MdtEnergySystem.canConnect(
                        this,
                        (MdtEnergyNode) nearby
                )) {
                    continue;
                }

                Draw.rect(
                        region,
                        x,
                        y,
                        direction * 90f + rotationOffset
                );
            }
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            nodeState.write(write);

            if (isConfigurableGenerator()) {
                ensureGeneratorConfigInitialized();
                write.bool(generatorEnabled);
                write.f(configuredVoltageV);
                write.f(configuredGenerationJPerSecond);
                write.i(configuredMaxOutputA);
            }
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            nodeState.read(read, MdtEnergyBlock.this.energySpec());

            if (isConfigurableGenerator()) {
                resetGeneratorConfig();

                if (revision >= 2) {
                    generatorEnabled = read.bool();
                    configuredVoltageV = Mathf.clamp(
                            read.f(),
                            0f,
                            Math.max(0f, maxConfigVoltageV)
                    );
                    configuredGenerationJPerSecond = Mathf.clamp(
                            read.f(),
                            0f,
                            Math.max(0f, maxConfigGenerationJPerSecond)
                    );
                    configuredMaxOutputA = Mathf.clamp(
                            read.i(),
                            0,
                            Math.max(0, maxConfigOutputA)
                    );
                    refreshRuntimeSpec();
                }
            }
        }

        @Override
        public byte version() {
            return 2;
        }
    }
}
