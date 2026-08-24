package mdtnh.modui.introduction;

import mindustry.ctype.UnlockableContent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 可交互产线流程图的数据定义。
 *
 * <p>节点使用整数网格坐标。标题、说明、分组名称和连线标签
 * 全部保存 bundle key。</p>
 */
public final class FlowChartDefinition {

    public static final class Group {
        public final String id;
        public final String titleKey;

        private Group(
                String id,
                String titleKey) {

            this.id = id;
            this.titleKey = titleKey;
        }
    }

    public static final class Node {
        public final String id;
        public final int column;
        public final int row;
        public final String titleKey;
        public final String descriptionKey;
        public final String groupId;
        public final UnlockableContent content;

        private Node(
                String id,
                int column,
                int row,
                String titleKey,
                String descriptionKey,
                String groupId,
                UnlockableContent content) {

            this.id = id;
            this.column = column;
            this.row = row;
            this.titleKey = titleKey;
            this.descriptionKey = descriptionKey;
            this.groupId = groupId;
            this.content = content;
        }
    }

    public static final class Edge {
        public final String fromId;
        public final String toId;
        public final String labelKey;

        private Edge(
                String fromId,
                String toId,
                String labelKey) {

            this.fromId = fromId;
            this.toId = toId;
            this.labelKey = labelKey;
        }
    }

    public final String id;
    public final String titleKey;
    public final String descriptionKey;
    public final int order;

    private final List<Group> groups =
            new ArrayList<>();

    private final List<Node> nodes =
            new ArrayList<>();

    private final List<Edge> edges =
            new ArrayList<>();

    public FlowChartDefinition(
            String id,
            String titleKey,
            String descriptionKey,
            int order) {

        this.id = id;
        this.titleKey = titleKey;
        this.descriptionKey = descriptionKey;
        this.order = order;
    }

    public FlowChartDefinition group(
            String id,
            String titleKey) {

        groups.add(
                new Group(
                        id,
                        titleKey
                )
        );

        return this;
    }

    public FlowChartDefinition node(
            String id,
            int column,
            int row,
            String titleKey,
            String descriptionKey,
            String groupId,
            UnlockableContent content) {

        nodes.add(
                new Node(
                        id,
                        column,
                        row,
                        titleKey,
                        descriptionKey,
                        groupId,
                        content
                )
        );

        return this;
    }

    public FlowChartDefinition edge(
            String fromId,
            String toId,
            String labelKey) {

        edges.add(
                new Edge(
                        fromId,
                        toId,
                        labelKey
                )
        );

        return this;
    }

    public List<Group> groups() {
        return Collections.unmodifiableList(
                groups
        );
    }

    public List<Node> nodes() {
        return Collections.unmodifiableList(
                nodes
        );
    }

    public List<Edge> edges() {
        return Collections.unmodifiableList(
                edges
        );
    }
}
