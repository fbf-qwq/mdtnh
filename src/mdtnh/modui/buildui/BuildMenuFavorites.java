package mdtnh.modui.buildui;

import arc.Core;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.world.Block;

/**
 * 建造方块收藏数据。
 *
 * <p>收藏与 MDT 自定义建造树完全解耦：这里仅保存 Block，
 * 因此收藏页可以作为原版 PlacementFragment 的一个伪分类显示。</p>
 */
public final class BuildMenuFavorites {

    private static final String settingsKey =
            "mdtnh.favorite-build-blocks";

    private static final Seq<Block> favorites =
            new Seq<>();

    private static boolean loaded;

    private BuildMenuFavorites() {
    }

    public static synchronized void load() {
        if (loaded) return;
        loaded = true;

        String raw =
                Core.settings.getString(
                        settingsKey,
                        ""
                );

        if (raw == null ||
                raw.trim().isEmpty()) {
            return;
        }

        for (String name :
                raw.split(",")) {

            String trimmed =
                    name.trim();

            if (trimmed.isEmpty()) {
                continue;
            }

            Block block = findBlock(
                    trimmed
            );

            if (block != null &&
                    !favorites.contains(
                            block,
                            true
                    )) {

                favorites.add(block);
            }
        }
    }

    public static synchronized boolean contains(
            Block block) {

        load();

        return block != null &&
                favorites.contains(
                        block,
                        true
                );
    }

    /**
     * 切换收藏状态。
     *
     * @return 切换后是否处于收藏状态
     */
    public static synchronized boolean toggle(
            Block block) {

        load();

        if (block == null) {
            return false;
        }

        if (favorites.contains(
                block,
                true
        )) {
            favorites.remove(
                    block,
                    true
            );

            save();
            return false;
        }

        favorites.add(block);
        save();
        return true;
    }

    /**
     * 返回当前收藏顺序的副本，避免 UI 误修改内部数据。
     */
    public static synchronized Seq<Block> all() {
        load();

        Seq<Block> copy =
                new Seq<>();

        copy.addAll(favorites);
        return copy;
    }

    private static Block findBlock(
            String name) {

        if (Vars.content == null) {
            return null;
        }

        for (Block block :
                Vars.content.blocks()) {

            if (block != null &&
                    name.equals(block.name)) {
                return block;
            }
        }

        return null;
    }

    private static void save() {
        StringBuilder result =
                new StringBuilder();

        for (Block block :
                favorites) {

            if (result.length() > 0) {
                result.append(',');
            }

            result.append(block.name);
        }

        Core.settings.put(
                settingsKey,
                result.toString()
        );
    }
}
