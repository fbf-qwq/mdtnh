package mdtnh.energy;

import mindustry.gen.Building;

/**
 * 能够参与 MDT 离散能源网络的建筑接口。
 *
 * <p>能源系统只依赖该接口，不要求建筑继承某个特定方块类。因此普通单方块工厂、
 * 多方块能源仓和专用能源方块都可以通过组合 {@link EnergySpec} 与
 * {@link EnergyState} 接入同一网络，避免 Java 单继承带来的限制。</p>
 */
public interface MdtEnergyNode {

    /**
     * 返回承载该能源节点的 Mindustry 建筑实例。
     *
     * <p>网络使用建筑的位置、队伍和邻接信息进行分组与寻路。</p>
     */
    Building energyBuilding();

    /** @return 该节点的方块级能源参数。 */
    EnergySpec energySpec();

    /** @return 该建筑实例独立保存的能源状态。 */
    EnergyState energyState();

    /**
     * 指示该节点是否允许加入 MDT 导线网络。
     *
     * <p>蒸汽驱动设备仍可复用 {@link EnergyState} 作为内部能量缓存，但会返回
     * {@code false}，从而不会被全局电网收集、寻路或绘制连接线。</p>
     */
    default boolean canConnectToElectricGrid() {
        return true;
    }

    /** @return 当前节点是否只承担导线传输职责。 */
    default boolean isEnergyWire() {
        return energySpec().isWire();
    }

    /**
     * 判断一个相邻节点是否允许作为当前节点的下一跳。
     *
     * <p>默认双向允许。变压器、二极管等有方向性的节点可覆盖此方法，
     * 从而在不改变网络拓扑收集逻辑的前提下限制实际能量流向。</p>
     */
    default boolean canSendEnergyTo(MdtEnergyNode neighbor) {
        return true;
    }

    /**
     * 判断当前节点是否允许从指定相邻节点接收能量。
     */
    default boolean canReceiveEnergyFrom(MdtEnergyNode neighbor) {
        return true;
    }

    /**
     * 是否允许与另一个 battery 角色节点直接进行能量交换。
     *
     * <p>普通电池默认返回 false，避免多个储能方块之间来回倒能。
     * 变压器和二极管属于主动桥接设备，会覆盖为 true。</p>
     */
    default boolean allowsBatteryBridge(MdtEnergyNode other) {
        return false;
    }

    /**
     * 处理超过最高输入电压的电流包。
     *
     * <p>默认行为是清空能源缓存并摧毁建筑。特殊设备可以重写该方法，
     * 实现耐压保险、分阶段损坏或自定义爆炸效果，而无需改变全局寻路器。</p>
     *
     * @param inputVoltageV 实际到达建筑的电压
     */
    default void onOvervoltage(float inputVoltageV) {
        energyState().energyJ = 0f;
        energyBuilding().kill();
    }
    /**
     * 处理导线超过额定电压或额定电流。
     *
     * <p>默认行为是直接烧毁该导线建筑。特殊线材可覆盖此方法实现保险丝、残骸等。</p>
     */
    default void onWireOverload(
            float inputVoltageV,
            int currentA,
            boolean voltageExceeded,
            boolean currentExceeded
    ) {
        energyState().energyJ = 0f;
        energyBuilding().kill();
    }

}
