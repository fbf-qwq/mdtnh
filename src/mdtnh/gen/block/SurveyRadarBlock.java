package mdtnh.gen.block;

import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;

public class SurveyRadarBlock
        extends Block{

    /**
     * 地质扫描半径，单位 Tile。
     *
     * 注意：
     * 这不是原版 fogRadius。
     */
    public int oreScanRadius = 30;


    public SurveyRadarBlock(
            String name
    ){
        super(name);

        update = true;
        hasPower = true;

        buildType =SurveyRadarBuild::new;
        buildVisibility = BuildVisibility.shown;
    }


    public class SurveyRadarBuild
            extends Building{

        private boolean wasWorking = false;

        private int lastTeam = -1;


        @Override
        public void updateTile(){

            super.updateTile();

            boolean working =
                    enabled &&
                            efficiency > 0.001f;


            /*
             * 通电：
             *
             * 第一次扫描一次。
             *
             * 如果建筑换队伍，
             * 为新队伍重新扫描。
             */
            if(
                    working &&
                            (
                                    !wasWorking ||
                                            lastTeam != team.id
                            )
            ){

                OreExplorationController.reveal(
                        team,
                        tile.x,
                        tile.y,
                        oreScanRadius
                );

                lastTeam =
                        team.id;
            }


            /*
             * 断电以后设回 false。
             *
             * 再次通电时会重新调用 reveal，
             * 但 IntSet 会自动忽略已经探索的 Tile。
             */
            wasWorking =
                    working;
        }
    }
}
