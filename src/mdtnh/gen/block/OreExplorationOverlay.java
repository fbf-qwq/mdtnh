package mdtnh.gen.block;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;

import mindustry.Vars;
import mindustry.core.World;
import mindustry.game.Team;
import mindustry.game.EventType.Trigger;
import mindustry.graphics.Layer;
import mindustry.world.Tile;

/**
 * 矿物勘探区域显示。
 *
 * F8 矿物模式开启时：
 *
 * 1. 已勘探区域覆盖淡蓝色
 * 2. 已勘探区域边缘显示轮廓
 *
 * 探矿数据本身仍然完全由
 * OreExplorationController 管理。
 */
public final class OreExplorationOverlay{

    private OreExplorationOverlay(){
    }

    private static boolean installed = false;


    // =========================================================
    // 显示参数
    // =========================================================

    /**
     * 已探矿区域颜色。
     */
    public static final Color exploredColor =
            Color.valueOf("55cfff");


    /**
     * 区域内部透明度。
     *
     * 0 = 不填充，只显示边界
     */
    public static float fillAlpha = 0.10f;


    /**
     * 边界透明度。
     */
    public static float borderAlpha = 0.75f;


    /**
     * 边界粗细。
     */
    public static float borderStroke = 1.2f;


    // =========================================================
    // 安装
    // =========================================================

    public static void install(){

        if(
                installed ||
                        Vars.headless
        ){
            return;
        }

        installed = true;


        Events.run(
                Trigger.draw,
                OreExplorationOverlay::draw
        );
    }


    // =========================================================
    // 绘制
    // =========================================================

    private static void draw(){

        /*
         * F8 没有开启矿物模式：
         *
         * 不显示探矿区域。
         */
        if(!ModOreRender.renderOres){
            return;
        }


        if(
                Vars.state == null ||
                        Vars.state.isMenu() ||
                        Vars.player == null ||
                        Vars.world == null ||
                        Vars.world.tiles == null
        ){
            return;
        }


        Team team =
                Vars.player.team();


        /*
         * 只检查摄像机当前能看到的 Tile，
         * 不遍历整张地图。
         */
        int minX =
                Math.max(
                        0,
                        World.toTile(
                                Core.camera.position.x
                                        - Core.camera.width / 2f
                        ) - 2
                );

        int maxX =
                Math.min(
                        Vars.world.width() - 1,
                        World.toTile(
                                Core.camera.position.x
                                        + Core.camera.width / 2f
                        ) + 2
                );

        int minY =
                Math.max(
                        0,
                        World.toTile(
                                Core.camera.position.y
                                        - Core.camera.height / 2f
                        ) - 2
                );

        int maxY =
                Math.min(
                        Vars.world.height() - 1,
                        World.toTile(
                                Core.camera.position.y
                                        + Core.camera.height / 2f
                        ) + 2
                );


        /*
         * 在矿物/地面之上，
         * 建筑主体之下。
         */
        Draw.z(
                Layer.blockUnder - 0.02f
        );


        // =====================================================
        // 第一遍：区域填充
        // =====================================================

        if(fillAlpha > 0f){

            Draw.color(
                    exploredColor
            );

            Draw.alpha(
                    fillAlpha
            );


            for(
                    int y = minY;
                    y <= maxY;
                    y++
            ){

                for(
                        int x = minX;
                        x <= maxX;
                        x++
                ){

                    if(
                            !OreExplorationController
                                    .isDiscovered(
                                            team,
                                            x,
                                            y
                                    )
                    ){
                        continue;
                    }


                    Tile tile =
                            Vars.world.tile(
                                    x,
                                    y
                            );

                    if(tile == null){
                        continue;
                    }


                    Fill.rect(
                            tile.worldx(),
                            tile.worldy(),
                            Vars.tilesize,
                            Vars.tilesize
                    );
                }
            }
        }


        // =====================================================
        // 第二遍：画探索区域边缘
        // =====================================================

        Draw.color(
                exploredColor
        );

        Draw.alpha(
                borderAlpha
        );

        Lines.stroke(
                borderStroke
        );


        float half =
                Vars.tilesize / 2f;


        for(
                int y = minY;
                y <= maxY;
                y++
        ){

            for(
                    int x = minX;
                    x <= maxX;
                    x++
            ){

                if(
                        !discovered(
                                team,
                                x,
                                y
                        )
                ){
                    continue;
                }


                Tile tile =
                        Vars.world.tile(
                                x,
                                y
                        );

                if(tile == null){
                    continue;
                }


                float wx =
                        tile.worldx();

                float wy =
                        tile.worldy();


                /*
                 * 左边没有探索：
                 * 画左边界
                 */
                if(
                        !discovered(
                                team,
                                x - 1,
                                y
                        )
                ){

                    Lines.line(
                            wx - half,
                            wy - half,

                            wx - half,
                            wy + half
                    );
                }


                /*
                 * 右边界
                 */
                if(
                        !discovered(
                                team,
                                x + 1,
                                y
                        )
                ){

                    Lines.line(
                            wx + half,
                            wy - half,

                            wx + half,
                            wy + half
                    );
                }


                /*
                 * 下边界
                 */
                if(
                        !discovered(
                                team,
                                x,
                                y - 1
                        )
                ){

                    Lines.line(
                            wx - half,
                            wy - half,

                            wx + half,
                            wy - half
                    );
                }


                /*
                 * 上边界
                 */
                if(
                        !discovered(
                                team,
                                x,
                                y + 1
                        )
                ){

                    Lines.line(
                            wx - half,
                            wy + half,

                            wx + half,
                            wy + half
                    );
                }
            }
        }


        Draw.reset();
    }


    // =========================================================
    // 辅助
    // =========================================================

    private static boolean discovered(
            Team team,
            int x,
            int y
    ){

        if(
                x < 0 ||
                        y < 0 ||
                        x >= Vars.world.width() ||
                        y >= Vars.world.height()
        ){
            return false;
        }


        return OreExplorationController
                .isDiscovered(
                        team,
                        x,
                        y
                );
    }
}
