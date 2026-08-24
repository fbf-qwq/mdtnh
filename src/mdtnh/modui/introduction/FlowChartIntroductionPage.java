package mdtnh.modui.introduction;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import mindustry.ui.Styles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FlowChartDefinition 的默认交互式渲染器。
 *
 * <p>悬浮流程节点显示说明；悬浮图例突出对应分组；
 * 点击带游戏内容的节点进入该内容的介绍页。</p>
 */
public final class FlowChartIntroductionPage
        implements IntroductionPage {

    private static final float NODE_WIDTH = 170f;
    private static final float NODE_HEIGHT = 92f;
    private static final float CONNECTOR_WIDTH = 62f;
    private static final float CONNECTOR_HEIGHT = 42f;

    private final FlowChartDefinition definition;

    public FlowChartIntroductionPage(
            FlowChartDefinition definition) {

        if (definition == null) {
            throw new IllegalArgumentException(
                    "Flow chart definition is null."
            );
        }

        this.definition = definition;
    }

    @Override
    public String id() {
        return "mdtnh:flow:" +
                definition.id;
    }

    @Override
    public String sectionId() {
        return MdtIntroductionContent
                .SECTION_PRODUCTION;
    }

    @Override
    public String titleKey() {
        return definition.titleKey;
    }

    @Override
    public int order() {
        return definition.order;
    }

    @Override
    public void build(
            IntroductionContext context,
            Table root) {

        root.top().left();

        if (definition.descriptionKey != null) {
            Label description =
                    new Label(
                            Core.bundle.get(
                                    definition.descriptionKey
                            )
                    );

            description.setWrap(true);

            root.add(description)
                    .width(780f)
                    .left()
                    .pad(10f);

            root.row();
        }

        if (definition.nodes().isEmpty()) {
            root.add(
                    Core.bundle.get(
                            "mdtnh.intro.flow.empty"
                    )
            ).width(780f)
                    .left()
                    .pad(20f);

            return;
        }

        Table hoverPanel = new Table();
        hoverPanel.left();
        hoverPanel.background(Styles.black6);

        Map<String, List<Table>> groupCards =
                new HashMap<>();

        Table body = new Table();
        body.top().left();

        buildLegend(
                body,
                groupCards
        );

        buildGraph(
                context,
                body,
                hoverPanel,
                groupCards
        );

        root.add(body)
                .left()
                .top()
                .pad(8f);

        root.row();

        hoverPanel.add(
                Core.bundle.get(
                        "mdtnh.intro.flow.hover-hint"
                )
        ).width(780f)
                .left()
                .pad(10f);

        root.add(hoverPanel)
                .width(800f)
                .left()
                .pad(8f);
    }

    private void buildLegend(
            Table body,
            Map<String, List<Table>> groupCards) {

        if (definition.groups().isEmpty()) {
            return;
        }

        Table legend = new Table();
        legend.top().left();
        legend.background(Styles.black6);

        legend.add(
                Core.bundle.get(
                        "mdtnh.intro.flow.legend"
                )
        ).left()
                .pad(8f);

        legend.row();

        for (FlowChartDefinition.Group group :
                definition.groups()) {

            Table line = new Table();
            line.left();

            line.button(
                    Core.bundle.get(
                            group.titleKey
                    ),
                    Styles.flatt,
                    () -> {
                    }
            ).width(175f)
                    .height(42f);

            line.hovered(
                    () -> highlightGroup(
                            group.id,
                            groupCards
                    )
            );

            line.exited(
                    () -> resetHighlight(
                            groupCards
                    )
            );

            legend.add(line)
                    .width(185f)
                    .left()
                    .row();
        }

        body.add(legend)
                .width(195f)
                .top()
                .left()
                .padRight(8f);
    }

    private void buildGraph(
            IntroductionContext context,
            Table body,
            Table hoverPanel,
            Map<String, List<Table>> groupCards) {

        Table graph = new Table();
        graph.top().left();

        int maxColumn = 0;
        int maxRow = 0;

        for (FlowChartDefinition.Node node :
                definition.nodes()) {

            maxColumn = Math.max(
                    maxColumn,
                    node.column
            );

            maxRow = Math.max(
                    maxRow,
                    node.row
            );
        }

        for (int visualRow = 0;
             visualRow <= maxRow * 2;
             visualRow++) {

            for (int visualColumn = 0;
                 visualColumn <= maxColumn * 2;
                 visualColumn++) {

                if (visualRow % 2 == 0 &&
                        visualColumn % 2 == 0) {

                    FlowChartDefinition.Node node =
                            nodeAt(
                                    visualColumn / 2,
                                    visualRow / 2
                            );

                    if (node == null) {
                        graph.add()
                                .size(
                                        NODE_WIDTH,
                                        NODE_HEIGHT
                                );
                    } else {
                        Table card =
                                buildNodeCard(
                                        context,
                                        node,
                                        hoverPanel
                                );

                        if (node.groupId != null) {
                            groupCards
                                    .computeIfAbsent(
                                            node.groupId,
                                            ignored ->
                                                    new ArrayList<>()
                                    )
                                    .add(card);
                        }

                        graph.add(card)
                                .size(
                                        NODE_WIDTH,
                                        NODE_HEIGHT
                                );
                    }

                } else if (visualRow % 2 == 0) {

                    String connector =
                            horizontalConnector(
                                    (visualColumn - 1) / 2,
                                    visualRow / 2
                            );

                    graph.add(connector)
                            .width(CONNECTOR_WIDTH)
                            .height(NODE_HEIGHT)
                            .center();

                } else if (visualColumn % 2 == 0) {

                    String connector =
                            verticalConnector(
                                    visualColumn / 2,
                                    (visualRow - 1) / 2
                            );

                    graph.add(connector)
                            .width(NODE_WIDTH)
                            .height(CONNECTOR_HEIGHT)
                            .center();

                } else {
                    graph.add()
                            .size(
                                    CONNECTOR_WIDTH,
                                    CONNECTOR_HEIGHT
                            );
                }
            }

            graph.row();
        }

        body.add(graph)
                .top()
                .left();
    }

    private Table buildNodeCard(
            IntroductionContext context,
            FlowChartDefinition.Node node,
            Table hoverPanel) {

        Table card = new Table();
        card.top();
        card.background(Styles.black6);

        if (node.content != null &&
                node.content.uiIcon != null) {

            card.image(
                    node.content.uiIcon
            ).size(38f)
                    .padTop(6f);

            card.row();
        }

        Label title =
                new Label(
                        nodeTitle(node)
                );

        title.setWrap(true);

        card.add(title)
                .width(NODE_WIDTH - 16f)
                .center()
                .pad(5f);

        card.hovered(
                () -> rebuildHoverPanel(
                        hoverPanel,
                        node
                )
        );

        if (node.content != null) {
            card.clicked(
                    () -> context.openContent(
                            node.content
                    )
            );
        }

        return card;
    }

    private void rebuildHoverPanel(
            Table hoverPanel,
            FlowChartDefinition.Node node) {

        hoverPanel.clearChildren();
        hoverPanel.left();

        if (node.content != null &&
                node.content.uiIcon != null) {

            hoverPanel.image(
                    node.content.uiIcon
            ).size(42f)
                    .pad(8f);
        }

        hoverPanel.table(text -> {
            text.left();

            text.add(nodeTitle(node))
                    .left()
                    .padBottom(4f);

            text.row();

            Label body =
                    new Label(
                            nodeDescription(node)
                    );

            body.setWrap(true);

            text.add(body)
                    .width(700f)
                    .left();
        }).left()
                .pad(6f);
    }

    private String nodeTitle(
            FlowChartDefinition.Node node) {

        if (node.titleKey != null) {
            return Core.bundle.get(
                    node.titleKey
            );
        }

        if (node.content != null) {
            return node.content.localizedName;
        }

        return Core.bundle.get(
                "mdtnh.intro.flow.unnamed-node"
        );
    }

    private String nodeDescription(
            FlowChartDefinition.Node node) {

        if (node.descriptionKey != null) {
            return Core.bundle.get(
                    node.descriptionKey
            );
        }

        if (node.content != null &&
                node.content.description != null &&
                !node.content.description
                        .trim()
                        .isEmpty()) {

            return node.content.description;
        }

        return Core.bundle.get(
                "mdtnh.intro.content.no-description"
        );
    }

    private void highlightGroup(
            String groupId,
            Map<String, List<Table>> groupCards) {

        for (Map.Entry<String, List<Table>> entry :
                groupCards.entrySet()) {

            Color color =
                    entry.getKey().equals(groupId)
                            ? Color.white
                            : Color.gray;

            for (Table card :
                    entry.getValue()) {

                card.setColor(color);
            }
        }
    }

    private void resetHighlight(
            Map<String, List<Table>> groupCards) {

        for (List<Table> list :
                groupCards.values()) {

            for (Table card : list) {
                card.setColor(Color.white);
            }
        }
    }

    private FlowChartDefinition.Node nodeAt(
            int column,
            int row) {

        for (FlowChartDefinition.Node node :
                definition.nodes()) {

            if (node.column == column &&
                    node.row == row) {

                return node;
            }
        }

        return null;
    }

    private String horizontalConnector(
            int leftColumn,
            int row) {

        FlowChartDefinition.Node left =
                nodeAt(leftColumn, row);

        FlowChartDefinition.Node right =
                nodeAt(leftColumn + 1, row);

        if (left == null ||
                right == null) {
            return "";
        }

        for (FlowChartDefinition.Edge edge :
                definition.edges()) {

            if (edge.fromId.equals(left.id) &&
                    edge.toId.equals(right.id)) {

                return Core.bundle.get(
                        "mdtnh.intro.flow.arrow.right"
                );
            }

            if (edge.fromId.equals(right.id) &&
                    edge.toId.equals(left.id)) {

                return Core.bundle.get(
                        "mdtnh.intro.flow.arrow.left"
                );
            }
        }

        return "";
    }

    private String verticalConnector(
            int column,
            int topRow) {

        FlowChartDefinition.Node top =
                nodeAt(column, topRow);

        FlowChartDefinition.Node bottom =
                nodeAt(column, topRow + 1);

        if (top == null ||
                bottom == null) {
            return "";
        }

        for (FlowChartDefinition.Edge edge :
                definition.edges()) {

            if (edge.fromId.equals(top.id) &&
                    edge.toId.equals(bottom.id)) {

                return Core.bundle.get(
                        "mdtnh.intro.flow.arrow.down"
                );
            }

            if (edge.fromId.equals(bottom.id) &&
                    edge.toId.equals(top.id)) {

                return Core.bundle.get(
                        "mdtnh.intro.flow.arrow.up"
                );
            }
        }

        return "";
    }
}
