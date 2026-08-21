package mdtnh.gen.block;

import arc.Core;
import arc.Events;
import arc.struct.IntIntMap;
import arc.struct.IntMap;
import arc.struct.IntSet;
import arc.struct.Seq;
import arc.util.Time;

import mindustry.Vars;
import mindustry.game.Team;
import mindustry.game.EventType.ResetEvent;
import mindustry.game.EventType.Trigger;
import mindustry.game.EventType.WorldLoadEvent;
import mindustry.gen.Groups;
import mindustry.io.SaveFileReader.CustomChunk;
import mindustry.io.SaveVersion;
import mindustry.world.Tile;

/**
 * 独立矿产勘探系统。
 *
 * 与 Mindustry 原版 FogControl 完全无关。
 *
 * 每个队伍拥有自己永久探索过的 Tile 集合。
 */
public final class OreExplorationController{

    private OreExplorationController(){
    }

    /**
     * team.id -> 已勘探 Tile position
     */
    private static final IntMap<IntSet> explored =
            new IntMap<>();

    /**
     * 当前地图里的所有 ToggleOreBlock。
     *
     * 用于 F8 切换时快速刷新渲染缓存。
     */
    private static final Seq<Tile> oreTiles =
            new Seq<>();

    /**
     * unit.id -> 上一次扫描时所在 Tile。
     *
     * 防止勘探单位停着不动时反复扫描。
     */
    private static final IntIntMap lastUnitTiles =
            new IntIntMap();

    private static boolean installed = false;

    private static float unitScanTimer = 0f;

    /**
     * 单位扫描检测间隔。
     *
     * 6 tick ≈ 0.1 秒。
     */
    private static final float unitScanInterval = 6f;

    /**
     * 自定义存档块名称。
     *
     * 必须尽量唯一。
     */
    private static final String saveChunkName =
            "mindustry-newhorizon-ore-exploration";

    private static final int saveVersion = 1;


    // =========================================================
    // 安装
    // =========================================================

    public static void install(){

        if(installed){
            return;
        }

        installed = true;


        // -----------------------------------------------------
        // 独立存档
        // -----------------------------------------------------

        SaveVersion.addCustomChunk(
                saveChunkName,
                new CustomChunk(){

                    @Override
                    public void write(java.io.DataOutput out)
                            throws java.io.IOException{

                        out.writeByte(saveVersion);

                        // 队伍数量
                        out.writeInt(explored.size);

                        for(
                                IntMap.Entry<IntSet> entry :
                                explored.entries()
                        ){

                            // team id
                            out.writeInt(entry.key);

                            IntSet set = entry.value;

                            // 已探索 Tile 数量
                            out.writeInt(set.size);

                            IntSet.IntSetIterator iterator =
                                    set.iterator();

                            while(iterator.hasNext){

                                out.writeInt(
                                        iterator.next()
                                );
                            }
                        }
                    }


                    @Override
                    public void read(java.io.DataInput in)
                            throws java.io.IOException{

                        explored.clear();
                        lastUnitTiles.clear();

                        int version =
                                in.readUnsignedByte();

                        if(version != saveVersion){
                            throw new java.io.IOException(
                                    "Unsupported ore exploration save version: "
                                            + version
                            );
                        }

                        int teamCount =
                                in.readInt();

                        for(
                                int t = 0;
                                t < teamCount;
                                t++
                        ){

                            int teamId =
                                    in.readInt();

                            int count =
                                    in.readInt();

                            IntSet set =
                                    new IntSet(
                                            Math.max(
                                                    count,
                                                    16
                                            )
                                    );

                            for(
                                    int i = 0;
                                    i < count;
                                    i++
                            ){
                                set.add(
                                        in.readInt()
                                );
                            }

                            explored.put(
                                    teamId,
                                    set
                            );
                        }
                    }
                }
        );


        // -----------------------------------------------------
        // 世界加载
        // -----------------------------------------------------

        Events.on(
                WorldLoadEvent.class,
                event -> {

                    /*
                     * 等 FloorRenderer 初始化结束，
                     * 再建立矿石索引和刷新缓存。
                     */
                    if(
                            !Vars.headless &&
                                    Core.app != null
                    ){
                        Core.app.post(
                                OreExplorationController
                                        ::rebuildOreIndex
                        );
                    }else{
                        rebuildOreIndex();
                    }
                }
        );


        // -----------------------------------------------------
        // 世界重置
        // -----------------------------------------------------

        Events.on(
                ResetEvent.class,
                event -> clearRuntimeData()
        );


        // -----------------------------------------------------
        // 勘探单位
        // -----------------------------------------------------

        Events.run(
                Trigger.update,
                OreExplorationController
                        ::updateSurveyUnits
        );
    }


    // =========================================================
    // 查询
    // =========================================================

    public static boolean isDiscovered(
            Tile tile
    ){

        if(tile == null){
            return false;
        }

        /*
         * 内容预览 / 菜单里正常显示矿石。
         */
        if(
                Vars.state == null ||
                        Vars.state.isMenu()
        ){
            return true;
        }

        /*
         * 没有本地玩家时先隐藏。
         */
        if(Vars.player == null){
            return false;
        }

        return isDiscovered(
                Vars.player.team(),
                tile.x,
                tile.y
        );
    }


    public static boolean isDiscovered(
            Team team,
            int x,
            int y
    ){

        if(
                team == null ||
                        Vars.world == null
        ){
            return false;
        }

        IntSet set =
                explored.get(team.id);

        if(set == null){
            return false;
        }

        return set.contains(
                position(x, y)
        );
    }


    // =========================================================
    // 永久勘探
    // =========================================================

    /**
     * 永久探索一个圆形区域。
     *
     * radius 单位：Tile
     */
    public static void reveal(
            Team team,
            int centerX,
            int centerY,
            int radius
    ){

        if(
                team == null ||
                        Vars.world == null ||
                        Vars.world.tiles == null ||
                        radius <= 0
        ){
            return;
        }

        IntSet set =
                getTeamSet(team.id);

        int radius2 =
                radius * radius;

        int minX =
                Math.max(
                        0,
                        centerX - radius
                );

        int maxX =
                Math.min(
                        Vars.world.width() - 1,
                        centerX + radius
                );

        int minY =
                Math.max(
                        0,
                        centerY - radius
                );

        int maxY =
                Math.min(
                        Vars.world.height() - 1,
                        centerY + radius
                );


        for(int y = minY; y <= maxY; y++){

            int dy =
                    y - centerY;

            for(
                    int x = minX;
                    x <= maxX;
                    x++
            ){

                int dx =
                        x - centerX;

                if(
                        dx * dx +
                                dy * dy >
                                radius2
                ){
                    continue;
                }

                int pos =
                        position(x, y);

                /*
                 * add() == true
                 *
                 * 说明这个 Tile 是第一次被发现。
                 */
                if(set.add(pos)){

                    Tile tile =
                            Vars.world.tile(
                                    x,
                                    y
                            );

                    /*
                     * 如果这里刚好有矿物，
                     * 立即刷新对应 Floor chunk。
                     */
                    if(
                            tile != null &&
                                    tile.overlay()
                                            instanceof ToggleOreBlock
                    ){
                        recache(tile);
                    }
                }
            }
        }
    }


    // =========================================================
    // 勘探单位
    // =========================================================

    private static void updateSurveyUnits(){

        if(
                Vars.state == null ||
                        Vars.state.isMenu() ||
                        Vars.world == null
        ){
            return;
        }

        unitScanTimer +=
                Time.delta;

        if(
                unitScanTimer <
                        unitScanInterval
        ){
            return;
        }

        unitScanTimer = 0f;


        Groups.unit.each(unit -> {

            if(
                    !(unit.type
                            instanceof SurveyUnitType)
            ){
                return;
            }

            SurveyUnitType type =
                    (SurveyUnitType)unit.type;

            if(type.oreScanRadius <= 0){
                return;
            }


            int tileX =
                    (int)(
                            unit.x /
                                    Vars.tilesize
                    );

            int tileY =
                    (int)(
                            unit.y /
                                    Vars.tilesize
                    );

            if(
                    tileX < 0 ||
                            tileY < 0 ||
                            tileX >= Vars.world.width() ||
                            tileY >= Vars.world.height()
            ){
                return;
            }


            int currentPosition =
                    position(
                            tileX,
                            tileY
                    );

            int previousPosition =
                    lastUnitTiles.get(
                            unit.id,
                            -1
                    );


            /*
             * 只有进入新的 Tile 才扫描。
             */
            if(
                    previousPosition ==
                            currentPosition
            ){
                return;
            }

            lastUnitTiles.put(
                    unit.id,
                    currentPosition
            );


            reveal(
                    unit.team,
                    tileX,
                    tileY,
                    type.oreScanRadius
            );
        });
    }


    // =========================================================
    // Ore cache
    // =========================================================

    private static void rebuildOreIndex(){

        oreTiles.clear();

        if(
                Vars.world == null ||
                        Vars.world.tiles == null
        ){
            return;
        }

        for(Tile tile : Vars.world.tiles){

            if(
                    tile.overlay()
                            instanceof ToggleOreBlock
            ){

                oreTiles.add(tile);

                /*
                 * 读取存档以后：
                 *
                 * explored 已经恢复，
                 * 这里重新执行 drawBase。
                 */
                recache(tile);
            }
        }
    }


    /**
     * F8 切换时调用。
     */
    public static void recacheAllOres(){

        for(
                int i = 0;
                i < oreTiles.size;
                i++
        ){
            recache(
                    oreTiles.get(i)
            );
        }
    }


    private static void recache(
            Tile tile
    ){

        if(
                Vars.headless ||
                        Vars.renderer == null ||
                        Vars.renderer.blocks == null ||
                        Vars.renderer.blocks.floor == null
        ){
            return;
        }

        Vars.renderer.blocks.floor
                .recacheTile(tile);
    }


    // =========================================================
    // 数据
    // =========================================================

    private static IntSet getTeamSet(
            int teamId
    ){

        IntSet set =
                explored.get(teamId);

        if(set == null){

            set =
                    new IntSet();

            explored.put(
                    teamId,
                    set
            );
        }

        return set;
    }


    private static int position(
            int x,
            int y
    ){

        return x +
                y * Vars.world.width();
    }


    private static void clearRuntimeData(){

        explored.clear();
        oreTiles.clear();
        lastUnitTiles.clear();

        unitScanTimer = 0f;
    }
}
